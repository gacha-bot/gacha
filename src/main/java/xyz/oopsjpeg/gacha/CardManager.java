package xyz.oopsjpeg.gacha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.data.CardData;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class CardManager implements ObjectManager<Card>
{
    private final Core core;
    private final Map<String, Card> cardMap = new HashMap<>();

    public CardManager(Core core)
    {
        this.core = core;
    }

    @Override
    public Card get(String id)
    {
        return cardMap.get(id);
    }

    public List<Card> findByTier(int t)
    {
        return cardMap.values().stream()
                .filter(c -> c.getTier() == t)
                .collect(Collectors.toList());
    }

    public Map<Card, Integer> findMany(String query)
    {
        Map<Card, Integer> results = new HashMap<>();
        String[] split = query.toLowerCase().split(" ");

        for (String term : split)
        {
            for (Card card : cardMap.values())
            {
                int matches = 0;
                // ID
                if (card.getId().equals(term))
                    matches++;
                // Name
                if (card.getName().toLowerCase().contains(term))
                    matches++;
                // Variant
                if (card.hasVariant() && card.getVariant().toLowerCase().contains(term))
                    matches++;

                if (matches > 0)
                    results.put(card, matches);
            }
        }
        return results;
    }

    public Card findOne(String query)
    {
        return findMany(query).entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    @Override
    public Map<String, Card> all()
    {
        return cardMap;
    }

    public Card pullCard()
    {
        final List<Card> pool = new ArrayList<>();
        final float f = Util.RANDOM.nextFloat();

        if (f <= 0.012)
            pool.addAll(findByTier(5));
        else if (f <= 0.04)
            pool.addAll(findByTier(4));
        else if (f <= 0.12)
            pool.addAll(findByTier(3));
        else if (f <= 0.32)
            pool.addAll(findByTier(2));
        else
            pool.addAll(findByTier(1));

        if (pool.isEmpty()) return null;

        return pool.get(Util.RANDOM.nextInt(pool.size()));
    }

    @Override
    public void fetch() throws IOException
    {
        cardMap.clear();

        URL url = new URL(core.getSettings().getDataUrl() + "cards.json");
        URLConnection con = url.openConnection();

        try (InputStreamReader isr = new InputStreamReader(con.getInputStream()))
        {
            CardData[] data = Core.GSON.fromJson(isr, CardData[].class);
            for (int i = 0; i < data.length; i++)
            {
                String id = String.valueOf(i);
                cardMap.put(id, new Card(this, data[i], id));
            }

            getLogger().info("Fetched " + cardMap.size() + " cards");
        }
    }

    @Override
    public Core getCore()
    {
        return core;
    }
}
