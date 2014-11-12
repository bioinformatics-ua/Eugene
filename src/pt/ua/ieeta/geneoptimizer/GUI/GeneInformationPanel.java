package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;
import pt.ua.ieeta.geneoptimizer.GUI.GenePanel.MultiSequencePanel;
import pt.ua.ieeta.geneoptimizer.GUI.GenePanel.SingleGenePanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport.Optimization;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 */
public class GeneInformationPanel extends ContentPanel implements Observer {

    private static volatile GeneInformationPanel instance = null;
    private static JPanel content;

    private class InformationZone extends JPanel {

        public InformationZone(String title, String info) {
            this(title, info, false);
        }

        public InformationZone(String title, String info, boolean horizontal) {
            if (!horizontal) {
                this.setLayout(new GridLayout(2, 1));
            } else {
                this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            }

            JTextArea area = new JTextArea();
            area.setFont(UIManager.getFont("Label.font"));
            area.setEditable(false);
            area.setOpaque(false);
            area.setWrapStyleWord(true);
            area.setLineWrap(true);
            area.setText(info);

            add(new JLabel("<html><b>" + title + "</b></html>"));
            //add(new JLabel("<html>"+info+"</html>"));
            add(area);

            setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        }

        public InformationZone(String title, String info1, String info2) {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(new JLabel("<html><b>" + title + "</b></html>"));
            add(new JLabel("<html>" + info1 + "</html>"));
            add(new JLabel("<html>" + info2 + "</html>"));
            add(Box.createHorizontalGlue());
        }
    }

    public static GeneInformationPanel getInstance() {
        if (instance == null) {
            synchronized (GeneInformationPanel.class) {
                if (instance == null) {
                    instance = new GeneInformationPanel();
                    instance.setLayout(new BoxLayout(instance, BoxLayout.Y_AXIS));
                    content = new JPanel();
                    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                    instance.add(content);

                    JLabel label = new JLabel("<html><b>No gene selected</b></html>", JLabel.CENTER);

                    content.add(Box.createHorizontalGlue());
                    content.add(label);
                    content.add(Box.createHorizontalGlue());
                }
            }
        }

        return instance;
    }

    /**
     * Creates new form GeneInformationPanel
     */
    private GeneInformationPanel() {
        super("Gene Information Panel", false);
    }

    @Override
    public void update(Observable o, Object arg) {
        Study study = (Study) arg;

        if (study.isMultiGeneStudy()) {
            updateInformationMultiSequence((Study) arg);
        } else {
            updateInformationSingleGene((Study) arg);
        }
    }

    public void updateInformationForSelectedStudy() {
        assert ProjectManager.getInstance() != null;
        assert ProjectManager.getInstance().getSelectedProject() != null;

        Study s = ProjectManager.getInstance().getSelectedProject().getSelectedStudy();
        if ((s == null) || !s.isMultiGeneStudy()) {
            updateInformationSingleGene(ProjectManager.getInstance().getSelectedProject().getSelectedStudy());
        } else {
            updateInformationMultiSequence(ProjectManager.getInstance().getSelectedProject().getSelectedStudy());
        }
    }

