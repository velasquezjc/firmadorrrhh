package es.gob.afirma.signfolder.server.proxy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/** Servicio de almacenamiento temporal de firmas. &Uacute;til para servir de intermediario en comunicaci&oacute;n
 // * entre JavaScript y <i>Apps</i> m&oacute;viles nativas.
 * @author Tom&aacute;s Garc&iacute;a-;er&aacute;s */
public final class StorageService extends HttpServlet {

	private static final long serialVersionUID = -3272368448371213403L;

	/** Fichero de configuraci&oacute;n. */
	private static final String CONFIG_FILE = "configuration.properties"; //$NON-NLS-1$

	/** Log para registrar las acciones del servicio. */
	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma");  //$NON-NLS-1$

	/** Nombre del par&aacute;metro con la operaci&oacute;n realizada. */
	private static final String PARAMETER_NAME_OPERATION = "op"; //$NON-NLS-1$

	/** Nombre del par&aacute;metro con el identificador del fichero temporal. */
	private static final String PARAMETER_NAME_ID = "id"; //$NON-NLS-1$

	/** Nombre del par&aacute;metro con la versi&oacute;n de la sintaxis de petici&oacute; utilizada. */
	private static final String PARAMETER_NAME_SYNTAX_VERSION = "v"; //$NON-NLS-1$

	/** Nombre del par&aacute;metro con los datos a firmar. */
	private static final String PARAMETER_NAME_DATA = "dat"; //$NON-NLS-1$

	private static final String OPERATION_STORE = "put"; //$NON-NLS-1$
	private static final String SUCCESS = "OK"; //$NON-NLS-1$

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {

		LOGGER.info("Se recibe una peticion de almacenamiento"); //$NON-NLS-1$

		// Leemos la entrada
		int n;
		final byte[] buffer = new byte[1024];
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ServletInputStream sis = request.getInputStream();
		while ((n = sis.read(buffer)) > 0) {
			baos.write(buffer, 0, n);
		}
		baos.close();
		sis.close();

		// Separamos los parametros y sus valores
		final Hashtable<String, String> params = new Hashtable<String, String>();
		final String[] urlParams = new String(baos.toByteArray()).split("&"); //$NON-NLS-1$
		for (final String param : urlParams) {
			final int equalsPos = param.indexOf('=');
			if (equalsPos != -1) {
				params.put(param.substring(0, equalsPos), param.substring(equalsPos + 1));
			}
		}

		final String operation = params.get(PARAMETER_NAME_OPERATION);
		final String syntaxVersion = params.get(PARAMETER_NAME_SYNTAX_VERSION);
		response.setHeader("Access-Control-Allow-Origin", "*"); //$NON-NLS-1$ //$NON-NLS-2$
		response.setContentType("text/plain"); //$NON-NLS-1$
		response.setCharacterEncoding("utf-8"); //$NON-NLS-1$

		final PrintWriter out = response.getWriter();
		if (operation == null) {
			LOGGER.warning("No se ha indicado codigo de operacion"); //$NON-NLS-1$
			out.println(ErrorManager.genError(ErrorManager.ERROR_MISSING_OPERATION_NAME, null));
			return;
		}
		if (syntaxVersion == null) {
			LOGGER.warning("No se ha indicado la version del formato de llamada"); //$NON-NLS-1$
			out.println(ErrorManager.genError(ErrorManager.ERROR_MISSING_SYNTAX_VERSION, null));
			return;
		}

		final StorageConfig config;
		try {
			config = new StorageConfig();
			config.load(CONFIG_FILE);
		} catch (final IOException e) {
			LOGGER.severe(ErrorManager.genError(ErrorManager.ERROR_CONFIGURATION_FILE_PROBLEM, null));
			out.println(ErrorManager.genError(ErrorManager.ERROR_CONFIGURATION_FILE_PROBLEM, null));
			return;
		}

		if (OPERATION_STORE.equalsIgnoreCase(operation)) {
			storeSign(out, params, config);
		} else {
			out.println(ErrorManager.genError(ErrorManager.ERROR_UNSUPPORTED_OPERATION_NAME, null));
		}
		out.close();
	}

	/**
	 * Almacena una firma en servidor.
	 * @param response Respuesta a la petici&oacute;n.
	 * @param request Petici&oacute;n.
	 * @throws IOException Cuando ocurre un error al general la respuesta.
	 */
	private static void storeSign(final PrintWriter out, final Hashtable<String, String> params, final StorageConfig config) throws IOException {

		LOGGER.info("Solicitud de guardado"); //$NON-NLS-1$

		final String id = params.get(PARAMETER_NAME_ID);
		if (id == null) {
			LOGGER.severe(ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA_ID, null));
			out.println(ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA_ID, null));
			return;
		}

		LOGGER.info("Se solicita guardar un fichero con el identificador: " + id); //$NON-NLS-1$

		// Si no se indican los datos, se transmite el error en texto plano a traves del fichero generado
		String dataText = params.get(PARAMETER_NAME_DATA);
		if (dataText == null) {
			LOGGER.severe(ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA, null));
			dataText = ErrorManager.genError(ErrorManager.ERROR_MISSING_DATA, null);
		}

		final byte[] data = dataText.getBytes();

		if (!config.getTempDir().exists()) {
			config.getTempDir().mkdirs();
		}

		final File outFile = new File(config.getTempDir(), id);
		try {
			final OutputStream fos = new FileOutputStream(outFile);
			fos.write(data);
			fos.flush();
			fos.close();
		} catch (final IOException e) {
			LOGGER.severe("No se ha podido generar el fichero temporal para el envio de datos a la web: " + e); //$NON-NLS-1$
			out.println(ErrorManager.genError(ErrorManager.ERROR_COMMUNICATING_WITH_WEB, null));
			return;
		}

		LOGGER.info("Se guardo correctamente el fichero: " + outFile.getAbsolutePath()); //$NON-NLS-1$

		out.print(SUCCESS);
	}
}