
package pt.ua.ieeta.geneoptimizer.PluginSystem;

/**
 *
 * @author Paulo Gaspar
 */
public abstract interface IPlugin
{
    /* Returns a inteligeble name of the plugin. */
    public abstract String getPluginName();

    /* Returns an unique identifier of this plugin. */
    public abstract String getPluginId();
    
    /* Returns the version of the plugin. */
    public abstract String getPluginVersion();
}
