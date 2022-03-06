package me.gacha.gacha;

import com.mongodb.connection.ServerConnectionState;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import me.gacha.gacha.command.Commands;
import me.gacha.gacha.command.listener.TextCommandListener;
import me.gacha.gacha.object.DBGuild;
import me.gacha.gacha.object.DBUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.retry.Repeat;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static me.gacha.gacha.Constants.DATABASE_RETRIES;

public class Core
{
    private static final Logger log = LoggerFactory.getLogger(Core.class);

    private static Database database;
    private static GatewayDiscordClient gateway;

    private static Map<String, DBGuild> dbGuildMap;
    private static Map<String, DBUser> dbUserMap;

    public static void main(String[] args) throws IOException
    {
        // Init config
        log.info("Initializing config");
        Config.init();
        final Config config = Config.get();

        // Init emotes
        log.info("Initializing custom emotes");
        Emotes.init();

        // Connect to database
        log.info("Connecting to database");
        database = Mono
                // Create the database
                .just(new Database(config.getMongoConnectionString(), config.getMongoDatabaseName()))
                // Wait, then try for connection
                .delayElement(Duration.ofSeconds(1))
                .filter(db -> db.getState() == ServerConnectionState.CONNECTED)
                // Retry connection
                .repeatWhenEmpty(Repeat.times(DATABASE_RETRIES)
                        .exponentialBackoff(Duration.ofSeconds(5), Duration.ofSeconds(20))
                        .doOnRepeat(context -> log.error("Retrying connection to database (t. " + context.companionValue() + ")")))
                // Throw if connection failed after retries
                .switchIfEmpty(Mono.error(new TimeoutException("Couldn't connect to database")))
                .block();

        // Log in to Discord
        log.info("Logging in to Discord");
        final String token = config.getDiscordToken();
        final DiscordClient client = DiscordClient.create(token);

        client.withGateway(gateway ->
        {
            Core.gateway = gateway;

            // Fetch DB objects
            log.info("Fetching database objects");
            dbGuildMap = database.createGuildMap();
            dbUserMap = database.createUserMap();

            // Init command manager
            log.info("Initializing command manager");
            Commands.init();

            // Register listeners
            register(new TextCommandListener());

            log.info("Ready!");

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

    public static Database getDatabase()
    {
        return database;
    }

    private static DBGuild getDBGuild(String id)
    {
        if (!dbGuildMap.containsKey(id))
            dbGuildMap.put(id, DBGuild.create(id));
        return dbGuildMap.get(id);
    }

    public static DBGuild getDBGuild(Snowflake id)
    {
        return getDBGuild(id.asString());
    }

    public static DBGuild getDBGuild(Guild guild)
    {
        return getDBGuild(guild.getId());
    }

    private static DBUser getDBUser(String id)
    {
        if (!dbUserMap.containsKey(id))
            dbUserMap.put(id, DBUser.create(id));
        return dbUserMap.get(id);
    }

    public static DBUser getDBUser(Snowflake id)
    {
        return getDBUser(id.asString());
    }

    public static DBUser getDBUser(User user)
    {
        return getDBUser(user.getId());
    }

    public static GatewayDiscordClient getGateway()
    {
        return gateway;
    }
}
