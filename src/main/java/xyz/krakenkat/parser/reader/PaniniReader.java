package xyz.krakenkat.parser.reader;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.parser.dto.ItemDTO;

import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@Setter
public class PaniniReader implements Reader {

    private String KEY;
    private String URL;
    private String TITLE;
    private String PATH;
    private Pattern PAGE_PATTERN;
    private Pattern NUMBER_PATTERN;
    private Pattern PAGE_NUMBER_PATTERN;
    private Pattern TOTAL_PAGES_PATTERN;
    private Pattern ISBN_PATTERN;
    private Pattern BOXSET_PATTERN;

    public PaniniReader() {
        ResourceBundle rb = ResourceBundle.getBundle("system");
        this.KEY = rb.getString("key");
        this.URL = rb.getString("panini-url");
        this.TITLE = rb.getString("panini-title");
        this.PATH = rb.getString("images-path");
        this.PAGE_PATTERN = Pattern.compile(rb.getString("panini-page-pattern"));
        this.NUMBER_PATTERN = Pattern.compile(rb.getString("panini-number-pattern"));
        this.PAGE_NUMBER_PATTERN = Pattern.compile(rb.getString("panini-page-number-pattern"));
        this.TOTAL_PAGES_PATTERN = Pattern.compile(rb.getString("panini-total-pages-pattern"));
        this.ISBN_PATTERN = Pattern.compile(rb.getString("panini-isbn-pattern"), Pattern.CASE_INSENSITIVE);
        this.BOXSET_PATTERN = Pattern.compile(rb.getString("panini-boxset-pattern"), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> items) {
        return items.stream().map(item -> {
            try {
                Document document = Jsoup.connect(item.getLink()).get();
                Elements additionalInfo = document.select("#more-infos .container ul.list-group li ul li");
                item.setShortDescription(document.select("#content .details-description p").size() >= 1
                        ? document.select("#content .details-description p").get(0).text()
                        : "-");
                item.setPages(getPages(additionalInfo));
                item.setIsbn(getIsbn(additionalInfo));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return item;
        }).collect(Collectors.toList());
    }

    public Integer getTotalPages() {
        int pages = 1;
        try {
            Document document = Jsoup.connect(URL + TITLE).get();
            boolean isOnePage = document.select(".results *").isEmpty();

            if(!isOnePage) {
                Matcher totalPages = TOTAL_PAGES_PATTERN.matcher(
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
            Document document = Jsoup.connect(URL + TITLE + page).get();
            Elements items = document
                    .select(".row")
                    .get(3)
                    .select(".item");

            return items.stream()
                    .filter(Predicate.not(this::isBoxSet))
                    .map(this::buildItem)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean isBoxSet(Element element) {
        return BOXSET_PATTERN.matcher(element.select("p.little-desc a").text()).find();
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .name(this.getName(element))
                .link(element.select(".description h4 a").attr("href"))
                .number(this.getNumber(element))
                .price(Double.parseDouble(element.select("p.price").text().substring(1)))
                .currency("MXN")
                .edition(1)
                .variant(false)
                .build();
    }

    private String getName(Element element) {
        return element.select(".description h4 a").text().replace(" - ", " ");
    }

    private int getNumber(Element element) {
        Matcher matcher = NUMBER_PATTERN.matcher(this.getName(element));
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(0));
        } else {
            Matcher numberMatcher = NUMBER_PATTERN.matcher(element.select("p.little-desc a").text());
            if (numberMatcher.find())
                return Integer.parseInt(numberMatcher.group(0));
            return 0;
        }
    }

    private Integer getPages(Elements additionalInfo) {
        for (Element e : additionalInfo) {
            Matcher pages = PAGE_PATTERN.matcher(e.select("strong").text());
            if (pages.find()) {
                Matcher numberPages = PAGE_NUMBER_PATTERN.matcher(e.text());
                if (numberPages.find()) {
                    return Integer.parseInt(numberPages.group(0).trim());
                }
            }
        }
        return 192;
    }

    private String getIsbn(Elements additionalInfo) {
        for (Element e : additionalInfo) {
            Matcher isbn = ISBN_PATTERN.matcher(e.select("strong").text());
            if (isbn.find()) {
                return e.text().split(":")[1].trim();
            }
        }
        return "000-0000000000";
    }
}
