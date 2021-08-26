package xyz.oopsjpeg.gacha.command;

import discord4j.core.object.entity.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class CommandRateLimiter
{
    private final Map<String, Cycle> cycleMap = new HashMap<>();

    public Cycle getCycle(String id)
    {
        if (!cycleMap.containsKey(id))
            cycleMap.put(id, new Cycle());
        return cycleMap.get(id);
    }

    public boolean issueCommand(String id)
    {
        return getCycle(id).issueCommand();
    }

    public boolean issueCommand(User user)
    {
        return issueCommand(user.getId().asString());
    }

    public boolean warn(String id)
    {
        return getCycle(id).warn();
    }

    public boolean warn(User user)
    {
        return warn(user.getId().asString());
    }

    public static class Cycle
    {
        private LocalDateTime time;
        private int commands;
        private boolean warned;

        public void cycle()
        {
            time = LocalDateTime.now();
            commands = 0;
            warned = false;
        }

        public boolean isCycleComplete()
        {
            return time == null || LocalDateTime.now().isAfter(time.plusSeconds(5));
        }

        public boolean issueCommand()
        {
            // If there's no last cycle or 5 seconds have past since it
            if (isCycleComplete()) cycle();

            // If less than 3 commands have been issued this cycle
            if (commands < 3)
            {
                commands++;
                return true;
            }

            return false;
        }

        public boolean warn()
        {
            if (!warned)
            {
                warned = true;
                return true;
            }

            return false;
        }
    }
}
