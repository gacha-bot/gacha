package me.gacha.gacha.command.impl;

import me.gacha.gacha.command.Cmd;
import me.gacha.gacha.command.context.Context;
import me.gacha.gacha.object.DBUser;
import me.gacha.gacha.object.game.Unit;
import reactor.core.publisher.Mono;

public class CreateUnitCmd extends Cmd
{
    public CreateUnitCmd()
    {
        super("createunit", "[TEST] Create Unit");
    }

    @Override
    public Mono<?> execute(Context context)
    {
        DBUser user = context.getDBUser();
        String query = context.getArgs()[0];
        Unit.Type type = Unit.Type.fromName(query);
        Unit unit = user.createUnit(type);
        unit.markForSave();
        return context.reply("New unit created for you: " + unit.getName());
    }
}
