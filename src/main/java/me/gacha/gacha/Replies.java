package me.gacha.gacha;

import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.MessageCreateMono;

public class Replies
{
    public static MessageCreateMono failure(MessageChannel channel, String content)
    {
        return channel.createMessage(Emotes.X + " " + content);
    }
}
