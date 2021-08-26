package xyz.oopsjpeg.gacha;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.data.CardData;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class CardManager
{
    private static final Logger logger = LoggerFactory.getLogger(CardManager.class);

    private final Gacha gacha;
    private final Map<String, Card> cardMap = new HashMap<>();

    public CardManager(Gacha gacha)
    {
        this.gacha = gacha;
    }

    public Card get(String id)
    {
        return cardMap.get(id);
    }

    public List<Card> getByTier(int t)
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

    public List<Card> allAsList()
    {
        return new ArrayList<>(cardMap.values());
    }

    public int total()
    {
        return cardMap.size();
    }

    public void fetch() throws IOException
    {
        cardMap.clear();

        URL url = new URL(gacha.getSettings().getDataUrl() + "cards.json");
        URLConnection con = url.openConnection();

        try (InputStreamReader isr = new InputStreamReader(con.getInputStream()))
        {
            CardData[] data = Gacha.GSON.fromJson(isr, CardData[].class);
            for (int i = 0; i < data.length; i++)
            {
                String id = String.valueOf(i);
                cardMap.put(id, new Card(this, data[i], id));
            }

            logger.info("Fetched " + cardMap.size() + " cards");
        }
    }

    public Gacha getGacha()
    {
        return gacha;
    }
}
