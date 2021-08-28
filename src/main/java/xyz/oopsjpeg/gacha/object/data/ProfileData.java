package xyz.oopsjpeg.gacha.object.data;

import org.bson.codecs.pojo.annotations.BsonId;

import java.util.HashMap;
import java.util.Map;

public class ProfileData
{
    @BsonId
    public String id;
    public ResourcesData resources;
    public Map<String, ProfileCardData> cards = new HashMap<>();
    public String dailyDate;
    public String weeklyDate;
    public String description;
    public String favoriteCardId;
    public String voteDate;
}
