package es.gob.afirma.cert.signvalidation;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

/** Valida una firma del tipo del validador instanciado.
 * @author Sergio Mart&iacute;nez Rico. 
 * Update: gov.ar*/
public interface SignValider {

	/** Valida una firma del tipo del validador instanciado.
     * @param sign Firma a validar
     * @return Validez de la firma. 
	 * @throws IOException Fallo durante la validaci&oacute;n de la firma. */
    SignValidity validate(final byte[] sign) throws IOException;
    /**Obtiene una lista de certificados de los firmantes
     *@return Lista de certificados de los firmantes. 
     *  */
    public ArrayList<X509Certificate> getCertsFirmantes();
}
