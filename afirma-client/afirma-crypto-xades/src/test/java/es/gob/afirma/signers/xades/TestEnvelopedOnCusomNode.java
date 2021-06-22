package es.gob.afirma.signers.xades;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSigner;

/** Pruebas de XAdES Enveloped con inserci&oacute;n de firma en nodo espec&iacute;fico.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class TestEnvelopedOnCusomNode {

    private static final String CERT_PATH = "PFActivoFirSHA256.pfx"; //$NON-NLS-1$
    private static final String CERT_PASS = "12341234"; //$NON-NLS-1$
    private static final String CERT_ALIAS = "fisico activo prueba"; //$NON-NLS-1$

    private static final Properties p4 = new Properties();
    static {
	    p4.setProperty("format", AOSignConstants.SIGN_FORMAT_XADES_ENVELOPED); //$NON-NLS-1$
	    p4.setProperty("insertEnvelopedSignatureOnNodeByXPath", "//*[namespace-uri()='http://www.facturae.es/Facturae/2007/v3.0/Facturae'][local-name()='Facturae']"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final Properties p3 = new Properties();
    static {
	    p3.setProperty("format", AOSignConstants.SIGN_FORMAT_XADES_ENVELOPED); //$NON-NLS-1$
	    p3.setProperty("includeOnlySignningCertificate", "true"); //$NON-NLS-1$ //$NON-NLS-2$
	    p3.setProperty("insertEnvelopedSignatureOnNodeByXPath", "/bookstore/book[1]/title"); //$NON-NLS-1$ //$NON-NLS-2$
	    p3.setProperty("useManifest", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final Properties p2 = new Properties();
    static {
	    p2.setProperty("format", AOSignConstants.SIGN_FORMAT_XADES_ENVELOPED); //$NON-NLS-1$
	    p2.setProperty("includeOnlySignningCertificate", "true"); //$NON-NLS-1$ //$NON-NLS-2$
	    p2.setProperty("nodeToSign", "2"); //$NON-NLS-1$ //$NON-NLS-2$
	    p2.setProperty("useManifest", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static final Properties p1 = new Properties();
    static {
	    p1.setProperty("format", AOSignConstants.SIGN_FORMAT_XADES_ENVELOPED); //$NON-NLS-1$
	    p1.setProperty("avoidXpathExtraTransformsOnEnveloped", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    }

	/** Prueba de XAdES Enveloped con inserci&oacute;n de firma en el mismo nodo espec&iacute;fico
	 * que se firma.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void TestInsertSignatureOnSignedNode() throws Exception {
        Logger.getLogger("es.gob.afirma").setLevel(Level.WARNING); //$NON-NLS-1$
        final PrivateKeyEntry pke;
        final KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$

        ks.load(ClassLoader.getSystemResourceAsStream(CERT_PATH), CERT_PASS.toCharArray());
        pke = (PrivateKeyEntry) ks.getEntry(CERT_ALIAS, new KeyStore.PasswordProtection(CERT_PASS.toCharArray()));
        final AOSigner signer = new AOXAdESSigner();

        final byte[] data = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("xpathnodenveloped.xml")); //$NON-NLS-1$

        final byte[] result = signer.sign(
    		data,
    		"SHA512withRSA", //$NON-NLS-1$
    		pke.getPrivateKey(),
    		pke.getCertificateChain(),
    		p2
		);

        final File f = File.createTempFile("xpathnodenveloped-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        final java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        fos.write(result);
        fos.flush(); fos.close();
        System.out.println("Temporal para comprobacion manual: " + f.getAbsolutePath()); //$NON-NLS-1$
	}

	/** Prueba de XAdES Enveloped con inserci&oacute;n de firma en nodo espec&iacute;fico.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void TestInsertSignatureOnCustomNode() throws Exception {

        Logger.getLogger("es.gob.afirma").setLevel(Level.WARNING); //$NON-NLS-1$
        final PrivateKeyEntry pke;
        final KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$

        ks.load(ClassLoader.getSystemResourceAsStream(CERT_PATH), CERT_PASS.toCharArray());
        pke = (PrivateKeyEntry) ks.getEntry(CERT_ALIAS, new KeyStore.PasswordProtection(CERT_PASS.toCharArray()));
        final AOSigner signer = new AOXAdESSigner();

        final byte[] data = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("xpathnodenveloped.xml")); //$NON-NLS-1$

        final byte[] result = signer.sign(
    		data,
    		"SHA512withRSA", //$NON-NLS-1$
    		pke.getPrivateKey(),
    		pke.getCertificateChain(),
    		p3
		);

        final File f = File.createTempFile("xpathnodenveloped-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        final java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        fos.write(result);
        fos.flush(); fos.close();
        System.out.println("Temporal para comprobacion manual: " + f.getAbsolutePath()); //$NON-NLS-1$

	}

	/** Prueba de XAdES Enveloped con inserci&oacute;n de firma en nodo espec&iacute;fico sobre un XML con un namespace.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void TestInsertSignatureOnCustomNodeWithNamespace() throws Exception {

        Logger.getLogger("es.gob.afirma").setLevel(Level.WARNING); //$NON-NLS-1$
        final PrivateKeyEntry pke;
        final KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$

        ks.load(ClassLoader.getSystemResourceAsStream(CERT_PATH), CERT_PASS.toCharArray());
        pke = (PrivateKeyEntry) ks.getEntry(CERT_ALIAS, new KeyStore.PasswordProtection(CERT_PASS.toCharArray()));
        final AOSigner signer = new AOXAdESSigner();

        final byte[] data = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("xpathnodenvelopedwithns.xml")); //$NON-NLS-1$

        final byte[] result = signer.sign(
    		data,
    		"SHA512withRSA", //$NON-NLS-1$
    		pke.getPrivateKey(),
    		pke.getCertificateChain(),
    		p4
		);

        final File f = File.createTempFile("xpathnodenvelopedwithns-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        final java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        fos.write(result);
        fos.flush(); fos.close();
        System.out.println("Temporal para comprobacion manual: " + f.getAbsolutePath()); //$NON-NLS-1$

	}

	/** Prueba de XAdES Enveloped sin transformaci&oacute;n XPath extra.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testAvoidXpathExtraTransformsOnEnveloped() throws Exception {
        Logger.getLogger("es.gob.afirma").setLevel(Level.WARNING); //$NON-NLS-1$
        final PrivateKeyEntry pke;
        final KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$

        ks.load(ClassLoader.getSystemResourceAsStream(CERT_PATH), CERT_PASS.toCharArray());
        pke = (PrivateKeyEntry) ks.getEntry(CERT_ALIAS, new KeyStore.PasswordProtection(CERT_PASS.toCharArray()));
        final AOSigner signer = new AOXAdESSigner();

        final byte[] data = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("xpathnodenveloped.xml")); //$NON-NLS-1$

        final byte[] result = signer.sign(
    		data,
    		"SHA512withRSA", //$NON-NLS-1$
    		pke.getPrivateKey(),
    		pke.getCertificateChain(),
    		p1
		);

        final File f = File.createTempFile("xpathnodenveloped-", ".xml"); //$NON-NLS-1$ //$NON-NLS-2$
        final java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        fos.write(result);
        fos.flush(); fos.close();
        System.out.println("Temporal para comprobacion manual: " + f.getAbsolutePath()); //$NON-NLS-1$
	}

}
