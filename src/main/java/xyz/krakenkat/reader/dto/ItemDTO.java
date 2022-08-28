package xyz.krakenkat.reader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDTO {
    private String name;
    private String link;
    private int number;
    private Integer pages;
    private double price;
    private String cover;
    private String shortDescription;
    private int edition;
    private boolean variant;
    private String date;
    private String isbn;
    private String currency;
}