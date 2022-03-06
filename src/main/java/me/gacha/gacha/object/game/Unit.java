package me.gacha.gacha.object.game;

import me.gacha.gacha.DBObject;

public class Unit extends DBObject<Unit.Data>
{
    public Unit(Data data)
    {
        super(data);
    }

    public static Unit create(Type type)
    {
        Data data = new Data();
        data.type = type;
        return new Unit(data);
    }

    public String getName()
    {
        return getType().getName();
    }

    public Type getType()
    {
        return data.type;
    }

    public Stats getStats()
    {
        return getType().getStats();
    }

    public int getXp()
    {
        return data.xp;
    }

    public enum Type
    {
        LUELLA("Luella", new Stats(495, 60, 30, 35, 30)),
        AVERIL("Averil", new Stats(460, 50, 50, 20, 55));

        private final String name;
        private final Stats stats;

        Type(String name, Stats stats)
        {
            this.name = name;
            this.stats = stats;
        }

        public static Type fromName(String name)
        {
            for (Type t : values())
                if (t.name.equalsIgnoreCase(name)) return t;
            return null;
        }

        public String getName()
        {
            return name;
        }

        public Stats getStats()
        {
            return stats;
        }
    }

    public static class Data
    {
        Type type;
        int xp;
    }
}
