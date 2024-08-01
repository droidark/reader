package xyz.krakenkat.reader.builder;

import xyz.krakenkat.reader.dto.ItemDTO;

import java.util.List;
import java.util.Map;

import static xyz.krakenkat.reader.util.ReaderConstants.MXN_CURRENCY;

public class ObjectsBuilder {

    private static final Map<String, String> SUFFIXES = Map.of(
            "1", "one",
            "2", "two",
            "3", "three",
            "4", "four",
            "5", "five",
            "6", "six",
            "7", "seven",
            "8", "eight",
            "9", "nine",
            "10", "ten");

    public static ItemDTO buildItemDTO() {
        return ItemDTO
                .builder()
                .titleId("title-id")
                .name("name")
                .link("link")
                .number(1)
                .pages(196)
                .price(169.0)
                .cover("cover")
                .shortDescription("shortDescription")
                .edition(1)
                .variant(Boolean.FALSE)
                .date("2022-12-25")
                .isbn("978-3-16-148410-0")
                .currency("MXN")
                .build();
    }

    public static ItemDTO buildItemDTO(String suffix) {
        return ItemDTO
                .builder()
                .titleId("title-id-" + SUFFIXES.get(suffix))
                .name("name-" + SUFFIXES.get(suffix))
                .link("link-" + SUFFIXES.get(suffix))
                .number(Integer.parseInt(suffix))
                .pages(196)
                .price(169.0)
                .cover("cover-" + SUFFIXES.get(suffix))
                .shortDescription("shortDescription-" + SUFFIXES.get(suffix))
                .edition(1)
                .variant(Boolean.FALSE)
                .date("2022-12-25")
                .isbn("978-3-16-148410-" + suffix)
                .currency(MXN_CURRENCY)
                .build();
    }

    public static List<ItemDTO> buildSequenceItemDTOList() {
        return SUFFIXES.keySet().stream().map(ObjectsBuilder::buildItemDTO).toList();
    }

    private ObjectsBuilder() {}
}
