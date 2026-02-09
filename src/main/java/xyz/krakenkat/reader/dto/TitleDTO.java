package xyz.krakenkat.reader.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TitleDTO {
    private String name;
    private String key;
    private String cover;
    private String format;
    private String demography;
    private String type;
    private String frequency;
    private String status;
    private String totalIssues;
    private String releaseDate;
    private List<String> genres;
    private List<AuthorDTO> authors;
    private List<ItemDTO> issues;
}
