package xyz.oopsjpeg.gacha;

import xyz.oopsjpeg.gacha.util.BadSettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * Properties wrapper with custom defaults.
 * Created by oopsjpeg on 1/30/2019.
 */
public class Settings
{
    public static final String MONGO_CONNECTION_STRING = "mongo_connection_string";
    public static final String DATA_URL = "data_url";

    public static final String TOKEN = "token";
    public static final String PREFIX = "prefix";

    private static final Properties DEFAULTS = new Properties();

    static
    {
        DEFAULTS.put(MONGO_CONNECTION_STRING, "mongodb://127.0.0.1");

        DEFAULTS.put(DATA_URL, "");

        DEFAULTS.put(TOKEN, "");
        DEFAULTS.put(PREFIX, "g.");
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final File file;
    private final Properties properties = new Properties();

    public Settings(File file)
    {
        this.file = file;
        properties.putAll(DEFAULTS);
    }

    public void load() throws IOException
    {
        FileReader fr = new FileReader(file);
        properties.load(fr);
        fr.close();
    }

    public void store() throws IOException
    {
        FileWriter fw = new FileWriter(file);
        properties.store(fw, "Gacha");
        fw.close();
    }

    private String get(String key)
    {
        return properties.getProperty(key, "");
    }

    private int getInt(String key)
    {
        return Integer.parseInt(get(key));
    }

    private long getLong(String key)
    {
        return Long.parseLong(get(key));
    }

    private float getFloat(String key)
    {
        return Float.parseFloat(get(key));
    }

    private double getDouble(String key)
    {
        return Double.parseDouble(get(key));
    }

    private boolean has(String key)
    {
        return properties.containsKey(key) && !get(key).isEmpty();
    }

    public String getMongoConnectionString()
    {
        return get(MONGO_CONNECTION_STRING);
    }

    public String getDataUrl()
    {
        return get(DATA_URL);
    }

    public String getToken()
    {
        return get(TOKEN);
    }

    public String getPrefix()
    {
        return get(PREFIX);
    }

    public Logger getLogger()
    {
        return logger;
    }

    public File getFile()
    {
        return file;
    }

    public void validate() throws BadSettingsException
    {
        if (!has(Settings.MONGO_CONNECTION_STRING))
            throw new BadSettingsException("Missing MongoDB connection string in " + file.getName());
        if (!has(Settings.TOKEN))
            throw new BadSettingsException("Missing Discord bot token in " + file.getName());
        if (!has(Settings.PREFIX))
            throw new BadSettingsException("Missing prefix in " + file.getName());
        if (!has(Settings.DATA_URL))
            throw new BadSettingsException("Missing data folder in " + file.getName());
    }
}