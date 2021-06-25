package zk.logcollector.core.web.dao;

import java.util.List;

import zk.logcollector.core.exception.SameLogSourceAlreadyExistException;
import zk.logcollector.core.logsource.LogSourceDefinitionExt;

public interface LogSourceDao {

    void saveLogSource(LogSourceDefinitionExt logSourceDefinitionExt) throws SameLogSourceAlreadyExistException;

    List<LogSourceDefinitionExt> getLogSources(int offset, int limit);

    LogSourceDefinitionExt getLogSourceByName(String name);

    void deleteLogSource(String logSourceName);

    void updateLogSource(LogSourceDefinitionExt logSourceDefinitionExt);

    int getLogSourceCount();

}
