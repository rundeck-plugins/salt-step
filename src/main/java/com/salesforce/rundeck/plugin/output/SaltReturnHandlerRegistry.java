package com.salesforce.rundeck.plugin.output;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.google.common.collect.Maps;

@Component
public class SaltReturnHandlerRegistry {
    protected static final String RETURN_HANDLER_CONFIGURATION_PROPERTY_KEY = "saltStep.returnHandlers";
    protected static final String RUNDECK_CONFIGURATION_LOCATION_KEY = "rundeck.config.location";
    protected static final String HANDLER_MAPPINGS_KEY = "handlerMappings";

    /**
     * Keys in this map are either the module name OR a fully qualified function name (i.e.
     * module.function)
     */
    protected final Map<String, SaltReturnHandler> handlerMap = Maps.newHashMap();

    protected final String configurationFile;

    @Autowired
    public SaltReturnHandlerRegistry(@Value("${saltReturnerRegistry.defaultReturnerConfiguration}") String configurationFile) {
        this.configurationFile = configurationFile;
    }

    @PostConstruct
    public void configure() {
        try {
            configureFromResource(configurationFile);

            String configLocation = System.getProperty(RUNDECK_CONFIGURATION_LOCATION_KEY);
            if (configLocation != null) {
                Properties properties = new Properties();
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(configLocation);
                    properties.load(fis);
                    if (properties.containsKey(RETURN_HANDLER_CONFIGURATION_PROPERTY_KEY)) {
                        String files = (String) properties.get(RETURN_HANDLER_CONFIGURATION_PROPERTY_KEY);
                        if (files != null) {
                            for (String file : files.split(",")) {
                                if (StringUtils.isNotEmpty(file)) {
                                    configureFromFile(file);
                                }
                            }
                        }
                    }
                } finally {
                    if (fis != null) {
                        fis.close();
                    }
                }
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Retrieves the configured handler for the given fullyQualifiedFunctionName.
     * 
     * Lookups occur in this order:
     * 1. module.function
     * 2. module
     * 3. The default handler provided
     * 
     * @param fullyQualifiedFunctionName
     *            (i.e. module.function)
     * @param defaultHandler
     *            the default handler if no matching handler is found.
     */
    public SaltReturnHandler getHandlerFor(String fullyQualifiedFunctionName, SaltReturnHandler defaultHandler) {
        if (handlerMap.containsKey(fullyQualifiedFunctionName)) {
            return handlerMap.get(fullyQualifiedFunctionName);
        } else {
            String[] decomposedFunction = fullyQualifiedFunctionName.split("\\.", 2);
            if (handlerMap.containsKey(decomposedFunction[0])) {
                return handlerMap.get(decomposedFunction[0]);
            } else {
                return defaultHandler;
            }
        }
    }

    protected void configureFromResource(String resource) throws IOException {
        InputStream is = getClass().getResourceAsStream(resource);
        try {
            configureFromInputStream(is);
        } finally {
            is.close();
        }
    }

    protected void configureFromFile(String file) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(new File(file));
        try {
            configureFromInputStream(fis);
        } finally {
            fis.close();
        }
    }

    @SuppressWarnings("unchecked")
    protected void configureFromInputStream(InputStream is) {
        Yaml yaml = new Yaml(new CustomClassLoaderConstructor(getClass().getClassLoader()));
        Map<String, Object> document = (Map<String, Object>) yaml.load(is);
        if (document == null || !document.containsKey(HANDLER_MAPPINGS_KEY)) {
            throw new IllegalArgumentException(String.format("Expected yaml document with key: %s",
                    HANDLER_MAPPINGS_KEY));
        } else {
            Map<String, SaltReturnHandler> handlers = (Map<String, SaltReturnHandler>) document
                    .get(HANDLER_MAPPINGS_KEY);
            for (Map.Entry<String, SaltReturnHandler> entry : handlers.entrySet()) {
                if (handlerMap.containsKey(entry.getKey())) {
                    throw new IllegalStateException(String.format(
                            "Already received a salt return handler configuration entry for %s", entry.getKey()));
                }
                handlerMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
