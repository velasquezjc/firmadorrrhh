package es.gob.afirma.core.misc;

import java.io.File;
import java.io.FileWriter;
import java.util.zip.CRC32;

import org.junit.Assert;
import org.junit.Test;

/** Pruebas de codificaci&oacute;n en Base64. */
public final class TestBase64 {

	private static String[] TEST_FILES = new String[] {
		
		/*"gde-valido.cer" */
		"firmadoMiranda.pdf"
		

	};
/*"excel.xls", //$NON-NLS-1$
		"prueba2.pdf", //$NON-NLS-1$
		"powerpoint.ppt", //$NON-NLS-1$
		"project.mpp", //$NON-NLS-1$
		"rar.rar", //$NON-NLS-1$
		"visio.vsd", //$NON-NLS-1$
		"word.doc", //$NON-NLS-1$
		"xml.xml" //$NON-NLS-1$*/
	/** Prueba simple de codificaci&oacute;n / decodificaci&oacute;n Base64.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	//@Test
	public void testBase64Encoding() throws Exception {
		for (final String f : TEST_FILES) {
			final byte[] data = AOUtil.getDataFromInputStream(TestBase64.class.getResourceAsStream(f));

			// Calculamos el CRC
			final CRC32 crc = new CRC32();
			crc.update(data);
			final long crcl = crc.getValue();
	
			// Lo pasamos a Base64
			final String tmpB64Bin = Base64.encode(data);

			// Lo pasamos de nuevo a binario
			final byte[] newBin = Base64.decode(tmpB64Bin);

			// Volvemos a calcular el CRC y vemos si coincide con el original
			crc.reset();
			crc.update(newBin);
			Assert.assertEquals(crcl, crc.getValue());
		}
	}
	
	@Test
	public void testBase64Encoding2() throws Exception {
		for (final String f : TEST_FILES) {
			ClassLoader classLoader = getClass().getClassLoader();
			final byte[] data = AOUtil.getDataFromInputStream(classLoader.getResourceAsStream(f));

			//ClassLoader classLoader = getClass().getClassLoader();
			//File file2 = new File(classLoader.getResource("prueba.pdf").getFile());
			
			// Calculamos el CRC
			/*final CRC32 crc = new CRC32();
			crc.update(data);
			final long crcl = crc.getValue();
*/

			// Lo pasamos a Base64
			final String tmpB64Bin = Base64.encode(data);

			File archivo=new File("c:/texto.b64");
            System.out.println(tmpB64Bin);
			//Crear objeto FileWriter que sera el que nos ayude a escribir sobre archivo
			FileWriter escribir=new FileWriter(archivo,true);

			//Escribimos en el archivo con el metodo write
			escribir.write(tmpB64Bin);

			//Cerramos la conexion
			escribir.close();
			
			// Lo pasamos de nuevo a binario
			final byte[] newBin = Base64.decode(tmpB64Bin);

			// Volvemos a calcular el CRC y vemos si coincide con el original
//			crc.reset();
//			crc.update(newBin);
//			Assert.assertEquals(crcl, crc.getValue());
		}
	}
}
