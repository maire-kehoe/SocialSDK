/*
 * © Copyright IBM Corp. 2014
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
package com.ibm.sbt.services.client.connections.follow;

import com.ibm.sbt.services.client.base.BaseService;
import com.ibm.sbt.services.client.base.ConnectionsConstants;
import com.ibm.sbt.services.client.base.NamedUrlPart;
import com.ibm.sbt.services.client.base.URLBuilder;
import com.ibm.sbt.services.client.base.URLContainer;
import com.ibm.sbt.services.client.base.Version;
import com.ibm.sbt.services.client.base.VersionedUrl;

/**
 * 
 * @author Carlos Manias
 *
 */
public enum FollowUrls implements URLContainer {
	ACTIVITIES(new VersionedUrl(ConnectionsConstants.v4_0, "activities/follow/{authType}/atom/resources/{resourceId}")),
	BLOGS(new VersionedUrl(ConnectionsConstants.v4_0, "blogs/follow/{authType}/atom/resources/{resourceId}")),
	COMMUNITIES(new VersionedUrl(ConnectionsConstants.v4_0, "communities/follow/{authType}/atom/resources/{resourceId}")),
	FORUMS(new VersionedUrl(ConnectionsConstants.v4_0, "forums/follow/{authType}/atom/resources/{resourceId}")),
	PROFILES(new VersionedUrl(ConnectionsConstants.v4_0, "profiles/follow/{authType}/atom/resources/{resourceId}")),
	WIKIS(new VersionedUrl(ConnectionsConstants.v4_0, "wikis/follow/{authType}/atom/resources/{resourceId}")),
	TAGS(new VersionedUrl(ConnectionsConstants.v4_0, "tags/follow/{authType}/atom/resources/{resourceId}"));

	private URLBuilder builder;
	
	private FollowUrls(VersionedUrl... urlVersions) {
		builder = new URLBuilder(urlVersions);
	}
	
	public String format(BaseService service, NamedUrlPart... args) {
		return builder.format(service, args);
	}

	public String getPattern(Version version){
		return builder.getPattern(version).getUrlPattern();
	}
	
	public static String format(String serviceName, BaseService service, NamedUrlPart... args) {
		return valueOf(serviceName.toUpperCase()).format(service, args);
	}
}
