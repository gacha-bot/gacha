package me.gacha.gacha.command.listener;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.PermissionSet;
import me.gacha.gacha.Config;
import me.gacha.gacha.EventListener;
import me.gacha.gacha.Replies;
import me.gacha.gacha.command.Cmd;
import me.gacha.gacha.command.Commands;
import me.gacha.gacha.command.context.Context;
import reactor.core.publisher.Mono;

public class TextCommandListener implements EventListener<MessageCreateEvent>
{
    @Override
    public Class<MessageCreateEvent> getEventType()
    {
        return MessageCreateEvent.class;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Mono<?> execute(final MessageCreateEvent event)
    {
        Message message = event.getMessage();
        MessageChannel channel = message.getChannel().block();
        String content = message.getContent();
        String prefix = Config.get().getPrefix();

        // Check if message starts with command prefix
        if (!content.startsWith(prefix)) return Mono.empty();

        // Split the message to get the alias
        String[] split = content.split(" ");
        String alias = split[0].replaceFirst(prefix, "");

        // Find the command from the provided alias
        Cmd cmd = Commands.find(alias);

        if (cmd == null) return Mono.empty();

        // Check if command is guild-only or has required permissions
        if (cmd.isGuildOnly() || cmd.hasRequiredPerms())
        {
            TextChannel textChannel = message.getChannel().ofType(TextChannel.class).blockOptional().orElse(null);

            // Check if channel is in a guild
            if (textChannel == null)
                return Replies.failure(channel, "**" + cmd.getTitle() + "** only works in servers.");

            // Check required permissions if necessary
            if (cmd.hasRequiredPerms())
            {
                Snowflake userId = message.getAuthor().map(User::getId).orElse(null);
                PermissionSet perms = textChannel.getEffectivePermissions(userId).block();

                // Check user has required permissions
                if (!perms.containsAll(cmd.getRequiredPerms()))
                    return Replies.failure(channel, "You don't have the required permission(s) for **" + cmd.getTitle() + "**.");
            }
        }

        return cmd.execute(new Context(event));
    }
}
