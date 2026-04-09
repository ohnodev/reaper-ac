package ac.reaper.reaperac.utils.nmsutil;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.data.tags.SyncedTag;
import ac.reaper.reaperac.utils.data.tags.SyncedTags;
import ac.reaper.reaperac.utils.enums.FluidTag;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.component.builtin.item.ItemTool;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.mapper.MappedEntitySet;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.google.common.collect.Sets;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

@UtilityClass
public class BlockBreakSpeed {
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
        ItemStack tool = player.inventory.getHeldItem();
        return getBlockDamage(player, tool, block.getType());
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
                || HARVESTABLE_TYPES_1_21_4.contains(block);

        float damage = speedMultiplier / blockHardness;
        damage /= canHarvest ? 30F : 100F;
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

    // TODO technically its possible to use packet level manipulation to enforce Tool rules on newer clients on older servers
    // But I've yet to hear of anyone even trying to do such a thing rather than just update the server
    // And we can't support this because we don't see the tool components/data before Via
    private static ToolSpeedData getModernToolSpeedData(GrimPlayer player, ItemStack tool, StateType block) {
        Optional<ItemTool> toolComponentOpt = tool.getComponent(ComponentTypes.TOOL);
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
                    isMatch = (playerTag != null && playerTag.contains(block))
                            || BlockTags.getByName(tagKey.getKey()).contains(block);
                } else {
                    isMatch = predicate.getEntities().contains(block.getMapped());
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
        return -1;
    }

    private static boolean isInTag(GrimPlayer player, ResourceLocation syncedTag, com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags defaultTag, StateType block) {
        SyncedTag<StateType> synced = player.tagManager.block(syncedTag);
        return (synced != null && synced.contains(block)) || defaultTag.contains(block);
    }


}
