package zk.logcollector.etl;

public interface EtlEngine {

    void run(LogSourceDefinition logSource);

    void stop(String logSourceName);

}
