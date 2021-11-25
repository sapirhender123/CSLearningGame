import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * This class manage the collections of question, answer and subject in DB.
 * It uses the input and output handler in order to accept answers to the questions and
 * check if it correct or not.
 */
public class dataBase implements InputHandler, OutputHandler {
    private String m_fileName;
    String m_disk_url;
    String m_cache_url;
    Boolean need_flush = false;

    Connection disk_conn;
    Connection cache_conn;

    String cur_subject;

    public dataBase(String fileName) throws RuntimeException, SQLException {
        m_fileName = fileName;
        m_disk_url = "jdbc:sqlite:" + fileName;
        m_cache_url = "jdbc:sqlite:file::memory:?cached=shared";

        disk_conn = DriverManager.getConnection(m_disk_url);
        createTable(disk_conn);
    }

    public void closeAllDB() {
        if (need_flush) {
            flushCache();
        }

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

    public void createTable(Connection conn) throws SQLException { // + parameters - tuples
         conn.prepareStatement("CREATE TABLE IF NOT EXISTS Questions (\n" // read from file in loop
                + " subject text NOT NULL, \n"
                + "	identify integer,\n" // add  PRIMARY KEY
                + " question text NOT NULL UNIQUE, \n"
                + " answer text NOT NULL, \n"
                + " wrong_answers text NOT NULL \n"
                + ")"
         ).execute();
    }

    /**
     * Change cache, when user change his choice
     */
    public void flushCache()
    {
        try {
            disk_conn.prepareStatement("DELETE FROM Questions WHERE subject = \"" + cur_subject + "\"").executeUpdate();

            cache_conn.prepareStatement("ATTACH DATABASE '" + m_fileName + "' AS disk_db;").execute();
            cache_conn.prepareStatement(
                "INSERT INTO disk_db.Questions " +
                "SELECT * FROM Questions WHERE subject = \"" + cur_subject + "\""
            ).executeUpdate();

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
        try {
            // Close because attach creates a new connection
            closeDB(disk_conn);

            // Important! Cannot attach to memory database because it creates a new empty database
            cache_conn.prepareStatement("ATTACH DATABASE '" + m_fileName + "' AS disk_db").execute();
            cache_conn.prepareStatement("INSERT INTO Questions " +
                    "SELECT * FROM disk_db.Questions WHERE subject = \"" + subject + "\"").execute();
            cache_conn.prepareStatement("DETACH DATABASE disk_db").execute();

            // Re-open the connection
            disk_conn = DriverManager.getConnection(m_disk_url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    // TODO:CHECK
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
    // TODO: NEED CHECK
    private String fetchStringQueryUpdateCache(String query, String keyword) {
        // Try from cache
        String res = fetchStringQuery(query, keyword, cache_conn);
        if (res.isEmpty()) {
            // Not in cache, fetch from DB
            res = fetchStringQuery(query, keyword, disk_conn);
            if (res.isEmpty()) {
                return "";
            }
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
