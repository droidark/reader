package xyz.krakenkat.reader.lector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static xyz.krakenkat.reader.util.Utilities.saveCover;

public interface Lector {
    Logger log = LoggerFactory.getLogger(Lector.class);

    String getKey();

    void setKey(String key);

    String getUrl();

    void setUrl(String url);

    String getTitleId();

    void setTitleId(String titleId);

    String getPath();

    void setPath(String path);

    String getFolder();

    void setFolder(String folder);

    boolean isDownload();

    void setDownload(boolean download);

    Integer getTotalPages();

    List<ItemDTO> getSinglePage(Integer index);

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
        log.info("Getting item details to compare with the database list");
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

    default String getCover(int number, String key, String path, String folder, String url) {
        return isDownload() ? saveCover(number, key, path, folder, url) : ReaderConstants.DEFAULT_EMPTY;
    }

    default String getCover() { return ReaderConstants.DEFAULT_EMPTY; }
}
