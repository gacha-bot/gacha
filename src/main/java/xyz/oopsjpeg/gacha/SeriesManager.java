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

public class SeriesManager
{
    private static final Logger logger = LoggerFactory.getLogger(SeriesManager.class);

    private final Gacha gacha;
    private final Map<String, Series> seriesMap = new HashMap<>();

    public SeriesManager(Gacha gacha)
    {
        this.gacha = gacha;
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public Series get(String id)
    {
        return seriesMap.getOrDefault(id, null);
    }

    public void fetch() throws IOException
    {
        seriesMap.clear();

        URL url = new URL(gacha.getSettings().getDataUrl() + "series.json");
        URLConnection con = url.openConnection();

        try (InputStreamReader isr = new InputStreamReader(con.getInputStream()))
        {
            JsonObject json = Gacha.GSON.fromJson(isr, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : json.entrySet())
            {
                SeriesData data = Gacha.GSON.fromJson(e.getValue(), SeriesData.class);
                seriesMap.put(e.getKey(), new Series(this, data, e.getKey()));
            }
            logger.info("Fetched " + seriesMap.size() + " series");
        }
    }
}
