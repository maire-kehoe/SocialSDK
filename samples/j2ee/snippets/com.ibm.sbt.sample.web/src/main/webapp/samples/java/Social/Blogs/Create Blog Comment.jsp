<!-- /*
 * � Copyright IBM Corp. 2013
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
<%@page import="com.ibm.sbt.services.client.base.datahandlers.EntityList"%>
<%@page import="com.ibm.sbt.services.client.connections.blogs.BlogService"%>
<%@page import="com.ibm.sbt.services.client.connections.blogs.Blog"%>
<%@page import="com.ibm.sbt.services.client.connections.blogs.BlogPost"%>
<%@page import="com.ibm.sbt.services.client.connections.blogs.Comment"%>
<%@page import="java.util.Collection"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="com.ibm.commons.runtime.Context"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.List"%>
<%@page 
	language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<html>
<head>
	<title>SBT JAVA Sample - Create Blog Comment</title>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
</head>

<body>	
	<h4>Create Blog Comment</h4>
	<div id="content">
	<%
	try {
		BlogService service = new BlogService();
		EntityList<Blog> blogs = service.getAllBlogs();
		if(blogs.size()>0){
			String blogHandle = ((Blog)blogs.get(0)).getHandle();
			EntityList<BlogPost> posts = service.getBlogPosts(blogHandle);
			if(posts.size()>0){
				String postId = ((BlogPost)posts.get(0)).getUid();
				Comment comment = new Comment(service, "");
				Long random = System.currentTimeMillis();
				comment.setTitle("Test Blog Comment" + random);
				comment.setContent("Test Blog Post Content12");
				service.createBlogComment(comment, blogHandle, postId);
				out.println("Blog Comment created with title : " + comment.getTitle());
			}
			else{
				out.println("No Post exists to Comment "); 
			}
		}
		else{
			out.println("No Blog exists to Comment "); 
		}
	
	} catch (Exception e) {
		out.println("<pre>");
		out.println(e.getMessage());
		out.println("</pre>");
	}
	%>
	</div>
</body>
</html>