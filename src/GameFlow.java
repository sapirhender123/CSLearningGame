
public class GameFlow {

    private InputHandler m_in;
    private OutputHandler m_out;

    GameFlow(InputHandler in, OutputHandler out) {
        m_in = in;
        m_out = out;
    }
    public static void main(String[] args) throws ClassNotFoundException {
        dataBase db = new dataBase("Test.db");
        db.addQuestion("Algo", 1, "How fun it is from 1 to 10",
                "1", "1. 10\n2. 35");
        //db.deleteQuestion(1);
//        System.out.println(db.getAns("How fun it is from 1 to 10"));
        db.loadInfo();
        //KeyboardInHandler k= new KeyboardInHandler();
        //k.getAns("A");
    }
}
