package me.gacha.gacha.command.context;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.MessageChannel;
import me.gacha.gacha.Config;
import me.gacha.gacha.Util;
import reactor.core.publisher.Mono;

import java.util.Arrays;

public class Context
{
    private final MessageCreateEvent event;
    private final String alias;
    private final String[] args;

    public Context(final MessageCreateEvent event)
    {
        this.event = event;

        String[] split = event.getMessage().getContent().split(" ");
        String prefix = Config.get().getPrefix();
        this.alias = split[0].replaceFirst(prefix, "");
        this.args = Arrays.copyOfRange(split, 0, split.length);
    }

    public Mono<?> reply(String content)
    {
        return getChannel().flatMap(channel -> channel.createMessage(content));
    }

    public String getAlias()
    {
        return alias;
    }

    public String[] getArgs()
    {
        return args;
    }

    public String getArg(int index)
    {
        try
        {
            return getArgs()[index];
        }
        catch (IndexOutOfBoundsException err)
        {
            return null;
        }
    }

    public Mono<MessageChannel> getChannel()
    {
        return event.getMessage().getChannel();
    }

    public Mono<Guild> getGuild()
    {
        return getChannel().ofType(GuildChannel.class).flatMap(GuildChannel::getGuild);
    }

    public MessageCreateEvent getEvent()
    {
        return event;
    }
}
