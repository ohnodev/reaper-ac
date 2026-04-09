package ac.reaper.reaperac.utils.nmsutil;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.data.tags.SyncedTag;
import ac.reaper.reaperac.utils.data.tags.SyncedTags;
import ac.reaper.reaperac.utils.enums.FluidTag;
import ac.reaper.reaperac.utils.latency.CompensatedInventory;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTool;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType.Mapped;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

@UtilityClass
public class BlockBreakSpeed {
    private static final boolean DEBUG_MINING_TRACE =
            Boolean.getBoolean("reaper.debug.mining.trace");
    // temporary hardcode to workaround PE bug https://github.com/retrooper/packetevents/issues/1217; see https://github.com/GrimAnticheat/Grim/issues/2117
    private static final Set<StateType> HARVESTABLE_TYPES_1_21_4 = Sets.newHashSet(
            StateTypes.BELL,
            StateTypes.LANTERN,
            StateTypes.SOUL_LANTERN,
            StateTypes.COPPER_DOOR,
            StateTypes.EXPOSED_COPPER_DOOR,
            StateTypes.OXIDIZED_COPPER_DOOR,
            StateTypes.WEATHERED_COPPER_DOOR,
            StateTypes.WAXED_COPPER_DOOR,
            StateTypes.WAXED_EXPOSED_COPPER_DOOR,
            StateTypes.WAXED_OXIDIZED_COPPER_DOOR,
            StateTypes.WAXED_WEATHERED_COPPER_DOOR,
            StateTypes.IRON_DOOR,
            StateTypes.HEAVY_WEIGHTED_PRESSURE_PLATE,
            StateTypes.LIGHT_WEIGHTED_PRESSURE_PLATE,
            StateTypes.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            StateTypes.STONE_PRESSURE_PLATE,
            StateTypes.BREWING_STAND,
            StateTypes.ENDER_CHEST
    );

    record ToolSpeedData(float speedMultiplier, boolean isCorrectToolForDrop) {
    }

    public static double getBlockDamage(GrimPlayer player, WrappedBlockState block) {
        return getBlockDamage(player, toolStackForMiningSimulation(player), block.getType());
    }

    /**
     * Prefer the packet-tracked held item for dig-time correctness, because START/FINISH digging arrives on
     * Netty and slot selection updates are mirrored there. If the platform stack is the same item type and has
     * richer TOOL component data, use it to keep modern mining rules accurate.
     */
    private static ItemStack toolStackForMiningSimulation(GrimPlayer player) {
        return MiningToolUtils.resolveEffectiveToolStack(
                player.inventory.getPacketTrackedHeldItem(),
                player.inventory.getNativeKeyMainHandStack());
    }

