package xyz.krakenkat.reader;

import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.lector.*;
import xyz.krakenkat.reader.util.Utilities;
import xyz.krakenkat.reader.writer.CSVWriter;

import java.util.List;

public class RunApp {
    public static void main(String[] args) {

        Lector whakoomLector = new WhakoomLector();
//        Reader paniniReader = new PaniniReader();
//        Reader kamiteReader = new KamiteReader();
        Lector paniniLectorV2 = new PaniniLectorV2();

//
        List<ItemDTO> whakoomItems = whakoomLector.getDetails(whakoomLector.getIssues());
        List<ItemDTO> paniniV2Items = paniniLectorV2.getDetails(paniniLectorV2.getIssues());
//        List<ItemDTO> paniniItems = paniniReader.getDetails(paniniReader.getIssues());
//        List<ItemDTO> kamiteItems = kamiteReader.getDetails(kamiteReader.getIssues());
//
//        //List<ItemDTO> whakoomItems = paniniItems;
//        List<ItemDTO> whakoomItems = kamiteItems;
//        //List<ItemDTO> paniniItems = whakoomItems;
//        List<ItemDTO> kamiteItems = whakoomItems;
//        List<ItemDTO> paniniV2Items = whakoomItems;
//
//        List<ItemDTO> finalList =  Utilities.joinLists(whakoomItems, paniniItems);
//        List<ItemDTO> finalList =  Utilities.joinLists(whakoomItems, kamiteItems);
        List<ItemDTO> finalList =  Utilities.joinLists(whakoomItems, paniniV2Items);

        CSVWriter csvWriter = new CSVWriter();
        csvWriter.writeCSV(finalList);
    }
}
