package com.cardinalstar.cubicchunks.command;

import java.util.Locale;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import com.cardinalstar.cubicchunks.debug.CCProfiler;
import com.cardinalstar.cubicchunks.debug.CCProfiler.Snapshot;

public final class CommandProfile extends CubicCommandBase {

    public CommandProfile() {
        super(PermissionLevel.ALL);
    }

    @Override
    public String getCommandName() {
        return "profile";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cc profile <start|status|stop>";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length != 1) throw new CommandException(getCommandUsage(sender));
        if ("start".equalsIgnoreCase(args[0])) {
            CCProfiler.start();
            sender.addChatMessage(new ChatComponentText("CubicChunks streaming profile started"));
            return;
        }
        if ("status".equalsIgnoreCase(args[0])) {
            report(sender, CCProfiler.snapshot());
            return;
        }
        if ("stop".equalsIgnoreCase(args[0])) {
            report(sender, CCProfiler.stop());
            return;
        }
        throw new CommandException(getCommandUsage(sender));
    }

    private static void report(ICommandSender sender, Snapshot s) {
        double seconds = s.elapsedNanos / 1.0E9D;
        double mib = 1024D * 1024D;
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "CC profile: %.2fs, %,d cube packets, %.2f MiB raw / %.2f MiB compressed (%.1f%%)",
                    seconds,
                    s.cubePackets,
                    s.rawBytes / mib,
                    s.compressedBytes / mib,
                    s.rawBytes == 0 ? 0D : 100D * s.compressedBytes / s.rawBytes)));
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "server: encode %.1fms, LZ4 %.1fms, sync flush %.1fms across %,d flushes",
                    millis(s.encodeNanos),
                    millis(s.compressNanos),
                    millis(s.syncFlushNanos),
                    s.syncFlushes)));
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "client: LZ4/decode %.1fms, install %.1fms, %,d render invalidations",
                    millis(s.decodeNanos),
                    millis(s.installNanos),
                    s.renderInvalidations)));
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "loader: %.1fms active (%.1f%%), %,d requests / %,d loads, queue %,d (peak %,d)",
                    millis(s.eagerActiveNanos),
                    s.elapsedNanos == 0 ? 0D : 100D * s.eagerActiveNanos / s.elapsedNanos,
                    s.eagerRequests,
                    s.eagerLoadAttempts,
                    s.eagerQueueColumns,
                    s.peakEagerQueueColumns)));
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "classified: %,d air / %,d sealed / %,d generate; %,d lit",
                    s.classifiedAir,
                    s.classifiedSealed,
                    s.classifiedGenerate,
                    s.litCubes)));
        sender.addChatMessage(
            new ChatComponentText(
                String.format(
                    Locale.ROOT,
                    "renderer: %,d mesh rebuilds in %.1fms (%.1f%% of elapsed)",
                    s.renderBuilds,
                    millis(s.renderBuildNanos),
                    s.elapsedNanos == 0 ? 0D : 100D * s.renderBuildNanos / s.elapsedNanos)));
    }

    private static double millis(long nanos) {
        return nanos / 1.0E6D;
    }
}
