package ac.reaper.reaperac.checks.impl.misc;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.PacketCheck;
import ac.reaper.reaperac.checks.type.PostPredictionCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.update.PredictionComplete;
import ac.reaper.reaperac.utils.lists.EvictingQueue;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityAnimation;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Locale;

import static com.github.retrooper.packetevents.protocol.packettype.PacketType.Play.Client.*;

@CheckData(name = "Post")
public class Post extends Check implements PacketCheck, PostPredictionCheck {
    private final ArrayDeque<PacketTypeCommon> post = new ArrayDeque<>();
    // Due to 1.9+ missing the idle packet, we must queue flags
    // 1.8 clients will have the same logic for simplicity, although it's not needed
    private final List<String> flags = new EvictingQueue<>(10);
    private boolean sentFlying = false;
    private int isExemptFromSwingingCheck = Integer.MIN_VALUE;

    public Post(GrimPlayer playerData) {
        super(playerData);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
        if (!flags.isEmpty()) {
            // Okay, the user might be cheating, let's double check
            // 1.8 clients have the idle packet, and this shouldn't false on 1.8 clients
            // 1.9+ clients have predictions, which will determine if hidden tick skipping occurred
            if (player.isTickingReliablyFor(3)) {
                for (String flag : flags) {
                    flagAndAlert(flag);
                }
            }

            flags.clear();
        }
    }

    @Override
    public void onPacketSend(final PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.ENTITY_ANIMATION) {
            WrapperPlayServerEntityAnimation animation = new WrapperPlayServerEntityAnimation(event);
            if (animation.getEntityId() == player.entityID) {
                if (animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_MAIN_ARM ||
                        animation.getType() == WrapperPlayServerEntityAnimation.EntityAnimationType.SWING_OFF_HAND) {
                    isExemptFromSwingingCheck = player.lastTransactionSent.get();
                }
            }
        }
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (isTickPacket(event.getPacketType())) { // Don't count teleports or duplicates as movements
            post.clear();
            sentFlying = true;
        } else {
            // 1.13+ clients can click inventory outside tick loop, so we can't post check those two packets on 1.13+
            PacketTypeCommon packetType = event.getPacketType();

            if (isTransaction(packetType) && player.packetStateData.lastTransactionPacketWasValid) {
                if (sentFlying && !post.isEmpty()) {
                    flags.add(post.getFirst().toString().toLowerCase(Locale.ROOT).replace("_", " ") + " v" + player.getClientVersion().getReleaseName());
                }
                post.clear();
                sentFlying = false;
            } else if (
                    PLAYER_ABILITIES.equals(packetType) ||
                    HELD_ITEM_CHANGE.equals(packetType) ||
                    INTERACT_ENTITY.equals(packetType) ||
                    PLAYER_BLOCK_PLACEMENT.equals(packetType) ||
                    ATTACK.equals(packetType) ||
                    SPECTATE_ENTITY.equals(packetType) ||
                    USE_ITEM.equals(packetType) ||
                    PLAYER_DIGGING.equals(packetType)) {
                if (sentFlying) post.add(event.getPacketType());
            }
            else if (!ANIMATION.equals(packetType) && ENTITY_ACTION.equals(packetType)) {
                if (player.inVehicle()) {
                    return;
                }
                if (sentFlying) post.add(event.getPacketType());
            }
        }
    }
}
