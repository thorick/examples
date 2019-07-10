
from flask import Flask, jsonify,  request, render_template, redirect, url_for, g, session
import os
import json
from wtforms import TextField
from flask_wtf import Form

import httplib2 as http
# from urlparse import urlparse
from urllib.parse import urlparse

'''
This is a short demo that runs from a browser that has a Python function make a REST call to fetch data 
and then render the data on the demo HTTP browser.

This simulates a webapp that uses a REST API provided by a 3rd party to access information from that 3rd party.

This app uses Flask as the framework for handling the web browser and REST API

To run in a python dev env:

-----------------------------
In a Windows window (adapt for Unix/Linux) create an isolated sandboxed environment:

cd G:\b\notes\python\my_examples\src\call_REST_from_python_demo\sandbox
g:
py -m venv venv                  
cd G:\b\notes\python\my_examples\src\call_REST_from_python_demo\sandbox\venv\Scripts

activate

cd G:\b\notes\python\my_examples\src\call_REST_from_python_demo\sandbox
pip install flask
pip install flask-wtf
pip install wtforms
pip install httplib2




set FLASK_ENV=development
set FLASK_APP=call_REST_from_python_demo.py



flask run


-----------------------------
In a browser run:


Show all books:
http://127.0.0.1:5000/demo

Show only checked out books:
http://127.0.0.1:5000/demo?checkedOut=true

Show only books not checked out:
http://127.0.0.1:5000/demo/?checkedOut=false



----------------------------
The supported REST URI  is for the /books   resource

argument:
    none                    show all books
    checkedOut=true         show only books that are checked out
    checkedOut=false        show only books that are not checked out

e.g.:
    http://127.0.0.1:5000/books?checkedOut=true



'''

app = Flask(__name__)
app.config['SECRET_KEY'] = os.urandom(24)    # required for Flask client session writing ensure that only I can write to the seesion object

book_dict = {}      #  this will be used as a class global to simulate a persistent store
#  for as long as the library demo service is continuously running
patron_dict = {}    #  likewise for the patrons




###################################################
#
#   BOOKS  REST API
#
####################################################



#  GET supports request argument 'checkedOut' == true/false
#      if no checkedOut argument then the request is for is all books checkedOut or not
@app.route('/')
def home():
    retval = "<h4>Demo calling a REST API from a Python App</h4>" + "For base URL&nbsp;&nbsp;&nbsp;&nbsp; http://127.0.0.1:5000/"
    retval = retval + "<br></br><li>To see all books:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; http://127.0.0.1:5000/demo</li>"
    retval = retval + "<li>To see only books checked out:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; http://127.0.0.1:5000/demo?checkedOut=true</li>"
    retval = retval + "<li>To see only books not checked out:&nbsp; http://127.0.0.1:5000/demo?checkedOut=false</li>"
    return retval


@app.route('/books', methods=['GET'])
@app.route('/books/', methods=['GET'])
def allBooks():
    global book_dict
    print(request.method + " " + url_for('allBooks') + "  entered")
    print("we have entered allBooks ")
    print("---  value of request.args.  "+str(request.args))
    print("----    request.args keys.  "+str(request.args.keys()))
    print("---     len request.args.  "+str(len(request.args)))
    for item in request.args.keys():
        print("---  key: "+item)
        print("---  value: "+request.args.get(item))

    _argCheckedOut = request.args.get('checkedOut')
    print(request.method + " " + url_for('allBooks') + "  _argCheckedOut = "+str(_argCheckedOut))
    _showCheckedOutOnly = False
    if _argCheckedOut is None:
        _showCheckedOutOnly = False;
    else:
        if _argCheckedOut == 'true' or _argCheckedOut == 'True':
            _checkedOutValue = True
        else:
            _checkedOutValue = False
        _showCheckedOutOnly = True
    if _showCheckedOutOnly:
        allBooksJSON = bookDictToJSON(_checkedOutValue)
    else:
        allBooksJSON = bookDictToJSONallBooks()

    _response = jsonify(allBooksJSON, 200)
    #  do we need to add a JSON  Content-Type header ?
    return _response


def bookDictToJSONallBooks():
    print("Entered bookDictToJSON1  call  bookDictToJSON(False, False)")
    return bookDictToJSON(None)


def bookDictToJSON(showAllCheckedOut):
    global book_dict
    allBooks = False
    if showAllCheckedOut is None:
        allBooks = True
    print("we have entered bookListToJSON, allBooks = "+str(allBooks)+",  checkedOut = " + str(showAllCheckedOut))
    keys = book_dict.keys()
    result = '{ "bookList" : ['
    isFirst = True
    for bookName in keys:
        try:
            _book = book_dict[bookName]
            if isinstance(_book, Book):
                qualifiedBook = False
                if allBooks == True:
                    qualifiedBook = True
                elif _book.patronName is None:
                    if showAllCheckedOut == False:
                        qualifiedBook = True
                else:
                    if showAllCheckedOut == True:
                        qualifiedBook = True
                if qualifiedBook:
                    if not isFirst:
                        result = result + ', '
                    else:
                        pass
                    result = result + _book.toJSON()
                    isFirst = False
        except Exception:
            pass
    result = result + ' ]}'
    return result


