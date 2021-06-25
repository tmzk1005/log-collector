package zk.logcollector.plugin.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class LogRecord implements Serializable {

    public static final String MESSAGE = "message";

    private Map<String, Object> fields = new HashMap<>();

    public LogRecord() {
    }

    public LogRecord(String message) {
        this.fields.put(MESSAGE, message);
    }

    public void setField(String name, Object value) {
        fields.put(name, value);
    }

    public Object getField(String name) {
        return fields.get(name);
    }

    public Object hasField(String name) {
        return this.fields.containsKey(name);
    }

    public Object getRawMessage() {
        return fields.get(MESSAGE);
    }

    public Map<String, Object> getAllFields() {
        return fields;
    }

    public void fromAnother(LogRecord logRecord) {
        fields = logRecord.fields;
    }

}
