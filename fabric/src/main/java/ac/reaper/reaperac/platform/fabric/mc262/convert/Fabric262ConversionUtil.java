package ac.reaper.reaperac.platform.fabric.mc262.convert;

import ac.reaper.reaperac.platform.fabric.GrimACFabricLoaderPlugin;
import ac.reaper.reaperac.platform.fabric.utils.convert.FabricItemStackConversion;
import ac.reaper.reaperac.platform.fabric.utils.convert.FabricTextConversion;
import ac.reaper.reaperac.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.mojang.serialization.JsonOps;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class Fabric262ConversionUtil implements IFabricConversionUtil {

    @Override
    public ItemStack fromFabricItemStack(net.minecraft.world.item.ItemStack fabricStack) {
        return FabricItemStackConversion.peItemStackFromNative(fabricStack);
    }

    @Override
    public net.minecraft.network.chat.Component toNativeText(Component component) {
        try {
            return ComponentSerialization.CODEC.decode(
                    GrimACFabricLoaderPlugin.FABRIC_SERVER.registryAccess().createSerializationContext(JsonOps.INSTANCE),
                    GsonComponentSerializer.gson().serializeToTree(component)
            ).getOrThrow(IllegalArgumentException::new).getFirst();
        } catch (RuntimeException e) {
            LogUtil.error(
                    "Failed to decode Adventure Component with server registry context: " + String.valueOf(component),
                    e);
            return FabricTextConversion.parseAdventureToNative(component);
        }
    }
}
