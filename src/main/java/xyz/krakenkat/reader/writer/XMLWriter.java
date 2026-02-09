package xyz.krakenkat.reader.writer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import xyz.krakenkat.reader.dto.AuthorDTO;
import xyz.krakenkat.reader.dto.ItemDTO;
import xyz.krakenkat.reader.dto.TitleDTO;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class XMLWriter {

    public void buildFile(TitleDTO titleDTO) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // title element
        Document doc = builder.newDocument();
        Element title = doc.createElement("title");
        doc.appendChild(title);

        // title -> name
        Element name = doc.createElement("name");
        name.setTextContent(titleDTO.getName());
        title.appendChild(name);

        // title -> key
        Element key = doc.createElement("key");
        key.setTextContent(titleDTO.getKey());
        title.appendChild(key);

        // title -> cover
        Element cover = doc.createElement("cover");
        cover.setTextContent(titleDTO.getCover());
        title.appendChild(cover);

        // title -> demography
        Element demography = doc.createElement("demography");
        demography.setTextContent(titleDTO.getDemography());
        title.appendChild(demography);

        // title -> format
        Element format = doc.createElement("format");
        format.setTextContent(titleDTO.getFormat());
        title.appendChild(format);

        // title -> type
        Element type = doc.createElement("type");
        type.setTextContent(titleDTO.getType());
        title.appendChild(type);

        // title -> frequency
        Element frequency = doc.createElement("frequency");
        frequency.setTextContent(titleDTO.getFrequency());
        title.appendChild(frequency);

        // title -> status
        Element status = doc.createElement("status");
        status.setTextContent(titleDTO.getStatus());
        title.appendChild(status);

        // title -> totalIssues
        Element totalIssues = doc.createElement("totalIssues");
        totalIssues.setTextContent(titleDTO.getTotalIssues());
        title.appendChild(totalIssues);

        // title -> releaseDate
        Element releaseDate = doc.createElement("releaseDate");
        releaseDate.setTextContent(titleDTO.getReleaseDate());
        title.appendChild(releaseDate);

        // title -> genres
        Element genres = doc.createElement("genres");

        for (String genre : titleDTO.getGenres()) {
            Element genreElement = doc.createElement("genre");
            genreElement.setTextContent(genre);
            genres.appendChild(genreElement);
        }

        title.appendChild(genres);

        // title -> authors
        Element authors = doc.createElement("authors");

        for (AuthorDTO authorDTO : titleDTO.getAuthors()) {
            Element author = doc.createElement("author");
            Element authorName = doc.createElement("name");
            authorName.setTextContent(authorDTO.getName());
            author.appendChild(authorName);

            Element authorRoles = doc.createElement("roles");

            for (String role : authorDTO.getRoles()) {
                Element roleElement = doc.createElement("role");
                roleElement.setTextContent(role);
                authorRoles.appendChild(roleElement);
            }

            author.appendChild(authorRoles);
            authors.appendChild(author);
        }

        title.appendChild(authors);

        // title -> issues
        Element issues = doc.createElement("issues");

        for (ItemDTO itemDTO : titleDTO.getIssues()) {
            Element issue = doc.createElement("issue");

            // title -> issues -> issue -> name
            Element issueName = doc.createElement("name");
            issueName.setTextContent(itemDTO.getName());
            issue.appendChild(issueName);

            // title -> issues -> issue -> number
            Element issueNumber = doc.createElement("number");
            issueNumber.setTextContent(itemDTO.getNumber().toString());
            issue.appendChild(issueNumber);

            // title -> issues -> issue -> cover
            Element issueCover = doc.createElement("cover");
            issueCover.setTextContent(itemDTO.getCover());
            issue.appendChild(issueCover);

            // title -> issues -> issue -> pages
            Element issuePages = doc.createElement("pages");
            issuePages.setTextContent(itemDTO.getPages().toString());
            issue.appendChild(issuePages);

            // title -> issues -> issue -> printedPrice
            Element issuePrintedPrice = doc.createElement("printedPrice");
            issuePrintedPrice.setTextContent(itemDTO.getPrice().toString());
            issue.appendChild(issuePrintedPrice);

            if (itemDTO.getDigitalPrice() != null) {
                // title -> issues -> issue -> digitalPrice
                Element issueDigitalPrice = doc.createElement("digitalPrice");
                issueDigitalPrice.setTextContent(itemDTO.getDigitalPrice().toString());
                issue.appendChild(issueDigitalPrice);
            }

            // title -> issues -> issue -> currency
            Element issueCurrency = doc.createElement("currency");
            issueCurrency.setTextContent(itemDTO.getCurrency());
            issue.appendChild(issueCurrency);

            // title -> issues -> issue -> releaseDate
            Element issueReleaseDate = doc.createElement("releaseDate");
            issueReleaseDate.setTextContent(itemDTO.getDate());
            issue.appendChild(issueReleaseDate);

            // title -> issues -> issue -> isbn
            Element issueISBN = doc.createElement("isbn");
            issueISBN.setTextContent(itemDTO.getIsbn());
            issue.appendChild(issueISBN);

            // title -> issues -> issue -> variant
            Element issueVariant = doc.createElement("variant");
            issueVariant.setTextContent(itemDTO.getVariant().toString());
            issue.appendChild(issueVariant);

            issues.appendChild(issue);
        }

        title.appendChild(issues);

        try(FileOutputStream outputStream = new FileOutputStream("D:\\titles\\panini-manga-mx\\" + titleDTO.getKey() + ".xml")) {
            writeXml(doc, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeXml(Document doc, OutputStream output) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);
    }
}
