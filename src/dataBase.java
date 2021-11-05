/**
 * This class manage the collections of question, answer and subject in DB.
 * It uses the input and output handler in order to accept answers to the questions and
 * check if it correct or not.
 */
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
public class dataBase implements InputHandler, OutputHandler {

    public dataBase(String fileName) throws ClassNotFoundException {
        String url = "jdbc:sqlite:" + fileName;
        createNewDB(url);
        createConnection();
        createTable(url);

    }

    public void createTable(String url) {
        String sql = "CREATE TABLE IF NOT EXISTS Questions (\n"
                + " subject text NOT NULL, \n"
                + "	id integer PRIMARY KEY,\n"
                + " question text NOT NULL, \n"
                + " answer text NOT NULL, \n"
                + " wrong_answers text NOT NULL \n"
                + ");";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            // create a new table
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createNewDB(String url){
        try(Connection conn=DriverManager.getConnection(url)){
        if (conn != null)  {
            DatabaseMetaData meta=conn.getMetaData();
            System.out.println("The driver name is "+meta.getDriverName());
            System.out.println("A new database has been created.");
            }

        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
    public void createConnection() {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite:Test.db";
            // create a connection to the database
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    private Connection connect() {
        // SQLite connection string
        String url = "jdbc:sqlite:Test.db";
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    public void addQuestion(String subject, int index, String question,
                            String answer, String wrong_answers) {

        String sql = "INSERT INTO Questions(subject, id, question, answer, wrong_answers) VALUES(?,?,?,?,?)";

        try (Connection conn = this.connect();
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

        try (Connection conn = this.connect();
             PreparedStatement prepareState = conn.prepareStatement(sql)) {

            // set the corresponding param
            prepareState.setInt(1, id);
            // execute the delete statement
            prepareState.executeUpdate();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    @Override
    public String getQuestion() {
        return null;
    }

    @Override
    public String getAns(String question) {
        String sql = "SELECT answer FROM Questions WHERE question = \"" + question + "\"";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.getString("answer");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return "";
    }

    @Override
    public String checkAns(String question, String ans) {
        return null;
    }

    // implement or have an local parameter?

}
