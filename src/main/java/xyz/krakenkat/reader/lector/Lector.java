package xyz.krakenkat.reader.lector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface Lector {
    Logger log = LoggerFactory.getLogger(Lector.class);

    String getKey();

    void setKey(String key);

    String getUrl();

    void setUrl(String url);

    String getTitleId();

    void setTitleId(String titleId);

    Integer getTotalPages();

    List<ItemDTO> getSinglePage(Integer index);

    String getPath();

    void setPath(String path);

    String getFolder();

    void setFolder(String folder);

    boolean isDownload();
    void setDownload(boolean download);

    ItemDTO buildDetails(ItemDTO item);

    default List<ItemDTO> getDetails() {
        log.info("Getting item details");
        return getIssues()
                .stream()
                .map(this::buildDetails)
                .sorted(Comparator.comparing(ItemDTO::getNumber))
                .toList();
    }
    default List<ItemDTO> getDetails(List<ItemDTO> databaseList) {
        log.info("Getting item details");
        return getIssues()
                .stream()
                .filter(item -> !databaseList.contains(item))
                .map(this::buildDetails)
                .sorted(Comparator.comparing(ItemDTO::getNumber))
                .toList();
    }

    default List<ItemDTO> getIssues() {
        int pages = this.getTotalPages();
        List<ItemDTO> items = new ArrayList<>();
        for(int i = 1; i <= pages; i++) {
            items.addAll(this.getSinglePage(i));
        }
        return items;
    }

    /*
    * [
    *   0: number
    *   1: key
    *   2: path: 'path to save image'      -> /d/Documents/imageCollector/
    *   3: folder: 'publisher-folder-name' -> panini-manga-mx
    *   4: url: where-is-located-the-image -> https://...
    * ]
    * */
    default String saveCover(String... imageParams) {
        String num = imageParams[0].length() == 1 ? "0" + imageParams[0] : imageParams[0];
        String imagePath = imageParams[2]
                + imageParams[3]
                + File.separator
                + imageParams[1]
                + File.separator
                + imageParams[1]
                + ReaderConstants.DEFAULT_EMPTY
                + num
                + ".jpg";

        File directory = new File(imageParams[2] + imageParams[3] + File.separator + imageParams[1]);
        if (!directory.exists())
            directory.mkdir();

        File originalFile = new File(imagePath);
        if (!originalFile.exists()) {
            try (InputStream in = new URL(imageParams[4]).openStream()) {
                Files.copy(in, Paths.get(imagePath));
                log.info(String.format("The image %s-%s.jpg was saved in the selected folder", imageParams[1], num));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "/" + imageParams[3] + "/" + imageParams[1] + "/" + imageParams[1] + "-" + num + ".jpg";
    }

    default String getCover(String... params) {
        return isDownload() ? this.saveCover(params) : ReaderConstants.DEFAULT_EMPTY;
    }

    default String getCover() { return ReaderConstants.DEFAULT_EMPTY; }
}
