package es.gob.afirma.standalone.configurator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

import java.lang.reflect.Method;
import es.gob.afirma.core.misc.Platform;
import java.net.URISyntaxException;

/*import javax.jnlp.ExtendedService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;*/

final class ConfiguratorUtil {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	static final String CERT_ALIAS = "SocketAutoFirma"; //$NON-NLS-1$

	private ConfiguratorUtil() {
		// No instanciable
	}

	/** Guarda datos en disco.
	 * @param data Datos a guardar.
	 * @param outDir Directorio local.
	 * @throws IOException Cuando ocurre un error durante el guardado. */
	/*static void installFileViejo(final byte[] data, final File outDir) throws IOException {
		ExtendedService es; 
		try { 
		    es = 
		(ExtendedService)ServiceManager.lookup("javax.jnlp.ExtendedService");
		} catch (UnavailableServiceException e) { es=null; }
		
		try {
			OutputStream configScriptOs;
			if(es!=null){
				configScriptOs = es.openFile(outDir).getOutputStream(true);
			}else{
				configScriptOs = new FileOutputStream(outDir);
			}
			final BufferedOutputStream bos = new BufferedOutputStream(configScriptOs);
			bos.write(data);
			bos.flush();
			bos.close();
		}catch(IOException e){
			throw e;
		}
	}*/
	
	/** Guarda datos en disco.
	 * @param data Datos a guardar.
	 * @param outFile Fichero de salida.
	 * @throws IOException Cuando ocurre un error durante el guardado. */
	static void installFile(final byte[] data, final File outFile) throws IOException {
		OutputStream os;
		if (isJnlpDeployment()) {
			os = selectFileToWrite(outFile);
		}
		else {
			os = new FileOutputStream(outFile);
		}
		try (final BufferedOutputStream bos = new BufferedOutputStream(os)) {
			bos.write(data);
		}
	}
	
	/**
	 * Selecciona un fichero para empezar a escribir en &eacute;l.
	 * @param outFile Fichero de salida.
	 * @return Flujo de datos en el que escribir.
	 * @throws IOException Si no se puede crear o escribir en el fichero.
	 */
	public static OutputStream selectFileToWrite(final File outFile) throws IOException {
		return selectFileToWrite(outFile, false);
	}

