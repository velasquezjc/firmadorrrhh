/* Copyright (C) 2011 [Gobierno de Espana]
 * This file is part of "Cliente @Firma".
 * "Cliente @Firma" is free software; you can redistribute it and/or modify it under the terms of:
 *   - the GNU General Public License as published by the Free Software Foundation;
 *     either version 2 of the License, or (at your option) any later version.
 *   - or The European Software License; either version 1.1 or (at your option) any later version.
 * Date: 11/01/11
 * You may contact the copyright holder at: soporte.afirma5@mpt.es
 */

package es.gob.afirma.ui.core.jse.certificateselection;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.misc.Platform.OS;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.ui.core.jse.JSEUIManager;

/** Funciones de utilidad del di&aacute;logo de selecci&oacute;n de certificados. */
final class CertificateUtils {

    private CertificateUtils() {
        // No permitimos la instanciacion
    }

    private static final String CERTIFICATE_DEFAULT_EXTENSION = ".cer"; //$NON-NLS-1$

	/** Abre un certificado con la aplicaci&oacute;n por defecto del sistema. Si no
	 * puede hacerlo, permite que el usuario lo almacene en la ruta que desee.
	 * @param parent Componente padre sobre el que se muestran los di&aacute;logos.
	 * @param certificate Certificado que deseamos abrir. */
	static void openCert(final Component parent, final X509Certificate certificate) {

		// Tratamos de abrir el certificado en Java 6
		Class<?> desktopClass;
		try {
			desktopClass = Class.forName("java.awt.Desktop"); //$NON-NLS-1$
		}
		catch (final ClassNotFoundException e) {
			desktopClass = null;
		}

		if (desktopClass != null) {
			try {
				final File certFile = saveTemp(certificate.getEncoded(), CERTIFICATE_DEFAULT_EXTENSION);
				final Method getDesktopMethod = desktopClass.getDeclaredMethod("getDesktop", (Class[]) null); //$NON-NLS-1$
				final Object desktopObject = getDesktopMethod.invoke(null, (Object[]) null);
				final Method openMethod = desktopClass.getDeclaredMethod("open", File.class); //$NON-NLS-1$
				openMethod.invoke(desktopObject, certFile);
				return;
			}
			catch (final Exception e) {
				Logger.getLogger("es.gob.afirma").warning("No ha sido posible abrir el certificado: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// En entornos Java 5 intentamos abrirlo manualmente en Windows
		if (Platform.getOS() == OS.WINDOWS) {
			try {
				final File certFile = saveTemp(certificate.getEncoded(), CERTIFICATE_DEFAULT_EXTENSION);
				new ProcessBuilder(
						new String[] {
								"cmd", "/C", "start", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								"\"" + CertificateSelectionDialogMessages.getString("CertificateUtils.0") + "\"", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								"\"" + certFile.getAbsolutePath() + "\""} //$NON-NLS-1$ //$NON-NLS-2$
				).start();
				return;
			}
			catch (final Exception e) {
				Logger.getLogger("es.gob.afirma").warning("No ha sido posible abrir el certificado: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// Si no podemos abrirlo, lo guardamos en disco
		try {
	    	AOUIFactory.getSaveDataToFile(
    			certificate.getEncoded(),
    			CertificateSelectionDialogMessages.getString("CertificateUtils.1"),  //$NON-NLS-1$
    			null,
    			CertificateSelectionDialogMessages.getString("CertificateUtils.5") + CERTIFICATE_DEFAULT_EXTENSION, //$NON-NLS-1$
    			new String[] { CERTIFICATE_DEFAULT_EXTENSION },
    			CertificateSelectionDialogMessages.getString("CertificateUtils.3"), //$NON-NLS-1$
    			parent
			);
		}
		catch (final IOException e) {
			new JSEUIManager().showConfirmDialog(
				parent,
				CertificateSelectionDialogMessages.getString("CertificateUtils.2"), //$NON-NLS-1$
				CertificateSelectionDialogMessages.getString("CertificateUtils.3"), //$NON-NLS-1$
				JOptionPane.CLOSED_OPTION,
				JOptionPane.ERROR_MESSAGE
			);
		}
		catch (final CertificateEncodingException e) {
			new JSEUIManager().showConfirmDialog(
					parent,
					CertificateSelectionDialogMessages.getString("CertificateUtils.4"), //$NON-NLS-1$
					CertificateSelectionDialogMessages.getString("CertificateUtils.3"), //$NON-NLS-1$
					JOptionPane.CLOSED_OPTION,
					JOptionPane.ERROR_MESSAGE
				);
		}
		catch(final AOCancelledOperationException e) {
			// El usuario ha cancelado la operacion, no hacemos nada
		}
	}

    private static boolean saveFile(final File file, final byte[] dataToSave) throws IOException {
    	final FileOutputStream fos = new FileOutputStream(file);
    	fos.write(dataToSave);
    	fos.close();
    	return true;
    }

    private static File saveTemp(final byte[] data, final String suffix) throws IOException {
    	final File tempFile = File.createTempFile("afirma", suffix); //$NON-NLS-1$
    	tempFile.deleteOnExit();
    	if (saveFile(tempFile, data)) {
    		return tempFile;
    	}
    	return null;
    }
}
