package pt.ua.ieeta.geneoptimizer.WebServices;

/**
 *
 * @author ${user}
 */
public class WebServiceParameter {
    private String name;
    private String value;

    public WebServiceParameter(String _name, String _value)
    {
        this.name = _name;
        this.value = _value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }
}
