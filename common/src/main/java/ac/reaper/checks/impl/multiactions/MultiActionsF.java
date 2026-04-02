package ac.reaper.checks.impl.multiactions;

import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.BlockPlaceCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.BlockBreak;
import ac.reaper.utils.anticheat.update.BlockPlace;
import ac.reaper.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;

import java.util.ArrayList;
import java.util.List;

@CheckData(name = "MultiActionsF", description = "Interacting with a block and an entity in the same tick", experimental = true)
public class MultiActionsF extends BlockPlaceCheck {
    private final List<String> flags = new ArrayList<>();
    private boolean entity, block;

    public MultiActionsF(ReaperPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(BlockPlace place) {
        block = true;
        if (entity) {
            if (!player.canSkipTicks()) {
                if (flagAndAlert("place") && shouldModifyPackets() && shouldCancel()) {
                    place.resync();
                }
            } else {
                flags.add("place");
            }
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            entity = true;
            if (block) {
                if (!player.canSkipTicks()) {
                    if (flagAndAlert("entity") && shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    flags.add("entity");
                }
            }
        }

        if (isTickPacket(event.getPacketType())) {
            block = entity = false;
        }
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action == DiggingAction.START_DIGGING || blockBreak.action == DiggingAction.FINISHED_DIGGING) {
            block = true;
            if (entity) {
                if (!player.canSkipTicks()) {
                    if (flagAndAlert("dig") && shouldModifyPackets()) {
                        blockBreak.cancel();
                    }
                } else {
                    flags.add("dig");
                }
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) return;

        if (player.isTickingReliablyFor(3)) {
            for (String verbose : flags) {
                flagAndAlert(verbose);
            }
        }

        flags.clear();
    }
}
