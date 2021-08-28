package xyz.oopsjpeg.gacha.object.user;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.ProfileManager;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.Resources;
import xyz.oopsjpeg.gacha.object.SavedObject;
import xyz.oopsjpeg.gacha.object.data.ProfileData;
import xyz.oopsjpeg.gacha.object.data.ResourcesData;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class Profile implements SavedObject
{
    private final ProfileManager manager;
    private final ProfileData data;

    private boolean markedForSave;

    public Profile(ProfileManager manager, ProfileData data)
    {
        this.manager = manager;
        this.data = data;
    }

    public static Profile create(ProfileManager manager, String id)
    {
        ProfileData data = new ProfileData();
        data.id = id;
        return new Profile(manager, data);
    }

    public Core getCore()
    {
        return manager.getCore();
    }

    public GatewayDiscordClient getGateway()
    {
        return getCore().getGateway();
    }

    public ProfileManager getManager()
    {
        return manager;
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
        return getGateway().getUserById(Snowflake.of(getId())).block();
    }

    public String getUsername()
    {
        return getUser().getUsername();
    }

    public String getAvatarUrl()
    {
        return getUser().getAvatarUrl();
    }

    public Resources getResources()
    {
        if (data.resources == null)
            data.resources = new ResourcesData();
        return new Resources(this, data.resources);
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

    public Map<ProfileCard, Integer> findManyCards(String query)
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
                if (card.hasVariant() && card.getVariant().toLowerCase().contains(term))
                    matches++;

                if (matches > 0)
                    results.put(card, matches);
            }
        }
        return results;
    }

    public ProfileCard findOneCard(String query)
    {
        return findManyCards(query).entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean hasCards()
    {
        return !data.cards.isEmpty();
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

    public boolean canCollectDaily()
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

    public boolean canCollectWeekly()
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
        return data.cards.isEmpty() ? 1 : Collections.max(getCards().stream()
                .map(ProfileCard::getTier)
                .collect(Collectors.toList()));
    }

    public LocalDateTime getVoteDate()
    {
        return data.voteDate == null ? null : LocalDateTime.parse(data.voteDate);
    }

    public void setVoteDate(LocalDateTime ldt)
    {
        data.voteDate = ldt.toString();
    }

    public boolean hasVoted()
    {
        return data.voteDate != null && LocalDateTime.now().isBefore(getVoteDate().plusHours(12));
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
