package xyz.krakenkat.parser;

import xyz.krakenkat.parser.dto.ItemDTO;
import xyz.krakenkat.parser.reader.KamiteReader;
import xyz.krakenkat.parser.reader.PaniniReader;
import xyz.krakenkat.parser.reader.Reader;
import xyz.krakenkat.parser.reader.WhakoomReader;
import xyz.krakenkat.parser.util.Utilities;
import xyz.krakenkat.parser.writer.CSVWriter;

import java.util.List;

public class RunApp {
    public static void main(String[] args) {

        Reader whakoomReader = new WhakoomReader();
//        Reader paniniReader = new PaniniReader();
        Reader kamiteReader = new KamiteReader();

//
        List<ItemDTO> whakoomItems = whakoomReader.getDetails(whakoomReader.getIssues());
//        List<ItemDTO> paniniItems = paniniReader.getDetails(paniniReader.getIssues());
        List<ItemDTO> kamiteItems = kamiteReader.getDetails(kamiteReader.getIssues());
//
//        //List<ItemDTO> whakoomItems = paniniItems;
//        //List<ItemDTO> paniniItems = whakoomItems;
//
//        List<ItemDTO> finalList =  Utilities.joinLists(whakoomItems, paniniItems);
        List<ItemDTO> finalList =  Utilities.joinLists(whakoomItems, kamiteItems);

        CSVWriter csvWriter = new CSVWriter();
        csvWriter.writeCSV(finalList);
    }
}
