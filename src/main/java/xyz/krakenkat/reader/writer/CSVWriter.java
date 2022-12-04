package xyz.krakenkat.reader.writer;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.util.ReaderConstants;

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


    public CSVWriter() {
        this.key = ResourceBundle.getBundle("application").getString("reader.key");
        this.outputPath = ResourceBundle.getBundle("application").getString("reader.csv.output-file");
    }

    public void setKey(String key) { this.key = key; }

    public void writeCSV(List<ItemDTO> itemDTOS) {
        log.info(String.format("Starting to build CSV %s%s", key, ReaderConstants.OUTPUT_FORMAT));
        try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputPath + key + ReaderConstants.OUTPUT_FORMAT))) {
            CSVFormat csvFormat = CSVFormat
                    .Builder
                    .create()
                    .setHeader(ReaderConstants.HEADERS)
                    .setDelimiter(ReaderConstants.DELIMITER)
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
                            itemDTO.getVariant()
                    );
                    csvPrinter.flush();
                }
                log.info(String.format("The CSV file is located in: %s%s%s", outputPath, key, ReaderConstants.OUTPUT_FORMAT));
            }
        } catch (IOException e) {
            log.error(String.format("There was an error at the moment to build the CSV file %s", e.getMessage()));
        }
    }
}