    public static double getBlockDamage(GrimPlayer player, ItemStack tool, StateType block) {
        // GET destroy speed
        // Starts with itemstack get destroy speed
        ItemType toolType = tool.getType();

        if (player.gamemode == GameMode.CREATIVE) {
            return tool.getComponent(ComponentTypes.TOOL)
                    .map(ItemTool::isCanDestroyBlocksInCreative)
                    .orElse(true) ? 1 : 0;
        }

        float blockHardness = block.getHardness();

        if (blockHardness == -1) return 0; // Unbreakable block

        final ToolSpeedData toolSpeedData = getModernToolSpeedData(player, tool, block);

        final float speedMultiplier = getSpeedMultiplierFromToolData(player, tool, toolSpeedData);

        final boolean canHarvest = !block.isRequiresCorrectTool()
                || toolSpeedData.isCorrectToolForDrop
                || inferCorrectToolFromTags(player, toolType, block)
                // temporary hardcode to workaround PE bug https://github.com/retrooper/packetevents/issues/1217; see https://github.com/GrimAnticheat/Grim/issues/2091
                || harvestableHardcoded(block);

        float damage = speedMultiplier / blockHardness;
        damage /= canHarvest ? 30F : 100F;

        // Keep this trace active while diagnosing 26.2 mining mismatches.
        if (DEBUG_MINING_TRACE && damage > 0 && block.isRequiresCorrectTool()) {
            double predictedMs = Math.ceil(1.0 / damage) * 50.0;
            if (predictedMs >= 5000) {
                CompensatedInventory compensatedInventory = player.inventory;
                ItemStack packetHeld = ItemStack.EMPTY;
                if (compensatedInventory != null) {
                    ItemStack tracked = compensatedInventory.getPacketTrackedHeldItem();
                    if (tracked != null) {
                        packetHeld = tracked;
                    }
                }
                ItemStack effectiveHeld = ItemStack.EMPTY;
                if (compensatedInventory != null) {
                    ItemStack held = compensatedInventory.getHeldItem();
                    if (held != null) {
                        effectiveHeld = held;
                    }
                }
                ItemStack platformHeld = ItemStack.EMPTY;
                if (player.platformPlayer != null) {
                    ItemStack fromPlatform = player.platformPlayer.getInventory().getItemInHand();
                    if (fromPlatform != null) {
                        platformHeld = fromPlatform;
                    }
                }
                int selected = compensatedInventory != null && compensatedInventory.inventory != null
                        ? compensatedInventory.inventory.getSelected()
                        : -1;
                int lastSelected = player.packetStateData != null ? player.packetStateData.lastSlotSelected : -1;
                boolean packetInvActive = compensatedInventory != null && compensatedInventory.isPacketInventoryActive;
                if (player.user != null) {
                    LogUtil.warn(String.format(
                            "[TRACE][mining-tool] player=%s block=%s hardness=%.2f predicted=%.0fms speedMul=%.2f canHarvest=%s "
                                    + "toolArg=%s(hasTOOL=%s,empty=%s) packetHeld=%s(hasTOOL=%s,empty=%s,selected=%d,lastSelected=%d) "
                                    + "effectiveHeld=%s(hasTOOL=%s,empty=%s,packetInvActive=%s) "
                                    + "platformHeld=%s(hasTOOL=%s,empty=%s) correctForDrop=%s inferTags=%s",
                            player.user.getName(),
                            block.getName(),
                            blockHardness,
                            predictedMs,
                            speedMultiplier,
                            canHarvest,
                            toolType != null ? toolType.getName() : "null",
                            tool.hasComponent(ComponentTypes.TOOL),
                            tool.isEmpty(),
                            packetHeld.getType().getName(),
                            packetHeld.hasComponent(ComponentTypes.TOOL),
                            packetHeld.isEmpty(),
                            selected,
                            lastSelected,
                            effectiveHeld.getType().getName(),
                            effectiveHeld.hasComponent(ComponentTypes.TOOL),
                            effectiveHeld.isEmpty(),
                            packetInvActive,
                            platformHeld.getType().getName(),
                            platformHeld.hasComponent(ComponentTypes.TOOL),
                            platformHeld.isEmpty(),
                            toolSpeedData.isCorrectToolForDrop,
                            inferCorrectToolFromTags(player, toolType, block)));
                }
            }
        }

        return damage;
    }

    private static float getSpeedMultiplierFromToolData(GrimPlayer player, ItemStack tool, ToolSpeedData data) {
        float speedMultiplier = data.speedMultiplier;

        if (speedMultiplier > 1.0f) {
            speedMultiplier += (float) player.compensatedEntities.self.getAttributeValue(Attributes.MINING_EFFICIENCY);
        }

        OptionalInt digSpeed = player.compensatedEntities.getPotionLevelForSelfPlayer(PotionTypes.HASTE);
        OptionalInt conduit = player.compensatedEntities.getPotionLevelForSelfPlayer(PotionTypes.CONDUIT_POWER);

        if (digSpeed.isPresent() || conduit.isPresent()) {
            int hasteLevel = Math.max(digSpeed.isEmpty() ? 0 : digSpeed.getAsInt(), conduit.isEmpty() ? 0 : conduit.getAsInt());
            speedMultiplier *= (float) (1 + (0.2 * (hasteLevel + 1)));
        }

        OptionalInt miningFatigue = player.compensatedEntities.getPotionLevelForSelfPlayer(PotionTypes.MINING_FATIGUE);

        if (miningFatigue.isPresent()) {
            switch (miningFatigue.getAsInt()) {
                case 0:
                    speedMultiplier *= 0.3f;
                    break;
                case 1:
                    speedMultiplier *= 0.09f;
                    break;
                case 2:
                    speedMultiplier *= 0.0027f;
                    break;
                default:
                    speedMultiplier *= 0.00081f;
            }
        }

        speedMultiplier *= (float) player.compensatedEntities.self.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);

