<%@ page contentType="application/x-java-jnlp-file; charset=utf-8" %>

<%@page import="java.net.URLEncoder" %>
<%@page import="java.io.*" %>

<% 	response.setHeader("Content-Disposition", "inline; filename=combinFormation.jnlp");
	response.setHeader("Cache-Control","no-cache"); //HTTP 1.1
	response.setHeader("Pragma","no-cache"); 		//HTTP 1.0
	response.setDateHeader ("Expires", 0); 			//prevents caching at the proxy server

	// server-side generation
	String	prefSet		= null;
	final String prefSetArg ="<argument>manzana</argument>";
	String prefSetFromForm	= request.getParameter("cadenaFirma");
	if (prefSetFromForm != null)
	{
	  // prefSetFromForm		= URLEncoder.encode(prefSetFromForm, "UTF-8");	  
	   //prefSet				= prefSetFromForm;
	//	prefSetArg = "<argument>" +prefSetFromForm.replaceAll("&", "&amp;")+ "</argument>" ;
	   
	}

	//String prefSetArg	= (prefSet == null) ? "": "<argument>" + prefSet+ "</argument>" ;
	
	
%>

<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="http://www.milocal.com:8080" href="launch.jsp" > 
	<information> 
		<title>AutoFirma Universidad de Almeria</title> 
		<vendor>www.ual.es</vendor> 
		<homepage href="index.html"/> 
		<description>Proyecto basado en la AutoFirma del MINHAP</description> 
		<description kind="short">AutoFirma UAL</description> 
		<offline-allowed/> 
	</information> 
	<security>
	   <all-permissions/>
	</security>
	<update check="timeout" policy="always"/>
	<resources> 
		<j2se version="1.6+" /> 
		<jar href="applet_afirma_3_4.jar" /> 
	</resources> 
	<!-- notar que se segun el xml de este jsp se espera recibir codificacion utf-8,tener cuidado si se usan simbolos especiales -->
   	<application-desc main-class="es.gob.afirma.standalone.SimpleAfirma">
   	<argument><%=request.getParameter("cadenaFirma")%></argument> 

 	</application-desc> 
</jnlp>
