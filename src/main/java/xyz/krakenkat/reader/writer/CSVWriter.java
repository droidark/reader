package xyz.krakenkat.reader.writer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import xyz.krakenkat.reader.dto.ItemDTO;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
@AllArgsConstructor
public class CSVWriter {

    private String key;
    private String outputPath;
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
        this.key = ResourceBundle.getBundle("application").getString("reader.key");
        this.outputPath = ResourceBundle.getBundle("application").getString("reader.csv.output-file");
    }

    public void writeCSV(List<ItemDTO> itemDTOS) {
        log.info(String.format("Starting to build CSV %s%s", key, OUTPUT_FORMAT));
        try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath + key + OUTPUT_FORMAT))) {
            CSVFormat csvFormat = CSVFormat
                    .Builder
                    .create()
                    .setHeader(HEADERS)
                    .setDelimiter(DELIMITER)
                    .build();

            try (final CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {

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
                log.info(String.format("The CSV file is located in: %s%s%s", outputPath, key, OUTPUT_FORMAT));
            }
        } catch (IOException e) {
            log.error(String.format("There was an error at the moment to build the CSV file %s", e.getMessage()));
        }
    }
}
