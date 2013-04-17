package com.salesforce.rundeck.plugin;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * DI support for salt NodeStepPlugins
 */
public class DependencyInjectionUtil {

    protected static String CONFIG_FILE_LOCATION = "/beans.xml";

    protected static volatile ClassPathXmlApplicationContext context;

    protected ApplicationContext getContext() {
        if (context == null) {
            synchronized (DependencyInjectionUtil.class) {
                if (context == null) {
                    context = new ClassPathXmlApplicationContext();
                    context.setClassLoader(DependencyInjectionUtil.class.getClassLoader());
                    context.setConfigLocation(CONFIG_FILE_LOCATION);
                    context.refresh();
                    context.registerShutdownHook();
                }
            }
        }
        return context;
    }

    public void inject(Object bean) {
        getContext().getAutowireCapableBeanFactory().autowireBean(bean);
    }
}
