package zk.logcollector.core.web.service;

import zk.logcollector.core.exception.CreateLogSourceFailedException;
import zk.logcollector.core.logsource.LogSourceDefinitionExt;
import zk.logcollector.core.web.PaginationData;
import zk.logcollector.etl.LogSourceDefinition;

public interface LogSourceService {

    void createNewLogSource(LogSourceDefinition logSourceDefinition) throws CreateLogSourceFailedException;

    PaginationData<LogSourceDefinitionExt> getLogSources(int pageNum, int pageSize);

    LogSourceDefinitionExt getLogSourceByName(String logSourceName);

    void startLogSource(String logSourceName);

    void stopLogSource(String logSourceName);

    void deleteLogSource(String logSourceName);

}
