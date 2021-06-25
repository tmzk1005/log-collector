package zk.logcollector.core.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import zk.logcollector.core.exception.CreateLogSourceFailedException;
import zk.logcollector.core.exception.SameLogSourceAlreadyExistException;
import zk.logcollector.core.logsource.LogSourceDefinitionExt;
import zk.logcollector.core.web.PaginationData;
import zk.logcollector.core.web.RestResponse;
import zk.logcollector.core.web.service.LogSourceService;
import zk.logcollector.etl.LogSourceDefinition;

@RestController
@RequestMapping("/logSource")
@Slf4j
public class LogSourceController {

    @Autowired
    private LogSourceService logSourceService;

    @PostMapping("")
    public RestResponse<Void> createNewLogSource(@RequestBody LogSourceDefinition logSourceDefinition) throws CreateLogSourceFailedException {
        logSourceService.createNewLogSource(logSourceDefinition);
        return RestResponse.ok();
    }

    @GetMapping("")
    public RestResponse<PaginationData<LogSourceDefinitionExt>> getAllLogSources(
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") int pageSize
    ) {
        // TODO: 校验参数
        return RestResponse.of(logSourceService.getLogSources(pageNum, pageSize));
    }

    @PatchMapping("/{logSourceName}/_start")
    public RestResponse<Void> startLogSource(@PathVariable("logSourceName") String logSourceName) {
        logSourceService.startLogSource(logSourceName);
        return RestResponse.ok();
    }

    @PatchMapping("/{logSourceName}/_stop")
    public RestResponse<Void> stopLogSource(@PathVariable("logSourceName") String logSourceName) {
        logSourceService.stopLogSource(logSourceName);
        return RestResponse.ok();
    }

    @DeleteMapping("/{logSourceName}")
    public RestResponse<Void> deleteLogSource(@PathVariable("logSourceName") String logSourceName) {
        logSourceService.deleteLogSource(logSourceName);
        return RestResponse.ok();
    }

    @ExceptionHandler(CreateLogSourceFailedException.class)
    public RestResponse<Void> handleCreateLogSourceFailedException(CreateLogSourceFailedException exception) {
        log.error("Failed to create new log source.", exception);
        return RestResponse.ofError("Create log source failed.");
    }

    @ExceptionHandler(SameLogSourceAlreadyExistException.class)
    public RestResponse<Void> handleSameLogSourceAlreadyExistException(SameLogSourceAlreadyExistException exception) {
        log.error(
                "Failed to create log source named {} because of same name already exist.",
                exception.getLogSourceName()
        );
        return RestResponse.ofError("Same log source name already exist.");
    }

}
