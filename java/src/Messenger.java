/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Messenger {

    // reference to physical database connection.
    private Connection _connection = null;

    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
    static BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

    /**
     * Creates a new instance of Messenger
     *
     * @param hostname the MySQL or PostgreSQL server hostname
     * @param database the name of the database
     * @param username the user name used to login to the database
     * @param password the user login password
     * @throws java.sql.SQLException when failed to make a connection.
     */
    public Messenger (String dbname, String dbport, String user, String passwd) throws SQLException {

        System.out.print("Connecting to database...");
        try{
            // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println ("Connection URL: " + url + "\n");

            // obtain a physical connection
            this._connection = DriverManager.getConnection(url, user, passwd);
            System.out.println("Done");
        }catch (Exception e){
            System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
        }//end catch
    }//end Messenger

    /**
     * Method to execute an update SQL statement.  Update SQL instructions
     * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
     *
     * @param sql the input SQL string
     * @throws java.sql.SQLException when update failed
     */
    public void executeUpdate (String sql) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the update instruction
        stmt.executeUpdate (sql);

        // close the instruction
        stmt.close ();
    }//end executeUpdate

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and outputs the results to
     * standard out.
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQueryAndPrintResult (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        /*
         ** obtains the metadata object for the returned result set.  The metadata
         ** contains row and column info.
         */
        ResultSetMetaData rsmd = rs.getMetaData ();
        int numCol = rsmd.getColumnCount ();
        int rowCount = 0;

        // iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while (rs.next()){
            if(outputHeader){
                for(int i = 1; i <= numCol; i++){
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();
                outputHeader = false;
            }
            for (int i=1; i<=numCol; ++i)
                System.out.print (rs.getString (i) + "\t");
            System.out.println ();
            ++rowCount;
        }//end while
        stmt.close ();
        return rowCount;
    }//end executeQuery

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the results as
     * a list of records. Each record in turn is a list of attribute values
     *
     * @param query the input query string
     * @return the query result as a list of records
     * @throws java.sql.SQLException when failed to execute the query
     */
    public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
        // creates a statement object 
        Statement stmt = this._connection.createStatement (); 

        // issues the query instruction 
        ResultSet rs = stmt.executeQuery (query); 

        /* 
         ** obtains the metadata object for the returned result set.  The metadata 
         ** contains row and column info. 
         */ 
        ResultSetMetaData rsmd = rs.getMetaData (); 
        int numCol = rsmd.getColumnCount (); 
        int rowCount = 0; 

        // iterates through the result set and saves the data returned by the query. 
        boolean outputHeader = false;
        List<List<String>> result  = new ArrayList<List<String>>(); 
        while (rs.next()){
            List<String> record = new ArrayList<String>(); 
            for (int i=1; i<=numCol; ++i) 
                record.add(rs.getString (i)); 
            result.add(record); 
        }//end while 
        stmt.close (); 
        return result; 
    }//end executeQueryAndReturnResult

    /**
     * Method to execute an input query SQL instruction (i.e. SELECT).  This
     * method issues the query to the DBMS and returns the number of results
     *
     * @param query the input query string
     * @return the number of rows returned
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int executeQuery (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        int rowCount = 0;

        // iterates through the result set and count nuber of results.
        if(rs.next()){
            rowCount++;
        }//end while
        stmt.close ();
        return rowCount;
    }

    /**
     * Method to fetch the last value from sequence. This
     * method issues the query to the DBMS and returns the current 
     * value of sequence used for autogenerated keys
     *
     * @param sequence name of the DB sequence
     * @return current value of a sequence
     * @throws java.sql.SQLException when failed to execute the query
     */
    public int getCurrSeqVal(String sequence) throws SQLException {
        Statement stmt = this._connection.createStatement ();

        ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
        if (rs.next())
            return rs.getInt(1);
        return -1;
    }

    /**
     * Method to close the physical connection if it is open.
     */
    public void cleanup(){
        try{
            if (this._connection != null){
                this._connection.close ();
            }//end if
        }catch (SQLException e){
            // ignored.
        }//end try
    }//end cleanup

    /**
     * The main execution method
     *
     * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
     */
    public static void main (String[] args) {
        if (args.length != 3) {
            System.err.println (
                    "Usage: " +
                    "java [-classpath <classpath>] " +
                    Messenger.class.getName () +
                    " <dbname> <port> <user>");
            return;
        }//end if

        Greeting();
        Messenger esql = null;
        try{
            // use postgres JDBC driver.
            Class.forName ("org.postgresql.Driver").newInstance ();
            // instantiate the Messenger object and creates a physical
            // connection.
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];
            esql = new Messenger (dbname, dbport, user, "");

            boolean keepon = true;
            while(keepon) {
                // These are sample SQL statements
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. Create user");
                System.out.println("2. Log in");
                System.out.println("0. < EXIT");
                String authorisedUser = null;
                switch (readChoice()){
                    case 1: CreateUser(esql); break;
                    case 2: authorisedUser = LogIn(esql); break;
                    case 0: keepon = false; break;
                    default : System.out.println("Unrecognized choice!"); break;
                }//end switch
                if (authorisedUser != null) {
                    boolean usermenu = true;
                    while(usermenu) {
                        System.out.println("MAIN MENU");
                        System.out.println("---------");
                        System.out.println("1. Messages");
                        System.out.println("2. Chats");
                        System.out.println("3. Users");
                        System.out.println(".........................");
                        System.out.println("9. Delete your account");
                        System.out.println("0. Log out");
                        switch (readChoice()){
                            case 1: Messages( esql, authorisedUser); break;
                            case 2: Chats( esql, authorisedUser); break;
                            case 3: User( esql, authorisedUser); break;
                            case 9: if (DeleteAccount(esql, authorisedUser)){
											usermenu=false;
											authorisedUser=null; 
											break;
									}
									break;
                            case 0: usermenu = false; break;
                            default : System.out.println("Unrecognized choice!"); break;
                        }
                    }
                }
            }//end while
        }catch(Exception e) {
            System.err.println (e.getMessage ());
        }finally{
            // make sure to cleanup the created table and close the connection.
            try{
                if(esql != null) {
                    System.out.print("Disconnecting from database...");
                    esql.cleanup ();
                    System.out.println("Done\n\nBye !");
                }//end if
            }catch (Exception e) {
                // ignored.
            }//end try
        }//end try
    }//end main

    public static void Greeting(){
        System.out.println(
                "\n\n*******************************************************\n" +
                "              User Interface                      \n" +
                "*******************************************************\n");
    }//end Greeting

    /*
     * Reads the users choice given from the keyboard
     * @int
     **/
    public static int readChoice() {
        int input;
        // returns only if a correct value is given.
        do {
            System.out.print("Please make your choice: ");
            try { // read the integer, parse it and break.
                input = Integer.parseInt(in.readLine());
                break;
            }catch (Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }//end try
        }while (true);
        return input;
    }//end readChoice

    /*
     * Creates a new user with privided login, passowrd and phoneNum
     * An empty block and contact list would be generated and associated with a user
     **/
    public static void CreateUser(Messenger esql){
        try{
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();
            System.out.print("\tEnter user phone: ");
            String phone = in.readLine();

            //Creating empty contact\block lists for a user
            esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('block')");
            int block_id = esql.getCurrSeqVal("user_list_list_id_seq");
            esql.executeUpdate("INSERT INTO USER_LIST(list_type) VALUES ('contact')");
            int contact_id = esql.getCurrSeqVal("user_list_list_id_seq");

            String query = String.format("INSERT INTO USR (phoneNum, login, password, block_list, contact_list) VALUES ('%s','%s','%s',%s,%s)", phone, login, password, block_id, contact_id);

            esql.executeUpdate(query);
            System.out.println ("User successfully created!");
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    /*
     * Check log in credentials for an existing user
     * @return User login or null is the user does not exist
     **/
    public static String LogIn(Messenger esql){
        try{
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            login.replace("'","\\'");
            System.out.print("\tEnter user password: ");
            String password = in.readLine();

            String query = String.format("SELECT * FROM Usr WHERE login = '%s' AND password = '%s'", login, password);
            int userNum = esql.executeQuery(query);
            if (userNum > 0)
                return login;
            return null;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    //Message menu
    public static void Messages(Messenger esql, String login){
        try{
            boolean keepIn=true;
            while (keepIn){
                System.out.println("Messages Menu");
                System.out.println("---------");
                System.out.println("1. Write a new message");
                System.out.println("2. Edit message");
                System.out.println("3. Delete message");
                System.out.println(".........................");
                System.out.println("0. Back");
                switch (readChoice()){
                    case 1: CreateMsgOpt(esql, login);break;
                    case 2: EditMsgOpt(esql, login);break;
                    case 3: DeleteMsgOpt(esql, login);break;
                    case 0: keepIn=false;break;
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    //Chat menu
    public static void Chats(Messenger esql, String login){
        try{
            boolean keepIn=true;
            while (keepIn){
                System.out.println("Chats Menu");
                System.out.println("---------");
                System.out.println("1. Browse your chat list");
                System.out.println("2. Browse chat messages");
                System.out.println("3. Browse the chat members");
                System.out.println("4. Create a group chat");
                System.out.println("5. Create a private chat");
                System.out.println("6. Add members to chat");                
                System.out.println("7. Delete members from chat");
                System.out.println("8. Delete chat");
                System.out.println(".........................");
                System.out.println("0. Back");
                switch (readChoice()){
                    case 1:ListChat(esql, login); break;
                    case 2:System.out.println("Please input the chat(id) that you want to enter in:");
                           ListChatMsgs(esql, login, readChoice());
                           break;
                    case 3:System.out.println("Please input the chat_id:");
                           ListChatMembers(esql, login, readChoice());
                           break;
                    case 4:CreateGroupChat(esql, login); break;
                    case 5:CreatePrivateChat(esql, login); break;
                    case 6:System.out.print("Please input the chat(id) you want to add member(s) in :");
                           addToChat(esql, login, readChoice());
                           break;
                    case 7:System.out.print("Please input the chat(id) you want to delete member(s) from :");
                           deleteFromChat(esql, login, readChoice());
                           break;
                    case 8:System.out.print("Please input the chat(id) you want to delete");
                           DeleteChat(esql, login, readChoice());
                           break;
                    case 0: keepIn=false;break;
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    //Use menu
    public static void User(Messenger esql, String login){
        try{
            boolean keepIn=true;
            while (keepIn){
                System.out.println("User Menu");
                System.out.println("---------");
                System.out.println("1. Browse your contact list");
                System.out.println("2. Browse your block list");
                System.out.println("3. Add user to your contact list");
                System.out.println("4. Add user to your block list");
                System.out.println("5. Delete user from your contact list");
                System.out.println("6. Delete user from your block list");
                System.out.println(".........................");
                System.out.println("0. Back");
                switch (readChoice()){
                    case 1: ListContacts(esql, login);break;
                    case 2: ListBlocks(esql, login); break;
                    case 3: addToContact(esql, login); break;
                    case 4: addToBlock(esql, login);break;
                    case 5: DeleteContact(esql, login); break;
                    case 6: DeleteBlock(esql, login);break;
                    case 0: keepIn=false;break;
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    //Delete account
    public static boolean DeleteAccount(Messenger esql, String user){
        try{
            System.out.print("Are you sure you want to delete your account?(y/n)");
            String opt = in.readLine();

            if(opt.equals("y")){
                String query = String.format("DELETE FROM USR WHERE USR.login='%s'", user);
                esql.executeUpdate(query);
                System.out.println("You have deleted your own account!");
                return true;
            }

            if(opt.equals("n")){
                return false;
            }

            System.out.println("Invalid option! Please choose again:(y/n)");
            DeleteAccount(esql, user);
            return false;   
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return false;
        }
    }

    /* Menu for user to choose which kind of chat he/she would like to send message
    */
    public static void CreateMsgOpt(Messenger esql, String user){
        try{
            boolean keepIn=true;
            List<String> chat_id_list;
            int chat_id_opt;
            while (keepIn){
                System.out.println("What type of chats do you want to send msg to?");
                System.out.println("1. group");
                System.out.println("2. private");
                System.out.println(".........................");
                System.out.println("0. Back");

                switch (readChoice()){
                    case 1: chat_id_list=browseChats(esql, user, "group"); 
                            chat_id_opt=ChatsOpt(esql, user, chat_id_list);
                            if (chat_id_opt == 0){
                                break;
                            }
                            CreateMsg(esql, user, chat_id_opt); break;
                    case 2: chat_id_list=browseChats(esql, user, "private"); 
                            chat_id_opt=ChatsOpt(esql, user, chat_id_list);
                            if (chat_id_opt == 0){
                                break;
                            }
                            CreateMsg(esql, user, chat_id_opt); break;

                    case 0: keepIn = false; break;
                    default : System.out.println("Unrecognized choice!"); break;
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());   
        }
    }//end

    //return the input chat_id
    public static int ChatsOpt(Messenger esql, String user, List<String> chat_id_list){
        try{
            if (chat_id_list.size() == 0){
                System.out.println("No chat exists!");
                return 0;
            }
            for (int i=0; i<chat_id_list.size(); ++i)
            {
                System.out.print(String.format("%d. ", i+1));
                List<String> member = browseChatMember(esql, user, Integer.parseInt(chat_id_list.get(i)));
                for(int j=0; j<member.size()-1; ++j)
                    System.out.print(member.get(j).trim()+", ");
                System.out.println(member.get(member.size()-1).trim());
            }
            boolean keepIn=true;
            while (keepIn){
                System.out.print("\tEnter the chat num(0 for quit): ");
                String input = in.readLine();
                //error here
                int chat_id_opt = Integer.parseInt(input);
                if (chat_id_opt>=1 && chat_id_opt<=chat_id_list.size()){
                    return Integer.parseInt(chat_id_list.get(chat_id_opt-1));
                }
                else if (chat_id_opt == 0)
                {
                    return 0;
                }
                else{
                    System.out.println("Invalid chat_id! Please type again: ");
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return 0;
        }
        return 0;
    }//end

    //Menu for mesaage editing
    public static void EditMsgOpt(Messenger esql, String user){
        try{
            boolean keepIn=true;
            List<String> chat_id_list;
            List<String> msg_id_list;
            int chat_id_opt;
            int msg_id_opt;
            while (keepIn){
                System.out.println("What type of chats do you want to Enter in?");
                System.out.println("1. group");
                System.out.println("2. private");
                System.out.println(".........................");
                System.out.println("0. Back");

                switch (readChoice()){
                    case 1: chat_id_list=browseChats(esql, user, "group"); 
                            chat_id_opt=ChatsOpt(esql, user, chat_id_list);
                            if (chat_id_opt == 0){
                                break;
                            }
                            msg_id_list=ListMsgs(esql, user, chat_id_opt);
                            msg_id_opt=MsgsOpt(esql, user, msg_id_list);
                            if (msg_id_opt == 0){
                                break;
                            }
                            EditMsg(esql, user, msg_id_opt); break;
                    case 2: chat_id_list=browseChats(esql, user, "private"); 
                            chat_id_opt=ChatsOpt(esql, user, chat_id_list);
                            if (chat_id_opt == 0){
                                break;
                            }
                            msg_id_list=ListMsgs(esql, user, chat_id_opt);
                            msg_id_opt=MsgsOpt(esql, user, msg_id_list);
                            if (msg_id_opt == 0){
                                break;
                            }
                            EditMsg(esql, user, msg_id_opt); break;

                    case 0: keepIn = false; break;
                    default : System.out.println("Unrecognized choice!"); break;
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());   
        }
    }

    //Return the message id of choice
    public static int MsgsOpt(Messenger esql, String user, List<String> msg_id_list){
        try{
            if (msg_id_list.size() == 0){
                System.out.println("No msg from you!");
                return 0;
            }

            boolean keepIn=true;
            while (keepIn){
                System.out.print("\tEnter the msg num(0 for quit): ");
                String input = in.readLine();
                int msg_id_opt = Integer.parseInt(input);
                if (msg_id_opt>=1 && msg_id_opt<=msg_id_list.size()){
                    keepIn = false;
                    return Integer.parseInt(msg_id_list.get(msg_id_opt-1));
                }
                else if (msg_id_opt == 0)
                {
                    return 0;
                }
                else{
                    System.out.println("Invalid msg_id! Please type again: ");
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return 0;
        }
        return 0;
    }//end

    //final step of editing message
    public static void EditMsg(Messenger esql, String user, int msg_id_opt){
        try{
            System.out.print("Input your new msg: ");
            String msg_text = in.readLine();
            String query = String.format("UPDATE MESSAGE SET msg_text='%s', msg_timestamp=CURRENT_TIMESTAMP WHERE msg_id=%d", msg_text, msg_id_opt);
            esql.executeUpdate(query);
            System.out.println("The message has been edited!");
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }

    /* Final step of sending message.
    */
    public static void CreateMsg(Messenger esql, String user, int chat_id_opt){
        try{
            System.out.print("Input your msg: ");
            String msg_text = in.readLine();
            String query = String.format("INSERT INTO MESSAGE(msg_text, msg_timestamp, sender_login, chat_id) VALUES('%s', CURRENT_TIMESTAMP, '%s', %d)", msg_text, user, chat_id_opt);
            esql.executeUpdate(query);
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    //Messagg-delete menu
    public static void DeleteMsgOpt(Messenger esql, String user){
        try{
            boolean keepIn=true;
            List<String> chat_id_list;
            List<String> msg_id_list;
            int chat_id_opt;
            int msg_id_opt;
            while (keepIn){
                System.out.println("What type of chats do you want to Enter in?");
                System.out.println("1. group");
                System.out.println("2. private");
                System.out.println(".........................");
                System.out.println("0. Back");

                switch (readChoice()){
                    case 1: chat_id_list=browseChats(esql, user, "group"); 
                            chat_id_opt=ChatsOpt(esql, user, chat_id_list);
                            if (chat_id_opt == 0){
                                break;
                            }
                            msg_id_list=ListMsgs(esql, user, chat_id_opt);
                            msg_id_opt=MsgsOpt(esql, user, msg_id_list);
                            if (msg_id_opt == 0){
                                break;
                            }
                            DeleteMsg(esql, user, msg_id_opt); break;
                    case 2: chat_id_list=browseChats(esql, user, "private"); 
                            chat_id_opt=ChatsOpt(esql, user, chat_id_list);
                            if (chat_id_opt == 0){
                                break;
                            }
                            msg_id_list=ListMsgs(esql, user, chat_id_opt);
                            msg_id_opt=MsgsOpt(esql, user, msg_id_list);
							if (msg_id_opt == 0){
                                break;
							}
                            DeleteMsg(esql, user, msg_id_opt); break;

                    case 0: keepIn = false; break;
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());   
        }
    }//end

    //Delete message
    public static void DeleteMsg(Messenger esql, String user, int msg_id_opt){
        try{
            /*
               System.out.print("Input the msg id you want to delete: ");
               String msg_text = in.readLine();*/
            String query = String.format("DELETE FROM MESSAGE WHERE msg_id=%d", msg_id_opt);
            esql.executeUpdate(query);
            System.out.println("The message has been deleted!");
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }

    /* Create group chat
    */
    public static void CreateGroupChat(Messenger esql, String login){
        try{
            esql.executeUpdate(String.format("INSERT INTO chat(chat_type,init_sender) VALUES ('group','%s')",login));
            int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
            String query = String.format("INSERT INTO chat_list VALUES(%d,'%s')",chat_id,login);
            esql.executeUpdate(query);
            addToChat(esql, login, chat_id);
            System.out.println(String.format("You have created the chat, chat_id is %d", chat_id));
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    /* Create private chat
    */
    public static void CreatePrivateChat(Messenger esql, String login){
        try{
            esql.executeUpdate(String.format("INSERT INTO chat(chat_type,init_sender) VALUES ('private','%s')",login));
            int chat_id = esql.getCurrSeqVal("chat_chat_id_seq");
            List<List<String>> contact = browseContact(esql, login, "contact");
            int i = 0;
            System.out.println("-------------------------");
            System.out.println("No.\tContacts");
            System.out.println(".........................");                
            while(i < contact.size()){
                System.out.println(String.format("%d. %s", i+1, contact.get(i).get(0).trim()));
                contact.get(i).get(0).replace("'","\\'");
                ++i;
            }
            System.out.println(".........................");
            System.out.println("0. Back");
            System.out.print("Select the contact you would like to chat with(0 for quit):");
            int choice = readChoice();
            if(choice==0) return;
            if(choice<0 || choice>contact.size()){
                System.out.println("Unrecognized Choice.");
                return;
            }
            String query1 = String.format("INSERT INTO chat_list VALUES(%d,'%s')",chat_id,login);
            String query2 = String.format("INSERT INTO chat_list VALUES(%d,'%s')",chat_id, contact.get(choice-1).get(0).trim());
            esql.executeUpdate(query1);
            esql.executeUpdate(query2);
            System.out.println(String.format("You have created a private chat between you and %s, the chat's id is %d", contact.get(choice-1).get(0).trim(), chat_id));
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    /* Print chat id and members
    */
    public static void ListChat(Messenger esql, String login){
        try{
            List<String> privateChat = browseChats(esql, login, "private");
            List<String> groupChat = browseChats(esql, login, "group");
            System.out.println("No. chat_id\tMembers");
            System.out.println("--------------------");
            System.out.println("Private chat");
            int i = 0;
            String chat_id;
            while (i < privateChat.size()){
                chat_id = privateChat.get(i).trim();
                List<String> chatMember = browseChatMember(esql,login,Integer.parseInt(chat_id));
                System.out.println(String.format("%d. %s\t%s,%s",++i, chat_id, chatMember.get(0), chatMember.get(1)));
            }
            System.out.println("--------------------");
            System.out.println("Group chat");
            i = 0;
            while(i < groupChat.size()){
                chat_id = groupChat.get(i).trim();
                List<String> chatMember = browseChatMember(esql,login,Integer.parseInt(chat_id));
                int j = 0;
                String output = String.format("%d. %s\t", ++i, chat_id);
                while(j<chatMember.size() && j<3)
                    output += "\t" + chatMember.get(j++).trim();
                if(chatMember.size()>3) output += "...";
                System.out.println(output);
            }
            System.out.println(".......................");
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    /* Print 10 msg each time of a chat
    */
    public static void ListChatMsgs(Messenger esql, String user, int chat_id){
        if(!chatAccessPermission(esql, user, chat_id)){
            System.out.println("Action denied: You are not a member of this chat");
            return;
        }
        try{
            String query = String.format("SELECT msg_id, msg_text, msg_timestamp, sender_login FROM message WHERE chat_id=%d ORDER BY msg_timestamp DESC;",chat_id);
            List<List<String>> msgList = esql.executeQueryAndReturnResult(query);
            for (int i = 0; i < msgList.size(); i+=10){
                for(int j = 0; j<10 && j+i<msgList.size(); ++j){
                    System.out.print(String.format("ID:%s Time:%s Sender:%s\n%s\n\n", msgList.get(i+j).get(0), msgList.get(i+j).get(2),msgList.get(i+j).get(3), msgList.get(i+j).get(1)));
                }
                System.out.print("Next 10 messages? (y/n):");
                String choice = in.readLine();
                if(choice.equals("n")) return;
                else if (!choice.equals("y")){
                    System.out.println("Unrecognized choice");
                    return;
                }
            }
            System.out.println("Tis is all messages in the chat");
            return;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    /* Print the list of members of the chat
    */
    public static void ListChatMembers(Messenger esql, String login, int chat_id){
        if(!chatAccessPermission(esql, login, chat_id)){
            System.out.println("Action denied: You are not a member of this chat");
            return;
        }
        try{
            List<String> chatMembers= browseChatMember(esql, login, chat_id);
            int i = 0;
            System.out.println("No.\t Members");
            while (i < chatMembers.size()){
                System.out.println(String.format("%d. %s",i+1, chatMembers.get(i)));
                ++i;
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    /* Return a list of String of the id of chat for certain chat_type 
    */
    public static List<String> browseChats(Messenger esql, String login, String chat_type){
        try{
            String query = chat_type.equals("group")? String.format("SELECT chat_id FROM chat_list WHERE member='%s' and chat_id in (SELECT chat_id FROM chat WHERE chat_type='group')", login): String.format("SELECT chat_id FROM chat_list WHERE member='%s' and chat_id in( (SELECT chat_id FROM chat_list WHERE chat_id in ( SELECT chat_id FROM chat WHERE chat_type='private')) EXCEPT (SELECT chat_id FROM chat_list WHERE chat_id in(SELECT chat_id FROM chat WHERE chat_type='private') and member in (SELECT list_member FROM user_list_contains WHERE list_id=(SELECT block_list FROM usr WHERE login='%s'))))",login, login);
            List<List<String>> chatList = esql.executeQueryAndReturnResult(query);
            int i = 0;
            List<String> result= new ArrayList<String>();
            while(i<chatList.size())
                result.add(chatList.get(i++).get(0).trim());
            return result;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    /* Add new member to chat that you initiate.
     * User can only add people that in their contact list
     */
    public static void addToChat(Messenger esql, String login,int chat_id){
        if(!chatEditPermission(esql, login, chat_id)){
            System.out.println("Permission denied: you are not the initial sender of the chat");
            return;
        }
        if(!isChatGroup(esql, login, chat_id)){
            System.out.println("Action denied: you can not add members to a private chat, you could start a new group chat");
            return;
        }
        try{
            boolean keepIn=true;
            while(keepIn){
                String query = String.format("SELECT list_member FROM user_list_contains WHERE list_id=(SELECT contact_list FROM usr WHERE login ='%s') and list_member not in (SELECT member FROM chat_list WHERE chat_id=%d)", login, chat_id);
                List<List<String>> contactNotInChat = esql.executeQueryAndReturnResult(query);
                int i = 0;
                System.out.println("-------------------------");
                System.out.println("No.\tContact not in the chat");
                System.out.println(".........................");                
                while(i < contactNotInChat.size()){
                    System.out.println(String.format("%d. %s", i+1, contactNotInChat.get(i).get(0).trim()));
                    contactNotInChat.get(i).get(0).replace("'","\\'");
                    ++i;
                }
                System.out.println(".........................");
                System.out.println("0. Back");
                System.out.println("Please choose the No. of contact you want to add");
                int choice = readChoice();
                if(choice<0 || choice>contactNotInChat.size()){
                    System.out.print("Action denied: Wrong input, please input the serial number in the menu\n.........................\n");
                }else if (choice == 0){
                    keepIn=false;
                }else{
                    String query1 = String.format("INSERT INTO chat_list(chat_id, member) VALUES(%d,'%s')",chat_id, contactNotInChat.get(choice-1).get(0).trim());
                    esql.executeUpdate(query1);
                    System.out.println(String.format("Action Permitted: You have successfully add %s to chat %d", contactNotInChat.get(choice-1).get(0).trim(), chat_id));
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    /* Delete member from a chat.
     * To do this, you need edit-authority of this chat, which means you need to be the init_sender of this chat to do this action.
     */
    public static void deleteFromChat(Messenger esql, String login, int chat_id){
        if(!chatEditPermission(esql, login, chat_id)){
            System.out.println("Permission denied: you are not the initial sender of the chat");
            return;
        }
        if(!isChatGroup(esql, login, chat_id)){
            System.out.println("Action denied: you can not delete members in a private chat, but you can delete the chat.");
            return;
        }
        try{
            boolean keepIn=true;
            while(keepIn){
                System.out.println("Member in this chat");
                System.out.println(".........................");
                List<String> memberList=browseChatMember(esql, login, chat_id);
                int i = 0;
                while(i<memberList.size()){
                    System.out.println(String.format("%d. %s", i+1, memberList.get(i)));
                    ++i;
                }
                System.out.println(".........................");
                System.out.println("0. back to the privious menu");
                System.out.println("Please choose the member you want to remove from the chat:");
                int choice = readChoice();
                if(0>choice ||choice>memberList.size())
                    System.out.print("Action denied: Wrong input, please input the serial number in the menu\n.........................\n");
                else if (choice==0)
                    keepIn=false;
                else if (login.equals(memberList.get(choice-1).trim())){
                    System.out.println("Action denied: You can not delete yourself from a chat initiated by you");  
                    System.out.println(".........................");
                }
                else{
                    String query=String.format("DELETE FROM chat_list WHERE chat_id=%d and member='%s'", chat_id, memberList.get(choice-1).replace("'","\\'"));
                    esql.executeUpdate(query);
                    System.out.println(String.format("Action Permitted: You have deleted %s from this chat", memberList.get(choice-1)));
                    System.out.println("-------------------------");
                }
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    //Delete chat
    public static void DeleteChat(Messenger esql,String login, int chat_id){
        if(!chatEditPermission(esql, login, chat_id)){
            System.out.println("Permission denied: you are not the initial sender of the chat");
            return;
        }
        try{
            System.out.print("If you delete the chat, all the message(s) in the chat will be deleted, and other members in this chat will also be kicked out.");
            System.out.print("Are you sure you want delete the chat?(y/n):");
            String choice = in.readLine();
            if(choice.equals("n"))
                System.out.print("This chat survived:)");
            else if (!choice.equals("y"))
                System.out.println("Fail to delete the chat due to unreconized choice");
            else{
                String query = String.format("DELETE FROM chat WHERE chat_id=%d",chat_id);
                esql.executeUpdate(query);
                System.out.println("Chat has been deleted.");
            }
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    /* BrowseChatMember return a list of string that contains the members' login in the chat.
    */
    public static List<String> browseChatMember(Messenger esql, String login, int chat_id){
        try{
            if(!chatAccessPermission(esql, login, chat_id)){
                System.out.println("Permission denied: you are not a member of the chat");
                return null;
            }
            String query = String.format("SELECT member FROM chat_list WHERE chat_id=%d", chat_id);
            List<List<String>> memberList = esql.executeQueryAndReturnResult(query);
            int i = 0;
            List<String> result= new ArrayList<String>();
            while(i<memberList.size())
                result.add(memberList.get(i++).get(0).trim());
            return result;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    /* Return the truth value about if the user is the initial sender of the chat
     * If return true, then the user is the initial sender and would have the authority to add\delete other contacter in the chat and even delete the chat.
     * If return false, then the user has not authority to do such things listed above.
     */
    public static boolean chatEditPermission(Messenger esql, String login, int chat_id){
        try{
            String query = String.format("SELECT * FROM chat WHERE init_sender='%s' and chat_id=%d", login, chat_id);
            int isSender=esql.executeQuery(query);
            if(isSender>0)return true;
            return false;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return false;       
        }   
    }//end

    /* Return the truth value about if the user is the member of the chat
     * If return true, then the user has the permission to browse the member in the chat and the messages in the chat.
     * If return false, then the user has no permission to do such things listed above.
     */
    public static boolean chatAccessPermission(Messenger esql, String login, int chat_id){
        try{
            String query = String.format("SELECT * FROM chat_list WHERE chat_id=%d and member='%s'", chat_id, login);
            int isMember=esql.executeQuery(query);
            if(isMember>0)return true;
            return false;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return false;       
        }   
    }//end

    /* Return the truth value about if the chat is group or not
     * If return true, then the chat could be added/deleted members
     * If return false, then the chat could not be do such thing listed above
     */
    public static boolean isChatGroup(Messenger esql, String login, int chat_id){
        try{
            String query = String.format("SELECT * FROM chat WHERE chat_id=%d and chat_type='group'", chat_id);
            int isGroup=esql.executeQuery(query);
            if(isGroup>0)return true;
            return false;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return false;       
        }   
    }//end

    //Add people to your contact list
    public static void addToContact(Messenger esql, String user){
        try{
            System.out.print("\tEnter the user login you want to add to contact list: ");
            String contact_login = in.readLine();

            if (contact_login.equals(user)){
                System.out.println("You cannot add yourself to your contact list.");
                return;
            }

            //Tell whether the user exists in user list 
            String query_usr = String.format("SELECT * FROM USR WHERE login = '%s'", contact_login);

            int userNum = esql.executeQuery(query_usr);
            if (userNum <= 0){
                System.out.println("This user does NOT exist.");
                return;
            }
            //Tell whether the user exists in contact list  
            if (isInContactList(esql, user, contact_login)){        
                System.out.println("This user already exists in your contact list.");
                return;
            }
            //If the user exists in block list    
            if (isInBlockList(esql, user, contact_login)){      
                String query_listid0 = String.format("SELECT block_list FROM USR WHERE login='%s'", user);
                int listID = Integer.parseInt(esql.executeQueryAndReturnResult(query_listid0).get(0).get(0));
                String query_delete = String.format("DELETE FROM user_list_contains WHERE list_member='%s' AND list_id='%d'", contact_login, listID);
                esql.executeUpdate(query_delete);
                System.out.println(contact_login + " is deleted from your block list!");
            }
            //Add to contact list   
            String query_listid = String.format("SELECT contact_list FROM USR WHERE login='%s'", user);
            int list_id = Integer.parseInt(esql.executeQueryAndReturnResult(query_listid).get(0).get(0));
            String query_add = String.format("INSERT INTO user_list_contains(list_id, list_member) VALUES(%d, '%s')", list_id, contact_login);
            esql.executeUpdate(query_add);
            System.out.println(contact_login+" is added to your contact list!");    
        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    //Add people to your block list
    public static void addToBlock(Messenger esql, String user){
        try{
            System.out.print("\tEnter the user login you want to add to block list: ");
            String block_login = in.readLine();

            if (block_login.equals(user)){
                System.out.println("You cannot add yourself to your block list.");
                return;
            }

            //Tell whether the user exists in user list 
            String query_usr = String.format("SELECT * FROM USR WHERE login = '%s'", block_login);

            int userNum = esql.executeQuery(query_usr);
            if (userNum <= 0){
                System.out.println("This user does NOT exist.");
                return;
            }

            //Tell whether the user exists in block list    
            if (isInBlockList(esql, user, block_login)){    
                System.out.println("This user already exists in your block list.");
                return;
            }

            //If the user exists in contact list    
            if (isInContactList(esql, user, block_login)){      
                String query_listid0 = String.format("SELECT contact_list FROM USR WHERE login='%s'", user);
                int listID = Integer.parseInt(esql.executeQueryAndReturnResult(query_listid0).get(0).get(0));
                String query_delete = String.format("DELETE FROM user_list_contains WHERE list_member='%s' AND list_id='%d'", block_login, listID);
                esql.executeUpdate(query_delete);
                System.out.println(block_login + " is deleted from your contact list!");
            }

            //Add to block list 
            String query_listid = String.format("SELECT block_list FROM USR WHERE login='%s'", user);
            int list_id = Integer.parseInt(esql.executeQueryAndReturnResult(query_listid).get(0).get(0));
            String query_add = String.format("INSERT INTO user_list_contains(list_id, list_member) VALUES(%d, '%s')", list_id, block_login);
            esql.executeUpdate(query_add);
            System.out.println(block_login + " is added to your block list!");  


        }catch(Exception e){
            System.err.println (e.getMessage ());
        }
    }//end

    //List all contacts
    public static void ListContacts(Messenger esql, String login){
        try{
            List<List<String>> contacts = browseContact(esql, login, "contact");
            int i = 0;
            System.out.println("No. User\t\tStatus Message\n-------------------------");
            while(i<contacts.size()){
                System.out.println(String.format("%d. %s\t\t%s",i+1, contacts.get(i).get(0).trim(), contacts.get(i).get(1).trim()));
                ++i;
            }
            System.out.println("-------------------------");
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }
    }//end

    //List all blocks
    public static void ListBlocks(Messenger esql, String login){
        try{
            List<List<String>> contacts = browseContact(esql, login, "block");
            int i = 0;
            System.out.println("No. User\t\tStatus Message\n-------------------------");
            while(i<contacts.size()){
                System.out.println(String.format("%d. %s\t\t%s",i+1, contacts.get(i).get(0).trim(), contacts.get(i).get(1).trim()));
                ++i;
            }
            System.out.println("-------------------------");
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;
        }

    }//end

    //Delete contact from contact list
    public static void DeleteContact(Messenger esql, String login){
        try{
            System.out.print("\tEnter the user login you want to remove from contact_list:");
            String contacter = in.readLine();
            if(isInContactList(esql, login, contacter)){
                String query = String.format("DELETE FROM user_list_contains WHERE list_member='%s' and list_id=(SELECT contact_list FROM usr WHERE login='%s')",contacter, login);
                esql.executeUpdate(query);
                System.out.println ("The user has been deleted from your contact list!");
                return;
			}
            System.out.println("This user is not in your contact list");
            return;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;     
        }
    }//end

    //Delete contact form block list
    public static void DeleteBlock(Messenger esql, String login){
        try{
            System.out.print("\tEnter the user login you want to remove from block_list:");
            String contacter = in.readLine();
            if(isInBlockList(esql, login, contacter)){
                String query = String.format("DELETE FROM user_list_contains WHERE list_member='%s' and list_id=(SELECT block_list FROM usr WHERE login='%s')",contacter, login);
                esql.executeUpdate(query);
                System.out.println ("The user has been deleted from your block list!");
                return;
			}
            System.out.println("This user is not in your block list");
            return;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return;     
        }
    }//end

    /* BrowseContact return a list of 2 string that contains the user's login and the status in the list.
     * Need to input the user's login and the type of list (contact/block) you would like to browse.
     */
    public static List<List<String>> browseContact(Messenger esql, String login, String list_type){
        try{
            String query = String.format("SELECT U2.login, U2.status FROM usr U1,usr U2,user_list_contains UL WHERE U1.login='%s' and U1.%s_list=UL.list_id and UL.list_member=U2.login",login, list_type);
            List<List<String>> memberList = esql.executeQueryAndReturnResult(query);
            int i = 0;
            while(i<memberList.size()){
                memberList.get(i).get(0).replace("'","\\'");
                memberList.get(i++).get(1).replace("'","\\'");
            }
            return memberList;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    //return list of message for certian chat
    public static List<String> ListMsgs(Messenger esql, String user, int chat_id_opt){
        try{
            String query = String.format("SELECT msg_id FROM message WHERE sender_login='%s' AND chat_id='%d'", user, chat_id_opt);
            String query_print = String.format("CREATE SEQUENCE t_seq; SELECT nextval('t_seq') AS Number, REPLACE(sender_login, ' ', '') AS Author, DATE(msg_timestamp) AS Date, SUBSTRING(msg_text, 1, 18) AS Text FROM message WHERE sender_login='%s' AND chat_id='%d'; DROP SEQUENCE t_seq", user, chat_id_opt);
            List<List<String>> msgList = esql.executeQueryAndReturnResult(query);
            esql.executeQueryAndPrintResult(query_print);
            List<String> result = new ArrayList<String>();
            for (int i = 0; i < msgList.size(); ++i)
                result.add(msgList.get(i).get(0).trim());
            return result;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public static boolean isInContactList(Messenger esql, String login, String contacter){
        try{
            String query = String.format("SELECT UL.* FROM usr, user_list_contains UL WHERE usr.login='%s' and usr.contact_list=UL.list_id and UL.list_member='%s'", login, contacter);
            int ifexist=esql.executeQuery(query);
            if(ifexist>0)return true;
            return false;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return false;       
        }
    }//end

    public static boolean isInBlockList(Messenger esql, String login, String contacter){
        try{
            String query = String.format("SELECT UL.* FROM usr, user_list_contains UL WHERE usr.login='%s' and usr.block_list=UL.list_id and UL.list_member='%s'", login, contacter);
            int ifexist=esql.executeQuery(query);
            if(ifexist>0)return true;
            return false;
        }catch(Exception e){
            System.err.println (e.getMessage ());
            return false;       
        }
    }
}//end Messenger
