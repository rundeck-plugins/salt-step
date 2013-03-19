package com.salesforce.rundeck.plugin;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.dtolabs.rundeck.plugins.step.NodeStepPlugin;

public abstract class DependencyManagedNodeStepPlugin implements NodeStepPlugin {

    protected static String CONFIG_FILE_LOCATION = "/beans.xml";

    protected static volatile ClassPathXmlApplicationContext context;

    protected ApplicationContext getContext() {
        if (context == null) {
            synchronized (DependencyManagedNodeStepPlugin.class) {
                if (context == null) {
                    context = new ClassPathXmlApplicationContext();
                    context.setClassLoader(DependencyManagedNodeStepPlugin.class.getClassLoader());
                    context.setConfigLocation(CONFIG_FILE_LOCATION);
                    context.refresh();
                    context.registerShutdownHook();
                }
            }
        }
        return context;
    }

    public DependencyManagedNodeStepPlugin() {
        getContext().getAutowireCapableBeanFactory().autowireBean(this);
    }
}
