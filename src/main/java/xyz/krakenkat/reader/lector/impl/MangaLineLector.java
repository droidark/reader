package xyz.krakenkat.reader.lector.impl;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.lector.Lector;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;

import static java.lang.Boolean.FALSE;
import static xyz.krakenkat.reader.util.ReaderConstants.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class MangaLineLector implements Lector {

    private String titleId;
    private String key;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;
    @Override
    public Integer getTotalPages() {
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            String page = ReaderConstants.MANGALINE_BASE_URL + url;
            Document document = Jsoup.connect(page).get();
            Elements issues = document.select("ul.products.products-list.row.grid li .products-entry.content-product2.clearfix.product-wapper");

            return issues
                    .stream()
                    .filter(Predicate.not(this::isSpecialEdition))
                    .map(this::buildItem)
                    .toList();
        } catch (IOException e) {
            log.info("There was an error processing the page: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO itemDTO) {
        log.info("Reading {} details", itemDTO.getName());
        try {
            Document document = Jsoup.connect(itemDTO.getLink()).userAgent(USER_AGENT).get();
            itemDTO.setPrice(buildPrice(document));
            itemDTO.setShortDescription(buildShortDescription(document));
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
                .pages(DEFAULT_PAGES)
                .isbn(DEFAULT_ISBN)
                .currency(MXN_CURRENCY)
                .edition(DEFAULT_EDITION)
                .variant(FALSE)
                .build();
    }

    private String buildName(Element element) {
        return element.select("h3.product-title").text();
    }

    private String buildLink(Element element) {
        return element.select("h3.product-title a").attr("href");
    }

    private Integer buildNumber(Element element) {
        Matcher matcher = ReaderConstants.MANGALINE_NUMBER_PATTERN.matcher(element.select("h3.product-title").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private Double buildPrice(Element element) {
        String price = element.select(".price-single .price .woocommerce-Price-amount").text().replace("$", "");
        return Double.parseDouble(price) > 0 ? Double.parseDouble(price) : 0.00;
    }

    private String buildShortDescription(Element element) {
        return element.select(".description").text();
    }

    private boolean isSpecialEdition(Element element) {
        String name = buildName(element);
        return !MANGALINE_NAME_PATTERN.matcher(name).find();
    }
}
