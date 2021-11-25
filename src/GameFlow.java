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
        db.createNewCacheForSubject("Algo");
        db.addQuestion("tal", 4, "How much I it",
                "a lot", "1. 0\n2. 60");
        String ans = db.getAns("How much I love it");
        System.out.println(ans);
        db.flushCache();
        db.createNewCacheForSubject("Networks-connection");
        db.addQuestion("Networks-connection", 4, "What is DHCP",
                "server", "1. client\n2. name of a person");
//        db.deleteQuestion(1);

        ans = db.getAns("How much I love it");
        System.out.println(ans);

        db.closeAllDB();
        //db.deleteQuestion(1);
//        System.out.println(db.getAns("How fun it is from 1 to 10"));
        //KeyboardInHandler k= new KeyboardInHandler();
        //k.getAns("A");

        //db.getQuestion(1);
    }


}
