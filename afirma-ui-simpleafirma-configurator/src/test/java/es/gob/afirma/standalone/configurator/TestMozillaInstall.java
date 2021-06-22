package es.gob.afirma.standalone.configurator;

import java.io.File;
import java.util.Locale;

import org.junit.Test;

/** Pruebas de instalaci&oacute;n de certificado ra&iacute;z SSL en Firefox.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class TestMozillaInstall {

	/** Prueba simple de instalaci&oacute;n de certificado ra&iacute;z SSL en Firefox.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testSimpleFirefoxInstall() throws Exception {
		//ConfiguratorFirefox.installRootCAMozillaKeyStore(new File("/var/tmp")); //$NON-NLS-1$
		//ClassLoader classLoader = getClass().getClassLoader();
		//File file = new File(classLoader.getResource("SSL_CERT.cer").getFile());
		//System.out.println(file.getAbsolutePath());
		//File file2 = new File(TestMozillaInstall.class.getClassLoader().getResource("SSL_CERT.cer").toURI());
		//System.out.println(file2.getAbsolutePath());
		//carpeta donde se encuentra el certificado autofirma.root
		
		ConfiguratorFirefox.installRootCAMozillaKeyStore(new File("/var"));
			//System.out.println(System.getProperty("os.locale"));
		//System.out.println(Locale.getDefault());
		/*if ((Locale.getDefault()).toString().contains("es_")){
			System.out.println(Locale.getDefault());
		}*/
		//System.out.println(System.getProperty("user.language"));
	//
	}

}
