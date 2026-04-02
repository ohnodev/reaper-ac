package ac.reaper.platform.fabric.mc1216.convert;

import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.utils.anticheat.LogUtil;
import com.mojang.serialization.JsonOps;
import io.github.retrooper.packetevents.adventure.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class Fabric1216ConversionUtil extends ac.reaper.platform.fabric.mc1205.convert.Fabric1205ConversionUtil {

    @Override
    public net.minecraft.network.chat.Component toNativeText(Component component) {
        try {
            return ComponentSerialization.CODEC.decode(
                    ReaperACFabricLoaderPlugin.FABRIC_SERVER.registryAccess().createSerializationContext(JsonOps.INSTANCE),
                    GsonComponentSerializer.gson().serializeToTree(component)
            ).getOrThrow(IllegalArgumentException::new).getFirst();
        } catch (RuntimeException e) {
            LogUtil.error(
                    "Failed to decode Adventure Component with server registry context: " + String.valueOf(component),
                    e);
            return super.toNativeText(component);
        }
    }
}
