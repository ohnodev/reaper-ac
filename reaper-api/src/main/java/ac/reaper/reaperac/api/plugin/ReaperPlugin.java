package ac.reaper.reaperac.api.plugin;

import java.io.File;
import java.util.logging.Logger;

public interface ReaperPlugin {

    ReaperPluginDescription getDescription();

    Logger getLogger();

    File getDataFolder();
}
