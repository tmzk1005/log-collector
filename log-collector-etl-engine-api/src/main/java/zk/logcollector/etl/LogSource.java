package zk.logcollector.etl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import zk.logcollector.plugin.api.ConfigureFailedException;
import zk.logcollector.plugin.api.Extractor;
import zk.logcollector.plugin.api.Input;
import zk.logcollector.plugin.api.Output;
import zk.logcollector.plugin.api.PluginDefinition;
import zk.logcollector.plugin.api.PluginManager;

/**
 * @author zoukang60456
 */
@Data
@NoArgsConstructor
public class LogSource implements Serializable {

    private String name;

    private Input input;

    private List<Extractor> extractors;

    private List<Output> outputs;

    public static LogSource fromLogSourceDefinition(LogSourceDefinition logSourceDefinition) throws ReflectiveOperationException, ConfigureFailedException {
        LogSource logSource = new LogSource();
        logSource.name = logSourceDefinition.getName();
        logSource.input = PluginManager.buildInput(logSourceDefinition.getInput());
        logSource.extractors = new ArrayList<>();
        for (PluginDefinition pluginDefinition : logSourceDefinition.getExtractors()) {
            logSource.extractors.add(PluginManager.buildExtractor(pluginDefinition));
        }
        logSource.outputs = new ArrayList<>();
        for (PluginDefinition pluginDefinition : logSourceDefinition.getOutputs()) {
            logSource.outputs.add(PluginManager.buildOutput(pluginDefinition));
        }
        return logSource;
    }

}
