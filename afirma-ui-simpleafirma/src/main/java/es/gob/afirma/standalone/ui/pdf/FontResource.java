package es.gob.afirma.standalone.ui.pdf;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

enum FontResource {

	TIMES_ROMAN(  "Times Roman",  "/resources/fonts/times.ttf",        2), //$NON-NLS-1$//$NON-NLS-2$
	SYMBOL(       "Symbol",       "/resources/fonts/symbol.ttf",       3), //$NON-NLS-1$ //$NON-NLS-2$
	COURIER(      "Courier",      "/resources/fonts/courier.otf",      0), //$NON-NLS-1$ //$NON-NLS-2$
	HELVETICA(    "Helvetica",    "/resources/fonts/helvetica.otf",    1), //$NON-NLS-1$ //$NON-NLS-2$
	ZAPF_DINGBATS("ZapfDingBats", "/resources/fonts/zapfdingbats.otf", 4); //$NON-NLS-1$//$NON-NLS-2$

	private final String fontName;
	private Font font = null;
	private final int pdfFontIndex;

    private final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    private final Font[] fonts = this.ge.getAllFonts(); // Get the fonts

	private FontResource(final String name,
			             final String file,
			             final int index) {
		this.fontName = name;
		this.pdfFontIndex = index;
		for (final Font f : this.fonts) {
	      if (f.getFontName().equals(this.fontName)) {
	    	  this.font = f;
	      }
	    }
		if (this.font == null) {
	    	try (
        		final InputStream is = FontResource.class.getResourceAsStream(file);
            ) {
	    		this.font = Font.createFont(Font.TRUETYPE_FONT, is);
	    		this.ge.registerFont(this.font);
	    	}
	    	catch(final Exception e) {
	    		throw new IllegalStateException(
    				"Error creando el tipo de letra " + this.fontName + ": " + e //$NON-NLS-1$ //$NON-NLS-2$
				);
	    	}
		}
	}

	@Override
	public String toString() {
		return this.fontName;
	}

	Font getFont() {
		return this.font;
	}

	static FontResource[] getAllFontresources() {
		return new FontResource[] {
			COURIER,
			HELVETICA,
			TIMES_ROMAN,
			ZAPF_DINGBATS
		};
	}

	String getPdfFontIndex() {
		return Integer.toString(this.pdfFontIndex);
	}

}
