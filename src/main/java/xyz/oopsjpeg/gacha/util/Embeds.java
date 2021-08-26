package xyz.oopsjpeg.gacha.util;

import discord4j.core.object.entity.User;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.rest.util.Color;
import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.Stats;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.object.user.ProfileCard;

import java.util.function.Consumer;

public class Embeds
{
    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard profileCard, Profile profile, String message)
    {
        Gacha gacha = profile.getGacha();
        Card card = profileCard.getCard();
        String avatar = profile.getUser().getAvatarUrl();
        Stats stats = profileCard.getStats();

        return e -> e
                .setTitle(card.getName() + " (" + Util.stars(card.getTier()) + ")")
                .setColor(Color.of(card.getFrameColor().getRGB()))
                .setDescription((card.hasVariant() ? card.getVariant() + "\n\n" : "")
                        + Util.sticker("Level", "**" + (profileCard.getLevel() + 1) + "** (" + profileCard.getXp() + " / " + profileCard.getMaxXp() + ")") + "\n"
                        + Util.sticker("Stats", "**" + stats.getHealth() + "** HP **" + stats.getDefense() + "** DF **" + stats.getAttack() + "** AT **" + stats.getMagic() + "** MG"))
                .setImage(gacha.getSettings().getDataUrl() + "cards/renders/" + card.getImageRaw() + ".png")
                .setFooter(card.getArchetype().getName() + " - " + card.getSeries().getName() + " Series", null);
    }

    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard card, Profile profile)
    {
        return card(card, profile, null);
    }


    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard card, User user, String message)
    {
        return card(card, card.getGacha().getProfile(user), message);
    }

    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard card, User user)
    {
        return card(card, card.getGacha().getProfile(user), null);
    }
}
