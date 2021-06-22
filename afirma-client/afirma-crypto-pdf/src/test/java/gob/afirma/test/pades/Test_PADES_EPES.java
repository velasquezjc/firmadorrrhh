/**
 * 
 */
package gob.afirma.test.pades;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.KeyStore.PrivateKeyEntry;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import es.gob.afirma.core.misc.AOUtil;
import es.gob.afirma.core.misc.Base64;
import es.gob.afirma.core.signers.AOSignConstants;
import es.gob.afirma.signers.pades.AOPDFSigner;
import es.gob.afirma.signers.tsp.pkcs7.TsaParams;

/**
 * @author qwerty2
 *
 */
public class Test_PADES_EPES {
	//datos para el sello de tiempo
	private static final String CATCERT_POLICY = "0.4.0.2023.1.1"; //$NON-NLS-1$
	private static final String CATCERT_TSP = "http://psis.catcert.net/psis/catcert/tsp"; //$NON-NLS-1$
	private static final Boolean CATCERT_REQUIRECERT = Boolean.TRUE;

	//archivo a firmar
	private final static String TEST_FILE = "PADES-EPES.pdf"; //$NON-NLS-1$

	//NOTA: el algoritmo de firma sha1 con RSA ya es obsoleto pero la onti aun no actualizo o 
	//emitio un nuevo certificado	
	private final static String DEFAULT_SIGNATURE_ALGORITHM = "SHA1withRSA"; //$NON-NLS-1$

	//certificado del firmante
	private final static String CERT_PATH = "autofirma.pfx";//"PFActivoFirSHA256.pfx"; //$NON-NLS-1$
	private final static String CERT_PASS = "654321";//"12341234"; //$NON-NLS-1$
	private final static String CERT_ALIAS = "socketautofirma";//"fisico activo prueba"; //$NON-NLS-1$


	/** Prueba de firma PADES-EPES.
	 * @throws Exception En cualquier error. */
	@SuppressWarnings("static-method")
	@Test
	public void testSignTime() throws Exception {

		final byte[] testPdf = AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream(TEST_FILE));
		final AOPDFSigner signer = new AOPDFSigner();

        final KeyStore ks = KeyStore.getInstance("PKCS12"); //$NON-NLS-1$
        ks.load(ClassLoader.getSystemResourceAsStream(CERT_PATH), CERT_PASS.toCharArray());
        final PrivateKeyEntry pke = (PrivateKeyEntry) ks.getEntry(CERT_ALIAS, new KeyStore.PasswordProtection(CERT_PASS.toCharArray()));

        final String prueba = "Firma PAdES-EPES de PDF"; //$NON-NLS-1$
        System.out.println(prueba);
        
        final Properties extraParams = new Properties();
       // extraParams.put("signTime", "2017:06:03:12:30:01"); //$NON-NLS-1$ //$NON-NLS-2$
        
        //atributos de la firma
        extraParams.setProperty("format", AOSignConstants.SIGN_FORMAT_PDF); //$NON-NLS-1$
        //extraParams.setProperty("mode", AOSignConstants.SIGN_MODE_EXPLICIT); //no es necesario se ignora en pades //$NON-NLS-1$
        extraParams.setProperty("signReason", "test"); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.setProperty("signatureProductionCity", "Bs.As."); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.setProperty("signerContact", "rrhh@desa.gov.ar"); //$NON-NLS-1$ //$NON-NLS-2$
        //politica
        extraParams.setProperty("policyQualifier", "https://desarrollo.gov.ar/politicafirma/politica_firma_v1.0.pdf"); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.setProperty("policyIdentifier", "2.16.724.1.3.1.1.2.1.8"); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.setProperty("policyIdentifierHash", "8lVVNGDCPen6VELRD1Ja8HARFk=="); //$NON-NLS-1$ //$NON-NLS-2$
        //se puede calcular el hash con
        //new String(Base64.encode(MessageDigest.getInstance("SHA1").digest(AOUtil.getDataFromInputStream(ClassLoader.getSystemResourceAsStream(POL_PATH))))) //$NON-NLS-1$
        extraParams.setProperty("policyIdentifierHashAlgorithm", "SHA1"); //$NON-NLS-1$ //$NON-NLS-2$
       
        //Se indica que el tipo de firma es CADES con modo explicito
        extraParams.setProperty("signatureSubFilter", "ETSI.CAdES.detached"); //$NON-NLS-1$ //$NON-NLS-2$
        
        //---------Ej, firma modo agregando una pagina y utilizando el render de firma default
        //Al no agregar nada en la capa 2 y 4 de la firma,pero si seteo un rectangulo visible
        //se muestra,firmadop por,fecha,motivo,lugar de firma
        //El renderizado de la firma puede ser la descripcion, el nombre del firmante y la 
        //descripcion, imagen y la descripcion.
        //La clase que maneja el render es afirma-crypto-pdf.....PdfSessionManager.java
        //el modo del render se deberia poder parametrizar, mejora a futuro
        
