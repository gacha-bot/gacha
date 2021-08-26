package xyz.oopsjpeg.gacha.object;

import xyz.oopsjpeg.gacha.SeriesManager;
import xyz.oopsjpeg.gacha.object.data.SeriesData;

public class Series
{
    private final SeriesManager manager;
    private final SeriesData data;
    private final String id;

    public Series(SeriesManager manager, SeriesData data, String id)
    {
        this.manager = manager;
        this.data = data;
        this.id = id;
    }

    public SeriesManager getManager()
    {
        return manager;
    }

    public SeriesData getData()
    {
        return data;
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return data.name;
    }
}
