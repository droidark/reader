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
import java.util.List;
import java.util.regex.Matcher;

import static xyz.krakenkat.reader.util.ReaderConstants.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class VizLector implements Lector {

    private String titleId;
    private String key;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;

    @Override
    public Integer getTotalPages() { return 1; }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            String page = VIZ_BASE_URL + url;
            Document document = Jsoup.connect(page).get();
            Elements issues = document.select(".shelf.flex.flex-wrap.type-rg.line-caption article");
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
            Thread.sleep(5000);
            Document document = Jsoup.connect(VIZ_BASE_URL + item.getLink()).userAgent(USER_AGENT).get();
            item.setShortDescription(buildShortDescription(document));
            item.setDate(buildReleaseDate(document));
            item.setIsbn(buildISBN(document));
            item.setPrice(buildPrice(document));
            item.setPages(buildPages(document));
            item.setCover(buildCover(document, item.getNumber(), key, path, folder, download));
        } catch (IOException | InterruptedException e) {
            log.info("There was an error parsing the info for {}: {}", item.getName(), e.getMessage());
        }
        return item;
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .name(buildName(element))
                .link(buildLink(element))
                .number(buildNumber(element))
                .digitalPrice(DEFAULT_US_DIGITAL_PRICE)
                .currency(USD_CURRENCY)
                .edition(DEFAULT_EDITION)
                .variant(Boolean.FALSE)
                .build();
    }

    private String buildName(Element element) {
        return element.select(".pad-x-md a.color-off-black.hover-red").text();
    }

    private String buildLink(Element element) {
        return element.select("figure a.product-thumb").attr("href");
    }

    private Integer buildNumber(Element element) {
        String text = element.select(".pad-x-md a.color-off-black.hover-red").text();
        Matcher matcher = VIZ_NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private Integer buildPages(Element element) {
        String text = element.select(".g-6--md.g-omega--md .mar-b-md:first-child").text();
        Matcher matcher = VIZ_PAGES_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0)) : DEFAULT_PAGES;
    }

    private Double buildPrice(Element element) {
        String text = element.select(".type-md.type-lg--md.type-xl--lg.line-solid.weight-bold").text();
        Matcher matcher = VIZ_PRICE_PATTERN.matcher(text);
        return  matcher.find() ? Double.parseDouble(matcher.group(0)) : 0.00;
    }

    private String buildCover(Element element, int number, String key, String path, String folder, boolean download) {
        String coverUrl = element.select(".product-image.mar-x-auto.mar-b-lg.pad-x-md img").attr("src");
        return download ? this.getCover(number, key, path, folder, coverUrl) : DEFAULT_EMPTY;
    }

    private String buildShortDescription(Element element) {
        return element.select(".row.pad-b-xl .g-6--lg.pad-x-lg--lg.mar-b-lg.type-rg.type-md--md.line-caption.text-spacing p").text();
    }

    private String buildReleaseDate(Element element) {
        String text = element.select(".o_release-date.mar-b-md").text();
        Matcher matcher = VIZ_DATE_PATTERN.matcher(text);
        return matcher.find() ? LocalDate.parse(matcher.group(0), DATETIME_FORMATTER).toString() : DEFAULT_DATE;
    }

    private String buildISBN(Element element) {
        String text = element.select(".o_isbn13.mar-b-md").text();
        Matcher matcher = VIZ_ISBN_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(0) : DEFAULT_ISBN;
    }
}
