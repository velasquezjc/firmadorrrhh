package es.gob.afirma.standalone.configurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.Locale;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.standalone.configurator.CertUtil.CertPack;
import es.gob.afirma.standalone.configurator.ConfiguratorFirefox.MozillaProfileNotFoundException;

/** Configura la instalaci&oacute;n en Windows para la correcta ejecuci&oacute;n de AutoFirma. */
final class ConfiguratorWindows implements Configurator {

	static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String KS_FILENAME = "firmadigitalkeystore.pfx"; //$NON-NLS-1$
	private static final String FILE_AUTOFIRMA_CERTIFICATE = "FirmaDigital_ROOT_navegador.cer"; //$NON-NLS-1$
	private static final String KS_PASSWORD = "654321malaracha"; //$NON-NLS-1$

	//private static String CHROME_CONFIG_FILE = "AppData/Local/Google/Chrome/User Data/Local State"; //$NON-NLS-1$
	/** Nombre del usuario por defecto en Windows. Este usuario es el que se usa como base para
	 * crear nuevos usuarios y no se deber&iacute;a tocar. */
	private static String DEFAULT_WINDOWS_USER_NAME = "Default"; //$NON-NLS-1$

	// A partir de la version 57 de Chrome cambia el fichero en el que se guardan los protocol handler
	private static final String CHROME_V56_OR_LOWER_CONFIG_FILE = "AppData/Local/Google/Chrome/User Data/Local State"; //$NON-NLS-1$
	private static final String CHROME_V57_OR_HIGHER_CONFIG_FILE = "AppData/Local/Google/Chrome/User Data/Default/Preferences"; //$NON-NLS-1$


