package me.gacha.gacha.command.context;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateSpec;
import me.gacha.gacha.Config;
import me.gacha.gacha.Core;
import me.gacha.gacha.object.DBGuild;
import me.gacha.gacha.object.DBUser;
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
        this.args = Arrays.copyOfRange(split, 1, split.length);
    }

    public Mono<Message> reply(MessageCreateSpec spec)
    {
        return getChannel().flatMap(channel -> channel.createMessage(spec.withMessageReference(getMessage().getId())));
    }

    public Mono<?> reply(String content)
    {
        return reply(MessageCreateSpec.builder().content(content).build());
    }

    public String getAlias()
    {
        return alias;
    }

    public String[] getArgs()
    {
        return args;
    }

    public String getArg(int index, String defaultVal)
    {
        return hasArg(index) ? args[index] : defaultVal;
    }

    public boolean hasArg(int index)
    {
        return index >= 0 && index < args.length;
    }

    public Message getMessage()
    {
        return event.getMessage();
    }

    public Mono<MessageChannel> getChannel()
    {
        return getMessage().getChannel();
    }

    public User getUser()
    {
        return getMessage().getAuthor().get();
    }

    public DBUser getDBUser()
    {
        return Core.getDBUser(getUser());
    }

    public Mono<Guild> getGuild()
    {
        return event.getGuild();
    }

    public Mono<DBGuild> getDBGuild()
    {
        return getGuild().map(Core::getDBGuild);
    }

    public MessageCreateEvent getEvent()
    {
        return event;
    }
}
