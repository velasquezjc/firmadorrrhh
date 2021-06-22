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

import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.Locale;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import es.gob.afirma.core.AOCancelledOperationException;
import es.gob.afirma.core.ui.AOUIFactory;
import es.gob.afirma.standalone.ui.MainScreen;
import es.gob.afirma.standalone.ui.VisorPanel;
import es.gob.afirma.standalone.ui.preferences.PreferencesManager;

/** Ventana para la visualizaci&oacute;n de datos de firma.
 * @author Carlos Gamuci. */
public class VisorFirma extends JApplet implements WindowListener {

    /** Serial ID */
    private static final long serialVersionUID = 7060676034863587322L;

    private Window window;
    private Container container = null;
    private JPanel currentPanel;
    private final Frame parentComponent;

    private final boolean standalone;

    /** Fichero de firma. */
    private File signFile;

    /** Crea la pantalla para la visualizaci&oacute;n de la informaci&oacute;n de la firma indicada.
     * @param standalone <code>true</code> si el visor se ha arrancado como aplicaci&oacute;n independiente,
     *                   <code>false</code> si se ha arrancado desde otra aplicaci&oacute;n Java.
     * @param parent Componente padre. Si no es nulo, se crea el visor como un di&aacute;logo modal respecto
     *               a &eacute;l. */
    public VisorFirma(final boolean standalone, final Frame parent) {
        this.standalone = standalone;
        LookAndFeelManager.applyLookAndFeel();
        this.parentComponent = parent;
    }

    /** Reinicia la pantalla con los datos de una nueva firma.
     * @param asApplet Indica que si se desea cargar la pantalla en forma de Applet.
     * @param sigFile Nuevo fichero de firma. */
    public void initialize(final boolean asApplet, final File sigFile) {

        if (sigFile != null) {
            this.signFile = sigFile;
        }

        // Cargamos las preferencias establecidas
        setDefaultLocale(
    		buildLocale(
				PreferencesManager.get(
					SimpleAfirma.PREFERENCES_LOCALE,
					Locale.getDefault().toString()
				)
			)
		);

        if (asApplet) {
            this.container = this;
        }
        else {
            this.currentPanel = new VisorPanel(
        		this.signFile,
        		null,
        		this,
        		this.standalone
    		);

            if (this.parentComponent == null) {
	           	final MainScreen mainScreen = new MainScreen();
	           	mainScreen.showMainScreen(this, this.currentPanel, 780, 500);
	            this.container = mainScreen;
            }
            else {
            	final JDialog dialog = new JDialog(this.parentComponent);
            	dialog.setModalityType(ModalityType.APPLICATION_MODAL);
            	dialog.setSize(780, 500);
            	dialog.setResizable(false);
            	final Point cp = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
        		dialog.setLocation(cp.x - 780/2, cp.y - 500/2);
        		dialog.add(this.currentPanel);
            	this.container = dialog;
            }

            if (this.window != null) {
                this.window.dispose();
            }

            this.window = (Window) this.container;
            if (this.window instanceof JFrame) {
            	((JFrame)this.window).getRootPane().putClientProperty("Window.documentFile", this.signFile); //$NON-NLS-1$
            	((JFrame)this.window).setTitle(SimpleAfirmaMessages.getString("VisorFirma.0") + (this.signFile != null ? " - " + this.signFile.getAbsolutePath() : ""));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            else if (this.window instanceof JDialog) {
            	((JDialog)this.window).getRootPane().putClientProperty("Window.documentFile", this.signFile); //$NON-NLS-1$
            	((JDialog)this.window).setTitle(SimpleAfirmaMessages.getString("VisorFirma.0") + (this.signFile != null ? " - " + this.signFile.getAbsolutePath() : ""));  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            	this.window.setVisible(true);
            }
        }
    }

    private static Locale buildLocale(final String locale) {
        final String[] frags = locale.split("_"); //$NON-NLS-1$
        if (frags.length == 1) {
            return new Locale(frags[0]);
        }
        else if (frags.length == 2) {
            return new Locale(frags[0], frags[1]);
        }
        else {
            return new Locale(frags[0], frags[1], frags[2]);
        }
    }

    /** Establece el idioma de la aplicaci&oacute;n.
     * @param l <code>Locale</code> a establecer. */
    public static void setDefaultLocale(final Locale l) {
        if (l != null) {
            Locale.setDefault(l);
            PreferencesManager.put(SimpleAfirma.PREFERENCES_LOCALE, l.toString());
            SimpleAfirmaMessages.changeLocale();
        }
    }

    @Override
    public void windowClosing(final WindowEvent e) {
        closeApplication(0);
    }

    @Override public void windowOpened(final WindowEvent e) { /* No implementado */ }
    @Override public void windowClosed(final WindowEvent e) { /* No implementado */ }
    @Override public void windowIconified(final WindowEvent e) { /* No implementado */ }
    @Override public void windowDeiconified(final WindowEvent e) { /* No implementado */ }
    @Override public void windowActivated(final WindowEvent e) {  /* No implementado */ }
    @Override public void windowDeactivated(final WindowEvent e) { /* No implementado */ }

    /** Cierra la aplicaci&oacute;n.
     * @param exitCode C&oacute;digo de cierre de la aplicaci&oacute;n (negativo
     *                 indica error y cero indica salida normal. */
    public void closeApplication(final int exitCode) {
        if (this.window != null) {
            this.window.dispose();
        }
        if (this.standalone) {
            System.exit(exitCode);
        }
    }

    /** Carga una nueva firma en el Visor, preguntando al usuario por el fichero de firma. */
    public void loadNewSign() {
    	final File sgFile;
       	try {
       		sgFile = AOUIFactory.getLoadFiles(
       			SimpleAfirmaMessages.getString("VisorFirma.1"), //$NON-NLS-1$
				null,
				null,
				null,
				null,
				false,
				false,
				AutoFirmaUtil.getDefaultDialogsIcon(),
				VisorFirma.this.window
			)[0];
    	}
    	catch(final AOCancelledOperationException e) {
    		return;
    	}
        if (sgFile == null) {
            return;
        }
        initialize(VisorFirma.this.equals(VisorFirma.this.container), sgFile);
    }
}
