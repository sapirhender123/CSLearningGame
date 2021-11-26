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
        db.addQuestion("Algo", 4, "How much I love it",
                2, "1. 0\n2. 60");
        int ans = db.getAns("How much I love it");
        System.out.println(ans);
        db.flushCache();
        db.createNewCacheForSubject("asas");
        db.addQuestion("asas", 4, "What is DHCP",
                1, "1. client\n2. name of a person");

        ans = db.getAns("How much I love it");
        System.out.println(ans);

        ans = db.getAns("What is DHCP");
        System.out.println(ans);

        db.deleteQuestion("How much I love it");

        db.closeAllDB();

    }


}
