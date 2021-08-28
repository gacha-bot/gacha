package xyz.oopsjpeg.gacha.manager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.Core;
import xyz.oopsjpeg.gacha.Manager;
import xyz.oopsjpeg.gacha.object.data.ProfileData;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.util.Collection;

/**
 * Manages all MongoDB interactions.
 * Created by oopsjpeg on 2/3/2019.
 */
public class MongoManager implements Manager
{
    private final Core core;
    private final MongoDatabase database;

    public MongoManager(Core core, String conString)
    {
        this.core = core;

        ConnectionString connection = new ConnectionString(conString);
        // Add BSON/POJO translator
        CodecRegistry pojoRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        // Add default codec for Java types
        CodecRegistry registry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoRegistry);
        // Create settings
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connection)
                .codecRegistry(registry)
                .build();

        this.database = MongoClients.create(settings).getDatabase("gacha");
    }

    public MongoCollection<ProfileData> getProfileCollection()
    {
        return database.getCollection("profiles", ProfileData.class);
    }

    public void saveProfiles(Collection<Profile> profiles)
    {
        getLogger().info("Saving " + profiles.size() + " profiles");
        profiles.stream().filter(Profile::isMarkedForSave).forEach(this::saveProfile);
    }

    public void saveProfile(Profile profile)
    {
        profile.setMarkedForSave(false);
        saveProfile(profile.getData());
    }

    public void saveProfile(ProfileData data)
    {
        getLogger().info("Saving profile data for ID " + data.id);
        getProfileCollection().replaceOne(Filters.eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }

    @Override
    public Core getCore()
    {
        return core;
    }
}
