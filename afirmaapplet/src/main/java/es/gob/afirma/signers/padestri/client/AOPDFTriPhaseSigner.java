/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.signers.padestri.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Logger;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.AOInvalidFormatException;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.core.signers.AOSignInfo;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.util.tree.AOTreeModel;

/** Firmador PAdES en tres fases.
 * Las firmas que genera no se etiquetan como ETSI, sino como "Adobe PKCS#7 Detached".
 * @author Tom&acute;s Garc&iacute;a-Mer&aacute;s */
public final class AOPDFTriPhaseSigner implements AOSigner {

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	/** Nombre de la propiedad de URL del servidor de firma trif&aacute;sica. */
	private static final String PROPERTY_NAME_SIGN_SERVER_URL = "serverUrl"; //$NON-NLS-1$

	private static final String PDF_FILE_HEADER = "%PDF-"; //$NON-NLS-1$
	private static final String PDF_FILE_SUFFIX = ".pdf"; //$NON-NLS-1$

	@Override
	public byte[] sign(final byte[] data,
			           final String algorithm,
			           final PrivateKey key,
			           final Certificate[] certChain,
			           final Properties extraParams) throws AOException {

		if (extraParams == null) {
			throw new IllegalArgumentException("Se necesitan parametros adicionales"); //$NON-NLS-1$
		}
		if (key == null) {
			throw new IllegalArgumentException("Es necesario proporcionar la clave privada de firma"); //$NON-NLS-1$
		}
		if (certChain == null || certChain.length == 0) {
			throw new IllegalArgumentException("Es necesario proporcionar el certificado de firma"); //$NON-NLS-1$
		}
		if (data == null) {
			throw new IllegalArgumentException("No se ha proporcionado el identificador de documento a firmar"); //$NON-NLS-1$
		}

		// Comprobamos la direccion del servidor
		final URL signServerUrl;
		try {
			signServerUrl = new URL(extraParams.getProperty(PROPERTY_NAME_SIGN_SERVER_URL));
		}
		catch (final Exception e) {
			throw new IllegalArgumentException("No se ha proporcionado una URL valida para el servidor de firma: " + extraParams.getProperty(PROPERTY_NAME_SIGN_SERVER_URL), e); //$NON-NLS-1$
		}

		// Decodificamos el identificador del documento
		final String documentId = Base64.encode(data, true);

		// ---------
		// PREFIRMA
		// ---------

		final byte[] preSignResult = PDFTriPhaseSignerUtil.doPresign(signServerUrl, algorithm, certChain, documentId, extraParams);

		try {
			LOGGER.info("Recibido el XML de prefirma PAdES:\n" + new String(Base64.decode(new String(preSignResult), true))); //$NON-NLS-1$
		}
		catch (final IOException e) {
			// Se ignora
		}

		// ----------
		// FIRMA
		// ----------

		final String preResultAsBase64 = Base64.encode(
			PDFTriPhaseSignerUtil.doSign(preSignResult, algorithm, key, certChain),
			true
		);

		// ---------
		// POSTFIRMA
		// ---------

		return PDFTriPhaseSignerUtil.doPostSign(preResultAsBase64, signServerUrl, algorithm, certChain, documentId, extraParams);


	}

	@Override
	public byte[] cosign(final byte[] data,
			final byte[] sign,
			final String algorithm,
			final PrivateKey key,
			final Certificate[] certChain,
			final Properties extraParams) throws AOException {
		return sign(sign, algorithm, key, certChain, extraParams);
	}

	@Override
	public byte[] cosign(final byte[] sign,
			final String algorithm,
			final PrivateKey key,
			final Certificate[] certChain,
			final Properties extraParams) throws AOException {
		return sign(sign, algorithm, key, certChain, extraParams);
	}

	@Override
	public byte[] countersign(final byte[] sign,
			final String algorithm,
			final CounterSignTarget targetType,
			final Object[] targets,
			final PrivateKey key,
			final Certificate[] certChain,
			final Properties extraParams) throws AOException {
		throw new UnsupportedOperationException("No se soportan contrafirmas en PAdES"); //$NON-NLS-1$
	}

	@Override
	public AOTreeModel getSignersStructure(final byte[] sign,
			final boolean asSimpleSignInfo) {
		throw new UnsupportedOperationException("No soportado para firmas trifasicas"); //$NON-NLS-1$
	}

	@Override
	public boolean isSign(final byte[] data) {
		return false;
	}

	@Override
	public boolean isValidDataFile(final byte[] data) {
		if (data == null) {
			LOGGER.warning("Se han introducido datos nulos para su comprobacion"); //$NON-NLS-1$
			return false;
		}
		return isPdfFile(data);
	}

	@Override
	public String getSignedName(final String originalName, final String inText) {
		final String inTextInt = inText != null ? inText : ""; //$NON-NLS-1$
		if (originalName == null) {
			return "signed.pdf"; //$NON-NLS-1$
		}
		if (originalName.toLowerCase(Locale.ENGLISH).endsWith(PDF_FILE_SUFFIX)) {
			return originalName.substring(0, originalName.length() - PDF_FILE_SUFFIX.length()) + inTextInt + PDF_FILE_SUFFIX;
		}
		return originalName + inTextInt + PDF_FILE_SUFFIX;
	}

	@Override
	public byte[] getData(final byte[] sign) throws AOException {
		// Si no es una firma PDF valida, lanzamos una excepcion
		if (!isSign(sign)) {
			throw new AOInvalidFormatException("El documento introducido no contiene una firma valida"); //$NON-NLS-1$
		}
		return sign;
	}

	@Override
	public AOSignInfo getSignInfo(final byte[] data) throws AOException {
		if (data == null) {
			throw new IllegalArgumentException("No se han introducido datos para analizar"); //$NON-NLS-1$
		}

		if (!isSign(data)) {
			throw new AOInvalidFormatException("Los datos introducidos no se corresponden con un objeto de firma"); //$NON-NLS-1$
		}

		return new AOSignInfo(AOSignConstants.SIGN_FORMAT_PDF);
		// Aqui podria venir el analisis de la firma buscando alguno de los
		// otros datos de relevancia que se almacenan en el objeto AOSignInfo
	}

	private static boolean isPdfFile(final byte[] data) {
		byte[] buffer = new byte[PDF_FILE_HEADER.length()];
		try {
			new ByteArrayInputStream(data).read(buffer);
		}
		catch (final Exception e) {
			buffer = null;
		}
		// Comprobamos que cuente con una cabecera PDF
		if (buffer != null && !PDF_FILE_HEADER.equals(new String(buffer))) {
			return false;
		}
		return true;
	}

}