        if (player.isEyeInFluid(FluidTag.WATER)) {
            speedMultiplier *= (float) player.compensatedEntities.self.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
        }

        if (!player.packetStateData.packetPlayerOnGround) {
            speedMultiplier /= 5;
        }

        return speedMultiplier;
    }

    private static ToolSpeedData getModernToolSpeedData(GrimPlayer player, ItemStack tool, StateType block) {
        // Prefer native platform mining metadata when available.
        // This keeps 26.2 behavior aligned with vanilla when PacketEvents item components drift.
        if (player.platformPlayer != null && player.platformPlayer.getInventory() != null) {
            String blockKey = block.getName();
            Float nativeSpeed = player.platformPlayer.getInventory().getNativeMainHandDestroySpeed(blockKey);
            if (nativeSpeed != null) {
                Boolean nativeCorrect = player.platformPlayer.getInventory().isNativeMainHandCorrectToolForDrops(blockKey);
                return new ToolSpeedData(nativeSpeed, nativeCorrect != null && nativeCorrect);
            }
        }

        Optional<ItemTool> toolComponentOpt = tool.getComponent(ComponentTypes.TOOL);
        if (toolComponentOpt.isEmpty()) {
            // ItemStacks synthesized from native registry keys may not carry runtime component payloads.
            // Fall back to ItemType default components for the player's protocol version.
            try {
                var components = tool.getType().getComponents(player.getClientVersion());
                if (components != null) {
                    ItemTool typeTool = components.get(ComponentTypes.TOOL);
                    if (typeTool != null) {
                        toolComponentOpt = Optional.of(typeTool);
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // Non-release/snapshot client version not supported by PE type component map.
            }
        }
        float speedMultiplier = 1.0f;
        boolean isCorrectToolForDrop = false;
        if (toolComponentOpt.isPresent()) {
            ItemTool itemTool = toolComponentOpt.get();

            // Initialize with final default values. These will be used if the loop doesn't find a value.
            // isCorrectToolForDrop is already set to false, no need to set again as default
            speedMultiplier = itemTool.getDefaultMiningSpeed();

            boolean speedFound = false;
            boolean dropsFound = false;

            for (ItemTool.Rule rule : itemTool.getRules()) {
                MappedEntitySet<StateType.Mapped> predicate = rule.getBlocks();
                ResourceLocation tagKey = predicate.getTagKey();
                boolean isMatch;

                // First, determine if the current rule even applies to this block.
                if (tagKey != null) {
                    SyncedTag<StateType> playerTag = player.tagManager.block(tagKey);
                    BlockTags staticTag = BlockTags.getByName(tagKey.getKey());
                    isMatch = (playerTag != null && playerTag.matchesBlock(block))
                            || (staticTag != null && staticBlockTagContains(staticTag, block));
                } else {
                    isMatch = ruleBlockListMatches(predicate, block);
                }

                // If the rule matches the block, check if we still need its properties.
                if (isMatch) {
                    // Check for speed if we haven't found it yet.
                    if (!speedFound && rule.getSpeed() != null) {
                        speedMultiplier = rule.getSpeed();
                        speedFound = true;
                    }

                    // Check for drops if we haven't found it yet.
                    if (!dropsFound && rule.getCorrectForDrops() != null) {
                        isCorrectToolForDrop = rule.getCorrectForDrops();
                        dropsFound = true;
                    }
                }

                if (speedFound && dropsFound) {
                    break;
                }
            }
        }
        return new ToolSpeedData(speedMultiplier, isCorrectToolForDrop);
    }

    private static boolean inferCorrectToolFromTags(GrimPlayer player, ItemType toolType, StateType block) {
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.PICKAXE)) {
            if (!isInTag(player, SyncedTags.MINEABLE_PICKAXE, BlockTags.MINEABLE_PICKAXE, block)) {
                return false;
            }
            int tier = getPickaxeTier(toolType);
            if (tier < 0) {
                return false;
            }
            if (tier < 3 && isInTag(player, SyncedTags.NEEDS_DIAMOND_TOOL, BlockTags.NEEDS_DIAMOND_TOOL, block)) {
                return false;
            }
            if (tier < 2 && isInTag(player, SyncedTags.NEEDS_IRON_TOOL, BlockTags.NEEDS_IRON_TOOL, block)) {
                return false;
            }
            return tier >= 1 || !isInTag(player, SyncedTags.NEEDS_STONE_TOOL, BlockTags.NEEDS_STONE_TOOL, block);
        }

        if (toolType.hasAttribute(ItemTypes.ItemAttribute.AXE)) {
            return isInTag(player, SyncedTags.MINEABLE_AXE, BlockTags.MINEABLE_AXE, block);
        }
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.SHOVEL)) {
            return isInTag(player, SyncedTags.MINEABLE_SHOVEL, BlockTags.MINEABLE_SHOVEL, block);
        }
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.HOE)) {
            return isInTag(player, SyncedTags.MINEABLE_HOE, BlockTags.MINEABLE_HOE, block);
        }
        return false;
    }

    private static int getPickaxeTier(ItemType toolType) {
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.NETHERITE_TIER)) return 4;
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.DIAMOND_TIER)) return 3;
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.IRON_TIER)) return 2;
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.STONE_TIER)) return 1;
        if (toolType.hasAttribute(ItemTypes.ItemAttribute.WOOD_TIER) || toolType.hasAttribute(ItemTypes.ItemAttribute.GOLD_TIER)) return 0;
        // 26.x: copper_pickaxe only has ItemAttribute.PICKAXE in PacketEvents — treat as stone-tier harvest for tag inference.
        if (toolType == ItemTypes.COPPER_PICKAXE) return 1;
        return -1;
    }

    private static boolean isInTag(GrimPlayer player, ResourceLocation syncedTag, com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags defaultTag, StateType block) {
        SyncedTag<StateType> synced = player.tagManager.block(syncedTag);
        return (synced != null && synced.matchesBlock(block)) || staticBlockTagContains(defaultTag, block);
    }

    /**
     * {@link BlockTags} uses {@link java.util.HashSet} with {@link StateType#equals(Object)}, which compares
     * hardness, blast resistance, and more. Runtime/registry {@link StateType} instances often differ from static
     * {@code StateTypes.*} used when tags were built, so we also match by {@link StateType#getName()}.
     */
    private static boolean staticBlockTagContains(BlockTags tag, StateType block) {
        if (tag.contains(block)) {
            return true;
        }
        String name = block.getName();
        for (StateType listed : tag.getStates()) {
            if (listed.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean ruleBlockListMatches(MappedEntitySet<Mapped> predicate, StateType block) {
        List<Mapped> entities = predicate.getEntities();
        if (entities == null) {
            return false;
        }
        Mapped mapped = block.getMapped();
        if (entities.contains(mapped)) {
            return true;
        }
        String name = block.getName();
        for (Mapped m : entities) {
            if (m.getName().getKey().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean harvestableHardcoded(StateType block) {
        if (HARVESTABLE_TYPES_1_21_4.contains(block)) {
            return true;
        }
        String name = block.getName();
        for (StateType listed : HARVESTABLE_TYPES_1_21_4) {
            if (listed.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
