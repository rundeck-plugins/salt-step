package com.salesforce.rundeck.plugin;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(EntityUtils.class)
public class SaltApiNodeStepPlugin_HttpHandlersTest {

    protected SaltApiNodeStepPlugin plugin;

    @Before
    public void setup() {
        plugin = new SaltApiNodeStepPlugin();
        PowerMockito.mockStatic(EntityUtils.class);
    }

    @Test
    public void testCloseResource() {
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        plugin.closeResource(entity);
        
        PowerMockito.verifyStatic(Mockito.times(1));
        EntityUtils.consumeQuietly(Mockito.same(entity));
    }
    
    @Test
    public void testExtractBodyFromEntity() throws Exception {
        HttpEntity entity = Mockito.mock(HttpEntity.class);
        String body = "some body";
        PowerMockito.doReturn(body).when(EntityUtils.class);
        EntityUtils.toString(Mockito.same(entity));
        
        Assert.assertSame(body, plugin.extractBodyFromEntity(entity));
    }

}
