package xyz.krakenkat.reader.util;

import xyz.krakenkat.reader.dto.ItemDTO;

import java.util.List;
import java.util.Objects;

import static xyz.krakenkat.reader.util.ReaderConstants.*;

public class Utilities {
    private Utilities() {
    }

    public static ItemDTO findItemByNumber(List<ItemDTO> items, Integer number) {
        return items.stream().filter(item -> Objects.equals(item.getNumber(), number)).findFirst().orElse(null);
    }

    public static List<ItemDTO> joinLists(List<ItemDTO> whakoomList, List<ItemDTO> publisherList) {
        return whakoomList.stream().peek(itemDTO -> {
            ItemDTO item = Utilities.findItemByNumber(publisherList, itemDTO.getNumber());
            if (item != null) {
                itemDTO.setPages(item.getPages());
                itemDTO.setPrice(item.getPrice());
                if (itemDTO.getIsbn().equals(DEFAULT_ISBN) && !item.getIsbn().equals(DEFAULT_ISBN)) {
                    itemDTO.setIsbn(item.getIsbn());
                }
                if ((itemDTO.getShortDescription().equals("-") || itemDTO.getShortDescription().isEmpty()) && !item.getShortDescription().equals("-")) {
                    itemDTO.setShortDescription(item.getShortDescription());
                }
                if (itemDTO.getDate().isEmpty() && !item.getDate().isEmpty()) {
                    itemDTO.setDate(item.getDate());
                }
            } else {
                itemDTO.setPages(DEFAULT_PAGES);
                itemDTO.setPrice(DEFAULT_PRICE);
            }
        }).toList();
    }
}
