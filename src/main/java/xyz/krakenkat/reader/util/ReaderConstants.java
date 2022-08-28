package xyz.krakenkat.reader.util;

import java.util.regex.Pattern;

public class ReaderConstants {
    private ReaderConstants() {}
    // GENERAL CONSTANTS
    public static final String DEFAULT_EMPTY= "-";
    public static final String DEFAULT_DATE = "0000-00-00";
    public static final String DEFAULT_ISBN = "000-0-00-000000-0";
    public static final String MXN_CURRENCY = "MXN";
    public static final double DEFAULT_PRICE = 139.0;
    public static final int DEFAULT_PAGES = 192;
    public static final int DEFAULT_EDITION = 1;

    // PANINI V1
    public static final String PANINI_BASE_URL = "https://tiendapanini.com.mx";
    public static final String PANINI_PAGE_PATTERN = "páginas";
    public static final String PANINI_NUMBER_PATTERN = "\\d{1,3}$";
    public static final String PANINI_PAGE_NUMBER_PATTERN = "\\d{3}";
    public static final String PANINI_TOTAL_PAGES_PATTERN = "\\d{1,3}$";
    public static final Pattern PANINI_ISBN_PATTERN = Pattern.compile("isbn", Pattern.CASE_INSENSITIVE);
    public static final String PANINI_BOXSET_PATTERN = "box[\\s]?set|serie[\\s]completa|paquete";

    // KAMITE
    public static final String KAMITE_BASE_URL = "https://kamite.com.mx";
    public static final String KAMITE_NUMBER_PATTERN = "\\d{1,3}$";
    public static final String KAMITE_PAGE_NUMBER_PATTERN= "\\d{3}";
    public static final String KAMITE_BOXSET_PATTERN = "^PAQUETE";

    // WHAKOOM
    public static final String WHAKOOM_BASE_URL = "https://whakoom.com";
    public static final String WHAKOOM_TOTAL_ISSUES_PATTERN = "^\\d{1,4}";


    // PANINI V2
    public static final String PANINI_V2_NUMBER_PATTERN = "^*?[0-9]{1,3}$";

}
