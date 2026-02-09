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
public class DistritoMangaLector implements Lector {

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
            Document document = Jsoup.connect(DISTRITO_MANGA_BASE_URL + url).userAgent(USER_AGENT).get();
            Elements elements = document.select("article.product-miniature .product-description");

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
        log.info("Reading {}", itemDTO.getName());
        try {
            Thread.sleep(5000);
            Document document = Jsoup.connect(itemDTO.getLink())
                    .userAgent(USER_AGENT)
                    .get();
            itemDTO.setShortDescription(buildShortDescription(document));
            itemDTO.setPages(buildPageNumbers(document));
            itemDTO.setDate(buildDate(document));
        } catch (IOException | InterruptedException e) {
            log.info("There was an issue {} reading the detail document for {}", e.getMessage(), itemDTO.getName());
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
                .price(buildPrice(element))
                .isbn(buildISBN(element))
                .currency(MXN_CURRENCY)
                .edition(DEFAULT_EDITION)
                .variant(FALSE)
                .build();
    }

    private String buildName(Element element) {
        return element.select("p.h3.product-title.productTitle a span").text();
    }

    private String buildLink(Element element) {
        return element.select("p.h3.product-title.productTitle a").attr("href");
    }

    private Integer buildNumber(Element element) {
        Matcher matcher = DISTRITO_MANGA_NUMBER_PATTERN.matcher(element.select("p.h3.product-title.productTitle a").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String buildISBN(Element element) {
        return element.select(".product-reference.text-muted a").text();
    }

    private Double buildPrice(Element element) {
        return Double.parseDouble(element.select(".product-price.col-3").attr("content"));
    }

    private String buildShortDescription(Document document) {
        return document.select("#description .product-description .rte-content .p_leer_mas.p_leer_mas_prod p").text();
    }

    private Integer buildPageNumbers(Document document) {
        return Integer.parseInt((document.select("#product-details section.product-features.d-block.d-lg-block dl.caracteristicas-prod.data-sheet dd:nth-child(4)").text()));
    }

    private String buildDate(Document document) {
        String date = document.select("#product-details section.product-features.d-block.d-lg-block dl.caracteristicas-prod.data-sheet dd:nth-child(14)").text();
        return date.substring(6, 10) + DEFAULT_EMPTY + date.substring(3, 5) + DEFAULT_EMPTY + date.substring(0, 2);
    }
}
