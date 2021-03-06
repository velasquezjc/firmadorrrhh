package es.gob.jmulticard.card.pace;

import java.io.IOException;
import java.security.KeyPair;

import es.gob.jmulticard.CryptoHelper;
import es.gob.jmulticard.CryptoHelper.EcCurve;
import es.gob.jmulticard.HexUtils;
import es.gob.jmulticard.apdu.CommandApdu;
import es.gob.jmulticard.apdu.ResponseApdu;
import es.gob.jmulticard.apdu.connection.ApduConnection;
import es.gob.jmulticard.apdu.connection.ApduConnectionException;
import es.gob.jmulticard.apdu.iso7816four.GeneralAuthenticateApduCommand;
import es.gob.jmulticard.apdu.iso7816four.pace.MseSetPaceAlgorithmApduCommand;
import es.gob.jmulticard.asn1.Tlv;
import es.gob.jmulticard.asn1.TlvException;
import es.gob.jmulticard.asn1.der.x509.SubjectPublicKeyInfo;

/** Utilidades para el establecimiento de un canal <a href="https://www.bsi.bund.de/EN/Publications/TechnicalGuidelines/TR03110/BSITR03110.html">PACE</a>
 * (Password Authenticated Connection Establishment).
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s. */
public final class PaceChannelHelper {

