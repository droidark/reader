package xyz.krakenkat.reader.lector;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;

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
            String page = ReaderConstants.VIZ_BASE_URL + url;
            Document document = Jsoup.connect(page).get();
            Elements issues = document.select(".shelf.flex.flex-wrap.type-rg.line-caption article");
            return issues
                    .stream()
                    .map(issue -> ItemDTO
                            .builder()
                            .titleId(titleId)
                            .link(issue.select("figure a.product-thumb").attr("href"))
                            .name(issue.select(".pad-x-md a.color-off-black.hover-red").text())
                            .number(this.getNumber(issue))
                            .currency(ReaderConstants.USD_CURRENCY)
                            .edition(ReaderConstants.DEFAULT_EDITION)
                            .variant(Boolean.FALSE)
                            .build())
                    .toList();
        } catch (Exception e) {
            log.info("There was an error processing the page: {}", e.getMessage());
        }
        return List.of();
    }

    @Override
    public ItemDTO buildDetails(ItemDTO item) {
        try {
            Thread.sleep(5000);
            Document document = Jsoup
                    .connect(ReaderConstants.VIZ_BASE_URL + item.getLink())
                    .userAgent(ReaderConstants.USER_AGENT)
                    .get();
            item.setShortDescription(document.select(".row.pad-b-xl .g-6--lg.pad-x-lg--lg.mar-b-lg.type-rg.type-md--md.line-caption.text-spacing p").text());
            item.setDate(this.getReleaseDate(document));
            item.setIsbn(this.getISBN(document));
            item.setPrice(this.getPrice(document));
            item.setPages(this.getPages(document));
            item.setCover(this.getCover(item.getNumber().toString(), key, path, folder,
                    document.select(".product-image.mar-x-auto.mar-b-lg.pad-x-md img").attr("src")));
            // document.select(".row.pad-b-xl g-6--lg .type-sm.type-rg--md.line-caption .g-6--md.g-omega--md .mar-b-md:first-child").text()
        } catch (Exception e) {
            log.info("There was an error parsing the info for {}: {}", item.getName(), e.getMessage());
        }
        return item;
    }

    private Integer getNumber(Element element) {
        String text = element.select(".pad-x-md a.color-off-black.hover-red").text();
        Matcher matcher = ReaderConstants.VIZ_NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String getReleaseDate(Element element) {
        String text = element.select(".o_release-date.mar-b-md").text();
        Matcher matcher = ReaderConstants.VIZ_DATE_PATTERN.matcher(text);
        return matcher.find() ? LocalDate.parse(matcher.group(0), ReaderConstants.DATETIME_FORMATTER).toString() : ReaderConstants.DEFAULT_DATE;
    }

    private String getISBN(Element element) {
        String text = element.select(".o_isbn13.mar-b-md").text();
        Matcher matcher = ReaderConstants.VIZ_ISBN_PATTERN.matcher(text);
        return matcher.find() ? matcher.group(0) : ReaderConstants.DEFAULT_ISBN;
    }

    private Double getPrice(Element element) {
        String text = element.select(".type-md.type-lg--md.type-xl--lg.line-solid.weight-bold").text();
        Matcher matcher = ReaderConstants.VIZ_PRICE_PATTERN.matcher(text);
        return  matcher.find() ? Double.parseDouble(matcher.group(0)) : 0.00;
    }

    private Integer getPages(Element element) {
        String text = element.select(".g-6--md.g-omega--md .mar-b-md:first-child").text();
        Matcher matcher = ReaderConstants.VIZ_PAGES_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0)) : ReaderConstants.DEFAULT_PAGES;
    }
}
