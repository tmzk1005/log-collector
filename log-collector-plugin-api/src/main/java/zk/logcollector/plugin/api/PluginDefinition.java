package zk.logcollector.plugin.api;

import java.util.Map;

public class PluginDefinition {

    private String name;

    private Map<String, Object> conf;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getConf() {
        return conf;
    }

    public void setConf(Map<String, Object> conf) {
        this.conf = conf;
    }

}
