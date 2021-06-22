package es.gob.afirma.standalone.configurator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.security.KeyStoreException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

import es.gob.afirma.core.misc.BoundedBufferedReader;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.keystores.mozilla.MozillaKeyStoreUtilities;
import es.gob.afirma.keystores.mozilla.MozillaKeyStoreUtilitiesOsX;

/** Configurador para instalar un certificado SSL de confianza en Mozilla NSS.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
final class ConfiguratorFirefox {

	static final class MozillaProfileNotFoundException extends Exception {
		private static final long serialVersionUID = -2329746920496661591L;
	}

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final String FILE_AUTOFIRMA_CERTIFICATE = "FirmaDigital_ROOT.cer"; //$NON-NLS-1$
	static final String DIR_CERTUTIL = "certutil"; //$NON-NLS-1$
	private static final String LINUX_UNINSTALLSCRIPT_NAME = "uninstall.sh"; //$NON-NLS-1$
	private static final String LINUX_SCRIPT_NAME = "script.sh"; //$NON-NLS-1$
	private static final String LINUX_MOZILLA_PATH = "/.mozilla/firefox/profiles.ini";//$NON-NLS-1$
	private static final String LINUX_CHROME_PATH = "/.pki/nssdb";//$NON-NLS-1$
	private static final String LINUX_CHROMIUM_PREFS_PATH = "/.config/chromium/Local State";//$NON-NLS-1$
	private static final String LINUX_CHROME_PREFS_PATH = "/.config/google-chrome/Local State";//$NON-NLS-1$
	private static final String MACOSX_MOZILLA_PATH = "/Library/Application Support/firefox/profiles.ini";//$NON-NLS-1$
	private static String WINDOWS_MOZILLA_PATH = "\\AppData\\Roaming\\Mozilla\\Firefox\\profiles.ini"; //$NON-NLS-1$
	static final String CERTUTIL_EXE;
	private static final String FILE_CERTUTIL;
	private static final String RESOURCE_BASE;

	private static String USERS_WINDOWS_PATH = "C:\\Users\\"; //$NON-NLS-1$

	static {
		if (isWindowsXP()) {
			if ((Locale.getDefault()).toString().contains("es_")){				
				WINDOWS_MOZILLA_PATH = "\\Datos de programa\\Mozilla\\Firefox\\profiles.ini"; //$NON-NLS-1$
				
			}else{//es una instalacion de SO en ingles
				WINDOWS_MOZILLA_PATH = "\\Application Data\\Mozilla\\Firefox\\profiles.ini"; //$NON-NLS-1$
				
			}
			USERS_WINDOWS_PATH = "C:\\Documents and Settings\\"; //$NON-NLS-1$
		}else {
			WINDOWS_MOZILLA_PATH = "\\AppData\\Roaming\\Mozilla\\Firefox\\profiles.ini";
		//	if ((Locale.getDefault()).toString().contains("es_")){				
		//		USERS_WINDOWS_PATH = "C:\\Usuarios\\";
		//	}else{//es una instalacion de SO en ingles
				//USERS_WINDOWS_PATH = "C:\\Users\\";
				
				//puede tambien ser un hibrido de ambos idiomas
				if(new File("C:\\Usuarios\\").exists()) {
					USERS_WINDOWS_PATH = "C:\\Usuarios\\";
				}else{
					USERS_WINDOWS_PATH = "C:\\Users\\";	
				}
		//	}
			
		}
		LOGGER.info("Version de Windows detectada: " + System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
		LOGGER.info("PATH directorio mozilla: " + WINDOWS_MOZILLA_PATH); //$NON-NLS-1$ //$NON-NLS-2$
		LOGGER.info("PATH usuario: " + USERS_WINDOWS_PATH); //$NON-NLS-1$ //$NON-NLS-2$
		LOGGER.info("PATH usuario home: " + System.getProperty("user.home"));
		
		switch(Platform.getOS()) {
		case WINDOWS:
			CERTUTIL_EXE = "certutil.exe"; //$NON-NLS-1$
			FILE_CERTUTIL = "certutil.windows.zip"; //$NON-NLS-1$
			RESOURCE_BASE = "/windows/"; //$NON-NLS-1$
			break;
		case MACOSX:
			CERTUTIL_EXE = "certutil"; //$NON-NLS-1$
			FILE_CERTUTIL = "certutil.osx.zip"; //$NON-NLS-1$
			RESOURCE_BASE = "/osx/"; //$NON-NLS-1$
			break;
		case LINUX:
			CERTUTIL_EXE = "certutil"; //$NON-NLS-1$
			FILE_CERTUTIL = "certutil.linux.zip"; //$NON-NLS-1$
			RESOURCE_BASE = "/linux/"; //$NON-NLS-1$
			break;
		default:
			throw new IllegalStateException(
					"Sistema operativo no soportado: " + Platform.getOS() //$NON-NLS-1$
					);
		}
	}

	private ConfiguratorFirefox() {
		// No instanciable
	}

	static void installRootCAMozillaKeyStore(final File targetDir) throws MozillaProfileNotFoundException,
	                                                                      IOException {
		final ArrayList<File> firefoxProfilesDir = getFirefoxProfilesDir();
		
		if (firefoxProfilesDir == null || firefoxProfilesDir.isEmpty()) {
			
			throw new MozillaProfileNotFoundException();
		}
		if(!Platform.getOS().equals(Platform.OS.LINUX)) {
			copyConfigurationFiles(targetDir);
		}
		for (final File firefoxDir : firefoxProfilesDir) {
			ConfiguratorFirefox.importCARootOnFirefoxKeyStore(
				targetDir,
				firefoxDir
			);
		}

	}

	/** Genera el script que elimina el warning al ejecutar AutoFirma desde Chrome para LINUX.
	 * En linux genera el script que hay que ejecutar para realizar la instalaci&oacute;n pero no lo ejecuta, de eso se encarga el instalador Debian.
	 * @param targetDir Directorio de instalaci&oacute;n del sistema
	 * @param userDir Directorio de usuario dentro del sistema operativo.
	 * @param browserPath Directorio de configuraci&oacute;n de Chromium o Google Chrome.
	 *  <ul>
	 *   <li>En LINUX contiene el contenido del script a ejecutar.</li>
	 * </ul> */
	private static void createScriptsRemoveExecutionWarningInChrome(final File targetDir, final String userDir, final String browserPath) {
		final String[] commandInstall = new String[] {
				"sed", //$NON-NLS-1$
				"s/\\\"protocol_handler\\\":{\\\"excluded_schemes\\\":{/\\\"protocol_handler\\\":{\\\"excluded_schemes\\\":{\\\"afirma\\\":false,/g", //$NON-NLS-1$
				escapePath(userDir + browserPath),
				">", //$NON-NLS-1$
				escapePath(userDir + browserPath) + "1", //$NON-NLS-1$
		};

		final String[] commandUninstall = new String[] {
				"sed", //$NON-NLS-1$
				"s/\\\"afirma\\\":false,//g", //$NON-NLS-1$
				escapePath(userDir + browserPath),
				">", //$NON-NLS-1$
				escapePath(userDir + browserPath) + "1", //$NON-NLS-1$
		};

		//Se reemplaza el fichero generado por el original
		final String[] commandCopy = new String[] {
				"\\cp", //$NON-NLS-1$
				escapePath(userDir + browserPath) + "1", //$NON-NLS-1$
				escapePath(userDir + browserPath),
		};

		// Generamos el script de instalacion y desistalacion
		try {

			final StringBuilder sb = new StringBuilder();
			for (final String s : commandInstall) {
				sb.append(s);
				sb.append(' ');
			}

			final StringBuilder uninstall = new StringBuilder();
			for (final String s : commandUninstall) {
				uninstall.append(s);
				uninstall.append(' ');
			}
			uninstall.append("\n"); //$NON-NLS-1$
			sb.append("\n"); //$NON-NLS-1$

			for (final String s : commandCopy) {
				sb.append(s);
				sb.append(' ');
			}
			for (final String s : commandCopy) {
				uninstall.append(s);
				uninstall.append(' ');
			}
			String path = null;
			String uninstallPath = null;
			sb.append("\n"); //$NON-NLS-1$
			uninstall.append("\n"); //$NON-NLS-1$

			// Obtenemos la ruta de los scripts
			path = new File(targetDir, LINUX_SCRIPT_NAME).getAbsolutePath();
			uninstallPath = new File(targetDir, LINUX_UNINSTALLSCRIPT_NAME).getAbsolutePath();
			final File installScript = new File(path);
			final File uninstallScript = new File(uninstallPath);



			try (
					final FileOutputStream fout = new FileOutputStream(installScript, true);
					final FileOutputStream foutUninstall = new FileOutputStream(
							uninstallScript, true
							);
					) {
				fout.write(sb.toString().getBytes());
				foutUninstall.write(uninstall.toString().getBytes());
			}
		}
		catch (final Exception e) {
			LOGGER.severe(
					"Excepcion en la creacion del script linux para la modificacion del fichero de protocolos de Google Chrome: " + e //$NON-NLS-1$
					);
		}

	}

	/** Genera el script que elimina el warning al ejecutar AutoFirma desde Chrome para LINUX.
	 * En linux genera el script que hay que ejecutar para realizar la instalaci&oacute;n pero no lo ejecuta, de eso se encarga el instalador Debian.
	 * @param targetDir Directorio de instalaci&oacute;n del sistema
	 * @param command Usado para sacar los directorios de usuario dentro del sistema operativo.
	 *  <ul>
	 * <li>En LINUX contiene el contenido del script a ejecutar.</li>
	 * </ul>
	 */
	static void removeAppExecutionWarningInChrome(final File targetDir, final String[] command) {

		// sacamos el listado de usuarios de la aplicacion
		final List<String> usersDirs = getSystemUsersHomes(command);

		for ( final String userDir : usersDirs) {
			// Montamos el script de instalacion y desinstalacion que
			// incluya el protocolo "afirma" en el fichero Local State
			if ( Platform.OS.LINUX.equals(Platform.getOS()) ) {
				final File fileChrome = new File(escapePath(userDir) + LINUX_CHROME_PREFS_PATH);
				final File fileChromium = new File(escapePath(userDir) + LINUX_CHROMIUM_PREFS_PATH);
				if( fileChrome.isFile() ) {
					createScriptsRemoveExecutionWarningInChrome(targetDir, userDir, LINUX_CHROME_PREFS_PATH);
				}
				if ( fileChromium.isFile() ) {
					createScriptsRemoveExecutionWarningInChrome(targetDir, userDir, LINUX_CHROMIUM_PREFS_PATH);
				}
			}
		}
	}

	/**
	 * Genera el script de instalaci&oacute; del certificado en Chrome para LINUX.
	 * En linux genera el script que hay que ejecutar para realizar la instalaci&oacute;n pero no lo ejecuta, de eso se encarga el instalador Debian.
	 * @param targetDir Directorio de instalaci&oacute;n del sistema
	 * @param command Usado para sacar los directorios de usuario dentro del sistema operativo.
	 *  <ul>
	 * <li>En LINUX contiene el contenido del script a ejecutar.</li>
	 * </ul>
	 * @throws IOException Cuando ocurre un error en el tratamiento de datos.
	 */
	static void installRootCAChromeKeyStore(final File targetDir, final String[] command )
			throws IOException {

		// sacamos el listado de usuarios de la aplicacion
		final List<String> usersDirs = getSystemUsersHomes(command);

		for ( final String userDir : usersDirs) {
			final File file = new File(escapePath(userDir) + LINUX_CHROME_PATH);
			if( file.isDirectory()) {
				//Usamos el comando para importar en Chrome en Linux
				if ( Platform.OS.LINUX.equals(Platform.getOS()) ) {
					final String[] certutilCommands = new String[] {
							CERTUTIL_EXE,
							"-d", //$NON-NLS-1$
							"sql:" + escapePath(userDir) + LINUX_CHROME_PATH, //$NON-NLS-1$
							"-A", //$NON-NLS-1$
							"-n", //$NON-NLS-1$
							"\"" + ConfiguratorUtil.CERT_ALIAS + "\"", //$NON-NLS-1$ //$NON-NLS-2$
							"-i", //$NON-NLS-1$
							escapePath(new File(targetDir, FILE_AUTOFIRMA_CERTIFICATE).getAbsolutePath()),
							"-t", //$NON-NLS-1$
							"\"TCP,TCP,TCP\"" //$NON-NLS-1$
					};
					execCommandLineCertUtil(certutilCommands, true);
				}
			}
		}
	}

	/**
	 * Genera el script de instalaci&oacute; del certificado en firefox para MacOSX y LINUX.
	 * En linux genera el script que hay que ejecutar para realizar la instalaci&oacute;n pero no lo ejecuta, de eso se encarga el instalador Debian.
	 * En MacOSX el script se ejecuta a la vuelta de este m&eacute;todo.
	 * @param targetDir Directorio de instalaci&oacute;n del sistema
	 * @param command Usado para sacar los directorios de usuario dentro del sistema operativo.
	 *  <ul>
	 * <li>En LINUX contiene el contenido del script a ejecutar.</li>
	 * <li>En MacOSX contiene la ruta del script a ejecutar.</li>
	 * </ul>
	 * @throws MozillaProfileNotFoundException No se ha encontrado el directorio de perfiles de Mozilla.
	 * @throws IOException Cuando ocurre un error en el tratamiento de datos.
	 */
	static void installRootCAMozillaKeyStore(final File targetDir, final String[] command )
			throws MozillaProfileNotFoundException, IOException {

		// sacamos el listado de usuarios de la aplicacion
		final List<String> usersDirs = getSystemUsersHomes(command);

		// dados los usuarios sacamos el directorio de perfiles de mozilla en caso de que lo tengan
		final List <File> mozillaUsersProfilesPath = getMozillaUsersProfilesPath(usersDirs);
		// para cada usuario tenemos sus distintos directorios de perfiles
		final Set <File> profiles = getProfiles(mozillaUsersProfilesPath);
		if (profiles.isEmpty()){
			throw new MozillaProfileNotFoundException();
		}
		if(!Platform.getOS().equals(Platform.OS.LINUX)) {
			copyConfigurationFiles(targetDir);
		}

		ConfiguratorFirefox.importCARootOnFirefoxKeyStore(targetDir, profiles);

	}


	static void uninstallRootCAMozillaKeyStore(final File targetDir) {

		try {
			if(!Platform.getOS().equals(Platform.OS.LINUX)) {
				copyConfigurationFiles(targetDir);
			}
		}
		catch (final Exception e) {
			LOGGER.warning(
					"No se pudo descomprimir certutil para la desinstalacion del certificado SSL raiz del almacen de Mozilla Firefox. Se aborta la operacion: " + e //$NON-NLS-1$
					);
		}

		try {
			ConfiguratorFirefox.executeCertUtilToDelete(targetDir);
		}
		catch (final Exception e) {
			LOGGER.warning("No se pudo desinstalar el certificado SSL raiz del almacen de Mozilla Firefox: " + e); //$NON-NLS-1$
		}

		removeConfigurationFiles(targetDir);
	}


	static void generateUninstallScriptMac(final File targetDir) throws MozillaProfileNotFoundException, IOException {

		final StringBuilder sb = new StringBuilder(ConfiguratorMacOSX.OSX_GET_USERS_COMMAND);
		final String path = targetDir + ConfiguratorMacOSX.GET_USER_SCRIPT;
		try {
			ConfiguratorFirefox.writeScriptFile(path, sb, true);
		} catch (final IOException e) {
			LOGGER.severe(" Ha ocurrido un error : " + e); //$NON-NLS-1$
		}
		addExexPermissionsToAllFilesOnDirectory(ConfiguratorUtil.getApplicationDirectory());
		// sacamos el listado de usuarios de la aplicacion
		final List<String> usersDirs = getSystemUsersHomes(new String[]{path});
		// dados los usuarios sacamos el directorio de perfiles de mozilla en caso de que lo tengan
		final List <File> mozillaUsersProfilesPath = getMozillaUsersProfilesPath(usersDirs);
		// para cada usuario tenemos sus distintos directorios de perfiles
		final Set <File> profiles = getProfiles(mozillaUsersProfilesPath);
		if (profiles.isEmpty()){
			throw new MozillaProfileNotFoundException();
		}

		final File certutilFile = new File(targetDir, DIR_CERTUTIL + File.separator + CERTUTIL_EXE);

		if (!certutilFile.exists() || !certutilFile.isFile() || !certutilFile.canExecute()) {
			throw new IOException("No se encuentra o no se puede leer el ejecutable para la instalacion en Firefox"); //$NON-NLS-1$
		}

		for (final File profile : profiles) {
			if (!profile.isDirectory()) {
				continue;
			}

			final String scriptUninstall = "max=$(" //$NON-NLS-1$
			+ escapePath(certutilFile.getAbsolutePath())
			+ " -L -d " //$NON-NLS-1$
			+ escapePath(profile.getAbsolutePath())
			+ " | grep AutoFirma | wc -l);for ((i=0; i<$max; i++));do " //$NON-NLS-1$
			+ escapePath(certutilFile.getAbsolutePath())
			+ " -D -d " //$NON-NLS-1$
			+ escapePath(profile.getAbsolutePath())
			+ " -n \"SocketAutoFirma\";done"; //$NON-NLS-1$
			final String[] certutilCommands = scriptUninstall.split(" "); //$NON-NLS-1$

			execCommandLineCertUtil(certutilCommands, true);

		}

	}

	/** Elimina la carpeta certutil generada durante el proceso de instalaci&oacute;n.
	 * @param targetDir Directorio en el que se copia certUtil. */
	static void removeConfigurationFiles(final File targetDir) {
		if (!targetDir.exists()) {
			return;
		}
		ConfiguratorFirefox.deleteConfigDir(targetDir);
	}

	static void addExexPermissionsToFile(final File f) {
		final Set<PosixFilePermission> perms = new HashSet<>();
		perms.add(PosixFilePermission.OWNER_EXECUTE);
		perms.add(PosixFilePermission.GROUP_EXECUTE);
		perms.add(PosixFilePermission.OTHERS_EXECUTE);
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.GROUP_WRITE);
		perms.add(PosixFilePermission.OTHERS_WRITE);
		try {
			Files.setPosixFilePermissions(
				Paths.get(f.getAbsolutePath()),
				perms
			);
		}
		catch (final Exception e) {
			LOGGER.warning(
				"No se ha podido dar permiso de ejecucion a '" + f.getAbsolutePath() + "': " + e//$NON-NLS-1$ //$NON-NLS-2$
			);
		}
	}

	/**
	 * Da permisos de ejecuci&oacute;n a todos los ficheros de un directorio dado.
	 * @param dir Directorio al que dar permiso.
	 */
	public static void addExexPermissionsToAllFilesOnDirectory(final File dir) {
		if (!Platform.OS.WINDOWS.equals(Platform.getOS())) {
			for (final File fileEntry : dir.listFiles()) {
				addExexPermissionsToFile(fileEntry);
			}
		}
	}

	private static String escapePath(final String path) {
		if (path == null) {
			throw new IllegalArgumentException(
				"La ruta a 'escapar' no puede ser nula" //$NON-NLS-1$
			);
		}
		if (Platform.OS.WINDOWS.equals(Platform.getOS())) {
			if (path.contains(" ")) { //$NON-NLS-1$
				return "\"" + path + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			}
			return path;
		}
		return path.replace(" ", "\\ "); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Copiamos el certificado en un directorio para que Mozilla CertUtil pueda usarlo.
	 * @param certUtilAbsolutePath Ruta del ejecutable CertUtil.
	 * @return certutilFile Fichero ejecutable CertUtil.
	 * @throws IOException Se lanza cuando hay un problema con el fichero CertUtil. */
	public static File InstallCerFile(final String certUtilAbsolutePath) throws IOException {

		//En linux se trabaja con la dependencia del certutil
		if (!Platform.OS.LINUX.equals(Platform.getOS()) ) {
			final File certutilFile = new File(certUtilAbsolutePath);

			if (!certutilFile.exists() || !certutilFile.isFile()) {
				throw new IOException("No se encuentra el ejecutable CertUtil para la instalacion en Firefox" //$NON-NLS-1$
						);
			}

			if (!certutilFile.canExecute()) {
				addExexPermissionsToAllFilesOnDirectory(certutilFile.getParentFile());
			}

			if (!certutilFile.canExecute()) {
				throw new IOException("No hay permisos de ejecucion para Mozilla CertUtil" //$NON-NLS-1$
						);
			}

			return certutilFile;
		}
		return null;
	}

	/** Ejecuta la utilidad Mozilla CertUtil para la instalaci&oacute;n del certificado ra&iacute;z de  confianza en Firefox.
	 * @param certUtilAbsolutePath Directorio en el que se encuentra certutil.
	 * @param targetDir Directorio en el que se encuentra el certificado a importar.
	 * @param profilesDir Listado de directorios de perfiles de usuario de Mozilla Firefox.
	 * @throws IOException Cuando ocurre un error en el tratamiento de datos.
	 * @throws GeneralSecurityException Cuando ocurre un error en la inserci&oacute;n del certificado en el KeyStore.
	 */
	private static void executeCertUtilToImport(final String certUtilAbsolutePath,
			                                    final File targetDir,
			                                    final Set<File> profilesDir) throws IOException,
	                                                                                GeneralSecurityException {
		final File certutilFile = InstallCerFile(certUtilAbsolutePath);
		String certUtilExec = null;
		// Obtenemos todos los directorios de perfil de Firefox del usuario
		boolean error = false;

		// exportamos el PATH y LD_LIBRARY_PATH en MACOSX
		if ( Platform.OS.LINUX.equals(Platform.getOS()) ) {
			certUtilExec = CERTUTIL_EXE;
		}
		else {
			certUtilExec = escapePath(certutilFile.getAbsolutePath());

			if ( Platform.OS.MACOSX.equals(Platform.getOS()) ) {
				writeScriptFile(ConfiguratorMacOSX.mac_script_path, new StringBuilder(ConfiguratorMacOSX.EXPORT_PATH + certutilFile.getAbsolutePath().substring(0,certutilFile.getAbsolutePath().lastIndexOf(File.separator) )), true);
				writeScriptFile(ConfiguratorMacOSX.mac_script_path, new StringBuilder(ConfiguratorMacOSX.EXPORT_LIBRARY_LD + certutilFile.getAbsolutePath().substring(0,certutilFile.getAbsolutePath().lastIndexOf(File.separator) )), true);
			}
		}
		for (final File profileDir : profilesDir) {
			if (!profileDir.isDirectory()) {
				continue;
			}
/*certutil supports two types of databases: the legacy security databases (cert8.db, key3.db, and secmod.db) and new SQLite databases (cert9.db, key4.db, and pkcs11.txt).

NSS recognizes the following prefixes:
 sql: requests the newer database
 dbm: requests the legacy database
 Ej:
 To Install in sqlite3 (cert9) DB: certutil.exe -A -t "<trust_type>" -i "<cert_file>" -d "sql:<profile_path>"
 To Install in default Berkeley (cert8) DB: certutil.exe -A -t "<trust_type>" -i "<cert_file>" -d "<profile_path>"
 
 En este modo viejo tambien note que se acepta no poner el prefixo y anda igual
 solo que con todas las versiones de firefox > 57 se requiere que para que se instale el
 certificado del socket automaticamente en el repositorio de firefox, se debe realizar la primera
 carga del firmador desde chrome y teniendo al mismo tiempo al firefox no encendido
 sino hay que ver el certificado instalado FirmaDigital_ROOT.cer de chrome o desde la carpeta temp de windows
 e importarlo a firefox.
 Estoy pensando que si se instala firefox desde cero anda con el prefix sql pero si es un firefox viejo que se fue
 actualizando falla, si borro la bd de cert8.db y su key3.db el certutil no arranca porque
 justo su propio cert de seguridad estaba ahi.SOLUCIONADO
 
 https://support.mozilla.org/pl/questions/1207165
 https://support.mozilla.org/en-US/questions/1207165
 
 */
			// Si en el directorio del perfil existe el fichero pkcs11.txt entonces se trata
			// de un almacen de certificados SQL
			final boolean sqlDb = new File(profileDir, "pkcs11.txt").exists(); //$NON-NLS-1$
			//hay veces en las viejas versiones que no hay pkcs11.txt pero aun asi utiliza la base cert9
			//lo de abajo no anda, debe ser por ser un .db, en todo caso si hay error de ejecucion del comando
			//seria adecuado cuando hay error probar con ejecutar el comando simple
			//en algunas maquinas el certutil en caso de que no eixsta el cert8 lo RECREA sin problemas 
			//boolean sqlDb2 = new File(profileDir, "cert8.db").exists();
						
			
			final String[] certutilCommands = new String[] {
					certUtilExec,
					"-A", //$NON-NLS-1$
					"-d", //$NON-NLS-1$
					escapePath((sqlDb ? "sql:" : "") + profileDir.getAbsolutePath()), //$NON-NLS-1$ //$NON-NLS-2$
					"-i", //$NON-NLS-1$
					escapePath(new File(targetDir, FILE_AUTOFIRMA_CERTIFICATE).getAbsolutePath()), "-n", //$NON-NLS-1$
					"\"" + ConfiguratorUtil.CERT_ALIAS + "\"", //$NON-NLS-1$ //$NON-NLS-2$
					"-t", //$NON-NLS-1$
					"\"C,,\"" //$NON-NLS-1$
			};

			//En la ultima version de firefox 77.01 64 bits (quizas un poco antes) cuando se produce un 
			//could not authenticate to token NSS Certificate DB.: An I/O error occurred during security authorization.
			//porque se tienen dos db de certificados,y si bien el pkcs11.txt existe quizas en realidad se este usando la db vieja
			//https://www.systutorials.com/docs/linux/man/1-certutil/
			//fijandome en las db, la nueva representa al firefox del user actual, todabia no estoy seguro de este
			//comportamiento por lo que capturo el posible error y dejo que continue intentando inserta el certificado
			//en los demas perfiles, aunque puedo intentar ejecutar el comando en el modo viejo
			try {
				error = execCommandLineCertUtil(certutilCommands, false);
				if (error){
					//pruebo ejecutar de la forma vieja
					boolean sqlDb2 = true;
					final String[] certutilCommands2 = new String[] {
							certUtilExec,
							"-A", //$NON-NLS-1$
							"-d", //$NON-NLS-1$
							escapePath((sqlDb2 ? "sql:" : "") + profileDir.getAbsolutePath()), //$NON-NLS-1$ //$NON-NLS-2$
							"-i", //$NON-NLS-1$
							escapePath(new File(targetDir, FILE_AUTOFIRMA_CERTIFICATE).getAbsolutePath()), "-n", //$NON-NLS-1$
							"\"" + ConfiguratorUtil.CERT_ALIAS + "\"", //$NON-NLS-1$ //$NON-NLS-2$
							"-t", //$NON-NLS-1$
							"\"C,,\"" //$NON-NLS-1$
					};
					error = execCommandLineCertUtil(certutilCommands, false);
					
				}
			}catch (Exception e) {
				LOGGER.severe("Error al intentar agregar el certificado en firefox: "+profileDir.getAbsolutePath() +" -- "+ e);
			}
			
		}

		if (error) {
			/*throw new KeyStoreException(
				"Error en la instalacion del certificado de CA en alguno de los perfiles de usuario " //$NON-NLS-1$
					+ "de Firefox. Es posible que la aplicacion funcione en su propio perfil. Si desea que la aplicacion se " //$NON-NLS-1$
					+ "ejecute correctamente en todos los perfiles, desinstalela y vuelvala a instalar." //$NON-NLS-1$
			);*/
			throw new KeyStoreException(
					"Error en la instalacion del certificado de CA en alguno de los perfiles de usuario ");
		}

	}

	/** Ejecuta Mozilla CertUtil como comando del sistema.
	 * @param command Comando a ejecutar, con el nombre de comando y sus par&aacute;metros separados en un array.
	 * @return <code>true</code> si la ejecuci&oacute;n de CertUtil termin&oacute; con error, <code>false</code> si se
	 *         ejecut&oacute; correctamente.
	 * @throws IOException Si no se pudo realizar la propia ejecuci&oacute;n. */
	private static boolean execCommandLineCertUtil(final String[] command, final boolean chromeImport)
			throws IOException {

		final StringBuilder sb = new StringBuilder();
		for (final String s : command) {
			sb.append(s);
			sb.append(' ');
		}

		if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			writeScriptFile(ConfiguratorMacOSX.mac_script_path, sb, true);
			return false;
		}
		else if (Platform.OS.LINUX.equals(Platform.getOS())) {
			// Generamos el script de instalacion y desistalacion
			try {
				// Montamos el script de desinstalacion, reutilizamos datos que
				// vienen en el script de instalacion
				final StringBuilder uninstall = new StringBuilder();
				String path = null;
				String uninstallPath = null;

				if(chromeImport) {
					//En Linux tambien se instala para todos los perfiles de
					// ususario del almacen de Chrome
					uninstall.append(command[0] + ' ');
					uninstall.append("-D" + ' '); //$NON-NLS-1$
					uninstall.append("-d" + ' ');//$NON-NLS-1$
					uninstall.append(command[2] + ' ');
					uninstall.append("-n" + ' ');//$NON-NLS-1$
					uninstall.append(command[5] + ' ');

					// tenemos en command[7] la ruta del fichero .crt, sacamos de
					// ahi la ruta del directorio de instalacion
					path = command[7].substring(0, command[7].lastIndexOf("/") + 1) + LINUX_SCRIPT_NAME; //$NON-NLS-1$
					uninstallPath = command[7].substring(0, command[7].lastIndexOf("/") + 1) + LINUX_UNINSTALLSCRIPT_NAME; //$NON-NLS-1$
				}
				else {
					uninstall.append(command[0] + ' ');
					uninstall.append("-D" + ' '); //$NON-NLS-1$
					uninstall.append("-d" + ' ');//$NON-NLS-1$
					uninstall.append(command[3] + ' ');
					uninstall.append("-n" + ' ');//$NON-NLS-1$
					uninstall.append(command[7] + ' ');

					// tenemos en command[5] la ruta del fichero .cer, sacamos de
					// ahi la ruta del directorio de instalacion
					path = command[5].substring(0, command[5].lastIndexOf("/") + 1) + LINUX_SCRIPT_NAME; //$NON-NLS-1$
					uninstallPath = command[5].substring(0, command[5].lastIndexOf("/") + 1) + LINUX_UNINSTALLSCRIPT_NAME; //$NON-NLS-1$
				}
				final File installScript = new File(path);
				final File uninstallScript = new File(uninstallPath);
				sb.append("\n"); //$NON-NLS-1$
				uninstall.append("\n"); //$NON-NLS-1$


				try (
						final FileOutputStream fout = new FileOutputStream(installScript, true);
						final FileOutputStream foutUninstall = new FileOutputStream(
								uninstallScript, true
								);
						) {
					fout.write(sb.toString().getBytes());
					foutUninstall.write(uninstall.toString().getBytes());
				}
				return false;
			}
			catch (final Exception e) {
				LOGGER.severe(
						"Excepcion en la creacion del script linux para la instalacion del certificado en el almacen de Firefox: " + e //$NON-NLS-1$
						);
				return true;
			}

		}
		else {
			LOGGER.info("Se ejecutara el siguiente comando:\n" + sb.toString()); //$NON-NLS-1$
			final Process process = new ProcessBuilder(command).start();
			// Cuando se instala correctamente no hay salida de ningun tipo, asi que se interpreta
			// cualquier salida como un error
			String line;
			try (
					final InputStream resIs = process.getInputStream();
					final BufferedReader resReader = new BoundedBufferedReader(
							new InputStreamReader(resIs),
							256, // Maximo 256 lineas de salida
							1024 // Maximo 1024 caracteres por linea
							);
					) {
				while ((line = resReader.readLine()) != null) {
					LOGGER.severe(line);
					return true;
				}
			}

			try (
					final InputStream errIs = process.getErrorStream();
					final BufferedReader errReader = new BoundedBufferedReader(
							new InputStreamReader(errIs),
							256, // Maximo 256 lineas de salida
							1024 // Maximo 1024 caracteres por linea
							);
					) {
				while ((line = errReader.readLine()) != null) {
					LOGGER.severe(line);
					return true;
				}
			}
		}

		return false;
	}

	/** Ejecuta un script en OS X.
	 * @param path Ruta donde se encuentra el <i>script</i>.
	 * @param administratorMode <code>true</code> el <i>script</i> se ejecuta como permisos de adminsitrador, <code>false</code> en caso contrario.
	 * @param delete <code>true</code> se borra el fichero despu&eacute;s de haberse ejecutado.
	 * @return El objeto que da como resultado el <i>script</i>, o <code>null</code> en caso contrario.
	 * @throws IOException Excepci&oacute;n lanzada en caso de ocurrir alg&uacute;n error en la ejecuci&oacute;n del <i>script</i>. */
	public static Object executeScriptMacOsx(final String path, final boolean administratorMode, final boolean delete) throws IOException {

		final ScriptEngine se = MozillaKeyStoreUtilitiesOsX.getAppleScriptEngine();
		if (se != null) {
			LOGGER.info("Path del script: " + path); //$NON-NLS-1$
			try {
				Object o;
				if (administratorMode){
					o = se.eval("do shell script \"" + path + "\" with administrator privileges"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					o = se.eval("do shell script \"" + path + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (delete){
					final File scriptInstall = new File(path);
					if (scriptInstall.exists()){
						scriptInstall.delete();
					}
				}
				return o;
			}
			catch (final ScriptException e) {
				throw new IOException("Error en la ejecucion del script via AppleScript: " + e, e); //$NON-NLS-1$
			}
		}
		LOGGER.severe(
			"No se ha podido instanciar el motor AppleScript" //$NON-NLS-1$
		);
		return null;
	}

	private static void importCARootOnFirefoxKeyStore (final File appConfigDir,
			                                           final Set<File> profilesDir) {
		boolean installed = false;
		boolean cancelled = false;
		do {
			try {
				// Usamos CertUtil para instalar el certificado en Firefox.
				String certutilExe = null;
				certutilExe = appConfigDir.getAbsolutePath() + File.separator + DIR_CERTUTIL + File.separator + CERTUTIL_EXE;

				executeCertUtilToImport(
					certutilExe,
					appConfigDir,
					profilesDir
				);
				installed = true;
			}
			catch (final Exception e) {
				LOGGER.warning(
						"No se pudo instalar la CA del certificado SSL para el socket en el almacen de Firefox: " + e //$NON-NLS-1$
						);
				final int result = JOptionPane.showConfirmDialog(
						null,
						Messages.getString("ConfiguratorWindows.10"), //$NON-NLS-1$
						Messages.getString("ConfiguratorWindows.1"), //$NON-NLS-1$
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE
						);
				if (result == JOptionPane.CANCEL_OPTION) {
					cancelled = true;
					LOGGER.severe(
							"El usuario cancelo la instalacion del certificado SSL para el socket en Firefox: " + e //$NON-NLS-1$
							);
				}
			}
		} while (!installed && !cancelled);

	}

	private static void importCARootOnFirefoxKeyStore(final File appConfigDir,
			                                          final File profilesDir) {

		// En Windows recibimos un unico directorio de perfil, lo convertimos a una estructura Set<File>
		final Set<File> profile = new HashSet<>(Arrays.asList(profilesDir.listFiles()));
		importCARootOnFirefoxKeyStore(appConfigDir, profile);
	}

	/** Ejecuta la aplicacion Mozilla CertUtil para eliminar el certificado de confianza ra&iacute;z
	 * SSL de Firefox.
	 * @param targetDir Directorio padre en el que se encuentra el directorio de certUtil.
	 * @throws IOException Cuando no se encuentra o puede leer alguno de los ficheros necesarios.
	 * @throws GeneralSecurityException Cuando no se puede ejecutar. */
	private static void executeCertUtilToDelete(final File targetDir) throws IOException, GeneralSecurityException {

		String certutilExe;
		// En linux se tiene certutil como dependencia
		if(!Platform.getOS().equals(Platform.OS.LINUX)) {
			final File certutilFile = new File(targetDir, DIR_CERTUTIL + File.separator + CERTUTIL_EXE);

			if (!certutilFile.exists() || !certutilFile.isFile() || !certutilFile.canExecute()) {
				throw new IOException("No se encuentra o no se puede leer el ejecutable para la instalacion en Firefox"); //$NON-NLS-1$
			}
			certutilExe = certutilFile.getAbsolutePath();
		}
		else {
			certutilExe = CERTUTIL_EXE;
		}

		//Se obtienen todos los usuarios para los que se va a desinstalar el certificado en Firefox
		final File file = new File(USERS_WINDOWS_PATH);
		final String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(final File current, final String name) {
		    return new File(current, name).isDirectory();
		  }
		});

		//Para Windows XP la ruta de los perfiles de Firefox y de los usuarios es diferente
/*		if(System.getProperty("os.name") != null && System.getProperty("os.name").contains("XP")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			WINDOWS_MOZILLA_PATH = "\\Application Data\\Mozilla\\Firefox\\profiles.ini"; //$NON-NLS-1$
			USERS_WINDOWS_PATH = "C:\\Documents and Settings\\"; //$NON-NLS-1$
		}*/
		// Obtenemos todos los directorios de perfil de Firefox del usuario

		boolean error = false;

		for(final String profile : directories) {
			LOGGER.info("Se comprueba la existencia del perfil de Firefox: " + USERS_WINDOWS_PATH + profile + WINDOWS_MOZILLA_PATH); //$NON-NLS-1$
			if(new File(USERS_WINDOWS_PATH + profile + WINDOWS_MOZILLA_PATH).exists()) {
				final File profilesDir = new File(
						MozillaKeyStoreUtilities.getMozillaUserProfileDirectoryWindows(
								USERS_WINDOWS_PATH + profile + WINDOWS_MOZILLA_PATH
								)
						).getParentFile();
				for (final File profileDir : profilesDir.listFiles()) {
					if (!profileDir.isDirectory()) {
						continue;
					}

					final String[] certutilCommands = new String[] {
							"\"" + certutilExe + "\"", //$NON-NLS-1$ //$NON-NLS-2$
							"-D", //$NON-NLS-1$
							"-d", //$NON-NLS-1$
							"\"" + profileDir.getAbsolutePath() + "\"", //$NON-NLS-1$ //$NON-NLS-2$
							"-n", //$NON-NLS-1$
							"\"" + ConfiguratorUtil.CERT_ALIAS + "\"", //$NON-NLS-1$ //$NON-NLS-2$
					};

					try {	
					
					 final Process process = new ProcessBuilder(certutilCommands).start();

					 LOGGER.info("Comando certutil ejecutado: " + Arrays.toString(certutilCommands)); //$NON-NLS-1$
					 // Cuando se instala correctamente no hay salida de ningun tipo, asi que se interpreta
					 // cualquier salida como un error
					 String line;
					 try (
							final InputStream resIs = process.getInputStream();
							final BufferedReader resReader = new BoundedBufferedReader(
									new InputStreamReader(resIs),
									256, // Maximo 256 lineas de salida
									1024 // Maximo 1024 caracteres por linea
									);
							) {
						while ((line = resReader.readLine()) != null) {
							error = true;
							LOGGER.severe(line);
						}
					 }

					 try (
							final InputStream errIs = process.getErrorStream();
							final BufferedReader errReader = new BoundedBufferedReader(
									new InputStreamReader(errIs),
									256, // Maximo 256 lineas de salida
									1024 // Maximo 1024 caracteres por linea
									);
							) {
						while ((line = errReader.readLine()) != null) {
							error = true;
							LOGGER.severe(line);
						}
					 }
					}catch (Exception e) {
						LOGGER.severe("Error al intentar agregar el certificado en firefox: "+profileDir.getAbsolutePath() +" -- "+ e);
					}
				}
			}
		}

		if (error) {
			throw new KeyStoreException("Error en el borrado del certificado de CA en alguno de los perfiles de usuario de Firefox"); //$NON-NLS-1$
		}
	}

	private static void deleteConfigDir(final File appConfigDir) {
		ConfiguratorUtil.deleteDir(new File(appConfigDir, DIR_CERTUTIL));
	}

	static void copyConfigurationFiles(final File appConfigDir) throws IOException {
		final File certutil = new File(ConfiguratorUtil.getApplicationDirectory() + File.separator + DIR_CERTUTIL);
		if (!certutil.exists()) {
			uncompressResource(RESOURCE_BASE + FILE_CERTUTIL, appConfigDir);
			addExexPermissionsToAllFilesOnDirectory(certutil);
		}
	}

	/** Descomprime un fichero ZIP de recurso al disco.
	 * @param resource Ruta del recurso ZIP.
	 * @param outDir Directorio local en el que descomprimir.
	 * @throws IOException Cuando ocurre un error al descomprimir.
	 **/
	private static void uncompressResource(final String resource, final File outDir) throws IOException {
		int n;
		ZipEntry entry;
		final byte[] buffer = new byte[1024];
		try (final ZipInputStream zipIs = new ZipInputStream(
				ConfiguratorFirefox.class.getResourceAsStream(resource));) {
			// en linux el funcionamiento es ligeramente diferente
			if (Platform.OS.LINUX == Platform.getOS()) {
				while ((entry = zipIs.getNextEntry()) != null) {
					new File(outDir, "certutil").mkdirs(); //$NON-NLS-1$
					try (
							final FileOutputStream outFis = new FileOutputStream(
									new File(
											outDir,
											entry.getName()
											)
									);
							) {
						while ((n = zipIs.read(buffer)) > 0) {
							outFis.write(buffer, 0, n);
						}
						outFis.flush();
					}

					zipIs.closeEntry();
				}
			}
			else {
				while ((entry = zipIs.getNextEntry()) != null) {
					if (entry.isDirectory()) {
						new File(outDir, entry.getName()).mkdirs();
					}
					else {
						try (
								final FileOutputStream outFis = new FileOutputStream(
										new File(
												outDir,
												entry.getName()
												)
										);
								) {
							while ((n = zipIs.read(buffer)) > 0) {
								outFis.write(buffer, 0, n);
							}
							outFis.flush();
						}
					}
					zipIs.closeEntry();
				}
			}

		}
	}

	private static ArrayList<File> getFirefoxProfilesDir() {
		final ArrayList<File> fileList = new ArrayList<>();
		//TODO:esto es necesario para poder instalar el certificado el CA del SSL de autofirma (que es Autofirma.ROOT) en el almacen de firefox
		//si el SO (windows xp,windows 7,etc)esta en español los nombres de las carpetas cambian
				
		//Para Windows XP la ruta de los perfiles de Firefox y de los usuarios es diferente
		
/*		if(System.getProperty("os.name") != null && System.getProperty("os.name").contains("XP")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//TODO: asumo que si el lenguaje de la jvm es español entonces la instalacion
			//del SO es en español
			if ((Locale.getDefault()).toString().contains("es_")){				
				WINDOWS_MOZILLA_PATH = "\\Datos de programa\\Mozilla\\Firefox\\profiles.ini"; //$NON-NLS-1$
			}else{//es una instalacion de SO en ingles
				WINDOWS_MOZILLA_PATH = "\\Application Data\\Mozilla\\Firefox\\profiles.ini"; //$NON-NLS-1$				
			}
			USERS_WINDOWS_PATH = "C:\\Documents and Settings\\"; //$NON-NLS-1$
			
		}else{//es un windows (u otro SO review windows pero por si lo es configuro la variable bien) con una instalacion en ingles (lo asumo porque no se si el lenguaje de instalacion varia para los demas idiomas)
			if ((Locale.getDefault()).toString().contains("es_")){
				USERS_WINDOWS_PATH = "C:\\Usuarios\\"; //$NON-NLS-1$
			}			
		}	*/
		
		//Se obtienen todos los usuarios para los que se va a instalar el certificado en Firefox
		final File file = new File(USERS_WINDOWS_PATH);
		final String[] directories = file.list(new FilenameFilter() {
		  @Override
		  public boolean accept(final File current, final String name) {
		    return new File(current, name).isDirectory();
		  }
		});
		
		try {
			for(final String profile : directories) {
				if(new File(USERS_WINDOWS_PATH + profile + WINDOWS_MOZILLA_PATH).exists()) {
					fileList.add(
							new File(
									MozillaKeyStoreUtilities.getMozillaUserProfileDirectoryWindows(
											USERS_WINDOWS_PATH + profile + WINDOWS_MOZILLA_PATH)
							).getParentFile());
					LOGGER.info("Se usa el perfil de Firefox: " + USERS_WINDOWS_PATH + profile + WINDOWS_MOZILLA_PATH); //$NON-NLS-1$
				}				
			}
		}
		catch (final Exception e) {
			LOGGER.warning("No se encontro el directorio de perfiles de Mozilla Firefox: " + e); //$NON-NLS-1$
		}
		return fileList;
	}

	/** Devuelve un listado con todos directorios personales de los usuarios del sistema ejecutando un script.
	 * Utilizado en Linux y MacOSX
	 * @param command Script para sacar los directorios de usuario dentro del sistema operativo.
	 * <ul>
	 *  <li>En Linux contiene el contenido del script a ejecutar.</li>
	 *  <li>En OS X contiene la ruta del script a ejecutar.</li>
	 * </ul>
	 * @return Listado con todos directorios personales de los usuarios del sistema. */
	private static List<String> getSystemUsersHomes(final String[] command) {
		if (Platform.OS.LINUX.equals(Platform.getOS())) {
			try {
				final Process process = new ProcessBuilder(command).start();

				String line;
				// arraylist con todos los directorios de usuario
				final List<String> usersDir = new ArrayList<>();
				try (
						final InputStream resIs = process.getInputStream();
						final BufferedReader resReader = new BoundedBufferedReader(
								new InputStreamReader(resIs),
								2048, // Maximo 256 lineas de salida (256 perfiles)
								2048 // Maximo 2048 caracteres por linea
								);
						) {
					while ((line = resReader.readLine()) != null) {
						usersDir.add(line);
					}
				}

				return usersDir;
			}
			catch (final Exception e) {
				LOGGER.info("Error al generar el listado de directorios de usuarios del sistema." + e); //$NON-NLS-1$
				return null;
			}
		}
		// MAC
		else if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			try {
				final Object o = ConfiguratorFirefox.executeScriptMacOsx(command[0],false,false);
				final List<String> usersDir = new ArrayList<>();
				String line;
				final String initLine = "dir: "; //$NON-NLS-1$
				try (
						final InputStream resIs = new ByteArrayInputStream(o.toString().getBytes());
						final BufferedReader resReader = new BoundedBufferedReader(
								new InputStreamReader(resIs),
								2048, // Maximo 2048 lineas de salida (256 perfiles)
								2048 // Maximo 2048 caracteres por linea
								);
						) {
					while ((line = resReader.readLine()) != null) {
						if (line.startsWith(initLine)){
							usersDir.add(
									line.substring(
											line.indexOf(initLine) + initLine.length()
											)
									);
						}
					}
				}

				return usersDir;

			}
			catch (final IOException e) {
				LOGGER.info("Error al generar el listado perfiles de Firefox del sistema: " + e); //$NON-NLS-1$
				return null;
			}
		}
		else {
			throw new IllegalArgumentException("Sistema operativo no soportado: " + Platform.getOS()); //$NON-NLS-1$
		}

	}

	/** Devuelve un listado con los directorios donde se encuentra el fichero <i>profiles.ini</i> de firefox en Linux y en OS X.
	 * @param users Listado de usuarios del sistema.
	 * @return Listado de directorios donde se encuentra el fichero <i>profiles.ini</i>. */
	private static List<File> getMozillaUsersProfilesPath(final List<String> users){
		String pathProfile = null;
		final List<File> path = new ArrayList<>();
		if (Platform.OS.LINUX.equals(Platform.getOS())) {
			pathProfile = LINUX_MOZILLA_PATH;
		}
		else if (Platform.OS.MACOSX.equals(Platform.getOS())) {
			pathProfile = MACOSX_MOZILLA_PATH;
		}
		else {
			throw new IllegalArgumentException("Sistema operativo no soportado: " + Platform.getOS()); //$NON-NLS-1$
		}
		for (final String usr: users){
			final File mozillaPath = new File(usr + pathProfile);
			// comprobamos que el fichero exista
			if (mozillaPath.exists() && mozillaPath.isFile()){
				path.add(mozillaPath);
				LOGGER.info("Ruta: " + mozillaPath ); //$NON-NLS-1$
			}
		}
		return path;
	}

	/** Devuelve un listado de directorios donde se encuentran los perfiles de usuario de firefox en Linux.
	 * @param profilesPath Listado de directorios que contienen un fichero <i>profiles.ini</i>.
	 * @return Listado de directorios donde se encuentran los perfiles de usuario de Firefox. */
	private static Set<File> getProfiles(final List<File> profilesPath){
		final String PATH = "Path="; //$NON-NLS-1$
		final Set<File> profile = new HashSet<>();
		for (final File path: profilesPath){
			String line;
			try (
				final InputStream resIs = new FileInputStream(path);
				final BufferedReader resReader = new BoundedBufferedReader(
					new InputStreamReader(resIs),
					256, // Maximo 256 lineas de salida (256 perfiles por "profiles.ini")
					2048 // Maximo 2048 caracteres por linea
				);
			) {
				while ((line = resReader.readLine()) != null) {
					if (line.startsWith(PATH)){
						final File file = new File(
							path.getAbsolutePath().substring(
								0, path.getAbsolutePath().lastIndexOf("/") + 1) + line.substring(PATH.length() //$NON-NLS-1$
							)
						);
						if (file.exists() && file.isDirectory()){
							profile.add(file);
						}
					}
				}
			}
			catch (final Exception e) {
				LOGGER.severe("Error al buscar los directorios de perfiles de Firefox: " + e); //$NON-NLS-1$
			}
		}
		return profile;
	}

	/**
	 * Comprueba si el sistema operativo es Windows XP.
	 * @return {@code true} si el sistema operativo es Windows XP y {@code false} en caso contrario.
	 */
	private static boolean isWindowsXP() {
		return System.getProperty("os.name") != null && System.getProperty("os.name").contains("XP"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
	
	/** Escribe un <i>script</i> en un fichero dado.
	 * @param path Ruta donde se escribir&aacute; el <i>script</i>.
	 * @param data Datos a escribir.
	 * @param append <code>true</code> permite contatenar el contenido del fichero con lo que se va a escribir. <code>false</code> el fichero se sobrescribe.
	 * @throws IOException Se produce cuando hay un error en la creaci&oacute;n del fichero. */
	public static void writeScriptFile(final String path, final StringBuilder data, final boolean append ) throws IOException{
		LOGGER.info("Se escribira en fichero el siguiente comando:\n" + data.toString()); //$NON-NLS-1$
		final File macScript = new File(path);
		data.append("\n"); //$NON-NLS-1$
		try (final FileOutputStream fout = new FileOutputStream(macScript, append);) {
			fout.write(data.toString().getBytes());
		}
	}
}
