from xml import *
import xml.etree.ElementTree as ET

FILE_LOCATION = "book_store.xml"


def main():
    appendXML("New Book")
    readXML()


def readXML():
    # open the file using the xml api, then get the root
    tree = ET.parse(FILE_LOCATION)
    root = tree.getroot()
    # tag = name, attrib = attributes
    print(root.tag, root.attrib)
    # iterate through any children. in this case, there is currently one book.
    for child in root:
        print(child.tag, child.attrib, child.text)
    # iterate through all books. This is currently one book. Same as the child. But this is more explicit.
    for book in root.iter('book'):
        print(book.tag, book.attrib, book.text)
        for attributes in book:
            print(attributes.tag, attributes.attrib, attributes.text)


def appendXML(name):
    tree = ET.parse(FILE_LOCATION)
    bookstore = tree.getroot()
    # create a new element
    book = ET.Element("book")
    # create the subelements to fill the book element.
    title = ET.SubElement(book, "title")
    author = ET.SubElement(book, "author")
    year = ET.SubElement(book, "year")
    price = ET.SubElement(book, "price")
    title.attrib = {'lang': 'en'}
    title.text = name
    author.text = "New Author"
    year.text = str(2023)
    price.text = str(15.99)
    bookstore.append(book)
    # write the new tree to the file.
    tree.write(FILE_LOCATION)


if __name__ == "__main__":
    main()