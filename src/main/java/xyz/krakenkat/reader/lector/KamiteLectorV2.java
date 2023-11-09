package xyz.krakenkat.reader.lector;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KamiteLectorV2 implements Lector {

    private String titleId;
    private String key;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;

    @Override
    public Integer getTotalPages() {
        try {
            log.info("Getting total pages from {}{}", ReaderConstants.KAMITE_BASE_URL, url);
            Document document = Jsoup.connect(ReaderConstants.KAMITE_BASE_URL + url).get();
            Matcher matcher = ReaderConstants.KAMITE_V2_PAGE_PATTERN.matcher(document.select(".total-products").text());
            return matcher.find() ? (int) Math.ceil(Double.parseDouble(matcher.group(0)) / 20) : 1;
        } catch (Exception e) {
            log.info("There was an issue reading the document to calculate the total pages to parse. {}", e.getMessage());
        }
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info("Reading page {}", index);
            Document document = Jsoup.connect(ReaderConstants.KAMITE_BASE_URL + url + "&page=" + index).get();
            Elements elements = document.select("#js-product-list .product_content .item-product article");
            return elements
                    .stream()
                    .filter(Predicate.not(this::isBoxSet))
                    .map(this::buildItem)
                    .sorted(Comparator.comparing(ItemDTO::getNumber))
                    .toList();
        } catch (Exception e) {
            log.info("There was an issue reading the document.");
        }
        return List.of();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO itemDTO) {
        log.info("Reading {}", itemDTO.getName());
        try {
            Document document = Jsoup.connect(itemDTO.getLink())
                    .userAgent(ReaderConstants.USER_AGENT)
                    .get();
            itemDTO.setShortDescription(getDescription(document));
        } catch (Exception e) {
            log.info("There was an issue reading the detail document for {}", itemDTO.getName());
        }
        return itemDTO;
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .link(element.select(".img_block a").attr("href"))
                .name(getName(element))
                .number(getNumber(element))
                .pages(getPageNumbers(element))
                .price(getPrice(element))
                .cover(ReaderConstants.DEFAULT_EMPTY)
                .currency(ReaderConstants.MXN_CURRENCY)
                .isbn(ReaderConstants.DEFAULT_ISBN)
                .edition(ReaderConstants.DEFAULT_EDITION)
                .variant(Boolean.FALSE)
                .build();
    }

    private boolean isBoxSet(Element element) {
        return ReaderConstants.KAMITE_BOXSET_PATTERN.matcher(getName(element)).find();
    }

    private String getName(Element element) {
        return element.select(".product_desc h3[itemprop='name'] a").attr("title").trim();
    }

    private Integer getNumber(Element element) {
        Matcher matcher = ReaderConstants.KAMITE_NUMBER_PATTERN.matcher(element.select(".product_desc h3[itemprop='name'] a").attr("title"));
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String getDescription(Document document) {
        return document.select("#description .product-description p").text();
    }

    private Integer getPageNumbers(Element element) {
        Matcher matcher = ReaderConstants.KAMITE_PAGE_NUMBER_PATTERN.matcher(element.select(".product_desc .product-desc p:nth-child(4)").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private Double getPrice(Element element) {
        String text = element.select(".product-price-and-shipping span[itemprop='price']").text();
        Matcher matcher = ReaderConstants.KAMITE_V2_PRICE_PATTERN.matcher(text);
        return  matcher.find() ? Double.parseDouble(matcher.group(0)) : 0.00;
    }
}
