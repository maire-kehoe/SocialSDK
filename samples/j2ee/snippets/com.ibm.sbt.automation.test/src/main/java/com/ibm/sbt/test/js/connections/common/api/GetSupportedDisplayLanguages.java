/*
 * © Copyright IBM Corp. 2013
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */
package com.ibm.sbt.test.js.connections.common.api;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.sbt.automation.core.test.BaseApiTest;
import com.ibm.sbt.automation.core.test.pageobjects.JavaScriptPreviewPage;

/**
 * @author rajmeetbal
 *  
 * @since 30 Oct 2013
 */
public class GetSupportedDisplayLanguages extends BaseApiTest {
    
    static final String SNIPPET_ID = "Social_Common_API_GetSupportedDisplayLanguages";

    public GetSupportedDisplayLanguages() {
        setAuthType(AuthType.AUTO_DETECT);
    }
    
    @Test
    public void testGetSupportedDisplayLanguages() {
        JavaScriptPreviewPage previewPage = executeSnippet(SNIPPET_ID);
        List jsonList = previewPage.getJsonList();
        Assert.assertFalse("GetSupportedDisplayLanguages returned no results", jsonList.isEmpty());
        for (int i=0; i<jsonList.size(); i++) {
            JsonJavaObject json = (JsonJavaObject)jsonList.get(i);
            Assert.assertNotNull(json.getString("label"));
            Assert.assertNotNull(json.getString("term"));
        }
    }

}
