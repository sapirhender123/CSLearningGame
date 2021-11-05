/**
 * This interface define how to deal with the user answer.
 */

//import Observer;
public interface InputHandler {
    public String getQuestion();
    public String getAns(String question);
    public String checkAns(String question, String ans);

}
