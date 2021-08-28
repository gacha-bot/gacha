package xyz.oopsjpeg.gacha;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.object.Series;
import xyz.oopsjpeg.gacha.object.data.SeriesData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class SeriesManager implements ObjectManager<Series>
{
    private final Core core;
    private final Map<String, Series> seriesMap = new HashMap<>();

    public SeriesManager(Core core)
    {
        this.core = core;
    }

    @Override
    public Core getCore()
    {
        return core;
    }

    @Override
    public Series get(String id)
    {
        return seriesMap.getOrDefault(id, null);
    }

    @Override
    public Map<String, Series> all()
    {
        return seriesMap;
    }

    @Override
    public void fetch() throws IOException
    {
        seriesMap.clear();

        URL url = new URL(core.getSettings().getDataUrl() + "series.json");
        URLConnection con = url.openConnection();

        try (InputStreamReader isr = new InputStreamReader(con.getInputStream()))
        {
            JsonObject json = Core.GSON.fromJson(isr, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : json.entrySet())
            {
                SeriesData data = Core.GSON.fromJson(e.getValue(), SeriesData.class);
                seriesMap.put(e.getKey(), new Series(this, data, e.getKey()));
            }
            getLogger().info("Fetched " + seriesMap.size() + " series");
        }
    }
}
