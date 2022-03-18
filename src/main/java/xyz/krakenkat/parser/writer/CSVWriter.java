package xyz.krakenkat.parser.writer;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import xyz.krakenkat.parser.dto.ItemDTO;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

public class CSVWriter {

    private String KEY;
    private String OUTPUT_PATH;
    private static final String OUTPUT_FORMAT = ".csv";
    private static final String DELIMITER = "|";
    private static final String[] HEADERS = {"NAME",
            "KEY",
            "NUMBER",
            "COVER",
            "PAGES",
            "PRINTED_PRICE",
            "CURRENCY",
            "RELEASE_DATE",
            "SHORT_REVIEW",
            "ISBN10",
            "EDITION",
            "VARIANT"};

    public CSVWriter() {
        ResourceBundle rb = ResourceBundle.getBundle("system");
        this.KEY = rb.getString("key");
        this.OUTPUT_PATH = rb.getString("csv-output-path");
    }

    public void writeCSV(List<ItemDTO> itemDTOS) {
        try {
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(OUTPUT_PATH + KEY + OUTPUT_FORMAT));
            CSVFormat csvFormat = CSVFormat
                    .Builder
                    .create()
                    .setHeader(HEADERS)
                    .setDelimiter(DELIMITER)
                    .build();

            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            for(ItemDTO itemDTO : itemDTOS) {
                csvPrinter.printRecord(
                        itemDTO.getName(),
                        itemDTO.getNumber(),
                        itemDTO.getNumber(),
                        itemDTO.getCover(),
                        itemDTO.getPages(),
                        itemDTO.getPrice(),
                        itemDTO.getCurrency(),
                        itemDTO.getDate(),
                        itemDTO.getShortDescription(),
                        itemDTO.getIsbn(),
                        itemDTO.getEdition(),
                        itemDTO.isVariant()
                );

                csvPrinter.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
