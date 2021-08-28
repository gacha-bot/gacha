package xyz.oopsjpeg.gacha.object.user;

import discord4j.core.object.entity.User;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.object.Archetype;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.Series;
import xyz.oopsjpeg.gacha.object.Stats;
import xyz.oopsjpeg.gacha.object.data.ProfileCardData;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class ProfileCard
{
    private final Profile profile;
    private final ProfileCardData data;

    public ProfileCard(Profile profile, ProfileCardData data)
    {
        Objects.requireNonNull(profile);
        Objects.requireNonNull(data);
        this.profile = profile;
        this.data = data;
    }

    public static ProfileCard create(Profile profile, Card card)
    {
        ProfileCardData data = new ProfileCardData();
        data.id = card.getId();
        return new ProfileCard(profile, data);
    }

    public Core getCore()
    {
        return profile.getCore();
    }

    public Profile getProfile()
    {
        return profile;
    }

    public User getUser()
    {
        return profile.getUser();
    }

    public ProfileCardData getData()
    {
        return data;
    }

    public String getId()
    {
        return data.id;
    }

    public Card getCard()
    {
        return getCore().getCards().get(data.id);
    }

    public String getName()
    {
        return getCard().getName();
    }

    //public boolean hasAltImage()
    //{
    //    return card().hasAltImage();
    //}

    public int getTier()
    {
        return getCard().getTier();
    }

    public boolean isExclusive()
    {
        return getCard().isExclusive();
    }

    public Stats getStats()
    {
        return getCard().getStats().multiply(getLevel() * (0.1f + (0.02f * getTier())));
    }

    public int getLevel()
    {
        return data.level;
    }

    public void setLevel(int l)
    {
        data.level = l;
    }

    public void addLevels(int l)
    {
        setLevel(getLevel() + l);
    }

    public int getXp()
    {
        return data.xp;
    }

    public void setXp(int xp)
    {
        data.xp = xp;
    }

    public void addXp(int xp)
    {
        setXp(getXp() + xp);
    }

    public void subXp(int xp)
    {
        setXp(getXp() - xp);
    }

    public boolean handleXp()
    {
        int oldLevel = getLevel();
        while (getXp() > getMaxXp())
        {
            subXp(getMaxXp());
            addLevels(1);
        }
        return getLevel() != oldLevel;
    }

    public int getMaxXp()
    {
        return (int) (200 + (Math.pow(getLevel() * 125, 1.03)));
    }

    public String format()
    {
        return getCard().format() + " [`LV " + (getLevel() + 1) + "`]";
    }

    public String formatRaw()
    {
        return getCard().formatRaw() + " [`LV " + (getLevel() + 1) + "`]";
    }

    public BufferedImage getFrame() throws IOException
    {
        return getCard().getFrame();
    }

    public Font getFont() throws IOException
    {
        return getCard().getFont();
    }

    public Color getFrameColor()
    {
        return getCard().getFrameColor();
    }

    public Color getFontColor()
    {
        return getCard().getFontColor();
    }

    public String getImageRaw()
    {
        return getCard().getImageRaw();
    }

    public String getVariant()
    {
        return getCard().getVariant();
    }

    public boolean hasVariant()
    {
        return getCard().hasVariant();
    }

    public Series getSeries()
    {
        return getCard().getSeries();
    }

    public Archetype getArchetype()
    {
        return getCard().getArchetype();
    }
}
