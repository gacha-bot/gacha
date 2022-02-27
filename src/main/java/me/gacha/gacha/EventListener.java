package me.gacha.gacha;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface EventListener<T extends Event>
{
    Class<T> getEventType();

    Mono<?> execute(final T event);
}
