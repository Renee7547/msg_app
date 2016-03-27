This program should be run on @well.cs.ucr.edu server, with postgre sql installed.

Files description:
* ``project/data`` - contains the files, which will be used to populate your database
* ''project/sql/src/create tables.sql'' - SQL script creating the database relational schema. It also includes the commands to drop these tables.
* project/sql/src/create indexes.sql - SQL script which creates database indexes. Initially is empty, you should add all your indexes to this file.
* project/sql/src/load data.sql - SQL script for loading the data in your tables. The script loads each text file into the appropri- ate table. Note that the file paths have to be changed to absolute paths in order to make it work.
* project/sql/scripts/create db.sh - shell script, which you should to setup your database.
* project/java/src/Messenger.java - A basic java User Interface to your Postgres database. All SQL-specific code locates there.
* project/java/scripts/compile.sh-compiles&runsyourjavacode.
* project/java/lib/pg73jdbc3.jar - The Postgres JDBC driver, which is necessary for your Java code.

Change path to data files in project/sql/src/load data.sql. Use absolute paths to avoid ambiguity. After that your load statements should look like this:
'''
COPY USER LIST
FROM ’/home/user/project/data/usr list .csv ’ 
WITH DELIMITER ’ ; ’ ;
'''

Execute project/sql/scripts/create db.sh to load your database

Execute project/java/scripts/compile.sh to compile and run your Java client.
