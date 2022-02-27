package me.gacha.gacha;

import me.gacha.gacha.command.Commands;
import me.gacha.gacha.command.listener.TextCommandListener;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class Core
{
    private static final Logger log = LoggerFactory.getLogger(Core.class);

    private static GatewayDiscordClient gateway;

    public static void main(String[] args) throws IOException
    {
        // Init config
        log.info("Initializing config");
        Config.init();
        final Config config = Config.get();

        // Init emotes
        log.info("Initializing custom emotes");
        Emotes.init();

        // Log in to Discord
        log.info("Logging in to Discord");
        final String token = config.getDiscordToken();
        final DiscordClient client = DiscordClient.create(token);

        client.withGateway(gateway ->
        {
            Core.gateway = gateway;

            // Init command manager
            log.info("Initializing command manager");
            Commands.init();

            // Register listeners
            register(new TextCommandListener());

            return gateway.onDisconnect();
        }).block();
    }

    private static <T extends Event> void register(EventListener<T> listener)
    {
        log.info("Registering listener of type " + listener.getClass().getSimpleName());
        gateway
                .on(listener.getEventType())
                .flatMap(listener::execute)
                .subscribe(null, Throwable::printStackTrace);
    }
}
