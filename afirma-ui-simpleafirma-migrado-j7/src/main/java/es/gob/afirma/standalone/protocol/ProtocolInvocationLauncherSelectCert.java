package es.gob.afirma.standalone.protocol;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.logging.Logger;

import javax.security.auth.callback.PasswordCallback;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.protocol.UrlParametersToSelectCert;
import es.gob.afirma.keystores.AOCertificatesNotFoundException;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreDialog;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.filters.CertFilterManager;
import es.gob.afirma.keystores.filters.CertificateFilter;
import es.gob.afirma.standalone.crypto.CypherDataManager;

final class ProtocolInvocationLauncherSelectCert {

	private static final String RESULT_CANCEL = "CANCEL"; //$NON-NLS-1$

	private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private ProtocolInvocationLauncherSelectCert() {
		// No instanciable
	}

	static String processSelectCert(final UrlParametersToSelectCert options, final boolean bySocket) throws SocketOperationException {

		if (options == null) {
			LOGGER.severe("Las opciones de firma son nulas"); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.SAF_01);
			if (!bySocket){
				throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.SAF_01);
			}
			return ProtocolInvocationLauncherErrorManager.getErrorMessage(ProtocolInvocationLauncherErrorManager.SAF_01);
		}

		if (!ProtocolInvocationLauncher.MAX_PROTOCOL_VERSION_SUPPORTED.support(options.getMinimumVersion())) {
			LOGGER.severe(String.format("Version de protocolo no soportada (%1s). Version actual: %s2. Hay que actualizar la aplicacion.", options.getMinimumVersion(), ProtocolInvocationLauncher.MAX_PROTOCOL_VERSION_SUPPORTED)); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.SAF_21);
			return ProtocolInvocationLauncherErrorManager.getErrorMessage(ProtocolInvocationLauncherErrorManager.SAF_21);
		}

		final AOKeyStore aoks = AOKeyStore.valueOf(options.getDefaultKeyStore());
		if (aoks == null) {
			LOGGER.severe("No hay un KeyStore con el nombre: " + options.getDefaultKeyStore()); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.SAF_07);
			if (!bySocket){
				throw new SocketOperationException(ProtocolInvocationLauncherErrorManager.SAF_07);
			}
			return ProtocolInvocationLauncherErrorManager.getErrorMessage(ProtocolInvocationLauncherErrorManager.SAF_07);
		}

		final String aoksLib = options.getDefaultKeyStoreLib();

		final PasswordCallback pwc = aoks.getStorePasswordCallback(null);
		final AOKeyStoreManager ksm;
		try {
			ksm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
				aoks, // Store
				aoksLib, // Lib
				null, // Description
				pwc,  // PasswordCallback
				null  // Parent
			);
		}
		catch (final Exception e3) {
			LOGGER.severe("Error obteniendo el AOKeyStoreManager: " + e3); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(
				ProtocolInvocationLauncherErrorManager.SAF_08
			);
			if (!bySocket){
				throw new SocketOperationException(
					ProtocolInvocationLauncherErrorManager.SAF_08
				);
			}
			return ProtocolInvocationLauncherErrorManager.getErrorMessage(
					ProtocolInvocationLauncherErrorManager.SAF_08
				);
		}

		LOGGER.info("Obtenido gestor de almacenes de claves: " + ksm); //$NON-NLS-1$

		final CertFilterManager filterManager = new CertFilterManager(options.getExtraParams());
		final List<CertificateFilter> filters = filterManager.getFilters();
		final boolean mandatoryCertificate = filterManager.isMandatoryCertificate();
		final PrivateKeyEntry pke;

		LOGGER.info("Cargando dialogo de seleccion de certificados..."); //$NON-NLS-1$

		try {
			ServiceInvocationManager.focusApplication();
			final AOKeyStoreDialog dialog = new AOKeyStoreDialog(
				ksm,
				null,
				true, // checkPrivateKeys
				true, // showExpiredCertificates
				true, // checkValidity
				filters,
				mandatoryCertificate
			);
			dialog.allowOpenExternalStores(filterManager.isExternalStoresOpeningAllowed());
			dialog.show();
			pke = ksm.getKeyEntry(
				dialog.getSelectedAlias()
			);
		}
		catch (final AOCancelledOperationException e) {
			LOGGER.severe("Operacion cancelada por el usuario" + e); //$NON-NLS-1$
			if (!bySocket){
				throw new SocketOperationException(getResultCancel());
			}
			return getResultCancel();
		}
		catch(final AOCertificatesNotFoundException e) {
			LOGGER.severe("No hay certificados validos en el almacen: " + e); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.SAF_19);
			if (!bySocket){
				throw new SocketOperationException(
					ProtocolInvocationLauncherErrorManager.SAF_19
				);
			}
			return ProtocolInvocationLauncherErrorManager.getErrorMessage(
					ProtocolInvocationLauncherErrorManager.SAF_19
				);
		}
		catch (final Exception e) {
			LOGGER.severe("Error al mostrar el dialogo de seleccion de certificados: " + e); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.SAF_08);
			if (!bySocket){
				throw new SocketOperationException(
					ProtocolInvocationLauncherErrorManager.SAF_08
				);
			}
			return ProtocolInvocationLauncherErrorManager.getErrorMessage(
				ProtocolInvocationLauncherErrorManager.SAF_08
			);
		}

		// Concatenamos el certificado utilizado para firmar y la firma con un separador
		// para que la pagina pueda recuperar ambos
		byte[] certEncoded;
		try {
			certEncoded = pke.getCertificateChain()[0].getEncoded();
		}
		catch (final CertificateEncodingException e) {
			LOGGER.severe("Error en la decodificacion del certificado de firma: " + e); //$NON-NLS-1$
			ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.SAF_18);
			if (!bySocket){
				throw new SocketOperationException(
					ProtocolInvocationLauncherErrorManager.SAF_18
				);
			}
			return ProtocolInvocationLauncherErrorManager.getErrorMessage(
				ProtocolInvocationLauncherErrorManager.SAF_18
			);
		}

		String dataToSend;
		if (options.getDesKey() != null) {
			try {
				// El CipherData devuelve los datos directamente en Base64
				dataToSend = CypherDataManager.cipherData(certEncoded, options.getDesKey());
			}
			catch (final Exception e) {
				LOGGER.severe("Error en el cifrado de los datos a enviar: " + e); //$NON-NLS-1$
				ProtocolInvocationLauncherErrorManager.showError(ProtocolInvocationLauncherErrorManager.SAF_12);
				if (!bySocket){
					throw new SocketOperationException(
						ProtocolInvocationLauncherErrorManager.SAF_12
					);
				}
				return ProtocolInvocationLauncherErrorManager.getErrorMessage(
					ProtocolInvocationLauncherErrorManager.SAF_12
				);
			}
		}
		else {
			LOGGER.warning(
				"Se omite el cifrado de los datos resultantes por no haberse proporcionado una clave de cifrado" //$NON-NLS-1$
			);
			dataToSend = Base64.encode(certEncoded, true);
		}

		return dataToSend;
	}

	public static String getResultCancel() {
		return RESULT_CANCEL;
	}
}
