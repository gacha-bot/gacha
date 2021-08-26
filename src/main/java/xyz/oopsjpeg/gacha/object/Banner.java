package xyz.oopsjpeg.gacha.object;

import xyz.oopsjpeg.gacha.BannerManager;
import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.data.BannerData;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by oopsjpeg on 3/12/2019.
 */
public class Banner
{
    private final BannerManager manager;
    private final BannerData data;
    private final String id;
    private final Collection<String> includedCards;

    public Banner(BannerManager manager, BannerData data, String id)
    {
        this.manager = manager;
        this.data = data;
        this.id = id;

        includedCards = new ArrayList<>();
        for (String series : data.includedCards.series)
        {
            includedCards.addAll(getGacha().getCards().allAsList().stream()
                    .filter(card -> card.getSeries().getId().equals(series))
                    .map(Card::getId)
                    .collect(Collectors.toList()));
        }
    }

    public BannerManager getManager()
    {
        return manager;
    }

    public Gacha getGacha()
    {
        return manager.getGacha();
    }

    public BannerData getData()
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

    public int getCost()
    {
        return data.cost;
    }

    public List<Card> getIncludedCards()
    {
        return includedCards.stream().map(id -> getGacha().getCards().get(id)).collect(Collectors.toList());
    }

    public List<Card> getIncludedCardsForTier(int tier)
    {
        return getIncludedCards().stream().filter(card -> card.getTier() == tier).collect(Collectors.toList());
    }

    public boolean hasIncludedCardsForTier(int tier)
    {
        return getIncludedCards().stream().anyMatch(card -> card.getTier() == tier);
    }

    public Card pullCard(Profile profile)
    {
        final List<Card> cards = getIncludedCards();
        final List<Card> pool = new ArrayList<>();
        final float f = Util.RANDOM.nextFloat();

        if (hasIncludedCardsForTier(5) && (f <= 0.012 || profile.pityBannerT5(getId())))
        {
            pool.addAll(getIncludedCardsForTier(5));
            profile.resetBannerPityT5(getId());
        }
        else if (hasIncludedCardsForTier(4) && (f <= 0.04 || profile.pityBannerT4(getId())))
        {
            pool.addAll(getIncludedCardsForTier(4));
            profile.resetBannerPityT4(getId());
        }
        else if (hasIncludedCardsForTier(3) && f <= 0.12)
            pool.addAll(getIncludedCardsForTier(3));
        else if (hasIncludedCardsForTier(2) && f <= 0.32)
            pool.addAll(getIncludedCardsForTier(2));
        else if (hasIncludedCardsForTier(1))
            pool.addAll(getIncludedCardsForTier(1));

        if (pool.isEmpty()) return null;

        return pool.get(Util.RANDOM.nextInt(pool.size()));
    }
}