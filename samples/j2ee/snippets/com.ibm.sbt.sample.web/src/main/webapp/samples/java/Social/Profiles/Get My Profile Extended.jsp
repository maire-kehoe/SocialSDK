<!-- 
/*
 * � Copyright IBM Corp. 2012
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
 */ -->
 
 <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="java.util.*"%>
<%@page import="com.ibm.commons.xml.*"%>
<%@page import="org.w3c.dom.*"%>
<%@page import="com.ibm.sbt.services.client.*"%>
<%@page import="com.ibm.sbt.services.client.base.datahandlers.*"%>
<%@page import="com.ibm.commons.runtime.Application"%>
<%@page import="com.ibm.commons.runtime.Context"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="com.ibm.sbt.services.client.connections.profiles.ProfileService"%>
<%@page import="com.ibm.sbt.services.client.connections.profiles.Profile"%>
<%@page import="com.ibm.commons.util.StringUtil"%>
<%@page language="java" contentType="text/html; charset=ISO-8859-1"	pageEncoding="ISO-8859-1"%>

<html>
<head>
	<title>SBT JAVA Sample - Get My Profile</title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>
<body>
	<div id="content">
	<%
	
	
	 try {
         ProfileService connProfSvc = new ProfileService();
         Profile profile = connProfSvc.getMyProfile();
         out.println("my UserId "+profile.getUserid()+"<br>");
         out.println("my Job Title "+profile.getJobTitle()+"<br>");
         out.println("summary "+profile.getSummary()+"<br>");
         out.println("other " + profile.getExtendedAttributes());
 	} catch (Throwable e) {
         out.println("<pre>");
         out.println(e.getMessage());
         out.println("</pre>");
 	}
	%>
	</div>
</body>
</html>
