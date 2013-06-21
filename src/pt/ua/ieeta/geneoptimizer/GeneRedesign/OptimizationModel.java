package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel.StudyMakerPanel;
import pt.ua.ieeta.geneoptimizer.Main.Project;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.PluginLoader;
import pt.ua.ieeta.geneoptimizer.WebServices.GenomeAutoDiscovery;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 *
 * Singleton class that deals with the configuration of new or running
 * optimizations.
 */
public class OptimizationModel implements Observer {

    private static volatile OptimizationModel instance = null;
    private static Vector<OptimizationRunner> runningOpts;
    private static Vector<IOptimizationPlugin> optimizationList;

    /**
     * Constructor. Its private to avoid instantiation. Use getInstance instead.
     */
    private OptimizationModel() {
        runningOpts = new Vector<OptimizationRunner>(1, 1);
        optimizationList = new Vector<IOptimizationPlugin>(7, 1);
    }

    /**
     * Singleton getInstance call.
     */
    public static OptimizationModel getInstance() {
        if (instance == null) {
            synchronized(OptimizationModel.class){
                if (instance == null){
                    instance = new OptimizationModel();
                }
            }            
        }
        return instance;
    }

    public synchronized void applyOptimizationPlugins(boolean quickOptimization) {
        if (!verifySelections()) {
            return;
        }

        assert runningOpts != null;

        Project selectedProject = ProjectManager.getInstance().getSelectedProject();
        Study selectedStudy = selectedProject.getSelectedStudy();

        Vector<IOptimizationPlugin> selectedPlugins = new Vector<IOptimizationPlugin>();
        for (IOptimizationPlugin plugin : optimizationList) {
            if (plugin.isSelected()) {
                selectedPlugins.add(plugin);
            }
        }

        /* Create new optimization. */
        OptimizationRunner newOpt = new OptimizationRunner(selectedPlugins, selectedStudy, quickOptimization);

        /* Save optimization runner to list. */
        runningOpts.removeAllElements(); // temporary! only while only a single runner at a time is supported
        runningOpts.add(newOpt);

        /* Start optimization. */
        newOpt.start();

        assert !runningOpts.isEmpty();
    }

    public synchronized void optimizationEnded(OptimizationRunner opt) {
        assert opt != null;
        assert !opt.isRunning();
        assert runningOpts.contains(opt);

        runningOpts.remove(opt);
        StudyMakerPanel.getInstance().getButtonsPanel().enableAllButtons(true);
        StudyMakerPanel.getInstance().getButtonsPanel().setRedesignButtonToStop(false);
    }

    public synchronized void stopOptimization() {
        assert runningOpts != null;
        assert !runningOpts.isEmpty();

        OptimizationRunner opt = runningOpts.firstElement();
        opt.stopOptimization();
    }

    public synchronized boolean isRunning() {
        assert runningOpts != null;

        for (OptimizationRunner r : runningOpts) {
            if (r.isRunning()) {
                return true;
            }
        }

        return false;
    }

    /* Used to update available parameters in all plugins */
    public void refreshPlugins() {
        for (IOptimizationPlugin plugin : optimizationList) {
            if (plugin.isSelected()) {
                plugin.setSelected(true);
            }
        }
    }

    @Override
    /* Update study maker panel when a new plugin is added. */
    public void update(Observable o, Object arg) {
        if (o instanceof PluginLoader) {
            Class optimizationPlugin = (Class) arg;
            for (Class c : optimizationPlugin.getInterfaces()) {
                if (c == IOptimizationPlugin.class) {
                    try {
                        /* Add plugin to study maker panel. */
                        IOptimizationPlugin plugin = (IOptimizationPlugin) optimizationPlugin.newInstance();
                        StudyMakerPanel.getInstance().addOptimizationPlugin(plugin);

                        /* Add optimization plugin to list of optimizations. */
                        optimizationList.add(plugin);

                        break;
                    }//TODO: excepçao..
                    catch (Exception ex) {
                        System.out.println("Error: " + ex.getMessage());
                    }
                }
            }
        } else if (o instanceof GenomeAutoDiscovery) {
            // update plugins parameters
            Boolean refresh = (Boolean) arg;
            if (refresh) {
                refreshPlugins();
            }
        }
    }

    /* Verify if any optimization method is selected. */
    private static boolean verifySelectedOptimizations() {
        for (IOptimizationPlugin plugin : optimizationList) {
            if (plugin.isSelected()) {
                return true;
            }
        }

        return false;
    }

    public static boolean verifySelections() {
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return false;
        }

        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return false;
        }

        return verifySelectedOptimizations();
    }

    public static void makeAnalysis() {
        if (!verifySelections()) {
            return;
        }

        Vector<IOptimizationPlugin> selectedPlugins = new Vector<IOptimizationPlugin>();
        for (IOptimizationPlugin plugin : optimizationList) {
            if (plugin.isSelected()) {
                selectedPlugins.add(plugin);
            }
        }

        Project selectedProject = ProjectManager.getInstance().getSelectedProject();
        Study selectedStudy = selectedProject.getSelectedStudy();

        /* Start analysis. */
        new AnalysisRunner(selectedPlugins, selectedStudy).start();
    }

    /* Generate a report from the selected plugins and their parameters. */
    public static OptimizationReport getStudyParameters() {
        if (!verifySelectedOptimizations()) {
            return null;
        }

        OptimizationReport report = new OptimizationReport();
        for (IOptimizationPlugin plugin : optimizationList) {
            if (plugin.isSelected()) {
                report.addOptimization(plugin.getPluginName(), plugin.getParameters(), null, null, plugin);
            }
        }

        return report;
    }

    /* Load into the study maker panel a report with a selection of plugins and parameters. */
    public static void setStudyParameters(OptimizationReport report) {
        assert report != null;

        for (IOptimizationPlugin plugin : optimizationList) {
            StudyMakerPanel.setStudyParameters(report, plugin);
        }
    }

    public Vector<IOptimizationPlugin> getOptimizationMethods() {
        return optimizationList;
    }
}
