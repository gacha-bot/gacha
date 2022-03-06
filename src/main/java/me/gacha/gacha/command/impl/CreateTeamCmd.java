package me.gacha.gacha.command.impl;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.spec.MessageCreateSpec;
import me.gacha.gacha.command.Cmd;
import me.gacha.gacha.command.context.Context;
import me.gacha.gacha.object.DBUser;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

public class CreateTeamCmd extends Cmd
{
    public CreateTeamCmd()
    {
        super("createteam", "[TEST] Create Team");
    }

    @Override
    public Mono<?> execute(Context context)
    {
        DBUser user = context.getDBUser();
        return context.reply(MessageCreateSpec.create()
                .withComponents(
                        ActionRow.of(SelectMenu.of("units",
                                        user.getUnits().stream()
                                                .map(unit -> SelectMenu.Option.of(
                                                                unit.getName() + " (Lv. 1)",
                                                                unit.getType().name())
                                                        .withDescription(unit.getStats().print()))
                                                .collect(Collectors.toList()))
                                .withMinValues(2)
                                .withMaxValues(2)),
                        ActionRow.of(SelectMenu.of("runes",
                                        user.getRunes().stream()
                                                .map(rune -> SelectMenu.Option.of(
                                                                rune.getName() + " (Rk. " + rune.getDisplayedRank() + ")",
                                                                rune.getType().name())
                                                        .withDescription(rune.getDescription()))
                                                .collect(Collectors.toList()))
                                .withMinValues(2)
                                .withMaxValues(2)),
                        ActionRow.of(
                                Button.primary("create", "Use Team"),
                                Button.danger("cancel", "Cancel"))));
    }
}