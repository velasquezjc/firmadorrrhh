package es.gob.afirma.signers.batch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.http.DataDownloader;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.signers.batch.SingleSign.ProcessResult.Result;
import es.gob.afirma.signers.batch.SingleSignConstants.SignFormat;
import es.gob.afirma.signers.batch.SingleSignConstants.SignSubOperation;

/** Firma electr&oacute;nica &uacute;nica dentro de un lote.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class SingleSign {

	private static final List<String> ALLOWED_DATASOURCES;
	static {
		final Properties p = new Properties();
		try {
			p.load(SingleSign.class.getResourceAsStream("/signbatch.properties")); //$NON-NLS-1$
		}
		catch(final Exception e) {
			throw new IllegalStateException(
				"No se ha podido cargar la configuracion con los permisos para la carga de datos: " + e //$NON-NLS-1$
			);
		}
		final String sources = p.getProperty("allowedsources"); //$NON-NLS-1$
		if (sources == null || sources.isEmpty()) {
			throw new IllegalStateException(
				"No se ha definido ningun permiso para la carga de datos" //$NON-NLS-1$
			);
		}
		ALLOWED_DATASOURCES = Arrays.asList(sources.split(";")); //$NON-NLS-1$
	}

	private static final String PROP_ID = "SignatureId"; //$NON-NLS-1$

	private static final String XML_ATTRIBUTE_ID = "Id"; //$NON-NLS-1$

	private static final String XML_ELEMENT_DATASOURCE = "datasource"; //$NON-NLS-1$
	private static final String XML_ELEMENT_FORMAT = "format"; //$NON-NLS-1$
	private static final String XML_ELEMENT_SUBOPERATION = "suboperation"; //$NON-NLS-1$
	private static final String XML_ELEMENT_SIGNSAVER = "signsaver"; //$NON-NLS-1$
	private static final String XML_ELEMENT_SIGNSAVER_CLASSNAME = "class"; //$NON-NLS-1$
	private static final String XML_ELEMENT_SIGNSAVER_CONFIG = "config"; //$NON-NLS-1$
	private static final String XML_ELEMENT_EXTRAPARAMS = "extraparams"; //$NON-NLS-1$

	private final Properties extraParams;
	Properties getExtraParams() {
		return this.extraParams;
	}

	private final String dataSource;

	private final SignFormat format;
	SignFormat getSignFormat() {
		return this.format;
	}

	private final String id;

	private final SignSubOperation subOperation;
	SignSubOperation getSubOperation() {
		return this.subOperation;
	}

	private final SignSaver signSaver;

	private static void checkDataSource(final String dataSource) {
		if (dataSource == null) {
			throw new IllegalArgumentException(
				"El origen de los datos no puede ser nulo" //$NON-NLS-1$
			);
		}
		for (final String allowed : ALLOWED_DATASOURCES) {
			if ("base64".equalsIgnoreCase(allowed) && Base64.isBase64(dataSource)) { //$NON-NLS-1$
				return;
			}
			if (allowed.endsWith("*")) { //$NON-NLS-1$
				if (dataSource.startsWith(allowed.replace("*", ""))) { //$NON-NLS-1$ //$NON-NLS-2$
					return;
				}
			}
			else {
				if (dataSource.equals(allowed)) {
					return;
				}
			}
		}
		throw new SecurityException("Origen de datos no valido"); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(" <singlesign "); //$NON-NLS-1$
		sb.append(XML_ATTRIBUTE_ID);
		sb.append("=\""); //$NON-NLS-1$
		sb.append(getId());
		sb.append("\">\n  <"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_DATASOURCE);
		sb.append(">"); //$NON-NLS-1$
		sb.append(this.dataSource);
		sb.append("</"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_DATASOURCE);
		sb.append(">\n  <"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_FORMAT);
		sb.append(">"); //$NON-NLS-1$
		sb.append(getSignFormat().toString());
		sb.append("</"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_FORMAT);
		sb.append(">\n  <"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SUBOPERATION);
		sb.append(">"); //$NON-NLS-1$
		sb.append(getSubOperation().toString());
		sb.append("</"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SUBOPERATION);
		sb.append(">\n  <"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_EXTRAPARAMS);
		sb.append(">"); //$NON-NLS-1$
		try {
			sb.append(AOUtil.properties2Base64(getExtraParams()));
		}
		catch (final IOException e) {
			Logger.getLogger("es.gob.afirma").severe( //$NON-NLS-1$
				"Error convirtiendo los parametros adicionales de la firma '" + getId() + "' a Base64: " + e //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		sb.append("</"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_EXTRAPARAMS);
		sb.append(">\n  <"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SIGNSAVER);
		sb.append(">\n   <"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SIGNSAVER_CLASSNAME);
		sb.append(">"); //$NON-NLS-1$
		sb.append(this.signSaver.getClass().getName());
		sb.append("</"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SIGNSAVER_CLASSNAME);
		sb.append(">\n   <"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SIGNSAVER_CONFIG);
		sb.append(">"); //$NON-NLS-1$
		try {
			sb.append(AOUtil.properties2Base64(this.signSaver.getConfig()));
		}
		catch (final IOException e) {
			Logger.getLogger("es.gob.afirma").severe( //$NON-NLS-1$
				"Error convirtiendo la configuracion del objeto de guardado de la firma '" + getId() + "' a Base64: " + e //$NON-NLS-1$ //$NON-NLS-2$
			);
		}
		sb.append("</"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SIGNSAVER_CONFIG);
		sb.append(">\n  </"); //$NON-NLS-1$
		sb.append(XML_ELEMENT_SIGNSAVER);
		sb.append(">\n </singlesign>"); //$NON-NLS-1$

		return sb.toString();
	}

	/** Realiza el proceso de prefirma, incluyendo la descarga u obtenci&oacute;n de datos.
	 * @param certChain Cadena de certificados del firmante.
	 * @param algorithm Algoritmo de firma.
	 * @return Nodo <code>firma</code> del XML de datos trif&aacute;sicos (sin ninguna etiqueta
	 *         antes ni despu&eacute;s).
	 * @throws AOException Si hay problemas en la propia firma electr&oacute;nica.
	 * @throws IOException Si hay problemas en la obtenci&oacute;n, tratamiento o gradado de datos. */
	String doPreProcess(final X509Certificate[] certChain,
			            final SingleSignConstants.SignAlgorithm algorithm) throws IOException,
			                                                                      AOException {
		return SingleSignPreProcessor.doPreProcess(this, certChain, algorithm);
	}

	/** Obtiene la tarea de preproceso de firma para ser ejecutada en paralelo.
	 * @param certChain Cadena de certificados del firmante.
	 * @param algorithm Algoritmo de firma.
	 * @return Tarea de preproceso de firma para ser ejecutada en paralelo. */
	Callable<String> getPreProcessCallable(final X509Certificate[] certChain,
                                                  final SingleSignConstants.SignAlgorithm algorithm) {
		return new Callable<String>() {
			@Override
			public String call() throws IOException, AOException {
				return doPreProcess(certChain, algorithm);
			}
		};
	}

	/** Realiza el proceso de postfirma, incluyendo la subida o guardado de datos.
	 * @param certChain Cadena de certificados del firmante.
	 * @param td Datos trif&aacute;sicos relativos <b>&uacute;nicamente</b> a esta firma.
	 *           Debe serializarse como un XML con esta forma (ejemplo):
	 *           <pre>
	 *            &lt;xml&gt;
	 *             &lt;firmas&gt;
	 *              &lt;firma Id="53820fb4-336a-47ee-b7ba-f32f58e5cfd6"&gt;
	 *               &lt;param n="PRE"&gt;MYICXDAYBgk[...]GvykA=&lt;/param&gt;
	 *               &lt;param n="PK1"&gt;dC2dIILB9HV[...]xT1bY=&lt;/param&gt;
	 *               &lt;param n="NEED_PRE"&gt;true&lt;/param&gt;
	 *              &lt;/firma&gt;
	 *             &lt;/firmas&gt;
	 *            &lt;/xml&gt;
	 *           </pre>
	 * @param algorithm Algoritmo de firma.
	 * @param batchId Identificador del lote de firma.
	 * @throws AOException Si hay problemas en la propia firma electr&oacute;nica.
	 * @throws IOException Si hay problemas en la obtenci&oacute;n, tratamiento o gradado de datos.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario. */
	void doPostProcess(final X509Certificate[] certChain,
			                  final TriphaseData td,
			                  final SingleSignConstants.SignAlgorithm algorithm,
			                  final String batchId) throws IOException,
			                                               AOException,
			                                               NoSuchAlgorithmException {
		SingleSignPostProcessor.doPostProcess(
			this, certChain, td, algorithm, batchId
		);
	}

	static class CallableResult {

		private final String signId;
		private final Exception exception;

		CallableResult(final String id) {
			this.signId = id;
			this.exception = null;
		}

		CallableResult(final String id, final Exception e) {
			this.signId = id;
			this.exception = e;
		}

		boolean isOk() {
			return this.exception == null;
		}

		Exception getError() {
			return this.exception;
		}

		String getSignatureId() {
			return this.signId;
		}
	}

	/** Obtiene la tarea de postproceso de firma para ser ejecutada en paralelo.
	 * @param certChain Cadena de certificados del firmante.
	 * @param td Datos trif&aacute;sicos relativos <b>&uacute;nicamente</b> a esta firma.
	 *           Debe serializarse como un XML con esta forma (ejemplo):
	 *           <pre>
	 *            &lt;xml&gt;
	 *             &lt;firmas&gt;
	 *              &lt;firma Id="53820fb4-336a-47ee-b7ba-f32f58e5cfd6"&gt;
	 *               &lt;param n="PRE"&gt;MYICXDAYBgk[...]GvykA=&lt;/param&gt;
	 *               &lt;param n="PK1"&gt;dC2dIILB9HV[...]xT1bY=&lt;/param&gt;
	 *               &lt;param n="NEED_PRE"&gt;true&lt;/param&gt;
	 *              &lt;/firma&gt;
	 *             &lt;/firmas&gt;
	 *            &lt;/xml&gt;
	 *           </pre>
	 * @param algorithm Algoritmo de firma.
	 * @param batchId Identificador del lote de firma.
	 * @return Tarea de postproceso de firma para ser ejecutada en paralelo. */
	Callable<CallableResult> getPostProcessCallable(final X509Certificate[] certChain,
			                                                          final TriphaseData td,
			                                                          final SingleSignConstants.SignAlgorithm algorithm,
			                                                          final String batchId) {
		return new Callable<CallableResult>() {
			@Override
			public CallableResult call() {
				try {
					doPostProcess(certChain, td, algorithm, batchId);
				}
				catch(final Exception e) {
					return new CallableResult(getId(), e);
				}
				return new CallableResult(getId());
			}
		};

	}

	SingleSign(final Node singleSignNode) throws DOMException, IOException {
		if (!(singleSignNode instanceof Element)) {
			throw new IllegalArgumentException(
				"El nodo de definicion de la firma debe ser un Elemento DOM" //$NON-NLS-1$
			);
		}
		final Element el = (Element)singleSignNode;

		this.dataSource = el.getElementsByTagName(XML_ELEMENT_DATASOURCE).item(0).getTextContent();

		this.extraParams = new Properties();
		final NodeList tmpNl = el.getElementsByTagName(XML_ELEMENT_EXTRAPARAMS);
		if (tmpNl != null && tmpNl.getLength() > 0) {
			final String extraParamsText = new String(
						Base64.decode(tmpNl.item(0).getTextContent())
					).replace("\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
			this.extraParams.load(new ByteArrayInputStream(extraParamsText.getBytes()));
		}

		this.format = SignFormat.getFormat(
			el.getElementsByTagName(XML_ELEMENT_FORMAT).item(0).getTextContent()
		);

		this.id = el.getAttribute(XML_ATTRIBUTE_ID);
		if (this.id == null || "".equals(this.id)) { //$NON-NLS-1$
			throw new IllegalArgumentException(
				"Es obligatorio establecer identificadores unicos de firma" //$NON-NLS-1$
			);
		}
		this.extraParams.put(PROP_ID, getId());

		this.subOperation = SignSubOperation.getSubOperation(
			el.getElementsByTagName(XML_ELEMENT_SUBOPERATION).item(0).getTextContent()
		);

		final Element signSaverElement = (Element) el.getElementsByTagName(XML_ELEMENT_SIGNSAVER).item(0);
		final SignSaver ssaver;
		try {
			ssaver = (SignSaver) Class.forName(
				signSaverElement.getElementsByTagName(XML_ELEMENT_SIGNSAVER_CLASSNAME).item(0).getTextContent()
			).newInstance();
		}
		catch (final Exception ex) {
			throw new IOException(
				"No se ha podido instanciar el objeto de guardado: " + ex, ex //$NON-NLS-1$
			);
		}
		ssaver.init(
			AOUtil.base642Properties(
				signSaverElement.getElementsByTagName(XML_ELEMENT_SIGNSAVER_CONFIG).item(0).getTextContent()
			)
		);

		this.signSaver = ssaver;
	}

	/** Crea una definici&oacute;n de tarea de firma electr&oacute;nica &uacute;nica.
	 * @param id Identificador de la firma.
	 * @param dataSrc Datos a firmar.
	 * @param fmt Formato de firma.
	 * @param subOp Tipo de firma a realizar.
	 * @param xParams Opciones adicionales de la firma.
	 * @param ss Objeto para guardar la firma una vez completada. */
	public SingleSign(final String id,
			          final String dataSrc,
			          final SignFormat fmt,
			          final SignSubOperation subOp,
			          final Properties xParams,
			          final SignSaver ss) {

		if (dataSrc == null) {
			throw new IllegalArgumentException(
				"El origen de los datos a firmar no puede ser nulo" //$NON-NLS-1$
			);
		}

		if (fmt == null) {
			throw new IllegalArgumentException(
				"El formato de firma no puede ser nulo" //$NON-NLS-1$
			);
		}

		if (ss == null) {
			throw new IllegalArgumentException(
				"El objeto de guardado de firma no puede ser nulo" //$NON-NLS-1$
			);
		}

		this.dataSource = dataSrc;
		this.extraParams = xParams != null ? xParams : new Properties();
		this.format = fmt;

		this.id = id != null ? id : UUID.randomUUID().toString();
		this.extraParams.put(PROP_ID, getId());

		this.subOperation = subOp;
		this.signSaver = ss;
	}

	void save(final byte[] dataToSave) throws IOException {
		this.signSaver.saveSign(this, dataToSave);
	}

	Callable<CallableResult> getSaveCallable(final TempStore ts, final String batchId) {
		return new Callable<CallableResult>() {
			@Override
			public CallableResult call() {
				try {
					save(ts.retrieve(SingleSign.this, batchId));
				}
				catch(final Exception e) {
					return new CallableResult(getId(), e);
				}
				return new CallableResult(getId());
			}
		};
	}

	String getId() {
		return this.id;
	}

	byte[] getData() throws IOException {
		checkDataSource(this.dataSource);
		return DataDownloader.downloadData(this.dataSource);
	}

	static final class ProcessResult {

		enum Result {
			NOT_STARTED,
			DONE_AND_SAVED,
			DONE_BUT_NOT_SAVED_YET,
			DONE_BUT_SAVED_SKIPPED,
			DONE_BUT_ERROR_SAVING,
			ERROR_PRE,
			ERROR_POST,
			SKYPPED,
			SAVE_ROLLBACKED;
		}

		private final Result result;
		private final String description;
		private String signId;

		boolean wasSaved() {
			return Result.DONE_AND_SAVED.equals(this.result);
		}

		static final ProcessResult PROCESS_RESULT_OK_UNSAVED = new ProcessResult(Result.DONE_BUT_NOT_SAVED_YET, null);
		static final ProcessResult PROCESS_RESULT_SKYPPED    = new ProcessResult(Result.SKYPPED,                null);
		static final ProcessResult PROCESS_RESULT_DONE_SAVED = new ProcessResult(Result.DONE_AND_SAVED,         null);
		static final ProcessResult PROCESS_RESULT_ROLLBACKED = new ProcessResult(Result.SAVE_ROLLBACKED,        null);

		ProcessResult(final Result r, final String d) {
			if (r == null) {
				throw new IllegalArgumentException(
					"El resultado no puede ser nulo" //$NON-NLS-1$
				);
			}
			this.result = r;
			this.description = d != null ? d : ""; //$NON-NLS-1$
		}

		@Override
		public String toString() {
			return "<signresult id=\"" + this.signId + "\" result=\"" + this.result + "\" description=\"" + this.description + "\"/>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		}

		void setId(final String id) {
			this.signId = id;
		}

	}

	private ProcessResult processResult = new ProcessResult(Result.NOT_STARTED, null);

	void setProcessResult(final ProcessResult pResult) {
		this.processResult = pResult;
	}

	ProcessResult getProcessResult() {
		this.processResult.setId(getId());
		return this.processResult;
	}

	void rollbackSave() {
		this.signSaver.rollback(this);
	}

}
