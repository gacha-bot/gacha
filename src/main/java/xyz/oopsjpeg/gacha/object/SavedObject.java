package xyz.oopsjpeg.gacha.object;

public interface SavedObject
{
    boolean isMarkedForSave();

    void setMarkedForSave(boolean b);

    default void markForSave()
    {
        setMarkedForSave(true);
    }
}
