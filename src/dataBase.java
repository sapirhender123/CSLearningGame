/**
 * This class manage the collections of question, answer and subject in DB.
 * It uses the input and output handler in order to accept answers to the questions and
 * check if it correct or not.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
public class dataBase implements InputHandler, OutputHandler {

    String m_disk_url;
    String m_cache_url;

    public dataBase(String fileName) throws ClassNotFoundException {
        m_disk_url = "jdbc:sqlite" + fileName;
        m_cache_url = "jdbc:sqlite:memory:cache;create=true";

        createNewDB(m_disk_url);
        createTable(m_disk_url);

        createNewDB(m_cache_url);
        createTable(m_cache_url);
    }

    public void createTable(String url) { // + parameters - tuples
        String sql = "CREATE TABLE IF NOT EXISTS Questions (\n" // read from file in loop
                + " subject text NOT NULL, \n"
                + "	id integer PRIMARY KEY,\n"
                + " question text NOT NULL, \n"
                + " answer text NOT NULL, \n"
                + " wrong_answers text NOT NULL \n"
                + ");";
        try (Connection conn = DriverManager.getConnection(url); // function
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createNewDB(String url){
        try(Connection conn=DriverManager.getConnection(url)){
            if (conn != null)  {
                DatabaseMetaData meta=conn.getMetaData();
            }
        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }

    public void flushCache()
    {
        // TODO: Update disk db with cache values
    }

    public void addQuestion(String subject, int index, String question,
                            String answer, String wrong_answers) {

        String sql = "INSERT INTO Questions(subject, id, question, answer, wrong_answers) VALUES(?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(m_cache_url);
             PreparedStatement prepareState = conn.prepareStatement(sql)) {
            prepareState.setString(1, subject);
            prepareState.setInt(2, index);
            prepareState.setString(3, question);
            prepareState.setString(4, answer);
            prepareState.setString(5, wrong_answers);

            prepareState.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void deleteQuestion(int id) {
        String sql = "DELETE FROM Questions WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(m_disk_url);
             PreparedStatement prepareState = conn.prepareStatement(sql)) {

            // set the corresponding param
            prepareState.setInt(1, id);
            // execute the delete statement
            prepareState.executeUpdate();

            // TODO: Update cache [ if exist in the db delete also ]

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private String fetchStringQuery(String query, String keyword, String url) {
        try (Connection conn = DriverManager.getConnection(url); // function
             Statement stmt = conn.createStatement();
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
        String res = fetchStringQuery(query, keyword, m_cache_url);
        if (res.isEmpty()) {
            // Not in cache, fetch from DB
            res = fetchStringQuery(query, keyword, m_disk_url);
            if (res.isEmpty()) {
                return "";
            }

            // TODO: Insert into cache
        }

        return res;
    }

    @Override
    public String getQuestion(int userChoise) {
        String query = "SELECT question FROM Questions WHERE filter =" + userChoise;
        return fetchStringQueryUpdateCache(query, "question");
    }

    @Override
    public String getAns(String question) {
        String query = "SELECT answer FROM Questions WHERE question = \"" + question + "\"";
        return fetchStringQueryUpdateCache(query, "answer");
    }

    @Override
    public String checkAns(String question, String ans) {
        return null;
    }

    @Override
    public void printString(String string) {
    }

    public void rawQuery(String category, String fileName, String question) {
        String query = "SELECT"+ category  +"FROM [" + fileName + "] WHERE [Question] = " + question;
    }

    public void loadInfo() {
        String sql = "SELECT * FROM Questions";
        try {
            File file = new File("cashFile.txt");
            if (file.createNewFile()) {
                System.out.println("New one");
            }
        else {
                System.out.println("File already exists.");
        }
        } catch (IOException e) { // file already exist
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try {
            Connection conn = DriverManager.getConnection(m_cache_url);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                try {
                    FileWriter writeToFile = new FileWriter("cashFile.txt");
                    writeToFile.write(rs.getString("subject") +  "\t" +
                            rs.getInt("id") + "\t" +
                            rs.getString("question") +
                            rs.getString("answer") +
                            rs.getString("wrong_answers"));
                    writeToFile.close();
                    System.out.println("Successfully wrote to the file.");
                } catch (IOException e) {
                    System.out.println("An error occurred.");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
