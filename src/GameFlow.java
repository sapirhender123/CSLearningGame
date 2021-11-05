
public class GameFlow {
    public static void main(String[] args) throws ClassNotFoundException {
        dataBase db = new dataBase("Test.db");
        db.addQuestion("Algo", 1, "How fun it is from 1 to 10",
                "1", "1. 10\n2. 35");
        //db.deleteQuestion(1);
        System.out.println(db.getAns("How fun it is from 1 to 10"));
    }
}
