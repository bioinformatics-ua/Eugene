
package pt.ua.ieeta.geneoptimizer.PluginSystem;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Paulo Gaspar
 */
public class ParameterSet 
{
    private Map<String, ParameterDetails> paramList;

    public ParameterSet()
    {
        paramList = new HashMap<String, ParameterDetails>();
    }
    
    /* Constructor. The initialCapacity allows pre-allocating space. */
    public ParameterSet(int initialCapacity)
    {
        paramList = new HashMap<String, ParameterDetails>(initialCapacity);
    }
    
    /* Add new parameter to the set. A parameter has a name (key) and a value. */
    public void addParameter(String name, ParameterDetails pDetails)
    {
        assert name != null;
        assert pDetails != null;
        assert paramList != null;
        
        paramList.put(name, pDetails);
    }
    
    /* Obtain the parameter details for a parameter with key name. */
    public ParameterDetails getParamDetails(String name)
    {
        assert name != null;
        assert paramList != null;
        
        /* Key doesn't exist, return null. */
        if  (!paramList.containsKey(name)) return null;
        
        return paramList.get(name);
    }

    public Map<String, ParameterDetails> getParamList() {
        return paramList;
    }
}
