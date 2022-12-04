package xyz.krakenkat.reader.dto;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ItemDTO {
    private String titleId;
    private String name;
    private String link;
    private Integer number;
    private Integer pages;
    private Double price;
    private String cover;
    private String shortDescription;
    private Integer edition;
    private Boolean variant;
    private String date;
    private String isbn;
    private String currency;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemDTO itemDTO = (ItemDTO) o;
        return Objects.equals(titleId, itemDTO.titleId) && Objects.equals(number, itemDTO.number) && Objects.equals(variant, itemDTO.variant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titleId, number, variant);
    }
}