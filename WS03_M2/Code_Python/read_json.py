import json
from json import *

FILE_LOCATION = "book_store.json"


def readJSON():
    # Open the file
    jsonFile = json.load(open(FILE_LOCATION))
    # reformat the json file to be more readable.
    jsonFile = json.dumps(jsonFile, indent=2, sort_keys=True)
    print(jsonFile)


if __name__ == "__main__":
    readJSON()