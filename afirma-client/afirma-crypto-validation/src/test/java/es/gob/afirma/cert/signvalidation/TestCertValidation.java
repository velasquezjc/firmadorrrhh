package es.gob.afirma.cert.signvalidation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import org.junit.Test;

import es.gob.afirma.cert.certvalidation.CertificateVerifierFactory;
import es.gob.afirma.cert.certvalidation.ValidationResult;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;

/** Pruebas de validaci&oacute;n de certificados.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. 
 * *Update: gov.ar*/
public final class TestCertValidation {

	/** Prueba de certificados FNMT Componentes.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	////@Test
	public void testFnmt() throws Exception {
		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
			TestCertValidation.class.getResourceAsStream("/cert_test_fnmt.cer") //$NON-NLS-1$
		);
		final ValidationResult vr = CertificateVerifierFactory.getCertificateVerifier(
			cert
		).validateCertificate();
		vr.check();
	}
	
	/** Prueba de certificados MDEF.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	////@Test
	public void testMdef() throws Exception {
		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
			TestCertValidation.class.getResourceAsStream("/Defensa.cer") //$NON-NLS-1$
		);
		final ValidationResult vr = CertificateVerifierFactory.getCertificateVerifier(
			cert
		).validateCertificate();
		vr.check();
	}
	
	/** Prueba de certificados DNIe.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	///@Test
	public void testDnie() throws Exception {
		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
			TestCertValidation.class.getResourceAsStream("/certMinHAP.cer") //$NON-NLS-1$
		);
		final ValidationResult vr = CertificateVerifierFactory.getCertificateVerifier(
			cert
		).validateCertificate();
		vr.check();
	}
	
	/** Prueba de certificados ONTI.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testOnti() throws Exception {
		//en este caso se carga el certificado
/*		final X509Certificate cert2 = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
				TestCertValidation.class.getResourceAsStream("/gde-valido.cer") //$NON-NLS-1$
			);
*/		
		//	final InputStream is2 = ClassLoader.getSystemResourceAsStream("gde-valido.cer");
		//	final byte[] certb64 = AOUtil.getDataFromInputStream(is2);
		
		
		//obtenemos el certificado codificado en b64 sin urlsafe
		//asi se recibe desde la web
		ClassLoader classLoader = getClass().getClassLoader();
		final byte[] certb64 = AOUtil.getDataFromInputStream(classLoader.getResourceAsStream("gde-valido.b64"));
		
		//System.out.println(Arrays.toString(certb64));
		
		//nota: los b64 estan en modo no url safe,osea no va a ser usado como url porque solo es el certificado
		// Decodificamos como binario, tambien se puede decodificar a string y generar el inputstream a partir del mismo
		final byte[] newBin = Base64.decode(certb64,0,certb64.length,false);
		
		final InputStream is = new ByteArrayInputStream(newBin);
		//regeneramos el certificado		
		final X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
		//validamos el certificado, si esta en fecha,firmado correctamente por el emisor y su estado de revocacion		
		final ValidationResult vr = CertificateVerifierFactory.getCertificateVerifier(
			cert
		).validateCertificate();
		//lanza una excepcion en caso de invalidez
		vr.check();
		//imprimimos una respuesta json,sirve para la web
		System.out.println(vr.toJsonString()); //$NON-NLS-1$

	}

	// convert InputStream to String
		private static String getStringFromInputStream(InputStream is) {

			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();

			String line;
			try {

				br = new BufferedReader(new InputStreamReader(is));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return sb.toString();

		}

}
