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
import java.util.regex.Matcher;

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
            log.info("Reading page {}", index);
            Document document = Jsoup.connect(ReaderConstants.DISTRITO_MANGA_BASE_URL + url)
                    .userAgent(ReaderConstants.USER_AGENT)
                    .get();
            Elements elements = document.select("article.product-miniature .product-description");
            return elements
                    .stream()
                    .map(this::buildItem)
                    .toList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    @Override
    public List<ItemDTO> getDetails() {
        log.info("Getting item details from Distrito Manga");
        return getIssues()
                .stream()
                .map(this::buildDetails)
                .toList();
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> databaseList) {
        log.info("Getting item details from Distrito Manga");
        return getIssues()
                .stream()
                .filter(item -> !databaseList.contains(item))
                .map(this::buildDetails)
                .toList();
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .name(getName(element))
                .link(getLink(element))
                .number(getNumber(element))
                .price(getPrice(element))
                .isbn(getISBN(element))
                .currency(ReaderConstants.MXN_CURRENCY)
                .edition(ReaderConstants.DEFAULT_EDITION)
                .variant(Boolean.FALSE)
                .build();
    }

    private ItemDTO buildDetails(ItemDTO itemDTO) {
        log.info(String.format("Reading %s", itemDTO.getName()));
        try {
            Document document = Jsoup.connect(itemDTO.getLink())
                    .userAgent(ReaderConstants.USER_AGENT)
                    .get();
            itemDTO.setShortDescription(getShortDescription(document));
            itemDTO.setPages(getPageNumbers(document));
            itemDTO.setDate(getDate(document));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemDTO;
    }

    private String getName(Element element) {
        return element.select("h3.h3.product-title a").text();
    }

    private String getLink(Element element) {
        return element.select("h3.h3.product-title a").attr("href");
    }

    private Integer getNumber(Element element) {
        Matcher matcher = ReaderConstants.DISTRITO_MANGA_NUMBER_PATTERN.matcher(element.select("h3.h3.product-title a").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String getISBN(Element element) {
        return element.select(".product-reference.text-muted a").text();
    }

    private Double getPrice(Element element) {
        return Double.valueOf(element.select(".col-12.p-0.mt-2.ml-1 .product-price-and-shipping a span.product-price").attr("content"));
    }

    private String getShortDescription(Document document) {
        return document.select("#description .product-description .rte-content .p_leer_mas.p_leer_mas_prod p").text();
    }

    private Integer getPageNumbers(Document document) {
        return Integer.parseInt((document.select("#product-details .product-features.d-none.d-lg-block dl.caracteristicas-prod.data-sheet dd:nth-child(4)").text()));
    }

    private String getDate(Document document) {
        String date = document.select("#product-details .product-availability-date span").text();
        return new StringBuilder()
                .append(date.substring(6, 10))
                .append("-")
                .append(date.substring(3, 5))
                .append("-")
                .append(date.substring(0, 2))
                .toString();
    }
}
