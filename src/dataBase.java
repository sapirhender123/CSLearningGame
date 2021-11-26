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
                + " answer int NOT NULL, \n"
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

            cache_conn.prepareStatement("ATTACH DATABASE '" + m_fileName + "' AS disk_db").execute();
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
                            int answer, String wrong_answers) {
        String sql = "INSERT INTO Questions(subject, identify, question, answer, wrong_answers) VALUES(?,?,?,?,?)";

        try (PreparedStatement prepareState = cache_conn.prepareStatement(sql)) {
            prepareState.setString(1, subject);
            prepareState.setInt(2, index);
            prepareState.setString(3, question);
            prepareState.setInt(4, answer);
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
            System.out.println(e.getMessage());
        }
    }

//    private int fetchStringQueryInt(String query, int keyword, Connection conn) {
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//            return rs.getInt(keyword);
//        } catch (SQLException e) {
//            if (!e.getMessage().equals("ResultSet closed")) {
//                System.out.println(e.getMessage());
//            }
//        }
//
//        return 0;
//    }
//
//    /**
//     * Get a query from the cache or the db
//     * @param query the sql query
//     * @param keyword the result of the query
//     * @return the sql answer (from the cache or db)
//     */
//    private int fetchIntQueryAttemptCache(String query, int keyword) {
//        // Try from cache
//        int res = fetchStringQueryInt(query, keyword, cache_conn);
//        if (res == 0) {
//            // Not in cache, fetch from DB
//            res = fetchStringQueryInt(query, keyword, disk_conn);
//            if (res == 0) {
//                return 0;
//            }
//        }
//
//        return res;
//    }
//
//    private String fetchStringQuery(String query, String keyword, Connection conn) {
//        try (Statement stmt = conn.createStatement();
//             ResultSet rs = stmt.executeQuery(query)) {
//            return rs.getString(keyword);
//        } catch (SQLException e) {
//            if (!e.getMessage().equals("ResultSet closed")) {
//                System.out.println(e.getMessage());
//            }
//        }
//
//        return "";
//    }

    private ResultSet runQuery(String query, Connection conn) {
        try {
            return conn.prepareStatement(query).executeQuery();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return null;
    }

    private ResultSet runQueryCache(String query) throws SQLException {
        // Try from cache
        ResultSet res = runQuery(query, cache_conn);
        if (res == null || res.isClosed()) {
            // Not in cache, fetch from DB
            res = runQuery(query, disk_conn);
            if (res == null || res.isClosed()) {
                return null;
            }
        }

        return res;
    }

    @Override
    public String getQuestion(int userChoise) {
        String query = "SELECT question FROM Questions WHERE filter = \"" + userChoise + "\"";
        try {
            ResultSet rs = runQueryCache(query);
            if (rs == null) {
                return "";
            }
            return rs.getString("question");
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
    }
}
