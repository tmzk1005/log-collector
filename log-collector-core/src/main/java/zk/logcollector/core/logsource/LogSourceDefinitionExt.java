package zk.logcollector.core.logsource;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

import zk.logcollector.etl.LogSourceDefinition;

@EqualsAndHashCode(callSuper = true)
@Data
public class LogSourceDefinitionExt extends LogSourceDefinition {

    private LocalDateTime createTime;

    // TODO : 增加其他扩展： 运行状态信息，metrics统计等

    public static LogSourceDefinitionExt from(LogSourceDefinition logSourceDefinition) {
        LogSourceDefinitionExt logSourceDefinitionExt = new LogSourceDefinitionExt();
        logSourceDefinitionExt.setName(logSourceDefinition.getName());
        logSourceDefinitionExt.setInput(logSourceDefinition.getInput());
        logSourceDefinitionExt.setExtractors(logSourceDefinition.getExtractors());
        logSourceDefinitionExt.setOutputs(logSourceDefinition.getOutputs());
        return logSourceDefinitionExt;
    }

}
