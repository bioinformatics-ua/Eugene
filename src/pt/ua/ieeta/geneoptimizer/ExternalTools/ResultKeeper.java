package pt.ua.ieeta.geneoptimizer.ExternalTools;

import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo Gaspar
 */
public class ResultKeeper extends Observable
{
    private Object result = null;
    private boolean failed;

    /* For asynchronous waiting objects. */
    public ResultKeeper(Observer waitingObject)
    {
        assert waitingObject != null;

        failed = false;
        addObserver(waitingObject);
    }

    /* For synchronous calls. */
    public ResultKeeper() 
    {
        failed = false;
    }

    public synchronized Object getResult()
    {

        while ((result == null) && (!failed))
            try {    wait();   }
            catch (InterruptedException ex)
            { //TODO: excepçoes
                Logger.getLogger(ResultKeeper.class.getName()).log(Level.SEVERE, null, ex);
            }

        return result;
    }

    public synchronized void setResult(Object result)
    {
        this.result = result;
        notifyAll(); //notify synchronous waiting objects
        setChanged();
//        System.out.println("Entrou no notify");
        notifyObservers(result); //notify asynchronous waiting objects
//        System.out.println("Saíu do notify");
    }

    public synchronized void setFailed()
    {
        this.failed = true;
        notifyAll(); //notify synchronous waiting objects
        setChanged();
        notifyObservers(result); //notify asynchronous waiting objects
    }

    
    public boolean isFail() //if this function is synchronized, it causes a deadlock 
                            //with "setResults" because of the call to notifyObservers 
                            //(which in turn might call this function)
    {
        return failed;
    }
}
