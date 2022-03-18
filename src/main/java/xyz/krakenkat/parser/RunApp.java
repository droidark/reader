package xyz.krakenkat.parser;

import xyz.krakenkat.parser.dto.ItemDTO;
import xyz.krakenkat.parser.reader.PaniniReader;
import xyz.krakenkat.parser.reader.Reader;
import xyz.krakenkat.parser.reader.WhakoomReader;
import xyz.krakenkat.parser.util.Utilities;
import xyz.krakenkat.parser.writer.CSVWriter;

import java.util.List;

public class RunApp {
    public static void main(String[] args) {
        Reader whakoomReader = new WhakoomReader();
        Reader paniniReader = new PaniniReader();

        List<ItemDTO> whakoomItems = whakoomReader.getDetails(whakoomReader.getIssues());
        List<ItemDTO> paniniItems = paniniReader.getDetails(paniniReader.getIssues());

        List<ItemDTO> finalList =  Utilities.joinLists(whakoomItems, paniniItems);

        CSVWriter csvWriter = new CSVWriter();
        csvWriter.writeCSV(finalList);
    }
}
