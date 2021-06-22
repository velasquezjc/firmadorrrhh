package es.ual.autofirma.jws;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import jnlp.sample.servlet.JnlpDownloadServlet;

public class AutofirmaJWSServer extends JnlpDownloadServlet 
{
	public void service(ServletRequest request, ServletResponse res) throws ServletException, IOException 
	{

		//HttpServletRequest _req = (HttpServletRequest) request;

		res.setContentType("application/x-java-jnlp-file");

		//_req.getRequestDispatcher("/launch.jsp").include(request, res);
		PrintWriter out=res.getWriter();
		out.println("<?xml version='1.0' encoding='utf-8'?>");
		//out.println("<jnlp spec='1.0+' codebase='https://www.milocal.com:8443' > ");
		out.println("<jnlp spec='1.0+' codebase='http://jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/AutofirmaJWS/' > ");
		out.println("<information>");
		out.println("<title>AutoFirma MDS</title> ");
		out.println("<vendor>MDS</vendor> ");
		out.println("<homepage href='index.html'/> ");
		out.println("<description>Proyecto basado en AutoFirma</description>");
		out.println("<description kind='short'>AutoFirma MDS</description> ");
		out.println("<icon href='http://jws-app-clientefirma.7e14.starter-us-west-2.openshiftapps.com/AutofirmaJWS/img/images.jpg' width='64' height='64'/> ");
		out.println("<offline-allowed/> ");
		out.println("</information>");		
		out.println("<security>");
		out.println("<all-permissions/>");
		out.println("</security>");
		out.println("<update check='timeout' policy='always'/>");
		out.println("<resources> ");
		out.println("<j2se version='1.6+' />");
		out.println("<jar href='simple_afirma_3_4s.jar' size='29005256 '/> ");/*nota:actualizar el tamaño en bytes cuando este todo bien*/
		//por laguna razon, la app en el server luego de descargar el jnlp me tira que el comprimido esta dañado
		//puede ser que una cuestion de jre instalado, en jboss local tambien me tiraba un error
		//pero al limpiar la cache desde la configuracion de java se resolvio
		//por default si no encuentar el gzip busca el href de arriba
		//java.io.IOException: Corrupted pack file: magic/ver = 1F8B0800/0.0 should be CAFED00D/150.7 OR CAFED00D/160.1 OR CAFED00D/170.1 OR CAFED00D/171.0
		//out.println("<property name='jnlp.packEnabled' value='true'/>");
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