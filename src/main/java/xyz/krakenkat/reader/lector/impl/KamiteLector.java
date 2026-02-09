package xyz.krakenkat.reader.lector.impl;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.lector.Lector;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import static java.lang.Boolean.FALSE;
import static xyz.krakenkat.reader.util.ReaderConstants.*;

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

    @Override
    public Integer getTotalPages() {
        try {
            log.info("Getting total pages from {}{}", KAMITE_BASE_URL, url);
            Document document = Jsoup.connect(KAMITE_BASE_URL + url).get();
            Matcher matcher = KAMITE_PAGE_PATTERN.matcher(document.select(".total-products").text());
            return matcher.find() ? (int) Math.ceil(Double.parseDouble(matcher.group(0)) / 20) : 1;
        } catch (IOException e) {
            log.info("There was an issue reading the document to calculate the total pages to parse. {}", e.getMessage());
        }
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info("Reading single page {}", index);
            Document document = Jsoup.connect(KAMITE_BASE_URL + url + "&page=" + index).get();
            Elements elements = document.select("#js-product-list .product_content .item-product article");
            return elements
                    .stream()
                    .filter(Predicate.not(this::isBoxSet))
                    .map(this::buildItem)
                    .sorted(Comparator.comparing(ItemDTO::getNumber))
                    .toList();
        } catch (IOException e) {
            log.info("There was an issue reading the document. {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO itemDTO) {
        log.info("Reading {} details", itemDTO.getName());
        try {
            Document document = Jsoup.connect(itemDTO.getLink()).userAgent(USER_AGENT).get();
            itemDTO.setShortDescription(buildDescription(document));
        } catch (IOException e) {
            log.info("There was an issue {}; reading the detail document for {}", e.getMessage(), itemDTO.getName());
        }
        return itemDTO;
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .link(buildLink(element))
                .name(buildName(element))
                .number(buildNumber(element))
                .pages(buildPageNumbers(element))
                .price(buildPrice(element))
                .cover(DEFAULT_EMPTY)
                .currency(MXN_CURRENCY)
                .isbn(DEFAULT_ISBN)
                .edition(DEFAULT_EDITION)
                .variant(FALSE)
                .build();
    }

    private boolean isBoxSet(Element element) {
        return KAMITE_BOXSET_PATTERN.matcher(buildName(element)).find();
    }

    private String buildName(Element element) {
        return element.select(".product_desc h3[itemprop='name'] a").attr("title").trim();
    }

    private String buildLink(Element element) {
        return element.select(".img_block a").attr("href");
    }

    private Integer buildNumber(Element element) {
        Matcher matcher = KAMITE_NUMBER_PATTERN.matcher(element.select(".product_desc h3[itemprop='name'] a").attr("title"));
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String buildDescription(Document document) {
        return document.select("#description .product-description p").text();
    }

    private Integer buildPageNumbers(Element element) {
        Matcher matcher = KAMITE_PAGE_NUMBER_PATTERN.matcher(element.select(".product_desc .product-desc p:nth-child(4)").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private Double buildPrice(Element element) {
        String text = element.select(".product-price-and-shipping span[itemprop='price']").text();
        Matcher matcher = KAMITE_PRICE_PATTERN.matcher(text);
        return  matcher.find() ? Double.parseDouble(matcher.group(0)) : 0.00;
    }
}
