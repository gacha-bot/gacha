package xyz.oopsjpeg.gacha.object;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.object.data.VoteData;

public class Vote
{
    private final Gacha gacha;
    private final VoteData data;

    public Vote(Gacha gacha, VoteData data)
    {
        this.gacha = gacha;
        this.data = data;
    }

    public VoteData getData()
    {
        return data;
    }

    public String getBotId()
    {
        return data.bot;
    }

    public String getUserId()
    {
        return data.user;
    }

    public User getUser()
    {
        return gacha.getGateway().getUserById(Snowflake.of(getUserId())).block();
    }

    public String getType()
    {
        return data.type;
    }

    public boolean isWeekend()
    {
        return data.isWeekend;
    }

    public String getQuery()
    {
        return data.query;
    }
}
