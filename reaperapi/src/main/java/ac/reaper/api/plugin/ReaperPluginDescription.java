package ac.reaper.api.plugin;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ReaperPluginDescription {
    String getVersion();

    String getDescription();

    public @NotNull Collection<String> getAuthors();
}
