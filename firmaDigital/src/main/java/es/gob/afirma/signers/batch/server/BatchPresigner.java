package es.gob.afirma.signers.batch.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import es.gob.afirma.signers.batch.SignBatch;
import es.gob.afirma.triphase.server.SignatureService;

/** Realiza la primera fase de un proceso de firma por lote.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class BatchPresigner extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String CONFIG_FILE = "config.properties"; //$NON-NLS-1$

	private static final String BATCH_XML_PARAM = "xml"; //$NON-NLS-1$
	private static final String BATCH_CRT_PARAM = "certs"; //$NON-NLS-1$

	private static final String CONFIG_PARAM_ALLOW_ORIGIN = "Access-Control-Allow-Origin"; //$NON-NLS-1$

	/** Or&iacute;genes permitidos por defecto desde los que se pueden realizar peticiones al servicio. */
	private static final String CONFIG_DEFAULT_VALUE_ALLOW_ORIGIN = "*"; //$NON-NLS-1$

	private static final Properties config;

	static {
		try {
			final InputStream configIs = SignatureService.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
			if (configIs == null) {
				throw new RuntimeException("No se encuentra el fichero de configuracion del servicio: " + CONFIG_FILE); //$NON-NLS-1$
			}
			config = new Properties();
			config.load(configIs);
			configIs.close();
		}
		catch(final Exception e) {
			throw new RuntimeException("Error en la carga del fichero de propiedades: " + e, e); //$NON-NLS-1$
		}
	}

	/** Realiza la primera fase de un proceso de firma por lote.
	 * Debe recibir la definici&oacute;n del lote en un XML (<a href="../doc-files/batch-scheme.html">descripci&oacute;n
	 * del formato</a>) convertido completamente
	 * en Base64 y la cadena de certificados del firmante, convertidos a Base64 (puede ser
	 * <i>URL Safe</i>) y separados por punto y coma (<code>;</code>).
	 * Devuelve un XML de sesi&oacute;n trif&aacute;sica.
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response) */
	@Override
	protected void service(final HttpServletRequest request,
			               final HttpServletResponse response) throws ServletException,
			                                                          IOException {
		final String xml = request.getParameter(BATCH_XML_PARAM);
		if (xml == null) {
			LOGGER.severe("No se ha recibido una definicion de lote en el parametro " + BATCH_XML_PARAM); //$NON-NLS-1$
			response.sendError(
				HttpServletResponse.SC_BAD_REQUEST,
				"No se ha recibido una definicion de lote en el parametro " + BATCH_XML_PARAM //$NON-NLS-1$
			);
			return;
		}

		final SignBatch batch;
		try {
			batch = BatchServerUtil.getSignBatch(xml);
		}
		catch(final Exception e) {
			LOGGER.severe("La definicion de lote es invalida: " + e); //$NON-NLS-1$
			response.sendError(
				HttpServletResponse.SC_BAD_REQUEST,
				"La definicion de lote es invalida: " + e //$NON-NLS-1$
			);
			return;
		}

		final String certListUrlSafeBase64 = request.getParameter(BATCH_CRT_PARAM);
		if (certListUrlSafeBase64 == null) {
			LOGGER.severe("No se ha recibido la cadena de certificados del firmante en el parametro " + BATCH_CRT_PARAM); //$NON-NLS-1$
			response.sendError(
				HttpServletResponse.SC_BAD_REQUEST,
				"No se ha recibido la cadena de certificados del firmante en el parametro " + BATCH_CRT_PARAM //$NON-NLS-1$
			);
			return;
		}

		final X509Certificate[] certs;
		try {
			certs = BatchServerUtil.getCertificates(certListUrlSafeBase64);
		}
		catch (final Exception e) {
			LOGGER.severe("La cadena de certificados del firmante es invalida: " + e); //$NON-NLS-1$
			response.sendError(
				HttpServletResponse.SC_BAD_REQUEST,
				"La cadena de certificados del firmante es invalida: " + e //$NON-NLS-1$
			);
			return;
		}

		final String pre;
		try {
			pre = batch.doPreBatch(certs);
		}
		catch(final Exception e) {
			LOGGER.log(Level.SEVERE, "Error en el preproceso del lote: " + e, e); //$NON-NLS-1$
			response.sendError(
				HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				"Error en el preproceso del lote: " + e //$NON-NLS-1$
			);
			return;
		}

		String allowOrigin = CONFIG_DEFAULT_VALUE_ALLOW_ORIGIN;
		if (BatchPresigner.config.contains(CONFIG_PARAM_ALLOW_ORIGIN)) {
			allowOrigin = BatchPresigner.config.getProperty(CONFIG_PARAM_ALLOW_ORIGIN);
		}

		response.setHeader("Access-Control-Allow-Origin", allowOrigin); //$NON-NLS-1$
		response.setContentType("text/xml;charset=UTF-8"); //$NON-NLS-1$
		final PrintWriter writer = response.getWriter();
		writer.write(pre);
		writer.flush();
	}

}
