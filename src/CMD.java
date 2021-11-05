/**
 * This class prints the feedback to the user.
 */

public class CMD implements InputHandler, OutputHandler {
    @Override
    public String getQuestion() {
        return null;
    }

    @Override
    public String getAns(String question) {
        return null;
    }

    @Override
    public String checkAns(String question, String ans) {
        return null;
    }

    // player
    // player.getRecentAnswer()  --> print == ...
}
