package com.cardinalstar.cubicchunks.command;

import net.minecraft.command.ICommandSender;

public final class CommandCubicChunks extends SubCommandBase {

    public CommandCubicChunks() {
        super(PermissionLevel.ALL);
        addSubcommand(new CommandProfile());
    }

    @Override
    public String getCommandName() {
        return "cc";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cc <profile>";
    }
}
