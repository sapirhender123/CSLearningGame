/**
 * Classes that want to be observed should implement this interface.
 */
public interface Observable {

    public void addObserver();
    public void removeObserver();
    public void notifyObservers();

}
