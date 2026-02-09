package xyz.krakenkat.reader.title.impl;

import lombok.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;

import static xyz.krakenkat.reader.util.ReaderConstants.*;
import static xyz.krakenkat.reader.util.Utilities.buildKey;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Slf4j
public class WikipediaLector implements TitleLector {

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
            titleDTO.setType(buildType());
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
        return element.select("td.infobox-subheader").getFirst().text().trim();
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
                .select("th.infobox-label")
                .select(":contains(Demographic)")
                .next()
                .getFirst()
                .select("td.infobox-data span i a")
                .text()
                .trim()
                .replace("ō", "o")
                .toLowerCase();
    }

    private String buildType() {
        return DEFAULT_EMPTY;
    }

    private String buildStatus(Element element) {
        boolean ongoing = element
                .select("th.infobox-label")
                .select(":contains(Original run)")
                .next()
                .getFirst()
                .select("td.infobox-data")
                .text().contains("present");

        return STATUS.get(ongoing ? "Publishing" : "Finished");
    }

    private String buildTotalIssues(Element element) {
        String volumes = Optional.of(Objects.requireNonNull(element
                                .select("th.infobox-label")
                                .select(":contains(Volumes)")
                                .next()
                                .first())
                .select("td.infobox-data").text())
                .orElse("");

        Matcher matcher = TITLE_VOLUMES_PATTERN.matcher(volumes);
        return matcher.find() ? matcher.group(0).trim() : "1";
    }

    private List<String> buildGenres(Element element) {
        return element
                .select("th.infobox-label")
                .select(":contains(Genre)")
                .next()
                .select("td.infobox-data .plainlist ul li a")
                .stream()
                .map(Element::text)
                .filter(text -> !text.contains("["))
                .toList();
    }

    private List<AuthorDTO> buildAuthors(Element element) {
        List<AuthorDTO> authors = new ArrayList<>();

        String creator = getAuthor(element, "Created by");
        String writer = getAuthor(element, "Written by");
        String illustrator = getAuthor(element, "Illustrated by");

        log.info(">>>>> Creator: {}, Writer: {}, Illustrator: {}", creator, writer, illustrator);


        if (!creator.isEmpty()) {
            authors.add(AuthorDTO
                    .builder()
                    .name(creator)
                    .roles(List.of(TITLE_CREATOR))
                    .build());
            if (!writer.isEmpty()) {
                authors.add(AuthorDTO
                        .builder()
                        .name(writer)
                        .roles(List.of(ROLES.get("Story")))
                        .build());
            }
            if (!illustrator.isEmpty()) {
                authors.add(AuthorDTO
                        .builder()
                        .name(writer)
                        .roles(List.of(ROLES.get("Art")))
                        .build());
            }
        } else {
            if (illustrator.isEmpty()) {
                authors.add(AuthorDTO
                        .builder()
                        .name(creator)
                        .roles(List.of(TITLE_CREATOR))
                        .build());
            } else {
                authors.add(AuthorDTO
                        .builder()
                        .name(writer)
                        .roles(List.of(ROLES.get("Story")))
                        .build());
                authors.add(AuthorDTO
                        .builder()
                        .name(illustrator)
                        .roles(List.of(ROLES.get("Art")))
                        .build());
            }
        }
        return authors;
    }



    private String getAuthor(Element element, String role) {
        Element author = element
                        .select("th.infobox-label")
                        .select(":contains(" + role + ")")
                        .next()
                        .first();

        return author != null ? author.text() : "";
    }
}
