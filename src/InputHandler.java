/**
 * This interface define how to deal with the user answer.
 */

//import Observer;
public interface InputHandler {
    public String getQuestion(int userChoice);
    public int getAns(String question);


}
