package es.gob.afirma.triphase.signer.processors;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.TriphaseData.TriSign;
import es.gob.afirma.signers.pades.InvalidPdfException;
import es.gob.afirma.signers.pades.PAdESTriPhaseSigner;
import es.gob.afirma.signers.pades.PdfSignResult;

/** Procesador de firmas trif&aacute;sicas PAdES.
 * @author Tom&aacute;s Garc&iacute;a Mer&aacute;s. */
public final class PAdESTriPhasePreProcessor implements TriPhasePreProcessor {

	/** Momento de la firma, establecido en el servidor. */
	private static final String PROPERTY_NAME_SIGN_TIME = "TIME"; //$NON-NLS-1$

	/** Identificador interno del PDF. */
	private static final String PROPERTY_NAME_PDF_UNIQUE_ID = "PID"; //$NON-NLS-1$

	/** Prefijo para cada prefirma. */
	private static final String PROPERTY_NAME_PRESIGN = "PRE"; //$NON-NLS-1$

	/** Firma PKCS#1. */
	private static final String PROPERTY_NAME_PKCS1_SIGN = "PK1"; //$NON-NLS-1$

	/** Indica si la postfirma requiere la prefirma. */
	private static final String PROPERTY_NAME_NEED_PRE = "NEED_PRE"; //$NON-NLS-1$

	/** Manejador de registro. */
	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	@Override
	public byte[] preProcessPreSign(final byte[] data,
			                        final String algorithm,
			                        final X509Certificate[] cert,
			                        final Properties extraParams) throws IOException,
			                                                             AOException {
		LOGGER.info("Prefirma PAdES - Firma - INICIO"); //$NON-NLS-1$

		final GregorianCalendar signTime = new GregorianCalendar();

		// Primera fase (servidor)
		LOGGER.info("Se invocan las funciones internas de prefirma PAdES"); //$NON-NLS-1$
		final PdfSignResult preSignature;
		try {
			preSignature = PAdESTriPhaseSigner.preSign(
				AOSignConstants.getDigestAlgorithmName(algorithm),
				data,
				cert,
				signTime,
				extraParams
			);
		}
		catch (final InvalidPdfException e) {
			LOGGER.severe("El documento no es un PDF y no se puede firmar: " + e); //$NON-NLS-1$
			throw e;
		}

		LOGGER.info("Se prepara la respuesta de la prefirma PAdES"); //$NON-NLS-1$

		final TriphaseData triphaseData = new TriphaseData();

		// Ahora pasamos al cliente:
		// 1.- La prefirma para que haga el PKCS#1
		// 2.- La fecha generada en el servidor para reutilizarla en la postfirma
		// 3.- El ID de PDF para reutilizarlo en la postfirma

		final Map<String, String> signConfig = new HashMap<String, String>();
		signConfig.put(PROPERTY_NAME_PRESIGN, Base64.encode(preSignature.getSign()));
		signConfig.put(PROPERTY_NAME_NEED_PRE, Boolean.TRUE.toString());
		signConfig.put(PROPERTY_NAME_SIGN_TIME, Long.toString(signTime.getTimeInMillis()));
		signConfig.put(PROPERTY_NAME_PDF_UNIQUE_ID, Base64.encode(preSignature.getFileID().getBytes()));

		triphaseData.addSignOperation(
			new TriSign(
				signConfig,
				TriPhaseUtil.getSignatureId(extraParams)
			)
		);

		LOGGER.info("Prefirma PAdES - Firma - FIN"); //$NON-NLS-1$

		return triphaseData.toString().getBytes();
	}

	@Override
	public byte[] preProcessPostSign(final byte[] docBytes,
			                         final String algorithm,
			                         final X509Certificate[] cert,
			                         final Properties extraParams,
			                         final byte[] session) throws NoSuchAlgorithmException,
			                                                      AOException,
			                                                      IOException {

		return preProcessPostSign(docBytes, algorithm, cert, extraParams, TriphaseData.parser(session));
	}

