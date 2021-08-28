package xyz.oopsjpeg.gacha.command;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.EmojiData;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.util.Arrays;

public class CommandCall
{
    private final CommandManager manager;
    private final String alias;
    private final String[] arguments;

    private final Snowflake messageId;
    private final Snowflake guildId;
    private final Snowflake channelId;
    private final Snowflake userId;

    public CommandCall(CommandManager manager, String alias, String[] arguments, Message message, Guild guild, MessageChannel channel, User user)
    {
        this.manager = manager;
        this.alias = alias;
        this.arguments = arguments;

        messageId = message == null ? null : message.getId();
        guildId = guild == null ? null : guild.getId();
        channelId = channel == null ? null : channel.getId();
        userId = user == null ? null : user.getId();
    }

    public static CommandCall of(CommandManager manager, Message message)
    {
        User user = message.getAuthor().orElse(null);

        if (user != null && !user.isBot())
        {
            Guild guild = message.getGuild().block();
            MessageChannel channel = message.getChannel().block();

            String content = message.getContent();
            String[] split = content.split(" ");

            if (split[0].toLowerCase().startsWith(manager.getPrefix().toLowerCase()))
            {
                String alias = split[0].replaceFirst(manager.getPrefix(), "");
                String[] args = Arrays.copyOfRange(split, 1, split.length);
                return new CommandCall(manager, alias, args, message, guild, channel, user);
            }
        }

        return null;
    }

    public static CommandCall of(CommandManager manager, Interaction interaction)
    {
        ApplicationCommandInteraction aci = interaction.getCommandInteraction().get();
        String alias = aci.getName().get();
        String[] args = aci.getOptions().stream()
                .map(o -> o.getValue()
                        .map(ApplicationCommandInteractionOptionValue::asString).get())
                .toArray(String[]::new);
        Guild guild = interaction.getGuild().block();
        MessageChannel channel = interaction.getChannel().block();
        User user = interaction.getUser();

        return new CommandCall(manager, alias, args, null, guild, channel, user);
    }

    public String format(Command command)
    {
        return manager.getPrefix() + command.getName();
    }

    public CommandManager getManager()
    {
        return manager;
    }

    public Core getCore()
    {
        return getManager().getCore();
    }

    public GatewayDiscordClient getGateway()
    {
        return getManager().getGateway();
    }

    public String getAlias()
    {
        return alias;
    }

    public String[] getArguments()
    {
        return arguments;
    }

    public String getArgument(int index)
    {
        return index >= 0 && getArguments().length > index ? getArguments()[index] : "";
    }

    public boolean hasArguments()
    {
        return getArguments().length != 0;
    }

    public String getLastArgument()
    {
        return getArgument(getArguments().length - 1);
    }

    public String getRawArguments()
    {
        return String.join(" ", getArguments());
    }

    public Message getMessage()
    {
        return getGateway().getMessageById(channelId, messageId).block();
    }

    public Guild getGuild()
    {
        return getGateway().getGuildById(guildId).block();
    }

    public MessageChannel getChannel()
    {
        return getGateway().getChannelById(channelId).cast(MessageChannel.class).block();
    }

    public User getUser()
    {
        return getGateway().getUserById(userId).block();
    }

    public Profile getProfile()
    {
        return getCore().getProfiles().get(userId.asString());
    }

    public Member getMember()
    {
        return getUser().asMember(guildId).block();
    }

    public void reply(MessageCreateSpec spec)
    {
        reply(spec, getChannel());
    }

    public void reply(MessageCreateSpec spec, MessageChannel channel)
    {
        if (messageId != null)
            spec = spec.withMessageReference(messageId);
        channel.createMessage(spec).subscribe();
    }

    public void confirm(String s)
    {
        if (messageId != null)
            getMessage().addReaction(ReactionEmoji.unicode("\u2705")).subscribe();
        else
            reply(Replies.success(s).build());
    }
}
