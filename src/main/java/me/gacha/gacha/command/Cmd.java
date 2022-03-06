package me.gacha.gacha.command;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.PermissionSet;
import me.gacha.gacha.command.context.Context;
import me.gacha.gacha.command.exception.CommandException;
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

    public Mono<?> tryExecute(final Context context) throws CommandException
    {
        // Check if command is guild-only
        if (isGuildOnly())
        {
            MessageChannel msgChannel = context.getChannel().block();
            TextChannel txtChannel = Mono.just(msgChannel)
                    .ofType(TextChannel.class)
                    .blockOptional().orElse(null);

            // Check if channel is in a guild
            if (txtChannel == null)
                throw new CommandException("**" + getTitle() + "** only works in servers.");

            // Check if command has required perms
            if (hasRequiredPerms())
            {
                Snowflake userId = context.getUser().getId();
                PermissionSet userPerms = txtChannel.getEffectivePermissions(userId).block();

                // Check user has said required perms
                if (!userPerms.containsAll(getRequiredPerms()))
                    throw new CommandException("You don't have the required permission(s) for **" + getTitle() + "**.");
            }
        }

        return execute(context);
    }

    public abstract Mono<?> execute(final Context context) throws CommandException;

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
        // Imply we're guild only if there's required perms
        return guildOnly || hasRequiredPerms();
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
