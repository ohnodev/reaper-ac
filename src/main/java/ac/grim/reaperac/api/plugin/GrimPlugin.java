package ac.grim.reaperac.api.plugin;

import java.io.File;
import java.util.logging.Logger;

public interface GrimPlugin {

    GrimPluginDescription getDescription();

    Logger getLogger();

    File getDataFolder();
}
