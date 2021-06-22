package es.gob.afirma.signers.signer.processors;

import java.util.Properties;
import java.util.UUID;

final class TriPhaseUtil {

	private static final String PROP_ID = "SignatureId"; //$NON-NLS-1$

	private TriPhaseUtil() {
		// No instanciable
	}

	/**
	 * Recupera de los par&aacute;metros de configuraci&oacute;n el identificado de la firma
	 * que se est&aacute; procesando y, en caso de no estar reflejado, genera un identificador
	 * y lo agrega al extraParams.
	 * @param extraParams Par&aacute;metro de configuraci&oacute;n de la firma.
	 * @return Identificador de la firma.
	 */
	static String getSignatureId(final Properties extraParams) {
		if (extraParams == null) {
			return UUID.randomUUID().toString();
		}
		final String id = extraParams.getProperty(PROP_ID);
		if (id == null) {
			return UUID.randomUUID().toString();
		}
		return id;
	}

}
