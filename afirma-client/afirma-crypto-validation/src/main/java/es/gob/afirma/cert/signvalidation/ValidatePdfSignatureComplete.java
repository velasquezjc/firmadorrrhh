/**
 * 
 */
package es.gob.afirma.cert.signvalidation;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.aowagie.text.pdf.AcroFields;
import com.aowagie.text.pdf.PdfDictionary;
import com.aowagie.text.pdf.PdfName;
import com.aowagie.text.pdf.PdfPKCS7;
import com.aowagie.text.pdf.PdfReader;

import es.gob.afirma.cert.certvalidation.CertificateVerifierFactory;
import es.gob.afirma.cert.certvalidation.CertificateVerifierFactoryException;
import es.gob.afirma.cert.certvalidation.ValidationResult;
import es.gob.afirma.cert.signvalidation.SignValidity.SIGN_DETAIL_TYPE;
import es.gob.afirma.cert.signvalidation.SignValidity.VALIDITY_ERROR;

/** Validador de firmas PDF con estados de revocacion.
 * Se validan los certificados revisando las fechas de validez de los certificados
 * y los estados de revocacion online.
 * @author gov.ar
 *
 */
public class ValidatePdfSignatureComplete implements SignValider {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final PdfName PDFNAME_ETSI_RFC3161 = new PdfName("ETSI.RFC3161"); //$NON-NLS-1$
	private static final PdfName PDFNAME_DOCTIMESTAMP = new PdfName("DocTimeStamp"); //$NON-NLS-1$
	private ArrayList<X509Certificate> certsFirmantes = new ArrayList <X509Certificate>();
			

	/**
	 * @return the certsFirmantes
	 */
	public ArrayList<X509Certificate> getCertsFirmantes() {
		return certsFirmantes;
	}


	/**
	 * @param certsFirmantes the certsFirmantes to set
	 */
	public void setCertsFirmantes(ArrayList<X509Certificate> certsFirmantes) {
		this.certsFirmantes = certsFirmantes;
	}


	public SignValidity validate(final byte[] sign) throws IOException {
		final PdfReader reader = new PdfReader(sign);
		final AcroFields af = reader.getAcroFields();
		final List<String> sigNames = af.getSignatureNames();
			
		//SOLO verifico, para cada firmante, el certificado de firma
		//dado que la demas parte de la cadena en general siempre es la misma
		//y bastaria con preinstalar los certificados en la app y solo verificar
		//que los certificados de firma sean FIRMADOS por alguno de esos emisores
		//de certificados preinstalado, debido a que los mismos duran muchos años
		//si no pasa nada, asi como hacen los navegadores
		for (final String name : sigNames) {
			//el nombre verifySignature le queda grande
			//en realidad crea y retorna el objeto PdfPKCS7, que es una conjuncion de
			//varios campos de un signatureDiccionary  desglosados y algo mas
			final PdfPKCS7 pk = af.verifySignature(name);

    		// Comprobamos si es una firma o un sello
    		final PdfDictionary pdfDictionary = af.getSignatureDictionary(name);

    		// En los sellos no comprobamos el PKCS#1
    		if (!PDFNAME_ETSI_RFC3161.equals(pdfDictionary.get(PdfName.SUBFILTER)) && !PDFNAME_DOCTIMESTAMP.equals(pdfDictionary.get(PdfName.SUBFILTER))) {
				try {//si entra es que es una firma adbe.pkcs7.detached o adbe.pkcs7.sha1 o ETSI.CAdES.detached,etc. 
					//El objeto codificado con DER SignedData especificado en CMS (RFC 3852) esta incluido en la firma del PDF
					//en la entrada con la clave Contents del diccionario Signature como esta descripto en la iso 32000-8 clausula 12.8.1
					//Este objeto CMS forma una firma CADES descripta en TS 100 733 incluye varios atributos
					//Requirements specified in ISO 32000-1, clauses 12.8.3.2 (PKCS#1) and 12.8.3.3 (PKCS#7) signatures as used in ISO 32000-1 do not apply.
					
					if (!pk.verify()) {
						return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.NO_MATCH_DATA);
					}
				}
				catch (final Exception e) {
					//LOGGER.warning("Error validando la firma '" + name + "' del PDF: " + e); //$NON-NLS-1$ //$NON-NLS-2$
					return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.CORRUPTED_SIGN);
				}
    		}
    		//obtengo el certificado que se utilizo para firmar el resumen del documento
			final X509Certificate signCert = pk.getSigningCertificate();
			final ValidationResult vr;
			
			//agrego el certificado del firmante a la lista
			certsFirmantes.add(signCert);
			try {
				
				//validamos el certificado, si esta en fecha,firmado correctamente por el emisor y su estado de revocacion		
				vr = CertificateVerifierFactory.getCertificateVerifier(signCert).validateCertificate();
			    //verifico la respuesta de validez del certificado
				
				//lanza una excepcion en caso de invalidez 
				vr.check();
				
			}
			catch (CertificateVerifierFactoryException e) {
				return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.CA_NOT_SUPPORTED);
			} catch (CertificateException e) {
				//no muestro el detalle de error del certificado
				//solo muestro un detalle generico de error
				return new SignValidity(SIGN_DETAIL_TYPE.KO,VALIDITY_ERROR.CERTIFICATE_PROBLEM);
				
			}
		}
		reader.close();
		//verifico si el archivo firmado , en realidad no tenia ninguna firma
		if(sigNames.isEmpty()){
			return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.NO_SIGN);
		}
		return new SignValidity(SIGN_DETAIL_TYPE.OK, null);
	}



}
