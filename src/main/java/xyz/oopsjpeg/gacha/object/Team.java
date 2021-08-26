package xyz.oopsjpeg.gacha.object;

import xyz.oopsjpeg.gacha.object.data.TeamData;

public class Team
{
    private final TeamData data;

    public Team(TeamData data)
    {
        this.data = data;
    }

    public TeamData getData()
    {
        return data;
    }
}
