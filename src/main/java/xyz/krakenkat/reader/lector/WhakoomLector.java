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
public class WhakoomLector implements Lector {

    private String titleId;
    private String key;
    private String url;
    private String path;
    private String folder;
    private boolean download = false;

    public Integer getTotalPages() {
        try {
            log.info("Getting total pages from {}{}", ReaderConstants.WHAKOOM_BASE_URL, url);
            Document document = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + url).get();
            Matcher totalIssues = ReaderConstants.WHAKOOM_TOTAL_ISSUES_PATTERN.matcher(document.select("p.edition-issues").text());
            return totalIssues.find() ? (int) Math.ceil(Double.parseDouble(totalIssues.group(0)) / 48) : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info("Reading {}, page {}", key, index);
            String page = "?page=" + index;
            Document doc = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + url + page).get();
            Elements issues = doc.select("ul.v2-cover-list.auto-rows.same-edition li:not(.not-published)");
            return issues
                    .stream()
                    .map(issue -> ItemDTO
                            .builder()
                            .titleId(titleId)
                            .link(issue.select("a").attr("href"))
                            .name(issue.select("a").attr("title"))
                            .number(this.getNumber(issue))
                            .currency(ReaderConstants.MXN_CURRENCY)
                            .edition(1)
                            .variant(Boolean.FALSE)
                            .build())
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public ItemDTO buildDetails(ItemDTO item) {
        log.info("Reading {}", item.getName());
        try {
            Document document = Jsoup.connect(ReaderConstants.WHAKOOM_BASE_URL + item.getLink()).get();
            Element element = document.select(".b-info").get(0);

            item.setName(this.getName(element, item.getNumber()));
            item.setShortDescription(this.getShortDescription(document));

            item.setCover(download
                    ? this.getCover(item.getNumber().toString(), key, path, folder, element.select("p.comic-cover a.fancybox").attr("href"))
                    : ReaderConstants.DEFAULT_EMPTY);
            Elements elems = document.select(".info .content .info-item");

            item.setDate(!elems.isEmpty() && !elems.get(0).select("p").attr("content").isEmpty()
                    ? elems.get(0).select("p").attr("content")
                    : ReaderConstants.DEFAULT_DATE);

            item.setIsbn(elems.size() >= 2
                    ? elems.get(1).select("ul li").text()
                    : ReaderConstants.DEFAULT_ISBN);
        } catch (Exception e) {
            log.error("An issue happened at the moment to read the HTML document {}", e.getMessage());
        }
        return item;
    }

    private Integer getNumber(Element element) {
        String text = element.select(".issue-number").text();
        Matcher matcher = ReaderConstants.WHAKOOM_NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String getName(Element element, int number) {
        return element.select("span").text().replace("&nbps;", "").trim() + " #" + number;
    }

    private String getShortDescription(Element element) {
        Elements info = element.select(".wiki .wiki-content .wiki-text p");
        return !info.isEmpty() ? info.get(0).text().trim() : ReaderConstants.DEFAULT_EMPTY;
    }
}
