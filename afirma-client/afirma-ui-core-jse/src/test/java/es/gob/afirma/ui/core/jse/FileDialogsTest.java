package es.gob.afirma.ui.core.jse;

import java.io.File;
import java.io.IOException;

import es.gob.afirma.core.misc.Platform;
import es.gob.afirma.core.ui.AOUIFactory;

/** Pruebas de di&aacute;logos sin JUnit.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class FileDialogsTest {

	/** Prueba de selecci&oacute;n simple y m&uacute;ltiples de fichero.
	 * @param args Argumentos.
	 * @throws IOException Cuando ocurre un error al leer los datos. */
	public static void main(final String[] args) throws IOException {
		for (final File f : AOUIFactory.getLoadFiles(
			"Seleccion multiple de ficheros", //$NON-NLS-1$
			Platform.getSystemLibDir(),
			null,
			new String[] { "txt", "log" }, //$NON-NLS-1$ //$NON-NLS-2$
			"Ficheros de texto (*.txt, *.log)", //$NON-NLS-1$
			false,
			true,
			null,
			null
		)) {
			System.out.println(f.getAbsolutePath());
		}
		for (final File f : AOUIFactory.getLoadFiles("Seleccion de fichero", null, null, new String[] { "txt", "log" }, "Ficheros de texto (*.txt, *.log)", false, false, null, null)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			System.out.println(f.getAbsolutePath());
		}
		System.out.println(AOUIFactory.getSaveDataToFile(
			"Hola".getBytes(), //$NON-NLS-1$
			"Ejemplo de guardar", //$NON-NLS-1$
			null,
			null,
			new String[] { "txt" }, //$NON-NLS-1$
			"Ficheros de texto (*.txt)", //$NON-NLS-1$
			null
		));
	}
}

