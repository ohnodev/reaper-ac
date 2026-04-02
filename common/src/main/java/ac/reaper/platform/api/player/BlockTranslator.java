package ac.reaper.platform.api.player;

public interface BlockTranslator {
    BlockTranslator IDENTITY = serverBlockId -> serverBlockId;

    int translate(int serverBlockId);
}
