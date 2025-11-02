package com.taobao.arthas.core.shell.impl;

import com.taobao.arthas.core.shell.command.Command;
import com.taobao.arthas.core.shell.command.CommandBuilder;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.command.CommandResolver;
import com.taobao.arthas.core.shell.command.internal.GrepHandler;
import com.taobao.arthas.core.shell.command.internal.PlainTextHandler;
import com.taobao.arthas.core.shell.command.internal.WordCountHandler;
import com.taobao.arthas.core.shell.command.biz.BitzStatsHandler;
import com.taobao.arthas.core.shell.handlers.Handler;
import com.taobao.arthas.core.shell.handlers.NoOpHandler;
import com.taobao.arthas.core.shell.command.biz.BitzStatsCommand;
import java.util.Arrays;
import java.util.List;

/**
 * @author beiwei30 on 23/11/2016.
 */
class BuiltinCommandResolver implements CommandResolver {

    private Handler<CommandProcess> handler;
    private Handler<CommandProcess> bitzStatsHandler;

    public BuiltinCommandResolver() {
        this.handler = new NoOpHandler<CommandProcess>();
        this.bitzStatsHandler = new BitzStatsHandler();
    }

    @Override
    public List<Command> commands() {
        return Arrays.asList(CommandBuilder.command("exit").processHandler(handler).build(),
                             CommandBuilder.command("quit").processHandler(handler).build(),
                             CommandBuilder.command("jobs").processHandler(handler).build(),
                             CommandBuilder.command("fg").processHandler(handler).build(),
                             CommandBuilder.command("bg").processHandler(handler).build(),
                             CommandBuilder.command("kill").processHandler(handler).build(),
                             CommandBuilder.command(PlainTextHandler.NAME).processHandler(handler).build(),
                             CommandBuilder.command(GrepHandler.NAME).processHandler(handler).build(),
                             CommandBuilder.command(WordCountHandler.NAME).processHandler(handler).build(),
                             CommandBuilder.command("bitzstats").processHandler(bitzStatsHandler).build());
    }
}
