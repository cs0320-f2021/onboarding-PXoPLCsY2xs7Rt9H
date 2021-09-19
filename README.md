# README

##Project 0: Onboarding
a nearest neighbors search to list stars in space that surround input coordinates

###Design
Parses CSV file data and stores into lists. 
When coordinate or name is inputted, 
program calculates the distance of each stored star position from given
position. Then initializes a list of size k (the number of nearest neighbors to find) full of max integer values.
The program sorts the distances in ascending order by comparing each stored distance to each value in initalized list
and inserts value into appropriate place.

###Tests
**empty_data.test** - ensures an error is given when trying to find closeest neighbors with no data \
**include_star.test** - ensures that star id whose position is the same as the position inputted is included in the 
list of closest neighbors \
**invalid_name.test** - ensures an error is given when invalid name is inputted \ 
**not_enough_stars.test** - ensures that the amount of closest neighbors returned is the minimum of k and 
the number of stars in dataset




To run tests:
`./cs32-test src/test/system/stars/*` or `mvn test`


To build use:
`mvn package`

To run use:
`./run`

This will give you a barebones REPL, where you can enter text and you will be output the given number of closest neighbors.

To start the server use:
`./run --gui [--port=<port>]`