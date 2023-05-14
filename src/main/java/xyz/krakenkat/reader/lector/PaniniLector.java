package xyz.krakenkat.reader.lector;

import lombok.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Matcher;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaniniLector implements Lector {

    private String key;
    private String titleId;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;

    public Integer getTotalPages() {
        int pages = 1;
        try {
            Document document = Jsoup.connect(ReaderConstants.PANINI_BASE_URL + url).get();
            boolean isOnePage = document.select(".results *").isEmpty();

            if(!isOnePage) {
                Matcher totalPages = ReaderConstants.PANINI_TOTAL_PAGES_PATTERN.matcher(
                        document.select(".results .contagempaginas").get(0).text());
                if (totalPages.find()) {
                    return Integer.parseInt(totalPages.group(0).trim());
                }
                return pages;
            }
            return pages;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pages;
    }

    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            String page = "&pg=" + index;
            Document document = Jsoup.connect(ReaderConstants.PANINI_BASE_URL + url + page).get();
            Elements items = document
                    .select(".row")
                    .get(3)
                    .select(".item");

            return items.stream()
                    .filter(Predicate.not(this::isBoxSet))
                    .map(this::buildItem)
                    .filter(Objects::nonNull)
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public ItemDTO buildDetails(ItemDTO item) {
        try {
            Document document = Jsoup.connect(item.getLink()).get();
            Elements additionalInfo = document.select("#more-infos .container ul.list-group li ul li");
            item.setShortDescription(!document.select("#content .details-description p").isEmpty()
                    ? document.select("#content .details-description p").get(0).text()
                    : "-");
            item.setPages(getPages(additionalInfo));
            item.setIsbn(getIsbn(additionalInfo));
            item.setCover(getCover());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    private boolean isBoxSet(Element element) {
        return ReaderConstants.PANINI_BOXSET_PATTERN.matcher(element.select("p.little-desc a").text()).find();
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .name(this.getName(element))
                .link(element.select(".description h4 a").attr("href"))
                .number(this.getNumber(element))
                .price(Double.parseDouble(element.select("p.price").text().substring(1)))
                .currency(ReaderConstants.MXN_CURRENCY)
                .edition(1)
                .variant(Boolean.FALSE)
                .build();
    }

    private String getName(Element element) {
        return element.select(".description h4 a").text().replace(" - ", " ");
    }

    private int getNumber(Element element) {
        Matcher matcher = ReaderConstants.PANINI_NUMBER_PATTERN.matcher(this.getName(element));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        } else {
            Matcher numberMatcher = ReaderConstants.PANINI_NUMBER_PATTERN.matcher(element.select("p.little-desc a").text());
            if (numberMatcher.find())
                return Integer.parseInt(numberMatcher.group(0));
            return 0;
        }
    }

    private Integer getPages(Elements additionalInfo) {
        for (Element e : additionalInfo) {
            Matcher pages = ReaderConstants.PANINI_PAGE_PATTERN.matcher(e.select("strong").text());
            if (pages.find()) {
                Matcher numberPages = ReaderConstants.PANINI_PAGE_NUMBER_PATTERN.matcher(e.text());
                if (numberPages.find()) {
                    return Integer.parseInt(numberPages.group(0).trim());
                }
            }
        }
        return ReaderConstants.DEFAULT_PAGES;
    }

    private String getIsbn(Elements additionalInfo) {
        for (Element e : additionalInfo) {
            Matcher isbn = ReaderConstants.PANINI_ISBN_PATTERN.matcher(e.select("strong").text());
            if (isbn.find()) {
                return e.text().split(":")[1].trim();
            }
        }
        return ReaderConstants.DEFAULT_ISBN;
    }
}
