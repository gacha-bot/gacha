package xyz.oopsjpeg.gacha.util;

import discord4j.core.object.entity.User;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.rest.util.Color;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Card;
import xyz.oopsjpeg.gacha.object.Stats;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.object.user.ProfileCard;

import java.util.function.Consumer;

public class Embeds
{
    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard card, Profile profile, String message)
    {
        Core core = profile.getManager().getCore();
        Stats stats = card.getStats();

        return e -> e
                .setTitle(card.getName() + " (" + Util.stars(card.getTier()) + ")")
                .setColor(Color.of(card.getFrameColor().getRGB()))
                .setDescription((card.hasVariant() ? card.getVariant() + "\n\n" : "")
                        + Util.sticker("Level", "**" + (card.getLevel() + 1) + "** (" + card.getXp() + " / " + card.getMaxXp() + ")") + "\n"
                        + Util.sticker("Stats", "**" + stats.getHealth() + "** HP **" + stats.getDefense() + "** DF **" + stats.getAttack() + "** AT **" + stats.getMagic() + "** MG"))
                .setImage(core.getSettings().getDataUrl() + "cards/renders/" + card.getImageRaw() + ".png")
                .setFooter(card.getArchetype().getName() + " - " + card.getSeries().getName() + " Series", null);
    }

    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard card, Profile profile)
    {
        return card(card, profile, null);
    }


    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard card, User user, String message)
    {
        return card(card, card.getCore().getProfiles().get(user), message);
    }

    public static Consumer<LegacyEmbedCreateSpec> card(ProfileCard card, User user)
    {
        return card(card, card.getCore().getProfiles().get(user), null);
    }
}
