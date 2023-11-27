import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLParser {
    public static void main(String[] args) {
        try  {
            File file = new File("book_store.xml");
            System.out.println(file.getAbsolutePath());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            document.getDocumentElement().normalize();


            // Greife auf alle Buch-Elemente zu
            Element bookstoreElement = document.getDocumentElement();

            NodeList bookList = bookstoreElement.getElementsByTagName("book");

            // Iteriere über die Buch-Elemente
            for (int i = 0; i < bookList.getLength(); i++) {
                Node bookNode = bookList.item(i);
                if (bookNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element bookElement = (Element) bookNode;

                    // Lese Werte aus den Buch-Elementen
                    String category = bookElement.getAttribute("category");
                    String title = getElementValue(bookElement, "title");
                    String author = getElementValue(bookElement, "author");
                    String year = getElementValue(bookElement, "year");
                    String price = getElementValue(bookElement, "price");

                    // Gib die Informationen aus
                    System.out.println("Category: " + category);
                    System.out.println("Title: " + title);
                    System.out.println("Author: " + author);
                    System.out.println("Year: " + year);
                    System.out.println("Price: " + price);
                    System.out.println("------------------------");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Hilfsmethode, um den Wert eines bestimmten Elements zu erhalten
    private static String getElementValue(Element parentElement, String elementName) {
        NodeList nodeList = parentElement.getElementsByTagName(elementName);
        Node node = nodeList.item(0);
        return node.getTextContent();
    }
}
