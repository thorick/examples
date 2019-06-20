
from flask import Flask, jsonify,  request, render_template, redirect, url_for
import os
import json

'''
    This is a demo of a Library application that uses REST API calls to manage it's books and borrowers.
    It is a toy program that does things that should not be done in a real application.
    
    The Demo is to run on a single server that hosts this library_REST_API.py module.  The app must be continuously
    running for the Demo to work.
    A persistent data store for the Library's Books and Patrons is simulated with a pair of globals:
        Book Dictionary     book_dict()  indexed by bookName
        Patron Dictionary   patron_dict() indexed by patronName
        
    Persistence is simulated by using this pair of globals, hence the requirement that the python app must 
    run continuously for the Demo to work.  Shut down the server and all the Books and Patrons disappear.
    
    Here are a few REST calls to manipulate the Books
    

    Here are a few REST calls to manipulate the Patrons
    
    
    Between the Books API and the Patrons API there are NO constraint guarantees.
    It is assumed that the Library app will take care of reconciling Books and Patrons,
    e.g.  there is only a single copy of each book so a single book cannot be checked out by 
    more than one patron at a time.
    In a real physical library with physical books and patrons and a checkout line, it's not so bad to
    have the Library be in charge of constraints since people operations are so slow any race conditions
    between exchanges of books and patrons can be ignored (for the demo only, not in real life).
    
    Here are a few commands:
    
    Books
    
    GET all books
    http://127.0.0.1:5000/books/
    
    POST add new book   'Book C'
    http://127.0.0.1:5000/books/
       body:
       {
	        "Book": {
		    "bookName" : "Book C",
		    "patronName" : ""
	        }
        }
    
    PUT  assign 'Book C' to 'Patron 6'
    http://127.0.0.1:5000/books/Book C
       body:
        {
	        "Book": {
		        "bookName" : "book C",
		        "patronName" : "patron 6"
	        }
        }         
    
    DELETE remove 'Book C'
    http://127.0.0.1:5000/books/Book C
    
    
    
    Patrons
    
    GET all patrons
    http://127.0.0.1:5000/patrons/
    
    POST add new patron  'Patron 1'
    http://127.0.0.1:5000/patrons/
        body:
        {
	        "Patron": {
		        "patronName" : "Patron 1",
		        "checked_out_books" : {}
	        }
        }    
        
    PATCH  add 'Book A' to 'Patron 1'
    http://127.0.0.1:5000/patrons/Patron 1
        body:
        { 
            "op": "add", 
            "path": "checked_out_books", 
            "value": "Book C"
        }
    
    
    
    PATCH  remove 'Book B' from 'Patron 1"
    http://127.0.0.1:5000/patrons/Patron 1
        body:
        { 
            "op": "remove", 
            "path": "checked_out_books", 
            "value": "Book B"
        }  
        
    DELETE  'Patron 1'
    http://127.0.0.1:5000/patrons/Patron 1      



Library OPS   NYI


'''
app = Flask(__name__)
app.config['SECRET_KEY'] = os.urandom(24)    # required for Flask client session writing

book_dict = {}      #  this will be used as a class global to simulate a persistent store
                    #  for as long as the library demo service is continuously running
patron_dict = {}    #  likewise for the patrons


@app.route('/books', methods=['GET'])
@app.route('/books/', methods=['GET'])
def allBooks():
    global book_dict
    print("we have entered allBooks ")
    allBooksJSON = bookDictToJSON()
    _response = jsonify(allBooksJSON, 200)
    #  do we need to add a JSON  Content-Type header ?
    return _response


#  DOESN'T work with POST     @app.route('/books', methods=['POST'])
@app.route('/books/', methods=['POST'])
def doBookPost():
    global book_dict
    print("doPost   we have entered /books/  method="+request.method)
    try:
        print("POST  now parse input JSON data   data="+str(request.data))
        _book = Book.fromJSON(request.data)
        print("POST  parse complete")
    except Exception:
        print("got exception attempting to parse    exc="+str(Exception))
        return jsonify("could not parse Book from "+str(request.data), 404)

    bookname = _book.bookName
    if bookname in book_dict:
        return jsonify("book with bookName '"+bookname+"', already exists. ignore request", 409)
    book_dict[bookname] = _book
    return jsonify("new book "+bookname+" added", 200)


