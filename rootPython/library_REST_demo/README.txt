
library_REST_demo  README.txt

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
  
  
  