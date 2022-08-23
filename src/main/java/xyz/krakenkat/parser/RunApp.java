package xyz.krakenkat.parser;

import xyz.krakenkat.parser.dto.ItemDTO;
import xyz.krakenkat.parser.reader.*;
import xyz.krakenkat.parser.util.Utilities;
import xyz.krakenkat.parser.writer.CSVWriter;

import java.util.List;

public class RunApp {
    public static void main(String[] args) {

        Reader whakoomReader = new WhakoomReader();
//        Reader paniniReader = new PaniniReader();
//        Reader kamiteReader = new KamiteReader();
        Reader paniniReaderV2 = new PaniniReaderV2();

//
        List<ItemDTO> whakoomItems = whakoomReader.getDetails(whakoomReader.getIssues());
        List<ItemDTO> paniniV2Items = paniniReaderV2.getDetails(paniniReaderV2.getIssues());
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
