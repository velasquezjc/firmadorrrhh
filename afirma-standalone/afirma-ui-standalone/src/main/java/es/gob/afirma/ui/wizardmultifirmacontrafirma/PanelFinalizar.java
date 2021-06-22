/*
 * Este fichero forma parte del Cliente @firma.
 * El Cliente @firma es un applet de libre distribucion cuyo codigo fuente puede ser consultado
 * y descargado desde www.ctt.map.es.
 * Copyright 2009,2010 Ministerio de la Presidencia, Gobierno de Espana
 * Este fichero se distribuye bajo licencia GPL version 3 segun las
 * condiciones que figuran en el fichero 'licence' que se acompana.  Si se   distribuyera este
 * fichero individualmente, deben incluirse aqui las condiciones expresadas alli.
 */
package es.gob.afirma.ui.wizardmultifirmacontrafirma;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JPanel;

import es.gob.afirma.ui.principal.Main;
import es.gob.afirma.ui.utils.Constants;
import es.gob.afirma.ui.utils.InfoLabel;
import es.gob.afirma.ui.utils.JAccessibilityDialogWizard;
import es.gob.afirma.ui.utils.Messages;
import es.gob.afirma.ui.utils.RequestFocusListener;
import es.gob.afirma.ui.utils.Utils;
import es.gob.afirma.ui.wizardutils.BotoneraInferior;
import es.gob.afirma.ui.wizardutils.BotoneraSuperior;
import es.gob.afirma.ui.wizardutils.ImagenLateral;
import es.gob.afirma.ui.wizardutils.JDialogWizard;

/**
 * Panel explicativo de finalizacion.
 */
final class PanelFinalizar extends JAccessibilityDialogWizard {

	/**
	 * UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Relacion minima para el redimensionado de componentes.
	 */
	@Override
	public int getMinimumRelation(){
		return 9;
	}

	/**
     * Guarda todas las ventanas del asistente para poder controlar la botonera
     * @param ventanas	Listado con todas las paginas del asistente
     */
    public void setVentanas(final List<JDialogWizard> ventanas) {
    	this.setBotoneraSuperior(new BotoneraSuperior(ventanas));
    	this.setBotonera(new BotoneraInferior(ventanas, 3));
    	getContentPane().add(getBotoneraSuperior(), BorderLayout.PAGE_START);
    	getContentPane().add(getBotonera(), BorderLayout.PAGE_END);
    }

    /**
     * Constructor.
     */
    public PanelFinalizar() {
        initComponents();
    }

    /**
     * Inicializacion de componentes
     */
    private void initComponents() {
    	// Titulo de la ventana
    	setTitulo(Messages.getString("Wizard.multifirma.simple.contrafirma.titulo")); //$NON-NLS-1$

    	// Panel con la imagen lateral
        final ImagenLateral panelIzdo = new ImagenLateral();
        if (Main.isOSHighContrast()){
        	panelIzdo.setOpaque(false);
        }
        Utils.setContrastColor(panelIzdo);
        getContentPane().add(panelIzdo, BorderLayout.WEST);

    	// Panel central
        final JPanel panelCentral = new JPanel();
        panelCentral.setBackground(Color.WHITE);
        // si el color de fondo ya no es blanco
        if (Main.isOSHighContrast()){
        	panelCentral.setOpaque(false);
        }
        panelCentral.setLayout(new GridBagLayout());
        Utils.setContrastColor(panelCentral);

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(20, 20, 20, 20);
        c.weightx = 1.0;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;

		 // Etiqueta "felicidades" y "Ha finalizado con..."
		final String textLabel = Messages.getString("Wizard.sobres.final1") + //$NON-NLS-1$
		Constants.HTML_SALTO_LINEA+Constants.HTML_SALTO_LINEA + Messages.getString("Wizard.multifirma.simple.final.descripcion") ; //$NON-NLS-1$

		final InfoLabel finalizeLabel = new InfoLabel(textLabel, false);
		 //Foco al contenido
        finalizeLabel.addAncestorListener(new RequestFocusListener(false));
		panelCentral.add(finalizeLabel, c);

    	getContentPane().add(panelCentral, BorderLayout.CENTER);
    }
}
