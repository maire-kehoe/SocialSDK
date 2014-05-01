<!-- /*
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
 */-->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="com.ibm.commons.util.StringUtil"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="com.ibm.commons.runtime.Application"%>
<%@page import="com.ibm.commons.runtime.Context"%>
<%@page import="com.ibm.commons.util.io.json.JsonJavaObject"%>
<%@page import="com.ibm.sbt.services.client.base.JsonEntity"%>
<%@page import="com.ibm.sbt.services.client.base.datahandlers.EntityList"%>
<%@page import="com.ibm.sbt.services.client.smartcloud.bss.*"%>
<%@page import="java.util.*"%>

				
<%@page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
	
<html>
<head>
<title>Get Subscription List By Customer Id</title>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>

<body>
	<div id="content">
	<%
	try {
		String customerId = Context.get().getProperty("bss.customerId");
		if (StringUtil.isEmpty(customerId)) {
			out.println("Please provide a valid customer id in the sbt.properties.");
			return;
		}
		
    	SubscriptionManagementService subscriptionManagement = new SubscriptionManagementService("smartcloud");
		EntityList<JsonEntity> subscriptionList = subscriptionManagement.getSubscriptionsById(customerId);
		out.println("Id's of all subscriptions belonging to Customer Id: " + customerId + "<br/>");
		out.println("<ul>");
		for (JsonEntity subscription : subscriptionList) {
			long id = subscription.getAsLong("Id");
			out.println("<li>" + id + "</li>");
		}
		out.println("</ul>");
	}
	catch (BssException be) {
		out.println("<pre>");
		out.println("Error retrieving subscription list caused by: "+ be.getResponseJson());
		out.println("</pre>");	
	}
	
	catch (Exception e) {
		e.printStackTrace();
		out.println("Error retrieving subscription list caused by: "+e.getMessage());    		
	}
	%>
	</div>
</body>

</html>