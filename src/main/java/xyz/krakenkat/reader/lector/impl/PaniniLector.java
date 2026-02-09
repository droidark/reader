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
import java.util.List;
import java.util.regex.Matcher;

import static java.lang.Boolean.FALSE;
import static xyz.krakenkat.reader.util.ReaderConstants.*;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaniniLector implements Lector {

    private String titleId;
    private String key;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;
    private static final String LINK_LOCATION = ".product-item-details strong.product-item-name a.product-item-link";

    @Override
    public Integer getTotalPages() {
        try {
            log.info("Getting total pages from {}{}", PANINI_BASE_URL, url);
            Document document = Jsoup.connect(PANINI_BASE_URL + url).get();
            return document.select("#toolbar-amount").getFirst().select(".toolbar-number").size() == 1
                    ? 1
                    : (int) Math.ceil(Double.parseDouble(document.select("#toolbar-amount").getFirst().select(".toolbar-number").get(2).text()) / 12);
        } catch (IOException e) {
            log.info("There was an issue reading the document to calculate the total pages to parse. {}", e.getMessage());
        }
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info("Reading page {}", index);
            String page = PANINI_BASE_URL + url + "&p=" + index;
            Document document = Jsoup.connect(page).get();
            Elements elements = document.select("ol.product-items li.product-item [id^=product-item-info_]");

            return elements
                    .stream()
                    .map(this::buildItem)
                    .toList();
        } catch (IOException e) {
            log.info("There was an issue reading the document. {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO itemDTO) {
        log.info("Reading details for {}", itemDTO.getName());
        try {
            Document document = Jsoup.connect(itemDTO.getLink()).get();
            itemDTO.setShortDescription(buildDescription(document));
            itemDTO.setPages(buildNumberPages(document));
            itemDTO.setCover(getCover());
        } catch (IOException e) {
            log.info("There was an issue reading the detail document for {}. {}", itemDTO.getName(), e.getMessage());
        }
        return itemDTO;
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .name(buildName(element))
                .link(buildLink(element))
                .number(buildNumber(element))
                .cover(buildCover(element, buildNumber(element), key, path, folder, download))
                .price(buildPrice(element))
                .currency(MXN_CURRENCY)
                .edition(DEFAULT_EDITION)
                .isbn(DEFAULT_ISBN)
                .variant(FALSE)
                .build();
    }

    private String buildName(Element element) {
        return element
                .select(LINK_LOCATION)
                .text()
                .trim()
                .replace(" N.", " #");
    }

    private String buildLink(Element element) {
        return element.select(LINK_LOCATION).attr("href");
    }

    private int buildNumber(Element element) {
        String text = element.select(LINK_LOCATION).text();
        Matcher matcher = PANINI_NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String buildDescription(Element element) {
        String description = element.select(".product-info-main .overview").text();
        return description.isEmpty() ? "-" : description;
    }

    private int buildNumberPages(Element element) {
        String pages = element.select("#product-attribute-specs-table tbody tr td[data-th='Número de páginas']").text();
        return pages.isEmpty() ? 0 : Integer.parseInt(pages.trim());
    }

    private double buildPrice(Element element) {
        return Double.parseDouble(element
                .select(".product-item-details .price-final_price span.price-container [id^=product-price-]")
                .attr("data-price-amount"));
    }

    private String buildCover(Element element, int number, String key, String path, String folder, boolean download) {
        return download ? this.getCover(number, key, path, folder, getCoverUrl(element)) : DEFAULT_EMPTY;
    }

    private String getCoverUrl(Element element) {
        String text = element.select(".product-image-photo").attr("first-src");
        Matcher matcher = PANINI_IMAGE_URL_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(0) : "no-url-found";
    }
}
