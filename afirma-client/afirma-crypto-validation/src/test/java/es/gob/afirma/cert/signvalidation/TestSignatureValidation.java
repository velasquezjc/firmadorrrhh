package es.gob.afirma.cert.signvalidation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.aowagie.text.DocumentException;
import com.aowagie.text.pdf.AcroFields;
import com.aowagie.text.pdf.PRStream;
import com.aowagie.text.pdf.PdfArray;
import com.aowagie.text.pdf.PdfDictionary;
import com.aowagie.text.pdf.PdfName;
import com.aowagie.text.pdf.PdfObject;
import com.aowagie.text.pdf.PdfPKCS7;
import com.aowagie.text.pdf.PdfReader;
import com.aowagie.text.pdf.PdfSignatureAppearance;
import com.aowagie.text.pdf.PdfStamper;
import com.aowagie.text.pdf.PdfString;
import com.aowagie.text.pdf.RandomAccessFileOrArray;
import com.aowagie.text.pdf.AcroFields.*;

import es.gob.afirma.cert.certvalidation.CertificateVerifierFactory;
import es.gob.afirma.cert.certvalidation.CertificateVerifierFactoryException;
import es.gob.afirma.cert.certvalidation.ValidationResult;
import es.gob.afirma.cert.signvalidation.SignValidity.SIGN_DETAIL_TYPE;
import es.gob.afirma.cert.signvalidation.SignValidity.VALIDITY_ERROR;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.signers.pades.PdfUtil;

/** Pruebas de validaci&oacute;n de firmas.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. 
 * Update: gov.ar*/
public class TestSignatureValidation {

	private static final String CADES_IMPLICIT_FILE = "cades_implicit.csig"; //$NON-NLS-1$
	private static final String CADES_EXPLICIT_FILE = "cades_explicit.csig"; //$NON-NLS-1$
	private static final String DATA_TXT_FILE = "txt"; //$NON-NLS-1$
	private static final String PADES_FILE = "pades.pdf"; //$NON-NLS-1$
	private static final String PADES_BASIC_FILE_INVALIDO = "pades-validaeinvalida.pdf"; //$NON-NLS-1$
	private static final String PADES_BASIC_FILE_VALIDO = "pades-FirmadoCaracol.pdf"; //$NON-NLS-1$
	private static final String PADES_EPES_FILE_INVALIDO = "pades-FirmadoCadesInvalido.pdf"; //$NON-NLS-1$
	private static final String PADES_EPES_FILE_VALIDO = "pades-FirmadoCadesValido.pdf"; //$NON-NLS-1$

