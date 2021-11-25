/**
 * This class manage the collections of question, answer and subject in DB.
 * It uses the input and output handler in order to accept answers to the questions and
 * check if it correct or not.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
public class dataBase implements InputHandler, OutputHandler {

    String m_disk_url;
    String m_cache_url;
    Boolean need_flush;

    Connection disk_conn;
    Connection cache_conn;

    String cur_subject;

    public dataBase(String fileName) throws RuntimeException, SQLException {
        m_disk_url = "jdbc:sqlite:" + fileName;
        m_cache_url = "jdbc:sqlite:memory:cache;create=true";
        //m_cache_url = "jdbc:sqlite:sapir.db";

        disk_conn = DriverManager.getConnection(m_disk_url);
        createTable(disk_conn);
    }

    public void closeAllDB() {
        flushCache();

        closeDB(disk_conn);
        disk_conn = null;

        closeDB(cache_conn);
        cache_conn = null;
    }

    private void closeDB(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createTable(Connection conn) { // + parameters - tuples
        String sql = "CREATE TABLE IF NOT EXISTS Questions (\n" // read from file in loop
                + " subject text NOT NULL, \n"
                + "	identify integer PRIMARY KEY,\n" // add  PRIMARY KEY
                + " question text NOT NULL, \n"
                + " answer text NOT NULL, \n"
                + " wrong_answers text NOT NULL \n"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

//    public void createNewDB(String url){
//        try (Connection conn = DriverManager.getConnection(url)){
//            if (conn != null)  {
//                return conn;
//            }
//        } catch(SQLException e){
//            System.out.println(e.getMessage());
//        }
//
//        return null;
//    }

    /**
     * Change cache, when user change his choice
     */
    public void flushCache()
    {
        // TODO: Update disk db with cache values
        // add -> need flush? (only in the end)
        // or delete it from the cache and doing flush to the db
        String query = "DELETE FROM Questions WHERE subject = \"" + cur_subject + "\"";
        try (Statement stmt = disk_conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        String sync_query = "ATTACH DATABASE \"Test.db\" AS disk_db;" +
                "INSERT INTO disk_db.Questions " +
                "SELECT * FROM Questions WHERE subject = \"" + cur_subject + "\"";
        try (Statement stmt = cache_conn.createStatement()) {
            stmt.executeUpdate(sync_query);
            cache_conn.close();
            cache_conn = null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addQuestion(String subject, int index, String question,
                            String answer, String wrong_answers) {

        String sql = "INSERT INTO Questions(subject, identify, question, answer, wrong_answers) VALUES(?,?,?,?,?)";

        try (PreparedStatement prepareState = cache_conn.prepareStatement(sql)) {
            prepareState.setString(1, subject);
            prepareState.setInt(2, index);
            prepareState.setString(3, question);
            prepareState.setString(4, answer);
            prepareState.setString(5, wrong_answers);

            prepareState.executeUpdate();
            need_flush = true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createNewCacheForSubject(String subject) throws SQLException {
        cache_conn = DriverManager.getConnection(m_cache_url);
        createTable(cache_conn);

        cur_subject = subject;
//        String query = "ATTACH DATABASE ':memory:' AS cache_db; " +
//                "INSERT INTO cache_db.Questions " +
//                "SELECT * FROM Questions WHERE subject = \"" + subject + "\"";
        String query = "ATTACH DATABASE ':memory:' AS cache_db; " +
                "INSERT INTO cache_db.Questions " +
                "SELECT * FROM Questions WHERE subject = \"" + subject + "\"";
        try (Statement stmt = disk_conn.createStatement()) {
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteQuestion(int id) {
        String sql = "DELETE FROM Questions WHERE id = ?";
        try (PreparedStatement prepareState = disk_conn.prepareStatement(sql)) {
            // set the corresponding param
            prepareState.setInt(1, id);
            // execute the delete statement
            prepareState.executeUpdate();

            // Create new cache  - synchronize
            closeDB(cache_conn);
            createNewCacheForSubject(cur_subject);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private String fetchStringQuery(String query, String keyword, Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            stmt.execute(query);
            return rs.getString(keyword);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return "";
    }

    private String fetchStringQueryUpdateCache(String query, String keyword) {
        // Try from cache
        String res = fetchStringQuery(query, keyword, cache_conn);
        if (res.isEmpty()) {
            // Not in cache, fetch from DB
            res = fetchStringQuery(query, keyword, disk_conn);
            if (res.isEmpty()) {
                return "";
            }

            // TODO: Insert into cache
        }

        return res;
    }

    @Override
    public String getQuestion(int userChoise) {
        String query = "SELECT question FROM Questions WHERE filter = \"" + userChoise + "\"";
        return fetchStringQueryUpdateCache(query, "question");
    }

    @Override
    public String getAns(String question) {
        String query = "SELECT answer FROM Questions WHERE question = \"" + question + "\"";
        return fetchStringQueryUpdateCache(query, "answer");
    }

    @Override
    public void printString(String string) {
    }

//    public void loadInfo() {
//        String sql = "SELECT * FROM Questions";
//        try {
//            File file = new File("cashFile.txt");
//            if (file.createNewFile()) {
//                System.out.println("New one");
//            }
//        else {
//                System.out.println("File already exists.");
//        }
//        } catch (IOException e) { // file already exist
//            System.out.println("An error occurred.");
//            e.printStackTrace();
//        }
//
//        try {
//            Connection conn = DriverManager.getConnection(m_cache_url);
//            Statement stmt  = conn.createStatement();
//            ResultSet rs    = stmt.executeQuery(sql);
//
//            // loop through the result set
//            while (rs.next()) {
//                try {
//                    FileWriter writeToFile = new FileWriter("cashFile.txt");
//                    writeToFile.write(rs.getString("subject") +  "\t" +
//                            rs.getInt("id") + "\t" +
//                            rs.getString("question") +
//                            rs.getString("answer") +
//                            rs.getString("wrong_answers"));
//                    writeToFile.close();
//                    System.out.println("Successfully wrote to the file.");
//                } catch (IOException e) {
//                    System.out.println("An error occurred.");
//                    e.printStackTrace();
//                }
//            }
//        } catch (SQLException e) {
//            System.out.println(e.getMessage());
//        }
//    }
}
