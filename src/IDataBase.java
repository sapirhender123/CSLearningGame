import java.sql.SQLException;

public interface IDataBase {
    public void addQuestion(String subject, String question,
                            int answer, String wrong_answers);
    public void createNewCacheForSubject(String subject) throws SQLException;
    public void deleteQuestion(String question);
    public String getQuestion();
    public int getAns(String question);
}
