import java.sql.SQLException;

public class GameFlow {

    private InputHandler m_in;
    private OutputHandler m_out;

    GameFlow(InputHandler in, OutputHandler out) {
        m_in = in;
        m_out = out;
    }
    public static void main(String[] args) throws SQLException {
        dataBase db = new dataBase("Test.db");
        db.createNewCacheForSubject("Sapir");
        db.addQuestion("Sapir", 4, "How fun it is from 1 to 100",
                "100", "1. 100\n2. 305");
//        db.deleteQuestion(1);
        db.closeAllDB();
        //db.deleteQuestion(1);
//        System.out.println(db.getAns("How fun it is from 1 to 10"));
        //KeyboardInHandler k= new KeyboardInHandler();
        //k.getAns("A");

        //db.getQuestion(1);
    }


}
