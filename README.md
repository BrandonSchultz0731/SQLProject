# SQLProject
Program that reads a file that containing a SQL query, submits the query to a specified SQL Server, waits for the query to finish, then prints out any messages returned, error message or otherwise but not the query result set.   The program should use the log4j logging package to write a log file with entries for each stage of the process: read query file, submit query, query completed.  The program connection to SQL Server should use the login and password provided on the command line; if no login/pwd is provided then it connects using Windows Authentication of the user running the program.  This program will be used to create runtime baselines to track query performance during testing.

## How it works
1. Once the gradle project is built, locate the executable under build\distributions\SQLProject-1.0-SNAPSHOT\SQLProject-1.0-SNAPSHOT\bin inside the command line
![Dir image](https://github.com/BrandonSchultz0731/SQLProject/blob/master/SQLProjectCapture1.PNG)

2. Enter the SQLProject file name (this can be renamed to anything, i called it "SQL_Timer"). Run that executable with command line arguments.
