package me.gacha.gacha.command;

import me.gacha.gacha.command.impl.CreateRuneCmd;
import me.gacha.gacha.command.impl.CreateTeamCmd;
import me.gacha.gacha.command.impl.CreateUnitCmd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Commands
{
    private static final Logger log = LoggerFactory.getLogger(Commands.class);

    private static Map<String, Cmd> cmdMap;

    public static void init()
    {
        cmdMap = new HashMap<>();

        log.info("Adding commands");
        add(new CreateUnitCmd());
        add(new CreateRuneCmd());
        add(new CreateTeamCmd());
    }

    private static void add(Cmd cmd)
    {
        log.info("Adding command of type " + cmd.getClass().getSimpleName());
        cmdMap.put(cmd.getName(), cmd);
    }

    public static Cmd find(String alias)
    {
        return cmdMap.get(alias);
    }
}
