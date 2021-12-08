public class AddCmd implements Command {

    IDataBase db;
    AddCmd(IDataBase db) {
        this.db = db;
    }
    @Override
    public void run(String[] args) {
        db.addQuestion(args[0], args[1], Integer.parseInt(args[2]), args[3]);
    }
}
