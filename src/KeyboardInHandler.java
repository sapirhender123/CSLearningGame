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
    public int getAns(String string) {
        Scanner sc= new Scanner(System.in);
        //return sc.nextLine();
        return 0;
    }



    // player
    // player.getRecentAnswer()  --> print == ...
}