	@Override
	public byte[] preProcessPostSign(final byte[] docBytes,
			                         final String algorithm,
			                         final X509Certificate[] cert,
			                         final Properties extraParams,
			                         final TriphaseData triphaseData) throws NoSuchAlgorithmException,
			                                                                 AOException,
			                                                                 IOException {

		LOGGER.info("Postfirma PAdES - Firma - INICIO"); //$NON-NLS-1$

		// Cargamos la configuracion de la operacion
		if (triphaseData.getSignsCount() < 1) {
			LOGGER.severe("No se ha encontrado la informacion de firma en la peticion"); //$NON-NLS-1$
			throw new AOException("No se ha encontrado la informacion de firma en la peticion"); //$NON-NLS-1$
		}

		// Preparo la fecha de firma
		final GregorianCalendar cal = (GregorianCalendar) Calendar.getInstance();

		final TriSign signConfig = triphaseData.getSign(0);

		try {
			cal.setTimeInMillis(Long.parseLong(signConfig.getProperty(PROPERTY_NAME_SIGN_TIME)));
		}
		catch (final Exception e) {
			LOGGER.warning("La hora de firma indicada no es valida: " + e.toString()); //$NON-NLS-1$
		}

		// Ya con todos los datos hacemos la postfirma
		final PdfSignResult signResult = new PdfSignResult(
			new String(Base64.decode(signConfig.getProperty(PROPERTY_NAME_PDF_UNIQUE_ID))),
			Base64.decode(signConfig.getProperty(PROPERTY_NAME_PRESIGN)),
			null,
			cal,
			extraParams
		);

		LOGGER.info("Se invocan las funciones internas de postfirma PAdES"); //$NON-NLS-1$
		final byte[] postsign = PAdESTriPhaseSigner.postSign(
			AOSignConstants.getDigestAlgorithmName(algorithm),
			docBytes,
			cert,
			Base64.decode(signConfig.getProperty(PROPERTY_NAME_PKCS1_SIGN)),
			signResult,
			null,
			null
		);

		LOGGER.info("Postfirma PAdES - Firma - FIN"); //$NON-NLS-1$

		return postsign;
	}

	@Override
	public byte[] preProcessPreCoSign(final byte[] data,
			                          final String algorithm,
			                          final X509Certificate[] cert,
			                          final Properties extraParams) throws IOException,
			                                                               AOException {
		return preProcessPreSign(data, algorithm, cert, extraParams);
	}

	@Override
	public byte[] preProcessPostCoSign(final byte[] data,
			                           final String algorithm,
			                           final X509Certificate[] cert,
			                           final Properties extraParams,
			                           final byte[] session) throws NoSuchAlgorithmException,
			                                                        AOException,
			                                                        IOException {
		return preProcessPostSign(data, algorithm, cert, extraParams, TriphaseData.parser(session));
	}

	@Override
	public byte[] preProcessPostCoSign(final byte[] data,
			                           final String algorithm,
			                           final X509Certificate[] cert,
			                           final Properties extraParams,
			                           final TriphaseData triphaseData) throws NoSuchAlgorithmException,
			                                                        AOException,
			                                                        IOException {
		return preProcessPostSign(data, algorithm, cert, extraParams, triphaseData);
	}

	@Override
	public byte[] preProcessPreCounterSign(final byte[] sign,
			                               final String algorithm,
			                               final X509Certificate[] cert,
			                               final Properties extraParams,
			                               final CounterSignTarget targets) throws IOException, AOException {
		throw new UnsupportedOperationException("La operacion de contrafirma no esta soportada en PAdES."); //$NON-NLS-1$
	}

	@Override
	public byte[] preProcessPostCounterSign(final byte[] sign,
			                                final String algorithm,
			                                final X509Certificate[] cert,
			                                final Properties extraParams,
			                                final TriphaseData triphaseData,
			                                final CounterSignTarget targets) throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException("La operacion de contrafirma no esta soportada en PAdES."); //$NON-NLS-1$
	}

	@Override
	public byte[] preProcessPostCounterSign(final byte[] sign,
			                                final String algorithm,
			                                final X509Certificate[] cert,
			                                final Properties extraParams,
			                                final byte[] session,
			                                final CounterSignTarget targets) throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException("La operacion de contrafirma no esta soportada en PAdES."); //$NON-NLS-1$
	}
}
