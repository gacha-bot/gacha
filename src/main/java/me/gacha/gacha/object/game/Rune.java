package me.gacha.gacha.object.game;

import me.gacha.gacha.DBObject;

public class Rune extends DBObject<Rune.Data>
{
    public Rune(Data data)
    {
        super(data);
    }

    public static Rune create(Type type)
    {
        Data data = new Data();
        data.type = type;
        return new Rune(data);
    }

    public String getName()
    {
        return getType().getName();
    }

    public String getDescription()
    {
        return getType().getDescription();
    }

    public Type getType()
    {
        return data.type;
    }

    public int getRank()
    {
        return data.rank;
    }

    public int getDisplayedRank()
    {
        return getRank() + 1;
    }

    public enum Type
    {
        UNDYING_JUSTICE("Undying Justice", "The first ally that dies regains 25% HP."),
        WITHER("Wither", "The fastest enemy loses 60% ATK on the first round."),
        RALLY("Rally", "All allies gain 20% SPD on the first round.");

        private final String name;
        private final String description;

        Type(String name, String description)
        {
            this.name = name;
            this.description = description;
        }

        public static Rune.Type fromName(String name)
        {
            for (Rune.Type r : values())
                if (r.name.equalsIgnoreCase(name)) return r;
            return null;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }
    }

    public static class Data
    {
        Type type;
        int rank;
    }
}
