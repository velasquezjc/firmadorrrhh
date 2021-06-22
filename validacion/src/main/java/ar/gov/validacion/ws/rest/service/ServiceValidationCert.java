package ar.gov.validacion.ws.rest.service;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.jsoniter.output.JsonStream;

import ar.gov.validation.ws.rest.dto.ValidaciontServiceResp;

import es.gob.afirma.cert.certvalidation.CertificateVerifierFactory;
import es.gob.afirma.cert.certvalidation.CertificateVerifierFactoryException;
import es.gob.afirma.cert.certvalidation.ValidationResult;
import es.gob.afirma.core.misc.Base64;

@Path("/validacionCert")
public class ServiceValidationCert {

	private final static int MAX_BYTES_CERT =1024*10; //10KB tamaño maximo permitido para los certificados
	private final static String MENSAJE_TAM_CERT = "El certificado supera los 10 KB.";
	@POST
	@Path("/valideCert")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	/*cert esta codificado en Base64 con urlsafe*/
	public String valideCert(String certB64){
		byte[] certANS1;
		ValidationResult vr = null;
		ValidaciontServiceResp vrws =null;
		ValidationResult no = null;  //para los retornos por excepciones
		ArrayList<X509Certificate> certs = new ArrayList<X509Certificate>();
		
		//descodifico el certificado
		try {
			certANS1 =Base64.decode(certB64,false);
			
			//verifico el tamaño del archivo
			if(certANS1.length > MAX_BYTES_CERT){
				vrws = new ValidaciontServiceResp(certs,false,MENSAJE_TAM_CERT);
				//convierto la respuesta en json y la retorno
				return JsonStream.serialize(vrws);
			}
		} catch (IOException e1) {
			//"error en la decodificacion del certificado";
			//se retorna error desconocido por no considerarse la decodificacion b64
			//como un error propio del manejo del certificado
			no = ValidationResult.UNKNOWN;
    		//retorno solo los atributos reason, result
		    return  no.toJsonString();
		}
		//genero el certificado	
		X509Certificate cert;
		try {
			/*cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
					ServiceValidationCert.class.getResourceAsStream("/fabi-rebocado.cer")		//new ByteArrayInputStream(certANS1) //$NON-NLS-1$
				);*/
			cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
					new ByteArrayInputStream(certANS1) //$NON-NLS-1$
				);
		} catch (CertificateException e1) {
			//"error en la generacion del certificado";se podria considerar corrupto
			no = ValidationResult.CORRUPT;
    		//retorno solo los atributos reason, result
		    return  no.toJsonString();
		}
		
		//valido el certificado, formato x509,caducidad,revocacion	
		//en tiempo actual
		try {
				vr = CertificateVerifierFactory.getCertificateVerifier(
					cert).validateCertificate();
		} catch (CertificateVerifierFactoryException e) {
			
			no = ValidationResult.VERIFICADOR_NO_SOPORTADO;
				//e.printStackTrace();
			//retorno solo los atributos reason, result
			return  no.toJsonString();
		}
        //para lanzar excepcion con el tipo de error en caso de
		//que el certificado tenga errores de validacion
		//vr.check();
		
		//retorno si es un certificad valido x.509, esta en periodo de validez y no revocado
			
		//return Boolean.toString(vr.isValid());
        // return vr.toString();
		//System.out.println(vr.toJsonString()); 
		
		
		//creo la respuesta
		certs.add(cert);
		vrws = new ValidaciontServiceResp(certs,vr.isValid(),vr.toString());
		//convierto la respuesta en json y la retorno
		return JsonStream.serialize(vrws);
	}
	
	
	
	
	
	/***funcion a borrar porque el certificado la conversion la obtengo
	 * desde los test de base64*/
	/*obtengo el B64 de un certificado, en este caso el base64 es url safe*/
	public String valideCert2(String certB64){
		byte[] certANS1;
		ValidationResult vr = null;
		String s;
		//descodifico el certificado
		try {
			certANS1 =Base64.decode(certB64.trim(),true);
		} catch (IOException e1) {
			return "error en la decodificacion del certificado";
		}
		//genero el certificado	
		X509Certificate cert;
		try {
			cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
					ServiceValidationCert.class.getResourceAsStream("/fabi-rebocado.cer")		//new ByteArrayInputStream(certANS1) //$NON-NLS-1$
				);
			s =Base64.encode(cert.getEncoded(), true);
			/*cert = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate( //$NON-NLS-1$
					new ByteArrayInputStream(certANS1) //$NON-NLS-1$
				);*/
		} catch (CertificateException e1) {
			return  Base64.encode(certANS1, true);// e1.toString();//"error en la generacion del certificado";
		}
		
		//valido el certificado, formato x509,caducidad,revocacion	
		try {
				vr = CertificateVerifierFactory.getCertificateVerifier(
					cert).validateCertificate();
		} catch (CertificateVerifierFactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
	///		vr.check();
		
		//retorno si es un certificad valido x.509, esta en periodo de validez y no revocado
			
		//return Boolean.toString(vr.isValid());
		return s;
	}
}