	@Override
	public void configure(final Console window) throws IOException, GeneralSecurityException {

		window.print(Messages.getString("ConfiguratorWindows.2")); //$NON-NLS-1$

		final File appDir = ConfiguratorUtil.getApplicationDirectory();

		window.print(Messages.getString("ConfiguratorWindows.3") + appDir.getAbsolutePath()); //$NON-NLS-1$

		//TODO:importante, en caso de existir el keystore autofirma.pfx no se instalara nada
		//esto hara que la app descargada no se pueda comunicar con el navegador
		//debido a que se genero otro certificado raiz AutoFirma.ROOT y no pudo instalarlo
		//es mas el viejo certificado tampoco lo borro. Seria mejor preinstalar un certificado
		//y aqui solo instalarlo en la app, debido a que la isntalacion en windows my
		//require ser administrador para poner certificados de tipo confiable, pero para cualquier usuario 
		//no siendo administrador se puede utilizar el almacen de firefox, chrome usa el de windows
		if (!checkSSLKeyStoreGenerated(appDir)) {
			window.print(Messages.getString("ConfiguratorWindows.5")); //$NON-NLS-1$
			final CertPack certPack = CertUtil.getCertPackForLocalhostSsl(
				ConfiguratorUtil.CERT_ALIAS,
				KS_PASSWORD
			);

			window.print(Messages.getString("ConfiguratorWindows.11")); //$NON-NLS-1$

			//Generacion del certificado pfx
			ConfiguratorUtil.installFile(
				certPack.getPkcs12(),
				new File(appDir, KS_FILENAME)
			);

			//Generacion del certificado raiz .cer
			ConfiguratorUtil.installFile(
					certPack.getCaCertificate().getEncoded(),
					new File(appDir, FILE_AUTOFIRMA_CERTIFICATE));

			//instalo el certificado en chrome
			window.print(Messages.getString("ConfiguratorWindows.6")); //$NON-NLS-1$
			importCARootOnWindowsKeyStore(certPack.getCaCertificate());
			// Insertamos el protocolo afirma en el fichero de configuracion de Google Chrome
			configureChrome(window, true);
			
			//instalo el certificado en firefox
			try {
				window.print(Messages.getString("ConfiguratorWindows.13")); //$NON-NLS-1$
				ConfiguratorFirefox.installRootCAMozillaKeyStore(appDir);
				ConfiguratorFirefox.removeConfigurationFiles(appDir);
				window.print(Messages.getString("ConfiguratorWindows.4")); //$NON-NLS-1$
				window.print(Messages.getString("ConfiguratorWindows.9")); //$NON-NLS-1$
				window.print(Messages.getString("ConfiguratorWindows.7")); //$NON-NLS-1$
			}
			catch(final MozillaProfileNotFoundException e) {
				window.print(Messages.getString("ConfiguratorWindows.12") + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
			}catch (final Exception e) {
				LOGGER.severe("Error en la configuracion de Firefox: " + e); //$NON-NLS-1$
				/*ConsoleManager.showErrorMessage(
						null,
						Messages.getString("Error en la instalacion del certificado de CA en alguno de los perfiles firefox de usuario") //$NON-NLS-1$
					);*/
				throw e;
			
			}
//*			window.print(Messages.getString("ConfiguratorWindows.6")); //$NON-NLS-1$
//*			importCARootOnWindowsKeyStore(certPack.getCaCertificate());
//*			// Insertamos el protocolo afirma en el fichero de configuracion de Google Chrome
//			configureChrome(window, true);
		
		}
		else {
			window.print(Messages.getString("ConfiguratorWindows.14")); //$NON-NLS-1$
		}

		// Insertamos el protocolo afirma en el fichero de configuracion de Google Chrome
//*		configureChrome(window, true);

		window.print(Messages.getString("ConfiguratorWindows.8")); //$NON-NLS-1$
	}

	/** Comprueba si ya existe un almac&eacute;n de certificados generado.
	 * @param appDir Directorio de la aplicaci&oacute;n.
	 * @return {@code true} si ya existe un almacen de certificados SSL, {@code false} en caso contrario. */
	private static boolean checkSSLKeyStoreGenerated(final File appDir) {
		return new File(appDir, KS_FILENAME).exists();
	}

	@Override
	public void uninstall() {
		
		LOGGER.info("Desinstalamos el certificado raiz del almacen de Windows"); //$NON-NLS-1$
		uninstallRootCAWindowsKeyStore();

		LOGGER.info("Desinstalamos el certificado raiz del almacen de Firefox"); //$NON-NLS-1$
		ConfiguratorFirefox.uninstallRootCAMozillaKeyStore(ConfiguratorUtil.getApplicationDirectory());

		// Insertamos el protocolo afirma en el fichero de configuracion de Google Chrome
		configureChrome(null, false);

		// No es necesario eliminar nada mas porque el verdadero proceso de desinstalacion
        // eliminara el directorio de aplicacion con todo su contenido
	}
	
	private static void uninstallRootCAWindowsKeyStore() {
		try {
			final KeyStore ks = KeyStore.getInstance("Windows-ROOT"); //$NON-NLS-1$
			ks.load(null,  null);
			ks.deleteEntry(CertUtil.AF_ROOT_SUBJECT_PRINCIPAL);
		}
		catch (final Exception e) {
			LOGGER.warning("No se pudo desinstalar el certificado SSL raiz del almacen de Windows: " + e); //$NON-NLS-1$
		}
	}
	
	private static void importCARootOnWindowsKeyStore(final Certificate cert) throws GeneralSecurityException, IOException {

		final KeyStore ks = KeyStore.getInstance("Windows-ROOT"); //$NON-NLS-1$
		ks.load(null,  null);

		boolean installed = false;
		boolean cancelled = false;
		do {
			try {
				ks.setCertificateEntry(CertUtil.AF_ROOT_SUBJECT_PRINCIPAL, cert);
				installed = true;
			}
			catch (final KeyStoreException e) {
				LOGGER.warning(
					"No se pudo instalar la CA del certificado SSL para el socket en el almacen de Windows: " + e //$NON-NLS-1$
				);
				final int result = JOptionPane.showConfirmDialog(
					null,
					Messages.getString("ConfiguratorWindows.0"), //$NON-NLS-1$
					Messages.getString("ConfiguratorWindows.1"), //$NON-NLS-1$
					JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE
				);
				if (result == JOptionPane.CANCEL_OPTION) {
					cancelled = true;
					LOGGER.severe("El usuario cancelo la instalacion del certificado SSL para el socket: " + e); //$NON-NLS-1$
				}
			}
		}
		while (!installed && !cancelled);
	}
	
	
	/*TODO: MODO viejo no compatible on nuevas versiones de chrome*/
	/**
	 * Configura el protocolo "afirma" en Chrome para todos los usuarios de Windows.
	 * @param window Consola de salida.
	 * @param installing Indica si se debe configurar ({@code true}) o desconfigurar
	 * ({@code false}) el protocolo "afirma" en Chrome.
	 */
	/*private static void configureChrome(final Console window, boolean installing) {

		if(System.getProperty("os.name") != null && System.getProperty("os.name").contains("XP")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//TODO: asumo que si el lenguaje de la jvm es español entonces la instalacion
			//del SO es en español
			if ((Locale.getDefault()).toString().contains("es_")){				
				CHROME_CONFIG_FILE = "Configuración local/Datos de programa/Google/Chrome/User Data/Local State"; //$NON-NLS-1$
			}	
			
		}
		
		final File usersDir = new File(System.getProperty("user.home")).getParentFile(); //$NON-NLS-1$
		for (final File userDir : usersDir.listFiles()) {
			if (userDir.isDirectory()) {
				try {
					final File chromeConfigFile = new File(userDir, CHROME_CONFIG_FILE);
					if (chromeConfigFile.isFile() && chromeConfigFile.canWrite()) {
						String config;
						try (final FileInputStream fis = new FileInputStream(chromeConfigFile)) {
							config = new String(AOUtil.getDataFromInputStream(fis), StandardCharsets.UTF_8);
						}

						config = config.replace("\"afirma\":false,", ""); //$NON-NLS-1$ //$NON-NLS-2$
						if (installing) {
							config = config.replace("\"protocol_handler\":{\"excluded_schemes\":{", //$NON-NLS-1$
									"\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false,"); //$NON-NLS-1$
						}
						try (final FileOutputStream fos = new FileOutputStream(chromeConfigFile)) {
							fos.write(config.getBytes(StandardCharsets.UTF_8));
						}
					}

				}
				catch (final Exception e) {
					if (window != null) {
						window.print(String.format(Messages.getString("ConfiguratorWindows.15"), userDir.getName())); //$NON-NLS-1$
					}
					LOGGER.warning("No se pudo configurar Chrome para el usuario " + userDir + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}*/
	
	/**
	 * Configura el protocolo "afirma" en Chrome para todos los usuarios de Windows.
	 * @param window Consola de salida.
	 * @param installing Indica si se debe configurar ({@code true}) o desconfigurar
	 * ({@code false}) el protocolo "afirma" en Chrome.
	 */
	private static void configureChrome(final Console window, final boolean installing) {

		if (window != null && installing) {
			window.print(Messages.getString("ConfiguratorWindows.16")); //$NON-NLS-1$
		}

		final File usersDir = new File(System.getProperty("user.home")).getParentFile(); //$NON-NLS-1$
		for (final File userDir : usersDir.listFiles()) {
			if (userDir.isDirectory() && !DEFAULT_WINDOWS_USER_NAME.equalsIgnoreCase(userDir.getName())) {
				try {
					final File chromeConfigFileV56OrLower = new File(userDir, CHROME_V56_OR_LOWER_CONFIG_FILE);
					if (chromeConfigFileV56OrLower.isFile() && chromeConfigFileV56OrLower.canWrite()) {
						String config;
						try (final FileInputStream fis = new FileInputStream(chromeConfigFileV56OrLower)) {
							config = new String(AOUtil.getDataFromInputStream(fis), StandardCharsets.UTF_8);
						}
						config = config.replace("\"afirma\":false,", ""); //$NON-NLS-1$ //$NON-NLS-2$
						config = config.replace("\"afirma\":false", ""); //$NON-NLS-1$ //$NON-NLS-2$

						if (installing) {
							config = config.replace("\"protocol_handler\":{\"excluded_schemes\":{", //$NON-NLS-1$
									"\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false,"); //$NON-NLS-1$
							config = config.replace("\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false,}", //$NON-NLS-1$
									"\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false}"); //$NON-NLS-1$
							// En caso de que Google Chrome este recien instalado no encontrara cadena a reemplazar,
							// por lo que directamente insertaremos la cadena nosotros en el lugar correspondiente
							if(!config.contains("excluded_schemes")) { //$NON-NLS-1$
								config = config.replaceAll("last_active_profiles([^,]*),", //$NON-NLS-1$
										"last_active_profiles$1,\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false}},"); //$NON-NLS-1$
							}
						}
						else {
							// Elimina la sintaxis que define los protocolos de confianza si no existe ninguno.
							config = config.replace("\"protocol_handler\":{\"excluded_schemes\":{}},", ""); //$NON-NLS-1$ //$NON-NLS-2$
						}
						try (final FileOutputStream fos = new FileOutputStream(chromeConfigFileV56OrLower)) {
							fos.write(config.getBytes(StandardCharsets.UTF_8));
						}
					}
					final File chromeConfigFileV57OrHigher = new File(userDir, CHROME_V57_OR_HIGHER_CONFIG_FILE);
					if (chromeConfigFileV57OrHigher.isFile() && chromeConfigFileV57OrHigher.canWrite()) {
						String config;
						try (final FileInputStream fis = new FileInputStream(chromeConfigFileV57OrHigher)) {
							config = new String(AOUtil.getDataFromInputStream(fis), StandardCharsets.UTF_8);
						}
						config = config.replace("\"afirma\":false,", ""); //$NON-NLS-1$ //$NON-NLS-2$
						config = config.replace("\"afirma\":false", ""); //$NON-NLS-1$ //$NON-NLS-2$
						if (installing) {
							config = config.replace("\"protocol_handler\":{\"excluded_schemes\":{", //$NON-NLS-1$
									"\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false,"); //$NON-NLS-1$
							config = config.replace("\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false,}", //$NON-NLS-1$
									"\"protocol_handler\":{\"excluded_schemes\":{\"afirma\":false}"); //$NON-NLS-1$
							// En caso de que Google Chrome este recien instalado no encontrara cadena a reemplazar,
							// pero si se ha insertado en el fichero Local State del paso anterior se configurara automaticamente
							// en el Default/Preferences cuando se invoque el protocolo afirma
						}
						try (final FileOutputStream fos = new FileOutputStream(chromeConfigFileV57OrHigher)) {
							fos.write(config.getBytes(StandardCharsets.UTF_8));
						}
					}
				}
				catch (final Exception e) {
					if (window != null) {
						window.print(String.format(Messages.getString("ConfiguratorWindows.15"), userDir.getName())); //$NON-NLS-1$
					}
					LOGGER.warning("No se pudo configurar Chrome para el usuario " + userDir + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
	}
	
}