class Book():

    def __init__(self, bookname, patronname = None):
        self.bookName = bookname
        self.patronName = patronname

    def get_name(self):
        return self.bookName

    def checkoutBook(self, patronname):
        _patron = patron_dict[patronname]
        if isinstance(_patron, Patron):
            self.patronName = patronname
            _book = _patron.book_checkout(self.bookName)
            if isinstance(_book, Book):
                return _patron
        return None

    def returnBook(self):
        if isinstance(self.patronName, str):
            _patron = patron_dict[self.patronName]
            if isinstance(_patron, Patron):
                _patron.book_return(self)
        self.patronName = None     # no matter what, the book returns

    def get_checkedout(self):
        return self.patronName

    def isBook(self, other):
        if isinstance(other, Book):
            return True
        else:
            return False


    @staticmethod
    def fromJSON(jsonString):
        try:
            _book = json.loads(jsonString)
            _bookDict = _book['Book']
            _bookname = _bookDict['bookName']
            _patronname = _bookDict['patronName']
            return Book(_bookname, _patronname)
        except Exception:
            print(" could not parse into Book, JSON string: "+jsonString)
            return " could not parse into Book, JSON string: "+jsonString


    @staticmethod
    def getFlattenedBookListFromJSON(jsonString, lineLength=120):
        '''
        Take the JSON representation of the bookList Dictionary:
            { "bookList" : [{ "Book" : [ { "bookName" : "Book A" }, { "patronName" : "None" } ] } ]}

        Returns:  a list of flattened Books each item containing the values:   bookName   patronName
        '''

        print("Book.printFlattenedBookListFromJSON   jsonString="+str(jsonString))

        bookListDict = json.loads(jsonString)
        bookList = bookListDict['bookList']
        flattenedList = []
        for bookDictEntry in bookList:
            # each bookDictEntry for 'Book' is a List of 2 elements:  bookName dict  and patronName dict
            bookDictList = bookDictEntry['Book']
            bookNameDict = bookDictList[0]
            patronNameDict = bookDictList[1]
            flattenedBook = Book.getFlattenedBook(bookNameDict['bookName'], patronNameDict['patronName'], lineLength)
            flattenedList.append(flattenedBook)
        return flattenedList

    @staticmethod
    def getFlattenedBook(bookName, patronName, lineLen):
        strlength = int((lineLen / 2) - 2)
        if len(bookName) > strlength:
            bookName = bookName[:strlength]
        else:
            bookName = bookName.ljust(strlength)

        if patronName == 'None':
            patronName = '- - - - - -'
        if len(patronName) > strlength:
            patronName = patronName[:strlength]
        else:
            patronName = patronName.ljust(strlength)
        return bookName + "  " + patronName

    def toJSON(self):
        return '{ "Book" : [ { "bookName" : "' + str(self.bookName) + '" }, { "patronName" : "' + str(self.patronName) + '" } ] }'




######################
#   LOAD  DEMO BOOKS
######################

key = "Book A"
book = Book(key)
book_dict[key] = book

key = "Book B"
book = Book(key)
book.patronName = "Patron X"
book_dict[key] = book

key = "Book C"
book = Book(key)
book_dict[key] = book


###################
#  RUN DEMO  CALL REST APIs DIRECTLY FROM HERE TO FLASK APP
####################

@app.route('/demo/')
def demo():
    args = request.args
    _args = ""
    _showAllcheckedOut = ""
    if 'checkedOut' in request.args:
        _showAllcheckedOut = request.args.get('checkedOut')
        _args = "?checkedOut="+_showAllcheckedOut

    headers = {
        'Accept': 'application/json',
        'Content-Type': 'application/json; charset=UTF-8'
    }

    _uri = 'http://127.0.0.1:5000'
    _path = '/books/'
    target = urlparse(_uri + _path + _args)
    method = 'GET'
    body = ''

    print("DEMO:  have set up URL "+_uri+_path+_args+".   Now get http Object")
    h = http.Http()

    print("DEMO:  now do REST call.")
    response, content = h.request(target.geturl(), method, body, headers)

    print("DEMO: got response: "+str(response))
    print("DEMO: got content: "+str(content))

    data = json.loads(content)

    #  data[0] is the JSON content    data[1] is the HTTP response status
    flattenedBookList = Book.getFlattenedBookListFromJSON(data[0], 120)

    return render_template('bookList.html', booklist=flattenedBookList)