	/** Prueba de validaci&oacute;n de firma CAdES.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
//	@Test
	public void testCadesImplicitValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(CADES_IMPLICIT_FILE);
		) {
			final byte[] cades = AOUtil.getDataFromInputStream(is);
			System.out.println(ValidateBinarySignature.validate(cades, null));
		}
	}

	/** Prueba de validaci&oacute;n de firma CAdES explicita sin datos.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
//	@Test
	public void testCadesExplicitValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(CADES_EXPLICIT_FILE);
		) {
			final byte[] cades = AOUtil.getDataFromInputStream(is);
			System.out.println(ValidateBinarySignature.validate(cades, null));
		}
	}

	/** Prueba de validaci&oacute;n de firma CAdES expl&iacute;cita con datos erroneos.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
//	@Test
	public void testCadesExplicitValidationWrongData() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(CADES_EXPLICIT_FILE);
			final InputStream dataIs = ClassLoader.getSystemResourceAsStream(DATA_TXT_FILE);
		) {
			final byte[] cades = AOUtil.getDataFromInputStream(is);
			final byte[] data = AOUtil.getDataFromInputStream(dataIs);
			System.out.println(ValidateBinarySignature.validate(cades, data));
		}
	}

	/** Prueba de validaci&oacute;n de firma PAdES-BES. Sin verificar estados de revocacion,
	 * solo verifica la validez de los certificados a la fecha actual
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
//	@Test
	public void testPadesValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(PADES_FILE);
		) {
			final byte[] pades = AOUtil.getDataFromInputStream(is);
			//se validan pades-basic en caso de varias firmas, si existe algun certificado de firma vencido
			//como indica la iso 32000-1 se considera firma invalida, y retorna
			System.out.println(SignValiderFactory.getSignValider(pades).validate(pades));
		}
	}

	/** Prueba de validaci&oacute;n de firma PAdES-EPES.Sin verificar estados de revocacion,
	 * solo verifica la validez de los certificados a la fecha actual
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
//	@Test
	public void testPadesEpesValidation() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(PADES_EPES_FILE_VALIDO);
				final InputStream is2 = 	TestSignatureValidation.class.getResourceAsStream("/pades-FirmadoCadesValido.pdf");
		) {		
			
			final byte[] pades = AOUtil.getDataFromInputStream(is2);
			///////////////////
			////
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] data = new byte[16384];
			int nRead;
			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			buffer.flush();
			final byte[]  ccc= buffer.toByteArray();
			
			
			//////////////////
			//verifico si tiene una politica de certificacion valida
			//obtener la politica es costosa en tiempo , se podira asumir
			//que si existe el doc en el sistema interno de archivado
			//entonces la politica fue seteada correctamente
			//o que con solo existir dentro del sistema este respeta
			//el contexto de uso de la firma por default
			
			//catch si no tiene politica o es una politica no soportada
			//verifico cades en pades	
			
			//se validan pades-epes en caso de varias firmas, si existe algun certificado de firma vencido
			//como indica la iso 32000-1 se considera firma invalida, y retorna
			//nota: las firmas cades en pades las valida comparando solo digest pero 
			//no se si faltarian validar mas atributos propios del cades
			//para eso hay que leer la especificacion de cades
//			System.out.println(SignValiderFactory.getSignValider(pades).validate(pades));
			//FIX: creo que deberia obtener el campo de firmar del pdf
			//y verificar cades con eso,osea obtener un ValidateSignatureBinary.validate(datos de firma, null)
			System.out.println(validar(pades,ccc));
/*			final File saveFile = File.createTempFile("TEST_c_", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
	        final OutputStream fos = new FileOutputStream(saveFile);
	        fos.write(ccc);
	        fos.flush();
	        fos.close();
	        System.out.println("Temporal para comprobacion manual: " + saveFile.getAbsolutePath()); //$NON-NLS-1$
*/
	        
		}
	}
	
	public SignValidity validar(final byte[] pades,byte[]  ccc2) throws IOException {
		final PdfReader reader = new PdfReader(pades);
		final AcroFields af = reader.getAcroFields();
		final List<String> sigNames = af.getSignatureNames();
		

	//	ByteArrayOutputStream buffer = new ByteArrayOutputStream();


			
			
			/*byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
			  buffer.write(data, 0, nRead);
			}

			buffer.flush();
			ccc= buffer.toByteArray();*/
			//byte[] bytes = IOUtils.toByteArray(is);
			

		
		/*************/
		for (final String name : sigNames) {
			//el nombre verifySignature le queda grande
			//en realidad crea y retorna el objeto PdfPKCS7, que es una conjuncion de
			//varios campos de un signatureDiccionary  desglosados y algo mas
			final PdfPKCS7 pk = af.verifySignature(name);

    		// Comprobamos si es una firma o un sello
    		final PdfDictionary pdfDictionary = af.getSignatureDictionary(name);

    		// En los sellos no comprobamos el PKCS#1
    		if (!new PdfName("ETSI.RFC3161").equals(pdfDictionary.get(PdfName.SUBFILTER)) && !new PdfName("DocTimeStamp").equals(pdfDictionary.get(PdfName.SUBFILTER))) {
				try {//si entra es que es una firma adbe.pkcs7.detached o adbe.pkcs7.sha1 o ETSI.CAdES.detached,etc. 
					//El objeto codificado con DER SignedData especificado en CMS (RFC 3852) esta incluido en la firma del PDF
					//en la entrada con la clave Contents del diccionario Signature como esta descripto en la iso 32000-8 clausula 12.8.1
					//Este objeto CMS forma una firma CADES descripta en TS 100 733 incluye varios atributos
					//Requirements specified in ISO 32000-1, clauses 12.8.3.2 (PKCS#1) and 12.8.3.3 (PKCS#7) signatures as used in ISO 32000-1 do not apply.
					
					//Verifico cades
					//obtengo un validador cades
				
					//obtengo la firma del pdf
					PdfObject o = pdfDictionary.get(PdfName.CONTENTS); 
					PdfString contents = pdfDictionary.getAsString(PdfName.CONTENTS);
					
					
					/*PdfDictionary annot = (PdfDictionary) reader.getPdfObject(2);
					PdfDictionary ef = annot.getAsDict(PdfName.FS); 
					PRStream prs = (PRStream) PdfReader.getPdfObject(ef.get(PdfName.F));
					byte b[] = PdfReader.getStreamBytes(prs);*/
					//FileInputStream os = new FileInputStream(reader);
					//byte[] ddd= 
					/*for (final Object element : af.getFields().entrySet()) {
						Map.Entry entry = (Map.Entry)element;
						PdfArray  ro = (PdfArray) pdfDictionary.get(PdfName.BYTERANGE);
						int x = ro.getAsNumber( ro.size() - 1).intValue();
						int y = ro.getAsNumber(ro.size() - 2).intValue();
						final int length =  x + y;
				           
						Object i = new Object[]{entry.getKey(), new int[]{length, 0}};
						
					}*/
					//get(PdfName.V)).getMerged(0);
					//byte[] vv = ccc.getBytes();
					//final byte msd[] = this.messageDigest.digest();
					//////armo el dato original.length/2  54317, 85891
					byte[] a1 = Arrays.copyOfRange(ccc2, 0, 315);
					byte[] a2 = Arrays.copyOfRange(ccc2, 54317, 54317+85891);
					byte[] au= new byte[a1.length+a2.length];
					System.arraycopy(a1, 0, au, 0, a1.length);
					///////
					byte[] a = contents.getBytes();//otra forma seria o.getBytes();
					SignValidity s = ValidateBinarySignature.validate(a, au);
					System.out.println(s);

					/*if (!pk.verify()) {
						return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.NO_MATCH_DATA);
					}*/
				}
				catch (final Exception e) {
					//LOGGER.warning("Error validando la firma '" + name + "' del PDF: " + e); //$NON-NLS-1$ //$NON-NLS-2$
					return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.CORRUPTED_SIGN);
				}
    		}
			final X509Certificate signCert = pk.getSigningCertificate();
			try {
				//verifica que el certificado es actualmente valido.Osea si
				//la fecha actual esta dentro del periodo de validez del certificado
				signCert.checkValidity();
			}
			catch (final CertificateExpiredException e) {
				// Certificado caducado
				//LOGGER.info("El certificado usado ha expirado: " + e); //$NON-NLS-1$
	            return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.CERTIFICATE_EXPIRED);
			}
			catch (final CertificateNotYetValidException e) {
				// Certificado aun no valido
				//LOGGER.info("El certificado usado todavia no es valido: " + e); //$NON-NLS-1$
	            return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.CERTIFICATE_NOT_VALID_YET);
			}
		}
		return new SignValidity(SIGN_DETAIL_TYPE.OK, null);
	}
	
	
	/** Prueba de validaci&oacute;n de firma PAdES-BES.Con verificar estados de revocacion,
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testPadesValidationComplete() throws Exception {
		try (
			final InputStream is = ClassLoader.getSystemResourceAsStream(PADES_EPES_FILE_VALIDO);
		) {
			final byte[] pades = AOUtil.getDataFromInputStream(is);
			//se validan pades-basic en caso de varias firmas, si existe algun certificado de firma vencido
			//como indica la iso 32000-1 se considera firma invalida, y retorna
			System.out.println(SignValiderFactory.getSignValider(pades).validate(pades).toJsonString());
				
			
		}		
	}
	
	
}

