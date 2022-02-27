package me.gacha.gacha.command;

import discord4j.rest.util.PermissionSet;
import me.gacha.gacha.command.context.Context;
import reactor.core.publisher.Mono;

public abstract class Cmd
{
    private final String name;
    private final String title;

    protected boolean guildOnly = false;
    protected PermissionSet requiredPerms = PermissionSet.none();

    public Cmd(final String name, final String title)
    {
        this.name = name;
        this.title = title;
    }

    public abstract Mono<?> execute(final Context context);

    public String getName()
    {
        return name;
    }

    public String getTitle()
    {
        return title;
    }

    public boolean isGuildOnly()
    {
        return guildOnly;
    }

    public PermissionSet getRequiredPerms()
    {
        return requiredPerms;
    }

    public boolean hasRequiredPerms()
    {
        return !requiredPerms.isEmpty();
    }
}
