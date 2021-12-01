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
public class dataBase implements OutputHandler, IDataBase {
    private String m_fileName;
    String m_disk_url;
    String m_cache_url;
    Boolean need_flush = false;

    Connection disk_conn;
    Connection cache_conn;

    String cur_subject;
    String wrong_ans;

    public dataBase(String fileName) throws RuntimeException, SQLException {
        m_fileName = fileName;
        m_disk_url = "jdbc:sqlite:" + fileName;
        m_cache_url = "jdbc:sqlite:file::memory:?cached=shared";

        disk_conn = DriverManager.getConnection(m_disk_url);
        createTable(disk_conn, false);
    }

    public dataBase() {

    }

    public void closeAllDB() {
        if (need_flush) {
            // Closes cache db
            flushCache();
        }

        closeDB(disk_conn);
        disk_conn = null;
    }

    private void closeDB(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            printString(e.getMessage());
        }
    }

    public void createTable(Connection conn, Boolean isCache) throws SQLException { // + parameters - tuples
        String query = "CREATE TABLE IF NOT EXISTS Questions (\n" // read from file in loop
                + " subject text NOT NULL, \n"
                + " question text NOT NULL UNIQUE, \n"
                + " answer int NOT NULL, \n"
                + " wrong_answers text NOT NULL \n";

        if (isCache) {
            query += ", displayed integer DEFAULT 0 \n";
        }

        query += ")";
        conn.prepareStatement(query).execute();
    }

    /**
     * Change cache, when user change his choice
     */
    public void flushCache()
    {
        try {
            disk_conn.prepareStatement("DELETE FROM Questions WHERE subject = \"" + cur_subject + "\"").executeUpdate();

            // Close because attach creates a new connection
            closeDB(disk_conn);

            if (cache_conn == null) {
                return;
            }

            cache_conn.prepareStatement("ATTACH DATABASE '" + m_fileName + "' AS disk_db;").execute();
            cache_conn.prepareStatement(
                "INSERT INTO disk_db.Questions(subject,question,answer,wrong_answers) " +
                "SELECT subject,question,answer,wrong_answers " +
                "FROM Questions WHERE subject = \"" + cur_subject + "\""
            ).execute();

            cache_conn.close();
            cache_conn = null;

            // Re-open the connection
            disk_conn = DriverManager.getConnection(m_disk_url);
        } catch (SQLException e) {
            printString(e.getMessage());
        }
    }
    @Override
    public void addQuestion(String subject, String question,
                            int answer, String wrong_answers) {
        String sql = "INSERT INTO Questions(subject, question, answer, wrong_answers) VALUES(?,?,?,?)";

        try (PreparedStatement prepareState = cache_conn.prepareStatement(sql)) {
            prepareState.setString(1, subject);
            prepareState.setString(2, question);
            prepareState.setInt(3, answer);
            prepareState.setString(4, wrong_answers);
            wrong_ans = wrong_answers;
            prepareState.executeUpdate();
            need_flush = true;
        } catch (SQLException e) {
            printString(e.getMessage());
        }
    }

    public String getWrong_ans() throws SQLException {
        return wrong_ans;
    }

//    public String getSubject() throws SQLException {
//        // Close because attach creates a new connection
//        closeDB(disk_conn);
//
//        // Important! Cannot attach to memory database because it creates a new empty database
//        cache_conn.prepareStatement("ATTACH DATABASE '" + m_fileName + "' AS disk_db").execute();
//        cache_conn.prepareStatement("SELECT subject FROM disk_db.Questions");
//
//        disk_conn = DriverManager.getConnection(m_disk_url);
//    }

    @Override
    public void createNewCacheForSubject(String subject) throws SQLException {

        cache_conn = DriverManager.getConnection(m_cache_url);
        createTable(cache_conn, true);

        cur_subject = subject;
        try {
            // Close because attach creates a new connection
            closeDB(disk_conn);

            // Important! Cannot attach to memory database because it creates a new empty database
            cache_conn.prepareStatement("ATTACH DATABASE '" + m_fileName + "' AS disk_db").execute();
            cache_conn.prepareStatement("INSERT INTO Questions(subject, question, answer, wrong_answers) " +
                    "SELECT subject, question, answer, wrong_answers FROM disk_db.Questions WHERE subject = \"" + subject + "\"").execute();
            cache_conn.prepareStatement("DETACH DATABASE disk_db").execute();

            // Re-open the connection
            disk_conn = DriverManager.getConnection(m_disk_url);
        } catch (SQLException e) {
            printString(e.getMessage());
        }
    }
    @Override
    public void deleteQuestion(String question) {
        String sql = "DELETE FROM Questions WHERE question = ?";
        try (PreparedStatement prepareState = disk_conn.prepareStatement(sql)) {
            // set the corresponding param
            prepareState.setString(1, question);
            // execute the delete statement
            prepareState.executeUpdate();

            // Create new cache  - synchronize
            closeDB(cache_conn);
            createNewCacheForSubject(cur_subject);
        } catch (SQLException e) {
            printString(e.getMessage());
        }
    }

    private ResultSet runQueryCache(String query) throws SQLException {
        // Try from cache
        ResultSet res = cache_conn.prepareStatement(query).executeQuery();
        if (res == null || res.isClosed()) {
            // Not in cache, fetch from DB
            res = disk_conn.prepareStatement(query).executeQuery();
            if (res == null || res.isClosed()) {
                return null;
            }
        }

        return res;
    }

    @Override
    public String getQuestion() {
        String queryForQuestion = "SELECT * FROM Questions WHERE displayed = 0 ORDER BY RANDOM() LIMIT 1;";
        try {
            // check the count of the questions in the cache
            ResultSet res = runQueryCache(queryForQuestion);
            if (res == null) {
                return "";
            }

            String question = res.getString("question");
            cache_conn.prepareStatement("UPDATE Questions SET displayed = 1 WHERE question = \"" + question + "\"").execute();
            return question;
        } catch (SQLException|NullPointerException e) {
            return "";
        }
    }

    @Override
    public int getAns(String question) {
        String query = "SELECT answer FROM Questions WHERE question = \"" + question + "\"";
        try {
            ResultSet rs = runQueryCache(query);
            if (rs == null) {
                return -1;
            }
            return rs.getInt("answer");
        } catch (SQLException|NullPointerException e) {
            return -1;
        }
    }

    @Override
    public void printString(String string) {
        System.out.print(string);
    }

}
