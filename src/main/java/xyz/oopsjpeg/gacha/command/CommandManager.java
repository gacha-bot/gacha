package xyz.oopsjpeg.gacha.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import xyz.oopsjpeg.gacha.Gacha;

import java.util.Arrays;
import java.util.List;

/**
 * Created by oopsjpeg on 2/28/2019.
 */
public class CommandManager
{
    private final Gacha gacha;
    private final String prefix;
    private final List<Command> commands;
    private final CommandRateLimiter rateLimiter = new CommandRateLimiter();

    public CommandManager(Gacha gacha, String prefix, Command... commands)
    {
        this.gacha = gacha;
        this.prefix = prefix;
        this.commands = Arrays.asList(commands);

        gacha.getGateway().on(MessageCreateEvent.class).subscribe(this::onMessageCreate);
        //gacha.getGateway().on(ButtonInteractEvent.class).subscribe(this::onButtonInteract);
        // gacha.getGateway().on(SlashCommandEvent.class).subscribe(this::onSlashCommand);
    }

    //private void onButtonInteract(ButtonInteractEvent ev)
    //{
    //    Message message = ev.getMessage();
    //    MessageChannel channel = message.getChannel().block();
    //    String[] customId = ev.getCustomId().split("/");
    //    String command = customId[0];
    //    User user = gacha.getGateway().getUserById(Snowflake.of(customId[1])).block();
//
    //    if (command.equals("pull"))
    //    {
    //        Banner type = Banner.valueOf(customId[2]);
//
    //        CommandCall call = new CommandCall(this, Command.PULL.getName(), new String[]{type.getName()},
    //                message.getGuild().blockOptional().orElse(null),
    //                message.getChannel().blockOptional().orElse(null),
    //                user);
    //        CommandResult result = executeCommand(call, Command.PULL);
    //
    //        Consumer<InteractionApplicationCommandCallbackSpec> spec = m -> m
    //                .setContent(result.getContent())
    //                .addEmbed(result.getEmbed());
    //        if (result.getComponents() != null)
    //            spec = spec.andThen(m -> m.setComponents(result.getComponents()));
    //        if (result.getFile() != null)
    //            spec = spec.andThen(m -> m.addFile(result.getFile().getT1(), result.getFile().getT2()));
//
    //        channel.createMessage(spec).block();
    //    }
//
    //}

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

        Message response = reply.create(user, channel, message);
    }



    /*
    Interaction responses don't allow files.
     */

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

    public Gacha getGacha()
    {
        return gacha;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public List<Command> getCommands()
    {
        return commands;
    }
}
