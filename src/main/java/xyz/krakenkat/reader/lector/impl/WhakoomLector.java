package xyz.krakenkat.reader.lector.impl;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.lector.Lector;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;

import static java.lang.Boolean.FALSE;
import static xyz.krakenkat.reader.util.ReaderConstants.*;

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
    public Integer getTotalPages() {
        try {
            log.info("Getting total pages from {}{}", WHAKOOM_BASE_URL, url);
            Document document = Jsoup.connect(WHAKOOM_BASE_URL + url).get();
            Matcher totalIssues = ReaderConstants.WHAKOOM_TOTAL_ISSUES_PATTERN.matcher(document.select("p.edition-issues").text());
            return totalIssues.find() ? (int) Math.ceil(Double.parseDouble(totalIssues.group(0)) / 48) : 1;
        } catch (IOException e) {
            log.info("There was an issue reading the document to calculate the total pages to parse. {}", e.getMessage());
            return 1;
        }
    }

    @Override
    public List<ItemDTO> getSinglePage(Integer index) {
        try {
            log.info("Reading {}, page {}", key, index);
            String page = "?page=" + index;
            Document doc = Jsoup.connect(WHAKOOM_BASE_URL + url + page).get();
            Elements elements = doc.select("ul.v2-cover-list.auto-rows.same-edition li:not(.not-published)");

            return elements
                    .stream()
                    .map(this::buildItem)
                    .toList();

        } catch (IOException e) {
            log.info("There was an issue reading the document. {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public ItemDTO buildDetails(ItemDTO item) {
        log.info("Reading details for {}", item.getName());

        try {
            Document document = Jsoup.connect(WHAKOOM_BASE_URL + item.getLink()).get();
            Element element = document.select(".b-info").getFirst();
            Elements elements = document.select(".info .content .info-item");

            item.setName(buildName(element, item.getNumber()));
            item.setShortDescription(buildShortDescription(document));
            item.setCover(buildCover(element, item.getNumber(), key, path, folder, download));
            item.setDate(buildDate(elements));
            item.setIsbn(buildISBN(elements));
        } catch (IOException e) {
            log.error("An issue happened at the moment to read the HTML document {}", e.getMessage());
        }
        return item;
    }

    private ItemDTO buildItem(Element element) {
        return ItemDTO
                .builder()
                .titleId(titleId)
                .name(buildName(element))
                .link(buildLink(element))
                .number(buildNumber(element))
                .currency(MXN_CURRENCY)
                .edition(DEFAULT_EDITION)
                .variant(FALSE)
                .build();
    }

    private String buildName(Element element) {
        return element.select("a").attr("title");
    }

    private String buildLink(Element element) {
        return element.select("a").attr("href");
    }

    private String buildName(Element element, int number) {
        return element.select("span").text().replace("&nbps;", "").trim() + " #" + number;
    }

    private Integer buildNumber(Element element) {
        String text = element.select(".issue-number").text();
        Matcher matcher = WHAKOOM_NUMBER_PATTERN.matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(0).trim()) : 1;
    }

    private String buildShortDescription(Element element) {
        Elements info = element.select(".wiki .wiki-content .wiki-text p");
        return !info.isEmpty() ? info.getFirst().text().trim() : DEFAULT_EMPTY;
    }

    private String buildCover(Element element, int number, String key, String path, String folder, boolean download) {
        String coverUrl = element.select("p.comic-cover a.fancybox").attr("href");
        return download ? getCover(number, key, path, folder, coverUrl) : DEFAULT_EMPTY;
    }

    private String buildDate(Elements elements) {
        return !elements.isEmpty() && !elements.getFirst().select("p").attr("content").isEmpty()
                ? elements.getFirst().select("p").attr("content")
                : DEFAULT_DATE;
    }

    private String buildISBN(Elements elements) {
        return elements.size() >= 2 ? elements.get(1).select("ul li").text() : DEFAULT_ISBN;
    }
}
