/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.keystores;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Provider;
import java.security.cert.X509Certificate;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.JOptionPane;

import es.gob.afirma.core.keystores.KeyStoreManager;
import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.keystores.filters.CertificateFilter;

/** Utilidades para le manejo de almacenes de claves y certificados. */
public final class KeyStoreUtilities {

    private KeyStoreUtilities() {
        // No permitimos la instanciacion
    }

    static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

    /** Nombre de los ficheros de biblioteca de los controladores de la FNMT para DNIe, CERES y TIF
     * que no tienen implementados el algoritmo SHA1withRSA. */
    private static final String[] FNMT_PKCS11_LIBS_WITHOUT_SHA1 = {
    	"DNIe_P11_priv.dll", //$NON-NLS-1$
    	"DNIe_P11_pub.dll", //$NON-NLS-1$
    	"FNMT_P11.dll", //$NON-NLS-1$
    	"UsrPkcs11.dll", //$NON-NLS-1$
    	"UsrPubPkcs11.dll", //$NON-NLS-1$
    	"TIF_P11.dll"   ,
    	"eTPKCS11.dll" //$NON-NLS-1$
    };

    /** Crea las l&iacute;neas de configuraci&oacute;n para el proveedor PKCS#11
     * de Sun.
     * @param lib Nombre (con o sin ruta) de la biblioteca PKCS#11
     * @param name Nombre que queremos tenga el proveedor. CUIDADO: SunPKCS11
     *             a&ntilde;ade el prefijo <i>SunPKCS11-</i>.
     * @param slot Lector de tarjetas en el que buscar la biblioteca.
     * @return Fichero con las propiedades de configuracion del proveedor
     *         PKCS#11 de Sun para acceder al KeyStore de un token gen&eacute;rico */
    static String createPKCS11ConfigFile(final String lib, final String name, final Integer slot) {

        final StringBuilder buffer = new StringBuilder("library="); //$NON-NLS-1$

        if (lib.contains(")") || lib.contains("(")) { //$NON-NLS-1$ //$NON-NLS-2$
        	buffer.append(getShort(lib));
        }
        else {
        	buffer.append(lib);
        }
		buffer.append("\r\n") //$NON-NLS-1$

        // Ignoramos la descripcion que se nos proporciona, ya que el
        // proveedor PKCS#11 de Sun
        // falla si llegan espacios o caracteres raros
              .append("name=") //$NON-NLS-1$
              .append(name != null ? name : "AFIRMA-PKCS11") //$NON-NLS-1$
              // El showInfo debe ser false para mantener la compatibilidad con el PKCS#11 de los dispositivos Clauer
              .append("\r\nshowInfo=false\r\n"); //$NON-NLS-1$

        if (slot != null) {
            buffer.append("slot=").append(slot).append("\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Por un problema con la version 10 del driver de la FNMT para el DNIe y tarjetas CERES
        // debemos deshabilitar el mecanismo del algorimto de firma con SHA1 para que lo emule
        for (final String problematicLib : FNMT_PKCS11_LIBS_WITHOUT_SHA1) {
        	if (problematicLib.equalsIgnoreCase(new java.io.File(lib).getName())) {
        		buffer.append("disabledMechanisms={ CKM_SHA1_RSA_PKCS }\r\n"); //$NON-NLS-1$
        		break;
        	}
        }

        LOGGER.info("Creada configuracion PKCS#11:\r\n" + buffer.toString()); //$NON-NLS-1$
        return buffer.toString();
    }

    private static final int ALIAS_MAX_LENGTH = 120;

    /** Obtiene una mapa con las descripciones usuales de los alias de
     * certificados (como claves de estas &uacute;ltimas). Se aplicar&aacute;n los
     * filtros de certificados sobre todos ellos y se devolver&aacute;n aquellos
     * certificados que cumplan con los filtros definidos.
     * @param aliases
     *        Alias de los certificados entre los que el usuario debe
     *        seleccionar uno
     * @param ksm
     *        Gestor de los almac&eacute;nes de certificados a los que pertenecen los alias.
     *        Debe ser {@code null} si se quiere usar el m&eacute;todo para seleccionar
     *        otra cosa que no sean certificados X.509 (como claves de cifrado)
     * @param checkPrivateKeys
     *        Indica si se debe comprobar que el certificado tiene clave
     *        privada o no, para no mostrar aquellos que carezcan de ella
     * @param showExpiredCertificates
     *        Indica si se deben o no mostrar los certificados caducados o
     *        a&uacute;n no v&aacute;lidos
     * @param certFilters
     *        Filtros a aplicar sobre los certificados.
     * @return Mapa que asocia los alias reales de los certificados que han pasados los
     * filtros con un nombre mas amistoso. */
    public static Map<String, String> getAliasesByFriendlyName(final String[] aliases,
                                                               final KeyStoreManager ksm,
                                                               final boolean checkPrivateKeys,
                                                               final boolean showExpiredCertificates,
                                                               final List<? extends CertificateFilter> certFilters) {

        // Creamos un mapa con la relacion Alias-Nombre_a_mostrar de los
        // certificados
    	final String[] trimmedAliases = aliases.clone();
        final Map<String, String> aliassesByFriendlyName = new Hashtable<String, String>(trimmedAliases.length);
        for (final String trimmedAlias : trimmedAliases) {
            aliassesByFriendlyName.put(trimmedAlias, trimmedAlias);
        }

        String tmpCN;
        if (ksm != null) {

        	X509Certificate tmpCert;
            for (final String al : aliassesByFriendlyName.keySet().toArray(new String[aliassesByFriendlyName.size()])) {
                tmpCert = null;

                try {
                    tmpCert = ksm.getCertificate(al);
                }
                catch (final RuntimeException e) {

                	// Comprobaciones especifica para la compatibilidad con el proveedor de DNIe
                	if ("es.gob.jmulticard.ui.passwordcallback.CancelledOperationException".equals(e.getClass().getName()) || //$NON-NLS-1$
                		"es.gob.jmulticard.card.AuthenticationModeLockedException".equals(e.getClass().getName()) || //$NON-NLS-1$
                		"es.gob.jmulticard.jse.provider.BadPasswordProviderException".equals(e.getClass().getName()) || //$NON-NLS-1$
                		"es.gob.jmulticard.jse.provider.SignatureAuthException".equals(e.getClass().getName())) { //$NON-NLS-1$
                			throw e;
                	}
                    LOGGER.warning("No se ha inicializado el KeyStore indicado: " + e); //$NON-NLS-1$
                    continue;
                }

                if (tmpCert == null) {
                    LOGGER.warning("El KeyStore no permite extraer el certificado publico para el siguiente alias: " + al); //$NON-NLS-1$
                    continue;
                }

                if (!showExpiredCertificates) {
                    try {
                        tmpCert.checkValidity();
                    }
                    catch (final Exception e) {
                        LOGGER.info(
                            "Se ocultara el certificado '" + al + "' por no ser valido: " + e //$NON-NLS-1$ //$NON-NLS-2$
                        );
                        aliassesByFriendlyName.remove(al);
                        continue;
                    }
                }

                if (checkPrivateKeys) {
                    try {
                    	if (!ksm.isKeyEntry(al)) {
                    		aliassesByFriendlyName.remove(al);
                            LOGGER.info(
                              "Se ha ocultado un certificado (emitido por '" + AOUtil.getCN(tmpCert.getIssuerX500Principal().toString()) + "') por no soportar operaciones de clave privada" //$NON-NLS-1$ //$NON-NLS-2$
                            );
                    	}
                    }
                    catch (final Exception e) {
                    	aliassesByFriendlyName.remove(al);
                    	LOGGER.info(
                            "Se ha ocultado un certificado (emitido por '" + AOUtil.getCN(tmpCert.getIssuerX500Principal().toString()) + "') por no poderse comprobar su clave privada: "  + e //$NON-NLS-1$ //$NON-NLS-2$
            			);
                    }
                }
            }

            // Aplicamos los filtros de certificados
            if (certFilters != null && certFilters.size() > 0) {
            	// Tabla para los certificados que si hay que mostrar
            	final Map<String, String> filteredAliases = new Hashtable<String, String>();
                for (final CertificateFilter cf : certFilters) {
                	for (final String filteredAlias : cf.matches(aliassesByFriendlyName.keySet().toArray(new String[aliassesByFriendlyName.size()]), ksm)) {
                		filteredAliases.put(filteredAlias, aliassesByFriendlyName.get(filteredAlias));
                	}
                }
            	aliassesByFriendlyName.clear();
            	aliassesByFriendlyName.putAll(filteredAliases);
            }

            for (final String alias : aliassesByFriendlyName.keySet().toArray(new String[0])) {
            	tmpCN = AOUtil.getCN(ksm.getCertificate(alias));

            	if (tmpCN != null) {
            		aliassesByFriendlyName.put(alias, tmpCN);
            	}
            	else {
            		// Hacemos un trim() antes de insertar, porque los alias de los
            		// certificados de las tarjetas CERES terminan con un '\r', que se
            		// ve como un caracter extrano
            		aliassesByFriendlyName.put(alias, alias.trim());
            	}
            }

        }

        else {

            // Vamos a ver si en vez de un alias nos llega un Principal X.500 completo,
            // en cuyo caso es muy largo como para mostrase y mostrariamos solo el
            // CN o una version truncada si no nos cuela como X.500.
            // En este bucle usamos la clave tanto como clave como valor porque
            // asi se ha inicializado el mapa.
            for (final String al : aliassesByFriendlyName.keySet().toArray(new String[aliassesByFriendlyName.size()])) {
                final String value = aliassesByFriendlyName.get(al);
                if (value.length() > ALIAS_MAX_LENGTH) {
                    tmpCN = AOUtil.getCN(value);
                    if (tmpCN != null) {
                        aliassesByFriendlyName.put(al, tmpCN);
                    }
                    else {
                        aliassesByFriendlyName.put(al, value.substring(0, ALIAS_MAX_LENGTH - "...".length()) + "..."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                else {
                    aliassesByFriendlyName.put(al, value.trim());
                }
            }
        }

        return aliassesByFriendlyName;
    }

	/** Obtiene el nombre corto (8+3) de un fichero o directorio indicado (con ruta).
	 * @param originalPath Ruta completa hacia el fichero o directorio que queremos pasar a nombre corto.
	 * @return Nombre corto del fichero o directorio con su ruta completa, o la cadena originalmente indicada si no puede
	 *         obtenerse la versi&oacute;n corta */
	public static String getShort(final String originalPath) {
		if (originalPath == null || !Platform.OS.WINDOWS.equals(Platform.getOS())) {
			return originalPath;
		}
		final File dir = new File(originalPath);
		if (!dir.exists()) {
			return originalPath;
		}
		try {
			final Process p = new ProcessBuilder(
					"cmd.exe", "/c", "for %f in (\"" + originalPath + "\") do @echo %~sf" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			).start();
			return new String(AOUtil.getDataFromInputStream(p.getInputStream())).trim();
		}
		catch(final Exception e) {
			LOGGER.warning("No se ha podido obtener el nombre corto de " + originalPath + ": " + e); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return originalPath;
	}

	/** A&ntilde;ade los almacenes preferentes (por ahora DNIe 100% Java y CERES 100% Java) a un almac&eacute;n agredado.
	 * @param aksm Almac&eacute;n agredado al que se desea a&ntilde;adir los almacenes preferentes.
	 * @param parentComponent Componente padre para los di&aacute;logos de los almacenes preferentes
	 *                        (solicitud de PIN, confirmaci&oacute;n de firma, etc.).
	 * @return Devuelve {@code true} cuando se ha detectado alguno de los almacenes preferentes,
	 *         {@code false} en caso contrario. */
	public static boolean addPreferredKeyStoreManagers(final AggregatedKeyStoreManager aksm, final Object parentComponent) {
		// Anadimos el controlador Java del DNIe SIEMPRE excepto:
		// -Que se indique "es.gob.afirma.keystores.mozilla.disableDnieNativeDriver=true"
		if (!Boolean.getBoolean("es.gob.afirma.keystores.mozilla.disableDnieNativeDriver")) { //$NON-NLS-1$
			try {
				aksm.addKeyStoreManager(getDnieKeyStoreManager(parentComponent));
				return true; // Si instancia DNIe no pruebo otras tarjetas, no deberia haber varias tarjetas instaladas
			}
			catch (final Exception ex) {
				LOGGER.warning("No se ha podido inicializar el controlador DNIe 100% Java: " + ex); //$NON-NLS-1$
			}
		}

		// Anadimos el controlador Java de CERES SIEMPRE a menos que:
		// -Se indique "es.gob.afirma.keystores.mozilla.disableCeresNativeDriver=true"
		// -El sistema sea Linux
		if (!Boolean.getBoolean("es.gob.afirma.keystores.mozilla.disableCeresNativeDriver") && !Platform.OS.LINUX.equals(Platform.getOS())) { //$NON-NLS-1$
			try {
				aksm.addKeyStoreManager(getCeresKeyStoreManager(parentComponent));
				return true; // Si instancia CERES no pruebo otras tarjetas, no deberia haber varias tarjetas instaladas
			}
			catch (final Exception ex) {
				LOGGER.warning("No se ha podido inicializar la tarjeta CERES: " + ex); //$NON-NLS-1$
			}
		}
		return false;
	}

	private static AOKeyStoreManager getDnieKeyStoreManager(final Object parentComponent) throws AOKeystoreAlternativeException, IOException {
		final AOKeyStoreManager tmpKsm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
				AOKeyStore.DNIEJAVA,
				null,
				null,
				null,
				parentComponent
			);
			LOGGER.info("El DNIe 100% Java ha podido inicializarse, se anadiran sus entradas"); //$NON-NLS-1$
			tmpKsm.setPreferred(true);

			return tmpKsm;
	}

	private static AOKeyStoreManager getCeresKeyStoreManager(final Object parentComponent) throws AOKeystoreAlternativeException, IOException {
		final AOKeyStoreManager tmpKsm = AOKeyStoreManagerFactory.getAOKeyStoreManager(
			AOKeyStore.CERES, // Store
			null,             // Lib (null)
			null,             // Description (null)
			null,             // PasswordCallback (no hay en la carga, hay en la firma
			parentComponent   // Parent
		);
		LOGGER.info("La tarjeta CERES ha podido inicializarse, se anadiran sus entradas"); //$NON-NLS-1$
		tmpKsm.setPreferred(true);
		return tmpKsm;
	}

    /** Obtiene un almac&eacute;n de claves agregando un gestor de <i>callbacks</i> gen&eacute;rico.
     * @param ks Tipo de almac&eacute;n a obtener.
     * @param pssCallBack <i>PasswordCallback</i> para solilcitar la contrase&ntilde;a al usuario.
     * @param provider Proveedor de <code>KeyStore</code>.
     * @param parentComponent Componente padre para la modalidad.
     * @return Almac&eacute;n de claves con un gestor de <i>callbacks</i> gen&eacute;rico instalado.
     * @throws KeyStoreException Si no se puede obtener el almac&eacute;n de claves. */
    public static KeyStore getKeyStoreWithPasswordCallbackHandler(final AOKeyStore ks,
    		                                                      final PasswordCallback pssCallBack,
                                                                  final Provider provider,
                                                                  final Object parentComponent) throws KeyStoreException {
    	final KeyStore.CallbackHandlerProtection chp = new KeyStore.CallbackHandlerProtection(
			new CallbackHandler() {
				@Override
				public void handle(final Callback[] cbs) throws UnsupportedCallbackException {
					for (final Callback callback : cbs) {
						if (callback instanceof PasswordCallback) {
							((PasswordCallback) callback).setPassword(pssCallBack.getPassword());
						}
						else if (callback instanceof TextOutputCallback) {
							final TextOutputCallback toc = (TextOutputCallback)callback;
							switch (toc.getMessageType()) {
								case TextOutputCallback.INFORMATION:
									LOGGER.info("Informacion del dispositivo criptografico: " + toc.getMessage()); //$NON-NLS-1$
									AOUIFactory.showMessageDialog(
										parentComponent,
										toc.getMessage(),
										KeyStoreMessages.getString("KeyStoreUtilities.0"), //$NON-NLS-1$
										JOptionPane.INFORMATION_MESSAGE
									);
									break;
								case TextOutputCallback.ERROR:
									LOGGER.severe("Informacion del dispositivo criptografico: " + toc.getMessage()); //$NON-NLS-1$
									AOUIFactory.showMessageDialog(
										parentComponent,
										toc.getMessage(),
										KeyStoreMessages.getString("KeyStoreUtilities.1"), //$NON-NLS-1$
										JOptionPane.ERROR_MESSAGE
									);
									break;
								case TextOutputCallback.WARNING:
									LOGGER.warning("Informacion del dispositivo criptografico: " + toc.getMessage()); //$NON-NLS-1$
									AOUIFactory.showMessageDialog(
										parentComponent,
										toc.getMessage(),
										KeyStoreMessages.getString("KeyStoreUtilities.2"), //$NON-NLS-1$
										JOptionPane.WARNING_MESSAGE
									);
									break;
								default:
									LOGGER.warning(
										"Recibida informacion del dispositivo criptografico en un formato desconocido: " + toc.getMessageType() //$NON-NLS-1$
									);
							}
						}
						else if (callback instanceof NameCallback) {
							final Object name = AOUIFactory.showInputDialog(
								parentComponent,
								KeyStoreMessages.getString("KeyStoreUtilities.3"), //$NON-NLS-1$
								KeyStoreMessages.getString("KeyStoreUtilities.4"), //$NON-NLS-1$
								JOptionPane.WARNING_MESSAGE,
								null,
								null,
								null
							);
							if (name != null) {
								((NameCallback)callback).setName(name.toString());
							}
							throw new UnsupportedCallbackException(
								callback,
								"No se soporta la solicitud de nombre de usuario para dispositivos criptograficos" //$NON-NLS-1$
							);
						}
						else {
							throw new UnsupportedCallbackException(
								callback,
								"Recibido tipo de callback desconocido: " + callback.getClass().getName() //$NON-NLS-1$
							);
						}
					}
				}
			}
		);
    	final KeyStore.Builder builder = KeyStore.Builder.newInstance(
			ks.getProviderName(),
			provider,
			chp
		);
    	return builder.getKeyStore();
    }

    /** Busca un fichero (o una serie de ficheros) en el <i>LIBRARY PATH</i> del sistema. Deja
     * de buscar en la primera ocurrencia.
     * @param files Ficheros a buscar en el <i>LIBRARY PATH</i>.
     * @return Ruta completa del fichero encontrado en el <i>LIBRARY PATH</i> o <code>null</code> si no se encontr&oacute; nada */
    public static String searchPathForFile(final String[] files) {
        if (files == null || files.length < 1) {
            return null;
        }

        // Si existe el primero con el PATH completo lo devolvemos sin mas
        if (new File(files[0]).exists()) {
            return files[0];
        }

        final StringTokenizer st = new StringTokenizer(Platform.getJavaLibraryPath(), File.pathSeparator);
        String libPath;
        while (st.hasMoreTokens()) {
            libPath = st.nextToken();
            if (!libPath.endsWith(File.separator)) {
                libPath = libPath + File.separator;
            }
            File tmpFile;
            for (final String f : files) {
                tmpFile = new File(libPath, f);
                if (tmpFile.exists() && !tmpFile.isDirectory()) {
                    return libPath + f;
                }
            }
        }
        return null;
    }

}
