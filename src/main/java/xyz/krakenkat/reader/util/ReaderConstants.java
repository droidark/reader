package xyz.krakenkat.reader.util;

import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ReaderConstants {
    private ReaderConstants() {}
    // GENERAL CONSTANTS
    public static final String DEFAULT_EMPTY= "-";
    public static final String DEFAULT_DATE = "0000-00-00";
    public static final String DEFAULT_ISBN = "000-0-00-000000-0";
    public static final String MXN_CURRENCY = "MXN";
    public static final String USD_CURRENCY = "USD";
    public static final double DEFAULT_PRICE = 139.0;
    public static final int DEFAULT_PAGES = 192;
    public static final int DEFAULT_EDITION = 1;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    // PANINI V1
    public static final String PANINI_BASE_URL = "https://tiendapanini.com.mx";
    public static final Pattern PANINI_PAGE_PATTERN = Pattern.compile("páginas");
    public static final Pattern PANINI_NUMBER_PATTERN = Pattern.compile("\\d{1,3}$");
    public static final Pattern PANINI_PAGE_NUMBER_PATTERN = Pattern.compile("\\d{3}");
    public static final Pattern PANINI_TOTAL_PAGES_PATTERN = Pattern.compile("\\d{1,3}$");
    public static final Pattern PANINI_ISBN_PATTERN = Pattern.compile("isbn", Pattern.CASE_INSENSITIVE);
    public static final Pattern PANINI_BOXSET_PATTERN = Pattern.compile("box[\\s]?set|serie[\\s]completa|paquete", Pattern.CASE_INSENSITIVE);

    // PANINI V2
    public static final Pattern PANINI_V2_NUMBER_PATTERN = Pattern.compile("^*?[0-9]{1,3}$");

    // KAMITE
    public static final String KAMITE_BASE_URL = "https://kamite.com.mx";
    public static final Pattern KAMITE_NUMBER_PATTERN = Pattern.compile("\\d{1,3}$");
    public static final Pattern KAMITE_PAGE_NUMBER_PATTERN= Pattern.compile("\\d{3}");
    public static final Pattern KAMITE_BOXSET_PATTERN = Pattern.compile("^PAQUETE", Pattern.CASE_INSENSITIVE);

    // KAMITE V2
    public static final Pattern KAMITE_V2_PRICE_PATTERN = Pattern.compile("\\d{1,3}\\.\\d{2}");
    public static final Pattern KAMITE_V2_PAGE_PATTERN = Pattern.compile("\\d{1,3}");

    // WHAKOOM
    public static final String WHAKOOM_BASE_URL = "https://whakoom.com";
    public static final Pattern WHAKOOM_TOTAL_ISSUES_PATTERN = Pattern.compile("^\\d{1,4}");
    public static final Pattern WHAKOOM_NUMBER_PATTERN = Pattern.compile("\\d{1,3}$");

    // VIZ MEDIA
    public static final String VIZ_BASE_URL = "https://www.viz.com";
    public static final Pattern VIZ_NUMBER_PATTERN = Pattern.compile("\\d{1,3}$");
    public static final Pattern VIZ_DATE_PATTERN = Pattern.compile("(January|February|March|April|May|June|July|August|September|October|November|December) \\d{1,2}, \\d{4}");
    public static final Pattern VIZ_ISBN_PATTERN = Pattern.compile("\\d{3}-\\d{1}-\\d{4}-\\d{4}-\\d{1}");
    public static final Pattern VIZ_PRICE_PATTERN = Pattern.compile("\\d{1,2}\\.\\d{2}");
    public static final Pattern VIZ_PAGES_PATTERN = Pattern.compile("\\d{2,4}");

    // DISTRITO MANGA
    public static final String DISTRITO_MANGA_BASE_URL = "https://www.penguinlibros.com";
    public static final Pattern DISTRITO_MANGA_NUMBER_PATTERN = Pattern.compile("\\d{1,3}$");

    // MANGALINE
    public static final String MANGALINE_BASE_URL = "https://mangaline.com.mx";
    public static final Pattern MANGALINE_NUMBER_PATTERN = Pattern.compile("\\d{1,3}$");
    public static final Pattern MANGALINE_PRICE_PATTERN = Pattern.compile("\\d{1,3}\\.\\d{2}");

    // WRITER
    public static final String OUTPUT_FORMAT = ".csv";
    public static final String DELIMITER = "|";
    public static final String[] HEADERS = {"NAME", "KEY", "NUMBER", "COVER", "PAGES", "PRINTED_PRICE", "CURRENCY", "RELEASE_DATE", "SHORT_REVIEW", "ISBN10", "EDITION", "VARIANT"};
}
