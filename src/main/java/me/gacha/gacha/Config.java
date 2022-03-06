package me.gacha.gacha;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class Config
{
    private static Config instance;

    private String discordToken;
    private String prefix;
    private String mongoConnectionString;
    private String mongoDatabaseName;

    public static void init() throws IOException
    {
        final ObjectMapper mapper = new ObjectMapper();
        instance = mapper.readValue(new File("config.json"), Config.class);
    }

    public static Config get()
    {
        return instance;
    }

    public String getDiscordToken()
    {
        return discordToken;
    }

    public String getPrefix()
    {
        return prefix;
    }

    public String getMongoConnectionString()
    {
        return mongoConnectionString;
    }

    public String getMongoDatabaseName()
    {
        return mongoDatabaseName;
    }
}
