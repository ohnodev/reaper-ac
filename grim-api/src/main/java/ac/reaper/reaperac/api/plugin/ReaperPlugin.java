package ac.reaper.reaperac.api.plugin;

/**
 * Reaper-branded alias for the plugin contract.
 * <p>
 * Extends {@link GrimPlugin} for binary/source compatibility while allowing
 * Reaper-facing APIs to use project-aligned naming.
 */
public interface ReaperPlugin extends GrimPlugin {
}
