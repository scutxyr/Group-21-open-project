package com.taobao.arthas.core.command.monitor200;

import com.taobao.arthas.core.command.Constants;
import com.taobao.arthas.core.shell.command.AnnotatedCommand;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.middleware.cli.annotations.Description;
import com.taobao.middleware.cli.annotations.Name;
import com.taobao.middleware.cli.annotations.Summary;

@Name("memory-leak")
@Summary("Detect memory leak")
@Description(Constants.EXAMPLE + "memory-leak --class java.lang.String --limit 10")
public class MemoryLeakCommand extends AnnotatedCommand {

    @Override
    public void process(CommandProcess process) {
        process.write("MemoryLeak command is not fully implemented yet.\n");
        process.end();
    }
}