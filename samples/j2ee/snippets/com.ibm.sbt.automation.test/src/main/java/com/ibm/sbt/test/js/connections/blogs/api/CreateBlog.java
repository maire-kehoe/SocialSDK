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
package com.ibm.sbt.test.js.connections.blogs.api;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import com.ibm.commons.util.io.json.JsonJavaObject;
import com.ibm.sbt.automation.core.test.connections.BaseBlogsTest;
import com.ibm.sbt.automation.core.test.pageobjects.JavaScriptPreviewPage;
import com.ibm.sbt.services.client.connections.blogs.BlogServiceException;

/**
 * 
 * @author Francis
 * @date 29 Apr 2014
 */
public class CreateBlog extends BaseBlogsTest {
    
    static final String SNIPPET_ID = "Social_Blogs_API_CreateBlog";

    private String createdBlogUuid = "";
    @Test
    public void testCreateBlog() {
        JavaScriptPreviewPage previewPage = executeSnippet(SNIPPET_ID);
        JsonJavaObject json = previewPage.getJson();
        createdBlogUuid = json.getString("uid");
        Assert.assertNotNull(createdBlogUuid);
    }
    
    @After
    public void deleteCreatedBlog(){
    	try {
    		if(createdBlogUuid.length() > 28){
    			String uuid = createdBlogUuid.substring(createdBlogUuid.indexOf("urn:lsid:ibm.com:blogs:") + 28);
    			deleteBlog(uuid);
    		}
		} catch (BlogServiceException e) {
			e.printStackTrace();
		}
    }
    
}
