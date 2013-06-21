package pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.Border;
import pt.ua.ieeta.geneoptimizer.GUI.ContentPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class StudyMakerPanel extends ContentPanel {

    private static boolean DEBUG = false;
    private JOutlookBar accordionPanel;
    private static volatile StudyMakerPanel instance = null;
    private static Map<String, AccordionHeader> accordionHeaderList;
    private static StudyPanelButtons buttonsPanel;

    /* Implement singleton pattern. */
    public static StudyMakerPanel getInstance() {
        if (instance == null) {
            synchronized (StudyMakerPanel.class) {
                if (instance == null) {
                    instance = new StudyMakerPanel();
                    accordionHeaderList = new HashMap<String, AccordionHeader>();
                }
            }

        }
        return instance;
    }

    private StudyMakerPanel() {
        super("Gene Optimization", false);

        /* Create accordion menu. */
//        accordionPanel = new AccordionPanel(100);
        accordionPanel = new JOutlookBar();

//        this.setLayout(new BorderLayout());
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        if (DEBUG) {
            this.setBorder(BorderFactory.createLineBorder(Color.red, 1));
        }

        /* Add host choice to the top of the layout. */
        JPanel hostSelection = HostSelectionPanel.getInstance();
        if (DEBUG) {
            hostSelection.setBorder(BorderFactory.createLineBorder(Color.green, 1));
        }
//        this.add(hostSelection, BorderLayout.NORTH);
        this.add(hostSelection);

        /* Add accordion panel to the layout. */
        JPanel redesignPlugins = new JPanel();
        redesignPlugins.setLayout(new BoxLayout(redesignPlugins, BoxLayout.Y_AXIS));
        if (DEBUG) {
            redesignPlugins.setBorder(BorderFactory.createLineBorder(Color.green, 1));
        }
        redesignPlugins.add(accordionPanel);
        if (DEBUG) {
            accordionPanel.setBorder(BorderFactory.createLineBorder(Color.blue, 1));
        }
//        this.add(accordionPanel, BorderLayout.CENTER);
//        this.add(redesignPlugins, BorderLayout.CENTER);
        this.add(redesignPlugins);

        /* Add buttons to the layout. */
        buttonsPanel = new StudyPanelButtons();
        if (DEBUG) {
            buttonsPanel.setBorder(BorderFactory.createLineBorder(Color.green, 1));
        }
//        this.add(buttonsPanel, BorderLayout.SOUTH);
        this.add(buttonsPanel);

        this.setMinimumSize(new Dimension(225, 100));
    }

    public StudyPanelButtons getButtonsPanel() {
        return buttonsPanel;
    }

    public Collection<AccordionHeader> getAccordionHeaderList() {
        return accordionHeaderList.values();
    }

    public void setPluginsGenome(Genome genome) {
        for (Map.Entry<String, AccordionHeader> key : accordionHeaderList.entrySet()) {
            key.getValue().getPlugin().setHost(genome);
        }
    }

    public void addOptimizationPlugin(IOptimizationPlugin plugin) {
        /* Create content parametersPanel to create a border around the parameters panel from the plugin. */
        JPanel parametersPanel = new JPanel();
        parametersPanel.setLayout(new BorderLayout());
        Border border = BorderFactory.createLineBorder(new Color(160, 160, 160));
        parametersPanel.setBorder(border);

        /* Add plugin's parameters panel to above created panel. */
        parametersPanel.add(plugin.getParametersPanel(), BorderLayout.CENTER);

        /* Create new menu in the accordion, and add the parameters parametersPanel. */
        AccordionHeader header = new AccordionHeader(plugin);
        accordionPanel.addBar(header, parametersPanel);
        accordionHeaderList.put(plugin.getPluginName(), header);
        //accordionPanel.add(parametersPanel, plugin.getPluginName());

        updateUI();
    }

    public static void setStudyParameters(OptimizationReport report, IOptimizationPlugin plugin) {
        assert report != null;
        assert plugin != null;

        accordionHeaderList.get(plugin.getPluginName()).setSelected(false);
        plugin.setSelected(false);
        for (OptimizationReport.Optimization opt : report.getOptimizations()) {
            if (opt.getName().equals(plugin.getPluginName())) {
                plugin.setSelected(true);
                accordionHeaderList.get(plugin.getPluginName()).setSelected(true);
                plugin.getParametersPanel().updateUI();
                plugin.setParameters(opt.getParameters());
                break;
            }
        }

        getInstance().updateUI();
    }
}
