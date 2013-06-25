package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.util.ArrayList;
import java.util.List;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;

/**
 *
 * @author Paulo Gaspar
 */
public class OptimizationReport
{

    public class Optimization
    {
        private String name;
        private ParameterSet parameters;
        private String result;
        private String improvement;
        private IOptimizationPlugin plugin;

        public Optimization(String name, ParameterSet parameters, String result, String improvement, IOptimizationPlugin plugin) {
            this.name = name;
            this.parameters = parameters;
            this.result = result;
            this.improvement = improvement;
            this.plugin = plugin;
        }

        public String getName() {
            return name;
        }

        public ParameterSet getParameters() {
            return parameters;
        }

        public String getResult() {
            return result;
        }

        public String getImprovement() {
            return improvement;
        }

        public IOptimizationPlugin getPlugin() {
            return plugin;
        }                
    }

    private List<Optimization> optimizationList;
    private String reportName;
    private int order;

    public OptimizationReport()
    {
        optimizationList = new ArrayList<Optimization>();
    }
    
    public OptimizationReport(int order)
    {
        this.order = order;
        optimizationList = new ArrayList<Optimization>();
    }

    public void addOptimization(String name, ParameterSet parameters, String result, String improvement, IOptimizationPlugin plugin)
    {
        assert name != null;
        assert parameters != null;

        optimizationList.add(new Optimization(name, parameters, result, improvement, plugin));
    }

    public List<Optimization> getOptimizations()
    {
        assert optimizationList != null;

        return optimizationList;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getReportName() {
        return reportName;
    }

    public int getOrder() {        
        return order;
    }
    
    
    
}
