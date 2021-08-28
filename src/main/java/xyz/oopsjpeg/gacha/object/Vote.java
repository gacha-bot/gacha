package xyz.oopsjpeg.gacha.object;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.object.data.VoteData;

public class Vote
{
    private final Core core;
    private final VoteData data;

    public Vote(Core core, VoteData data)
    {
        this.core = core;
        this.data = data;
    }

    public GatewayDiscordClient getGateway()
    {
        return core.getGateway();
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
        return getGateway().getUserById(Snowflake.of(getUserId())).block();
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
