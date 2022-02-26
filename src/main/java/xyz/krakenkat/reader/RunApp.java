package xyz.krakenkat.reader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import xyz.krakenkat.reader.dto.ItemDTO;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunApp {

    private static final String KEY = "defense-devil";
    private static final Pattern PATTERN = Pattern.compile("\\d{1,3}$");

    public static void main(String[] args) {
        List<ItemDTO> itemDTOS = new ArrayList<>();
        String url = "https://www.tiendapanini.com.mx/mexico/soluciones/busqueda.aspx?t=defense%20devil&o=7";
        try {
            Document doc = Jsoup.connect(url).get();
            Elements rows = doc.select(".row");
            Element row = rows.get(3);

            Elements items = row.select(".item");

            for(Element item : items) {
                boolean isRegular = item.select("p.little-desc a").text().equals("");
                if(isRegular) {
                    ItemDTO itemDTO = ItemDTO
                            .builder()
                            .name(item.select(".description h4 a").text())
                            .price(item.select("p.price").text())
                            .build();

                    Document issue = Jsoup.connect(item.select(".description h4 a").attr("href")).get();
                    Elements description = issue.select("#content .details-description p");

                    itemDTO.setShortDescription(description.get(0).text());

                    // GET NUMBER
                    Matcher m = PATTERN.matcher(itemDTO.getName());
                    if(m.find()) {
                        itemDTO.setNumber(Integer.parseInt(m.group(0)));
                    }

                    // GET ADDITIONAL INFO
                    Elements additionalInfo = issue.select("#more-infos .container ul.list-group");
                    System.out.println(additionalInfo);

//                    // GET IMAGE
//                    Elements imageLink = issue.select("#sync1 .item img");
//                    Element image = imageLink.get(0);
//
//                    // SAVE IMAGE
//                    try(InputStream in =  new URL(image.attr("bigsrc")).openStream()) {
//                        String number = itemDTO.getNumber() < 10 ? "0" + itemDTO.getNumber().toString() : itemDTO.getNumber().toString();
//                        Files.copy(in, Paths.get("D:\\Documents\\imagesCollector\\panini-manga-mx\\" + KEY + "\\" + KEY + "-" + number + ".jpg"));
//                    }
                }
            }
            //System.out.println(itemDTOS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
