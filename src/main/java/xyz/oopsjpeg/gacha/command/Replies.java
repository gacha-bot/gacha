package xyz.oopsjpeg.gacha.command;

import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;

public class Replies
{
    private static final Color COLOR_SUCCESS = Color.of(119, 178, 85);
    private static final Color COLOR_FAILURE = Color.of(221, 46, 68);

    public static MessageCreateSpec.Builder success(String s)
    {
        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .color(COLOR_SUCCESS)
                        .description(":white_check_mark:｜" + s)
                        .build());
    }

    public static MessageCreateSpec.Builder failure(String s)
    {
        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .color(COLOR_FAILURE)
                        .description(":x:｜" + s)
                        .build());
    }

    public static MessageCreateSpec.Builder failure(Throwable throwable)
    {
        return failure("An unknown exception occurred.\n\n" + throwable.getMessage());
    }

    public static MessageCreateSpec.Builder info(String s)
    {
        return MessageCreateSpec.builder()
                .addEmbed(EmbedCreateSpec.builder()
                        .description(s)
                        .build());
    }

}
