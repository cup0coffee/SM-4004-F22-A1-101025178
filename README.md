# 4004-SeleniumBlackjack
A basic blackjack web application to get experience using selenium as a testing method.

Some code taken from the web (spring documentation), see:

https://github.com/spring-projects/spring-boot/tree/master/spring-boot-samples/spring-boot-sample-websocket-tomcat


To build this project
---------------------

Building this project requires git (or download it manually), and maven. 

  1. `git clone https://github.com/Sacredify/4004-SeleniumBlackjack.git`
  2. `cd 4004-SeleniumBlackjack`
  3. `mvn clean install`
  
To log output
-------------

Two options:

  1. `mvn clean install --log-file resultbj.txt`
  2. `mvn clean install > resultbj.txt`

To run this project
-------------------

Build the project as above to assemble the stand-alone jar.

  1. `java -jar SeleniumBlackjack-1.0-SNAPSHOT.jar`

Allow the code to run until you see this message:
  
  `... c.c.blackjack.BlackJackApplication       : Started BlackJackApplication in 4.512 seconds`
  
After that, open any browser and navigate to:

  `http://localhost:8080/`

Running the tests
-----------------

Running the tests (selenium) is best done through maven:

  `mvn clean test`
  
Tests aren't run as part of the build process, although they are compiled for errors.

IMPORTING AND RUNNING WITH ECLIPSE (Eclipse Mars)
----------------------

For the eclipse users, you can:

  1. `git clone https://github.com/Sacredify/4004-SeleniumBlackjack.git`
  2. `File --> Import --> Maven --> Import existing maven projects --> 4004-SeleniumBlackjack --> pom.xml selected.
  3. From there, you can do whatever. Select the main class (BlackJackApplication) and run (as application).
  4. To run the tests, you can right click src/test/java/ and do run as JUnit which will run both selenium (allow the browser to do its thing, or switch to PhantomJS) and cucumber tests. 

IMPORTANT INFO
--------------

  1. When someone disconnects, if they were the admin, the entire game shuts down. Re-connect as a new admin.
  2. When someone else disconnects, they will be replaced by an AI.
  3. Aces are automaticlly converted when possible.
  4. And probably some other stuff.
  5. The cucumber tests cover basic AI functionality and whatnot.
  6. The selenium tests will try to run through an actual game.
  7. I'm not spending 100% more time making it 'truly testable' by allowing custom input - thats not the point of this assignment.
  8. Please note there may be timing issues with the tests - they worked for me but selenium is like that. Try re-running them. Who knows?
