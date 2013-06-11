
package pt.ua.ieeta.geneoptimizer.PluginSystem;

/**
 *
 * @author Paulo Gaspar
 */
public enum PluginType
{
        OptimizationPlugin(IOptimizationPlugin.class);

        Class classe;
        private PluginType(Class c)
        {
            classe = c;
        }

        public String getClassName()
        {
            return classe.getName();
        }

        public Class getPluginClass()
        {
            return classe;
        }
}
