import xml
import json

import xmltodict as xmltodict

FILE_LOCATION = "book_store.xml"

if __name__ == '__main__':
    xml_file = open(FILE_LOCATION, 'r')
    xml_data = xml_file.read()
    xml_dict_data = xmltodict.parse(xml_data)
    # dumps generates a string representation of the xml string.
    json_data = json.dumps(xml_dict_data)
    print(json_data)
    # save the json data to a file.
    json_file = open("book_store.json", 'w')
    json_file.write(json_data)
    json_file.close()