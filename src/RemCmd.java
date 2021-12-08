public class RemCmd implements Command {
    IDataBase db;
    RemCmd(IDataBase db) {
        this.db = db;
    }

    @Override
    public void run(String[] args) {
        db.deleteQuestion(args[0]);
    }
}
