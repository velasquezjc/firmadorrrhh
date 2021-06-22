package es.gob.afirma.core.misc;

import org.junit.Assert;
import org.junit.Test;

/** Pruebas de detecci&oacute;n de documentos Microsoft Office 97-2003.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public final class TestOfficeMIMEDetection {

	/** Prueba la detecci&oacute;n de documentos Excel.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testExcelDetection() throws Exception {
		final byte[] file = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("excel.xls")); //$NON-NLS-1$
		final String mime = new MimeHelper(file).getMimeType();
		Assert.assertEquals("El MIME-Type obtenido no es correcto para el fichero Excel: " + mime, "application/vnd.ms-excel", mime); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Prueba la detecci&oacute;n de documentos Word.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testWordDetection() throws Exception {
		final byte[] file = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("word.doc")); //$NON-NLS-1$
		final String mime = new MimeHelper(file).getMimeType();
		Assert.assertEquals("El MIME-Type obtenido no es correcto para el fichero Word: " + mime, "application/msword", mime); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Prueba la detecci&oacute;n de documentos PowerPoint.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testPowerPointDetection() throws Exception {
		final byte[] file = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("powerpoint.ppt")); //$NON-NLS-1$
		final String mime = new MimeHelper(file).getMimeType();
		Assert.assertEquals("El MIME-Type obtenido no es correcto para el fichero PowerPoint: " + mime, "application/vnd.ms-powerpoint", mime); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Prueba la detecci&oacute;n de documentos Project.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testProjectDetection() throws Exception {
		final byte[] file = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("project.mpp")); //$NON-NLS-1$
		final String mime = new MimeHelper(file).getMimeType();
		Assert.assertEquals("El MIME-Type obtenido no es correcto para el fichero Project: " + mime, "application/vnd.ms-project", mime); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/** Prueba la detecci&oacute;n de documentos Visio.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testVisioDetection() throws Exception {
		final byte[] file = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream("visio.vsd")); //$NON-NLS-1$
		final String mime = new MimeHelper(file).getMimeType();
		Assert.assertEquals("El MIME-Type obtenido no es correcto para el fichero Visio: " + mime, "application/vnd.visio", mime); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
