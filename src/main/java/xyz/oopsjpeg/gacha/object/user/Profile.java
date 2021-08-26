package xyz.oopsjpeg.gacha.object.user;

import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.Resources;
import xyz.oopsjpeg.gacha.object.SavedObject;
import xyz.oopsjpeg.gacha.object.data.ProfileData;
import xyz.oopsjpeg.gacha.object.data.ResourcesData;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Profile implements SavedObject
{
    private final Gacha gacha;
    private final ProfileData data;

    private boolean markedForSave;

    public Profile(Gacha gacha, ProfileData data)
    {
        this.gacha = gacha;
        this.data = data;
    }

    public static Profile create(Gacha gacha, String id)
    {
        ProfileData data = new ProfileData();
        data.id = id;
        return new Profile(gacha, data);
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public ProfileData getData()
    {
        return data;
    }

    public String getId()
    {
        return data.id;
    }

    public User getUser()
    {
        return gacha.getGateway().getUserById(Snowflake.of(getId())).block();
    }

    public Resources getResources()
    {
        if (data.resources == null)
            data.resources = new ResourcesData();
        return new Resources(gacha, data.resources);
    }

    public void setResources(Resources resources)
    {
        data.resources = resources.getData();
    }

    public List<ProfileCard> getCards()
    {
        return data.cards.values().stream()
                .map(cardData -> new ProfileCard(this, cardData))
                .collect(Collectors.toList());
    }

    public Map<ProfileCard, Integer> searchCards(String query)
    {
        Map<ProfileCard, Integer> results = new HashMap<>();
        String[] split = query.toLowerCase().split(" ");

        for (String term : split)
        {
            for (ProfileCard card : getCards())
            {
                int matches = 0;
                // ID
                if (String.valueOf(card.getId()).equals(term))
                    matches++;
                // Name
                if (card.getName().toLowerCase().contains(term))
                    matches++;
                // Variant
                if (card.getCard().hasVariant() && card.getCard().getVariant().toLowerCase().contains(term))
                    matches++;

                if (matches > 0)
                    results.put(card, matches);
            }
        }
        return results;
    }

    public ProfileCard searchCard(String query)
    {
        return searchCards(query).entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean hasCards()
    {
        return !getCards().isEmpty();
    }

    public ProfileCard getCard(Card card)
    {
        return getCard(card.getId());
    }

    public ProfileCard getCard(String id)
    {
        return new ProfileCard(this, data.cards.get(id));
    }

    public ProfileCard getBestCard()
    {
        return getCards().stream().max(Comparator.comparingInt(ProfileCard::getTier)).orElse(null);
    }

    public void addCard(ProfileCard card)
    {
        data.cards.put(String.valueOf(card.getId()), card.getData());
    }

    public void removeCard(ProfileCard card)
    {
        data.cards.remove(String.valueOf(card.getId()));
    }

    public boolean hasCard(Card card)
    {
        return data.cards.containsKey(String.valueOf(card.getId()));
    }

    public LocalDateTime getDailyDate()
    {
        return data.dailyDate == null ? null : LocalDateTime.parse(data.dailyDate);
    }

    public void setDailyDate(LocalDateTime dailyDate)
    {
        data.dailyDate = dailyDate.toString();
    }

    public boolean hasDaily()
    {
        return data.dailyDate == null || LocalDateTime.now().isAfter(getDailyDate().plusDays(1));
    }

    public String timeUntilDaily()
    {
        return Util.timeDiff(LocalDateTime.now(), getDailyDate().plusDays(1));
    }

    public LocalDateTime getWeeklyDate()
    {
        return data.weeklyDate == null ? null : LocalDateTime.parse(data.weeklyDate);
    }

    public void setWeeklyDate(LocalDateTime weeklyDate)
    {
        data.weeklyDate = weeklyDate.toString();
    }

    public String timeUntilWeekly()
    {
        return Util.timeDiff(LocalDateTime.now(), getWeeklyDate().plusWeeks(1));
    }

    public boolean hasWeekly()
    {
        return data.weeklyDate == null || LocalDateTime.now().isAfter(getWeeklyDate().plusWeeks(1));
    }

    public String getDescription()
    {
        return hasDescription() ? data.description : "Curious Cardseeker";
    }

    public void setDescription(String description)
    {
        data.description = description;
    }

    public void clearDescription()
    {
        data.description = null;
    }

    public boolean hasDescription()
    {
        return data.description != null;
    }

    public ProfileCard getFavoriteCard()
    {
        return hasFavoriteCard() ? getCard(data.favoriteCardId) : null;
    }

    public void setFavoriteCard(ProfileCard card)
    {
        data.favoriteCardId = String.valueOf(card.getId());
    }

    public boolean hasFavoriteCard()
    {
        return data.favoriteCardId != null;
    }

    public void clearFavoriteCard()
    {
        data.favoriteCardId = null;
    }

    public int getTier()
    {
        return getCards().isEmpty() ? 1 : Collections.max(getCards().stream()
                .map(ProfileCard::getTier)
                .collect(Collectors.toList()));
    }

    public int getBannerPityT5(String id)
    {
        return data.bannerPityT5.getOrDefault(id, 0);
    }

    public void resetBannerPityT5(String id)
    {
        data.bannerPityT5.put(id, 0);
    }

    public boolean pityBannerT5(String id)
    {
        if (!data.bannerPityT5.containsKey(id))
            data.bannerPityT5.put(id, 1);
        else
            data.bannerPityT5.put(id, getBannerPityT5(id) + 1);
        return getBannerPityT5(id) >= 70;
    }

    public int getBannerPityT4(String id)
    {
        return data.bannerPityT4.getOrDefault(id, 0);
    }

    public void resetBannerPityT4(String id)
    {
        data.bannerPityT4.put(id, 0);
    }

    public boolean pityBannerT4(String id)
    {
        if (!data.bannerPityT4.containsKey(id))
            data.bannerPityT4.put(id, 1);
        else
            data.bannerPityT4.put(id, getBannerPityT4(id) + 1);
        return getBannerPityT4(id) >= 10;
    }

    public void setVoteDate(LocalDateTime ldt)
    {
        data.voteDate = ldt.toString();
    }

    public LocalDateTime getVoteDate()
    {
        return data.voteDate == null ? null : LocalDateTime.parse(data.voteDate);
    }

    public boolean hasVoted()
    {
        return getVoteDate() != null && LocalDateTime.now().isBefore(getVoteDate().plusHours(12));
    }

    @Override
    public boolean isMarkedForSave()
    {
        return markedForSave;
    }

    @Override
    public void setMarkedForSave(boolean b)
    {
        markedForSave = b;
    }
}
