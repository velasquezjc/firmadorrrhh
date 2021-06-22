package es.gob.afirma.triphase.signer.processors;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.core.signers.TriphaseData;
import es.gob.afirma.core.signers.asic.ASiCUtil;

/** Procesador de firmas trif&aacute;sicas CAdES-ASiC-S.
 * @author Tom&aacute;s Garc&iacute;a Mer&aacute;s. */
public final class CAdESASiCSTriPhasePreProcessor extends CAdESTriPhasePreProcessor {

	@Override
	public TriphaseData preProcessPreSign(final byte[] data,
			                        final String algorithm,
			                        final X509Certificate[] cert,
			                        final Properties xParams) throws IOException,
			                                                         AOException {

		final Properties extraParams = xParams != null ? xParams : new Properties();
		extraParams.put("mode", "explicit"); //$NON-NLS-1$ //$NON-NLS-2$

		return super.preProcessPreSign(data, algorithm, cert, extraParams);
	}

	@Override
	public byte[] preProcessPostSign(final byte[] data,
			                         final String algorithm,
			                         final X509Certificate[] cert,
			                         final Properties xParams,
			                         final byte[] triphaseDataBytes) throws NoSuchAlgorithmException,
			                                                                AOException,
			                                                                IOException {

		return preProcessPostSign(data, algorithm, cert, xParams, TriphaseData.parser(triphaseDataBytes));
	}

	@Override
	public byte[] preProcessPostSign(final byte[] data,
			                         final String algorithm,
			                         final X509Certificate[] cert,
			                         final Properties xParams,
			                         final TriphaseData triphaseData) throws NoSuchAlgorithmException,
			                                                                AOException,
			                                                                IOException {

		final Properties extraParams = xParams != null ? xParams : new Properties();
		extraParams.put("mode", "explicit"); //$NON-NLS-1$ //$NON-NLS-2$

		final byte[] signature = super.preProcessPostSign(data, algorithm, cert, extraParams, triphaseData);
		return ASiCUtil.createSContainer(
			signature,
			data,
			ASiCUtil.ENTRY_NAME_BINARY_SIGNATURE,
			extraParams.getProperty("asicsFilename") //$NON-NLS-1$
		);
	}

	@Override
	public TriphaseData preProcessPreCoSign(final byte[] data,
			                          final String algorithm,
			                          final X509Certificate[] cert,
			                          final Properties xParams) throws IOException, AOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] preProcessPostCoSign(final byte[] sign,
			                           final String algorithm,
			                           final X509Certificate[] cert,
			                           final Properties xParams,
			                           final byte[] session) throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] preProcessPostCoSign(final byte[] sign,
			                           final String algorithm,
			                           final X509Certificate[] cert,
			                           final Properties xParams,
			                           final TriphaseData triphaseData) throws NoSuchAlgorithmException, AOException, IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public TriphaseData preProcessPreCounterSign(final byte[] sign,
			                               final String algorithm,
			                               final X509Certificate[] cert,
			                               final Properties extraParams,
			                               final CounterSignTarget targets) throws IOException, AOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] preProcessPostCounterSign(final byte[] sign,
			                                final String algorithm,
			                                final X509Certificate[] cert,
			                                final Properties extraParams,
			                                final byte[] session,
			                                final CounterSignTarget targetType) throws NoSuchAlgorithmException,
			                                                                           AOException,
			                                                                           IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] preProcessPostCounterSign(final byte[] sign,
			                                final String algorithm,
			                                final X509Certificate[] cert,
			                                final Properties extraParams,
			                                final TriphaseData triphaseData,
			                                final CounterSignTarget targetType) throws NoSuchAlgorithmException,
			                                                                           AOException,
			                                                                           IOException {
		throw new UnsupportedOperationException();
	}
}