	/**
	 * Selecciona un fichero para empezar a escribir en &eacute;l. Debe comprobarse antes si nos
	 * encontramos en un entorno JNLP.
	 * @param outFile Fichero de salida.
	 * @param append Indica si el nuevo contenido se debe agregar al que ya hay en el fichero.
	 * @return Flujo de datos en el que escribir.
	 * @throws IOException Si no se puede crear o escribir en el fichero.
	 * @throws java.lang.NoClassDefFoundError Si no nos encontramos en un entorno JNLP.
	 */
	public static OutputStream selectFileToWrite(final File outFile, final boolean append) throws IOException {

		// La siguiente llamada es equivalente a:
		// return ((javax.jnlp.ExtendedService) getJnlpExtendedService())
		// 		.openFile(outFile).getOutputStream(append);
		// Devuelve un OutputStream para la salida del fichero.
		try {
			final Object serviceObject = getJnlpExtendedService();
			final Method openFileMethod = serviceObject.getClass().getMethod("openFile", File.class); //$NON-NLS-1$

			final Object fileContentsObject = openFileMethod.invoke(serviceObject, outFile);
			final Method getOutputStreamMethod = fileContentsObject.getClass().getMethod("getOutputStream", Boolean.TYPE); //$NON-NLS-1$

			return (OutputStream) getOutputStreamMethod.invoke(openFileMethod, Boolean.valueOf(append));
		}
		catch (final Exception e) {
			throw new IOException("No se ha podido obtener el flujo de salida del fichero", e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Recupera un objeto {@code javax.jnlp.ExtendedService} con el que controlar
	 * funciones del servicio JNLP.
	 * @return Objeto {@code javax.jnlp.ExtendedService} o {@code null} en caso de
	 * no encontrarnos en un despliegue JNLP o no poder recuperar el servicio.
	 */
	private static Object getJnlpExtendedService() {

		Object extendedService = null;
		
		//if (!isJnlpDeployment()) {
			try {
				final Class<?> serviceManagerClass = Class.forName("javax.jnlp.ServiceManager"); //$NON-NLS-1$
				final Method lookupMethod = serviceManagerClass.getMethod("lookup", String.class); //$NON-NLS-1$
				extendedService = lookupMethod.invoke(null, "javax.jnlp.ExtendedService"); //$NON-NLS-1$
			} catch (final Throwable e) {
				extendedService = null;
			}
		//}
		
		return extendedService;
	}
	
	/** Elimina un directorio con todo su contenido.
	 * @param targetDir Directorio a eliminar. */
	static void deleteDir(final File targetDir) {
		try {
			Files.walkFileTree(
				targetDir.toPath(),
				new SimpleFileVisitor<Path>() {
			         @Override
			         public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
			             Files.delete(file);
			             return FileVisitResult.CONTINUE;
			         }
			         @Override
			         public FileVisitResult postVisitDirectory(final Path dir, final IOException e) throws IOException {
			             if (e != null) {
			            	 throw e;
			             }
			             Files.delete(dir);
		                 return FileVisitResult.CONTINUE;
			         }
			   }
			);
		}
		catch (final Exception e) {
			LOGGER.warning("No se pudo borrar el directorio '" + targetDir.getAbsolutePath() + "': " + e); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/** Recupera el directorio en el que se encuentra la aplicaci&oacute;n actual.
	 * @return Directorio de ejecuci&oacute;n. */
/*	static File getApplicationDirectoryViejo() {
		File appDir;
		try{
			ServiceManager.lookup("javax.jnlp.ExtendedService");
			appDir = new File(System.getProperty("java.io.tmpdir"));
			return appDir;
		} catch (UnavailableServiceException e) {
            
		}
		
		try {
			return new File(ConfiguratorUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
		}
		catch (final Exception e) {
			LOGGER.warning("No se pudo localizar el directorio del fichero en ejecucion: " + e); //$NON-NLS-1$
		}
		return null;
	}*/
	
	
	public static File getApplicationDirectory() {

		if (isJnlpDeployment()) {
			return getJNLPApplicationDirectory();
		}

		// Identificamos el directorio de instalacion
		try {
			return new File(ConfiguratorUtil.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile();
		}
		catch (final URISyntaxException e) {
			LOGGER.warning("No se pudo localizar el directorio del fichero en ejecucion: " + e); //$NON-NLS-1$
		}

		return null;
	}
	/**
	 * Comprueba si estamos en un despliegue JNLP de la aplicaci&oacute;n.
	 * @return {@code true} si estamos en un despliegue JNLP, {@code false}
	 * en caso contrario.
	 */
	private static boolean isJnlpDeployment() {

		// Para comprobar si estamos en un despliegue JNLP sin crear una dependencia
		// con javaws, hacemos una llamada equivalente a:
		//     javax.jnlp.ServiceManager.lookup("javax.jnlp.ExtendedService");
		// Si falla la llamda, no estamos en un despliegue JNLP
		try {
			final Class<?> serviceManagerClass = Class.forName("javax.jnlp.ServiceManager"); //$NON-NLS-1$
			final Method lookupMethod = serviceManagerClass.getMethod("lookup", String.class); //$NON-NLS-1$
			lookupMethod.invoke(null, "javax.jnlp.ExtendedService"); //$NON-NLS-1$
		}
		catch (final Throwable e) {
			return false;
		}
		return true;
	}
	/**
	 * Obtiene el directorio de aplicaci&oacute;n que corresponde cuando se
	 * ejecuta la aplicaci&oacute;n mediante un despliegue es JNLP.
	 * @return Directorio de aplicaci&oacute;n.
	 */
	public static File getJNLPApplicationDirectory() {
		if (Platform.getOS() == Platform.OS.WINDOWS) {
			final File appDir = getWindowsAlternativeAppDir();
			if (appDir.isDirectory() || appDir.mkdirs()) {
				return appDir;
			}
		}
		else if (Platform.getOS() == Platform.OS.MACOSX) {
			final File appDir = getMacOsXAlternativeAppDir();
			if (appDir.isDirectory() || appDir.mkdirs()) {
				return appDir;
			}
		}
		return new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
	}
	
	/**
	 * Recupera el directorio de instalaci&oacute;n alternativo en los sistemas Windows.
	 * @return Directorio de instalaci&oacute;n.
	 */
	public static File getWindowsAlternativeAppDir() {
		final String commonDir = System.getenv("ALLUSERSPROFILE"); //$NON-NLS-1$
		return new File (commonDir, "AutoFirma"); //$NON-NLS-1$
	}
	
	/**
	 * Recupera el directorio de instalaci&oacute;n alternativo en los sistemas macOS.
	 * @return Directorio de instalaci&oacute;n.
	 */
	public static File getMacOsXAlternativeAppDir() {
		final String userDir = System.getenv("HOME"); //$NON-NLS-1$
		return new File (userDir, "Library/Application Support/AutoFirma"); //$NON-NLS-1$
	}
	
	
	
	
	
}
