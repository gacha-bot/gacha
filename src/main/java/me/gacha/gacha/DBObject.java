package me.gacha.gacha;

public abstract class DBObject<T>
{
    protected final T data;

    private boolean markedForSave;

    public DBObject(T data)
    {
        this.data = data;
    }

    public T getData()
    {
        return data;
    }

    public boolean isMarkedForSave()
    {
        return markedForSave;
    }

    public void setMarkedForSave(boolean b)
    {
        this.markedForSave = b;
    }

    /**
     * Mark this object to be saved to a Database.
     */
    public void markForSave()
    {
        markedForSave = true;
    }
}