@app.route('/books/<bookname>', methods=['GET', 'PUT', 'DELETE'])
def handleBook(bookname):
    global book_dict
    global DEBUG
    print("we have entered books(bookname)  bookname="+bookname+",  method="+request.method)
    if request.method == 'GET':
        if isinstance(bookname, str):
            try:
                book = book_dict[bookname]
            except Exception:
                return jsonify("", 404)
            if isinstance(book, Book):
                bookJSON = Book.toJSON(book)
                return jsonify(bookJSON, 200)
            else:
                return jsonify("", 404)
        else:
            print("GET   we have entered allBooks ")
            allBooksJSON = bookDictToJSON()
            _response = jsonify(allBooksJSON, 200)
            return _response
    elif request.method == 'PUT':
        try:
            print("PUT  now parse input JSON data   data="+str(request.data))
            _book = Book.fromJSON(request.data)
            print("PUT  parse complete")
        except Exception:
            print("PUT   got exception attempting to parse    exc="+str(Exception))
            return jsonify("", 404)
        patronname = _book.patronName
        try:
            #  PUT we assume that the book already exists
            book = book_dict[bookname]
        except KeyError:
            print("PUT  book "+bookname+" not found so now add it.")
            book_dict[bookname] = _book    # RFC-2616 if book not there  then add it to the library
            print("PUT  added book.  complete")
            return jsonify("", 200)
        except Exception:
            return jsonify("unable to process request", 400)
        book.patronName = patronname       #  UPDATE
        return jsonify("", 200)
    elif request.method == 'DELETE':
        try:
            print("entered DELETE  bookname='"+bookname+"'")
            if bookname in book_dict:
                print("found bookname "+bookname+" in book_dict now do pop()")
                result = book_dict.pop(bookname, None)
                print(" pop complete popped entry is "+str(result))
            return jsonify("", 200)
        except Exception:
            return jsonify("Book not found", 404)
    else:
        return jsonify("command "+str(request.method)+" NYI", 404)


def bookDictToJSON():
    global book_dict
    print("we have entered bookListToJSON")
    keys = book_dict.keys()
    result = '{ "bookList" : {'
    isFirst = True
    for bookName in keys:
        if not isFirst:
            result = result + ', '
        else:
            pass
        try:
            _book = book_dict[bookName]
            if isinstance(_book, Book):
                result = result + _book.toJSON()
                isFirst = False
        except Exception:
            pass
    result = result + ' }'
    return result


@app.route('/patrons', methods=['GET'])
@app.route('/patrons/', methods=['GET'])
def allPatrons():
    global patron_dict
    print("we have entered allPatrons ")
    allPatronsJSON = patronDictToJSON()
    _response = jsonify(allPatronsJSON, 200)
    #  do we need to add a JSON  Content-Type header ?
    return _response


#  DOESN'T work with POST     @app.route('/patrons', methods=['POST'])
@app.route('/patrons/', methods=['POST'])
def doPatronPost():
    global patron_dict
    print("doPost   we have entered /patrons/  method="+request.method)
    try:
        print("POST  now parse input JSON data   data="+str(request.data))
        _patron = Patron.fromJSON(request.data)
        print("POST  parse complete")
    except Exception:
        print("got exception attempting to parse    exc="+str(Exception))
        return jsonify("could not parse patron from "+str(request.data), 404)

    patronname = _patron.patronName
    if patronname in patron_dict:
        return jsonify("patron with patronName '"+patronname+"', already exists. ignore request", 409)
    patron_dict[patronname] = _patron
    print("POST  return normal  new patron added to global dictionary.  verify read back dictionary now: "+str(patronDictToJSON()))
    return jsonify("new patron "+patronname+" added", 200)


@app.route('/patrons/<patronname>', methods=['GET', 'DELETE', 'PATCH'])
def handlepatron(patronname):
    global patron_dict
    global DEBUG
    print("we have entered patrons(patronname)  patronname="+patronname+",  method="+request.method)
    if request.method == 'GET':
        if isinstance(patronname, str):
            try:
                patron = patron_dict[patronname]
            except Exception:
                return jsonify("", 404)
            if isinstance(patron, patron):
                patronJSON = patron.toJSON(patron)
                return jsonify(patronJSON, 200)
            else:
                return jsonify("", 404)
        else:
            print("GET   we have entered allPatrons ")
            allPatronsJSON = patronDictToJSON()
            _response = jsonify(allPatronsJSON, 200)
            return _response
    elif request.method == 'PUT':                # PUT currently has no use in Patron but that could change
        try:
            print("PUT  now parse input JSON data   data="+str(request.data))
            _patron = Patron.fromJSON(request.data)
            print("PUT  parse complete")
        except Exception:
            print("PUT   got exception attempting to parse    exc="+str(Exception))
            return jsonify("", 404)
        patronname = _patron.patronName
        try:
            #  PUT we assume that the patron already exists
            patron = patron_dict[patronname]
        except KeyError:
            print("PUT  patron "+patronname+" not found so now add it.")
            patron_dict[patronname] = _patron    # RFC-2616 if patron not there  then add it to the library
            print("PUT  added patron.  complete")
            return jsonify("", 200)
        except Exception:
            return jsonify("unable to process request", 400)
        patron.patronName = patronname       #  UPDATE
        return jsonify("", 200)
    elif request.method == 'DELETE':
        try:
            print("entered DELETE  patronname='"+patronname+"'")
            if patronname in patron_dict:
                print("found patronname "+patronname+" in patron_dict now do pop()")
                result = patron_dict.pop(patronname, None)
                print(" pop complete popped entry is "+str(result))
                return jsonify("", 200)
            return jsonify("patron "+patronname+" not found", 404)
        except Exception:
            return jsonify("patron not found", 404)
    elif request.method == 'PATCH':
        try:
            print("entered PATCH  patronname='"+patronname+"'")
            if patronname in patron_dict:
                print(patronname+" is in the patron_dict")
                _patron = patron_dict[patronname]
                _command_dict = json.loads(request.data)
                print("  PATCH  the command is "+str(_command_dict))
                _path = _command_dict['path']
                print("  PATCH  _path is "+str(_path))
                if isinstance(_path, str):
                    if _path == 'checked_out_books':
                        _value = _command_dict['value']
                        print("  PATH  _value is "+str(_value))
                        # _book = book_dict[_value]
                        # if isinstance(_book, Book):
                        _command = _command_dict['op']
                        if isinstance(_command, str):
                            if _command == 'add':
                                _patron.checked_out_books[_value] = 'X'
                                return jsonify("", 200)
                            elif _command == 'remove':
                                _patron.checked_out_books.pop(_value, None)
                                return jsonify("", 200)
                        else:
                            return jsonify("cannot handle PATCH command '"+str(_command)+"'", 400)
                    else:
                        return jsonify("cannot handle PATCH path '"+str(_path)+"'", 400)
                else:
                    return jsonify("cannot handle PATCH path '"+str(_path)+"'", 400)
            else:
                return jsonify("there is no patron named '"+str(patronname)+"'", 400)
        except Exception:
            return jsonify("error attempting PATCH using data: "+str(request.data), 404)
    else:
        return jsonify("command "+str(request.method)+" NYI", 404)


