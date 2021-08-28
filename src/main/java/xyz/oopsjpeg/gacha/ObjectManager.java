package xyz.oopsjpeg.gacha;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface ObjectManager<T> extends Manager
{
    T get(String id);

    Map<String, T> all();

    default List<T> allAsList()
    {
        return Collections.unmodifiableList(new ArrayList<>(all().values()));
    }

    default boolean has(String id)
    {
        return all().containsKey(id);
    }

    default boolean has(T t)
    {
        return all().containsValue(t);
    }

    void fetch() throws IOException;

    default int size()
    {
        return all().size();
    }
}
