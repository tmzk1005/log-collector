package zk.logcollector.etl.hazaealcast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Job;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;

import zk.logcollector.etl.LogSource;

@Slf4j
public class HazelcastJetEtlEngine {

    private static final String HAZELCAST_CLUSTER_NAME = "log_collector_hazelcast";

    private static final String IMAP_KEY_LOG_SOURCES = "logSources";

    private final JetInstance jetInstance;

    private final IMap<String, LogSource> logSources;

    public HazelcastJetEtlEngine() {
        JetConfig jetConfig = new JetConfig();
        Config hazelcastConfig = jetConfig.getHazelcastConfig();

        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setImplementation(new LogSourceMapStore());
        mapStoreConfig.setWriteDelaySeconds(0);

        MapConfig mapConfig = hazelcastConfig.getMapConfig(IMAP_KEY_LOG_SOURCES);
        mapConfig.setMapStoreConfig(mapStoreConfig);
        hazelcastConfig.setClusterName(HAZELCAST_CLUSTER_NAME);

        jetInstance = Jet.newJetInstance(jetConfig);
        logSources = jetInstance.getMap(IMAP_KEY_LOG_SOURCES);
        log.info("JetInstance initialized succeed!");
    }

    public void createNewLogSource(LogSource logSource) {
        if (logSources.containsKey(logSource.getName())) {
            // TODO
            return;
        }
        try {
            logSources.set(logSource.getName(), logSource);
        } catch (Exception e) {
            // TODO:
        }
    }

    public List<LogSource> getLogSources() {
        Set<String> keys = logSources.keySet();
        List<LogSource> result = new ArrayList<>();
        for (String key : keys) {
            result.add(logSources.get(key));
        }
        return result;
    }

    public void startLogSource(String logSourceName) {
        LogSource logSource = logSources.get(logSourceName);
        Pipeline pipeline = JetPipelineFactory.fromLogSource(logSource);
        JobConfig jobConfig = new JobConfig();
        jobConfig.setName(logSourceName);
        jobConfig.setAutoScaling(true);
        jobConfig.setMetricsEnabled(true);
        jetInstance.newJob(pipeline, jobConfig);
    }

    public void stopLogSource(String logSourceName) {
        Job job = jetInstance.getJob(logSourceName);
        if (Objects.isNull(job)) {
            return;
        }
        try {
            job.cancel();
        } catch (IllegalStateException ise) {
            log.error("Exception happened while stop job with name = {}", logSourceName, ise);
        }
    }

    public void removeLogSource(String logSourceName) {
        stopLogSource(logSourceName);
        logSources.remove(logSourceName);
    }

}
