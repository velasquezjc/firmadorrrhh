package es.gob.jmulticard.apdu.connection.cwa14890;

import es.gob.jmulticard.apdu.connection.ApduConnection;

/** Conexi&oacute;n CWA-14890.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public interface Cwa14890Connection extends ApduConnection {

	/** Recupera la conexi&oacute;n subyacente utilizada por la conexi&oacute;n segura.
     * @return Conexi&oacute;n con la tarjeta. */
    ApduConnection getSubConnection();

}
