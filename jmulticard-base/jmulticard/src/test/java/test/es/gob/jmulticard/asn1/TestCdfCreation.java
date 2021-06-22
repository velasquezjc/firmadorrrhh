package test.es.gob.jmulticard.asn1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import es.gob.jmulticard.HexUtils;
import es.gob.jmulticard.asn1.der.pkcs15.Cdf;

/** Prueba de creaci&oacute;n de CDF PKCS#15.
 * @author Tom&aacute;s Garc&iacute;a-Mer&aacute;s */
public class TestCdfCreation extends TestCase {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(TestCdfCreation.class.getName());

    private static final int BUFFER_SIZE = 4096;

    private static final String[] TEST_FILES = new String[] {
        "CDF_GSD.BER", //$NON-NLS-1$
        "CDF_EEE.BER", //$NON-NLS-1$
        "CDF_GVA.BER", //$NON-NLS-1$
        "CDF_JBM.BER", //$NON-NLS-1$
        "CDF_JMA.BER", //$NON-NLS-1$
        "CDF_TGM.BER", //$NON-NLS-1$
        "CDF_TUI_SAMPLE1.asn1" //$NON-NLS-1$
    };

    /** Prueba la creaci&oacute;n de CDF con volcados en disco.
     * @throws Exception En caso de cualquier tipo de error */
    @Test
    public static void testCdf() throws Exception {
        byte[] cdfdata;
        for (final String element : TEST_FILES) {
            cdfdata = getDataFromInputStream(ClassLoader.getSystemResourceAsStream(element));
            LOGGER.info("\n\nCDF completo (" + Integer.toString(cdfdata.length) + "):"); //$NON-NLS-1$ //$NON-NLS-2$
            LOGGER.info(HexUtils.hexify(cdfdata, true));
            final Cdf cdf = new Cdf();
            Assert.assertNotNull(cdf);
            cdf.setDerValue(cdfdata);
            LOGGER.info("\n" + cdf.toString()); //$NON-NLS-1$
        }
    }

    /** Lee un flujo de datos de entrada y los recupera en forma de array de
     * bytes. Este m&eacute;todo consume pero no cierra el flujo de datos de
     * entrada.
     * @param input
     *        Flujo de donde se toman los datos.
     * @return Los datos obtenidos del flujo.
     * @throws IOException
     *         Cuando ocurre un problema durante la lectura */
    private static byte[] getDataFromInputStream(final InputStream input) throws IOException {
        if (input == null) {
            return new byte[0];
        }
        int nBytes = 0;
        final byte[] buffer = new byte[BUFFER_SIZE];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((nBytes = input.read(buffer)) != -1) {
            baos.write(buffer, 0, nBytes);
        }
        return baos.toByteArray();
    }
}