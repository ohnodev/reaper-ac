package ac.reaper.reaperac.utils.anticheat.update;

import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.LogUtil;
import ac.reaper.reaperac.utils.collisions.HitboxData;
import ac.reaper.reaperac.utils.collisions.datatypes.CollisionBox;
import ac.reaper.reaperac.utils.collisions.datatypes.SimpleCollisionBox;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.util.Vector3i;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public final class BlockBreak {
    public final Vector3i position;
    public final BlockFace face;
    public final int faceId;
    public final DiggingAction action;
    public final int sequence;
    public final WrappedBlockState block;
    private final GrimPlayer player;
    @Getter
    private boolean cancelled;

    public BlockBreak(GrimPlayer player, Vector3i position, BlockFace face, int faceId, DiggingAction action, int sequence, WrappedBlockState block) {
        this.player = player;
        this.position = position;
        this.face = face;
        this.faceId = faceId;
        this.action = action;
        this.sequence = sequence;
        this.block = block;
    }

    public void cancel() {
        traceCancel("unspecified");
        this.cancelled = true;
    }

    public void cancel(String reason) {
        traceCancel(reason);
        this.cancelled = true;
    }

    public SimpleCollisionBox getCombinedBox() {
        CollisionBox placedOn = HitboxData.getBlockHitbox(player, player.inventory.getHeldItem().getType().getPlacedType(), player.getClientVersion(), block, true, position.x, position.y, position.z);

        List<SimpleCollisionBox> boxes = new ArrayList<>();
        placedOn.downCast(boxes);

        SimpleCollisionBox combined = new SimpleCollisionBox(position.x, position.y, position.z);
        for (SimpleCollisionBox box : boxes) {
            double minX = Math.max(box.minX, combined.minX);
            double minY = Math.max(box.minY, combined.minY);
            double minZ = Math.max(box.minZ, combined.minZ);
            double maxX = Math.min(box.maxX, combined.maxX);
            double maxY = Math.min(box.maxY, combined.maxY);
            double maxZ = Math.min(box.maxZ, combined.maxZ);
            combined = new SimpleCollisionBox(minX, minY, minZ, maxX, maxY, maxZ);
        }

        return combined;
    }

    private void traceCancel(String reason) {
        String state = block.getType().getName();
        if (!isSulfurFamily(state)) {
            return;
        }
        StateType t = block.getType();
        LogUtil.info("[TRACE][break-cancel] user=" + player.user.getName() + "/" + player.user.getUUID()
                + " check=" + resolveCheckName()
                + " reason=" + reason
                + " action=" + action
                + " pos=" + position
                + " face=" + faceId
                + " seq=" + sequence
                + " state=" + state
                + " hardness=" + t.getHardness());
    }

    private String resolveCheckName() {
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (className.startsWith("ac.reaper.reaperac.checks.")) {
                int idx = className.lastIndexOf('.');
                String simple = idx >= 0 ? className.substring(idx + 1) : className;
                return simple + "#" + element.getMethodName();
            }
        }
        return "unknown";
    }

    private boolean isSulfurFamily(String stateName) {
        String normalized = stateName.toLowerCase();
        return normalized.contains("sulfur") || normalized.contains("cinnabar");
    }
}
