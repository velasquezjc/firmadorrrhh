package es.gob.afirma.triphase.signer.processors;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.TriphaseData;

/** Operaciones gen&eacute;ricas de firma trif&aacute;sica.
 * @author Tom&aacute;s Garc&iacute;a Mer&aacute;s. */
public interface TriPhasePreProcessor {

	/** Prefirma.
	 * @param data Datos a firmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @return Resultado de la prefirma.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	TriphaseData preProcessPreSign(byte[] data, String algorithm, X509Certificate[] cert, Properties extraParams) throws IOException, AOException;

	/** Postfirma.
	 * @param data Datos a firmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @param session Prefirma.
	 * @return Firma completa.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	byte[] preProcessPostSign(byte[] data, String algorithm, X509Certificate[] cert, Properties extraParams, byte[] session) throws NoSuchAlgorithmException, IOException, AOException;

	/** Postfirma.
	 * @param data Datos a firmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @param sessionData Datos de la sesi&oacute;n (PK1, prefirma,...).
	 * @return Firma completa.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	byte[] preProcessPostSign(byte[] data, String algorithm, X509Certificate[] cert, Properties extraParams, TriphaseData sessionData) throws NoSuchAlgorithmException, IOException, AOException;


	/** Precofirma.
	 * @param data Datos a cofirmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @return Resultado de la precofirma.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	TriphaseData preProcessPreCoSign(byte[] data, String algorithm, X509Certificate[] cert, Properties extraParams) throws IOException, AOException;

	/** Postcofirma.
	 * @param data Datos a cofirmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @param session Precofirma.
	 * @return Cofirma completa.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	byte[] preProcessPostCoSign(byte[] data, String algorithm, X509Certificate[] cert, Properties extraParams, byte[] session) throws NoSuchAlgorithmException, AOException, IOException;

	/** Postcofirma.
	 * @param data Datos a cofirmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @param sessionData Datos de la sesi&oacute;n (PK1, prefirma,...).
	 * @return Cofirma completa.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	byte[] preProcessPostCoSign(byte[] data, String algorithm, X509Certificate[] cert, Properties extraParams, TriphaseData sessionData) throws NoSuchAlgorithmException, AOException, IOException;


	/** Precontrafirma.
	 * @param sign Firma a contrafirmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @param targets Objetivo de la contrafirma.
	 * @return Resultado de la precontrafirma.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	TriphaseData preProcessPreCounterSign(byte[] sign, String algorithm, X509Certificate[] cert, Properties extraParams, CounterSignTarget targets) throws IOException, AOException;

	/** Postcontrafirma.
	 * @param sign Firma a contrafirmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @param session Precontrafirma.
	 * @param targets Objetivo de la contrafirma.
	 * @return Contrafirma completa.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	byte[] preProcessPostCounterSign(byte[] sign, String algorithm, X509Certificate[] cert, Properties extraParams, byte[] session, CounterSignTarget targets) throws NoSuchAlgorithmException, AOException, IOException;

	/** Postcontrafirma.
	 * @param sign Firma a contrafirmar.
	 * @param algorithm Algoritmo de firma.
	 * @param cert Cadena de certificados del firmante.
	 * @param extraParams Par&aacute;meros adicionales de la firma.
	 * @param sessionData Datos de la sesi&oacute;n (PK1, prefirma,...).
	 * @param targets Objetivo de la contrafirma.
	 * @return Contrafirma completa.
	 * @throws NoSuchAlgorithmException Si no se soporta alg&uacute;n algoritmo necesario.
	 * @throws IOException Si hay errores en el tratamiento de los datos.
	 * @throws AOException En cualquier otro tipo de error. */
	byte[] preProcessPostCounterSign(byte[] sign, String algorithm, X509Certificate[] cert, Properties extraParams, TriphaseData sessionData, CounterSignTarget targets) throws NoSuchAlgorithmException, AOException, IOException;

}
