
package es.gob.afirma.signers.multi.cades;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.signers.cades.AOCAdESSigner;

/** Prueba de contrafirmas CAdES.
 * @author Carlos Gamuci. */
public class TestCountersign {

	private static final String PKCS12_KEYSTORE = "ANCERTCCP_FIRMA.p12"; //$NON-NLS-1$
	private static final String PASSWORD = "1111"; //$NON-NLS-1$
	private static final String IMPLICIT_SHA1_COUNTERSIGN_FILE = "contrafirma_implicita.csig"; //$NON-NLS-1$
	private static final String EXPLICIT_SHA1_COUNTERSIGN_FILE = "contrafirma_explicita.csig"; //$NON-NLS-1$
	private static final String EXPLICIT_SHA1_CADES_A_FILE = "cadesA.csig"; //$NON-NLS-1$
	private static final String IMPLICIT_SHA1_CADES_T_FILE = "CAdES-T.asn1"; //$NON-NLS-1$

	private static InputStream ksIs;
	private static KeyStore ks;

	/** Carga el almac&eacute;n de certificados.
	 * @throws Exception Cuando ocurre algun problema al cargar el almac&eacute;n o los datos. */
	@Before
	public void cargaAlmacen() throws Exception {
		ksIs = getClass().getClassLoader().getResourceAsStream(PKCS12_KEYSTORE);
		ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
		ks.load(ksIs, PASSWORD.toCharArray());
		ksIs.close();
	}

