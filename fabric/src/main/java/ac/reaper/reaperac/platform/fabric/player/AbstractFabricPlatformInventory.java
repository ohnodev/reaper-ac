package ac.reaper.reaperac.platform.fabric.player;

import ac.reaper.reaperac.platform.api.player.PlatformInventory;
import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.reaper.reaperac.platform.fabric.utils.convert.IFabricConversionUtil;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


public abstract class AbstractFabricPlatformInventory implements PlatformInventory {

    private static final IFabricConversionUtil fabricConversionUtil = GrimACFabricLoaderPlugin.LOADER.getFabricConversionUtil();
    protected final AbstractFabricPlatformPlayer fabricPlatformPlayer;

    public AbstractFabricPlatformInventory(AbstractFabricPlatformPlayer fabricPlatformPlayer) {
        this.fabricPlatformPlayer = fabricPlatformPlayer;
    }

    @Override
    public ItemStack getItemInHand() {
        return fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getSelectedItem());
    }

    @Override
    public ItemStack getItemInOffHand() {
        return fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getItem(40));
    }

    @Override
    public ItemStack getStack(int bukkitSlot, int vanillaSlot) {
        return fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getItem(bukkitSlot));
    }

    @Override
    public ItemStack getHelmet() {
        return fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getItem(39));
    }

    @Override
    public ItemStack getChestplate() {
        return fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getItem(38));
    }

    @Override
    public ItemStack getLeggings() {
        return fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getItem(37));
    }

    @Override
    public ItemStack getBoots() {
        return fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getItem(36));
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] items = new ItemStack[fabricPlatformPlayer.fabricPlayer.inventory.getContainerSize()];
        for (int i = 0; i < fabricPlatformPlayer.fabricPlayer.inventory.getContainerSize(); i++) {
            items[i] = fabricConversionUtil.fromFabricItemStack(fabricPlatformPlayer.fabricPlayer.inventory.getItem(i));
        }
        return items;
    }

    @Override
    public String getNativeMainHandItemKey() {
        net.minecraft.world.item.ItemStack nativeHeld = fabricPlatformPlayer.fabricPlayer.inventory.getSelectedItem();
        if (nativeHeld == null || nativeHeld.isEmpty()) {
            return "minecraft:air";
        }
        return BuiltInRegistries.ITEM.getKey(nativeHeld.getItem()).toString();
    }

    @Override
    public Float getNativeMainHandDestroySpeed(String blockKey) {
        net.minecraft.world.item.ItemStack nativeHeld = fabricPlatformPlayer.fabricPlayer.inventory.getSelectedItem();
        if (nativeHeld == null || nativeHeld.isEmpty()) {
            return 1.0f;
        }
        BlockState state = resolveBlockState(blockKey);
        if (state == null) {
            return null;
        }
        return nativeHeld.getDestroySpeed(state);
    }

    @Override
    public Boolean isNativeMainHandCorrectToolForDrops(String blockKey) {
        net.minecraft.world.item.ItemStack nativeHeld = fabricPlatformPlayer.fabricPlayer.inventory.getSelectedItem();
        if (nativeHeld == null || nativeHeld.isEmpty()) {
            return false;
        }
        BlockState state = resolveBlockState(blockKey);
        if (state == null) {
            return null;
        }
        return nativeHeld.isCorrectToolForDrops(state);
    }

    private static BlockState resolveBlockState(String blockKey) {
        if (blockKey == null || blockKey.isBlank()) {
            return null;
        }
        String normalized = blockKey.contains(":") ? blockKey : "minecraft:" + blockKey;
        Identifier id = Identifier.tryParse(normalized);
        if (id == null) {
            return null;
        }
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        return block != null ? block.defaultBlockState() : null;
    }
}
