package xyz.krakenkat.parser.reader;

import xyz.krakenkat.parser.dto.ItemDTO;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public interface Reader {
    List<ItemDTO> getIssues();
    List<ItemDTO> getDetails(List<ItemDTO> items);

    default String saveCover(String url, String key, String path, String folder, Integer number) throws MalformedURLException {
        String num = number < 10 ? "0" + number : number.toString();
        String imagePath = path + key + "\\" + key  + "-" + num + ".jpg";
        try (InputStream in = new URL(url).openStream()) {
            Files.copy(in, Paths.get(imagePath));
            return "/" + folder + "/" + key + "/" + key + "-" + num + ".jpg";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
