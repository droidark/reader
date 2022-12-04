package xyz.krakenkat.reader.lector;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhakoomLector implements Lector {

    private String titleId;
    private String key;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;

    @Override
    public List<ItemDTO> getDetails() {
        log.info("Getting item details from Whakoom");
        return getIssues()
                .stream()
                .map(this::buildDetails)
                .sorted(Comparator.comparing(ItemDTO::getNumber))
                .toList();
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> databaseList) {
        log.info("Getting item details from Whakoom");
        return getIssues()
                .stream()
                .filter(item -> !databaseList.contains(item))
                .map(this::buildDetails)
                .sorted(Comparator.comparing(ItemDTO::getNumber))
                .toList();
    }

    public Integer getTotalPages() {
        try {
            log.info(String.format("Getting total pages from %s%s", ReaderConstants.WHAKOOM_BASE_URL, url));
            Document document = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + url).get();
            Matcher totalIssues = ReaderConstants.WHAKOOM_TOTAL_ISSUES_PATTERN.matcher(document.select("p.edition-issues").text());
            return totalIssues.find() ? (int) Math.ceil(Double.parseDouble(totalIssues.group(0)) / 48) : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info(String.format("Reading %s, page %d", key, index));
            String page = "?page=" + index;
            Document doc = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + url + page).get();
            Elements issues = doc.select("ul.v2-cover-list li");
            return issues
                    .stream()
                    .map(issue -> ItemDTO
                            .builder()
                            .titleId(titleId)
                            .link(issue.select("a").attr("href"))
                            .name(issue.select("a").attr("title"))
                            .number(Integer.parseInt(issue.select(".issue-number").text().substring(1)))
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

    private ItemDTO buildDetails(ItemDTO item) {
        log.info(String.format("Reading %s", item.getName()));
        try {
            Document document = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + item.getLink()).get();
            Element element = document.select(".b-info").get(0);

            item.setNumber(this.getNumber(element));
            item.setName(this.getName(element, item.getNumber()));
            item.setShortDescription(this.getShortDescription(document));

            item.setCover(download
                    ? this.getCover(item.getNumber().toString(), key, path, folder, element.select("p.comic-cover a.fancybox").attr("href"))
                    : ReaderConstants.DEFAULT_EMPTY);
            Elements elems = document.select(".info .content .info-item");

            item.setDate(!elems.isEmpty() && !elems.get(0).select("p").attr("content").equals("")
                    ? elems.get(0).select("p").attr("content")
                    : ReaderConstants.DEFAULT_DATE);

            item.setIsbn(elems.size() >= 2
                    ? elems.get(1).select("ul li").text()
                    : ReaderConstants.DEFAULT_ISBN);
        } catch (Exception e) {
            log.error(String.format("An issue happened at the moment to read the HTML document %s", e.getMessage()));
        }
        return item;
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
