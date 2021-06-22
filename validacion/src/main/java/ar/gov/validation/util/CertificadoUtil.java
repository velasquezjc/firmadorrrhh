/**
 * 
 */
package ar.gov.validation.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.spongycastle.asn1.x500.RDN;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x500.style.IETFUtils;


/**
 * @author gov.ar
 * 
 */
public class CertificadoUtil {
	//usos permitidos del certificado
    private static final  String digitalSignature = "Firma Digital";
    private static final  String nonRepudiation = "No Repudio";
    private static final  String keyEncipherment = "Clave de cifrado";
    private static final  String dataEncipherment = "Cifrado de datos";
    private static final  String keyAgreement = "keyAgreement";
    private static final  String keyCertSign = "keyCertSign";
    private static final  String cRLSign = "cRLSign";
    private static final  String encipherOnly = "encipherOnly";
    private static final  String decipherOnly = "decipherOnly";
    private static final  String desconocido = "uso no estandar";
	
	public static String getCertificateAtributeSubject(X500Name x500name,org.spongycastle.asn1.ASN1ObjectIdentifier oid){
	  
		 //Por si me piden un atributo que no posee el certificado
		 try{
			 RDN valor = x500name.getRDNs(oid)[0];
			 return IETFUtils.valueToString(valor.getFirst().getValue());
		 }catch (Exception e) {
				return "";
         }
		 
		 
	  }
	 

	public static String getCertificatePolicyId(X509Certificate cert,
			int certificatePolicyPos, int policyIdentifierPos) {
		// los oid se obtienen de
		// https://www.cryptosys.net/pki/manpki/pki_distnames.html
		// http://oidref.com/1.2.840.113549.1
		// https://www.ietf.org/rfc/rfc2253.txt
		// https://www.ietf.org/rfc/rfc1779.txt
		// tambien haciendo boton derecho sobre cert con el debug
		// se pueden ver los campos con oid del certificado
		final String CERTIFICATE_POLICY_OID = "2.5.29.32";

		byte[] extPolicyBytes = cert.getExtensionValue(CERTIFICATE_POLICY_OID);
		if (extPolicyBytes == null) {
			return null;
		}

		DEROctetString oct;
		ASN1InputStream asn1InputStream = null;
		ASN1InputStream asn1InputStream2 = null;
		
		try {
			asn1InputStream = new ASN1InputStream(
					new ByteArrayInputStream(extPolicyBytes));
			oct = (DEROctetString) (asn1InputStream.readObject());

			asn1InputStream2 = new ASN1InputStream(
					new ByteArrayInputStream(oct.getOctets()));
			ASN1Sequence seq = (ASN1Sequence) asn1InputStream2.readObject();
			
			
			if (seq.size() <= (certificatePolicyPos)) {
				//asn1InputStream.close();
				//asn1InputStream2.close();
				return null;
			}

			CertificatePolicies certificatePolicies = new CertificatePolicies(
					PolicyInformation.getInstance(seq
							.getObjectAt(certificatePolicyPos)));
			if (certificatePolicies.getPolicyInformation().length <= policyIdentifierPos) {
				//asn1InputStream.close();
				//asn1InputStream2.close();
				return null;
			}

			PolicyInformation[] policyInformation = certificatePolicies
					.getPolicyInformation();
			
			//asn1InputStream.close();
			//asn1InputStream2.close();
			
			return policyInformation[policyIdentifierPos].getPolicyIdentifier()
					.getId();

		} catch (IOException e) {
			return "";// hubo un problema en la lectura, retorno politica de
						// certificacion vacia
		}finally{
			try {
				asn1InputStream.close();
				asn1InputStream2.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	public static String getKeyUsage(boolean[] v ) {
		StringBuffer res = new StringBuffer();
		
		for(int i=0; i < v.length;i++ ){
						
			if(v[i]){
				switch (i) {
	            case 0:  res = res.append(digitalSignature);
	                     break;
	            case 1:  res = res.append(nonRepudiation);
	                     break;
	            case 2:  res = res.append(keyEncipherment);
	                     break;
	            case 3:  res = res.append(dataEncipherment);
	                     break;
	            case 4:  res = res.append(keyAgreement);
	                     break;
	            case 5:  res = res.append(keyCertSign);
	                     break;
	            case 6:  res = res.append(cRLSign);
	                     break;
	            case 7:  res = res.append(encipherOnly);
	                     break;
	            case 8:  res = res.append(decipherOnly);
	                     break;           
	            default: res = res.append(desconocido);
	                     break;
	            }
				res = res.append(" | ");
			}
			
		}
		return res.toString();
	}

	
}