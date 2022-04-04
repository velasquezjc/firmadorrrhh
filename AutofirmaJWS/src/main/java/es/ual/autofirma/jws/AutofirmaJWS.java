package es.ual.autofirma.jws;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import jnlp.sample.servlet.JnlpDownloadServlet;

public class AutofirmaJWS extends JnlpDownloadServlet 
{
	public void service(ServletRequest request, ServletResponse res) throws ServletException, IOException 
	{

		//HttpServletRequest _req = (HttpServletRequest) request;

		res.setContentType("application/x-java-jnlp-file");

		//_req.getRequestDispatcher("/launch.jsp").include(request, res);
		PrintWriter out=res.getWriter();
		out.println("<?xml version='1.0' encoding='utf-8'?>");
		//TRABAJO: modificar a https://127.0.0.1:8443 para evitar la busqueda por dns para que funcione
		out.println("<jnlp spec='1.0+' codebase='https://www.milocal.com:8443/AutofirmaJWS/' > ");
		//facu sea http o https:   out.println("<jnlp spec='1.0+' codebase='http://www.milocal.com:8080/AutofirmaJWS/' > ");
		
		//server:  out.println("<jnlp spec='1.0+' codebase='https://clientefirma-pc16.rhcloud.com/AutofirmaJWS/' > ");
		out.println("<information>");
		out.println("<title>AutoFirma MDS</title> ");
		out.println("<vendor>MDS</vendor> ");
		out.println("<homepage href='index.html'/> ");
		out.println("<description>Proyecto basado en AutoFirma</description>");
		out.println("<description kind='short'>AutoFirma MDS</description> ");
		out.println("<icon href='https://www.milocal.com:8443/AutofirmaJWS/img/images.jpg' width='64' height='64'/> ");
		out.println("<offline-allowed/> ");
		out.println("</information>");		
		out.println("<security>");
		out.println("<all-permissions/>");
		out.println("</security>");
		out.println("<update check='timeout' policy='always'/>");
		out.println("<resources> ");
		out.println("<j2se version='1.6+' />");
		out.println("<jar href='simple_afirma_3_4s.jar' size='20005256 '/> ");/*nota:actualizar el tamaño en bytes cuando este todo bien*/
		out.println("<property name='jnlp.packEnabled' value='true'/>");
		out.println("</resources>");
		out.println("<application-desc main-class='es.gob.afirma.standalone.SimpleAfirma'>");
		out.print("<argument>");
		out.print(request.getParameter("cadenaFirma"));
		out.println("</argument>");
		out.println("</application-desc> ");
		out.println("</jnlp> ");
		out.close();
		
		
	}
}