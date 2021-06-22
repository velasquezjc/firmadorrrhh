/**
 * 
 */
package ar.gov.validacion.ws.rest.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ar.gov.validation.ws.rest.dto.ValidaciontServiceResp;

import com.aowagie.text.pdf.AcroFields;
import com.aowagie.text.pdf.PdfDictionary;
import com.aowagie.text.pdf.PdfName;
import com.aowagie.text.pdf.PdfPKCS7;
import com.aowagie.text.pdf.PdfReader;
import com.jsoniter.output.JsonStream;

import es.gob.afirma.cert.certvalidation.CertificateVerifierFactory;
import es.gob.afirma.cert.certvalidation.CertificateVerifierFactoryException;
import es.gob.afirma.cert.certvalidation.ValidationResult;
import es.gob.afirma.cert.signvalidation.SignValider;
import es.gob.afirma.cert.signvalidation.SignValiderFactory;
import es.gob.afirma.cert.signvalidation.SignValidity;
import es.gob.afirma.cert.signvalidation.SignValidity.SIGN_DETAIL_TYPE;
import es.gob.afirma.cert.signvalidation.SignValidity.VALIDITY_ERROR;
import es.gob.afirma.cert.signvalidation.ValidatePdfSignature;
import es.gob.afirma.cert.signvalidation.ValidatePdfSignatureComplete;
import es.gob.afirma.core.misc.Base64;

/**
 * @author gov.ar
 * 
 */
@Path("/validacionSign")
public class ServiceValidationSign {
	private final static int MAX_BYTES_DOC =1048576*8; //8MB tamaño maximo permitido para los certificados
	private final static String MENSAJE_TAM_DOC = "El documento supera los 8 MB.";
	
	@POST
	@Path("/valideSign")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	/* cert esta codificado en Base64 con urlsafe */
	public String valideSign(String fileB64) {
		byte[] signANS1;
		ValidaciontServiceResp vrws = null;
		SignValidity sv = null;
		final SignValider validator;
		ArrayList<X509Certificate> certsFirmantes = new ArrayList <X509Certificate>();
		
		try {
			// descodifico el archivo
			signANS1 = Base64.decode(fileB64, false);

			//verifico el tamaño del archivo
			if(signANS1.length > MAX_BYTES_DOC){
				vrws = new ValidaciontServiceResp(certsFirmantes,false,MENSAJE_TAM_DOC);
				//convierto la respuesta en json y la retorno
				return JsonStream.serialize(vrws);
			}
			
			// obtengo el validador adecuado, segun le formato del archivo a
			// validar
			validator = SignValiderFactory.getSignValider(signANS1);
			// verifico todas las firmas incluidas en el archivo
			sv = validator.validate(signANS1);
			
			// si todas las firmas estan ok, en conjunto con sus estados de
			// revocacion
			if (sv.isValid()) {
				// obtengo la lista de certificados de todos los firmantes
				certsFirmantes = validator.getCertsFirmantes();
				// creo la respuesta
				vrws = new ValidaciontServiceResp(certsFirmantes,sv.isValid(),sv.toString());
				/*
				 * vrws = new ValidacionCertResp(cert,vr); //convierto la
				 * respuesta en json y la retorno return
				 * JsonStream.serialize(vrws);
				 */
				
				return JsonStream.serialize(vrws);
			} else {
				// retorno solo los atributos reason, result
				return sv.toJsonString();
			}
		} catch (IOException e1) {
			// "error en la decodificacion del archivo" o ioException cuando no
			// se puede generar el reader;
			// se retorna error desconocido por no considerarse la
			// decodificacion b64
			// como un error propio del manejo del archivo
			sv = new SignValidity(SIGN_DETAIL_TYPE.KO,
					VALIDITY_ERROR.UNKOWN_ERROR);
			// retorno solo los atributos reason, result
			return sv.toJsonString();
		}

	}

}
