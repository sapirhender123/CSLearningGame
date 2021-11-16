/**
 * This class manage the collections of question, answer and subject in DB.
 * It uses the input and output handler in order to accept answers to the questions and
 * check if it correct or not.
 */
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
public class dataBase implements InputHandler, OutputHandler {

    String m_url;
    public dataBase(String fileName) throws ClassNotFoundException {
        String m_url = "jdbc:sqlite:" + fileName;
        createNewDB(m_url);
        createConnection(m_url);
        createTable(m_url);

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
    public Connection createConnection(String fileUrl) {
        Connection conn = null;
        try {
            // db parameters
            String url = "jdbc:sqlite" + fileUrl;
            // create a connection to the database
            conn = DriverManager.getConnection(url);
            //System.out.println("Connection to SQLite has been established.");
        return conn;
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
        } return null;
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
    public void getAns(String question) {
        String sql = "SELECT answer FROM Questions WHERE question = \"" + question + "\"";
        try (Connection conn = this.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            //return rs.getString("answer");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

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
            Connection conn = this.connect();
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


