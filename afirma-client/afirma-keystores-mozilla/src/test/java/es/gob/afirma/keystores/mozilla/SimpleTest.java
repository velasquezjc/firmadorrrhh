package es.gob.afirma.keystores.mozilla;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStore;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.junit.Test;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.KeyStoreUtilities;

/** Pruebas simples de almacenes Mozilla NSS. */
public final class SimpleTest {

    /** Inicio de las pruebas desde consola sin JUnit.
     * @param args No se usa.
     * @throws Exception En cualquier error. */
    public static void main(final String[] args) throws Exception {
    	System.out.println(MozillaKeyStoreUtilities.getMozillaUserProfileDirectory());
		//SimpleTest.testDirectNssUsage();
    	new SimpleTest().testKeyStoreManagerCreation();
    }

    /** Prueba de la obtenci&oacute;n de almac&eacute;n y alias con Mozilla NSS.
     * @throws Exception En cualquier error. */
    @SuppressWarnings("static-method")
    @Test
    //@Ignore // Necesita NSS
    public void testKeyStoreManagerCreation() throws Exception {
    	final AOKeyStoreManager ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
    	    AOKeyStore.MOZ_UNI, // Store
    	    null, // Lib
			"TEST-KEYSTORE", // Description //$NON-NLS-1$
			null, // PasswordCallback
			null // Parent
		);
    	final String[] aliases = ksm.getAliases();
    	for (final String alias : aliases) {
    		System.out.println(alias);
    	}
    	final Signature sig = Signature.getInstance("SHA512withRSA"); //$NON-NLS-1$
    	sig.initSign(
			ksm.getKeyEntry(
				aliases[0]
			).getPrivateKey()
		);
    	sig.update("Hola".getBytes()); //$NON-NLS-1$
    	System.out.println(AOUtil.hexify(sig.sign(), false));
    }

    /** Prueba de la obtenci&oacute;n de almac&eacute;n y alias con NSS de systema.
     * @throws Exception En cualquier error. */
    @SuppressWarnings("static-method")
    @Test
    //@Ignore // Necesita NSS
    public void testSystemKeyStoreManagerCreation() throws Exception {
    	final AOKeyStoreManager ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
    	    AOKeyStore.SHARED_NSS, // Store
    	    null, // Lib
			"TEST-KEYSTORE", // Description //$NON-NLS-1$
			null, // PasswordCallback
			null // Parent
		);
    	final String[] aliases = ksm.getAliases();
    	for (final String alias : aliases) {
    		System.out.println(alias);
    	}
    	final Signature sig = Signature.getInstance("SHA512withRSA"); //$NON-NLS-1$
    	sig.initSign(
			ksm.getKeyEntry(
				aliases[0]
			).getPrivateKey()
		);
    	sig.update("Hola".getBytes()); //$NON-NLS-1$
    	System.out.println(AOUtil.hexify(sig.sign(), false));
    }

    /** Prueba de uso directo de NSS.
     * @throws Exception En cualquier error. */
    @SuppressWarnings("static-method")
	@Test
    public void testDirectNssUsage() throws Exception {
    	final KeyStore keyStore = KeyStore.getInstance(
			"PKCS11", //$NON-NLS-1$
			loadNSS(
				"C:\\Users\\qwerty2\\AppData\\Local\\Temp\\nss", //$NON-NLS-1$
				KeyStoreUtilities.getShort("C:\\Users\\qwerty2\\AppData\\Local\\Temp\\moznProf").replace("\\", "/") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			)
		);
    	keyStore.load(null, new char[0]);
    	final Enumeration<String> aliases = keyStore.aliases();
    	while (aliases.hasMoreElements()) {
    		System.out.println(aliases.nextElement());
    	}
    }

	static Provider loadNSS(final String nssDirectory, final String mozProfileDir) throws AOException,
	                                                                                      InstantiationException,
	                                                                                      IllegalAccessException,
	                                                                                      IllegalArgumentException,
	                                                                                      InvocationTargetException,
	                                                                                      NoSuchMethodException,
	                                                                                      SecurityException,
	                                                                                      ClassNotFoundException {

		final String p11NSSConfigFile = MozillaKeyStoreUtilities.createPKCS11NSSConfigFile(
			mozProfileDir,
			nssDirectory
		);

		Logger.getLogger("es.gob.afirma").info("Configuracion de NSS para SunPKCS11:\n" + p11NSSConfigFile); //$NON-NLS-1$ //$NON-NLS-2$

		Provider p = null;
		try {
			p = (Provider) Class.forName("sun.security.pkcs11.SunPKCS11") //$NON-NLS-1$
				.getConstructor(InputStream.class)
					.newInstance(new ByteArrayInputStream(p11NSSConfigFile.getBytes()));
		}
		catch (final Exception e) {
			// No se ha podido cargar el proveedor sin precargar las dependencias
			// Cargamos las dependencias necesarias para la correcta carga
			// del almacen (en Mac se crean enlaces simbolicos)
			if (Platform.OS.MACOSX.equals(Platform.getOS())) {
				MozillaKeyStoreUtilitiesOsX.configureMacNSS(nssDirectory);
			}
			else {
				MozillaKeyStoreUtilities.loadNSSDependencies(nssDirectory);
			}

			try {
				p = (Provider) Class.forName("sun.security.pkcs11.SunPKCS11") //$NON-NLS-1$
					.getConstructor(InputStream.class)
						.newInstance(new ByteArrayInputStream(p11NSSConfigFile.getBytes()));
			}
			catch (final Exception e2) {
				// Un ultimo intento de cargar el proveedor valiendonos de que es posible que
				// las bibliotecas necesarias se hayan cargado tras el ultimo intento
				p = (Provider) Class.forName("sun.security.pkcs11.SunPKCS11") //$NON-NLS-1$
					.getConstructor(InputStream.class)
						.newInstance(new ByteArrayInputStream(p11NSSConfigFile.getBytes()));
			}
		}

		Security.addProvider(p);

		Logger.getLogger("es.gob.afirma").info("Proveedor PKCS#11 para Firefox anadido"); //$NON-NLS-1$ //$NON-NLS-2$

		return p;
	}

}