        //para agregar una pagina en blanco al pdf
        //FIX: la agregacion de una pagina debe ser hecha antes de realizar el sello
        //sino tira el adobe como documento modificado despues del sello
        //cuando se realiza la validacion desde adobe
        //extraParams.setProperty("signaturePage", "append"); //$NON-NLS-1$ //$NON-NLS-2$
        //para indicar la posicion de la firma
        extraParams.setProperty("signaturePositionOnPageLowerLeftX", "60"); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.setProperty("signaturePositionOnPageLowerLeftY", "60"); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.setProperty("signaturePositionOnPageUpperRightX", "550"); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.setProperty("signaturePositionOnPageUpperRightY", "130"); //$NON-NLS-1$ //$NON-NLS-2$
        
		//--------------------------Ej, firma con rubrica de imagen
        //para agregar una imagen de firma, por ejemplo una firma escaneada,         
        //extraParams.setProperty("signatureRubricImage", "/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAgGBgcGBQgHBwcJCQgKDBQNDAsLDBkSEw8UHRofHh0aHBwgJC4nICIsIxwcKDcpLDAxNDQ0Hyc5PTgyPC4zNDL/2wBDAQkJCQwLDBgNDRgyIRwhMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjIyMjL/wAARCAEcATEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD3+iiigAooooAKKKKACiiigAooooAKKKKACiiigAooozQAUUVBeXdtZWslxdXEVvBGMySyuEVR6kngUAT0ZA71ws/xX8LrcfZ9PkvdWlyRs020eb9cAH8DTh8TtJQj7dpXiDT4jx5l1pkir+YzQB3FFZuja9pOv2n2nSdQt7yEcM0T5Kn0YdQfY81pUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFBoA5/xj4qtfCHh6TVLlDK29YoIFPM0jZwoODjgE/gaq+EbXxMsdzqHia7he6u9hjs4FAitUGTtz1LHcc9fujk1keMUF/wDErwNpr4eCOS6vpY+eGjQGNj7Bs9aj1XVNS8ca3deHPD9w1ppdsTHquqIPmJPBhhPI3Y6t2/LIBc1bxrPd6nJoXg+0TVNVTiediRa2fp5jjqePug84NNsfhxDdype+LtRudfvAd4inci1hY9dkQwMcY5zXU6Hoen+HtNi0/TLWO2tox91Ryx7sx6knA5NaVAEFvawWkCQW0EcEScKkSBVUewAxUvJ7U6igDmNb8EaXrFwb6JZLDVQpEeoWbGOVT744bp/F+YqHQ9S13TrwaV4miilLNttNUgTbHcD+FXX/AJZyY7dDyB79bVe9tIL+zltbmJZYZVKOjdCDQBMv4/j2p2RnGelYVlJNo00Om30zTW7/ACWl1KcsxAJ8uQ92AHDfxDOeRk7a8n8M89qAHUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABSGloNAHj/j3VJ9K+J6S2gZ9Sl0L7NpoH8NxLcFQ3pwAT+GK9G8K+Hrbwv4ftdKtgD5S5kk7yyHlnJ7kn9MDtXl2oxnUf2kLWCVmCwJG6gnIwkRkAA/3ua9qH40ALRRRQAUUUUAFFFFAFe+tIb6zktpxmOQYOOo9CD2IPOar6bNMGktLt1e5iAbzFGBIhJCt7HjkevsRV9hkVVktUe4S58sGeIFUb2JG4Z9DgH8BQBbopBS0AFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRSN0oA8YuZPK/aXgZzhWiCc8ZJt2xj8eK9nFeH+NEisvGXiXxJ5amfRLjSroDfzJEcq4x26j8q9vQgqCpBGOCO9ADqKKKACiiigAooooAKKKKACiigEEZBzQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFI3TnpS0h6UAeZX+k/2t8QfGWjMVA1TQYsbuRuG6NX9sE10Xw21g614C0qeTP2iCH7LcBvvCSL5Gz7naD+NUWZofjkhYHZP4d2K3qy3BJ/Rqg8MtH4b+I3iDw7ITHBqhXV7BCcglvlnHsQwBA9MmgD0GikHU+1MmkjiheSV1SNFLOzEAKB1Jz2oAkyPWjIrz2b4mrqF01p4Q0G+8RtGwWSeEiG2B9PNbj9Me9b/AIdvPFV7GZNe0ax08E8JFeGSRR7gKVP/AH1QB0eaKaoIP4U6gAooooAQ9KjjkzK8R+8uD+B/yakNUrl5ItRs3D4iffE6kdSRuU59thH/AAKgC9RSAgnA+tLQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRUF3dW9nbSXFzPFBDGMvLK4VUHqSeBQBPSEjFcofFtxqq7fDOkXGojOBeT/AOj2313sNzgf7KkVB/wjHiPWCza/4klhgI4s9HXyF98yHLsD6ZH9KAKnim6tdL+JfhO+ubu3toZLe9t53nkVBt2qy5J/2gR+Nc38QPGfh6aXTNd0TVEudX0O5Eu2OGRw8DjbKhYLgAjHPbHvWj4n8F+HvD8nhi4s9JhyNbgjlmkzIzI4YHcWJz822vSLi1gubSW0njVreWMxOnYqRgj24NAHNj4ieHDGkonv/Idd6zf2bc7CD3zs6YPFcdf6/YfErxEdGi1m3s/DVuymb975U2oycHYobDBAeDjkn6giDTdU1e18Oy/Dm1uH/wCEit7prBLhlZvKssbxcHsBsOwc5ztr0aw8IaJY+H7XRP7PguLS2TaouYxIWb+JiSDyc9aANXT7C10yzis7K3jt7WFdscUa4VR7CrIIPQ5rlF8JzaK6y+Gb17SJTk6bcMZLV/YZy0X1Tp/dNamia5HqvnRTQyWV/b4FzZzEboyehB6Mpxww4PseAAbFFFFABRRRQAVU1NC2nTMsRleMCVEU4LMp3KPzAq3SN0oAbE6yIsiHKsAyn1B5p9UtLmM1l823MckkR29PkdlH8qu0AFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABSNyMVDeXUFlaS3NzNHDDEu6SSRgqqvqT2riRNq/xA8wW0s+leF2GPPQFLu+H+xkfu4j6/eOOwNAF+78XPeXlzpfhiyOqahASks7nZa2z+kkgGSR/dQEnnpRY+DDdut34ruV1q+B3qkke21gPpHFkjj+8eTj610GlaXZ6NYRWVhbpBbxjARFxk8cn1J9TzV6gBkaCNQqgAAYAAxxT6KKAOT+JVtLceANUeAsJrZVukZeo8tg5P5Ka6OO4hksUuVYCBoxIGfgbSM859qTUrOLUdMu7GfPk3MLwyY/usCD+hrzdNYudR+EOh6dE+y/1lY9JDIASuCUmfB9ERz7GgDK03V7oeONO8fTSKNH164fSEjMWDFGCRCxOerNGSfTNezLXK+LfC8Oq/D+90GwgSLy7YCyRBgRvHgxgenKgZ+tX/AAfro8S+E9N1jZse5hzIn91wSrj/AL6BoA226Vg+IdLnljXVNNjUataDMRHHnJnLQse6sBwOzYPat+kPSgCrpl9DqenW1/bljDcRLIu4YYZAOCOxHQj1q3XN+HSNO1jWNCZvljl+3WwJx+5mLEgD0WRZPwK10lABRRRQAUjdKWkNAFDTTi51KIbdsd1wB/tIjn9WNaFZOnsV1/V4+dp8mXp3Kbf/AGQVrUAHSs258Q6LZxiS61jT4EJIDS3KKMjg8k1bvbVL6xntJC6xzxtGxQ4YBgQcHsea4Ow+CvgeyVd+lPduDkPczOfwIBCnp3BoA6MeOvCJbb/wlOiZ6f8AIQi/+Kqe38X+Gbt9lt4i0iZsZ2x3sbHH4NUUXgrwtAMReG9IUdeLKPn8dtL/AMIZ4YI2nw5pBHvYx/8AxNAGzDcQXMYkgmjljP8AFGwYfmKkzXPnwR4VxhfDekqP9izjU/oKli8J6Hb/APHvp0dv7wlkP5gigDboqlY6dBYtIYTcfOckTXEkuPpuY4/CrtABRRRQAUUUUAFFFFABRRRQAUUUUAFVNS1Gz0nTp7+/uY7e1t03yyyHhR/j6epq02NvtXAWEZ8fa+dVmbf4e0u4K2EQPy3c68NO3ZkU5CDpkE0AT2ulXnjW6XUtfhkg0ZDusdIk4Mg7S3A7nuE6AYzznPbIoUAAYA4FKowemOOnanUAFFFFABRRRQAjdK828NadPL8SNUtpoojZaC8r2m0Y2yXZEpOPZcj/AIFXpLdPoa848CzSv4t1HU2ufOtvECz3EGcDaLabygOnOUdD+FAHop6dPeuN8GxpoXiPxD4a2eXEs41KzGeDDN94AdgsiuMf7QrtD0rhfHDNoWveHvFERKpFcDT77AJ3QTEAFsdlcKR7mgDu6Q9Kan/1qfQBymuu+m+MfDupAEJdNJps+BnO9fMT8mjP/fRrqlrkPiFctBZaEIoWlmfW7RUCnlcMWJ/75Vh+NdcOpNADqKKKACkJwKWkPAoAyrUr/wAJVqSqwJFpbFhnod039MVrVz+mSiXxpr2AuIoLSI885/eNz/32K6CgAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACkbpS0jdPxoA5Dxze3c1pb+HNLbbqWsM0IkzjyYAB5sn4A4HuwrpdOsbbTLC3sbOIRW1vGsUUa9FUDAH6VyPh8NrfxH1/W2GbfTUXSLVhjBYYeYkdchiFz6Cu4AwaAFooooAKKKKACiiigChrd0bDQdQvFzmC2klGOvyqT/SuDsLM6P4X+H2pQRyf6MYIZ/MOMJdIFYn/ALaMldl4ujabwZrkSHDvp86g+hMbVlppx134W2lnHxJPpcLQsD92QRqyH8GCmgDq+oIODXmvh+XUfiJ8Ntc03XkRdRW5ntDmPy9siENGSOxDY/Ku+0u/TVdIstQjGEuoEmUd13AH+tct4SQ6X448YaT9yKS4g1KJSR83nJtcjjoHjP5igDc8I61/wkPhbTtUPEs0IEy4xtlX5XHthgR+Fal5PBa2ktxcuqQxKXdm6ADk1y/hRTpfiXxLobE+ULldQtd3/POYfMFHoJEf8661huXHY9aAOd021u9W1SPW9RgktY4UKWNk+N0YJ+aSQDo5AAA/hHuSB0S+4xxQvXvTqACiiigApG6daWo55o7aCSaZ1SONS7sx4UAZJoA53wxIt1rfii8HQ6ituPpHDGP5lq6auZ8BJK3hK1vZ0VZ9QeS+fA6+a5cZ+iso/CumoAa3TpmuF8V+PNU8HXEtxqfhaefRAwVL+yuFkbkfxxkDb9ckdu9d5SHpQBR0fUV1bSrS/SCeBLmFZRFOm10BAIDDsav00dec06gAooooAKKKKACiiigAooooAKKKKACiiigApGGVwenelpD7UAcT8N2JTxUDn5fEd6APT5lP9c/jXb1wfhoroXxI8S6LK3lRal5eqWSNg+YSNs5B7ncF4POPau8yD3oAKKKKACiiigAooooAjniSeB4pFDI6lWU9weD+lcn8N2ki8GQabcAJdaVJJYTgHODGxC/mhRv+BV156VwutlvBfiifxQsUj6LfpHHqyxKSbd0yEuMDkrg7WA5wAeelAG14X3WyappjgILK/lWMA/8ALN8TLj2Ak2/8B9qz7y1Fl8VtK1P5sX+lT2Jx90NG6Srn32mX8qu28tu/iO11S0ljms9Ws/K86M7ldoyXiwR1yrTc/wCyKg8eRTR6FFq9sGafRrpNQCqcF40BEy/jE0nHrigCPxCRpPjHw7rQAEdzI+lXJA5Ik+aL8pEx/wACrrV64rn/ABZpv/CS+Dby3s5A0skS3FpIh/5aKQ8ZB9yBz71o6FqkWt6HY6pDxHdwJNtznaWAJU+46H6UAaNFFFABRRRQAh/nXJ+PLiSfRYtAtJCl7rcgtItnVYzzK30CbvzFdPc3ENrbS3E8qRwxKXkd2AVFAyST2AHJNcl4WSXxFq0/i68tnihkT7PpMUoIZbfOWlI7GQ4P+6q0Addbwx28KQwoEjjUKiL0UAYA/IVLSLzzjFLQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABSNypHP4UtFAHL+L9Bu9RtrXU9KCDXNJka4sS3CyEjDRN0+V14JyMHBq94Y8Q2niXRo9QtleJgTHcW8nD28o+9G47MD/Q962W6Vx2s+G76w1iTxL4YCjUnVVvLB22Ragq5wCf4ZBn5X/A8E0AdlkZxnmisDw54q0/xCkscRe21C3wt1p9wNs1u3+0vp6MOD61v0AFFFFABRRRQAUyRFkQoyhlPBB6Ee9PooA881fwnqPh1JtS8JPH9lilF3Jokyny2ZDuJgYZMTMAVxgqd3QVraT480bU7ldNvvN0rUpFH+g6koidw3TYfuuDkgbSc11jdKyLjw3pd9o0WlahZRXtpEu1EuUD7cDAIyOCBwCMGgDB8PTv4VvT4a1Ro47N5WOjXG47XizkQMT/AMtFzgf3lxj7pqz4OU6ZqGu+H2G1bO7NxbDGALefLqB7B/MX8KqX/wAPFewuLPS9b1CztpRg2tyReW45JyqyZZT6bXGK5m+svHPhDW9O1Aalpuri4/4lUTXavERuO5N5GSeVIySx+bvQB69RnjNchZ614tWzRLzwh/pePnNvfQ+UTntuORxjqKtvH4q1NkjkNlpFqeZHt5jcXB5HClkVE784f29aAOkyPWo5pY4oWlkkRI0G5mcgKAOSST0FY+t+ItM8N2sbahcN5r4WG3T55p29FQcsT+Xqawk0TVfGUqXXiZHstI4aLRY5DmTuGuGH3vXYMAcZyc0AQRtJ8R7os0ckfhGB8rkFTqbqcg46+SCP+BECu9RdoAAwAMYxSRRrCixooRFUKqqAFH0FSUAFFFGRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUjdKWigDnvEHhDS/EDpPMk1rqEQxDqFm/lTxfRh1HbBBFcRqXxJu/h3q8Wi+KLiPWlkXfFc2cfl3CJ282M/KT15U9uRzXq7DI/+tVa5sba8QpdWsM6kYxLGGGPTmgDmNF+KPgvWoRLD4gs4HPWK8kEDg+mGxn8M109tqdhejNrfW04xnMUqtx+BrMu/B/hvUEK3fh/TJgRjL2iZ/A4yPwrl7v4I+CLgqYNPnsmU8Pb3L5/JsigD0RWVgCrAg9CD1pa8pj+C9rZvI1tc2l2jHIj1G1diPbdHIn8qjT4ZyW7h5fBvhi8A6eXf3MJ/JlYfrQB6tLcQQLummjjHq7AfzrMm8U+HoCRNr2lxkHnfeRjH5muCTwn9nk3j4U6NIRwD/aSP/6FGKuR2N/bA/Z/hRpKHtsu7ZR+ezNAG9cfEbwdbMQ/iKycjtBIZf8A0DNRf8LG0KZc2UeqX7dQtrpVwxP5pilttQ8XrGqQ+D9Mtvl6Nq2Avt8sJpsjfEFzuC+GLaMDne08pH/oIoAVfHTyNtg8JeJ5O2TYiMf+PsKzvE1x4h8R6BdadD4Nu4/NUNDLc38EbRyKQyPhWY/KwB69sVqJoniu7Aa48YLCjc7bDTo0wPQNIX/PFH/CA6bMP+Jpfatqozkpe3rmMn12IVX9KAOai+Jmp3Wk21zBa6MbuRW3WEV3LcXBdeCNiR/Kc+p/Gr1pc/E7XIXL22i6BA4wryo8868cEIG2fg35Va8NW9t4Y8Zar4cgijhtLxBqViqqFAHCyx/g2GAA4DnNdwMZoA4DRvA+u6WrXL65YT6pKcy6jcaa007ewYygKvsAK1m0LxRImD4weNgeWi02LDfg2a6qigDmB4e8QZy3jO/x/s2VqP8A2mad/YGvAceML8tjgtZ22AfwjFdLRQBzI0TxKjhl8XSMuOVk0+I5P4YqxFp/iaNvm16xlX/b0w/0lFb1FAGMbbxEIuNU0wv3J06QZ5/67elayAjHpj0xT6KACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigApsihkKt0IINOpG6fjQBleGpPM8P2aFizwIbdyTyXjJjb/x5TWqeeK5zQJmtfEfiDSH+VEmS+t8tyY5lO7j/rqkv510lAHHePQdOs7HxRFHun0S4E0gCZLW7/JMo/4C272211sMiSxI6NuVlBBznIx1qHU7KLUtKu7CdQ0VzC8Lqe4ZSD/Ouf8AhvePf/DzQ7iU5lFqsLnOcmMlP/ZetAHVUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFGR60AFFFGRnFABRRkUZoAKKQEHoRQCD0IoAWkIyKXI9aiuLq3tIWmuZ4oYlxueRwqj6k0Acv4maTRtd0nxIoxaxk2WoNuxiGQjY5HTCSbcnsGY9M11a9e2O1Qzx22oWDxSLHcWtxGVZfvLIjD9QQa4PRPFVl4ZebRNVv0uNNs2EdrqqEyIiEnbDOwyEdRxuJwwHYgigD0M/WuN+FQx8N9KwpUEzsAwwcGZyP0IpNZ8d6bLavp3hrULXVdcukMdpDZzLKEYj77lchVX7xz6VleE/G3hnSoNI8JWtxcXMkKLZi6jtm+zySrw2HI6FumMjkUAek5ozXBT/FGyjudSji0HXbi1025e2uby3tleJWT7xB3dBwTnGAc1pWfjO1v/E2n6Taw77bUNL/ALRgu94AYbsbNp5zjn26YoA6vI9aMj1rifCHxAtfFl/qFgLZbS7tSdiGYP5i5wWBAGMHt7in+HPGv9p/DRfFmoxwW7rFPJLHG/yqUdlwM85O0cepoA7LIxnIxS5HrXnPgPxlrHiyw1aw1NINP1qFFltysR2+XIuY32McnB9+cisi/l8d2HizRtAHjCC7u71jNPHHp8cfkQJjLkkHOcHr3A9aAPXc5oyPWvIfHlprulXdnY6f4v1OXUdavTFa2hdIxGhBLMCq5wuV/OrJtNQ8ReN5vCN7rOqR6Vo2m2xdoJDHLfSsB87yDLY5ORnqM0AeqZGcZGaFZXUMrBlIyCDkEV5Tc20/gjxpa6RZ315caPr1ndBbe8uGlW2mjTf8m45AIIGCeSTyeMbnwhx/wqvQwrGQBJF/DzX9en/1qAOza/s0vksWu4Fu3TekBkAkZfULnJHB5qxkHvXgV/4p0abUtU8XWuoRf21bauv2O2UHdcWsaCJlP90OC7ZPTp3r3TT7yDUbC3vbZ98FxGssbYxlWGR+hoAs0UUUAFFFFABRRRQAUUUUAFFFFABVe9vLbT7OW7u544LeIbnlkYKqj1JNWK57x5Atx4A8QRtnH9nzsMdchCR+oFAEVn8QfCV/fJZWviCxkuHwFXzMZJ6AE8HPb1rpQeK828Q6do118E45dQjgVIdHjlglYBWjl8sGMq3YlsDjrmsiCyvPEPjjQNK1HVtUt4D4VguriK0vHh82UPtO7aeTlvrxQB6+xGOa47UfHTJq8+m6Fod7rk9qdty9syrFEwGdnmNwX9V6810OlXmnXunRyabfw3tqn7tZo5xMCRjqwJyfrzXluk61pnh+18Q+EtU1t9A1d9Smmju/LBM0bkOrgkFTlflOcEe1AHoGkeKob3Qk1LV7K50Nmn+zeRqQEbFycDGcbgc8euK1JNX0+HV49LkukS+khadYTnJjU4LeleMifV9Y+EutXi31xrZ0zWzc21zPGQ09vEUbco6gff8AwBHA6btvr9p4y+Jnh/VNItby501NPuYpp5bVljJZQdm5hgnOAe3zcd6AOlb4neGbhLyPTbye+uraBpmhtrSVyVBALA7QCASMkN0rG0j4pNceGkv7/wAO6xLdl1iAs7ImG4diwHlMxOcbec9Ce9Z/gKHV9P8AEsGmaTZa/beGFila4g1mAKIXzwsTZORuPQE5BJOetZ2l6Z48h8D3Hg600SWymsJmdbwXXlpdRGUuY0YcgnceQRwMHk4oA6p/igg0/WJDoN9Bf6Skc11ZXTCN/JY8yKQDkKOeg+tal944trbW9Os4YTc2s+my6pcXSHiG3Vcq4H8W48Y7cVznhHwdfw+MNQvr3w1FpWl3WlmzaBr0XLOxcE7myTyoPoOlSeDPhvc6PLrcerSebby27abYskmXW1JYnPHBOQcHPIoAUeOvFiaDF4sm8P2I8PSIsxhjuWN2sTYw542kcg4HOKt/Fa5tb34T3l2oE1vKLeWJ9hK4MiEMR1AwazZvh940m8PDwq/imzGhDEQmW1IufIXG1PTtjOfzHFdl4j8LQ674MuPDiT/ZoZIUhSTyg+wKRg7eAfu+1AHm2k+NLnwZ4J1rw/rUixa1pUSrZorZLJMB5YXI52k8+g46itf4Xaa+g6/4n8PXU3mywx2c0oYhgZHizJ25G44H610eq/DzS9a1HQ9Rvnne50kKu7IxcquCBIMYPzDPGOpFXW8G6afEGsa0z3Bm1a0FpdR+YAm0ALlcAEHA65oA2IrOxtreTyra2ihcEyBEVVYd84GDXk6ya38MfD9teaZr1l4h8MLOI1hdR5yB2OdkithyDn8SeMCvS/D/AIdsPD3hu20K0DyWUCMgEzb2YMSTk4Gclj2rH0/4W+CtJ1NNRtNChS6Q5RnkkdVPqFZio/AcUAef6DpfinxHd+L9O0vU4dM0W51acXcdxBuuAJF+YBf4flIHPccd66CfTbLw18UPAthb5S3/ALNubOHeQclVDcnuT39z716FZ6TYadc3dzZ2kcEt5IJbhkXBkbGAT/nv9akmsLW5vLe7ltoZbi23eTK6AvGWGG2ntkUAeIaB4bupdH1PXdJiRPEfh/X7pRs3ATRoBvh46gg8D6jvUFveXupfCHS/DtvB5baxrLWYZIWAERl3u7DHHzHBHAwPY174kUcZYpGqljubaANx9T6mnAY6/nQB5LqfhvxV4Y8W6b4uF9c+I2B+x3cFrZiGQQlSAwVSQQDg8Y5ANU/Dep+I9L1/WtbvPAusXmq6jKAkmBGsMAICxgnsOPrj2Jr2gEe1OoA8hFv45Pji58US+B0uZWhW3s4ptShX7MnG45yeSc5x03Hk9K1brQvFr6laeL9LsrK01ya1+yajplzceZE6B/lIcD72MHsO3Pf0migDhtG8OeIr7xRB4j8V3FkJ7OJ4rGzsNxji38O7FurEcdx79qzfDHgnxpoOiDS4/E9ja28UjGFYbLzsBjk/fxjkk9+pr0uigDlfCfgmy8M+GYNKmSC+mVZEmunt1Vpg7EnIOePmxgk8D06XPCPhz/hFdFGlJeTXUEcrtCZhzHGxyEz3x6nn6dK3qKACiiigAooooAKKKKACiiigAooooAKpavp0WsaRd6bOzrDdRNDIY22sFYYOD2PNXaKAOFsfhdo1obNLq91bUrWzIaG0vbnzIFIPB2AAcf0rpH0DT38RR68YD/aMVs1qsocgeWWDbcfXP5/lrUUAZWh+H9M8NaeLHSbUW1qHL+WrMfmPU8/hVi80qw1Ap9usLW6Cfd86FX2/TI4q7RQBDFBHBEsUMSxxoMKiABVHoAMU8DB6fjT65rx/rV74d8F32q6e0C3EBjw06llUNIqkkDrjdmgDpMgc5wKXI9a871rU/FngqGDWNT1K21jSFkWO+SOy8iSBSQBIuGORnsfUfhiax4017QviXq8zvLdeFtOS2+1xIq5gEyLtdf4m+fJ7jGRxQB6/SZB6EV5bfale3N38T4otTmMFtpttPZ7JfliLW7sShHTcVB4rptOv9em0LwrNYQ295FdW8L6hczSlWVCikso/iJ5/yaAOtyD0NRxzxTpvhlSRMkbkYEZBwR+YxVLWNLTWdJmsJp7iCOUDzGt5PLZgCCV3YyAcYPsTXnHgCG1t/ht4q0uS6eztbO9v7ZrnB3RIF5cepAJP4UAd9B4t8OXGoCwg13TZbwuYxClyhcsDgrjOc57da1bieG2t5LieaOKGNSzySMFVQOpJPQe9fPlja3mn6T4a1TV9BsdP8M2N3FOdUtIVS5mTpG8gzuCsSC3qDXvt1Z21/aS2t3BHPbyDEkTqGVhnPIPXt1oAo+H/ABLpPie2uLnR7sXMEE7QNIFIG8AE4z1HIwelc5p/xQstYu0h0zw94huoTP5L3UVkDDGc4yW3dAcE+xzUXw7tV0/X/GdjDHDFbRaoDFHCAqqGQHAUdAP89K5HVrO38D6HLq/hPx1cTu1yHg083EU0MzO4VlCgZPBPTnjseQAekeNfF9t4M0T7fLCbq4dwkNqsm1pT1bk5wAMnPTp61r6Rfpq+jWOpIhSO8to5ghOcBlBx+uK8y1/TPGN/4i1jUbvw7bT2Z0uWxtG+2oBCsi/PIAckv1B4HHAOOuz4O8WLp/gvwpFrYiil1IRWdgbfdJ5ihAFZ8gYPGD17HucAC+PviDceGLy2sNJt4Lu9O2W7EudtvCWCgnB4LFhjPTHQ5FL8SvFXiXwvpoudE0uKS3iCvc3tww8uIMwQIEyCST3GcZ9+Ob8Z+GvF9roniq7hl0meG/nFzKyRytcvCjfu0HYBVA6Du31Op4/utVk+Hz+H73Tb7VtYvrZSZdOsGMJYSKee6nA5/PAzigDd8da9f6XpOlmwu47GK/u44Z9TeMSLaowzu2txzwMtwPrio/BOparPr+r2Fzria7Y2yxlb1IEiCSnOYxs4bjBzzjPasrWZNZ8YeHdNkh8O6j5FndodQ0q/UW7XaqmflJ4ZQx6cZIq74b0TUk8ZyarDoEfh7TDaCKW2SRG+1PnKkonyrtHfr29aAPQaKaB8xNOoAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACiiigAooooAKKKKACuT+JdvLdfD3VooIJZ5GRMRwpuc/OucDB7e1dZSEAjkZFAHmWt6hq/xA0k+HbHw7qem212UF5e6nB5QSMEMwQfxk4AB461swaHM/xA8QLc6cX0fVNNgV3YAozRll2ccg7WP9K7QdelLQB5d4Y+H+p6LfeK9NfDaRfWn2eznkkDOwKsMEdRtDY98cVt2+j+LYPC3hW0sr20s7qxa3TUUK71lhQBWVSQecAHjH1FdtRQBQ1O3vrnTZ4tPvBZXTLiKcxiQIc/3T144/GuL8O/DfUNHvb433iebUtP1Aytd6e1qI45mkBDk/McZznivQ6KAPPrf4S6Wn2WC61rXr6wtZllisLq8324C/dUpt5A9K6BPCOnrqOvX2+683W4khuh5xACqmwFf7pwffGOMV0NFAHGaV8MvC+kaumqWljOLxX3pK91I21sHnBbB69wa0bTwR4ZsNSOoWuh2Ud00nmeYIgdrccjPAPHauiooAYVBGCMgjBB71FHaQQwxwxQRpHHjYioAE+g7VYooAYRkEFcjHfv7UoB4/rTqKAEHXpS0UUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQAUUUUAFFFFABRRRQB//9k=");
        //extraParams.setProperty("signaturePositionOnPageLowerLeftX", "60"); //$NON-NLS-1$ //$NON-NLS-2$
        //extraParams.setProperty("signaturePositionOnPageLowerLeftY", "60"); //$NON-NLS-1$ //$NON-NLS-2$
        //extraParams.setProperty("signaturePositionOnPageUpperRightX", "160"); //$NON-NLS-1$ //$NON-NLS-2$
        //extraParams.setProperty("signaturePositionOnPageUpperRightY", "160"); //$NON-NLS-1$ //$NON-NLS-2$
       
        
        //----------------------Ej,agregando descripcion personalizada        
        extraParams.setProperty("layer2Text", "Firmado Digitalmente conforme Ley 25.506 por: $$SUBJECTCN$$ el día $$SIGNDATE=dd/MM/yyyy HH:mm:ss$$ certificado emitido por $$ISSUERCN$$ con serial $$CERTSERIAL$$");
        extraParams.setProperty("layer2FontSize", "10");
        //para agregar un texto personalizado con fondo una imagen de rubrica se
        //debe modificar la clase que agrega la rubrica para que no setee a vacio
        //la capa 2 cuando hay una imagen de rubrica
        