def patronDictToJSON():
    global patron_dict
    print("we have entered patronListToJSON")
    keys = patron_dict.keys()
    print("patron_dict keys = "+str(keys)+",   len="+str(len(keys)))
    result = '{ "patronList" : {'
    isFirst = True
    for patronName in keys:
        if not isFirst:
            result = result + ', '
        else:
            pass
        try:
            _patron = patron_dict[patronName]
            if isinstance(_patron, Patron):
                result = result + _patron.toJSON()
                isFirst = False
        except Exception:
            pass
    result = result + ' }'
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

    def toJSON(self):
        return '{ "Book" : { "bookName" : "' + str(self.bookName) + '", "patronName" : "' + str(self.patronName) + '" } }'


class Patron():

    def __init(self, name, checked_out_books = None):
        self.patronName = name
        self.checked_out_books = checked_out_books

    def book_checkout(self, book):
        _book = book_dict[book.bookName]
        if isinstance(_book, Book):
            self.checked_out_books[book.bookName] = "X"
            _book.patronName = self.patronName
            return _book
        return None

    def book_return(self, book):
        _book = book_dict[book.bookName]
        if isinstance(_book, Book):
            _bookName = self.checked_out_books.pop(book.bookName, None)
            return _bookName
        return None

    @staticmethod
    def fromJSON(jsonString):
        try:
            _patron = json.loads(jsonString)
            _patronDict = _patron['Patron']
            print(" got patronDict "+str(_patronDict))
            _patronname = _patronDict['patronName']
            print("  got patronName = "+str(_patronname))
            _temp = _patronDict['checked_out_books']
            print("  got checked_out_books = "+str(_temp))
            _checked_out_books = {}
            print("  here is [*_temp] "+str([*_temp]) + " len="+str(len([*_temp])))
            # _test = { 1: 1}
            # _testKeys = [*_test]
            # print("  here is [*_test] "+str([*_test])+" len="+str(len([*_test])))
            _tempKeys = [*_temp]
            print("  here is _tempKeys "+str(_tempKeys))
            if len(_tempKeys) > 0:
                print(" length of keys in _temp is "+str(len(_tempKeys)))
                print("   here is _tempKeys "+str(_tempKeys))
                for _key in _tempKeys:
                    print(" process first key = '"+str(_key))
                    # _value = _temp[_key]
                    # if isinstance(_value, str):
                    _checked_out_books[_key] = "X"
                    print("_checked_out_books["+str(_key)+"] = "+str( _checked_out_books[_key]))
            print(" complete _checked_out_books = "+str(_checked_out_books))
            print("now construct new Patron and return")
            # patron = Patron(_patronname)
            patron = Patron()
            print("new Patron()  complete")
            patron.patronName = _patronname
            patron.checked_out_books = _checked_out_books
            print("new Patron() toJSON() "+str(patron.toJSON()))
            return patron
            # return Patron(_patronname, _checked_out_books)
        except Exception:
            print(" could not parse into Book, JSON string: "+jsonString)

    def toJSON(self):
        _json = '{ "Patron : {'
        _json += ' "patronName" : "' + str(self.patronName) + '", '
        _json += ' "checked_out_books" : {'
        first = True
        for _key in [*self.checked_out_books]:
            _value = self.checked_out_books[_key]
            if isinstance(_value, str):
                if first == False:
                    _json += ', '
                _json += ' "' + str(_key) + '" : "' + str(_value) + '"'
                first = False
        _json += '}'
        _json += '}}'
        return _json

key = "Book A"
bookA = Book(key)
book_dict[key] = bookA


if __name__ == '__main__':
    app.run(debug=True)

