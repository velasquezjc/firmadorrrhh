/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.envelopers.cms;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.cms.EncryptedContentInfo;
import org.spongycastle.asn1.cms.EnvelopedData;
import org.spongycastle.asn1.x509.AlgorithmIdentifier;

import es.gob.afirma.core.AOException;

/** Clase que descifra el contenido de un fichero en formato EnvelopedData. de
 * CMS.
 * Se usa para ello una clave del usuario. */
public final class CMSDecipherEnvelopData {

	private CMSDecipherEnvelopData() {
		// No permitimos la instanciacion
	}

    /** &Eacute;ste m&eacute;todo descifra el contenido de un CMS EnvelopedData.
     * @param cmsData
     *        Flujo de lectura de los datos que contienen el tipo
     *        EnvelopedData para obtener los datos cifrados.
     * @param keyEntry
     *        Clave privada del certificado usado para descifrar el
     *        contenido.
     * @return El contenido descifrado del EnvelopedData.
     * @throws java.io.IOException
     *         Si ocurre alg&uacute;n problema leyendo o escribiendo los
     *         datos
     * @throws java.security.cert.CertificateEncodingException
     *         Si se produce alguna excepci&oacute;n con los certificados de
     *         firma.
     * @throws AOException
     *         Cuando ocurre un error durante el proceso de descifrado
     *         (formato o clave incorrecto,...)
     * @throws AOInvalidRecipientException
     *         Cuando se indica un certificado que no est&aacute; entre los
     *         destinatarios del sobre.
     * @throws InvalidKeyException
     *         Cuando la clave almacenada en el sobre no es v&aacute;lida.
     * @throws NoSuchPaddingException Cuando no se soporta un tipo de relleno necesario.
     * @throws NoSuchAlgorithmException Si el JRE no soporta alg&uacute;n algoritmo necesario
     * @throws BadPaddingException Cuando hay problemas con un relleno de datos.
     * @throws IllegalBlockSizeException Cuando hay problemas internos con los tama&ntilde;os de bloque de cifrado.
     * @throws InvalidAlgorithmParameterException Si no se soporta un par&aacute;metro necesario para un algoritmo.
     * @throws Pkcs11WrapOperationException Cuando se produce un error derivado del uso del PKCS#11
     * 			de un dispositivo criptogr&aacute;fico.  */
    public static byte[] dechiperEnvelopData(final byte[] cmsData,
    		                                 final PrivateKeyEntry keyEntry) throws IOException,
                                                                                    CertificateEncodingException,
                                                                                    AOException,
                                                                                    InvalidKeyException,
                                                                                    NoSuchAlgorithmException,
                                                                                    NoSuchPaddingException,
                                                                                    InvalidAlgorithmParameterException,
                                                                                    IllegalBlockSizeException,
                                                                                    BadPaddingException,
                                                                                    Pkcs11WrapOperationException {

        // Contendra el contenido a tratar.
        EnvelopedData enveloped = null;
        Enumeration<?> elementRecipient;

        try {
            // Contenido de EnvelopedData
            final ASN1Sequence contentEnvelopedData = Utils.fetchWrappedData(cmsData);

            enveloped = EnvelopedData.getInstance(contentEnvelopedData);
            elementRecipient = enveloped.getRecipientInfos().getObjects();
        }
        catch (final Exception ex) {
            throw new AOException("El fichero no contiene un tipo EnvelopedData", ex); //$NON-NLS-1$
        }

        final EncryptedKeyDatas encryptedKeyDatas = Utils.fetchEncryptedKeyDatas((X509Certificate) keyEntry.getCertificate(), elementRecipient);

        // Obtenemos el contenido cifrado
        final EncryptedContentInfo contenidoCifrado = enveloped.getEncryptedContentInfo();

        // Obtenemos el algoritmo usado para cifrar la clave generada.
        final AlgorithmIdentifier algClave = contenidoCifrado.getContentEncryptionAlgorithm();

        // Asignamos la clave de descifrado del contenido.
        final KeyAsigned keyAsigned = Utils.assignKey(encryptedKeyDatas.getEncryptedKey(), keyEntry, algClave);

        // Desciframos el contenido.
        return Utils.deCipherContent(
                 contenidoCifrado.getEncryptedContent().getOctets(),
                 keyAsigned.getConfig(),
                 keyAsigned.getCipherKey()
        );
    }
}
