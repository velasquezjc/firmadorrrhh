package es.gob.afirma.signers.pades;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

final class XmpHelper {

    private static final String TAG_UUID = "$$UUID$$"; //$NON-NLS-1$
    private static final String TAG_DATE = "$$DATE$$"; //$NON-NLS-1$

    private static final String PROCESSING_INSTRUCTION_SUFFIX = "?>"; //$NON-NLS-1$

    private static final String DEFAULT_ENCODING = "UTF-8"; //$NON-NLS-1$

	private static final String NEW_HISTORY_LIST_ITEM = "<rdf:li rdf:parseType=\"Resource\">\n" + //$NON-NLS-1$
			"  <stEvt:action>signed</stEvt:action>\n" + //$NON-NLS-1$
			"  <stEvt:instanceID>uuid:" + TAG_UUID + "</stEvt:instanceID>\n" + //$NON-NLS-1$ //$NON-NLS-2$
			"  <stEvt:parameters>Firmado por el Cliente @firma</stEvt:parameters>\n" + //$NON-NLS-1$
			"  <stEvt:softwareAgent>Cliente @firma</stEvt:softwareAgent>\n" + //$NON-NLS-1$
			"  <stEvt:when>" + TAG_DATE + "</stEvt:when>\n" + //$NON-NLS-1$ //$NON-NLS-2$
			"</rdf:li>"; //$NON-NLS-1$

