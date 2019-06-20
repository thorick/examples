
library_REST_demo  README.txt   v2019-06-20

This is an in progress demo showing the use of Python and flask to build a set of REST APIs and a python driven webpage 
to simulate a simple Public Book Library with physical books to lend and patrons that are actual people.
The constraints of actual books and actual people allow us to forgo a lot of concurrency issues that might arise
if this were an electronic library with a finite collection of documents to check out to a highly concurrent set of 
on line users (maybe documents can only be lent to a single user at a time due to license conditions or something).

The Books and Patrons resources are maintained using a REST API.
The Library is a webapp that uses REST API calls to the Books and Patrons to manage the checking out and return of Books 
by the Libary's Patrons.


currently:
  the only things that work are the Books and Patrons REST API.
  The Library webapp is NYI.
  
  
To run the demo:
   1.  install Python 3    go to Python site and install
   2.  go to the directory where this README.txt is installed and cd down to sandbox 
			'cd <basedir>/library_REST_demo/sandbox'
   3.  start a virtual env  with name <myVirtualEnvName>:
			py -m venv <myVirtualEnvName>
			
   4. cd '<basedir>/library_REST_demo/sandbox/<myVirtualEnvName>/Scripts'
   5. run:    'activate'
   6. install flask:   'pip install flask'
   7. 'set FLASK_ENV=development'
   8. 'set FLASK_APP=library_REST_API.py'
   9. 'cd <basedir>/library_REST_demo/sandbox'
   10. bring up your development webapp server with library_REST_API.py running:   'flask run'   
   11. you can connect to the server at  http://127.0.0.1:5000
   12. I used Postman to send REST API requests, free for individual users.
   13. See library_REST_demo.py comments for instructions on sending REST API requests to the webserver
   
   
	
   
   
	
  