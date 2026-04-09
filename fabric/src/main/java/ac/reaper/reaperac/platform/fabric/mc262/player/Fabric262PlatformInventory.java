package ac.reaper.reaperac.platform.fabric.mc262.player;

import ac.reaper.reaperac.platform.fabric.player.AbstractFabricPlatformInventory;
import ac.reaper.reaperac.platform.fabric.player.AbstractFabricPlatformPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class Fabric262PlatformInventory extends AbstractFabricPlatformInventory {
    public Fabric262PlatformInventory(AbstractFabricPlatformPlayer player) {
        super(player);
    }

    @Override
    public String getOpenInventoryKey() {
        AbstractContainerMenu handler = fabricPlatformPlayer.getNative().containerMenu;
        MenuType<?> type = getSafeType(handler);

        if (type == null) {
            if (handler instanceof InventoryMenu) {
                return "CRAFTING";
            } else if (isPlayerCreative()) {
                return "CREATIVE";
            }
            return normalizeFallbackKey(handler);
        }

        if (type == MenuType.CRAFTING) {
            return "CRAFTING";
        } else if (type == MenuType.GENERIC_9x1
                || type == MenuType.GENERIC_9x2
                || type == MenuType.GENERIC_9x3
                || type == MenuType.GENERIC_9x4
                || type == MenuType.GENERIC_9x5
                || type == MenuType.GENERIC_9x6) {
            return "CHEST";
        } else if (type == MenuType.GENERIC_3x3) {
            return "DISPENSER";
        } else {
            Identifier registryKey = (Identifier) getScreenID(type);
            if (registryKey != null) {
                return registryKey.getPath().toUpperCase(Locale.ROOT).replace('.', '_');
            }
            return normalizeFallbackKey(handler);
        }
    }

    protected String normalizeFallbackKey(AbstractContainerMenu handler) {
        String simpleName = handler.getClass().getSimpleName().replace('$', '_');
        return simpleName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toUpperCase(Locale.ROOT);
    }

    protected Object getScreenID(MenuType<?> type) {
        return BuiltInRegistries.MENU.getKey(type);
    }

    protected boolean isPlayerCreative() {
        return fabricPlatformPlayer.getNative().isCreative();
    }

    protected @Nullable MenuType<?> getSafeType(AbstractContainerMenu handler) {
        try {
            return handler.getType();
        } catch (UnsupportedOperationException e) {
            return null;
        }
    }
}
