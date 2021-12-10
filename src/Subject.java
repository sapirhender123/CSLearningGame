import java.util.ArrayList;
import java.util.List;

//Subject is a SINGELTON
//save the list of observers && the enum

public class Subject {
    enum Choice
    {
        RIGHT, WRONG;
    }
    private static Subject obj;
    private List<IObserver> listeners=new ArrayList<>();;

    //private constructor to force use of getInstance() to create Singleton object
    private Subject(){}

    public static Subject getInstance()
    {
        if (obj==null) {
            obj = new Subject();
        }
        return obj;
    }

    public void processEvent(Choice choice){
        for(IObserver element : this.listeners) {
            element.handleEvent(choice);
        }
    }
    //Sign me to your list
    public void Subscribe(IObserver obs){
        listeners.add(obs);
    }
    public void Unsubscribe(IObserver obs){
        listeners.remove(obs);
    }
}
