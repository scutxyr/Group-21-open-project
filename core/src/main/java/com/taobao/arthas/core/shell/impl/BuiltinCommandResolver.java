package com.taobao.arthas.core.shell.impl;

import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandBuilder;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.command.impl.ConfigCheckerCommand;
import com.taobao.arthas.core.shell.command.internal.GrepHandler;
import com.taobao.arthas.core.shell.command.internal.PlainTextHandler;
import com.taobao.arthas.core.shell.command.internal.WordCountHandler;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.handlers.NoOpHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * @author beiwei30 on 23/11/2016.
 */
class BuiltinCommandResolver implements CommandResolver {

    private Handler<CommandProcess> handler;

    public BuiltinCommandResolver() {
        this.handler = new NoOpHandler<CommandProcess>();
        System.out.println("=== BuiltinCommandResolver 初始化 ===");
    }

    @Override
    public List<Command> commands() {
        List<Command> commands = new ArrayList<Command>();

        commands.add(CommandBuilder.command("exit").processHandler(handler).build());
        commands.add(CommandBuilder.command("quit").processHandler(handler).build());
        commands.add(CommandBuilder.command("jobs").processHandler(handler).build());
        commands.add(CommandBuilder.command("fg").processHandler(handler).build());
        commands.add(CommandBuilder.command("bg").processHandler(handler).build());
        commands.add(CommandBuilder.command("kill").processHandler(handler).build());
        commands.add(CommandBuilder.command(PlainTextHandler.NAME).processHandler(handler).build());
        commands.add(CommandBuilder.command(GrepHandler.NAME).processHandler(handler).build());
        commands.add(CommandBuilder.command(WordCountHandler.NAME).processHandler(handler).build());
        commands.add(Command.create(ConfigCheckerCommand.class));

        return commands;
    }
}
