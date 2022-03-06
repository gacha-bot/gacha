package me.gacha.gacha.command.impl;

import me.gacha.gacha.command.Cmd;
import me.gacha.gacha.command.context.Context;
import me.gacha.gacha.object.DBUser;
import me.gacha.gacha.object.game.Rune;
import reactor.core.publisher.Mono;

public class CreateRuneCmd extends Cmd
{
    public CreateRuneCmd()
    {
        super("createrune", "[TEST] Create Rune");
    }

    @Override
    public Mono<?> execute(Context context)
    {
        DBUser user = context.getDBUser();
        String query = context.getArgs()[0];
        Rune.Type type = Rune.Type.fromName(query);
        Rune rune = user.createRune(type);
        rune.markForSave();
        return context.reply("New rune created for you: " + rune.getName());
    }
}