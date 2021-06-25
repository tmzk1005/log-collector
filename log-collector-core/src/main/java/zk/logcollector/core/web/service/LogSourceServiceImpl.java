package zk.logcollector.core.web.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zk.logcollector.core.exception.CreateLogSourceFailedException;
import zk.logcollector.core.logsource.LogSourceDefinitionExt;
import zk.logcollector.core.web.PaginationData;
import zk.logcollector.core.web.dao.LogSourceDao;
import zk.logcollector.etl.EtlEngine;
import zk.logcollector.etl.LogSourceDefinition;

@Service
public class LogSourceServiceImpl implements LogSourceService {

    @Autowired
    private LogSourceDao logSourceDao;

    @Autowired
    private EtlEngine etlEngine;

    @Override
    public void createNewLogSource(LogSourceDefinition logSourceDefinition) throws CreateLogSourceFailedException {
        LogSourceDefinitionExt logSourceDefinitionExt = LogSourceDefinitionExt.from(logSourceDefinition);
        logSourceDefinitionExt.setCreateTime(LocalDateTime.now());
        logSourceDao.saveLogSource(logSourceDefinitionExt);
    }

    @Override
    public PaginationData<LogSourceDefinitionExt> getLogSources(int pageNum, int pageSize) {
        List<LogSourceDefinitionExt> logSources = logSourceDao.getLogSources((pageNum - 1) * pageSize, pageSize);
        // TODO: 从EtlEngine实时更新LogSourceDefinitionExt的状态信息
        int logSourceCount = logSourceDao.getLogSourceCount();
        return PaginationData.of(pageNum, pageSize, logSourceCount, logSources);
    }

    @Override
    public LogSourceDefinitionExt getLogSourceByName(String logSourceName) {
        // TODO: 从EtlEngine实时更新LogSourceDefinitionExt的状态信息然后才返回
        return logSourceDao.getLogSourceByName(logSourceName);

    }

    @Override
    public void startLogSource(String logSourceName) {
        // TODO
    }

    @Override
    public void stopLogSource(String logSourceName) {
        // TODO
    }

    @Override
    public void deleteLogSource(String logSourceName) {
        // TODO[
    }

}
