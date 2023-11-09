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
public class PaniniLectorV2 implements Lector {

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
            log.info("Getting total pages from {}{}", ReaderConstants.PANINI_BASE_URL, url);
            Document document = Jsoup.connect(ReaderConstants.PANINI_BASE_URL + url).get();
            return document.select("#toolbar-amount").get(0).select(".toolbar-number").size() == 1
                    ? 1
                    : (int) Math.ceil(Double.parseDouble(document.select("#toolbar-amount").get(0).select(".toolbar-number").get(2).text()) / 12);
        } catch (Exception e) {
            log.info("There was an issue reading the document to calculate the total pages to parse. {}", e.getMessage());
        }
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info("Reading page {}", index);
            String page = ReaderConstants.PANINI_BASE_URL + url + "&p=" + index;
            Document document = Jsoup.connect(page).get();
            Elements issues = document.select("ol.product-items li.product-item [id^=product-item-info_]");
            return issues
                    .stream()
                    .map(issue -> ItemDTO
                            .builder()
                            .titleId(titleId)
                            .link(issue.select(LINK_LOCATION).attr("href"))
                            .name(this.getName(issue))
                            .number(this.getNumber(issue))
                            .price(this.getPrice(issue))
                            .currency(ReaderConstants.MXN_CURRENCY)
                            .edition(ReaderConstants.DEFAULT_EDITION)
                            .isbn(ReaderConstants.DEFAULT_ISBN)
                            .variant(Boolean.FALSE)
                            .build())
                    .toList();
        } catch (Exception e) {
            log.info("There was an issue reading the document. {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO itemDTO) {
        log.info("Reading {}", itemDTO.getName());
        try {
            Document document = Jsoup.connect(itemDTO.getLink()).get();
            itemDTO.setShortDescription(this.getDescription(document));
            itemDTO.setPages(this.getNumberPages(document));
            itemDTO.setCover(getCover());
        } catch (Exception e) {
            log.info("There was an issue reading the detail document for {}. {}", itemDTO.getName(), e.getMessage());
        }
        return itemDTO;
    }

    private int getNumber(Element element) {
        String text = element.select(LINK_LOCATION).text();
        Matcher matcher = ReaderConstants.PANINI_V2_NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String getName(Element element) {
        return element
                .select(LINK_LOCATION)
                .text()
                .trim()
                .replace(" N.", " #");
    }

    private String getDescription(Element element) {
        String description = element.select(".product-info-main .overview").text();
        return description.isEmpty() ? "-" : description;
    }

    private int getNumberPages(Element element) {
        String pages = element.select("#product-attribute-specs-table tbody tr td[data-th='Número de páginas']").text();
        return pages.isEmpty()
                ? 0
                : Integer.parseInt(pages.trim());
    }

    private double getPrice(Element element) {
        return Double.parseDouble(element
                .select(".product-item-details .price-final_price span.price-container [id^=product-price-]")
                .attr("data-price-amount"));
    }
}
