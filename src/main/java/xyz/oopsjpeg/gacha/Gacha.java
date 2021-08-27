package xyz.oopsjpeg.gacha;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.command.Command;
import xyz.oopsjpeg.gacha.command.CommandManager;
import xyz.oopsjpeg.gacha.manager.MongoManager;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.util.BadSettingsException;
import xyz.oopsjpeg.gacha.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Gacha
{
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String SETTINGS_FILE = "gacha.properties";

    private static Gacha instance;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    private Settings settings;
    private GatewayDiscordClient gateway;
    private GachaServer server;
    private MongoManager mongo;
    private CommandManager commands;
    private SeriesManager series;
    private CardManager cards;
    private BannerManager banners;
    private ProfileManager profiles;

    public static void main(String[] args) throws BadSettingsException, IOException
    {
        instance = new Gacha();
        instance.start();
    }

    public static Gacha getInstance()
    {
        return instance;
    }

    public void start() throws BadSettingsException, IOException
    {
        loadSettings();

        // Create client
        logger.info("Creating client");
        final DiscordClient client = DiscordClient.create(settings.getToken());
        // Set intents
        client.gateway().setEnabledIntents(IntentSet.of(Intent.GUILDS));
        // Log in
        logger.info("Logging in");
        gateway = client.login().block();
        // Set up ready event
        gateway.on(ReadyEvent.class)
                .map(event -> event.getGuilds().size())
                .flatMap(size -> gateway
                        .on(GuildCreateEvent.class)
                        .take(size)
                        .collectList())
                .subscribe(events ->
                {
                    try
                    {
                        logger.info("Creating MongoDB manager");
                        mongo = new MongoManager(this, settings.getMongoConnectionString());

                        logger.info("Creating command manager");
                        commands = new CommandManager(this, settings.getPrefix(), Command.values());

                        logger.info("Creating series manager");
                        series = new SeriesManager(this);
                        series.fetch();

                        logger.info("Creating card manager");
                        cards = new CardManager(this);
                        cards.fetch();

                        logger.info("Creating banner manager");
                        banners = new BannerManager(this);
                        banners.fetch();

                        logger.info("Loading profile manager");
                        profiles = new ProfileManager(this);
                        profiles.fetch();

                        logger.info("Starting server");
                        server = new GachaServer(this, 8000);
                        server.start();

                        logger.info("Creating automatic data saver");
                        scheduler.scheduleAtFixedRate(() -> profiles.allAsList().stream()
                                .filter(Profile::isMarkedForSave)
                                .forEach(mongo::saveProfile), 5, 5, TimeUnit.MINUTES);

                        logger.info("Creating status updater");
                        scheduler.scheduleAtFixedRate(() -> gateway
                                        .updatePresence(ClientPresence
                                                .online(ClientActivity
                                                        .playing(Constants.GAMES[Util.RANDOM.nextInt(Constants.GAMES.length)])))
                                        .subscribe(),
                                0, 10, TimeUnit.MINUTES);

                        logger.info("Gacha is ready! " + gateway.getGuilds().count().block() + " guilds");
                    }
                    catch (IOException err)
                    {
                        err.printStackTrace();
                    }
                });

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            logger.info("Shutting down");

            if (gateway != null)
            {
                logger.info("Logging out");
                gateway.logout().subscribe();
                logger.info("Saving data");
                profiles.allAsList().stream()
                        .filter(Profile::isMarkedForSave)
                        .forEach(mongo::saveProfile);
            }
        }));

        gateway.onDisconnect().block();
    }

    private void loadSettings() throws BadSettingsException, IOException
    {
        logger.info("Loading settings");
        File settingsFile = new File(SETTINGS_FILE);
        settings = new Settings(settingsFile);

        // Check if settings exist
        if (!settingsFile.exists())
        {
            // Store new settings
            settings.store();
            throw new BadSettingsException("Created new settings");
        }

        // Load settings
        settings.load();
        settings.validate();
    }

    public Logger getLogger()
    {
        return logger;
    }

    public GatewayDiscordClient getGateway()
    {
        return gateway;
    }

    public Settings getSettings()
    {
        return settings;
    }

    public MongoManager getMongo()
    {
        return mongo;
    }

    public CardManager getCards()
    {
        return cards;
    }

    public BannerManager getBanners()
    {
        return banners;
    }

    public SeriesManager getAllSeries()
    {
        return series;
    }

    public ProfileManager getProfiles()
    {
        return profiles;
    }
}
