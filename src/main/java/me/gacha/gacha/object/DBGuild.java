package me.gacha.gacha.object;

import me.gacha.gacha.DBObject;

public class DBGuild extends DBObject<DBGuild.Data>
{
    public DBGuild(Data data)
    {
        super(data);
    }

    public static DBGuild create(String id)
    {
        Data data = new Data();
        data.id = id;
        return new DBGuild(data);
    }

    public static class Data
    {
        public String id;
    }
}
