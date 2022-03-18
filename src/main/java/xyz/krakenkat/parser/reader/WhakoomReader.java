package xyz.krakenkat.parser.reader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import xyz.krakenkat.parser.dto.ItemDTO;

import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class WhakoomReader implements Reader {

    private String KEY;
    private String URL;
    private String TITLE;
    private String PATH;
    private String FOLDER = "panini-manga-mx";

    public WhakoomReader() {
        ResourceBundle rb = ResourceBundle.getBundle("system");
        this.KEY = rb.getString("key");
        this.URL = rb.getString("whakoom-url");
        this.TITLE = rb.getString("whakoom-title");
        this.PATH = rb.getString("images-path");
        this.FOLDER = rb.getString("images-folder");
    }

    @Override
    public List<ItemDTO> getIssues() {
        try {
            Document doc = Jsoup.connect(URL + TITLE).get();
            Elements issues = doc.select("ul.v2-cover-list li");
            return issues
                    .stream()
                    .map(issue -> ItemDTO
                            .builder()
                            .link(issue.select("a").attr("href"))
                            .currency("MXN")
                            .edition(1)
                            .variant(false)
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ItemDTO> getDetails(List<ItemDTO> items) {
        return items.stream().map(item -> {
            try {
                Document document = Jsoup.connect(URL + item.getLink()).get();

                Elements elements = document.select(".b-info");
                item.setNumber(Integer.parseInt(elements.get(0).select("h1 strong").text().substring(1)));
                item.setName(elements.get(0).select("span").text().replace("&nbps;", "").trim() + " #" + item.getNumber());

                String imageUrl = elements.get(0).select("p.comic-cover a.fancybox").attr("href");
                item.setCover(this.saveCover(imageUrl, KEY, PATH, FOLDER, item.getNumber()));

                Elements info = document.select(".wiki .wiki-content .wiki-text p");
                item.setShortDescription(info.size() >= 1 ? info.get(0).text().trim() : "-");

                Elements elems = document.select(".info .content .info-item");

                item.setDate(elems.size() >= 1 ? elems.get(0).select("p").attr("content") : "0000-00-00");
                item.setIsbn(elems.size() >= 2 ? elems.get(1).select("ul li").text() : "000-0000000000");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return item;
        }).collect(Collectors.toList());
    }
}
