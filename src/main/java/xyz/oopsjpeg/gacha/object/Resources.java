package xyz.oopsjpeg.gacha.object;

import xyz.oopsjpeg.gacha.Gacha;
import xyz.oopsjpeg.gacha.object.data.ResourcesData;

public class Resources
{
    private final Gacha gacha;
    private final ResourcesData data;

    public Resources(Gacha gacha, ResourcesData data)
    {
        this.gacha = gacha;
        this.data = data;
    }

    public static Resources create(Gacha gacha)
    {
        ResourcesData data = new ResourcesData();
        return new Resources(gacha, data);
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

    public Gacha getGacha()
    {
        return gacha;
    }

    public ResourcesData getData()
    {
        return data;
    }
}
