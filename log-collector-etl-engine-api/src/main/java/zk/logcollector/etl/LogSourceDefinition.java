package zk.logcollector.etl;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

import zk.logcollector.plugin.api.PluginDefinition;

/**
 * @author zoukang60456
 */
@Data
@NoArgsConstructor
public class LogSourceDefinition {

    private String name;

    private PluginDefinition input;

    private List<PluginDefinition> extractors;

    private List<PluginDefinition> outputs;

}
