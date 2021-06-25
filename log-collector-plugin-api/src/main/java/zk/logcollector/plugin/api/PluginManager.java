package zk.logcollector.plugin.api;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public final class PluginManager {

    private static final String INPUTS_LOCATION = "META-INF/log-collector-inputs.properties";
    private static final String EXTRACTORS_LOCATION = "META-INF/log-collector-extractors.properties";
    private static final String OUTPUTS_LOCATION = "META-INF/log-collector-outputs.properties";
    private static final String ENCODER_LOCATION = "META-INF/log-collector-encoder.properties";
    private static final String DECODER_LOCATION = "META-INF/log-collector-decoder.properties";

    private static final Map<String, String> INPUT_PLUGINS = new HashMap<>();
    private static final Map<String, String> EXTRACTOR_PLUGINS = new HashMap<>();
    private static final Map<String, String> OUTPUT_PLUGINS = new HashMap<>();
    private static final Map<String, String> ENCODER_PLUGINS = new HashMap<>();
    private static final Map<String, String> DECODER_PLUGINS = new HashMap<>();

    static {
        load();
    }

    private PluginManager() {
    }

    private static void load() {
        INPUT_PLUGINS.putAll(loadClassPathProperties(INPUTS_LOCATION));
        EXTRACTOR_PLUGINS.putAll(loadClassPathProperties(EXTRACTORS_LOCATION));
        OUTPUT_PLUGINS.putAll(loadClassPathProperties(OUTPUTS_LOCATION));
        ENCODER_PLUGINS.putAll(loadClassPathProperties(ENCODER_LOCATION));
        DECODER_PLUGINS.putAll(loadClassPathProperties(DECODER_LOCATION));
    }

    private static Map<String, String> loadClassPathProperties(String location) {
        Map<String, String> keyValues = new HashMap<>();
        try {
            Enumeration<URL> urls = PluginManager.class.getClassLoader().getResources(location);
            while (urls.hasMoreElements()) {
                Properties properties = new Properties();
                properties.load(urls.nextElement().openStream());
                for (String key : properties.stringPropertyNames()) {
                    if (keyValues.containsKey(key)) {
                        // 重复的插件，忽略
                        continue;
                    }
                    keyValues.put(key, properties.getProperty(key));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return keyValues;
    }

    @SuppressWarnings("unchecked")
    public static Input buildInput(PluginDefinition pluginDefinition) throws ReflectiveOperationException, ConfigureFailedException {
        String className = INPUT_PLUGINS.get(pluginDefinition.getName());
        Class<?> clazz = Class.forName(className);
        if (!Input.class.isAssignableFrom(clazz)) {
            throw new ReflectiveOperationException("InputDefinition.class is not assignable from " + clazz.getName());
        }
        Plugin plugin = buildPlugin((Class<? extends Input>) clazz, pluginDefinition.getConf());
        return (Input) plugin;

    }

    @SuppressWarnings("unchecked")
    public static Extractor buildExtractor(PluginDefinition pluginDefinition) throws ReflectiveOperationException, ConfigureFailedException {
        String className = EXTRACTOR_PLUGINS.get(pluginDefinition.getName());
        Class<?> clazz = Class.forName(className);
        if (!Extractor.class.isAssignableFrom(clazz)) {
            throw new ReflectiveOperationException("ExtractorDefinition.class is not assignable from " + clazz.getName());
        }
        Plugin plugin = buildPlugin((Class<? extends Extractor>) clazz, pluginDefinition.getConf());
        return (Extractor) plugin;
    }

    @SuppressWarnings("unchecked")
    public static Output buildOutput(PluginDefinition pluginDefinition) throws ReflectiveOperationException, ConfigureFailedException {
        String className = OUTPUT_PLUGINS.get(pluginDefinition.getName());
        Class<?> clazz = Class.forName(className);
        if (!Output.class.isAssignableFrom(clazz)) {
            throw new ReflectiveOperationException("OutputDefinition.class is not assignable from " + clazz.getName());
        }
        Plugin plugin = buildPlugin((Class<? extends Output>) clazz, pluginDefinition.getConf());
        return (Output) plugin;
    }

    @SuppressWarnings("unchecked")
    static Encoder<?> buildEncoder(PluginDefinition pluginDefinition) throws ReflectiveOperationException, ConfigureFailedException {
        String className = ENCODER_PLUGINS.get(pluginDefinition.getName());
        Class<?> clazz = Class.forName(className);
        if (!Encoder.class.isAssignableFrom(clazz)) {
            throw new ReflectiveOperationException("Encoder.class is not assignable from " + clazz.getName());
        }
        Plugin plugin = buildPlugin((Class<? extends Encoder<?>>) clazz, pluginDefinition.getConf());
        return (Encoder<?>) plugin;
    }

    @SuppressWarnings("unchecked")
    static Decoder<?> buildDecoder(PluginDefinition pluginDefinition) throws ReflectiveOperationException, ConfigureFailedException {
        String className = DECODER_PLUGINS.get(pluginDefinition.getName());
        Class<?> clazz = Class.forName(className);
        if (!Decoder.class.isAssignableFrom(clazz)) {
            throw new ReflectiveOperationException("Encoder.class is not assignable from " + clazz.getName());
        }
        Plugin plugin = buildPlugin((Class<? extends Decoder<?>>) clazz, pluginDefinition.getConf());
        return (Decoder<?>) plugin;
    }

    private static Plugin buildPlugin(Class<? extends Plugin> clazz, Map<String, Object> conf) throws ReflectiveOperationException, ConfigureFailedException {
        Objects.requireNonNull(clazz);
        Constructor<? extends Plugin> defaultConstructor = clazz.getDeclaredConstructor();
        defaultConstructor.setAccessible(true);
        Plugin plugin = defaultConstructor.newInstance();
        plugin.configure(conf);
        return plugin;
    }

}
