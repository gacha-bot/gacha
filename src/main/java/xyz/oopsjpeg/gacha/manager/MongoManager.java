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
import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.object.data.ProfileData;
import xyz.oopsjpeg.gacha.object.user.Profile;

import java.util.Collection;
import java.util.HashMap;

/**
 * Manages all MongoDB interactions.
 * Created by oopsjpeg on 2/3/2019.
 */
public class MongoManager
{
    private final Gacha gacha;
    private final MongoDatabase database;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MongoManager(Gacha gacha, String conString)
    {
        this.gacha = gacha;

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

    public HashMap<String, Profile> fetchProfiles()
    {
        HashMap<String, Profile> profileMap = new HashMap<>();
        // Add each user into the map by ID
        getProfileCollection().find().forEach(data ->
        {
            Profile profile = new Profile(gacha, data);
            logger.info("Fetched profile data for ID " + data.id);
            profileMap.put(profile.getId(), profile);
        });
        return profileMap;
    }

    public void saveProfiles(Collection<Profile> profiles)
    {
        logger.info("Saving " + profiles.size() + " profiles");
        profiles.stream().filter(Profile::isMarkedForSave).forEach(this::saveProfile);
    }

    public void saveProfile(Profile profile)
    {
        profile.setMarkedForSave(false);
        saveProfile(profile.getData());
    }

    public void saveProfile(ProfileData data)
    {
        logger.info("Saving profile data for ID " + data.id);
        getProfileCollection().replaceOne(Filters.eq("_id", data.id), data, new ReplaceOptions().upsert(true));
    }
}
