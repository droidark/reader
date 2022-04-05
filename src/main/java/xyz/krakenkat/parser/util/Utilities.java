package xyz.krakenkat.parser.util;

import xyz.krakenkat.parser.dto.ItemDTO;

import java.util.List;
import java.util.stream.Collectors;

public class Utilities {

    public static ItemDTO findItemByNumber(List<ItemDTO> items, Integer number) {
        return items.stream().filter(item -> item.getNumber() == number).findFirst().orElse(null);
    }

    public static List<ItemDTO> joinLists(List<ItemDTO> whakoomList, List<ItemDTO> publisherList) {
        return whakoomList.stream().map(itemDTO -> {
            ItemDTO item = Utilities.findItemByNumber(publisherList, itemDTO.getNumber());
            if (item != null) {
                itemDTO.setPages(item.getPages());
                itemDTO.setPrice(item.getPrice());
                if (itemDTO.getIsbn().equals("000-0000000000") && !item.getIsbn().equals("000-0000000000")) {
                    itemDTO.setIsbn(item.getIsbn());
                }
                if ((itemDTO.getShortDescription().equals("-") || itemDTO.getShortDescription().equals("")) && !item.getShortDescription().equals("-")) {
                    itemDTO.setShortDescription(item.getShortDescription());
                }
            } else {
                itemDTO.setPages(192);
                itemDTO.setPrice(139.00);
            }
            return itemDTO;
        }).collect(Collectors.toList());
    }
}
