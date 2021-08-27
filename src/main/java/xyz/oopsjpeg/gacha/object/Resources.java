package xyz.oopsjpeg.gacha.object;

import xyz.oopsjpeg.gacha.object.data.ResourcesData;
import xyz.oopsjpeg.gacha.object.user.Profile;

public class Resources
{
    private final Profile profile;
    private final ResourcesData data;

    public Resources(Profile profile, ResourcesData data)
    {
        this.profile = profile;
        this.data = data;
    }

    public static Resources create(Profile profile)
    {
        ResourcesData data = new ResourcesData();
        return new Resources(profile, data);
    }

    public int getCrystals()
    {
        return data.crystals;
    }

    public void setCrystals(int cr)
    {
        data.crystals = cr;
    }

    public void addCrystals(int cr)
    {
        setCrystals(getCrystals() + cr);
    }

    public void subCrystals(int cr)
    {
        setCrystals(getCrystals() - cr);
    }

    public int getVioletRunes()
    {
        return data.violetRunes;
    }

    public void setVioletRunes(int vr)
    {
        data.violetRunes = vr;
    }

    public void addVioletRunes(int vr)
    {
        setVioletRunes(getVioletRunes() + vr);
    }

    public void subVioletRunes(int vr)
    {
        setVioletRunes(getVioletRunes() - vr);
    }

    public int getZenithCores()
    {
        return data.zenithCores;
    }

    public void setZenithCores(int zc)
    {
        data.zenithCores = zc;
    }

    public void addZenithCores(int zc)
    {
        setZenithCores(getZenithCores() + zc);
    }

    public void subZenithCores(int zc)
    {
        setZenithCores(getZenithCores() - zc);
    }

    public Profile getProfile()
    {
        return profile;
    }

    public ResourcesData getData()
    {
        return data;
    }
}
