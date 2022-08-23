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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PaniniReaderV2 implements Reader {

    private String title = ResourceBundle.getBundle("application").getString("reader.title.panini");
    private final Pattern numberPattern = Pattern.compile(ReaderConstants.PANINI_V2_NUMBER_PATTERN);
    private static final String LINK_LOCATION = ".product-item-details strong.product-item-name a.product-item-link";

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public Integer getTotalPages() {
        log.info(String.format("Getting total pages from %s%s", ReaderConstants.PANINI_BASE_URL, title));
        try {
            Document document = Jsoup.connect(ReaderConstants.PANINI_BASE_URL + title).get();
            return document.select("#toolbar-amount").get(0).select(".toolbar-number").size() == 1
                    ? 1
                    : (int) Math.ceil(Double.parseDouble(document.select("#toolbar-amount").get(0).select(".toolbar-number").get(2).text()) / 12);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        log.info(String.format("Reading page %d", index));
        try {
            String page = ReaderConstants.PANINI_BASE_URL + title + "&p=" + index;
            Document document = Jsoup.connect(page).get();
            Elements issues = document.select("ol.product-items li.product-item [id^=product-item-info_]");
            return issues
                    .stream()
                    .map(issue -> ItemDTO
                            .builder()
                            .link(issue.select(LINK_LOCATION).attr("href"))
                            .name(this.getName(issue))
                            .number(this.getNumber(issue))
                            .price(this.getPrice(issue))
                            .currency(ReaderConstants.MXN_CURRENCY)
                            .edition(ReaderConstants.DEFAULT_EDITION)
                            .isbn(ReaderConstants.DEFAULT_ISBN)
                            .variant(false)
                            .build())
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> items) {
        log.info("Getting item details from Panini");
        return items
                .stream()
                .map(item -> {
                    try {
                        Document document = Jsoup.connect(item.getLink()).get();
                        item.setShortDescription(this.getDescription(document));
                        item.setPages(this.getNumberPages(document));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return item;
                }).toList();
    }

    private int getNumber(Element element) {
        String text = element.select(LINK_LOCATION).text();
        Matcher matcher = numberPattern.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String getName(Element element) {
        return element
                .select(LINK_LOCATION)
                .text()
                .trim()
                .replace(" N.", " #");
    }

    private String getDescription(Element element) {
        String description = element.select(".product-info-main .overview").text();
        return description.equals("") ? "-" : description;
    }

    private int getNumberPages(Element element) {
        String pages = element.select("#product-attribute-specs-table tbody tr td[data-th='Número de páginas']").text();
        return pages.equals("")
                ? 0
                : Integer.parseInt(pages.trim());
    }

    private double getPrice(Element element) {
        return Double.parseDouble(element
                .select(".product-item-details .price-final_price span.price-container [id^=product-price-]")
                .attr("data-price-amount"));
    }
}
