package ac.reaper.checks.impl.breaking;

import ac.reaper.checks.Check;
import ac.reaper.checks.CheckData;
import ac.reaper.checks.type.BlockBreakCheck;
import ac.reaper.player.ReaperPlayer;
import ac.reaper.utils.anticheat.update.BlockBreak;
import ac.reaper.utils.math.ReaperMath;
import ac.reaper.utils.nmsutil.BlockBreakSpeed;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

import java.util.Set;

// Based loosely off of Hawk BlockBreakSpeedSurvival
// Also based loosely off of NoCheatPlus FastBreak
// Also based off minecraft wiki: https://minecraft.wiki/w/Breaking#Instant_breaking
@CheckData(name = "FastBreak", description = "Breaking blocks too quickly")
public class FastBreak extends Check implements BlockBreakCheck {

    // For some reason these states flag and I don't know why.
    // Better to just exempt to not annoy legit players.
    private static final Set<StateType> EXEMPT_STATES = Set.of();

    public FastBreak(ReaperPlayer playerData) {
        super(playerData);
    }

    // The block the player is currently breaking
    Vector3i targetBlockPosition = null;
    // The maximum amount of damage the player deals to the block
    //
    double maximumBlockDamage = 0;
    // The last time a finish digging packet was sent, to enforce 0.3-second delay after non-instabreak
    long lastFinishBreak = 0;
    // The time the player started to break the block, to know how long the player waited until they finished breaking the block
    long startBreak = 0;

    // The buffer to this check
    double blockBreakBalance = 0;
    double blockDelayBalance = 0;

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action == DiggingAction.START_DIGGING) {
            // Exempt all blocks that do not exist in the player version
            final WrappedBlockState defaultState = WrappedBlockState.getDefaultState(player.getClientVersion(), blockBreak.block.getType());
            if (defaultState.getType() == StateTypes.AIR || EXEMPT_STATES.contains(defaultState.getType())) {
                return;
            }
            WrappedBlockState block = blockBreak.block;

            startBreak = System.currentTimeMillis() - (targetBlockPosition == null ? 50 : 0); // ???
            targetBlockPosition = blockBreak.position;

            maximumBlockDamage = BlockBreakSpeed.getBlockDamage(player, block);

            double breakDelay = System.currentTimeMillis() - lastFinishBreak;

            if (breakDelay >= 275) { // Reduce buffer if "close enough"
                blockDelayBalance *= 0.9;
            } else { // Otherwise, increase buffer
                blockDelayBalance += 300 - breakDelay;
            }

            if (blockDelayBalance > 1000) { // If more than a second of advantage
                if (flagAndAlert("delay=" + breakDelay + "ms, type=" + blockBreak.block.getType()) && shouldModifyPackets()) {
                    blockBreak.cancel();
                }
            }

            clampBalance();
        }

        if (blockBreak.action == DiggingAction.FINISHED_DIGGING && targetBlockPosition != null) {
            double predictedTime = Math.ceil(1 / maximumBlockDamage) * 50;
            double realTime = System.currentTimeMillis() - startBreak;
            double diff = predictedTime - realTime;

            clampBalance();

            if (diff < 25) {  // Reduce buffer if "close enough"
                blockBreakBalance *= 0.9;
            } else { // Otherwise, increase buffer
                blockBreakBalance += diff;
            }

            if (blockBreakBalance > 1000) { // If more than a second of advantage
                if (flagAndAlert("diff=" + diff + "ms, balance=" + blockBreakBalance + "ms, type=" + blockBreak.block.getType()) && shouldModifyPackets()) {
                    blockBreak.cancel();
                }
            }

            // also set start time because the breaking netcode is fucked on 1.14.4+
            lastFinishBreak = startBreak = System.currentTimeMillis();
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        // Find the most optimal block damage using the animation packet, which is sent at least once a tick when breaking blocks
        // Select packet predicate by client protocol: animation for 1.9+, flying variants for legacy clients.
        if ((player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_9) ? event.getPacketType() == PacketType.Play.Client.ANIMATION : WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) && targetBlockPosition != null) {
            maximumBlockDamage = Math.max(maximumBlockDamage, BlockBreakSpeed.getBlockDamage(player, player.compensatedWorld.getBlock(targetBlockPosition)));
        }
    }

    private void clampBalance() {
        double balance = Math.max(1000, (player.getTransactionPing()));
        blockBreakBalance = ReaperMath.clamp(blockBreakBalance, -balance, balance); // Clamp not Math.max in case other logic changes
        blockDelayBalance = ReaperMath.clamp(blockDelayBalance, -balance, balance);
    }
}
