package cn.gofree.lingxi.eventcenter.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.net.URLClassLoader;

public class PluginLoader extends URLClassLoader {
    private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);
    private final URL pluginLocationUrl;

    static {
        /**
         * 支持可并行类加载机制，将类加载锁的粒度降低，以提高类加载的并发性能。
         * 它的作用是将一个类加载器声明为可并行类加载器，并将类加载锁的级别从 ClassLoader 对象本身降低为要加载的类名这个级别。
         * 这样一来，当多个线程同时尝试加载不同的类时，它们就可以同时进行，而不必竞争 ClassLoader 对象的锁，从而避免了死锁和性能问题。
         */
        ClassLoader.registerAsParallelCapable();
    }

    public PluginLoader(URL pluginLocationUrl, URL[] urls, ClassLoader parent) {
        super(urls, parent);
        this.pluginLocationUrl = pluginLocationUrl;
    }
    public PluginLoader(URL pluginLocationUrl, URL[] urls) {
        super(urls);
        this.pluginLocationUrl = pluginLocationUrl;
    }

    public String location() { return pluginLocationUrl.toString();}
    @Override
    public String toString() {
        return "PluginClassLoader(pluginLocationUrl=" + pluginLocationUrl + ")";
    }

    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> klass = findLoadedClass(name);
            if (klass == null) {
                try {
                    if (!PluginUtils.shouldNotLoadInIsolation(name)) {
                        klass = findClass(name);
                    }
                } catch (ClassNotFoundException ex) {
                    log.trace("Couldn't load class " + name + ", falling back on parent", ex);
                }
            }
            if (klass == null) {
                klass = super.loadClass(name, false);
            }
            if (resolve) {
                resolveClass(klass);
            }
            return klass;

        }
    }
}
