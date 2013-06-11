package pt.ua.ieeta.geneoptimizer.PluginSystem;

import java.util.List;

/**
 *
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class ParameterDetails {

    private Object type;
    private Object value;
    private int minRange;
    private int maxRange;    

    public ParameterDetails(Object type, Object value, int minRange, int maxRange) {
        assert type != null;
        this.type = type;
        this.value = value;
        this.minRange = minRange;
        this.maxRange = maxRange;        
    }

    public void setValue(Object value) {
        this.value = value;
    }

    
    public Object getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }

    public int getMinRange() {
        return minRange;
    }

    public int getMaxRange() {
        return maxRange;
    }
}
