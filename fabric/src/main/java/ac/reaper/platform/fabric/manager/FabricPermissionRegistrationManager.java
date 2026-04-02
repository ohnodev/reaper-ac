package ac.reaper.platform.fabric.manager;

import ac.reaper.platform.api.manager.PermissionRegistrationManager;
import ac.reaper.platform.api.permissions.PermissionDefaultValue;
import ac.reaper.platform.fabric.ReaperACFabricLoaderPlugin;
import ac.reaper.platform.fabric.sender.FabricSenderFactory;
import me.lucko.fabric.api.permissions.v0.Permissions;

import static ac.reaper.platform.fabric.sender.FabricSenderFactory.HAS_PERMISSIONS_API;

public class FabricPermissionRegistrationManager implements PermissionRegistrationManager {

    private final FabricSenderFactory fabricSenderFactory = ReaperACFabricLoaderPlugin.LOADER.getFabricSenderFactory();

    public FabricPermissionRegistrationManager() {
        registerPermission("reaper.exempt", PermissionDefaultValue.FALSE);
        registerPermission("reaper.nosetback", PermissionDefaultValue.FALSE);
        registerPermission("reaper.nomodifypacket", PermissionDefaultValue.FALSE);
        registerPermission("reaper.nosetback", PermissionDefaultValue.FALSE);
        registerPermission("reaper.alerts.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("reaper.verbose.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("reaper.brand.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("reaper.alerts.enable-on-join.silent", PermissionDefaultValue.FALSE);
        registerPermission("reaper.verbose.enable-on-join.silent", PermissionDefaultValue.FALSE);
        registerPermission("reaper.brand.enable-on-join.silent", PermissionDefaultValue.FALSE);
    }

    @Override
    public void registerPermission(String name, PermissionDefaultValue defaultValue) {
        fabricSenderFactory.registerPermissionDefault(name, defaultValue);
        if (HAS_PERMISSIONS_API)
            Permissions.check(ReaperACFabricLoaderPlugin.FABRIC_SERVER.createCommandSourceStack(), name);
    }
}
