package zk.logcollector.etl.hazaealcast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hazelcast.map.MapStore;

import zk.logcollector.etl.LogSource;

public class LogSourceMapStore implements MapStore<String, LogSource> {

    // TODO : 持久化数据

    private final Map<String, LogSource> mockStore = new ConcurrentHashMap<>();

    @Override
    public void store(String key, LogSource value) {
        System.out.println("store " + key);
        mockStore.put(key, value);
    }

    @Override
    public void storeAll(Map<String, LogSource> map) {
        System.out.println("storeAll");
        mockStore.putAll(map);
    }

    @Override
    public void delete(String key) {
        System.out.println("delete " + key);
        mockStore.remove(key);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        System.out.println("deleteAll");
        for (String key : keys) {
            mockStore.remove(key);
        }
    }

    @Override
    public LogSource load(String key) {
        System.out.println("load " + key);
        return mockStore.get(key);
    }

    @Override
    public Map<String, LogSource> loadAll(Collection<String> keys) {
        System.out.println("loadAll");
        return new HashMap<>(mockStore);
    }

    @Override
    public Iterable<String> loadAllKeys() {
        System.out.println("loadAllKeys");
        return mockStore.keySet();
    }

}
