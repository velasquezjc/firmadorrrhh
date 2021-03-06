package org.mozilla.universalchardet;

/** Nombres de los juegos de caracteres. */
public final class Constants {

	private Constants() {
		// No permitimos la instanciacion
	}

	/** ISO-2022-JP. */
    public static final String CHARSET_ISO_2022_JP  = "ISO-2022-JP".intern(); //$NON-NLS-1$

    /** ISO-2022-CN. */
    public static final String CHARSET_ISO_2022_CN  = "ISO-2022-CN".intern(); //$NON-NLS-1$

    /** ISO-2022-KR. */
    public static final String CHARSET_ISO_2022_KR  = "ISO-2022-KR".intern(); //$NON-NLS-1$

    /** ISO-8859-5. */
    public static final String CHARSET_ISO_8859_5   = "ISO-8859-5".intern(); //$NON-NLS-1$

    /** ISO-8859-7. */
    public static final String CHARSET_ISO_8859_7   = "ISO-8859-7".intern(); //$NON-NLS-1$

    /** ISO-8859-8. */
    public static final String CHARSET_ISO_8859_8   = "ISO-8859-8".intern(); //$NON-NLS-1$

    /** BIG5. */
    public static final String CHARSET_BIG5         = "BIG5".intern(); //$NON-NLS-1$

    /** GB18030. */
    public static final String CHARSET_GB18030      = "GB18030".intern(); //$NON-NLS-1$

    /** EUC-JP. */
    public static final String CHARSET_EUC_JP       = "EUC-JP".intern(); //$NON-NLS-1$

    /** EUC-KR. */
    public static final String CHARSET_EUC_KR       = "EUC-KR".intern(); //$NON-NLS-1$

    /** EUC-TW. */
    public static final String CHARSET_EUC_TW       = "EUC-TW".intern(); //$NON-NLS-1$

    /** SHIFT_JIS. */
    public static final String CHARSET_SHIFT_JIS    = "SHIFT_JIS".intern(); //$NON-NLS-1$

    /** IBM855. */
    public static final String CHARSET_IBM855       = "IBM855".intern(); //$NON-NLS-1$

    /** IBM855. */
    public static final String CHARSET_IBM866       = "IBM855".intern(); //$NON-NLS-1$

    /** KOI8-R. */
    public static final String CHARSET_KOI8_R       = "KOI8-R".intern(); //$NON-NLS-1$

    /** MACCYRILLIC. */
    public static final String CHARSET_MACCYRILLIC  = "MACCYRILLIC".intern(); //$NON-NLS-1$

    /** WINDOWS-1251. */
    public static final String CHARSET_WINDOWS_1251 = "WINDOWS-1251".intern(); //$NON-NLS-1$

    /** WINDOWS-1252. */
    public static final String CHARSET_WINDOWS_1252 = "WINDOWS-1252".intern(); //$NON-NLS-1$

    /** WINDOWS-1253. */
    public static final String CHARSET_WINDOWS_1253 = "WINDOWS-1253".intern(); //$NON-NLS-1$

    /** WINDOWS-1255. */
    public static final String CHARSET_WINDOWS_1255 = "WINDOWS-1255".intern(); //$NON-NLS-1$

    /** UTF-8. */
    public static final String CHARSET_UTF_8        = "UTF-8".intern(); //$NON-NLS-1$

    /** UTF-16BE. */
    public static final String CHARSET_UTF_16BE     = "UTF-16BE".intern(); //$NON-NLS-1$

    /** UTF-16LE. */
    public static final String CHARSET_UTF_16LE     = "UTF-16LE".intern(); //$NON-NLS-1$

    /** UTF-32BE. */
    public static final String CHARSET_UTF_32BE     = "UTF-32BE".intern(); //$NON-NLS-1$

    /** UTF-32LE. */
    public static final String CHARSET_UTF_32LE     = "UTF-32LE".intern(); //$NON-NLS-1$

    // WARNING: Listed below are charsets which Java does not support.

    /** HZ-GB-2312. */
    public static final String CHARSET_HZ_GB_2312   = "HZ-GB-2312".intern(); // Simplified Chinese //$NON-NLS-1$

    /** X-ISO-10646-UCS-4-3412. */
    public static final String CHARSET_X_ISO_10646_UCS_4_3412 = "X-ISO-10646-UCS-4-3412".intern(); // Malformed UTF-32 //$NON-NLS-1$

    /** X-ISO-10646-UCS-4-2143. */
    public static final String CHARSET_X_ISO_10646_UCS_4_2143 = "X-ISO-10646-UCS-4-2143".intern(); // Malformed UTF-32 //$NON-NLS-1$
}
