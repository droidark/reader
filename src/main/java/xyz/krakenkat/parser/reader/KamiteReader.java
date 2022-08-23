package xyz.krakenkat.parser.reader;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.parser.dto.ItemDTO;
import xyz.krakenkat.parser.util.ReaderConstants;

import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class KamiteReader implements Reader {

    private static final String PRODUCT_NAME_LOCATION = ".right-block .product-meta h5.name a.product-name";
    private static final String TITLE_ATTR = "title";
    private final Pattern numberPattern = Pattern.compile(ReaderConstants.KAMITE_NUMBER_PATTERN);
    private final Pattern pageNumberPattern = Pattern.compile(ReaderConstants.KAMITE_PAGE_NUMBER_PATTERN);
    private final Pattern boxsetPattern = Pattern.compile(ReaderConstants.KAMITE_BOXSET_PATTERN, Pattern.CASE_INSENSITIVE);
    private String title = ResourceBundle.getBundle("application").getString("reader.title.kamite");

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Integer getTotalPages() {
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        log.info(String.format("Reading page %d", index));
        try {
            Document document = Jsoup.connect(ReaderConstants.KAMITE_BASE_URL + title).get();
            Elements elements = document.select(".product_list .product-container");
            return elements
                    .stream()
                    .filter(Predicate.not(this::isBoxSet))
                    .map(this::buildItem)
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> items) {
        log.info("Getting item details from Kamite");
        return items.stream().map(item -> {
            try {
                Document document = Jsoup.connect(item.getLink()).get();
                item.setShortDescription(document.select("#tab2 .rte p").get(0).text());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return item;
        }).toList();
    }

    private boolean isBoxSet(Element element) {
        return boxsetPattern.matcher(element.select(PRODUCT_NAME_LOCATION).attr(TITLE_ATTR)).find();
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .link(element.select(PRODUCT_NAME_LOCATION).attr("href"))
                .name(element.select(PRODUCT_NAME_LOCATION).attr(TITLE_ATTR))
                .price(this.getPrice(element))
                .number(this.getNumber(element))
                .pages(this.getPageNumber(element))
                .isbn(ReaderConstants.DEFAULT_ISBN)
                .currency(ReaderConstants.MXN_CURRENCY)
                .edition(ReaderConstants.DEFAULT_EDITION)
                .variant(false)
                .build();
    }

    private int getNumber(Element element) {
        Matcher matcher = numberPattern.matcher(element.select(PRODUCT_NAME_LOCATION).attr(TITLE_ATTR));
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;

    }

    private double getPrice(Element element) {
        return Double.parseDouble(element.select(".right-block .product-meta .content_price .product-price")
                .text()
                .replace("$", "")
                .trim());
    }

    private int getPageNumber(Element element) {
        Matcher matcher = pageNumberPattern.matcher(element.select(".right-block .product-meta .product-desc").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : ReaderConstants.DEFAULT_PAGES;
    }
}
