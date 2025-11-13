package com.taobao.arthas.core.shell.command;

import java.util.ArrayList;
import java.util.List;

public class ConfigCheckerCommandResolver implements CommandResolver {
    @Override
    public List<Command> commands() {
        // List<Command> commands = new ArrayList<Command>();
        // commands.add(Command.create(com.taobao.arthas.core.shell.command.impl.ConfigCheckerCommand.class));
        // return commands;
        return new ArrayList<Command>();
    }
}
