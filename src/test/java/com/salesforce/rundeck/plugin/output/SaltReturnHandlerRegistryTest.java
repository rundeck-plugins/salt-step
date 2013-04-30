package com.salesforce.rundeck.plugin.output;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SaltReturnHandlerRegistryTest {

    @Test
    public void testConfigure() throws IOException {
        String configurationFile = "file";
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(configurationFile);
        registry = Mockito.spy(registry);
        Mockito.doNothing().when(registry).configureFromResource(Mockito.anyString());

        registry.configure();

        Mockito.verify(registry, Mockito.times(1)).configure();
        Mockito.verify(registry, Mockito.times(1)).configureFromResource(Mockito.eq(configurationFile));
        Mockito.verifyNoMoreInteractions(registry);
    }

    @Test
    public void testConfigureWithRundeckConfigFileProperty() throws IOException {
        String configurationFile = "file";
        String extraConfigurationFile1 = "extraFile1";
        String extraConfigurationFile2 = "extraFile2";
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(configurationFile);
        registry = Mockito.spy(registry);
        Mockito.doNothing().when(registry).configureFromResource(Mockito.anyString());

        File rundeckConfigurationFile = File.createTempFile("some", "file");
        rundeckConfigurationFile.deleteOnExit();
        System.setProperty(SaltReturnHandlerRegistry.RUNDECK_CONFIGURATION_LOCATION_KEY,
                rundeckConfigurationFile.getAbsolutePath());
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(rundeckConfigurationFile));
            try {
                writer.append(String.format("%s=%s,%s",
                        SaltReturnHandlerRegistry.RETURN_HANDLER_CONFIGURATION_PROPERTY_KEY, extraConfigurationFile1,
                        extraConfigurationFile2));
            } finally {
                writer.close();
            }
            Mockito.doNothing().when(registry).configureFromFile(Mockito.anyString());

            registry.configure();

            Mockito.verify(registry, Mockito.times(1)).configure();
            Mockito.verify(registry, Mockito.times(1)).configureFromResource(Mockito.eq(configurationFile));
            Mockito.verify(registry, Mockito.times(1)).configureFromFile(Mockito.eq(extraConfigurationFile1));
            Mockito.verify(registry, Mockito.times(1)).configureFromFile(Mockito.eq(extraConfigurationFile2));
            Mockito.verifyNoMoreInteractions(registry);
        } finally {
            // Unset the system property or else subsequent tests will try to configure using these keys.
            System.clearProperty(SaltReturnHandlerRegistry.RUNDECK_CONFIGURATION_LOCATION_KEY);
        }
    }

    @Test
    public void testGetHandlerResolvesFullyQualifiedFunction() {
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);
        SaltReturnHandler handler1 = new DefaultSaltReturnHandler();
        SaltReturnHandler handler2 = new DefaultSaltReturnHandler();
        SaltReturnHandler handler3 = new DefaultSaltReturnHandler();
        registry.handlerMap.put("module.function", handler1);
        registry.handlerMap.put("module", handler2);

        Assert.assertSame("Expected most specific handler to be returned", handler1, registry.getHandlerFor("module.function", handler3));
    }

    @Test
    public void testGetHandlerResolvesModule() {
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);
        SaltReturnHandler handler1 = new DefaultSaltReturnHandler();
        SaltReturnHandler handler2 = new DefaultSaltReturnHandler();
        SaltReturnHandler handler3 = new DefaultSaltReturnHandler();
        registry.handlerMap.put("module.function", handler1);
        registry.handlerMap.put("module", handler2);

        Assert.assertSame("Expected module level handler to be returned", handler2, registry.getHandlerFor("module.function2", handler3));
    }

    @Test
    public void testGetHandlerUsesDefault() {
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);
        SaltReturnHandler handler1 = new DefaultSaltReturnHandler();
        SaltReturnHandler handler2 = new DefaultSaltReturnHandler();
        SaltReturnHandler handler3 = new DefaultSaltReturnHandler();
        registry.handlerMap.put("module.function", handler1);
        registry.handlerMap.put("module", handler2);

        Assert.assertSame("Expected default handler to be returned", handler3, registry.getHandlerFor("module2.function", handler3));
    }

    @Test
    public void testConfigureFromResource() throws IOException {
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);
        registry.configureFromResource("/testReturners.yaml");

        Assert.assertEquals("Expected single returner to be registed", 1, registry.handlerMap.size());
        Assert.assertTrue("Expected cmd returner to be a DefaultSaltReturnHandler", registry.handlerMap.get("cmd") instanceof DefaultSaltReturnHandler);
    }

    @Test
    public void testConfigureFromFile() throws IOException {
        File copyOfConfiguration = File.createTempFile("some", "file");
        copyOfConfiguration.deleteOnExit();
        FileOutputStream os = new FileOutputStream(copyOfConfiguration);
        InputStream is = getClass().getResourceAsStream("/testReturners.yaml");
        try {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            os.close();
            is.close();
        }

        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);
        registry.configureFromFile(copyOfConfiguration.getAbsolutePath());

        Assert.assertEquals("Expected single returner to be registed", 1, registry.handlerMap.size());
        Assert.assertTrue("Expected cmd returner to be a DefaultSaltReturnHandler", registry.handlerMap.get("cmd") instanceof DefaultSaltReturnHandler);
    }

    @Test(expected = IllegalStateException.class)
    public void testConfigureWithSameConfigurationThrowsException() throws IOException {
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);

        registry.configureFromInputStream(getClass().getResourceAsStream("/testReturners.yaml"));
        registry.configureFromInputStream(getClass().getResourceAsStream("/testReturners.yaml"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConfigureFromResourceMissingHandlerMappingKey() throws IOException {
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);
        registry.configureFromResource("/badFormatReturners.yaml");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConfigureFromResourceEmpty() throws IOException {
        SaltReturnHandlerRegistry registry = new SaltReturnHandlerRegistry(null);
        registry.configureFromResource("/empty.yaml");
    }
}
