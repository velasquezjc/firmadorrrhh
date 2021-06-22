package es.gob.afirma.core.signers;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

import es.gob.afirma.core.misc.Base64;

/** Utilidades generales para firmas trif&aacute;sicas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TriphaseUtil {

	/** Separador que debe usarse para incluir varios certificados dentro del mismo par&aacute;metro. */
	private static final String PARAM_NAME_CERT_SEPARATOR = ","; //$NON-NLS-1$

	private TriphaseUtil() {
		// No instanciable
	}

	/** Compone un texto con los certificados de la cadena indicada codificados en Base64 de tipo <i>URL-Safe</i> concatenados
	 * usando el separador por defecto.
	 * @param certChain Cadena de certificados.
	 * @return Texto con los certificados de la cadena codificados en Base64 de tipo <i>URL-Safe</i> y concatenados
	 *         usando el separador por defecto.
	 * @throws CertificateEncodingException Si hay problemas obteniendo la codificaci&oacute;n de alg&uacute;n certificado. */
	public static String prepareCertChainParam(final Certificate[] certChain) throws CertificateEncodingException {
		if (certChain == null || certChain.length < 1) {
			throw new IllegalArgumentException(
				"La cadena de certificados no puede ser nula ni vacia" //$NON-NLS-1$
			);
		}
		final StringBuilder sb = new StringBuilder();
		for (final Certificate cert : certChain) {
			sb.append(Base64.encode(cert.getEncoded(), true));
			sb.append(PARAM_NAME_CERT_SEPARATOR);
		}
		final String ret = sb.toString();
		return ret.substring(0, ret.length() - PARAM_NAME_CERT_SEPARATOR.length());
	}

}
