package xyz.oopsjpeg.gacha.util;

import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.Util;
import xyz.oopsjpeg.gacha.object.Stats;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.object.user.ProfileCard;

public class Embeds
{
    public static EmbedCreateSpec.Builder card(ProfileCard card, Profile profile)
    {
        Core core = profile.getManager().getCore();
        Stats stats = card.getStats();

        return EmbedCreateSpec.builder()
                .title(card.getName() + " (" + Util.stars(card.getTier()) + ")")
                .color(Color.of(card.getFrameColor().getRGB()))
                .description((card.hasVariant() ? card.getVariant() + "\n\n" : "")
                        + Util.sticker("Level", "**" + (card.getLevel() + 1) + "** (" + card.getXp() + " / " + card.getMaxXp() + ")") + "\n"
                        + Util.sticker("Stats", "**" + stats.getHealth() + "** HP **" + stats.getDefense() + "** DF **" + stats.getAttack() + "** AT **" + stats.getMagic() + "** MG"))
                .image(core.getSettings().getDataUrl() + "cards/renders/" + card.getImageRaw() + ".png")
                .footer(EmbedCreateFields.Footer.of(card.getArchetype().getName() + " - " + card.getSeries().getName() + " Series", null));
    }

    public static EmbedCreateSpec.Builder card(ProfileCard card, User user)
    {
        return card(card, card.getCore().getProfiles().get(user));
    }
}
