/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.standalone.ui.preferences;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Nombre de las preferencias de configuraci&oacute;n del programa.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class PreferencesManager {

	/** Objecto general de preferencias donde se guarda la configuraci&oacute;n de la
	 * aplicaci&oacute;n. */
	private static final Preferences preferences;
	static {
		preferences = Preferences.userNodeForPackage(PreferencesManager.class);
	}

	private PreferencesManager() {
		/** No permitimos la instanciacion */
	}

	//**************************************************************************************************************************
	//**************** PREFERENCIAS GENERALES **********************************************************************************

	/** Configuraci&oacute;n de <i>proxy</i> seleccionada.
	 * Un valor de <code>true</code> en esta preferencia indica que debe usarse el <i>proxy</i> configurado,
	 * y un valor de <code>false</code> que no usar&aacute; <i>proxy</i> en las conexiones de red. */
	public static final String PREFERENCE_GENERAL_PROXY_SELECTED = "proxySelected"; //$NON-NLS-1$

	/** Host del servidor <i>proxy</i> configurado. */
	public static final String PREFERENCE_GENERAL_PROXY_HOST = "proxyHost"; //$NON-NLS-1$

	/** Puerto del servidor <i>proxy</i> configurado. */
	public static final String PREFERENCE_GENERAL_PROXY_PORT = "proxyPort"; //$NON-NLS-1$

	/** Nombre de usuario del servidor <i>proxy</i> configurado. */
	public static final String PREFERENCE_GENERAL_PROXY_USERNAME = "proxyUsername"; //$NON-NLS-1$

	/** Contraseña del servidor <i>proxy</i> configurado. */
	public static final String PREFERENCE_GENERAL_PROXY_PASSWORD = "proxyPassword"; //$NON-NLS-1$

	/** Evitar la confirmaci&oacute;n al cerrar la aplicaci&oacute;n o no.
	 * Un valor de <code>true</code> en esta preferencia permitir&aacute; cerrar la aplicaci&oacute;n sin ning&uacute;n di&aacute;logo
	 * de advertencia. Un valor de <code>false</code> har&aacute; que se muestre un di&aacute;logo para que el usuario confirme que
	 * realmente desea cerrar la aplicaci&oacute;n. */
	public static final String PREFERENCE_GENERAL_OMIT_ASKONCLOSE = "omitAskOnClose"; //$NON-NLS-1$

	/** No mostrar la pantalla inicial de uso de DNIe.
	 * Un valor de <code>true</code> en esta preferencia hace que nunca se muestre la pantalla inicial que sugiere al usuario
	 * el uso directo del DNIe como almac&eacute;n de claves. Un valor de <code>false</code> har&aacute; que se muestre esta pantalla
	 * al inicio siempre que se detecte un lector de tarjetas en el sistema. */
	public static final String PREFERENCE_GENERAL_HIDE_DNIE_START_SCREEN = "hideDnieStartScreen"; //$NON-NLS-1$

	/** Buscar actualizaciones al arrancar.
	 * Un valor de <code>true</code> en esta preferencia hace que, al arrancar, la aplicaci&oacute;n compruebe autom&aacute;ticamente
	 * si hay publicadas versiones m&aacute;s actuales del aplicativo. Un valor de <code>false</code> har&aacute; que no se haga
	 * esta comprobaci&oacute;n. */
	public static final String PREFERENCE_GENERAL_UPDATECHECK = "checkForUpdates"; //$NON-NLS-1$

	/** Env&iacute;a estad&iacute;sticas de uso.
	 * Un valor de <code>true</code> en esta preferencia hace que, al arrancar, la aplicaci&oacute;n env&iacute;e
	 * de forma an&oacute;nima estad&iacute;sticas de uso a <i>Google Analytics</i>. Un valor de <code>false</code>
	 * har&aacute; que no se env&iacute;e ning&uacute;n dato. */
	public static final String PREFERENCE_GENERAL_USEANALYTICS = "useAnalytics"; //$NON-NLS-1$

	/** Algoritmo de firma.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>SHA1withRSA</li>
	 *  <li>SHA256withRSA</li>
	 *  <li>SHA384withRSA</li>
	 *  <li>SHA512withRSA</li>
	 * </ul> */
	public static final String PREFERENCE_GENERAL_SIGNATURE_ALGORITHM = "signatureAlgorithm"; //$NON-NLS-1$

	/** Formato de firma por defecto para documentos PDF.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>PAdes</li>
	 *  <li>CAdes</li>
	 *  <li>XAdes</li>
	 * </ul> */
	public static final String PREFERENCE_GENERAL_DEFAULT_FORMAT_PDF = "defaultSignatureFormatPdf"; //$NON-NLS-1$

	/** Formato de firma por defecto para documentos OOXML de Microsoft Office.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>OOXML (Office Open XML)</li>
	 *  <li>CAdES</li>
	 *  <li>XAdES</li>
	 * </ul> */
	public static final String PREFERENCE_GENERAL_DEFAULT_FORMAT_OOXML = "defaultSignatureFormatOoxml"; //$NON-NLS-1$

	/** Formato de firma por defecto para Facturas Electr&oacute;nicas.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>FacturaE</li>
	 *  <li>CAdES</li>
	 *  <li>XAdES</li>
	 * </ul> */
	public static final String PREFERENCE_GENERAL_DEFAULT_FORMAT_FACTURAE = "defaultSignatureFormatFacturae"; //$NON-NLS-1$

	/** Formato de firma por defecto para documentos ODF de LibreOffice / OpenOffice.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>ODF (Open Document Format)</li>
	 *  <li>CAdES</li>
	 *  <li>XAdES</li>
	 * </ul> */
	public static final String PREFERENCE_GENERAL_DEFAULT_FORMAT_ODF = "defaultSignatureFormatOdf"; //$NON-NLS-1$

	/** Formato de firma por defecto para documentos XML.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>CAdES</li>
	 *  <li>XAdES</li>
	 * </ul> */
	public static final String PREFERENCE_GENERAL_DEFAULT_FORMAT_XML = "defaultSignatureFormatXml"; //$NON-NLS-1$

	/** Formato de firma por defecto para ficheros binarios que no se adec&uacute;en a ninguna otra categor&iacute;a.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>CAdES</li>
	 *  <li>XAdES</li>
	 * </ul> */
	public static final String PREFERENCE_GENERAL_DEFAULT_FORMAT_BIN = "defaultSignatureFormatBin"; //$NON-NLS-1$

	//**************** FIN PREFERENCIAS GENERALES ******************************************************************************
	//**************************************************************************************************************************

	//**************************************************************************************************************************
	//**************** PREFERENCIAS DE ALMACENES DE CLAVES *********************************************************************

	/** En firma, restringir que &uacute;nicamente se puedan usar certificados de firma.
	 * Un valor de <code>true</code> en esta preferencia permitir&aacute; usar solo certificados espec&iacute;ficos
	 * para firma en las firmas electr&oacute;nicas. */
	public static final String PREFERENCE_KEYSTORE_SIGN_ONLY_CERTS = "useOnlySignatureCertificates"; //$NON-NLS-1$

	/** En firma, restringir que &uacute;nicamente se puedan usar certificados de seud&oacute;nimo cuando estos est&eacute;n
	 * disponibles. Un valor de <code>true</code> en esta preferencia permitir&aacute; usar solo  certificados de
	 * seud&oacute;nimo cuando estos est&eacute;n disponibles.*/
	public static final String PREFERENCE_KEYSTORE_ALIAS_ONLY_CERTS = "useOnlyAliasCertificates"; //$NON-NLS-1$

	/** Almac&eacute;n de claves por defecto. */
	public static final String PREFERENCE_KEYSTORE_DEFAULT_STORE = "defaultStore"; //$NON-NLS-1$

	//**************** FIN PREFERENCIAS DE ALMACENES DE CLAVES *****************************************************************
	//**************************************************************************************************************************

	//**************************************************************************************************************************
	//************************* PREFERENCIAS DE FIRMAS XAdES *******************************************************************

	/** Identificador de la pol&iacute;tica de firma para XAdES. Debe ser un OID.*/
	public static final String PREFERENCE_XADES_POLICY_IDENTIFIER = "xadesPolicyIdentifier"; //$NON-NLS-1$

	/** Huella digital del identificador de la pol&iacute;tica de firma para XAdES. Debe estar en base64.*/
	public static final String PREFERENCE_XADES_POLICY_IDENTIFIER_HASH = "xadesPolicyIdentifierHash"; //$NON-NLS-1$

	/** Algoritmo de la huella digital del identificador de la pol&iacute;tica de firma para XAdES.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>SHA1</li>
	 *  <li>SHA-512</li>
	 *  <li>SHA-384</li>
	 *  <li>SHA-256</li>
	 * </ul> */
	public static final String PREFERENCE_XADES_POLICY_IDENTIFIER_HASH_ALGORITHM = "xadesPolicyIdentifierHashAlgorithm"; //$NON-NLS-1$

	/** Calificador de la pol&iacute;tica de firma para XAdES. Debe ser una URL.*/
	public static final String PREFERENCE_XADES_POLICY_QUALIFIER = "xadesPolicyQualifier"; //$NON-NLS-1$

	/** Ciudad de firma para firmas XAdES. */
	public static final String PREFERENCE_XADES_SIGNATURE_PRODUCTION_CITY = "xadesSignatureProductionCity"; //$NON-NLS-1$

	/** Provincia de firma para firmas XAdES. */
	public static final String PREFERENCE_XADES_SIGNATURE_PRODUCTION_PROVINCE = "xadesSignatureProductionProvince"; //$NON-NLS-1$

	/** C&oacute;digo de firma para firmas XAdES. */
	public static final String PREFERENCE_XADES_SIGNATURE_PRODUCTION_POSTAL_CODE = "xadesSignatureProductionPostalCode"; //$NON-NLS-1$

	/** Pa&iacute;s de firma para firmas XAdES. */
	public static final String PREFERENCE_XADES_SIGNATURE_PRODUCTION_COUNTRY = "xadesSignatureProductionCountry"; //$NON-NLS-1$

	/** Cargo supuesto para el firmante en firmas XAdES. */
	public static final String PREFERENCE_XADES_SIGNER_CLAIMED_ROLE = "xadesSignerClaimedRole"; //$NON-NLS-1$

	/** Formato de las firmas XAdES.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>XAdES Detached</li>
	 *  <li>XAdES Enveloping</li>
	 *  <li>XAdES Enveloped</li>
	 * </ul> */
	public static final String PREFERENCE_XADES_SIGN_FORMAT = "xadesSignFormat"; //$NON-NLS-1$

	//************************* FIN PREFERENCIAS DE FIRMAS XAdES ***************************************************************
	//**************************************************************************************************************************

	//**************************************************************************************************************************
	//************************* PREFERENCIAS DE FIRMAS PAdES *******************************************************************

	/** Motivo de la firma en firmas PAdES. */
	public static final String PREFERENCE_PADES_SIGN_REASON = "padesSignReason"; //$NON-NLS-1$

	/** Ciudad de firma para firmas PAdES. */
	public static final String PREFERENCE_PADES_SIGN_PRODUCTION_CITY = "padesSignProductionCity"; //$NON-NLS-1$

	/** Contacto del firmante en firmas PAdES. */
	public static final String PREFERENCE_PADES_SIGNER_CONTACT = "padesSignerContact"; //$NON-NLS-1$

	/** Formato de firma PAdES (PAdES B&aacute;sico o PAdES-BES).
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>PAdES-BES</li>
	 *  <li>PAdES B&aacute;sico</li>
	 * </ul>*/
	public static final String PREFERENCE_PADES_FORMAT = "padesBasicFormat"; //$NON-NLS-1$

	/** Identificador de la pol&iacute;tica de firma para PAdES. Debe ser un OID.*/
	public static final String PREFERENCE_PADES_POLICY_IDENTIFIER = "padesPolicyIdentifier"; //$NON-NLS-1$

	/** Huella digital del identificador de la pol&iacute;tica de firma para PAdES. Debe estar en base64.*/
	public static final String PREFERENCE_PADES_POLICY_IDENTIFIER_HASH = "padesPolicyIdentifierHash"; //$NON-NLS-1$

	/** Algoritmo de la huella digital del identificador de la pol&iacute;tica de firma para PAdES.
	 *  Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>SHA1</li>
	 *  <li>SHA-512</li>
	 *  <li>SHA-384</li>
	 *  <li>SHA-256</li>
	 * </ul> */
	public static final String PREFERENCE_PADES_POLICY_IDENTIFIER_HASH_ALGORITHM = "padesPolicyIdentifierHashAlgorithm"; //$NON-NLS-1$

	/** Calificador de la pol&iacute;tica de firma para PAdES. Debe ser una URL.*/
	public static final String PREFERENCE_PADES_POLICY_QUALIFIER = "padesPolicyQualifier"; //$NON-NLS-1$

	/** Si est&aacute; establecido a <code>true</code> se pide al usuario que determine mediante di&aacute;logos
	 * gr&aacute;ficos los par&aacute;metros de una firma visible PDF y se inserta como tal en el
	 * documento. */
	public static final String PREFERENCE_PADES_VISIBLE = "padesVisibleSignature"; //$NON-NLS-1$

	//************************* FIN PREFERENCIAS DE FIRMAS PAdES ***************************************************************
	//**************************************************************************************************************************

	//**************************************************************************************************************************
	//************************* PREFERENCIAS DE FIRMAS CAdES *******************************************************************

	/** Identificador de la pol&iacute;tica de firma para CAdES. Debe ser un OID.*/
	public static final String PREFERENCE_CADES_POLICY_IDENTIFIER = "cadesPolicyIdentifier"; //$NON-NLS-1$

	/** Huella digital del identificador de la pol&iacute;tica de firma para CAdES. */
	public static final String PREFERENCE_CADES_POLICY_HASH = "cadesPolicyIdentifierHash"; //$NON-NLS-1$

	/** Algoritmo de la huella digital del identificador de la pol&iacute;tica de firma para CAdES.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>SHA1</li>
	 *  <li>SHA-512</li>
	 *  <li>SHA-384</li>
	 *  <li>SHA-256</li>
	 * </ul> */
	public static final String PREFERENCE_CADES_POLICY_HASH_ALGORITHM = "cadesPolicyIdentifierHashAlgorithm"; //$NON-NLS-1$

	/** Calificador de la pol&iacute;tica de firma para CAdES. Debe ser una URL.*/
	public static final String PREFERENCE_CADES_POLICY_QUALIFIER = "cadesPolicyQualifier"; //$NON-NLS-1$

	/** Si est&aacute; establecido a <code>true</code> la firma CAdES se realizar&aacute; en modo impl&iacute;cito (<i>attached</i>),
	 *  si est&aacute; establecido a <code>false</code> se realizar&aacute; en modo (<i>detached</i>). */
	public static final String PREFERENCE_CADES_IMPLICIT = "cadesImplicitMode"; //$NON-NLS-1$

	//************************* FIN PREFERENCIAS DE FIRMAS CAdES ***************************************************************
	//**************************************************************************************************************************

	//**************************************************************************************************************************
	//**************** PREFERENCIAS DE FACTURAS ELECTRONICAS *******************************************************************

	/** Nombre de la pol&iacute;tica de FacturaE.
	 *  Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>Pol&iacute;tica de Factura Electr&oacute;nica 3.0</li>
	 *  <li>Pol&iacute;tica de Factura Electr&oacute;nica 3.1</li>
	 * </ul> */
	public static final String PREFERENCE_FACTURAE_POLICY = "facturaEPolicy"; //$NON-NLS-1$

	/** Ciudad de firma para firmas FacturaE. */
	public static final String PREFERENCE_FACTURAE_SIGNATURE_PRODUCTION_CITY = "facturaeSignatureProductionCity"; //$NON-NLS-1$

	/** Provincia de firma para firmas FacturaE. */
	public static final String PREFERENCE_FACTURAE_SIGNATURE_PRODUCTION_PROVINCE = "facturaeSignatureProductionProvince"; //$NON-NLS-1$

	/** C&oacute;digo de firma para firmas FacturaE. */
	public static final String PREFERENCE_FACTURAE_SIGNATURE_PRODUCTION_POSTAL_CODE = "facturaeSignatureProductionPostalCode"; //$NON-NLS-1$

	/** Pa&iacute;s de firma para firmas FacturaE. */
	public static final String PREFERENCE_FACTURAE_SIGNATURE_PRODUCTION_COUNTRY = "facturaeSignatureProductionCountry"; //$NON-NLS-1$

	/** Papel del firmante de las facturas.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>Emisor</li>
	 *  <li>Receptor</li>
	 *  <li>Tercero</li>
	 * </ul> */
	public static final String PREFERENCE_FACTURAE_SIGNER_ROLE = "facturaeSignerRole"; //$NON-NLS-1$

	/** Identificador de la pol&iacute;tica de firma para FacturaE. Debe ser un OID.*/
	public static final String PREFERENCE_FACTURAE_POLICY_IDENTIFIER = "facturaePolicyIdentifier"; //$NON-NLS-1$

	/** Huella digital del identificador de la pol&iacute;tica de firma para FacturaE. Debe estar en base64.*/
	public static final String PREFERENCE_FACTURAE_POLICY_IDENTIFIER_HASH = "facturaePolicyIdentifierHash"; //$NON-NLS-1$

	/** Algoritmo de la huella digital del identificador de la pol&iacute;tica de firma para FacturaE.
	 * Esta preferencia debe tener uno de estos valores:
	 * <ul>
	 *  <li>SHA1</li>
	 *  <li>SHA-512</li>
	 *  <li>SHA-384</li>
	 *  <li>SHA-256</li>
	 * </ul> */
	public static final String PREFERENCE_FACTURAE_POLICY_IDENTIFIER_HASH_ALGORITHM = "facturaePolicyIdentifierHashAlgorithm"; //$NON-NLS-1$

	/** Calificador de la pol&iacute;tica de firma para FacturaE. Debe ser una URL. */
	public static final String PREFERENCE_FACTURAE_POLICY_QUALIFIER = "facturaePolicyQualifier"; //$NON-NLS-1$

	//**************** FIN PREFERENCIAS DE FACTURAS ELECTRONICAS ***************************************************************
	//**************************************************************************************************************************

	//**************************************************************************************************************************
	//**************** PREFERENCIAS DE HUELLAS DIGITALES ***********************************************************************

	/** Algoritmo de la huella digital para la creaci&oacute;n de huellas digitales de ficheros.*/
	public static final String PREFERENCE_CREATE_HASH_ALGORITHM = "createHashAlgorithm"; //$NON-NLS-1$

	/** Formato de la huella digital para la creaci&oacute;n de huellas digitales de ficheros.*/
	public static final String PREFERENCE_CREATE_HASH_FORMAT = "createHashFormat"; //$NON-NLS-1$

	/** Si est&aacute; establecido a <code>true</code> se copiara la huella digital de fichero al portapapeles.*/
	public static final String PREFERENCE_CREATE_HASH_CLIPBOARD = "createHashCopyToClipBoard"; //$NON-NLS-1$

	/** Algoritmo de la huella digital para la creaci&oacute;n de huellas digitales de directorio. */
	public static final String PREFERENCE_CREATE_HASH_DIRECTORY_ALGORITHM = "createHashDirectoryAlgorithm"; //$NON-NLS-1$

	//**************** FIN PREFERENCIAS DE HUELLAS DIGITALES *******************************************************************
	//**************************************************************************************************************************


	//******************************************************************************************************************
	//**************** PREFERENCIAS DE ACTUALIZACION *******************************************************************

	/** Preferencia que designa la URL del fichero remoto con el n&uacute;mero de la versi&oacute;n m&aacute;s reciente de AutoFirma. */
	public static final String PREFERENCE_UPDATE_URL_VERSION = "updater.url.version"; //$NON-NLS-1$

	/** Preferencia que designa la URL de la web para la actualizaci&oacute;n de AutoFirma. */
	public static final String PREFERENCE_UPDATE_URL_SITE = "updater.url.site"; //$NON-NLS-1$

	//**************** FIN PREFERENCIAS DE ACTUALIZACION ***************************************************************
	//******************************************************************************************************************

	/** Recupera el valor de una cadena de texto almacenada entre las preferencias de la
	 * aplicaci&oacute;n.
	 * @param key Clave del valor que queremos recuperar.
	 * @param def Valor que se devolver&aacute;a si la preferencia no se encontraba almacenada.
	 * @return La preferencia almacenada o {@code def} si no se encontr&oacute;. */
	public static String get(final String key, final String def) {
		return preferences.get(key, def);
	}

	/** Recupera el valor {@code true} o {@code false} almacenado entre las preferencias de la
	 * aplicaci&oacute;n.
	 * @param key Clave del valor que queremos recuperar.
	 * @param def Valor que se devolver&aacute;a si la preferencia no se encontraba almacenada.
	 * @return La preferencia almacenada o {@code def} si no se encontr&oacute;. */
	public static boolean getBoolean(final String key, final boolean def) {
		return preferences.getBoolean(key, def);
	}

	/** Establece una cadena de texto en la configuraci&oacute;n de la aplicaci&oacute;n
	 * identific&aacute;ndola con una clave. Para realizar el guardado completo, es
	 * necesario ejecutar el m&eacute;todo {@code flush()}.
	 * @param key Clave con la que identificaremos el valor.
	 * @param value Valor que se desea almacenar. */
	public static void put(final String key, final String value) {
		preferences.put(key, value);
	}

	/** Establece un {@code true} o {@code false} en la configuraci&oacute;n de la aplicaci&oacute;n
	 * identific&aacute;ndolo con una clave. Para realizar el guardado completo, es
	 * necesario ejecutar el m&eacute;todo {@code flush()}.
	 * @param key Clave con la que identificaremos el valor.
	 * @param value Valor que se desea almacenar. */
	public static void putBoolean(final String key, final boolean value) {
		preferences.putBoolean(key, value);
	}

	/** Elimina una clave de entre la configuraci&oacute;n de la aplicaci&oacute;n.
	 * @param key Clave que eliminar. */
	public static void remove(final String key) {
		preferences.remove(key);
	}

	/**
	 * Elimina todas las preferencias de la aplicaci&oacute;n.
	 * @throws BackingStoreException Si ocurre un error eliminando las preferencias.
	 */
	public static void clearAll() throws BackingStoreException {
		preferences.clear();
	}

	/** Almacena en las preferencias de la aplicaci&oacute;n todos los valores
	 * establecidos hasta el momento.
	 * @throws BackingStoreException Cuando ocurre un error durante el guardado. */
	public static void flush() throws BackingStoreException {
		preferences.flush();
	}
}
