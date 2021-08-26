package xyz.oopsjpeg.gacha.command;

import discord4j.rest.util.Color;
import xyz.oopsjpeg.gacha.object.user.ProfileCard;
import xyz.oopsjpeg.gacha.util.Embeds;

import java.io.IOException;

public class Replies
{
    private static final Color COLOR_SUCCESS = Color.of(119, 178, 85);
    private static final Color COLOR_FAILURE = Color.of(221, 46, 68);

    public static Reply success(String message)
    {
        return new Reply().setEmbed(e -> e
                .setColor(COLOR_SUCCESS)
                .setDescription(":white_check_mark:｜" + message));
    }

    public static Reply failure(String message)
    {
        return new Reply().setEmbed(e -> e
                .setColor(COLOR_FAILURE)
                .setDescription(":x:｜" + message));
    }

    public static Reply failure(Throwable throwable)
    {
        return failure("An unknown exception occurred.\n\n" + throwable.getMessage());
    }

    public static Reply info(String message)
    {
        return new Reply().setEmbed(e -> e.setDescription(message));
    }

    public static Reply card(ProfileCard card, String content, String message) throws IOException
    {
        return new Reply()
                .setContent(content)
                .setEmbed(Embeds.card(card, card.getUser(), message));
    }
}
