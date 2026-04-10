package ac.reaper.reaperac.manager.init.start;

import ac.reaper.reaperac.GrimAPI;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.reflection.ReflectionUtils;
import com.viaversion.viaversion.api.Via;

public class ViaVersion implements StartableInitable {
    public static final boolean isAvailable = ReflectionUtils.hasClass("com.viaversion.viaversion.api.Via");
    @Override
    public void start() {
        if (!isAvailable && ReflectionUtils.hasClass("us.myles.ViaVersion.api.Via")) {
            LogUtil.error("Using unsupported ViaVersion 4.0 API, update ViaVersion to 5.0");
            return;
        }

        if (GrimAPI.INSTANCE.getPluginManager().getPlugin("ViaBackwards") != null) {
            LogUtil.warn("GrimAC has detected that you have installed ViaBackwards on a 1.21.2+ server.");
            LogUtil.warn("This setup is currently unsupported and you will experience issues with older clients using vehicles.");
        }
    }
}
