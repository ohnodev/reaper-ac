package ac.reaper.reaperac.checks.impl.breaking;

import ac.reaper.reaperac.checks.Check;
import ac.reaper.reaperac.checks.CheckData;
import ac.reaper.reaperac.checks.type.BlockBreakCheck;
import ac.reaper.reaperac.player.GrimPlayer;
import ac.reaper.reaperac.utils.anticheat.MessageUtil;
import ac.reaper.reaperac.utils.anticheat.update.BlockBreak;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;

import static ac.reaper.reaperac.utils.nmsutil.BlockBreakSpeed.getBlockDamage;

@CheckData(name = "WrongBreak")
public class WrongBreak extends Check implements BlockBreakCheck {
    private final int exemptedY;

    {
        player.getClientVersion();
        exemptedY = -1;
    }

    private boolean lastBlockWasInstantBreak = false;
    private Vector3i lastBlock, lastCancelledBlock, lastLastBlock = null;

    public WrongBreak(final GrimPlayer player) {
        super(player);
    }

    // The client sometimes sends a wierd cancel packet
    private boolean shouldExempt(final WrappedBlockState block, int yPos) {
        // lastLastBlock is always null when this happens, and lastBlock isn't
        if (lastLastBlock != null || lastBlock == null)
            return false;

        // on pre 1.14.4 clients, the YPos of this packet is always the same
        player.getClientVersion();

        // and if this block is not an instant break
        player.getClientVersion();
        return getBlockDamage(player, block) < 1;
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action == DiggingAction.START_DIGGING) {
            final Vector3i pos = blockBreak.position;

            lastBlockWasInstantBreak = getBlockDamage(player, blockBreak.block) >= 1;
            lastCancelledBlock = null;
            lastLastBlock = lastBlock;
            lastBlock = pos;
        }

        if (blockBreak.action == DiggingAction.CANCELLED_DIGGING) {
            final Vector3i pos = blockBreak.position;

            if (!shouldExempt(blockBreak.block, pos.y) && !pos.equals(lastBlock)) {
                // https://github.com/GrimAnticheat/Grim/issues/1512
                player.getClientVersion();
                if (!lastBlockWasInstantBreak && pos.equals(lastCancelledBlock)) {
                    if (flagAndAlert("action=CANCELLED_DIGGING" + ", last=" + MessageUtil.toUnlabledString(lastBlock) + ", pos=" + MessageUtil.toUnlabledString(pos))) {
                        if (shouldModifyPackets()) {
                            blockBreak.cancel();
                        }
                    }
                }
            }

            lastCancelledBlock = pos;
            lastLastBlock = null;
            lastBlock = null;
            return;
        }

        if (blockBreak.action == DiggingAction.FINISHED_DIGGING) {
            final Vector3i pos = blockBreak.position;

            // when a player looks away from the mined block, they send a cancel, and if they look at it again, they don't send another start. (thanks mojang!)
            if (!pos.equals(lastCancelledBlock) && !lastBlockWasInstantBreak && !pos.equals(lastBlock)) {
                if (flagAndAlert("action=FINISHED_DIGGING" + ", last=" + MessageUtil.toUnlabledString(lastBlock) + ", pos=" + MessageUtil.toUnlabledString(pos))) {
                    if (shouldModifyPackets()) {
                        blockBreak.cancel();
                    }
                }
            }

            // 1.14.4+ clients don't send another start break in protected regions
            player.getClientVersion();
        }
    }
}
