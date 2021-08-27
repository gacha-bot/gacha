package xyz.oopsjpeg.gacha;

import com.mongodb.client.MongoCollection;
import discord4j.core.object.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.oopsjpeg.gacha.object.data.ProfileData;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileManager
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gacha gacha;
    private final Map<String, Profile> profileMap = new HashMap<>();

    public ProfileManager(Gacha gacha)
    {
        this.gacha = gacha;
    }

    public Gacha getGacha()
    {
        return gacha;
    }

    public Profile register(User user)
    {
        String id = user.getId().asString();
        Profile profile = Profile.create(this, id);
        profile.getResources().addCrystals(Constants.STARTING_CRYSTALS);
        profile.markForSave();
        profileMap.put(id, profile);
        return profile;
    }

    public Profile get(String id)
    {
        return profileMap.getOrDefault(id, null);
    }

    public Profile get(User user)
    {
        return get(user.getId().asString());
    }

    public boolean has(String id)
    {
        return profileMap.containsKey(id);
    }

    public boolean has(User user)
    {
        return has(user.getId().asString());
    }

    public Map<String, Profile> all()
    {
        return profileMap;
    }

    public List<Profile> allAsList()
    {
        return new ArrayList<>(profileMap.values());
    }

    public void fetch() throws IOException
    {
        profileMap.clear();
        MongoCollection<ProfileData> collection = gacha.getMongo().getProfileCollection();
        collection.find().forEach(data ->
        {
            Profile profile = new Profile(this, data);
            logger.info("Fetched profile data for ID " + data.id);
            profileMap.put(profile.getId(), profile);
        });
        logger.info("Fetched " + profileMap.size() + " profiles");
    }
}
