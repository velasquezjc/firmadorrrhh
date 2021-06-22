package ar.gov.validation.ws.rest.dto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.asn1.x509.Extensions;
import org.spongycastle.asn1.ASN1OctetString;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.BCStyle;
import org.spongycastle.asn1.x509.CertificatePolicies;
import org.spongycastle.asn1.x509.PolicyInformation;
import org.spongycastle.cert.jcajce.JcaX509ExtensionUtils;

import es.gob.afirma.cert.certvalidation.ValidationResult;
import ar.gov.validation.util.CertificadoUtil;



/**
 * @author gov.ar
 *
 */
public class ValidaciontServiceResp {
	
	//Datos de la validacion
	public String result;
	public String reason;
	
	//lista de resumen de certificados y algunos datos mas, en la validacion de certificados solo tendra un 
	//elemento, en la validacion de firmas, tendra todos los resumenes de datos
	//de los firmantes
	ArrayList<ResumenResp> certsFirmantes = new ArrayList <ResumenResp>();
	
	
	public ValidaciontServiceResp(ArrayList<X509Certificate> certs,boolean resultado, String motivo) {
		// get the domain name
	    Map<String, String> oidMap = new HashMap<String, String>();
	    // oidMap.put("1.2.840.113549.1.9.1", "EMAILADDRESS"); // OID for email address
	    //el mapa de oid reemplaza los oid indicados por el value
	    //oidMap.put("2.5.4.5", "SERIALNUMBER");
	    
	    
		final Date ahora = new Date();
		final DateFormat datehourFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss ");
		
		//seteo los datos de validacion proporcionados
		this.result = resultado ? "OK" : "KO";
		this.reason = motivo;
		
		//recorro la lista de certificados y agrego los resumenes a la respuesta
		for (final X509Certificate cert : certs) {
			final X500Name x500name = new X500Name(cert.getSubjectX500Principal().getName());
			final ResumenResp res= new ResumenResp();
			
			
		    //String x = cert.getSubjectX500Principal().getName(X500Principal.RFC1779,oidMap);
			res.asunto_CN = CertificadoUtil.getCertificateAtributeSubject(x500name, BCStyle.CN);
			res.asunto_SerialNumber = CertificadoUtil.getCertificateAtributeSubject(x500name, BCStyle.SERIALNUMBER);
			res.emisor = cert.getIssuerDN().getName();
			res.firmante = cert.getSubjectDN().getName();
			//NOTA: como los certificados emitidos por la AC utilizan el campo E o EMAILADRESS
			//para ubicar el email, pero esto ya es obsoleto pero se mantiene por cuestiones de uso
			//igual el email tambien esta en subject alternative name rfc822Name
			//lo ideal y generico seria usarlo de ahi
			//http://www.alvestrand.no/objectid/2.5.29.17.html
			//pero por ahora lo sacamos directamente del subject si esta
			res.correo = CertificadoUtil.getCertificateAtributeSubject(x500name, BCStyle.E);
			res.valido_desde = datehourFormat.format(cert.getNotBefore());
			res.valido_hasta = datehourFormat.format(cert.getNotAfter());
			res.num_serie = cert.getSerialNumber().toString(10); //convierto el BigInteger a valor hexadecimal 16, mejor a decimal
			
			//el calculo de la politica del certificado es pesada asi que la 
			//harcodeo dado que por ahora siempre va a ser la misma
			//res.politica_cert = "2.16.32.1.1.3";//CertificadoUtil.getCertificatePolicyId(cert, 0, 0);//politica del certificado, esta forma de obtenerla es relenta
			//se asume que las politicas de certificacion son unicas,osea hay una sola politica usada por los certificados
			res.politica_cert = fun(cert)[0].getPolicyIdentifier().toString(); //String.valueOf(fun(cert).length);
			res.uso_certificado = CertificadoUtil.getKeyUsage(cert.getKeyUsage());
			res.fecha_consulta = datehourFormat.format(ahora);
			//res.correo = fun2(cert);
			
			certsFirmantes.add(res);
		}
		
	}
    public PolicyInformation[] fun(final X509Certificate cert){
	final byte[] certificatePoliciesBytes = cert.getExtensionValue("2.5.29.32"); //$NON-NLS-1$
	if (certificatePoliciesBytes == null || certificatePoliciesBytes.length < 1) {
		return null;
	}

	return CertificatePolicies.getInstance(
		ASN1Sequence.getInstance(
			ASN1OctetString.getInstance(certificatePoliciesBytes).getOctets()
		)
	).getPolicyInformation();
    }
    
    //////MAL, ver que el email es el atributo de la secuencia contenida en otro atributo
    //y es el rfc822Name, falta afinar la funcion, solo retorna el subject alternative name
    //pero dentro de eso hay que sacar el email aun
	public String fun2(final X509Certificate cert){
		//ASN1ObjectIdentifier
		final byte[] encodedExtensionValue = cert.getExtensionValue("2.5.29.17"); //$NON-NLS-1$
		if (encodedExtensionValue != null) {
		    ASN1Primitive extensionValue = null;
			try {
				extensionValue = JcaX509ExtensionUtils.parseExtensionValue(encodedExtensionValue);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    String values = extensionValue.toString();   
		    return values;
		}
		return "";
		
		
     }
	
	
}
