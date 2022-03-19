package xyz.krakenkat.parser.reader;

import xyz.krakenkat.parser.dto.ItemDTO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface Reader {
    Integer getTotalPages();
    List<ItemDTO> getSinglePage(Integer index);
    List<ItemDTO> getDetails(List<ItemDTO> items);

    default List<ItemDTO> getIssues() {
        int pages = this.getTotalPages();
        List<ItemDTO> items = new ArrayList<>();
        for(int i = 1; i <= pages; i++) {
            items.addAll(this.getSinglePage(i));
        }
        return items
                .stream()
                .collect(Collectors.toList());
    }

    default String saveCover(String url, String key, String path, String folder, Integer number) throws MalformedURLException {
        String num = number < 10 ? "0" + number : number.toString();
        String imagePath = path + key + "\\" + key  + "-" + num + ".jpg";

        File directory = new File(path + key);
        if (!directory.exists()) {
            directory.mkdir();
        }

        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(imagePath));
            return "/" + folder + "/" + key + "/" + key + "-" + num + ".jpg";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
