package es.gob.afirma.signers.batch;

import java.io.IOException;
import java.util.Properties;

/** Interfaz para el guardado, almacenaje o env&iacute;o de firmas una vez realizadas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public interface SignSaver {

	/** Guarda una firma electr&oacute;nica.
	 * @param sign Definici&oacute;n de la firma que se hizo.
	 * @param dataToSave Datos a guardar, resultado de la firma electr&oacute;nica.
	 * @throws IOException Si hay problemas durante el proceso. */
	void saveSign(final SingleSign sign, final byte[] dataToSave) throws IOException;

	/** Deshace un guardado previo (para los modos transaccionales).
	 * @param sign Identificador de la firma a deshacer. */
	void rollback(final SingleSign sign);

	/** Configura c&oacute;mo ha de guardarse la firma electr&oacute;nica.
	 * cada implementaci&oacute;n requerir&aacute; unas propiedades distintas dentro del
	 * objeto de propiedades.
	 * @param config Propiedades de configuraci&oacute;n. */
	void init(final Properties config);

	/** Obtiene las propiedades de configuraci&oacute;n.
	 * @return Propiedades de configuraci&oacute;n. */
	Properties getConfig();

}
