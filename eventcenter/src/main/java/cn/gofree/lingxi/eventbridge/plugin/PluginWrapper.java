package cn.gofree.lingxi.eventbridge.plugin;

public class PluginWrapper<T> {
    private final Class<? extends T> klass;
    private final ClassLoader classLoader;
    private final String name;
    private final String location;
    private final PluginType type;
    private final String typeName;

    public PluginWrapper(Class<? extends T> klass, ClassLoader classLoader) {
        this.klass = klass;
        this.classLoader = classLoader;
        this.name = klass.getName();
        this.type = PluginType.from(klass);
        this.typeName = type.toString();
        this.location = classLoader instanceof PluginLoader? ((PluginLoader) classLoader).location():"classpath";
    }
    public ClassLoader getClassLoader() { return this.classLoader; }
}
