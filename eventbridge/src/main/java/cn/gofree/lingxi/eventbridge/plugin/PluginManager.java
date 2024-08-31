package cn.gofree.lingxi.eventbridge.plugin;

import io.openmessaging.connector.api.component.Transform;
import io.openmessaging.connector.api.component.connector.Connector;
import io.openmessaging.connector.api.component.task.Task;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.ReflectionsException;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

public class PluginManager extends URLClassLoader {
    private static final Logger log = LoggerFactory.getLogger(PluginManager.class);

    private String pluginPath;

    private final Map<String, PluginWrapper> classLoaderMap = new HashMap<>();

    public PluginManager() {
        super(new URL[0], PluginManager.class.getClassLoader());
    }

    public void initPlugin(String pluginPath) {
        this.pluginPath = pluginPath;
        List<String> pluginPaths = initPluginPath(this.pluginPath);
        for (String configPath : pluginPaths) {
            loadPlugin(configPath);
        }
    }
    public String getPluginPath()
    {
        return this.pluginPath;
    }
    private List<String> initPluginPath(String plugin) {
        List<String> pluginPaths = new ArrayList<>();
        if (StringUtils.isNotEmpty(plugin)) {
            String[] strArr = plugin.split(",");
            for (String path : strArr) {
                if (StringUtils.isNotEmpty(path)) {
                    pluginPaths.add(path);
                }
            }
        }
        return pluginPaths;
    }

    private void loadPlugin(String path) {
        Path pluginPath = Paths.get(path).toAbsolutePath();
        path = pluginPath.toString();
        try {
            if (Files.isDirectory(pluginPath)) {
                for (Path pluginLocation : PluginUtils.pluginLocations(pluginPath)) {
                    registerPlugin(pluginLocation);
                }
            } else if (PluginUtils.isArchive(pluginPath)) {
                registerPlugin(pluginPath);
            }
        } catch (IOException e) {
            log.error("register plugin error, path: {}, e: {}", path, e);
        }
    }

    private void doLoad(
            ClassLoader loader,
            URL[] urls
    ) {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setClassLoaders(new ClassLoader[]{loader});
        builder.addUrls(urls);
        builder.setScanners(new SubTypesScanner());
        builder.useParallelExecutor();
        Reflections reflections = new PluginReflections(builder);
        getPlugin(reflections, Connector.class, loader);
        getPlugin(reflections, Task.class, loader);
        getPlugin(reflections, Transform.class, loader);
    }

    private <T> Collection<Class<? extends T>> getPlugin(
            Reflections reflections,
            Class<T> klass,
            ClassLoader loader
    ) {
        Set<Class<? extends T>> plugins = reflections.getSubTypesOf(klass);
        Collection<Class<? extends T>> result = new ArrayList<>();
        for (Class<? extends T> plugin : plugins) {
            log.info("Loaded plugin: {}", plugin.getName());
            classLoaderMap.put(plugin.getName(), new PluginWrapper(plugin, loader));
            result.add(plugin);
        }
        return result;
    }

    private static class PluginReflections extends Reflections {

        public PluginReflections(Configuration configuration) {
            super(configuration);
        }

        @Override
        protected void scan(URL url) {
            try {
                super.scan(url);
            } catch (ReflectionsException e) {
                Logger log = Reflections.log;
                if (log != null && log.isWarnEnabled()) {
                    log.warn("Scan url error. ignoring the exception and continuing", e);
                }
            }
        }
    }

    private static PluginLoader newPluginClassLoader(
            final URL pluginLocation,
            final URL[] urls,
            final ClassLoader parent
    ) {
        return AccessController.doPrivileged(
                (PrivilegedAction<PluginLoader>) () -> new PluginLoader(pluginLocation, urls, parent)
        );
    }

    private void registerPlugin(Path pluginLocation)
            throws IOException {
        log.info("Loading plugin from: {}", pluginLocation);
        List<URL> pluginUrls = new ArrayList<>();
        for (Path path : PluginUtils.pluginUrls(pluginLocation)) {
            pluginUrls.add(path.toUri().toURL());
        }
        URL[] urls = pluginUrls.toArray(new URL[0]);
        if (log.isDebugEnabled()) {
            log.debug("Loading plugin urls: {}", Arrays.toString(urls));
        }
        PluginLoader loader = newPluginClassLoader(
                pluginLocation.toUri().toURL(),
                urls,
                this
        );
        doLoad(loader, urls);
    }

    public ClassLoader getPluginClassLoader(String pluginName) {
        log.debug("???????????");

        PluginWrapper pluginWrapper = classLoaderMap.get(pluginName);
        if (null != pluginWrapper) {
            return pluginWrapper.getClassLoader();
        }
        return null;
    }

    public ClassLoader currentThreadLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    public static ClassLoader compareAndSwapLoaders(ClassLoader loader) {
        ClassLoader current = Thread.currentThread().getContextClassLoader();
        if (null != current && !current.equals(loader)) {
            Thread.currentThread().setContextClassLoader(loader);
        }
        return current;
    }

}
