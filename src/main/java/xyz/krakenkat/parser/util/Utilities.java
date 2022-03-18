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
            itemDTO.setPages(item.getPages());
            itemDTO.setPrice(item.getPrice());
            return itemDTO;
        }).collect(Collectors.toList());
    }
}
