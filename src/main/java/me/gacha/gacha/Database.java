package me.gacha.gacha;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import com.mongodb.connection.ServerConnectionState;
import me.gacha.gacha.object.DBGuild;
import me.gacha.gacha.object.DBUser;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.HashMap;
import java.util.Map;

public class Database
{
    private final MongoClient client;
    private final MongoDatabase database;

    public Database(String uri, String databaseName)
    {
        // Add BSON/POJO translator
        CodecRegistry pojoRegistry = CodecRegistries
                .fromProviders(PojoCodecProvider.builder()
                        .automatic(true)
                        .build());
        // Add default codec for Java types
        CodecRegistry registry = CodecRegistries
                .fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoRegistry);
        // Create settings
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(uri))
                .retryWrites(true)
                .codecRegistry(registry)
                .build();
        client = MongoClients.create(settings);
        database = client.getDatabase(databaseName);
    }

    public ServerConnectionState getState()
    {
        return client.getClusterDescription().getServerDescriptions().get(0).getState();
    }

    private MongoClient getClient()
    {
        return client;
    }

    private MongoDatabase getDatabase()
    {
        return database;
    }

    public MongoCollection<DBGuild.Data> fetchGuilds()
    {
        return database.getCollection("guilds", DBGuild.Data.class);
    }

    public MongoCollection<DBUser.Data> fetchUsers()
    {
        return database.getCollection("users", DBUser.Data.class);
    }

    public Map<String, DBGuild> createGuildMap()
    {
        Map<String, DBGuild> guildMap = new HashMap<>();
        fetchGuilds().find().forEach(data -> guildMap.put(data.id, new DBGuild(data)));
        return guildMap;
    }

    public Map<String, DBUser> createUserMap()
    {
        Map<String, DBUser> userMap = new HashMap<>();
        fetchUsers().find().forEach(data -> userMap.put(data.id, new DBUser(data)));
        return userMap;
    }

    public void saveGuild(DBGuild.Data data)
    {
        fetchGuilds().replaceOne(Filters.eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }

    public void saveUser(DBUser.Data data)
    {
        fetchUsers().replaceOne(Filters.eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }
}
