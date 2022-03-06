package me.gacha.gacha.object;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import me.gacha.gacha.Core;
import me.gacha.gacha.DBObject;
import me.gacha.gacha.object.game.Rune;
import me.gacha.gacha.object.game.Unit;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBUser extends DBObject<DBUser.Data>
{
    public DBUser(Data data)
    {
        super(data);
    }

    public static DBUser create(String id)
    {
        Data data = new Data();
        data.id = id;
        return new DBUser(data);
    }

    public Snowflake getId()
    {
        return Snowflake.of(getRawId());
    }

    public String getRawId()
    {
        return data.id;
    }

    public Mono<User> getUser()
    {
        return Core.getGateway().getUserById(getId());
    }

    public Mono<String> getUsername()
    {
        return getUser().map(User::getUsername);
    }

    private Map<Unit.Type, Unit.Data> getUnitsData()
    {
        if (data.units == null)
            data.units = new HashMap<>();
        return data.units;
    }

    public List<Unit> getUnits()
    {
        return getUnitsData().values().stream().map(Unit::new).toList();
    }

    public Unit getUnit(Unit.Type type)
    {
        return !hasUnit(type) ? null : new Unit(getUnitsData().get(type));
    }

    public Unit createUnit(Unit.Type type)
    {
        Unit unit = Unit.create(type);
        getUnitsData().put(type, unit.getData());
        return unit;
    }

    public void deleteUnit(Unit.Type type)
    {
        getUnitsData().remove(type);
    }

    public boolean hasUnit(Unit.Type type)
    {
        return getUnitsData().containsKey(type);
    }

    private Map<Rune.Type, Rune.Data> getRunesData()
    {
        if (data.runes == null)
            data.runes = new HashMap<>();
        return data.runes;
    }

    public List<Rune> getRunes()
    {
        return getRunesData().values().stream().map(Rune::new).toList();
    }

    public Rune getRune(Rune.Type type)
    {
        return !hasRune(type) ? null : new Rune(getRunesData().get(type));
    }

    public Rune createRune(Rune.Type type)
    {
        Rune rune = Rune.create(type);
        getRunesData().put(type, rune.getData());
        return rune;
    }

    public void deleteRune(Rune.Type type)
    {
        getRunesData().remove(type);
    }

    public boolean hasRune(Rune.Type type)
    {
        return getRunesData().containsKey(type);
    }

    public static class Data
    {
        public String id;
        public Map<Unit.Type, Unit.Data> units = new HashMap<>();
        public Map<Rune.Type, Rune.Data> runes = new HashMap<>();
    }
}