	/**
	 * Prueba de contrafirma de todo el &aacute;rbol de firmas de una firma expl&iacute;cita.
	 * @throws Exception Cuando se produce un error.
	 */
	@Test
	public void prueba_contrafirma_de_arbol_de_firma_explicita() throws Exception {

		final InputStream is = getClass().getClassLoader().getResourceAsStream(EXPLICIT_SHA1_COUNTERSIGN_FILE);
		final byte[] sign = AOUtil.getDataFromInputStream(is);
		is.close();

		final Properties config = new Properties();

		final AOCAdESSigner signer = new AOCAdESSigner();
		final PrivateKeyEntry pke = (PrivateKeyEntry) ks.getEntry(ks.aliases().nextElement(), new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
		final byte[] countersign = signer.countersign(
			sign,
			AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA,
			CounterSignTarget.TREE,
			null,
			pke.getPrivateKey(),
			pke.getCertificateChain(),
			config
		);

		final File tempFile = File.createTempFile("CountersignCades", ".csig"); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println("Prueba de contrafirma de arbol sobre firma explicita."); //$NON-NLS-1$
		System.out.println("El resultado se almacena en: " + tempFile.getAbsolutePath()); //$NON-NLS-1$

		final FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(countersign);
		fos.close();
	}

	/**
	 * Prueba de contrafirma de los nodos hoja de una firma explicita.
	 * @throws Exception Cuando se produce un error.
	 */
	@Test
	public void prueba_contrafirma_de_firma_explicita_nodos_hoja() throws Exception {

		final InputStream is = getClass().getClassLoader().getResourceAsStream(EXPLICIT_SHA1_COUNTERSIGN_FILE);
		final byte[] sign = AOUtil.getDataFromInputStream(is);
		is.close();

		final Properties config = new Properties();

		final AOCAdESSigner signer = new AOCAdESSigner();
		final PrivateKeyEntry pke = (PrivateKeyEntry) ks.getEntry(ks.aliases().nextElement(), new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
		final byte[] countersign = signer.countersign(
			sign,
			AOSignConstants.SIGN_ALGORITHM_SHA512WITHRSA,
			CounterSignTarget.LEAFS,
			null,
			pke.getPrivateKey(),
			pke.getCertificateChain(),
			config
		);

		final File tempFile = File.createTempFile("CountersignCades", ".csig"); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println("Prueba de contrafirma de nodos hoja sobre firma explicita."); //$NON-NLS-1$
		System.out.println("El resultado se almacena en: " + tempFile.getAbsolutePath()); //$NON-NLS-1$

		final FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(countersign);
		fos.close();
	}

	/** Prueba de contrafirma de todo el &aacute;rbol de firmas de una firma impl&iacute;cita.
	 * @throws Exception Cuando se produce un error. */
	@Test
	public void prueba_contrafirma_de_arbol_de_firma_implicita() throws Exception {

		final InputStream is = getClass().getClassLoader().getResourceAsStream(IMPLICIT_SHA1_COUNTERSIGN_FILE);
		final byte[] sign = AOUtil.getDataFromInputStream(is);
		is.close();

		final Properties config = new Properties();

		final AOCAdESSigner signer = new AOCAdESSigner();
		final PrivateKeyEntry pke = (PrivateKeyEntry) ks.getEntry(ks.aliases().nextElement(), new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
		final byte[] countersign = signer.countersign(
				sign,
				AOSignConstants.SIGN_ALGORITHM_SHA1WITHRSA,
				CounterSignTarget.TREE,
				null,
				pke.getPrivateKey(),
				pke.getCertificateChain(),
				config);

		final File tempFile = File.createTempFile("CountersignCades", ".csig"); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println("Prueba de contrafirma de arbol sobre firma implicita."); //$NON-NLS-1$
		System.out.println("El resultado se almacena en: " + tempFile.getAbsolutePath()); //$NON-NLS-1$

		final FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(countersign);
		fos.close();
	}

	/**
	 * Prueba de contrafirma de los nodos hoja de una firma implicita.
	 * @throws Exception Cuando se produce un error.
	 */
	@Test
	public void prueba_contrafirma_de_firma_implicita_nodos_hoja() throws Exception {

		final InputStream is = getClass().getClassLoader().getResourceAsStream(IMPLICIT_SHA1_COUNTERSIGN_FILE);
		final byte[] sign = AOUtil.getDataFromInputStream(is);
		is.close();

		final Properties config = new Properties();

		final AOCAdESSigner signer = new AOCAdESSigner();
		final PrivateKeyEntry pke = (PrivateKeyEntry) ks.getEntry(ks.aliases().nextElement(), new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
		final byte[] countersign = signer.countersign(
			sign,
			AOSignConstants.SIGN_ALGORITHM_SHA512WITHRSA,
			CounterSignTarget.LEAFS,
			null,
			pke.getPrivateKey(),
			pke.getCertificateChain(),
			config
		);

		final File tempFile = File.createTempFile("CountersignCades", ".csig"); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println("Prueba de contrafirma de nodos hoja sobre firma implicita."); //$NON-NLS-1$
		System.out.println("El resultado se almacena en: " + tempFile.getAbsolutePath()); //$NON-NLS-1$

		final FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(countersign);
		fos.close();
	}

	/** Main para pruebas sin JUnit.
	 * @param args No se usa.
	 * @throws Exception En cualquier error. */
	public static void main(final String[] args) throws Exception {
		final TestCountersign cs = new TestCountersign();
		cs.cargaAlmacen();
		cs.prueba_contrafirma_cades_T();
	}

	/** Prueba de contrafirma de los nodos hoja de una firma CAdES-A.
	 * @throws Exception Cuando se produce un error. */
	@Test
	public void prueba_contrafirma_cades_A() throws Exception {

		final InputStream is = getClass().getClassLoader().getResourceAsStream(EXPLICIT_SHA1_CADES_A_FILE);
		final byte[] sign = AOUtil.getDataFromInputStream(is);
		is.close();

		System.out.println("Sellos antes de la contrafirma:\n " + TimestampsAnalyzer.getCmsTimestamps(sign)); //$NON-NLS-1$

		final Properties config = new Properties();

		final AOCAdESSigner signer = new AOCAdESSigner();
		final PrivateKeyEntry pke = (PrivateKeyEntry) ks.getEntry(ks.aliases().nextElement(), new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
		final byte[] countersign = signer.countersign(
			sign,
			AOSignConstants.SIGN_ALGORITHM_SHA512WITHRSA,
			CounterSignTarget.TREE,
			null,
			pke.getPrivateKey(),
			pke.getCertificateChain(),
			config
		);

		final File tempFile = File.createTempFile("CountersignCadesA", ".csig"); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println("Prueba de contrafirma de una firma CAdES-A."); //$NON-NLS-1$
		System.out.println("El resultado se almacena en: " + tempFile.getAbsolutePath()); //$NON-NLS-1$

		final FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(countersign);
		fos.close();

		System.out.println("Sellos despues de la contrafirma:\n " + TimestampsAnalyzer.getCmsTimestamps(countersign)); //$NON-NLS-1$
	}

	/** Prueba de contrafirma de los nodos hoja de una firma CAdES-T.
	 * @throws Exception Cuando se produce un error. */
	@Test
	public void prueba_contrafirma_cades_T() throws Exception {

		final InputStream is = getClass().getClassLoader().getResourceAsStream(IMPLICIT_SHA1_CADES_T_FILE);
		final byte[] sign = AOUtil.getDataFromInputStream(is);
		is.close();

		System.out.println("Sellos antes de la contrafirma:\n " + TimestampsAnalyzer.getCmsTimestamps(sign)); //$NON-NLS-1$
		System.out.println();

		final Properties config = new Properties();

		final AOCAdESSigner signer = new AOCAdESSigner();
		final PrivateKeyEntry pke = (PrivateKeyEntry) ks.getEntry(ks.aliases().nextElement(), new KeyStore.PasswordProtection(PASSWORD.toCharArray()));
		final byte[] countersign = signer.countersign(
			sign,
			AOSignConstants.SIGN_ALGORITHM_SHA512WITHRSA,
			CounterSignTarget.TREE,
			null,
			pke.getPrivateKey(),
			pke.getCertificateChain(),
			config
		);

		final File tempFile = File.createTempFile("CountersignCadesT_", ".csig"); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println("Prueba de contrafirma de una firma CAdES-T."); //$NON-NLS-1$
		System.out.println("El resultado se almacena en: " + tempFile.getAbsolutePath()); //$NON-NLS-1$

		final FileOutputStream fos = new FileOutputStream(tempFile);
		fos.write(countersign);
		fos.close();

		System.out.println();
		System.out.println("Sellos despues de la contrafirma:\n " + TimestampsAnalyzer.getCmsTimestamps(countersign)); //$NON-NLS-1$
	}


	/** Cierra el flujo de lectura del almac&eacute;n de certificados.
	 * @throws IOException Cuando ocurre alg&uacute;n problema al cerrar el flujo de datos. */
	@SuppressWarnings("static-method")
	@After
	public void cerrar() throws IOException {
		ksIs.close();
	}
}
