/**
 * This class prints the feedback to the user.
 */
import java.awt.*;
import java.util.*;

public class KeyboardInHandler implements InputHandler {

    String m_answer;
    String m_question;
    dataBase m_db;
    private boolean m_invalidate; // information not valid

    /**
     *
     * @param db reading from db once so the information will be local, get information in O(1)
     */
    KeyboardInHandler(dataBase db) {

    }

    @Override
    public String getQuestion() {
        return null;
    }



    @Override
    public void getAns(String string) {
        Scanner sc= new Scanner(System.in);
        String answer= sc.nextLine();
        m_answer = answer;
    }

    @Override
    public String checkAns(String question, String ans) {
        return null;
    }



    // player
    // player.getRecentAnswer()  --> print == ...
}
