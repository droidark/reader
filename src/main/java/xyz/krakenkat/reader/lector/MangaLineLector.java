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
            Elements issues = document.select("ul.products.products-list.row.grid li");
            return issues
                    .stream()
                    .map(this::buildItem)
                    .toList();
        } catch (Exception e) {
            log.info(String.format("There was an error processing the page: %s", e.getMessage()));
        }
        return List.of();
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .link(element.select(".products-entry .products-content .contents h3.product-title a").attr("href"))
                .name(element.select(".products-entry .products-content .contents h3.product-title a").text())
                .number(getNumber(element))
                .pages(ReaderConstants.DEFAULT_PAGES)
                .price(getPrice(element))
                .isbn(ReaderConstants.DEFAULT_ISBN)
                .currency(ReaderConstants.MXN_CURRENCY)
                .edition(ReaderConstants.DEFAULT_EDITION)
                .variant(Boolean.FALSE)
                .build();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO itemDTO) {
        log.info("Reading {}", itemDTO.getName());
        try {
            Document document = Jsoup.connect(itemDTO.getLink())
                    .get();
            itemDTO.setShortDescription(document.select("div[itemprop='description'] p").text());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemDTO;
    }

    private Integer getNumber(Element element) {
        Matcher matcher = ReaderConstants.MANGALINE_NUMBER_PATTERN.matcher(element.select(".products-entry .products-content .contents h3.product-title a").text());
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private Double getPrice(Element element) {
        String text = element.select(".products-entry .products-content .contents span.price span.woocommerce-Price-amount bdi").text();
        Matcher matcher = ReaderConstants.MANGALINE_PRICE_PATTERN.matcher(text);
        return  matcher.find() ? Double.parseDouble(matcher.group(0)) : 0.00;
    }
}
