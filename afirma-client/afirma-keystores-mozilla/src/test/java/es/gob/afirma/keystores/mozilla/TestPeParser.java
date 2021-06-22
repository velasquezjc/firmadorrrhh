package es.gob.afirma.keystores.mozilla;

import java.io.FileInputStream;
import java.io.InputStream;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.keystores.mozilla.bintutil.MsPortableExecutable;

/** Pruebas de analizado de ficheros PE/COFF.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s Capote. */
public final class TestPeParser {

	private static final String FILE = "D:\\Program Files (x86)\\Mozilla Firefox\\softokn3.dll"; //$NON-NLS-1$

	/** Prueba b&aacute;sica de an&aacute;lisis de fichero PE/COFF.
	 * @param args No se usa.
	 * @throws Exception En cualquier error. */
	public static void main(final String[] args) throws Exception {
		final InputStream fis = new FileInputStream(FILE);
		final byte[] peBytes = AOUtil.getDataFromInputStream(fis);
		fis.close();
		final MsPortableExecutable pe = new MsPortableExecutable(peBytes);
		System.out.println(pe);

	}

}
