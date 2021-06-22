/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.signers.multi.cades;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.MimeHelper;
import es.gob.afirma.core.signers.AOCoSigner;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AdESPolicy;
import es.gob.afirma.signers.cades.AOCAdESSigner;
import es.gob.afirma.signers.cades.CAdESExtraParams;
import es.gob.afirma.signers.cades.CAdESSignerMetadataHelper;
import es.gob.afirma.signers.cades.CommitmentTypeIndicationsHelper;
import es.gob.afirma.signers.pkcs7.P7ContentSignerParameters;

/** Operaciones de cofirma CAdES. */
public final class AOCAdESCoSigner implements AOCoSigner {


	private static final String ALGORITHM = "SHA1"; //$NON-NLS-1$

	/** {@inheritDoc} */
	@Override
	public byte[] cosign(final byte[] data,
                         final byte[] sign,
                         final String algorithm,
                         final PrivateKey key,
                         final java.security.cert.Certificate[] certChain,
                         final Properties xParams) throws AOException, IOException {

        CAdESMultiUtil.checkUnsupportedAttributes(sign);

    	//****************************************************************************************************
    	//*************** LECTURA PARAMETROS ADICIONALES *****************************************************

        final Properties extraParams = xParams != null ? xParams : new Properties();

        final boolean onlySigningCertificate = Boolean.parseBoolean(extraParams.getProperty(CAdESExtraParams.INCLUDE_ONLY_SIGNNING_CERTIFICATE));

        byte[] messageDigest = null;
        final String precalculatedDigest = extraParams.getProperty(CAdESExtraParams.PRECALCULATED_HASH_ALGORITHM);
        if (precalculatedDigest != null) {
            messageDigest = data;
        }

        boolean signingCertificateV2;
        if (AOSignConstants.isSHA2SignatureAlgorithm(algorithm)) {
        	signingCertificateV2 = true;
        }
        else if (extraParams.containsKey(CAdESExtraParams.SIGNING_CERTIFICATE_V2)) {
        	signingCertificateV2 = Boolean.parseBoolean(extraParams.getProperty(CAdESExtraParams.SIGNING_CERTIFICATE_V2));
        }
        else {
        	signingCertificateV2 = !ALGORITHM.equals(AOSignConstants.getDigestAlgorithmName(algorithm));
        }

        final String contentTypeOid = extraParams.getProperty(CAdESExtraParams.CONTENT_TYPE_OID);
        final String contentDescription = extraParams.getProperty(CAdESExtraParams.CONTENT_DESCRIPTION);

        //*************** FIN LECTURA PARAMETROS ADICIONALES *************************************************
    	//****************************************************************************************************

        final P7ContentSignerParameters csp = new P7ContentSignerParameters(
    		data,
    		algorithm
		);

        String altContentTypeOid = MimeHelper.DEFAULT_CONTENT_OID_DATA;
        String altContentDescription = MimeHelper.DEFAULT_CONTENT_DESCRIPTION;
		if (data != null) {
			try {
				final MimeHelper mimeHelper = new MimeHelper(data);
				altContentTypeOid = MimeHelper.transformMimeTypeToOid(mimeHelper.getMimeType());
				altContentDescription = mimeHelper.getDescription();
			}
			catch (final Exception e) {
				Logger.getLogger("es.gob.afirma").warning( //$NON-NLS-1$
					"No se han podido cargar las librerias para identificar el tipo de dato firmado: " + e //$NON-NLS-1$
				);
			}
		}

		try {
			final String mode = extraParams.getProperty(CAdESExtraParams.MODE, AOSignConstants.DEFAULT_SIGN_MODE);
			final boolean omitContent = mode.equals(AOSignConstants.SIGN_MODE_EXPLICIT) || precalculatedDigest != null;

			return new CAdESCoSigner().coSigner(
				csp,
				sign,
				omitContent,
				AdESPolicy.buildAdESPolicy(extraParams),
				signingCertificateV2,
				key,
				onlySigningCertificate ? new X509Certificate[] { (X509Certificate) certChain[0] } : certChain,
				messageDigest,
				contentTypeOid != null ? contentTypeOid : altContentTypeOid,
                contentDescription != null ? contentDescription : altContentDescription,
				CommitmentTypeIndicationsHelper.getCommitmentTypeIndications(extraParams),
				Boolean.parseBoolean(extraParams.getProperty(CAdESExtraParams.INCLUDE_SIGNING_TIME_ATTRIBUTE, "false")), //$NON-NLS-1$
				CAdESSignerMetadataHelper.getCAdESSignerMetadata(extraParams)
			);

		}
		catch (final Exception e) {
			throw new AOException("Error generando la Cofirma CAdES: " + e, e); //$NON-NLS-1$
		}

    }

    /** {@inheritDoc} */
	@Override
	public byte[] cosign(final byte[] sign,
                         final String algorithm,
                         final PrivateKey key,
                         final java.security.cert.Certificate[] certChain,
                         final Properties xParams) throws AOException, IOException {

        CAdESMultiUtil.checkUnsupportedAttributes(sign);

        final Properties extraParams = xParams != null ? xParams : new Properties();

        final boolean onlySigningCertificate = Boolean.parseBoolean(extraParams.getProperty(
    		CAdESExtraParams.INCLUDE_ONLY_SIGNNING_CERTIFICATE)
		);

        boolean signingCertificateV2;
        if (AOSignConstants.isSHA2SignatureAlgorithm(algorithm)) {
        	signingCertificateV2 = true;
        }
        else if (extraParams.containsKey(CAdESExtraParams.SIGNING_CERTIFICATE_V2)) {
        	signingCertificateV2 = Boolean.parseBoolean(extraParams.getProperty(CAdESExtraParams.SIGNING_CERTIFICATE_V2));
        }
        else {
        	signingCertificateV2 = !ALGORITHM.equals(AOSignConstants.getDigestAlgorithmName(algorithm));
        }

        // algoritmo de firma.
        final String typeAlgorithm = algorithm;

        String contentTypeOid = MimeHelper.DEFAULT_CONTENT_OID_DATA;
        String contentDescription = MimeHelper.DEFAULT_CONTENT_DESCRIPTION;
        final byte[] data = new AOCAdESSigner().getData(sign);
        if (data != null) {
        	final MimeHelper mimeHelper = new MimeHelper(data);
			contentDescription = mimeHelper.getDescription();
			contentTypeOid = MimeHelper.transformMimeTypeToOid(mimeHelper.getMimeType());
        }

        try {
			return new CAdESCoSigner().coSigner(
			    typeAlgorithm,
			    onlySigningCertificate ?
					new X509Certificate[] { (X509Certificate) certChain[0] } :
						(X509Certificate[]) certChain,
			    new ByteArrayInputStream(sign),
			    AdESPolicy.buildAdESPolicy(extraParams),
			    signingCertificateV2,
			    key,
			    certChain,
			    null, // null porque no nos pueden dar un hash en este metodo, tendria que ser en el que incluye datos
			    contentTypeOid,
			    contentDescription,
			    CommitmentTypeIndicationsHelper.getCommitmentTypeIndications(extraParams),
			    Boolean.parseBoolean(extraParams.getProperty(CAdESExtraParams.INCLUDE_SIGNING_TIME_ATTRIBUTE, "false")), //$NON-NLS-1$
			    CAdESSignerMetadataHelper.getCAdESSignerMetadata(extraParams)
			);
		}
        catch (final Exception e) {
            throw new AOException("Error generando la Cofirma CADES: " + e, e); //$NON-NLS-1$
        }

    }
}