	/** A&ntilde;ade una entrada de firma al hist&oacute;rico XMP de un PDF.
	 * Un ejemplo de hist&oacute;rico XML con entrada de firma podr&iacute;a ser:
	 * <pre>
	 *    &lt;xmpMM:History&gt;
	 *        &lt;rdf:Seq&gt;
	 *         &lt;rdf:li rdf:parseType="Resource"&gt;
	 *          &lt;stEvt:action&gt;converted&lt;/stEvt:action&gt;
	 *          &lt;stEvt:instanceID&gt;uuid:681a4b7c-8815-4ac6-91c8-a3ea363af8d9&lt;/stEvt:instanceID&gt;
	 *          &lt;stEvt:parameters&gt;converted to PDF/A-1b&lt;/stEvt:parameters&gt;
	 *          &lt;stEvt:softwareAgent&gt;Preflight&lt;/stEvt:softwareAgent&gt;
	 *          &lt;stEvt:when&gt;2015-11-26T13:24:02+01:00&lt;/stEvt:when&gt;
	 *         &lt;/rdf:li&gt;
	 *         &lt;rdf:li rdf:parseType="Resource"&gt;
	 *          &lt;stEvt:action&gt;signed&lt;/stEvt:action&gt;
	 *          &lt;stEvt:instanceID&gt;uuid:9c89ef24-5974-3c4d-9ced-e52eb4160fbb&lt;/stEvt:instanceID&gt;
	 *          &lt;stEvt:parameters&gt;Firmado por el Cliente @firma&lt;/stEvt:parameters&gt;
	 *          &lt;stEvt:softwareAgent&gt;Cliente @firma&lt;/stEvt:softwareAgent&gt;
	 *          &lt;stEvt:when&gt;2016-01-25T17:42:08+01:00&lt;/stEvt:when&gt;
	 *         &lt;/rdf:li&gt;
	 *        &lt;/rdf:Seq&gt;
	 *    &lt;/xmpMM:History&gt;
	 * </pre>
	 * @param inPdf PDF de entrada.
	 * @param globalDate Fecha de la firma del PDF.
	 * @return PDF con la entrada de firma a&ntilde;adida a su hist&oacute;rico XMP.
	 * @throws DOMException Si hay errores en el tratamiento del XML.
	 * @throws SAXException Si hay errores en el tratamiento del XML.
	 * @throws IOException Si hay errores en la lectura del PDF.
	 * @throws ParserConfigurationException Si hay problemas con el analizador XML. */
	static byte[] addSignHistoryToXmp(final byte[] inPdf,
			                          final Calendar globalDate) throws DOMException,
	                                                                    SAXException,
	                                                                    IOException,
	                                                                    ParserConfigurationException {
		final PdfReader reader = new PdfReader(inPdf);
		final byte[] xmpBytes = reader.getMetadata();

		if (!PdfUtil.isPdfAx(xmpBytes) || new AOPDFSigner().isSign(inPdf)) {
			reader.close();
			return inPdf;
		}

		final String originalXmp = new String(xmpBytes, DEFAULT_ENCODING);
		final String originalXmpHeader = originalXmp.substring(
			0,
			originalXmp.indexOf(PROCESSING_INSTRUCTION_SUFFIX) + PROCESSING_INSTRUCTION_SUFFIX.length()
		).replace(PROCESSING_INSTRUCTION_SUFFIX, " encoding=\"" + DEFAULT_ENCODING.toLowerCase() + "\"" + PROCESSING_INSTRUCTION_SUFFIX); //$NON-NLS-1$ //$NON-NLS-2$
		final String originalXmpFooter = originalXmp.substring(
			originalXmp.lastIndexOf("<?"), //$NON-NLS-1$
			originalXmp.length()
		);

		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final Document doc = db.parse(new ByteArrayInputStream(xmpBytes));

		String newnode = NEW_HISTORY_LIST_ITEM.replace(
			TAG_UUID,
			UUID.nameUUIDFromBytes(
				BigInteger.valueOf(
					globalDate.getTimeInMillis()
				).toByteArray()
			).toString()
		).replace(
			TAG_DATE,
			new PdfDate(globalDate).getW3CDate()
		);

		NodeList nl = doc.getElementsByTagName("xmpMM:History"); //$NON-NLS-1$
		// Si no tiene historico
		if (nl.getLength() != 1) {
			nl = doc.getElementsByTagName("rdf:RDF"); //$NON-NLS-1$
			if (nl.getLength() != 1) {
				throw new IllegalStateException(
					"El PDF no tiene una entrada RDF XMP valida" //$NON-NLS-1$
				);
			}
			newnode = "<xmpMM:History xmlns:xmpMM=\"http://ns.adobe.com/xap/1.0/mm/\" xmlns:stEvt=\"http://ns.adobe.com/xap/1.0/sType/ResourceEvent#\"><rdf:Seq>" + newnode + "</rdf:Seq></xmpMM:History>"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		// Si tiene historico
		else {
			final Element e = (Element) nl.item(0);
			nl = e.getElementsByTagName("rdf:Seq"); //$NON-NLS-1$
			// Si existe el historico pero no esta declarada la secuencia
			if (nl.getLength() != 1) {
				nl = doc.getElementsByTagName("xmpMM:History"); //$NON-NLS-1$
				newnode = "<rdf:Seq xmlns:stEvt=\"http://ns.adobe.com/xap/1.0/sType/ResourceEvent#\">" + newnode + "</rdf:Seq>"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		final Node n = nl.item(0);

		final Node node = doc.importNode(
			db.parse(
				new InputSource(
					new StringReader(
						newnode
					)
				)
			).getDocumentElement(),
			true
		);

		n.appendChild(node);

		final Map<String, String> props = new ConcurrentHashMap<String, String>(1);
		props.put("encoding", "utf-8"); //$NON-NLS-1$ //$NON-NLS-2$

		String xmlString = new String(
			writeXml(doc.getDocumentElement()),
			DEFAULT_ENCODING
		);
		xmlString = xmlString.replace(
			xmlString.substring(
				0,
				xmlString.indexOf(PROCESSING_INSTRUCTION_SUFFIX) + PROCESSING_INSTRUCTION_SUFFIX.length()
			),
			originalXmpHeader
		).concat(originalXmpFooter);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			final PdfStamper stamper = new PdfStamper(reader, baos, globalDate);
			stamper.setXmpMetadata(xmlString.getBytes(DEFAULT_ENCODING));
			stamper.close(globalDate);
		}
		catch(final DocumentException ex) {
			throw new IOException(ex);
		}

		reader.close();

		Logger.getLogger("es.gob.afirma").info( //$NON-NLS-1$
			"Se ha registrado la firma en el historico XMP del PDF" //$NON-NLS-1$
		);

		return baos.toByteArray();

	}

    /** Escribe un XML como texto.
     * @param node Nodo XML que queremos pasar a texto.
     * @return Cadena de texto con el XML en forma de array de octetos */
	private static byte[] writeXml(final Node node) {
		final DOMImplementationLS domImpl = (DOMImplementationLS) node.getOwnerDocument().getImplementation();
        final LSSerializer lsSerializer = domImpl.createLSSerializer();
        final DOMConfiguration domConfiguration = lsSerializer.getDomConfig();
        if (domConfiguration.canSetParameter("namespaces", Boolean.FALSE)) { //$NON-NLS-1$
        	domConfiguration.setParameter("namespaces", Boolean.FALSE); //$NON-NLS-1$
        }
        if (domConfiguration.canSetParameter("canonical-form", Boolean.TRUE)) { //$NON-NLS-1$
            lsSerializer.getDomConfig().setParameter("canonical-form", Boolean.TRUE); //$NON-NLS-1$
        }
        final LSOutput lsOutput = domImpl.createLSOutput();
        lsOutput.setEncoding(DEFAULT_ENCODING);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        lsOutput.setByteStream(baos);
        lsSerializer.write(node, lsOutput);
        return baos.toByteArray();
    }

}