        //alwaysCreateRevision=true     esto obliga a que siempre se genere una version,por default la primera firma no crea un 
        //versionado para que sea compatible con viejos pdf.
        
        //Sello de tiempo, aplicado a nivel de firma
        //notar que no se guardan datos de validacion, agregar este sello
        //es similar a hacer un CAdES-T,
        extraParams.put("tsaURL", CATCERT_TSP); //$NON-NLS-1$
        extraParams.put("tsaPolicy", CATCERT_POLICY); //$NON-NLS-1$
        extraParams.put("tsaRequireCert", CATCERT_REQUIRECERT); //$NON-NLS-1$
        //extraParams.put("tsaHashAlgorithm", "SHA-512"); //$NON-NLS-1$ //$NON-NLS-2$
        extraParams.put("tsType", TsaParams.TS_SIGN_DOC); //$NON-NLS-1$
        
        
        final byte[] res = signer.sign(
    		testPdf,
    		DEFAULT_SIGNATURE_ALGORITHM,
    		pke.getPrivateKey(),
    		pke.getCertificateChain(),
    		extraParams
		);
          
        final File saveFile = File.createTempFile("TEST_PDF_PADES_", ".pdf"); //$NON-NLS-1$ //$NON-NLS-2$
        final OutputStream fos = new FileOutputStream(saveFile);
        fos.write(res);
        fos.flush();
        fos.close();
        System.out.println("Temporal para comprobacion manual: " + saveFile.getAbsolutePath()); //$NON-NLS-1$

        Assert.assertNotNull("No se pudo firmar ", res);
        Assert.assertTrue(signer.isSign(res));

        
	}
}
