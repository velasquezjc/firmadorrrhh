<?xml version="1.0" encoding="utf-8"?>
<jnlp spec="1.0+" codebase="http://www.milocal.com:8080" href="jnlp.jsp"> 
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
<%--  	  	<argument><%=request.getParameter("cadenaFirma")%></argument>   --%>
 	  	<argument>afirma://service?ports=52961,54705,52357&v=1&idsession=dL2HB2Sp1uXGb1kyNESY</argument>
 	</application-desc> 
</jnlp> 