package xyz.oopsjpeg.gacha.object.user;

import discord4j.core.object.entity.User;
import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.object.Card;
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

    public Gacha getGacha()
    {
        return profile.getGacha();
    }

    public String getId()
    {
        return data.id;
    }

    public Card getCard()
    {
        return getGacha().getCards().get(data.id);
    }

    public String getName()
    {
        return getCard().getName();
    }

    //public boolean hasAltImage()
    //{
    //    return getCard().hasAltImage();
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

    public void setLevel(int level)
    {
        data.level = level;
    }

    public void addLevels(int levels)
    {
        setLevel(getLevel() + levels);
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

    //public boolean isFlipped()
    //{
    //    return data.flipped;
    //}

    //public void setFlipped(boolean f)
    //{
    //    data.flipped = f;
    //}

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
        return getCard().getFrameColor(); //isFlipped() ? getCard().getAltFrameColor() : getCard().getFrameColor();
    }

    public Color getFontColor()
    {
        return getCard().getFontColor();
    }

    public String getVariant()
    {
        return getCard().getVariant();
    }

    public boolean hasVariant()
    {
        return getCard().hasVariant();
    }
}
