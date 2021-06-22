package es.gob.afirma.signers.batch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;
import java.util.logging.Logger;

/** Guarda firmas envi&aacute;ndolas a un servicio HTTP POST.
 * <b>Esta clase es &uacute;nicamente un ejemplo de implementaci&oacute;n del interfaz <code>SignSaver</code>
 * para depuraci&oacute;n, <u>nunca</u> debe usarse en entornos reales</b> (no hay comprobaciones de
 * qu&eacute; ficheros pueden sobrescribirse).
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class SignSaverFile implements SignSaver {

	private static final String PROP_FILENAME = "FileName"; //$NON-NLS-1$

	/** El guardado real est&aacute; deshabilitado por defecto, habilitar para usar esta clase
	 * para depuraci&oacute;n. No debe usarse para entornos reales, ya que no hay comprobaciones de
	 * qu&eacute; ficheros pueden sobrescribirse. */
	private static final boolean DISABLED = true;

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private String filename;

	/** Constyruye un objeto de guardado de firmas mediante un servicio HTTP POST.
	 * El servicio recibir&aacute; los datos en Base64 dentro del par&aacute;metro indicado.
	 * @param targetFileName Nombre del fichero, incluyendo ruta, donde guardar la firma. */
	public SignSaverFile(final String targetFileName) {
		if (targetFileName == null) {
			throw new IllegalArgumentException(
				"El nombre de fichero no puede ser nulo" //$NON-NLS-1$
			);
		}
		this.filename = targetFileName;
	}

	/** Constructor vac&iacute;o. */
	public SignSaverFile() {
		// Vacio
	}

	@Override
	public Properties getConfig() {
		final Properties p = new Properties();
		p.put(PROP_FILENAME, this.filename);
		return p;
	}

	@Override
	public void saveSign(final SingleSign sign, final byte[] dataToSave) throws IOException {
		if (!DISABLED) {
			final OutputStream fos = new FileOutputStream(this.filename);
			final BufferedOutputStream bos = new BufferedOutputStream(
				fos,
				dataToSave.length
			);
			bos.write(dataToSave);
			bos.flush();
			fos.close();
			LOGGER.info("Guardada finalmente la firma '" + sign.getId() + "' en: " + this.filename); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else {
			LOGGER.info(
				"El guardado esta deshabilitado. La ruta de guadado solicitada es: " +  this.filename //$NON-NLS-1$
			);
		}
	}

	@Override
	public void init(final Properties config) {
		if (config == null) {
			throw new IllegalArgumentException(
				"La configuracion no puede ser nula" //$NON-NLS-1$
			);
		}
		final String file = config.getProperty(PROP_FILENAME);
		if (file == null) {
			throw new IllegalArgumentException(
				"Es obligarorio que la configuracion incluya un valor para la propiedad " + PROP_FILENAME //$NON-NLS-1$
			);
		}
		this.filename = file;
	}

	@Override
	public void rollback(final SingleSign sign) {
		final File file = new File(this.filename);
		if (file.exists()) {
			file.delete();
		}
	}

}
