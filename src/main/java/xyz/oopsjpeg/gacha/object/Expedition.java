package xyz.oopsjpeg.gacha.object;

import xyz.oopsjpeg.gacha.object.data.ExpeditionData;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.time.LocalDateTime;

public class Expedition
{
    private final Profile profile;
    private final ExpeditionData data;

    public Expedition(Profile profile, ExpeditionData data)
    {
        this.profile = profile;
        this.data = data;
    }

    public ExpeditionData getData()
    {
        return data;
    }

    public LocalDateTime getStartDate()
    {
        return LocalDateTime.parse(data.startDate);
    }

    /**
     * Gets the duration of the Expedition
     * @return The duration, in hours
     */
    public long getDuration()
    {
        return data.duration;
    }

    public String getTeamId()
    {
        return data.teamId;
    }
}
