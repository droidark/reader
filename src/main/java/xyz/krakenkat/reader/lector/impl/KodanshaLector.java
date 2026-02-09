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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

import static xyz.krakenkat.reader.util.ReaderConstants.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class KodanshaLector implements Lector {

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
            String page = KODANSHA_BASE_URL + url;
            Document document = Jsoup.connect(page).get();
            Elements issues = document.select(".series-desktop-volume-grid .series-volumeItem");

            return issues
                    .stream()
                    .map(this::buildItem)
                    .toList();
        } catch (IOException e) {
            log.info("There was an error processing the page: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO item) {
        try {
            String page = KODANSHA_BASE_URL + item.getLink();
            Document document = Jsoup.connect(page).get();

            item.setName(buildName(document));
            item.setShortDescription(buildShortDescription(document));
            item.setIsbn(buildISBN(document));
        } catch (IOException e) {
            log.info("There was an error parsing the info for {}: {}", item.getName(), e.getMessage());
        }
        return item;
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .link(buildLink(element))
                .number(buildNumber(element))
                .price(buildPrice(element))
                .digitalPrice(buildPrice(element))
                .cover(buildCover(element, buildNumber(element), key, path, folder, download))
                .pages(buildPages(element))
                .edition(DEFAULT_EDITION)
                .variant(Boolean.FALSE)
                .date(buildDate(element))
                .currency(USD_CURRENCY)
                .build();
    }

    private String buildName(Element element) {
        return element.select("h1.product-title").text().trim();
    }

    private String buildLink(Element element) {
        String selector = ".series-volume-item-top-container.series-volume-item-top-container-desktop .series-volume-item-container.series-volume-item-container-normal .product-item-column-wrapper .product-item-wrapper a";
        return element.select(selector).attr("href").trim();
    }

    private Integer buildNumber(Element element) {
        String selector = ".series-volume-item-top-container.series-volume-item-top-container-desktop .series-volume-item-container.series-volume-item-container-normal .product-item-column-wrapper .product-item-wrapper a h3.product-name";
        String text = element.select(selector).text();
        Matcher matcher = KODANSHA_NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private Integer buildPages(Element element) {
        String selector = ".series-volume-item-top-container.series-volume-item-top-container-desktop .series-volume-item-container.series-volume-item-container-normal .product-item-column-wrapper .product-item-wrapper .info-wrapper .date-price-wrapper .date-pages > span:last-of-type";
        String text = element.select(selector).text();
        Matcher matcher = KODANSHA_PAGES_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private Double buildPrice(Element element) {
        String selector = ".series-volume-item-top-container.series-volume-item-top-container-desktop .series-volume-item-container.series-volume-item-container-normal .product-item-column-wrapper .product-item-wrapper .info-wrapper .price-grid-desktop .price-grid .price-grid-item .prices-wrapper .price";
        String text = element.select(selector).text().trim();
        Matcher matcher = KODANSHA_PRICE_PATTERN.matcher(text);
        return matcher.find() ? Double.parseDouble(matcher.group(0)) : 0.00;
    }

    private String buildCover(Element element, int number, String key, String path, String folder, boolean download) {
        return download ? this.getCover(number, key, path, folder, getCoverUrl(element)) : DEFAULT_EMPTY;
    }

    private String getCoverUrl(Element element) {
        String selector = ".series-volume-item-top-container.series-volume-item-top-container-desktop .series-volume-item-container.series-volume-item-container-normal .product-item-column-wrapper .product-item-wrapper a .ui-product-poster-container .ui-product-poster-container-2 .ui-product-poster-wrapper picture source:first-of-type";
        String text = element.select(selector).attr("srcset");
        Matcher matcher = KODANSHA_IMAGE_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(0) : "no-url-found";
    }

    private String buildDate(Element element) {
        DateTimeFormatter[] inputFormatters = {
                DateTimeFormatter.ofPattern("MMM. d, yyyy"),
                DateTimeFormatter.ofPattern("MMM dd, yyyy")
        };

        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String selector = ".series-volume-item-top-container.series-volume-item-top-container-desktop .series-volume-item-container.series-volume-item-container-normal .product-item-column-wrapper .product-item-wrapper .info-wrapper .date-price-wrapper .date-pages > span:first-of-type";
        String inputDate = element.select(selector).text().trim();

        return Arrays
                .stream(inputFormatters)
                .map(formatter -> {
                    try {
                        return LocalDate.parse(inputDate, formatter);
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .findFirst()
                .map(date -> date.format(outputFormatter))
                .orElse(DEFAULT_DATE);
    }

    private String buildShortDescription(Document document) {
        String selector = "p.series-desktop-header-info-description";
        return document.select(selector).text().trim();
    }

    private String buildISBN(Document document) {
        String selector = ".product-desktop-rating-wrapper .product-desktop-rating-table-container .product-desktop-rating-table-second-row-wrapper .product-desktop-rating-table-second-row-inner-wrapper .product-desktop-rating-table-title-value-wrapper:nth-of-type(3) .product-desktop-rating-table-value-wrapper";
        String isbn = document.select(selector).text().trim();
        if (isbn.length() != 13 || !isbn.matches(KODANSHA_ISBN_PATTERN)) {
            log.warn("Invalid ISBN-13 format");
            return DEFAULT_ISBN;
        }
        return String.format("%s-%s-%s-%s-%s",
                isbn.substring(0, 3),           // Group 1: First 3 digits
                isbn.charAt(3),                 // Group 2: 1 digit
                isbn.substring(4, 6),           // Group 3: 2 digits
                isbn.substring(6, 12),          // Group 4: 6 digits
                isbn.substring(12));  // Group 5: 1 digit
    }
}
