package xyz.krakenkat.reader.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.krakenkat.reader.dto.ItemDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static xyz.krakenkat.reader.util.ReaderConstants.*;

public class Utilities {

    public static final Logger log = LoggerFactory.getLogger(Utilities.class);

    public static ItemDTO findItemByNumber(List<ItemDTO> items, Integer number) {
        return items.stream().filter(item -> Objects.equals(item.getNumber(), number)).findFirst().orElse(null);
    }

    public static List<ItemDTO> joinLists(List<ItemDTO> whakoomList, List<ItemDTO> publisherList) {
        return whakoomList.stream().peek(itemDTO -> {
            ItemDTO item = findItemByNumber(publisherList, itemDTO.getNumber());
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

    public static String buildKey(String name) {
        return name.replace(":", "").replaceAll("\\s", "-").toLowerCase();
    }

    public static String saveCover(int number, String key, String path, String folder, String url) {
        String num = String.format("%02d", number);
        String fileName = String.format("%s-%s%s", key, num, DEFAULT_IMAGE_FORMAT);
        Path directoryPath = Paths.get(path, folder, key);
        Path imagePath = directoryPath.resolve(fileName);

        // Create the directory if it doesn't exist
        File directory = directoryPath.toFile();
        if (!directory.exists() && directory.mkdirs()) {
            log.info("The folder {} was created", key);
        }

        File imageFile = imagePath.toFile();
        if (!imageFile.exists()) {
            try {
                URI uri = URI.create(url);
                try (InputStream in = uri.toURL().openStream()) {
                    Files.copy(in, imagePath);
                    log.info("The image {} was saved in the selected folder", fileName);
                }
            } catch (IOException | IllegalArgumentException e) {
                log.error("There was an error: {}; while saving the image {}", e.getMessage(), fileName);
            }
        }

        return "/" + folder + "/" + key + "/" + fileName;
    }

    private Utilities() {
    }
}
