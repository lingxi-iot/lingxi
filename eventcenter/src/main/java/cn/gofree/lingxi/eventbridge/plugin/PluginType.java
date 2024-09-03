package cn.gofree.lingxi.eventbridge.plugin;

import io.openmessaging.connector.api.component.task.sink.SinkConnector;
import io.openmessaging.connector.api.component.task.source.SourceConnector;

import java.util.Locale;

public enum PluginType {
    SOURCE(SourceConnector.class),
    SINK(SinkConnector.class),
    UNKNOWN(Object.class);
    ;

    private final Class<?> klass;

    PluginType(Class<?> klass) {
        this.klass = klass;
    }
    public static PluginType from(Class<?> klass) {
        for (PluginType type : PluginType.values()) {
            if (type.klass.isAssignableFrom(klass)) {
                return type;
            }
        }
        return UNKNOWN;
    }
    public String simpleName() {
        return klass.getSimpleName();
    }
    @Override
    public String toString() {
        return super.toString().toLowerCase(Locale.ROOT);
    }
}
