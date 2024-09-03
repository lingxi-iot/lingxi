package cn.gofree.lingxi.eventcenter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Properties;

@Slf4j
@Component
public class ConfigLoader {

    @Autowired
    private PropertiesResolveService propertiesResolveService;

    private Properties properties;

    public ConfigLoader() {
        try {
            this.properties = PropertiesLoaderUtils.loadAllProperties("runtime.properties");
        } catch (IOException e) {
            log.warn("init runtime properties failed ", e);
        }
    }

    public String getString(String name, String defaultValue) {
        String result = getValue(name);
        if (StringUtils.isBlank(result)) {
            result = defaultValue;
        }
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    public String getString(String name){
        String result = getValue(name);
        log.debug("Load property '" + name + "' = " + result);
        return result;
    }

    private String getValue(String name){
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String value = null;
        try {
            if (propertiesResolveService != null) {
                value = propertiesResolveService.getPropertiesValue(name);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Failed load property '" + name + "' from spring.");
        }
        if (properties != null && StringUtils.isBlank((value))) {
            value = properties.getProperty(name);
        }
        return value;
    }
}
