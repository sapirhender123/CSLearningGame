public class PracticeCmd implements Command {
    IDataBase db;
    PracticeCmd(IDataBase db) {
        this.db = db;
    }
    public void run(String[] args) {
        db.getQuestion();
    }
}
