import com.microsoft.sqlserver.jdbc.SQLServerDriver;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;

import java.lang.reflect.Field;

import java.sql.*;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class Main {

    static {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_hhmmss");
        System.setProperty("current.date", dateFormat.format(new Date()));
    }

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        //User specified they want to see a help menu by entering '-help', this will provide help menu and exit
        if (args[0].equals("-help")) {
            System.out.println("Welcome to the help menu! Here is a list of possible arguments to choose from: \n");
            System.out.println("-n {server_name}   \t\tEnter in a server name.");
            System.out.println("-p {sql_path}      \t\tEnter a path to a SQL/txt file");
            System.out.println("-db {database_name}\t\tThis is optional, but you may enter a specific database name");
            System.out.println("-u {username}      \t\tEnter a username, or don\'t use this command if you want to connect" +
                    " using Windows Authentication");
            System.out.println("-pass {password} \t\tEnter your login password");
            System.out.println("\n");
            System.out.println("Please re-run this program using the valid arguments listed");
            System.out.println("Note: You at least need to enter a servername and a sql path file");
            System.exit(0);
        }
        //Create a path to C://sqlauth and see if the user already has this directory
        MyDownloader myDownloader = new MyDownloader(new File("C:\\\\sqlauth"));
        //returns true if the file was not found previous to running the program
        if (myDownloader.isNeedToRestart()) {
            //Must restart program since directory was not there previously
            System.out.println();
            System.out.println();
            System.out.println("There was an important directory missing in C: that we just created, called \'sqlauth\' " +
                    " which contained a file named \'sqljdbc_auth.dll\' that is needed for Windows Authentication.\n\n" +
                    "The folder and file is now there and the program is ready to be run again. Please re-run" +
                    " your program again");
            System.exit(0);
        }
        System.setProperty("java.library.path", "C:\\\\sqlauth"); //telling program where the sqljdbc_auth.dll file is
        try {
            //This process is part of setting up the setProperty method, since it is normally only has read only access
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logger.info("SQLTimer startup.");
        //Create a Map for command line arguments
        Map<String, String> map = new HashMap<>();
        map.put("-n", ""); //Server name
        map.put("-p", ""); //Path to SQL/TXT file
        map.put("-u", ""); //Username (optional)
        map.put("-pass", ""); //Password (optional)
        map.put("-db", ""); //Database name (optional)

        for (int i = 0; i < args.length; i += 2) {
            if (map.containsKey(args[i])) {
                map.put(args[i], args[i + 1]);
            } else {
                //The key entered is not part of the Map and is not '-help', not sure what they wanted to do
                System.out.println("Bad input. The flag \'" + args[i] + "\' was not recognized. Please enter using" +
                        " the following format: \n");
                System.out.println("-n {server_name} -p {sql_path_file} -u {username} -pass {password} " +
                        "-db {database_name}");
                System.out.println("\nYou may also use the \'-help\' for more information");
                System.exit(0);
            }
        }
        String path = map.get("-p");
        String serverName = map.get("-n");
        if (path.equals("") || serverName.equals("")) {
            //Missing path or server name or both, program needs at least these two.
            System.out.println();
            System.out.println("Not enough arguments given. Please re-run the program and enter arguments in the" +
                    " following format: ");
            System.out.println("-n {server_name} -p {sql_path_file} -u {username} -pass {password} " +
                    "-db {database_name}");
            System.out.println();
            System.out.println("Note: Servername (-n) and path file (-p) are mandatory");
            System.out.println("\nYou may also use the \'-help\' for more information");
            System.exit(0);
        }
        //Print out the Map
        for (Map.Entry<String, String> e : map.entrySet()) {
            System.out.println("Key: " + e.getKey() + "\tVal: " + e.getValue());
        }
        System.out.println();
        System.out.println("-----------------------------------------------");
        System.out.println();

        File sqlFile = new File(map.get("-p"));
        Scanner scan = null;
        //Try to read the sql/txt file given
        try {
            scan = new Scanner(sqlFile);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find the file specified, exiting now....");
            System.exit(0);
        } catch (Exception e) {
            System.out.println("Bad input file, something went wrong. Try using a .txt or .sql file");
            System.exit(0);
        }
        String sqlContent = "";
        logger.info("Reading query file " + sqlFile);
        //Keep scanning file until there's nothing else to read
        while (scan.hasNextLine()) {
            sqlContent = sqlContent.concat(scan.nextLine() + "\n");
        }
        String url;
        String dbName = map.get("-db");
        if (dbName.equals("")) {
            //no database name given, use 'master' as default
            url = "jdbc:sqlserver://" + map.get("-n") + ";databaseName=master";
        } else {
            //database name given, use the specified database name
            url = "jdbc:sqlserver://" + map.get("-n") + ";databaseName=" + map.get("-db");
        }
        String username = map.get("-u");
        String password = map.get("-pass");
        Connection con = null;
        Statement s1 = null;
        ResultSet res = null;
        try {

            try {
                if (username.equals("")) {
                    System.out.println("\nNo Username was given, attempting to connect from Windows Authentication...");
                    //no user specified, try to connect with Windows Auth using integratedSecurity=true
                    url += ";integratedSecurity=true";
                    //System.out.println("The url is: " + url);
                    con = DriverManager.getConnection(url);
                } else {
                    //username was given, check to see if database was given
                    if (dbName.equals("")) {
                        //username was given but use default database
                        url = "jdbc:sqlserver://" + map.get("-n") + ";databaseName=master";
                        con = DriverManager.getConnection(url, username, password);
                    } else {
                        //database was given and username was given
                        url = "jdbc:sqlserver://" + map.get("-n") + ";databaseName=" + map.get("-db");
                        con = DriverManager.getConnection(url, username, password);
                    }

                }

                System.out.println("\nConnected to server!");
                System.out.println("Running query now...\n");
            } catch (Exception e) {
                logger.error(e.getMessage());
                System.out.println("\nCould not connect to instance. Check log in credentials and make sure the user" +
                        " specified has access to that database. Also check if server name is correct. ERROR has been" +
                        " sent to the log. Quitting now...");
                System.exit(0);
            }
            try {
                //Passing in those arguments in order to be able to retrieve the row count without parsing through table
                s1 = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            } catch (Exception e) {
                logger.error(e.getMessage());
                System.out.println("Could not create statement. Exiting now...");
                System.exit(0);
            }
            logger.info("Submitting query to instance " + map.get("-n"));
            long start = System.currentTimeMillis(); //Starting a timer to determine query execution time
            try {
                res = s1.executeQuery(sqlContent);
            } catch (SQLException s) {
                //try to log all errors produced
                System.out.println("Query could not be executed. Check log for error. Quitting now...");
                while (s != null) {
                    logger.error(s.getMessage());
                    s = s.getNextException();
                }
                System.exit(0);
            } catch (Exception e) {
                logger.error(e.getMessage());
                System.out.println("Query could not be executed. Check log for error. Quitting now...");
                System.exit(0);
            }

            long end = System.currentTimeMillis();
            long millis = end - start;
            //convert the millis time to hh:mm:ss
            logger.info("Query finished, runtime = " +
                    String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(millis),
                            TimeUnit.MILLISECONDS.toMinutes(millis) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                            TimeUnit.MILLISECONDS.toSeconds(millis) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))));
            System.out.println("The time taken to execute result set was " + millis + "ms");
            //SimpleDateFormat queryFinished = new SimpleDateFormat("HH:mm:ss");
            //s1.execute(sqlContent);
            long rowCount = 0; //hard codded for now so we dont keep counting rows, CHANGE THIS TO 0
            try {
                //Instead of parsing through all the rows and counting, just point to the last row and save the row number
                res.last();
                rowCount = res.getRow();
                logger.info("Number of rows returned = " + rowCount);
                res.beforeFirst();
            } catch (Exception e) {
                logger.error(e.getMessage());
                System.out.println("Could not get number of rows...");
            }
            //Try to get any SQL Warnings that may exist
            SQLWarning warning = null;
            String warningMessage = "";
            try {
                warning = s1.getWarnings();
            } catch (Exception e) {
                logger.error(e.getMessage());
                System.out.println("Could not load warnings...");
            }
            if (warning != null) {
                //We found some warnings, keep printing out warnings until there's none left
                while (warning != null) {
                    System.out.println(warning.getMessage());
                    warningMessage += warning.getMessage() + "\n";
                    warning = warning.getNextWarning();
                }
                System.out.println(warningMessage);
                logger.warn(warningMessage);
            } else {
                System.out.println("No warning lines to print!");
            }
            String num_rows_affected = "(" + rowCount + " row(s) affected)";
            System.out.println(num_rows_affected);
            logger.info("SQLTimer end");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            //close all connections after use
            if (res != null) {
                try {
                    res.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (s1 != null) {
                try {
                    s1.close();
                } catch (SQLException e) { /* ignored */}
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) { /* ignored */}
            }
        }

    }
}
