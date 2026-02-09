package xyz.krakenkat.reader.title.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import xyz.krakenkat.reader.constant.Format;
import xyz.krakenkat.reader.constant.Frequency;
import xyz.krakenkat.reader.dto.AuthorDTO;
import xyz.krakenkat.reader.dto.TitleDTO;
import xyz.krakenkat.reader.title.TitleLector;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;

import static xyz.krakenkat.reader.util.ReaderConstants.*;
import static xyz.krakenkat.reader.util.Utilities.buildKey;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Slf4j
public class MyAnimeListLector implements TitleLector {

    private String url;
    private Format format;
    private Frequency frequency;
    private String releaseDate;

    @Override
    public TitleDTO buildTitle() {

        TitleDTO titleDTO = new TitleDTO();

        try {
            Document document = Jsoup.connect(url).get();

            titleDTO.setName(getName(document));
            titleDTO.setKey(buildKey(titleDTO.getName()));
            titleDTO.setCover(getCover());
            titleDTO.setFormat(getFormat(format));
            titleDTO.setFrequency(getFrequency(frequency));
            titleDTO.setDemography(buildDemography(document));
            titleDTO.setType(buildType(document));
            titleDTO.setStatus(buildStatus(document));
            titleDTO.setTotalIssues(buildTotalIssues(document));
            titleDTO.setGenres(buildGenres(document));
            titleDTO.setAuthors(buildAuthors(document));
            titleDTO.setReleaseDate(releaseDate);
        } catch (IOException e) {
            log.info("There was an error processing the page: {}", e.getMessage());
        }
        return titleDTO;
    }

    private String getName(Element element) {
        String name = element.select("#contentWrapper h1.h1 span.h1-title span[itemprop='name'] span.title-english").text().trim();
        return name.isEmpty() ? element.select("#contentWrapper h1.h1 span.h1-title span[itemprop='name']").text().trim() : name;
    }

    private String getCover() {
        return DEFAULT_EMPTY;
    }

    private String getFormat(Format format) {
        return format.toString().toLowerCase();
    }

    private String getFrequency(Frequency frequency) {
        return frequency.toString().toLowerCase();
    }

    private String buildDemography(Element element) {
        return element
                .select(".leftside .spaceit_pad")
                .not(".js-statistics-info")
                .select(":contains(Demographic:)")
                .getFirst()
                .select("span[itemprop='genre']")
                .text()
                .trim()
                .toLowerCase();
    }

    private String buildType(Element element) {
        return element
                .select(".leftside .spaceit_pad")
                .not(".js-statistics-info")
                .select(":contains(Type:)")
                .getFirst()
                .select("a")
                .text()
                .trim()
                .toLowerCase();
    }

    private String buildStatus(Element element) {
        String status = element
                .select(".leftside .spaceit_pad")
                .not(".js-statistics-info")
                .select(":contains(Status:)")
                .getFirst()
                .text()
                .substring(8)
                .trim();

        return STATUS.get(status);
    }

    private String buildTotalIssues(Element element) {
        String totalIssues = element
                .select(".leftside .spaceit_pad")
                .not(".js-statistics-info")
                .select(":contains(Volumes:)")
                .getFirst()
                .text()
                .substring(8)
                .trim();

        return totalIssues.equals("Unknown") ? "1" : totalIssues;
    }

    private List<String> buildGenres(Element element) {
        return element
                .select(".leftside .spaceit_pad")
                .not(".js-statistics-info")
                .select(":contains(Genres:)")
                .getFirst()
                .select("span[itemprop='genre']")
                .stream()
                .map(g -> g.text().trim())
                .toList();
    }

    private List<AuthorDTO> buildAuthors(Element element) {
        String authorsString = element
                .select(".leftside .spaceit_pad")
                .not(".js-statistics-info")
                .select(":contains(Authors:)")
                .getFirst()
                .text()
                .substring(9)
                .replace("), ", ");");

        return buildAuthorList(authorsString);
    }

    private List<AuthorDTO> buildAuthorList(String authorsString) {
        String[] split = authorsString.split(";");
        return split.length == 1
                ? List.of(AuthorDTO.builder().name(buildAuthorName(split[0].trim())).roles(List.of(TITLE_CREATOR)).build())
                : Arrays.stream(split).map(this::buildAuthor).toList();
    }

    private AuthorDTO buildAuthor(String author) {
        Matcher matcher = TITLE_ROLE_PATTERN.matcher(author);
        return matcher.find()
                ? AuthorDTO.builder().name(buildAuthorName(author)).roles(buildRoles(matcher.group(1))).build()
                : AuthorDTO.builder().build();
    }

    private String buildAuthorName(String author) {
        String[] authorFullName = author.replaceAll(TITLE_CLEAN_AUTHORS_REPLACEMENT, "").trim().split(",");
        return String.format("%s %s", authorFullName[1].trim(), authorFullName[0].trim());
    }

    private List<String> buildRoles(String rolessString) {
        String[] roles = rolessString.split("&");
        return Arrays.stream(roles).map(r -> ROLES.get(r.trim())).toList();
    }
}
