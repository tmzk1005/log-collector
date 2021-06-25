package zk.logcollector.plugin.api;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Plugin的子类的不可序列化或者不应该被不同节点上的实例共享的的属性，应该
 * 以{@code transient}修饰, 在start方法中完成初始化，否则不能运行于Jet之上。
 */
public interface Plugin extends Serializable {

    default void configure(Map<String, Object> conf) throws ConfigureFailedException {
        PluginHelper.configAnnotationFields(this, conf);
        PluginHelper.configAnnotationMethods(this, conf);
    }

    default void start() {
    }

    default void stop() {
    }

}

class PluginHelper {

    static void configAnnotationFields(final Plugin pluginInstance, final Map<String, Object> conf) throws ConfigureFailedException {
        Class<?> clazz = pluginInstance.getClass();
        Set<String> fieldNames = new HashSet<>();
        while (Plugin.class.isAssignableFrom(clazz)) {
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                PluginParameter annotation = field.getAnnotation(PluginParameter.class);
                String filedName = field.getName();
                if (Objects.isNull(annotation) || fieldNames.contains(filedName)) {
                    continue;
                }
                fieldNames.add(filedName);
                String confKey = annotation.value();
                if (PluginParameter.BLANK.equals(confKey)) {
                    confKey = filedName;
                }
                if (annotation.required() && !conf.containsKey(confKey)) {
                    String message = String.format("Required parameter %s not supplied!", confKey);
                    throw new ConfigureFailedException(message);
                }
                if (!conf.containsKey(confKey)) {
                    continue;
                }
                field.setAccessible(true);
                try {
                    if (!confKey.equals(Encoder.CONF_KEY) && !confKey.equals(Decoder.CONF_KEY)) {
                        field.set(pluginInstance, conf.get(confKey));
                    } else {
                        PluginDefinition pluginDefinition = createEncoderOrDecoderFromMapConf(conf, confKey);
                        if (confKey.equals(Encoder.CONF_KEY)) {
                            Encoder<?> encoder = PluginManager.buildEncoder(pluginDefinition);
                            field.set(pluginInstance, encoder);
                        } else {
                            Decoder<?> decoder = PluginManager.buildDecoder(pluginDefinition);
                            field.set(pluginInstance, decoder);
                        }
                    }
                } catch (ReflectiveOperationException | ClassCastException exception) {
                    throw new ConfigureFailedException(exception);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    static void configAnnotationMethods(final Plugin pluginInstance, final Map<String, Object> conf) throws ConfigureFailedException {
        Class<?> clazz = pluginInstance.getClass();
        Set<String> methodNames = new HashSet<>();
        while (Plugin.class.isAssignableFrom(clazz)) {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                PluginParameter annotation = method.getAnnotation(PluginParameter.class);
                if (Objects.isNull(annotation) || method.getParameterCount() != 1 || methodNames.contains(method.getName())) {
                    continue;
                }
                methodNames.add(method.getName());
                String confKey = annotation.value();

                if (PluginParameter.BLANK.equals(confKey)) {
                    String methodName = method.getName();
                    if (methodName.startsWith("set") && methodName.length() > 3) {
                        confKey = methodName.substring(3);
                        confKey = confKey.substring(0, 1).toLowerCase() + confKey.substring(1);
                    } else if (methodName.startsWith("is") && methodName.length() > 2) {
                        confKey = methodName.substring(2);
                        confKey = confKey.substring(0, 1).toLowerCase() + confKey.substring(1);
                    }
                }

                if (PluginParameter.BLANK.equals(confKey)) {
                    throw new ConfigureFailedException("PluginParameter value must not be empty");
                }

                if (annotation.required() && !conf.containsKey(confKey)) {
                    String message = String.format("Required parameter %s not supplied!", confKey);
                    throw new ConfigureFailedException(message);
                }
                if (conf.containsKey(confKey)) {
                    method.setAccessible(true);
                    try {
                        if (!confKey.equals(Encoder.CONF_KEY) && !confKey.equals(Decoder.CONF_KEY)) {
                            method.invoke(pluginInstance, conf.get(confKey));
                        } else {
                            PluginDefinition pluginDefinition = createEncoderOrDecoderFromMapConf(conf, confKey);
                            if (confKey.equals(Encoder.CONF_KEY)) {
                                Encoder<?> encoder = PluginManager.buildEncoder(pluginDefinition);
                                method.invoke(pluginInstance, encoder);
                            } else {
                                Decoder<?> decoder = PluginManager.buildDecoder(pluginDefinition);
                                method.invoke(pluginInstance, decoder);
                            }
                        }
                    } catch (ReflectiveOperationException | ClassCastException exception) {
                        throw new ConfigureFailedException(exception);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @SuppressWarnings("unchecked")
    private static PluginDefinition createEncoderOrDecoderFromMapConf(final Map<String, Object> conf, String confKey) {
        Map<String, Object> map = (Map<String, Object>) conf.get(confKey);
        PluginDefinition pluginDefinition = new PluginDefinition();
        pluginDefinition.setName((String) map.get("name"));
        pluginDefinition.setConf((Map<String, Object>) map.get("conf"));
        return pluginDefinition;
    }

}
