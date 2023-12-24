package xyz.krakenkat.reader.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemDTOTest {

    private ItemDTO itemDTO;

    @BeforeEach
    void setUp() {
        itemDTO = new ItemDTO();
    }

    @Test
    void testITemDtoGettersAndSetters() {
        // Given
        String titleId = "title-id";
        String name = "name";
        String link = "link";
        Integer number = 10;
        Integer pages = 196;
        Double price = 159.0;
        String cover = "cover";
        String shortDescription = "short-description";
        Integer edition = 1;
        Boolean variant = Boolean.FALSE;
        String date = "2023-11-12";
        String isbn = "000-0000000000";
        String currency = "MXN";

        // When
        itemDTO.setTitleId(titleId);
        itemDTO.setName(name);
        itemDTO.setLink(link);
        itemDTO.setNumber(number);
        itemDTO.setPages(pages);
        itemDTO.setPrice(price);
        itemDTO.setCover(cover);
        itemDTO.setShortDescription(shortDescription);
        itemDTO.setEdition(edition);
        itemDTO.setVariant(variant);
        itemDTO.setDate(date);
        itemDTO.setIsbn(isbn);
        itemDTO.setCurrency(currency);

        // Then
        assertEquals(titleId, itemDTO.getTitleId());
        assertEquals(name, itemDTO.getName());
        assertEquals(link, itemDTO.getLink());
        assertEquals(number, itemDTO.getNumber());
        assertEquals(pages, itemDTO.getPages());
        assertEquals(price, itemDTO.getPrice());
        assertEquals(cover, itemDTO.getCover());
        assertEquals(shortDescription, itemDTO.getShortDescription());
        assertEquals(edition, itemDTO.getEdition());
        assertEquals(variant, itemDTO.getVariant());
        assertEquals(date, itemDTO.getDate());
        assertEquals(isbn, itemDTO.getIsbn());
        assertEquals(currency, itemDTO.getCurrency());
    }
}