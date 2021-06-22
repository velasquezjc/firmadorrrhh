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

import java.awt.Color;
import java.awt.Toolkit;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import es.gob.afirma.core.misc.Platform;

/** Maneja el LookAndFeel aplicado al aplicativo.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s.
 * @author Carlos Gamuci. */
public final class LookAndFeelManager {

    private static final Logger LOGGER = Logger.getLogger("es.gob.afirma"); //$NON-NLS-1$

	private LookAndFeelManager() {
		// No permitimos la instanciacion
	}

    /** Color de fondo por defecto para los JPanel, JFrame y Applet. */
    public static final Color WINDOW_COLOR = UIManager.getColor("window") !=null ? new Color(UIManager.getColor("window").getRGB()) : Color.WHITE; //$NON-NLS-1$ //$NON-NLS-2$

    /** Indica si el sistema operativo tiene activada una combinaci&oacute;n de colores de alto contraste. */
    public static final boolean HIGH_CONTRAST;

    /** Tama&ntilde;o m&aacute;ximo de las fuentes por defecto antes de considerarse grandes. */
    private static final int LARGE_FONT_LIMIT = 12;

    /** Indica si el sistema operativo tiene activada una combinaci&oacute;n de colores de alto contraste. */
    private static final boolean LARGE_FONT;

    static {
        final Object highContrast = Toolkit.getDefaultToolkit().getDesktopProperty("win.highContrast.on"); //$NON-NLS-1$
        if (highContrast instanceof Boolean) {
            HIGH_CONTRAST = ((Boolean) highContrast).booleanValue();
        }
        // En Linux usmos siempre una configuracion como si se usase combinacion de colores
        // de alto contraste
        else if (Platform.OS.LINUX.equals(Platform.getOS())) {
            HIGH_CONTRAST = true;
        }
        else {
            HIGH_CONTRAST = false;
        }

        final  Object defaultFontHeight = Toolkit.getDefaultToolkit().getDesktopProperty("win.defaultGUI.font.height"); //$NON-NLS-1$
        if (defaultFontHeight instanceof Integer) {
           LARGE_FONT = ((Integer) defaultFontHeight).intValue() > LARGE_FONT_LIMIT;
        }
        // En Linux usmos siempre una configuracion como si se detectase un tamano de fuente grande
        else if (Platform.OS.LINUX.equals(Platform.getOS())) {
            LARGE_FONT = true;
        }
        else {
            LARGE_FONT = false;
        }
    }

    /** Establece el decorado de la aplicaci&oacute;n. */
    public static void applyLookAndFeel() {

    	// Comprobamos si esta activado algun modo de accesibilidad. Si es asi,
    	// usaremos el LookAndFeel del sistema
        final boolean useSystemLookAndFeel = HIGH_CONTRAST || LARGE_FONT;

        if (!useSystemLookAndFeel) {
            UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE); //$NON-NLS-1$
            UIManager.put("RootPane.background", WINDOW_COLOR); //$NON-NLS-1$
            UIManager.put("TextPane.background", WINDOW_COLOR); //$NON-NLS-1$
            UIManager.put("TextArea.background", WINDOW_COLOR); //$NON-NLS-1$
            UIManager.put("InternalFrameTitlePane.background", WINDOW_COLOR); //$NON-NLS-1$
            UIManager.put("InternalFrame.background", WINDOW_COLOR); //$NON-NLS-1$
            UIManager.put("Label.background", WINDOW_COLOR); //$NON-NLS-1$
            UIManager.put("PopupMenuSeparator.background", WINDOW_COLOR); //$NON-NLS-1$
        }

        JFrame.setDefaultLookAndFeelDecorated(true);
        JDialog.setDefaultLookAndFeelDecorated(true);

        // Propiedades especificas para Mac OS X
        if (Platform.OS.MACOSX.equals(Platform.getOS())) {
            UIManager.put("OptionPane.background", WINDOW_COLOR); //$NON-NLS-1$
            UIManager.put("Panel.background", WINDOW_COLOR); //$NON-NLS-1$
            System.setProperty("apple.awt.brushMetalLook", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("apple.awt.antialiasing", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("apple.awt.textantialiasing", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("apple.awt.rendering", "quality"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("apple.awt.graphics.EnableDeferredUpdates", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            System.setProperty("apple.laf.useScreenMenuBar", "true"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Configuracion necesaria para que la aplicacion se muestre correctamente en pantallas HDPI
        else if (Platform.OS.WINDOWS.equals(Platform.getOS()) && HDPIManager.isHDPIDevice()) {
           	setLookAndFeel("Metal"); //$NON-NLS-1$
        }
        // Configuramos el Look&Feel del sistema si se considero necesario por los modos de accesibilidad
        else if(useSystemLookAndFeel){
        	try {
        		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        	}
        	catch (final Exception e2) {
        		LOGGER.warning(
        				"No se ha podido establecer ningun 'Look&Feel': " + e2 //$NON-NLS-1$
        				);
        	}
        }
        else {
        	setLookAndFeel("Nimbus"); //$NON-NLS-1$
        }
    }

    //Define el look and feel
    private static void setLookAndFeel(final String lookandfeelName) {
    	try {
            for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lookandfeelName.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                	return;
             	}
             }

    	}
    	 catch (final Exception e) {
             LOGGER.warning(
                    "No se ha podido establecer el 'Look&Feel' " + lookandfeelName + ": " + e //$NON-NLS-1$ //$NON-NLS-2$
             );
         }
    }
}
