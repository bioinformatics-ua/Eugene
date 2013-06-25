package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pt.ua.ieeta.geneoptimizer.GUI.GenePanel.MultiSequencePanel;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 */
public class AnalysisRunner extends Thread
{
    private List<IOptimizationPlugin> selectedPlugins;
    private Study study;

    public AnalysisRunner(List<IOptimizationPlugin> selectedPlugins, Study study)
    {
        this.selectedPlugins = selectedPlugins;
        this.study = study;
    }

    @Override
    public void run()
    {
        /* Get optimizations parameters. */
        Map<IOptimizationPlugin, ParameterSet> parametersList = new HashMap<IOptimizationPlugin, ParameterSet>(selectedPlugins.size());
        List<Gene> genes = new ArrayList<Gene>();
        for (IOptimizationPlugin plugin : selectedPlugins)
        {
            parametersList.put(plugin, plugin.getParameters());
            genes.add(study.getResultingGene());
        }
        
        Study newStudy = new Study(study.getResultingGene(), genes, "[" + study.getResultingGene().getGenomeName()+"]   "+study.getResultingGene().getName()+" [Analysis]");
        List<List<Color>> colorList = new ArrayList<List<Color>>();
        List<IOptimizationPlugin> plugins = new ArrayList<IOptimizationPlugin>();

        for (IOptimizationPlugin plugin : selectedPlugins)
        {
            colorList.add(plugin.makeAnalysis(study, false));
            plugins.add(plugin);
        }

        newStudy.setProject(ProjectManager.getInstance().getSelectedProject());
        MultiSequencePanel panel = new MultiSequencePanel(newStudy, "[" + study.getResultingGene().getGenomeName()+"]   "+study.getResultingGene().getName()+" [Analysis]", colorList, plugins);
        newStudy.setPanel(panel);
        
        ProjectManager.getInstance().getSelectedProject().addNewStudy(newStudy);
    }
}
