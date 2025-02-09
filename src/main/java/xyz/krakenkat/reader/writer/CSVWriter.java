package xyz.krakenkat.reader.writer;

import lombok.AllArgsConstructor;
import lombok.Setter;
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

import static xyz.krakenkat.reader.util.ReaderConstants.*;

@Slf4j
@AllArgsConstructor
public class CSVWriter {

    @Setter
    private String key;
    private String outputPath;


    public CSVWriter() {
        this.key = ResourceBundle.getBundle("application").getString("reader.key");
        this.outputPath = ResourceBundle.getBundle("application").getString("reader.csv.output-file");
    }

    public void writeCSV(List<ItemDTO> itemDTOS) {
        log.info("Starting to build CSV {}{}", key, OUTPUT_FORMAT);
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
                            itemDTO.getTitleId(),
                            itemDTO.getName(),
                            itemDTO.getNumber(),
                            itemDTO.getNumber(),
                            itemDTO.getCover(),
                            itemDTO.getPages(),
                            itemDTO.getPrice(),
//                            DEFAULT_US_DIGITAL_PRICE,
                            itemDTO.getCurrency(),
                            itemDTO.getDate(),
                            itemDTO.getShortDescription(),
                            itemDTO.getIsbn(),
                            itemDTO.getEdition(),
                            itemDTO.getVariant()
                    );
                    csvPrinter.flush();
                }
                log.info("The CSV file is located in: {}{}{}", outputPath, key, OUTPUT_FORMAT);
            }
        } catch (IOException e) {
            log.error("There was an error at the moment to build the CSV file {}", e.getMessage());
        }
    }
}
