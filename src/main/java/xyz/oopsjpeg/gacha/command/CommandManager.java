package xyz.oopsjpeg.gacha.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.Manager;
import xyz.oopsjpeg.gacha.ObjectManager;

import java.util.Arrays;
import java.util.List;

public class CommandManager implements Manager
{
    private final Core core;
    private final String prefix;
    private final List<Command> commands;
    private final CommandRateLimiter rateLimiter = new CommandRateLimiter();

    public CommandManager(Core core, String prefix, Command... commands)
    {
        this.core = core;
        this.prefix = prefix;
        this.commands = Arrays.asList(commands);

        getGateway().on(MessageCreateEvent.class).subscribe(this::onMessageCreate);
        //getGateway().on(SlashCommandEvent.class).subscribe(this::onSlashCommand);
    }

    private void onMessageCreate(MessageCreateEvent ev)
    {
        Message message = ev.getMessage();
        MessageChannel channel = message.getChannel().block();
        User user = message.getAuthor().orElse(null);

        if (user == null) return;

        CommandCall call = CommandCall.of(this, message);

        if (call == null) return;

        Command command = get(call.getAlias());

        if (command == null) return;

        Reply reply = null;

        if (rateLimiter.issueCommand(user))
            reply = command.tryExecute(call);
        else if (rateLimiter.warn(user))
            reply = Replies.failure("Slow down!");

        if (reply == null) return;

        reply.create(user, channel, message);
    }

    // private void onSlashCommand(SlashCommandEvent ev)
    // {
    //     CommandCall call = CommandCall.of(this, ev.getInteraction());
    //     Command command = get(call.getAlias());

    //     if (command == null) return;

    //     CommandResult result = executeCommand(call, command);

    //     ev.reply(spec -> spec
    //             .setContent(result.getContent())
    //             .addEmbed(result.getEmbed())
    //             .
    //             .(result.getFile().getT1(), result.getFile().getT2()))
    //             .block();
    // }

    public Command get(String name)
    {
        for (Command command : commands)
            if (command.getName().equalsIgnoreCase(name))
                return command;
        return null;
    }

    public String format(Command command)
    {
        return prefix + command.getName();
    }

    public String getPrefix()
    {
        return prefix;
    }

    public List<Command> getCommands()
    {
        return commands;
    }

    @Override
    public Core getCore()
    {
        return core;
    }
}
