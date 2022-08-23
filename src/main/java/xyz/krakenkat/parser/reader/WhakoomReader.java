package xyz.krakenkat.parser.reader;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.parser.dto.ItemDTO;
import xyz.krakenkat.parser.util.ReaderConstants;

import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class WhakoomReader implements Reader {

    private String key = ResourceBundle.getBundle("application").getString("reader.key");
    private String title = ResourceBundle.getBundle("application").getString("reader.title.whakoom");
    private final Pattern totalIssuesPattern = Pattern.compile(ReaderConstants.WHAKOOM_TOTAL_ISSUES_PATTERN);

    public void setKey(String key) {
        this.key = key;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> items) {
        log.info("Getting item details from Whakoom");
        return items
                .stream()
                .map(item -> {
                    try {
                        Document document = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + item.getLink()).get();
                        Element element = document.select(".b-info").get(0);

                        item.setNumber(this.getNumber(element));
                        item.setName(this.getName(element, item.getNumber()));
                        item.setShortDescription(this.getShortDescription(document));
                        log.info("Downloading covers form Whakoom");
                        item.setCover(this.saveCover(
                                element.select("p.comic-cover a.fancybox").attr("href"),
                                key,
                                item.getNumber()));

                        Elements elems = document.select(".info .content .info-item");
                        item.setDate(!elems.isEmpty() ? elems.get(0).select("p").attr("content") : ReaderConstants.DEFAULT_DATE);
                        item.setIsbn(elems.size() >= 2 ? elems.get(1).select("ul li").text() : ReaderConstants.DEFAULT_ISBN);
                    } catch (Exception e) {
                        log.error(String.format("An issue happened at the moment to read the HTML document %s", e.getMessage()));
                    }
                    return item;
                })
                .sorted(Comparator.comparing(ItemDTO::getNumber))
                .toList();
    }

    public Integer getTotalPages() {
        log.info(String.format("Getting total pages from %s%s", ReaderConstants.WHAKOOM_BASE_URL, title));
        try {
            Document document = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + title).get();
            Matcher totalIssues = totalIssuesPattern.matcher(document.select("p.edition-issues").text());
            return totalIssues.find() ? (int) Math.ceil(Double.parseDouble(totalIssues.group(0)) / 48) : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public List<ItemDTO> getSinglePage(Integer index) {
        log.info(String.format("Reading %s, page %d", key, index));
        try {
            String page = "?page=" + index;
            Document doc = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + title + page).get();
            Elements issues = doc.select("ul.v2-cover-list li");
            return issues
                    .stream()
                    .map(issue -> ItemDTO
                            .builder()
                            .link(issue.select("a").attr("href"))
                            .currency("MXN")
                            .edition(1)
                            .variant(false)
                            .build())
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private int getNumber(Element element) {
        return Integer.parseInt(element.select("h1 strong").text().substring(1));
    }

    private String getName(Element element, int number) {
        return element.select("span").text().replace("&nbps;", "").trim() + " #" + number;
    }

    private String getShortDescription(Element element) {
        Elements info = element.select(".wiki .wiki-content .wiki-text p");
        return !info.isEmpty() ? info.get(0).text().trim() : ReaderConstants.DEFAULT_EMPTY;
    }
}
