package es.gob.afirma.signers.batch;

import java.io.File;
import java.util.Date;

/**
 * Clase para la limpieza del directorio de temporales de las operaciones de lotes.
 * Estable un periodo de caducidad y todos los ficheros del directorio que existan
 * desde antes de ese periodo se eliminar&aacute;n.
 * @author Carlos Gamuci
 */
public class TempStoreFileSystemCleaner implements Runnable {

	/** Tiempo de caducidad de los temporales. */
	private static final int EXPIRED_PERIOD = 300000; // 5 minutos

	private static boolean runningCleaning = false;

	/**
	 * Indica si el proceso se est&aacute; ejecutando actualmente.
	 * @return {@code true} si el proceso de limpieza se est&aacute; ejecutando,
	 * {@code false} si no.
	 */
	static boolean isRunningCleaning() {
		return runningCleaning;
	}

	@Override
	public void run() {

		runningCleaning = true;

		final long limitTime = new Date().getTime() - EXPIRED_PERIOD;
		for (final File file : BatchConfigManager.getTempDir().listFiles()) {
			if (file.isFile() && file.lastModified() < limitTime) {
				file.delete();
			}
		}
		runningCleaning = false;
	}
}