	private static final byte[] CAN_PADDING = new byte[] {
		(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03
	};

	private static final byte TAG_DYNAMIC_AUTHENTICATION_DATA = (byte) 0x7C;
	private static final byte TAG_MAPPING_DATA = (byte) 0x81;
	private static final byte UNCOMPRESSED_POINT = (byte) 0x04;


	private PaceChannelHelper() {
		// No instanciable
	}

	/** Abre un canal PACE mediante el CAN (<i>Card Access Number</i>).
	 * @param cla Clase de APDU para los comandos de establecimiento de canal.
	 * @param can CAN (<i>Card Access Number</i>).
	 * @param conn Conexi&oacute;n hacia la tarjeta inteligente.
	 * @param cryptoHelper Clase para la realizaci&oacute;n de operaciones criptogr&aacute;ficas auxiliares.
	 * @throws ApduConnectionException Si hay problemas de conexi&oacute;n con la tarjeta.
	 * @throws PaceException Si hay problemas en la apertura del canal. */
	public static void openPaceChannel(final byte cla,
			                           final String can,
			                           final ApduConnection conn,
			                           final CryptoHelper cryptoHelper) throws ApduConnectionException,
			                                                                   PaceException {
		if (conn == null) {
			throw new IllegalArgumentException(
				"El canal de conexion no puede ser nulo" //$NON-NLS-1$
			);
		}
		if (can == null || "".equals(can)) { //$NON-NLS-1$
			throw new IllegalArgumentException(
				"Es necesario proporcionar el CAN para abrir canal PACE" //$NON-NLS-1$
			);
		}
		if (cryptoHelper == null) {
			throw new IllegalArgumentException(
				"El CryptoHelper no puede ser nulo" //$NON-NLS-1$
			);
		}

		if (!conn.isOpen()) {
			conn.open();
		}

		ResponseApdu res;
		CommandApdu comm;

		// 1.3.2 - Establecemos el algoritmo para PACE

		System.out.println("Establecimiento algoritmo PACE");

		comm = new MseSetPaceAlgorithmApduCommand(
			cla,
			MseSetPaceAlgorithmApduCommand.PaceAlgorithmOid.PACE_ECDH_GM_AES_CBC_CMAC128,
			MseSetPaceAlgorithmApduCommand.PacePasswordType.CAN,
			MseSetPaceAlgorithmApduCommand.PaceAlgorithmParam.BRAINPOOL_256_R1
		);
		res = conn.transmit(comm);

		if (!res.isOk()) {
			throw new PaceException(
				res.getStatusWord(),
				comm,
				"Error estableciendo el algoritmo del protocolo PACE." //$NON-NLS-1$
			);
		}

		// 1.3.3 - Primer comando General Autenticate - Get Nonce

		System.out.println("Primer comando General Autenticate - Get Nonce");

		comm = new GeneralAuthenticateApduCommand(
			(byte) 0x10,
			new byte[] { (byte) 0x7C, (byte) 0x00 }
		);
		res = conn.transmit(comm);

		if (!res.isOk()) {
			throw new PaceException(
				res.getStatusWord(),
				comm,
				"Error solicitando el aleatorio de calculo PACE (Nonce)" //$NON-NLS-1$
			);
		}

		final byte[] nonce;
		try {
			nonce = new Tlv(new Tlv(res.getData()).getValue()).getValue();
		}
		catch (final TlvException e) {
			throw new PaceException(
				"El aleatorio de calculo PACE (Nonce) obtenido (" + HexUtils.hexify(res.getData(), true) + ") no sigue el formato esperado: " + e, e //$NON-NLS-1$ //$NON-NLS-2$
			);
		}

		System.out.println("'nonce' obtenido: " + HexUtils.hexify(nonce, false));

		final byte[] sk = new byte[16];
		try {
			System.arraycopy(
				cryptoHelper.digest(
					CryptoHelper.DigestAlgorithm.SHA1,
					HexUtils.concatenateByteArrays(
						can.getBytes(),
						CAN_PADDING
					)
				),
				0,
				sk,
				0,
				16
			);
		}
		catch (final IOException e) {
			throw new PaceException(
				"Error obteniendo el 'sk' a partir del CAN: " + e, e //$NON-NLS-1$
			);
		}

		System.out.println("'sk' obtenido: " + HexUtils.hexify(sk, false));

		final byte[] secret;
		try {
			secret = cryptoHelper.aesDecrypt(
				nonce,
				new byte[0],
				sk
			);
		}
		catch (final Exception e) {
			throw new PaceException(
				"Error descifranco el 'nonce': " + e, e //$NON-NLS-1$
			);
		}

		System.out.println("'secret' obtenido: " + HexUtils.hexify(secret, false));

		// 1.3.4 - Segundo comando General Autenticate - Map Nonce

		System.out.println("Segundo comando General Autenticate - Map Nonce");

		// Generamos un par de claves efimeras EC para el DH
		final KeyPair ifdDh1;
		try {
			ifdDh1 = cryptoHelper.generateEcKeyPair(EcCurve.BRAINPOOL_P256_R1);
		}
		catch (final Exception e) {
			throw new PaceException(
				"Error creando el par de claves EC: " + e, e //$NON-NLS-1$
			);
		}

		// Codificamos la parte publica...
		final SubjectPublicKeyInfo ecPuk = new SubjectPublicKeyInfo();
		try {
			ecPuk.setDerValue(
				ifdDh1.getPublic().getEncoded()
			);
		}
		catch (final Exception e) {
			throw new PaceException(
				"La clave publica EC no esta en el formato esperado: " + e, //$NON-NLS-1$
				e
			);
		}

		// ... La metemos en un TLV de autenticacion ...
		final Tlv tlv = new Tlv(
			TAG_DYNAMIC_AUTHENTICATION_DATA,
			new Tlv(
				TAG_MAPPING_DATA,
				ecPuk.getSubjectPublicKey()
			).getBytes()
		);

		// ... Y la enviamos a la tarjeta
		comm = new GeneralAuthenticateApduCommand(
			(byte) 0x10,
			tlv.getBytes()
		);

		res = conn.transmit(comm);

		if (!res.isOk()) {
			throw new PaceException(
				res.getStatusWord(),
				comm,
				"Error mapeando el aleatorio de calculo PACE (Nonce)" //$NON-NLS-1$
			);
		}

		System.out.println(
			"Clave privada del terminal (PKCS#8, " +
				ifdDh1.getPrivate().getEncoded().length + "):  " + HexUtils.hexify(ifdDh1.getPrivate().getEncoded(), false));

		// Obtengo la clave publica de la tarjeta

		final byte[] pukIccDh1;
		try {
			pukIccDh1 = unwrapEcKey(res.getData());
		}
		catch(final Exception e) {
			throw new PaceException(
				"Error obteniendo la clave efimera EC publica de la tarjeta: " + e, e //$NON-NLS-1$
			);
		}

		System.out.println("Clave publica de la tarjeta (sin TLV, " + pukIccDh1.length + "): " + HexUtils.hexify(
			pukIccDh1,
				false
		));

		final byte[] h;
		try {
			h = cryptoHelper.doEcDh(ifdDh1.getPrivate(), pukIccDh1, EcCurve.BRAINPOOL_P256_R1);
		}
		catch (final Exception e) {
			throw new PaceException(
				"Error calculando el EC-DH: " + e, e //$NON-NLS-1$
			);
		}

		System.out.println("h de ECDH: " + HexUtils.hexify(h, false));

	}

	private static byte[] unwrapEcKey(final byte[] key) throws TlvException {
		return new Tlv(new Tlv(key).getValue()).getValue();
	}



}
