package xyz.oopsjpeg.gacha.command;

import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.legacy.LegacyEmbedCreateSpec;
import discord4j.core.spec.legacy.LegacyMessageCreateSpec;
import discord4j.core.spec.legacy.LegacyMessageEditSpec;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import xyz.oopsjpeg.gacha.Util;

import java.io.InputStream;
import java.util.function.Consumer;

public class Reply
{
    private String content;
    private Consumer<LegacyEmbedCreateSpec> embed;
    private Tuple2<String, InputStream> file;
    private LayoutComponent[] components;

    public Message create(User user, MessageChannel channel, Message reference)
    {
        Consumer<LegacyEmbedCreateSpec> userAsAuthor = e -> e
                .setColor(Util.getDisplayColor(user, channel))
                .setAuthor(Util.formatUsername(user), null, user.getAvatarUrl());

        Consumer<LegacyMessageCreateSpec> spec = m -> m.setContent(getContent());

        if (getEmbed() != null)
            spec = spec.andThen(m -> m.addEmbed(userAsAuthor.andThen(getEmbed())));
        if (reference != null)
            spec = spec.andThen(m -> m.setMessageReference(reference.getId()));
        if (getComponents() != null)
            spec = spec.andThen(m -> m.setComponents(getComponents()));
        if (getFile() != null)
            spec = spec.andThen(m -> m.addFile(getFile().getT1(), getFile().getT2()));

        return channel.createMessage(spec).block();
    }

    public void edit(Message original, User user, MessageChannel channel)
    {
        Consumer<LegacyEmbedCreateSpec> userAsAuthor = e -> e
                .setColor(Util.getDisplayColor(user, channel))
                .setAuthor(Util.formatUsername(user), null, user.getAvatarUrl());

        Consumer<LegacyMessageEditSpec> spec = m -> m.setContent(getContent());

        if (getEmbed() != null)
            spec = spec.andThen(m -> m.addEmbed(getEmbed().andThen(userAsAuthor)));
        if (getComponents() != null)
            spec = spec.andThen(m -> m.setComponents(getComponents()));

        original.edit(spec).subscribe();
    }

    public String getContent()
    {
        return content;
    }

    public Reply setContent(String content)
    {
        this.content = content;
        return this;
    }

    public Consumer<LegacyEmbedCreateSpec> getEmbed()
    {
        return embed;
    }

    public Reply setEmbed(Consumer<LegacyEmbedCreateSpec> embed)
    {
        this.embed = embed;
        return this;
    }

    public Tuple2<String, InputStream> getFile()
    {
        return file;
    }

    public Reply setFile(String fileName, InputStream stream)
    {
        file = Tuples.of(fileName, stream);
        return this;
    }

    public LayoutComponent[] getComponents()
    {
        return components;
    }

    public Reply setComponents(LayoutComponent... components)
    {
        this.components = components;
        return this;
    }
}