    public void updateInformationSingleGene(Study study) {
        content.removeAll();

        /* When no study is selected. */
        if (study == null) {
            JLabel label = new JLabel("<html><b>No gene selected</b></html>", JLabel.CENTER);

            content.add(Box.createHorizontalGlue());
            content.add(label);
            content.add(Box.createHorizontalGlue());

            return;
        }

        content.add(new JLabel("<html><b><font color='#999999'>[Species and Gene]</font></b>"));
        content.add(new InformationZone("Genome name", study.getResultingGene().getGenome().getName()));
        content.add(new InformationZone("Gene name", study.getResultingGene().getName()));
        content.add(new JSeparator());

        /* If gene originally came from another genome, show information about that. */
        if (!study.getOriginalGene().getGenome().getName().equals(study.getResultingGene().getGenome().getName())
                || (!study.getOriginalGene().getName().equals(study.getResultingGene().getName()))) {
            content.add(new JLabel("<html><b><font color='#999999'>[Original Species and Gene]</font></b>"));
            content.add(new InformationZone("Original genome", study.getOriginalGene().getGenome().getName()));
            content.add(new InformationZone("Original gene", study.getOriginalGene().getName()));
            content.add(new JSeparator());
        }

        /* General information. */
        content.add(new JLabel("<html><b><font color='#999999'>[General information]</font></b>"));
        content.add(new InformationZone("Number of codons: ", "" + study.getResultingGene().getSequenceLength(), true));
        content.add(new InformationZone("GC content: ", "" + new DecimalFormat("##.##").format(study.getResultingGene().getGCContent() * 100) + "%", true));
        content.add(new InformationZone("Average RSCU: ", "" + new DecimalFormat("##.###").format(study.getResultingGene().getAverageRSCU()), true));
        content.add(new InformationZone("Codon Pair Bias: ", "" + new DecimalFormat("##.###").format(study.getResultingGene().getCPB()), true));
        content.add(new InformationZone("Effective Number of Codons: ", "" + new DecimalFormat("##.###").format(study.getResultingGene().getEffectiveNumberOfCodons()), true));
        if (study.getResultingGene().hasCAI()) {
            content.add(new InformationZone("CAI: ", "" + new DecimalFormat("##.###").format(study.getResultingGene().getCAI()), true));
        }
        if(study.hasCodonSelection())
            content.add(new InformationZone("CAI from selected zone: ", "" + new DecimalFormat("##.###").format(study.getResultingGene().getSelectedCAI(study.getSelectedStartIndex(), study.getSelectedEndIndex())), true));
        content.add(new JSeparator());

        /* Optimizations information. */
        if ((study.getOptimizationReports() != null) && !study.getOptimizationReports().isEmpty()) {
            content.add(new JLabel("<html><b><font color='#999999'>[Optimizations Report]</font></b>"));
            for (OptimizationReport report : study.getOptimizationReports()) {
                for (Optimization optimization : report.getOptimizations()) {
                    content.add(new InformationZone(optimization.getName() + ":  ", new DecimalFormat("##.#").format(Float.parseFloat(optimization.getResult())) + "%",
                            "(" + new DecimalFormat("##.#").format(Float.parseFloat(optimization.getImprovement())) + "%)"));
                }

                if (report != study.getOptimizationReports().get(study.getOptimizationReports().size() - 1)) {
                    content.add(new InformationZone("    +", "", true));
                }
            }

            content.add(new JSeparator());
        }

        /* Colour code information. */
        SingleGenePanel panel = (SingleGenePanel) study.getCurrentPanel();
        if (panel != null) {
            IOptimizationPlugin colouringPlugin = panel.getColouringPlugin();
            if (colouringPlugin != null) {
                JLabel description = new JLabel("<html><b><font color='#999999'>[Colour details]</font></b>");
                content.add(description);

                content.add(new InformationZone(colouringPlugin.getScaleDescription(), "", true));

                List<Color> scale = (List<Color>) colouringPlugin.colorScale();
                JPanel coloursPanel = new JPanel();
                coloursPanel.setLayout(new BoxLayout(coloursPanel, BoxLayout.X_AXIS));
                if (scale != null) {
                    for (int i = 0; i < scale.size(); i++) {
                        JPanel c = new JPanel();
                        c.setBackground(scale.get(i));
                        coloursPanel.add(c);
                    }
                }

                content.add(coloursPanel);

                JPanel descriptionPanel = new JPanel();
                descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
                JLabel minDescription = new JLabel(colouringPlugin.getScaleMinDescription());
                JLabel maxDescription = new JLabel(colouringPlugin.getScaleMaxDescription());
                Font f = new Font(Font.DIALOG, Font.PLAIN, 9);
                minDescription.setFont(f);
                maxDescription.setFont(f);
                descriptionPanel.add(minDescription);
                descriptionPanel.add(Box.createHorizontalGlue());
                descriptionPanel.add(maxDescription);

                content.add(descriptionPanel);
            }
        }

        updateUI();
    }

    private void updateInformationMultiSequence(Study study) {
        content.removeAll();

        content.add(new InformationZone("Original genome:", study.getOriginalGene().getGenome().getName()));
        content.add(new InformationZone("Original gene:", study.getOriginalGene().getName()));
        content.add(new JSeparator());

        float GCcontent = 0;
        for (Gene gene : study.getResultingGenes()) {
            GCcontent += gene.getGCContent() * 100;
        }
        GCcontent = GCcontent / study.getResultingGenes().size();

        content.add(new InformationZone("GC content:", "" + new DecimalFormat("##.##").format(GCcontent) + "%"));

        /* Colour code information. */
        MultiSequencePanel panel = (MultiSequencePanel) study.getCurrentPanel();
        if (panel != null) {
            List<IOptimizationPlugin> colouringPlugins = panel.getColouringPlugins();
            if (colouringPlugins != null) {
                /* Add a separator and a title for this section. */
                content.add(new JSeparator());
                JLabel description = new JLabel("<html><b><font color='#999999'>[Colour details]</font></b>");
                content.add(description);

                /* Add a zone for each plugins colour-description. */
                for (IOptimizationPlugin colouringPlugin : colouringPlugins) {
                    content.add(new InformationZone(colouringPlugin.getScaleDescription(), "", true));

                    List<Color> scale = (List<Color>) colouringPlugin.colorScale();
                    JPanel coloursPanel = new JPanel();
                    coloursPanel.setLayout(new BoxLayout(coloursPanel, BoxLayout.X_AXIS));
                    if (scale != null) {
                        for (int i = 0; i < scale.size(); i++) {
                            JPanel c = new JPanel();
                            c.setBackground(scale.get(i));
                            coloursPanel.add(c);
                        }
                    }

                    content.add(coloursPanel);

                    JPanel descriptionPanel = new JPanel();
                    descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.X_AXIS));
                    JLabel minDescription = new JLabel(colouringPlugin.getScaleMinDescription());
                    JLabel maxDescription = new JLabel(colouringPlugin.getScaleMaxDescription());
                    Font f = new Font(Font.DIALOG, Font.PLAIN, 9);
                    minDescription.setFont(f);
                    maxDescription.setFont(f);
                    descriptionPanel.add(minDescription);
                    descriptionPanel.add(Box.createHorizontalGlue());
                    descriptionPanel.add(maxDescription);

                    content.add(descriptionPanel);

                    content.add(Box.createVerticalStrut(5));
                }
            }
        }

        updateUI();
    }
}
