package com.taobao.arthas.core.shell.command.biz;

import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.shell.handlers.Handler;

public class BitzStatsHandler implements Handler<CommandProcess> {
    @Override
    public void handle(CommandProcess process) {
        process.write("bitzstats command is working!\n");
        process.write("Business statistics feature is ready.\n");
        process.end();
    }
}
