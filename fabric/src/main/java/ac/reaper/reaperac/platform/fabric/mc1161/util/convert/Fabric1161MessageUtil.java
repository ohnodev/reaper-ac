package ac.reaper.reaperac.platform.fabric.mc1161.util.convert;

import ac.reaper.reaperac.platform.fabric.utils.message.IFabricMessageUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class Fabric1161MessageUtil implements IFabricMessageUtil {
    @Override
    public Component textLiteral(String message) {
        return Component.literal(message);
    }

    @Override
    public void sendMessage(CommandSourceStack target, Component message, boolean overlay) {
        target.sendSuccess(() -> message, overlay);
    }
}
