/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.standalone;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import es.gob.afirma.core.AOException;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.misc.MimeHelper;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.signers.AOSigner;
import es.gob.afirma.core.signers.CounterSignTarget;
import es.gob.afirma.keystores.AOKeyStore;
import es.gob.afirma.keystores.AOKeyStoreManager;
import es.gob.afirma.keystores.AOKeyStoreManagerFactory;
import es.gob.afirma.keystores.AOKeystoreAlternativeException;
import es.gob.afirma.keystores.callbacks.CachePasswordCallback;
import es.gob.afirma.keystores.filters.CertFilterManager;
import es.gob.afirma.keystores.filters.CertificateFilter;
import es.gob.afirma.signers.batch.client.BatchSigner;
import es.gob.afirma.signers.cades.AOCAdESSigner;
import es.gob.afirma.signers.pades.AOPDFSigner;
import es.gob.afirma.signers.xades.AOFacturaESigner;
import es.gob.afirma.signers.xades.AOXAdESSigner;
import es.gob.afirma.standalone.ui.CertValidationUi;
import es.gob.afirma.standalone.ui.hash.HashHelper;

/** Clase para la gesti&oacute;n de los par&aacute;metros proporcionados desde l&iacute;nea de comandos.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
final class CommandLineLauncher {

	private CommandLineLauncher() {
		// No permitimos la instanciacion
	}

	private static final String PARAM_HELP    = "-help"; //$NON-NLS-1$

	private static final String EXTRA_PARAM_TARGET = "target"; //$NON-NLS-1$

	/** Clave con la que se configuran los filtros en el CertFilterManager. */
	private static final String KEY_FILTERS = "filters";  //$NON-NLS-1$

	private static final int STATUS_ERROR = -1;
	private static final int STATUS_SUCCESS = 0;

	private static final String STORE_AUTO = "auto"; //$NON-NLS-1$
	private static final String STORE_MAC  = "mac"; //$NON-NLS-1$
	private static final String STORE_WIN  = "windows"; //$NON-NLS-1$
	private static final String STORE_P12  = "pkcs12"; //$NON-NLS-1$
	private static final String STORE_NSS  = "mozilla"; //$NON-NLS-1$
	private static final String STORE_DNI  = "dni"; //$NON-NLS-1$
	private static final String STORE_P11  = "pkcs11"; //$NON-NLS-1$

	static void processCommandLine(final String[] args) {

		// Desactivamos el Logger de consola para que no interfiera con los comandos
		//TODO: Descomentar para poner en produccion
		final Handler[] handlers = Logger.getLogger("es.gob.afirma").getHandlers(); //$NON-NLS-1$
		for(final Handler handler : handlers) {
			if(handler instanceof ConsoleHandler) {
				handler.setLevel(Level.OFF);
			}
		}

		final Console console = System.console();

		try (
			final PrintWriter pw = console != null ? console.writer() : new PrintWriter(System.out);
		) {

			// Comprobamos si hay que mostrar la sintaxis de la aplicacion
			if (args == null || args.length < 1 || PARAM_HELP.equalsIgnoreCase(args[0])) {
				closeApp(STATUS_SUCCESS, pw, buildGeneralSyntax(null));
				return;
			}

			// Identificamos el comando
			final CommandLineCommand command = CommandLineCommand.parse(args[0].toLowerCase());
			if (command == null) {
				closeApp(STATUS_ERROR, pw, buildGeneralSyntax(CommandLineMessages.getString("CommandLineLauncher.15", args[0]))); //$NON-NLS-1$
				return;
			}

			// Comprobamos si se debe mostrar la ayuda del comando
			if (args.length > 1 && PARAM_HELP.equalsIgnoreCase(args[1])) {
				closeApp(STATUS_SUCCESS, pw, CommandLineParameters.buildSyntaxError(command, null));
				return;
			}

			// Cargamos los parametros
			final CommandLineParameters params;
			try {
				params = new CommandLineParameters(args);
			}
			catch (final CommandLineException e) {
				closeApp(STATUS_ERROR, pw, CommandLineParameters.buildSyntaxError(command, e.getMessage()));
				return;
			}

			// Actuamos segun corresponda para cada comando
			try {
				switch(command) {
					case LIST:
						final String aliases = listAliasesByCommandLine(params);
						closeApp(STATUS_SUCCESS, pw, aliases);
						return;
					case VERIFY:
						 verifyByGui(params);
						 return;
					case SIGN:
						if (params.isGui()) {
							signByGui(params);
						}
						else {
							final String response = signByCommandLine(command, params);
							closeApp(STATUS_SUCCESS, pw, response);
						}
						return;
					case COSIGN:
					case COUNTERSIGN:
						final String response = signByCommandLine(command, params);
						closeApp(STATUS_SUCCESS, pw, response);
						return;
					case CREATEHASH:
						createHashByGui(params);
						return;
					case CHECKHASH:
						checkHashByGui(params);
						return;
					case MASSIVE:
						//TODO: Implementar
						throw new UnsupportedOperationException(
							"Firma masiva en linea de comandos aun no implementada" //$NON-NLS-1$
						);
					case BATCHSIGN:
						batchByCommandLine(params);
						return;
					default:
						closeApp(
							STATUS_ERROR,
							pw,
							CommandLineMessages.getString(
								"CommandLineLauncher.54",  //$NON-NLS-1$
								command.getOp()
							)
						);
						return;
				}
			}
			catch (final CommandLineException e) {
				closeApp(STATUS_ERROR, pw, CommandLineParameters.buildSyntaxError(command, e.getMessage()));
				return;
			}
			catch (final IOException e) {
				closeApp(STATUS_ERROR, pw, e.getMessage());
				return;
			}
			catch (final AOKeystoreAlternativeException e) {
				closeApp(STATUS_ERROR, pw, CommandLineMessages.getString("CommandLineLauncher.49", e.getMessage())); //$NON-NLS-1$
				return;
			}
			catch (final Exception e) {
				closeApp(STATUS_ERROR, pw, CommandLineMessages.getString("CommandLineLauncher.50", e.getMessage())); //$NON-NLS-1$
				return;
			}

		}
	}

	/** Realizamos la operaci&oacute;n de creaci&oacute;n de huellas digitales mostrando los di&aacute;logos
	 * que fuesen necesarios.
	 * @param params Par&aacute;metros de configuraci&oacute;n.
	 * @throws CommandLineException Cuando falta algun par&aacute;metro necesario. */
	private static void createHashByGui(final CommandLineParameters params) throws CommandLineException {

		final File inputFile = params.getInputFile();
		if (inputFile == null) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.5")); //$NON-NLS-1$
		}

		HashHelper.createHashUI(params.getInputFile().getAbsolutePath());
	}

	/** Realizamos la operaci&oacute;n de comprobaci&oacute;n de huellas digitales mostrando los di&aacute;logos
	 * que fuesen necesarios.
	 * @param params Par&aacute;metros de configuraci&oacute;n.
	 * @throws CommandLineException Cuando falta algun par&aacute;metro necesario. */
	private static void checkHashByGui(final CommandLineParameters params) throws CommandLineException {

		final File inputFile = params.getInputFile();
		if (inputFile == null) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.5")); //$NON-NLS-1$
		}
		HashHelper.checkHashUI(params.getInputFile().getAbsolutePath());
	}

	/** Mostramos el panel de validaci&oacute;n de certificados y firmas.
 	 * @param params Par&aacute;metros de configuraci&oacute;n.
 	 * @throws CommandLineException Cuando falta algun par&aacute;metro necesario. */
 	private static void verifyByGui(final CommandLineParameters params) throws CommandLineException {

 		final File inputFile = params.getInputFile();
 		if (inputFile == null) {
 			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.5"));  //$NON-NLS-1$
 		}

 		// Comprobamos si lo que nos piden validar es un certificado...
 		X509Certificate cert;
 		try (
 			InputStream bis = new BufferedInputStream(
 				new FileInputStream(inputFile)
 			);
 		) {
 			cert = DataAnalizerUtil.isCertificate(AOUtil.getDataFromInputStream(bis));
 		}
 		catch (final Exception e) {
 			cert = null;
 		}

 		// Si no es un certificado asumimos que es una firma
 		if (cert == null) {
 			new VisorFirma(true, null).initialize(false, inputFile);
 		}

 		// Y si es certificado lo validamos como tal
 		else {
 			Image icon = null;
 			try {
 				icon = ImageIO.read(
 					CommandLineLauncher.class.getResource(
 						"/resources/certificate_16.png"  //$NON-NLS-1$
 					)
 				);
 			}
 			catch (final IOException e) {
 				// Se ignora
 			}
 			CertValidationUi.validateCert(cert, null, null, icon);
 		}
 	}

	/** Mostramos el panel de firmas. Se usara la configuraci&oacute;n de firma establecida
	 * en la interfaz de AutoFirma.
	 * @param params Par&aacute;metros de configuraci&oacute;n.
	 * @throws CommandLineException Cuando falta algun par&aacute;metro necesario. */
	private static void signByGui(final CommandLineParameters params) throws CommandLineException {

		final File inputFile = params.getInputFile();
		if (inputFile == null) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.5")); //$NON-NLS-1$
		}

		final SimpleAfirma simpleAfirma = new SimpleAfirma();
		simpleAfirma.initialize(inputFile);
		simpleAfirma.loadFileToSign(inputFile);
	}

	private static String batchByCommandLine(final CommandLineParameters params) throws CommandLineException {

		final File inputFile = params.getInputFile();
		if (inputFile == null) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.5")); //$NON-NLS-1$
		}

		String selectedAlias = params.getAlias();
		if (selectedAlias == null && params.getFilter() == null) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.17")); //$NON-NLS-1$
		}

		final File outputFile  = params.getOutputFile();
		if (outputFile == null && !params.isXml()) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.19")); //$NON-NLS-1$
		}

		final URL preUrl = params.getPreSignUrl();
		final URL postUrl = params.getPostSignUrl();
		if (preUrl == null || postUrl == null) {
			throw new CommandLineException(
				CommandLineMessages.getString("CommandLineLauncher.60") //$NON-NLS-1$
			);
		}

		try {
			final AOKeyStoreManager ksm = getKsm(params.getStore(), params.getPassword());
			if (params.getFilter() != null) {
				selectedAlias = filterCertificates(ksm, params.getFilter());
			}
			final PrivateKeyEntry pke = ksm.getKeyEntry(selectedAlias);
			if (pke == null) {
				throw new CommandLineException(
					CommandLineMessages.getString("CommandLineLauncher.61", selectedAlias) //$NON-NLS-1$
				);
			}
			final byte[] inputXml;
			try (
				final InputStream fis = new FileInputStream(inputFile);
				final InputStream bis = new BufferedInputStream(fis);
			) {
				inputXml = AOUtil.getDataFromInputStream(bis);
			}
			final String xml;
			if (!Base64.isBase64(inputXml)) {
				xml = Base64.encode(inputXml, true);
			}
			else {
				xml = new String(inputXml).replace("+", "-").replace("/", "_"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			}
			final String res = BatchSigner.sign(
				xml,
				preUrl.toString(),
				postUrl.toString(),
				pke.getCertificateChain(),
				pke.getPrivateKey()
			);
			try (
				final FileOutputStream fos = new FileOutputStream(outputFile);
				final BufferedOutputStream bos = new BufferedOutputStream(fos);
			) {
				fos.write(res.getBytes());
				fos.flush();
			}

			final String okMsg = CommandLineMessages.getString("CommandLineLauncher.22"); //$NON-NLS-1$
			if (params.isXml()) {
				if (params.getOutputFile() != null) {
					return buildXmlResponse(true, okMsg, null);
				}
				return buildXmlResponse(true, okMsg, res.getBytes());
			}
			return okMsg;

		}
		catch (IOException |
			   AOKeystoreAlternativeException |
			   KeyStoreException              |
			   NoSuchAlgorithmException       |
			   UnrecoverableEntryException    |
			   CertificateEncodingException   |
			   AOException e) {
					if (params.isXml()) {
						return buildXmlResponse(false, e.getMessage(), null);
					}
					return e.getMessage();
		}

	}

	/** Firma por l&iacute;nea de comandos.
	 * @param command Comando ejecutado en l&iacute;nea de comandos.
	 * @param params Par&aacute;metros de configuraci&oacute;n.
	 * @return Mensaje con el resultado de la operaci&oacute;n.
	 * @throws CommandLineException Cuando falta algun par&aacute;metro necesario. */
	private static String signByCommandLine(final CommandLineCommand command, final CommandLineParameters params) throws CommandLineException {

		if (params.getInputFile() == null) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.5")); //$NON-NLS-1$
		}

		if (params.getAlias() == null && params.getFilter() == null) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.17")); //$NON-NLS-1$
		}

		if (params.getOutputFile() == null && !params.isXml()) {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.19")); //$NON-NLS-1$
		}

		byte[] res;
		try {
			final AOKeyStoreManager ksm = getKsm(params.getStore(), params.getPassword());

			String selectedAlias = params.getAlias();
			if (params.getFilter() != null) {
				selectedAlias = filterCertificates(ksm, params.getFilter());
			}

			res = sign(
				command,
				params.getFormat(),
				params.getAlgorithm(),
				params.getExtraParams(),
				params.getInputFile(),
				selectedAlias,
				ksm,
				params.getPassword()
			);
		}
		catch (IOException | AOException | AOKeystoreAlternativeException e) {
			if (params.isXml()) {
				return buildXmlResponse(false, e.getMessage(), null);
			}
			return e.getMessage();
		}

		// Si se ha proporcionado un fichero de salida, se guarda el resultado de la firma en el.
		// La respuesta, si se indico que fuese XML, sera un XML con el texto descriptivo de la respuesta
		// y, si no se guardo la firma, el resultado de la firma. Si la respuesta no es XML simplemente
		// se devuelve el texto plano con el resultado.
		if (params.getOutputFile() != null) {

			try (final OutputStream fos = new FileOutputStream(params.getOutputFile());) {
				fos.write(res);
			}
			catch(final Exception e) {
				return CommandLineMessages.getString(
					"CommandLineLauncher.21", //$NON-NLS-1$
					params.getOutputFile().getAbsolutePath()
				);
			}
		}

		final String okMsg = CommandLineMessages.getString("CommandLineLauncher.22"); //$NON-NLS-1$
		if (params.isXml()) {
			if (params.getOutputFile() != null) {
				return buildXmlResponse(true, okMsg, null);
			}
			return buildXmlResponse(true, okMsg, res);
		}
		return okMsg;
	}

	/** Filtra los certificados del almac&eacute;n y devuelve el alias del &uacute;nico certificado
	 * que pasa el filtro.
	 * @param ksm Gestor del almac&eacute;n en el que aplicar el filtro.
	 * @param filterConfig Configuraci&oacute;n del filtro de certificados.
	 * @return Alias del certificado seleccionado.
	 * @throws CommandLineException Cuando no se ha encontrado ning&uacute;n certificado que pase
	 *                              el filtro o cuando se encuentra m&aacute;s de uno. */
	private static String filterCertificates(final AOKeyStoreManager ksm,
			                                 final String filterConfig) throws CommandLineException {

		final Properties config = new Properties();
		config.setProperty(KEY_FILTERS, filterConfig);

		// Componemos el filtro (solo puede haber 1)
		final CertificateFilter filter = new CertFilterManager(config).getFilters().get(0);

		final String[] filteredAliases = filter.matches(ksm.getAliases(), ksm);

		if (filteredAliases == null || filteredAliases.length == 0) {
			throw new CommandLineException(
					CommandLineMessages.getString("CommandLineLauncher.52") //$NON-NLS-1$
			);
		}

		if (filteredAliases.length > 1) {
			throw new CommandLineException(
					CommandLineMessages.getString("CommandLineLauncher.53") //$NON-NLS-1$
			);
		}

		return filteredAliases[0];
	}

	private static byte[] sign(final CommandLineCommand command,
			                   final String fmt,
			                   final String algorithm,
			                   final String extraParams,
			                   final File inputFile,
			                   final String alias,
			                   final AOKeyStoreManager ksm,
			                   final String storePassword) throws CommandLineException, IOException, AOException {

		final PrivateKeyEntry ke;
		ksm.setEntryPasswordCallBack(
			new CachePasswordCallback(
				storePassword != null ? storePassword.toCharArray() : "dummy".toCharArray() //$NON-NLS-1$
			)
		);
		try {
			ke = ksm.getKeyEntry(alias);
		}
		catch (final Exception e) {
			throw new AOException("No se ha podido obtener la referencia a la clave privada", e); //$NON-NLS-1$
		}
		if (ke == null) {
			throw new AOException("No se hay ninguna entrada en el almacen con el alias indicado: " + alias); //$NON-NLS-1$
		}

		// Leemos el fichero de entrada
		final byte[] data;
		try (
			final InputStream input = new FileInputStream(inputFile);
		) {
			data = AOUtil.getDataFromInputStream(input);
		}
		catch(final Exception e) {
			throw new IOException(
				"No se ha podido leer el fichero de entrada: " + inputFile.getAbsolutePath(), e //$NON-NLS-1$
			);
		}
		String format = null;
		Properties extraParamsProperties = null;
		if (command != CommandLineCommand.MASSIVE) {
			// Si el formato es "auto", miramos si es XML o PDF para asignar XAdES o PAdES
			if (CommandLineParameters.FORMAT_AUTO.equals(fmt)) {
				final String ext = new MimeHelper(data).getExtension();
				if ("pdf".equals(ext)) { //$NON-NLS-1$
					format = CommandLineParameters.FORMAT_PADES;
				}
				else if ("xml".equals(ext)) { //$NON-NLS-1$
					format = CommandLineParameters.FORMAT_XADES;
				}
				else {
					format = CommandLineParameters.FORMAT_CADES;
				}
			}
			else {
				format = fmt;
			}

			if (extraParams != null) {
				try {
					final String params = extraParams.trim();
					extraParamsProperties = new Properties();

					// La division no funciona correctamente con split porque el caracter salto de linea se protege
					// al insertarse por consola, asi que lo hacemos manualmente.
					int beginIndex = 0;
					int endIndex;
					while ((endIndex = params.indexOf("\\n", beginIndex)) != -1) { //$NON-NLS-1$
						final String keyValue = params.substring(beginIndex, endIndex).trim();
						// Solo procesamos las lineas con contenido que no sean comentario
						if (keyValue.length() > 0 && keyValue.charAt(0) != '#') {
							extraParamsProperties.setProperty(
								keyValue.substring(0, keyValue.indexOf('=')),
								keyValue.substring(keyValue.indexOf('=') + 1)
							);
						}
						beginIndex = endIndex + "\\n".length();  //$NON-NLS-1$
					}
					extraParamsProperties.setProperty(
						params.substring(beginIndex, params.indexOf('=', beginIndex)),
						params.substring(params.indexOf('=', beginIndex) + 1)
					);
				}
				catch (final Exception e) {
					throw new CommandLineException(
							CommandLineMessages.getString("CommandLineLauncher.51", extraParams), e); //$NON-NLS-1$
				}
			}
		}

		// Instanciamos un firmador del tipo adecuado
		final AOSigner signer;
		if (CommandLineParameters.FORMAT_CADES.equals(format)) {
			signer = new AOCAdESSigner();
		}
		else if (CommandLineParameters.FORMAT_XADES.equals(format)) {
			signer = new AOXAdESSigner();
		}
		else if (CommandLineParameters.FORMAT_PADES.equals(format)) {
			signer = new AOPDFSigner();
		}
		else if (CommandLineParameters.FORMAT_FACTURAE.equals(format)) {
			signer = new AOFacturaESigner();
		}
		else {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.4", format)); //$NON-NLS-1$
		}

		// Obtenemos el resultado de la operacion adecuada
		final byte[] resBytes;
		try {
			if (command == CommandLineCommand.SIGN) {
				resBytes = signer.sign(
					data,
					algorithm,
					ke.getPrivateKey(),
					ke.getCertificateChain(),
					extraParamsProperties
				);
			}
			else if (command == CommandLineCommand.COSIGN) {
				resBytes = signer.cosign(
					data, // Firma
					algorithm,
					ke.getPrivateKey(),
					ke.getCertificateChain(),
					extraParamsProperties
				);
			}
			else if (command == CommandLineCommand.COUNTERSIGN) {
				CounterSignTarget csTarget = CounterSignTarget.LEAFS;
				if (extraParamsProperties != null && extraParamsProperties.containsKey(EXTRA_PARAM_TARGET) &&
						CounterSignTarget.TREE.name().equalsIgnoreCase(extraParamsProperties.getProperty(EXTRA_PARAM_TARGET))) {
					csTarget = CounterSignTarget.TREE;
				}

				resBytes = signer.countersign(
					data, // Firma
					algorithm,
					csTarget,
					null,
					ke.getPrivateKey(),
					ke.getCertificateChain(),
					extraParamsProperties
				);
			}
			else {
				throw new CommandLineException("Operacion no soportada: " + command.getOp()); //$NON-NLS-1$
			}
		}
		catch(final Exception e) {
			throw new CommandLineException("Error en la operacion de firma: " + e.getMessage(), e); //$NON-NLS-1$
		}

		return resBytes;
	}

	private static String listAliasesByCommandLine(final CommandLineParameters params) throws IOException, CommandLineException, AOKeystoreAlternativeException {

		final String[] aliases = getKsm(params.getStore(), params.getPassword()).getAliases();
		final StringBuilder sb = new StringBuilder();

		if (params.isXml()) {
			sb.append("<afirma><result>ok</result><response>"); //$NON-NLS-1$
			for (final String alias : aliases) {
				sb.append("<alias>").append(alias).append("</alias>"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			sb.append("</response></afirma>"); //$NON-NLS-1$
		}
		else {
			for (final String alias : aliases) {
				sb.append(alias).append('\n');
			}
		}

		return sb.toString();
	}

	private static AOKeyStoreManager getKsm(final String storeType,
			                                final String pwd) throws IOException,
	                                                                 CommandLineException,
	                                                                 AOKeystoreAlternativeException {
		final AOKeyStore store;
		String lib = null;
		if (STORE_AUTO.equals(storeType) || storeType == null) {
			if (Platform.OS.MACOSX.equals(Platform.getOS())) {
				store = AOKeyStore.APPLE;
			}
			else if (Platform.OS.WINDOWS.equals(Platform.getOS())) {
				store = AOKeyStore.WINDOWS;
			}
			else {
				store = AOKeyStore.MOZ_UNI;
			}
		}
		else if (STORE_MAC.equals(storeType)) {
			store = AOKeyStore.APPLE;
		}
		else if (STORE_WIN.equals(storeType)) {
			store = AOKeyStore.WINDOWS;
		}
		else if (STORE_NSS.equals(storeType)) {
			store = AOKeyStore.MOZ_UNI;
		}
		else if (STORE_DNI.equals(storeType) || "dnie".equals(storeType)) { //$NON-NLS-1$
			store = AOKeyStore.DNIEJAVA;
		}
		else if (storeType.startsWith(STORE_P12 + ":")) { //$NON-NLS-1$
			store = AOKeyStore.PKCS12;
			final String libName = storeType.replace(STORE_P12 + ":", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!new File(libName).exists()) {
				throw new IOException("El fichero PKCS#12 indicado no existe: " + libName); //$NON-NLS-1$
			}
			if (!new File(libName).canRead()) {
				throw new IOException("No se tienen permisos de lectura para el fichero PKCS#12 indicado: " + libName); //$NON-NLS-1$
			}
			lib = libName;
		}
		else if (storeType.startsWith(STORE_P11 + ":")) { //$NON-NLS-1$
			store = AOKeyStore.PKCS11;
			final String libName = storeType.replace(STORE_P11 + ":", ""); //$NON-NLS-1$ //$NON-NLS-2$
			if (!new File(libName).exists()) {
				throw new IOException("La biblioteca PKCS#11 indicada no existe: " + libName); //$NON-NLS-1$
			}
			if (!new File(libName).canRead()) {
				throw new IOException("No se tienen permisos de lectura para la biblioteca PKCS#11 indicada: " + libName); //$NON-NLS-1$
			}
			lib = libName;
		}
		else {
			throw new CommandLineException(CommandLineMessages.getString("CommandLineLauncher.48", storeType)); //$NON-NLS-1$
		}

		return AOKeyStoreManagerFactory.getAOKeyStoreManager(
			store,
			lib,
			"CommandLine", //$NON-NLS-1$
			pwd != null ? new CachePasswordCallback(pwd.toCharArray()) : null,
			null
		);
	}

	/** Construye la cadena de texto que explica la sintaxis para el uso de la aplicaci&oacute;n
	 * por l&iacute;nea de comandos.
	 * @param errorMessage Mensaje con el error de explica cometido.
	 * @return Texto con el error de sintaxis y la explicaci&oacute;n de la sintaxis correcta. */
	private static String buildGeneralSyntax(final String errorMessage) {
		final StringBuilder sb = new StringBuilder();
		if (errorMessage != null) {
			sb.append(errorMessage).append("\n"); //$NON-NLS-1$;
		}
		else {
			sb.append(CommandLineMessages.getString("CommandLineLauncher.34")).append("\n\n"); //$NON-NLS-1$; //$NON-NLS-2$;
		}
		sb.append(CommandLineMessages.getString("CommandLineLauncher.7")).append(": AutoFirma cmd [options...]\n\n")  //$NON-NLS-1$//$NON-NLS-2$
		.append(CommandLineMessages.getString("CommandLineLauncher.33")) .append(" cmd:\n\n") //$NON-NLS-1$ //$NON-NLS-2$
		.append("  ").append(CommandLineCommand.SIGN.getOp())			 .append("\t\t (").append(CommandLineMessages.getString("CommandLineLauncher.8")).append(")\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("  ").append(CommandLineCommand.COSIGN.getOp())		     .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.9")).append(")\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("  ").append(CommandLineCommand.COUNTERSIGN.getOp())	 .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.10")).append(")\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		//TODO: Descomentar cuando se soporte firma masiva
		//.append("  ").append(CommandLineCommand.MASSIVE.getOp())	     .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.35")).append(")\n") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("  ").append(CommandLineCommand.LIST.getOp())			 .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.11")).append(")\n")  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("  ").append(CommandLineCommand.VERIFY.getOp())		     .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.29")).append(")\n\n")  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("  ").append(CommandLineCommand.BATCHSIGN.getOp())	     .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.69")).append(")\n\n")  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("  ").append(CommandLineCommand.CREATEHASH.getOp())	     .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.70")).append(")\n\n")  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append("  ").append(CommandLineCommand.CHECKHASH.getOp())	     .append("\t (")  .append(CommandLineMessages.getString("CommandLineLauncher.71")).append(")\n\n")  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		.append(CommandLineMessages.getString("CommandLineLauncher.30")) .append("\n\n"); //$NON-NLS-1$ //$NON-NLS-2$

		return sb.toString();
	}

	/** Construye el resultado para una firma/multifirma. Si el resultado debe
	 * devolverse en XML se devolver&aacute; un XML con la forma:<br>
	 * {@code
	 * <afirma>
	 *   <result>true|false</result>
	 * 	 <response>
	 *     <msg>MENSAJE</msg>
	 *     <sign>FIRMA_BASE64</sign>
	 * 	 </response>
	 * </afirma>
	 * }
	 * @param ok Resultado de la operaci&oacute;n.
	 * @param msg Mensaje de respuesta.
	 * @param resBytes Firma generada o {@code null} si se produjo un error.
	 * @return XML resultado de la operaci&oacute;n. */
	private static String buildXmlResponse(final boolean ok, final String msg, final byte[] resBytes) {

		final StringBuilder sb = new StringBuilder();
		sb.append("<afirma><result>").append(ok).append("</result>"); //$NON-NLS-1$ //$NON-NLS-2$
		if (msg != null || resBytes != null) {
			sb.append("<response>"); //$NON-NLS-1$
			if (msg != null){
				sb.append("<msg>").append(msg).append("</msg>"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (resBytes != null) {
				sb.append("<sign>").append(Base64.encode(resBytes)).append("</sign>"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			sb.append("</response>"); //$NON-NLS-1$
		}
		sb.append("</afirma>"); //$NON-NLS-1$

		return sb.toString();
	}

	/** Cierra la aplicaci&oacute;n mostrando un &uacute;ltimo mensaje si se le proporcionan
	 * los recursos necesarios.
	 * @param status Estado de cierre de la aplicaci&oacute;n.
	 * @param pw Objeto para la impresi&oacute;n por consola.
	 * @param message Mensaje a mostrar. */
	private static void closeApp(final int status, final PrintWriter pw, final String message) {
		if (pw != null) {
			if (message != null) {
				pw.write(message);
				pw.flush();
			}
		}
		System.exit(status);
	}

	public static void main(final String[] args) {
		processCommandLine( args );
	}
}
