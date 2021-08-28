package xyz.oopsjpeg.gacha;

import com.mongodb.client.MongoCollection;
import discord4j.core.object.entity.User;
import xyz.oopsjpeg.gacha.object.data.ProfileData;
import xyz.oopsjpeg.gacha.object.user.Profile;
import xyz.oopsjpeg.gacha.util.Constants;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileManager implements ObjectManager<Profile>
{
    private final Core core;
    private final Map<String, Profile> profileMap = new HashMap<>();

    public ProfileManager(Core core)
    {
        this.core = core;
    }

    @Override
    public Core getCore()
    {
        return core;
    }

    private Profile register(String id)
    {
        Profile profile = Profile.create(this, id);
        profile.getResources().addCrystals(Constants.STARTING_CRYSTALS);
        profile.markForSave();
        profileMap.put(id, profile);
        return profile;
    }

    @Override
    public Profile get(String id)
    {
        if (!profileMap.containsKey(id))
            return register(id);
        return profileMap.getOrDefault(id, null);
    }

    public Profile get(User user)
    {
        return get(user.getId().asString());
    }

    @Override
    public Map<String, Profile> all()
    {
        return profileMap;
    }

    @Override
    public void fetch() throws IOException
    {
        profileMap.clear();
        MongoCollection<ProfileData> collection = core.getMongo().getProfileCollection();
        collection.find().forEach(data ->
        {
            Profile profile = new Profile(this, data);
            getLogger().info("Fetched profile data for ID " + data.id);
            profileMap.put(profile.getId(), profile);
        });
        getLogger().info("Fetched " + profileMap.size() + " profiles");
    }
}
