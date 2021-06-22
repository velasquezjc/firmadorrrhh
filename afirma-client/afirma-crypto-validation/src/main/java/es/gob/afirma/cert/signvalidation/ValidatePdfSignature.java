/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.cert.signvalidation;

import java.io.IOException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.aowagie.text.pdf.AcroFields;
import com.aowagie.text.pdf.PdfDictionary;
import com.aowagie.text.pdf.PdfName;
import com.aowagie.text.pdf.PdfPKCS7;
import com.aowagie.text.pdf.PdfReader;

import es.gob.afirma.cert.signvalidation.SignValidity.SIGN_DETAIL_TYPE;
import es.gob.afirma.cert.signvalidation.SignValidity.VALIDITY_ERROR;

/** Validador de firmas PDF.
 * Se validan los certificados en local revisando las fechas de validez de los certificados.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s 
 * Update: gov.ar*/
public final class ValidatePdfSignature implements SignValider{

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private static final PdfName PDFNAME_ETSI_RFC3161 = new PdfName("ETSI.RFC3161"); //$NON-NLS-1$
	private static final PdfName PDFNAME_DOCTIMESTAMP = new PdfName("DocTimeStamp"); //$NON-NLS-1$

	/** Valida una firma PDF (PKCS#7/PAdES). 
	 * Se validan los certificados en local revisando las fechas de validez de los certificados.
     * @param sign PDF firmado.
     * @return Validez de la firma.
     * @throws IOException Si ocurren problemas relacionados con la lectura de la firma. */
	@Override
	public SignValidity validate(final byte[] sign) throws IOException {
		final PdfReader reader = new PdfReader(sign);
		final AcroFields af = reader.getAcroFields();
		final List<String> sigNames = af.getSignatureNames();
		for (final String name : sigNames) {
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
					
					//Verifico que el digesto del documento machee con el de la firma como se especifica en la iso 32000-1,clausula 12.8.1
					//Si bien para PKCS#7 y CAdES el campo digest en contents se ubica en el mismo
					//lugar(ambos comparten ciertos atributos comunes)quizas sea coveniente
					//crear una clase propia para cades???????
					//ver si en cades hay que verificar otros atributos
					if (!pk.verify()) {
						return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.NO_MATCH_DATA);
					}
				}
				catch (final Exception e) {
					LOGGER.warning("Error validando la firma '" + name + "' del PDF: " + e); //$NON-NLS-1$ //$NON-NLS-2$
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
				LOGGER.info("El certificado usado ha expirado: " + e); //$NON-NLS-1$
	            return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.CERTIFICATE_EXPIRED);
			}
			catch (final CertificateNotYetValidException e) {
				// Certificado aun no valido
				LOGGER.info("El certificado usado todavia no es valido: " + e); //$NON-NLS-1$
	            return new SignValidity(SIGN_DETAIL_TYPE.KO, VALIDITY_ERROR.CERTIFICATE_NOT_VALID_YET);
			}
		}
		return new SignValidity(SIGN_DETAIL_TYPE.OK, null);
	}


	@Override
	public ArrayList<X509Certificate> getCertsFirmantes() {
		// TODO FIX this
		return new ArrayList<X509Certificate>();
	}

}
