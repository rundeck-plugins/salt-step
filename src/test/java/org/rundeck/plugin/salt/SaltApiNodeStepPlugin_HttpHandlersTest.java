/**
 * Copyright (c) 2013, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.rundeck.plugin.salt;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.rundeck.plugin.salt.SaltApiNodeStepPlugin;

public class SaltApiNodeStepPlugin_HttpHandlersTest {

    protected SaltApiNodeStepPlugin plugin;

    @Before
    public void setup() {
        plugin = new SaltApiNodeStepPlugin();
    }

    @Test
    public void testCloseResource() {
        // Test that closeResource doesn't throw exceptions with null
        plugin.closeResource(null);
        
        // Test with a real entity - this will actually test the functionality
        HttpEntity entity = new StringEntity("test", "UTF-8");
        plugin.closeResource(entity);
        // If we get here without exception, the test passes
        Assert.assertTrue("closeResource should handle entities without throwing exceptions", true);
    }

    @Test
    public void testExtractBodyFromEntity() throws Exception {
        String testBody = "test response body";
        HttpEntity entity = new StringEntity(testBody, "UTF-8");

        String result = plugin.extractBodyFromEntity(entity);
        Assert.assertEquals("Expected body to be extracted from entity correctly", testBody, result);
    }

}
