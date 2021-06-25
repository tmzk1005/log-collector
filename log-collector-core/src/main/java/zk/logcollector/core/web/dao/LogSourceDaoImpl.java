package zk.logcollector.core.web.dao;

import java.util.List;

import org.springframework.stereotype.Component;
import zk.logcollector.core.logsource.LogSourceDefinitionExt;

@Component
public class LogSourceDaoImpl implements LogSourceDao {

    @Override
    public void saveLogSource(LogSourceDefinitionExt logSourceDefinitionExt) {
    }

    @Override
    public List<LogSourceDefinitionExt> getLogSources(int offset, int limit) {
        return null;
    }

    @Override
    public LogSourceDefinitionExt getLogSourceByName(String name) {
        return null;
    }

    @Override
    public void deleteLogSource(String logSourceName) {
    }

    @Override
    public void updateLogSource(LogSourceDefinitionExt logSourceDefinitionExt) {
    }

    @Override
    public int getLogSourceCount() {
        return 0;
    }
}
