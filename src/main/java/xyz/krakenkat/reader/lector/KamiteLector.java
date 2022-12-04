package xyz.krakenkat.reader.lector;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KamiteLector implements Lector {

    private String titleId;
    private String key;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;
    private static final String PRODUCT_NAME_LOCATION = ".right-block .product-meta h5.name a.product-name";
    private static final String TITLE_ATTR = "title";

    @Override
    public Integer getTotalPages() { return 1; }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info(String.format("Reading page %d", index));
            Document document = Jsoup.connect(ReaderConstants.KAMITE_BASE_URL + url).get();
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
    public List<ItemDTO> getDetails() {
        log.info("Getting item details from Kamite");
        return getIssues()
                .stream()
                .map(this::buildDetails)
                .toList();
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> databaseList) {
        log.info("Getting item details from Kamite");
        return getIssues()
                .stream()
                .filter(item -> !databaseList.contains(item))
                .map(this::buildDetails)
                .toList();
    }

    private ItemDTO buildDetails(ItemDTO item) {
        try {
            Document document = Jsoup.connect(item.getLink()).get();
            item.setCover(getCover());
            item.setShortDescription(document.select("#tab2 .rte p").get(0).text());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    private boolean isBoxSet(Element element) {
        return ReaderConstants.KAMITE_BOXSET_PATTERN.matcher(element.select(PRODUCT_NAME_LOCATION).attr(TITLE_ATTR)).find();
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
        Matcher matcher = ReaderConstants.KAMITE_NUMBER_PATTERN.matcher(element.select(PRODUCT_NAME_LOCATION).attr(TITLE_ATTR));
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;

    }

    private double getPrice(Element element) {
        return Double.parseDouble(element.select(".right-block .product-meta .content_price .price.product-price")
                .text()
                .replace("$", "")
                .trim());
    }

    private int getPageNumber(Element element) {
        Matcher matcher = ReaderConstants.KAMITE_PAGE_NUMBER_PATTERN.matcher(element.select(".right-block .product-meta .product-desc").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : ReaderConstants.DEFAULT_PAGES;
    }
}
