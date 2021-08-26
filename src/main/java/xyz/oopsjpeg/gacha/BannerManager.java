package xyz.oopsjpeg.gacha;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.object.Banner;
import xyz.oopsjpeg.gacha.object.data.BannerData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

public class BannerManager
{
    private static final Logger logger = LoggerFactory.getLogger(BannerManager.class);

    private final Gacha gacha;
    private final Map<String, Banner> bannerMap = new HashMap<>();

    public BannerManager(Gacha gacha)
    {
        this.gacha = gacha;
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public Banner get(String id)
    {
        return bannerMap.getOrDefault(id, null);
    }

    public void fetch() throws IOException
    {
        bannerMap.clear();

        URL url = new URL(gacha.getSettings().getDataUrl() + "banners.json");
        URLConnection con = url.openConnection();

        try (InputStreamReader isr = new InputStreamReader(con.getInputStream()))
        {
            JsonObject json = Gacha.GSON.fromJson(isr, JsonObject.class);
            for (Map.Entry<String, JsonElement> e : json.entrySet())
            {
                BannerData data = Gacha.GSON.fromJson(e.getValue(), BannerData.class);
                bannerMap.put(e.getKey(), new Banner(this, data, e.getKey()));
            }
            logger.info("Fetched " + bannerMap.size() + " banners");
        }
    }
}
