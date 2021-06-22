package es.gob.afirma.signers.multi.cades;

import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.SignedData;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;

import es.gob.afirma.core.AOFormatFileException;
import es.gob.afirma.signers.pkcs7.SigUtils;

final class CAdESMultiUtil {

	private static final ASN1ObjectIdentifier ARCHIVE_TIMESTAMP_V2_OID = new ASN1ObjectIdentifier(
		"1.2.840.113549.1.9.16.2.48" //$NON-NLS-1$
	);

	private static final List<ASN1ObjectIdentifier> UNSUPPORTED_ATTRIBUTES = new ArrayList<ASN1ObjectIdentifier>(8);
	static {
		UNSUPPORTED_ATTRIBUTES.add(ARCHIVE_TIMESTAMP_V2_OID);
		UNSUPPORTED_ATTRIBUTES.add(PKCSObjectIdentifiers.id_aa_signatureTimeStampToken);
		UNSUPPORTED_ATTRIBUTES.add(PKCSObjectIdentifiers.id_aa_ets_revocationRefs);
		UNSUPPORTED_ATTRIBUTES.add(PKCSObjectIdentifiers.id_aa_ets_archiveTimestamp);
		UNSUPPORTED_ATTRIBUTES.add(PKCSObjectIdentifiers.id_aa_ets_certificateRefs);
		UNSUPPORTED_ATTRIBUTES.add(PKCSObjectIdentifiers.id_aa_ets_revocationValues);
		UNSUPPORTED_ATTRIBUTES.add(PKCSObjectIdentifiers.id_aa_ets_certValues);
		UNSUPPORTED_ATTRIBUTES.add(PKCSObjectIdentifiers.id_aa_ets_escTimeStamp);
	}

	private CAdESMultiUtil() {
		// No instanciable
	}

	static ASN1Set addCertificates(final SignedData sd,
			                       final java.security.cert.Certificate[] certChain) throws CertificateEncodingException,
			                                                                                IOException {
		ASN1Set certificates = null;

		final ASN1Set certificatesSigned = sd.getCertificates();
		final ASN1EncodableVector vCertsSig = new ASN1EncodableVector();
		final Enumeration<?> certs = certificatesSigned.getObjects();

		// COJEMOS LOS CERTIFICADOS EXISTENTES EN EL FICHERO
		while (certs.hasMoreElements()) {
			vCertsSig.add((ASN1Encodable) certs.nextElement());
		}

		// Y ANADIMOS LOS DE LA NUEVA CADENA
		if (certChain.length != 0) {
			final List<ASN1Encodable> ce = new ArrayList<ASN1Encodable>();
			for (final java.security.cert.Certificate element : certChain) {
				ce.add(Certificate.getInstance(ASN1Primitive.fromByteArray(element.getEncoded())));
			}
			certificates = SigUtils.fillRestCerts(ce, vCertsSig);
		}
		return certificates;
	}

	static void checkUnsupportedAttributes(final byte[] signedDataBytes) throws AOFormatFileException, IOException {
		final CMSSignedData signedData;
		try {
			signedData = new CMSSignedData(signedDataBytes);
		}
		catch (final CMSException e) {
			throw new IOException(
				"La firma proporcionada no es un SignedData compatible CMS: " + e, e //$NON-NLS-1$
			);
		}
		checkUnsupportedAttributes(signedData);
	}

	private static void checkUnsupportedAttributes(final CMSSignedData signedData) throws AOFormatFileException {
		checkUnsupportedAttributes(
			signedData.getSignerInfos().getSigners().iterator().next()
		);
	}

    private static void checkUnsupportedAttributes(final SignerInformation signerInformation) throws AOFormatFileException {
    	final AttributeTable at = signerInformation.getUnsignedAttributes();
    	if (at != null) {
    		for (final ASN1ObjectIdentifier unsupOid : UNSUPPORTED_ATTRIBUTES) {
    			if (at.get(unsupOid) != null) {
        			throw new AOFormatFileException(
    					"No se soportan multifirmas de firmas con sellos de tiempo o atributos longevos (encontrado OID=" + unsupOid + ")" //$NON-NLS-1$ //$NON-NLS-2$
    				);
    			}
    		}
    	}
    }

    static void checkUnsupported(final ASN1ObjectIdentifier oid) throws AOFormatFileException {
    	for (final ASN1ObjectIdentifier unsupOid : UNSUPPORTED_ATTRIBUTES) {
    		if (unsupOid.equals(oid)) {
    			throw new AOFormatFileException(
					"No se soportan multifirmas de firmas con sellos de tiempo o atributos longevos (encontrado OID=" + oid + ")" //$NON-NLS-1$ //$NON-NLS-2$
				);
    		}
    	}
    }
}
