package xyz.krakenkat.parser.reader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.parser.dto.ItemDTO;

import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class KamiteReader implements Reader {

    private String KEY;
    private String URL;
    private String TITLE;
    private Pattern NUMBER_PATTERN;
    private Pattern PAGE_NUMBER_PATTERN;
    private Pattern BOXSET_PATTERN;

    public KamiteReader() {
        ResourceBundle rb = ResourceBundle.getBundle("system");
        this.KEY = rb.getString("key");
        this.URL = rb.getString("kamite-url");
        this.TITLE = rb.getString("kamite-title");
        this.NUMBER_PATTERN = Pattern.compile(rb.getString("kamite-number-pattern"));
        this.PAGE_NUMBER_PATTERN = Pattern.compile(rb.getString("kamite-page-number-pattern"));
        this.BOXSET_PATTERN = Pattern.compile(rb.getString("kamite-boxset-pattern"), Pattern.CASE_INSENSITIVE);
    }

    @Override
    public Integer getTotalPages() {
        return 1;
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            Document document = Jsoup.connect(URL + TITLE).get();
            Elements elements = document.select(".product_list .product-container");
            return elements
                    .stream()
                    .filter(Predicate.not(this::isBoxSet))
                    .map(this::buildItem)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> items) {
        return items.stream().map(item -> {
            try {
                Document document = Jsoup.connect(item.getLink()).get();
                item.setShortDescription(document.select("#tab2 .rte p").get(0).text());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return item;
        }).collect(Collectors.toList());
    }

    private boolean isBoxSet(Element element) {
        String title = element
                .select(".right-block .product-meta h5.name a.product-name")
                .attr("title");
        Matcher matcher = BOXSET_PATTERN.matcher(title);
        return matcher.find();
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .link(element.select(".right-block .product-meta h5.name a.product-name").attr("href"))
                .name(element.select(".right-block .product-meta h5.name a.product-name").attr("title"))
                .price(this.getPrice(element))
                .number(this.getNumber(element))
                .pages(this.getPageNumber(element))
                .isbn("000-0000000000")
                .currency("MXN")
                .edition(1)
                .build();
    }

    private int getNumber(Element element) {
        String title = element.select(".right-block .product-meta h5.name a.product-name").attr("title");
        Matcher matcher = NUMBER_PATTERN.matcher(title);
        if (matcher.find())
            return Integer.parseInt(matcher.group(0).trim());
        return 0;

    }

    private double getPrice(Element element) {
        return Double.parseDouble(element.select(".right-block .product-meta .content_price .product-price")
                .text()
                .replace("$", "")
                .trim());
    }

    private int getPageNumber(Element element) {
        String text = element.select(".right-block .product-meta .product-desc").text();
        Matcher matcher = PAGE_NUMBER_PATTERN.matcher(text);
        if (matcher.find())
            return Integer.parseInt(matcher.group(0).trim());
        return 192;
    }
}
