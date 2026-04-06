package ac.reaper.reaperac.api.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ReaperPluginDescription {
    String getVersion();

    String getDescription();

    @NotNull Collection<String> getAuthors();
}
