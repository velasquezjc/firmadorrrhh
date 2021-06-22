package es.gob.afirma.standalone.configurator;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/** Pantalla principal de la aplicaci&oacute;n. Muestra una consola para la notificaci&oacute;n de las accesiones
 * llevadas a cabo por el navegador. */
final class GraphicConfiguratorConsole extends JFrame implements Console {

	private static final int DEFAULT_WINDOW_WIDTH = 480;
	private static final int DEFAULT_WINDOW_HEIGHT = 350;

	/** Serial Id. */
	private static final long serialVersionUID = 398187262022150395L;

	private final JTextArea console;

	private final ConsoleListener listener;
	ConsoleListener getConsoleListener() {
		return this.listener;
	}


	/**
	 * Crea la pantalla.
	 * @param l Tipo de pantalla a crear.
	 */
	GraphicConfiguratorConsole(final ConsoleListener l) {
		this.listener = l;
    	this.console = new JTextArea();
	}

	/** Muestra la pantalla principal de la aplicaci&oacute;n. */
    @Override
	public void showConsole() {
    	SwingUtilities.invokeLater(
			new Runnable() {
				@Override
				public void run() {
					createUI(
						new WindowListener() {
						    @Override
						    public void windowClosing(final WindowEvent we) {
						    	if (getConsoleListener() != null) {
						    		getConsoleListener().close();
						    	}
						    }
						    @Override public void windowOpened(final WindowEvent we) { /* No implementado */ }
						    @Override public void windowClosed(final WindowEvent we) { /* No implementado */ }
						    @Override public void windowActivated(final WindowEvent we) { /* No implementado */ }
						    @Override public void windowIconified(final WindowEvent we) { /* No implementado */ }
						    @Override public void windowDeiconified(final WindowEvent we) { /* No implementado */ }
						    @Override public void windowDeactivated(final WindowEvent we) { /* No implementado */ }
						},
						DEFAULT_WINDOW_WIDTH,
						DEFAULT_WINDOW_HEIGHT
					);
				}
			}
		);
    }

    void createUI(final WindowListener wlist, final int width, final int height) {
        if (!LookAndFeelManager.HIGH_CONTRAST) {
            this.setBackground(LookAndFeelManager.WINDOW_COLOR);
        }
        this.setTitle(Messages.getString("ConfiguratorConsole.0")); //$NON-NLS-1$
        this.setSize(width, height);
        this.setLayout(new GridBagLayout());
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        if (wlist != null) {
            this.addWindowListener(wlist);
        }

        this.console.setMargin(new Insets(5,  5,  5,  5));
        this.console.setEditable(false);

        final JScrollPane scrollPane = new JScrollPane(this.console);

        final GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(11,  11,  11,  11);

        this.add(scrollPane, c);

        try {
            setIconImage(
        		Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/afirma_ico.png")) //$NON-NLS-1$
            );
        }
        catch (final Exception e) {
            Logger.getLogger("es.gob.afirma").warning("No se ha podido cargar el icono de la aplicacion: " + e);  //$NON-NLS-1$//$NON-NLS-2$
        }

        this.setVisible(true);
    }

    /** Muestra un texto por consola.
     * @param text Texto a mostrar. */
    @Override
	public void print(final String text) {
    	try {
    		this.console.append(text);
    		this.console.append("\n"); //$NON-NLS-1$
    	}
    	catch (final Exception e) {
    		Logger.getLogger("es.gob.afirma").warning("No se pudo mostrar por consola el mensaje:\n" + text);  //$NON-NLS-1$//$NON-NLS-2$
    	}
    }


	@Override
	public Component getParentComponent() {
		return this;
	}
}
