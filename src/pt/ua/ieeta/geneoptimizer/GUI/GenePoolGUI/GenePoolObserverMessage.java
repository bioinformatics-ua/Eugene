
package pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI;

/**
 *
 * @author Paulo Gaspar
 */
public class GenePoolObserverMessage 
{
    public static enum MessageType
    {
        LOADING_GENOME, PARSING_GENOME, UPDATE_PROGRESS, LOAD_COMPLETE;
    }
    
    private MessageType messageType;
    private Object information;

    public GenePoolObserverMessage(MessageType messageType, Object information)
    {
        this.messageType = messageType;
        this.information = information;
    }

    public Object getInformation()
    {
        return information;
    }

    public MessageType getMessageType()
    {
        return messageType;
    }
}
