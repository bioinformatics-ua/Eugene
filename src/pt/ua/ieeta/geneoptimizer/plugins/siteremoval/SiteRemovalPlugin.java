/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.siteremoval;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Eduardo
 */
public class SiteRemovalPlugin implements IOptimizationPlugin, ActionListener, DocumentListener {
    private SiteRemovalParametersPanel parametersPanel = null;
    private boolean isSelected;
    private final String shineDalgarmoAntiSequence = "CCUCCA";
    private final String kozakSequence = "GCCACCAUGG";
    private final String mazFSequence = "ACA";
    private final String RNaseSequence = "";
    private ParameterSet params;
    private Genome host;
    private static List<Color> colorScale;

    public SiteRemovalPlugin() {
        this.params = new ParameterSet();
        this.params.addParameter(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(SiteRemovalPlugin.Parameter.KOZAK.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(SiteRemovalPlugin.Parameter.RNASE.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString(), new ParameterDetails(String.class, "", 0, 0));
    }

    public Enum[] getAvailableParameters() {
        return SiteRemovalPlugin.Parameter.values();
    }

    public String getPluginName() {
        return "Site Removal";
    }

    public String getPluginId() {
        return "SITEREMOVAL1";
    }

    public String getPluginVersion() {
        return "2.0";
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.parametersPanel = new SiteRemovalParametersPanel();
            initComponentListener();
            setSelected(false);
        }
        return this.parametersPanel;
    }

    private void initComponentListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jACACheckBox.addActionListener(this);
            this.parametersPanel.jCustomCheckBox.addActionListener(this);
            this.parametersPanel.jKozakCheckBox.addActionListener(this);
            this.parametersPanel.jRNaseCheckBox.addActionListener(this);
            this.parametersPanel.jShineCheckBox.addActionListener(this);

            this.parametersPanel.jCustomTextField.getDocument().addDocumentListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()) == null) {
            this.params.addParameter(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        }
        if (this.params.getParamDetails(SiteRemovalPlugin.Parameter.KOZAK.toString()) == null) {
            this.params.addParameter(SiteRemovalPlugin.Parameter.KOZAK.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        }
        if (this.params.getParamDetails(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString()) == null) {
            this.params.addParameter(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        }
        if (this.params.getParamDetails(SiteRemovalPlugin.Parameter.RNASE.toString()) == null) {
            this.params.addParameter(SiteRemovalPlugin.Parameter.RNASE.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        }
        if (this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString()) == null) {
            this.params.addParameter(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        }

        if (this.parametersPanel.jACACheckBox.isSelected()) {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString()).setValue(Boolean.valueOf(false));
        }

        if (this.parametersPanel.jCustomCheckBox.isSelected()) {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString()).setValue(Boolean.valueOf(false));
        }

        if (this.parametersPanel.jKozakCheckBox.isSelected()) {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.KOZAK.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.KOZAK.toString()).setValue(Boolean.valueOf(false));
        }

        if (this.parametersPanel.jRNaseCheckBox.isSelected()) {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.RNASE.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.RNASE.toString()).setValue(Boolean.valueOf(false));
        }

        if (this.parametersPanel.jShineCheckBox.isSelected()) {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()).setValue(Boolean.valueOf(false));
        }
    }

    private void updateCustomParameter() {
        if (this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString()) == null) {
            this.params.addParameter(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString(), new ParameterDetails(String.class, "", 0, 0));
        }
        if ((this.parametersPanel.jCustomTextField.getText() != null) && (!this.parametersPanel.jCustomTextField.getText().isEmpty())) {
            this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString()).setValue(this.parametersPanel.jCustomTextField.getText());
        }
        System.out.println("custom: " + (String) this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString()).getValue());
    }

    public void insertUpdate(DocumentEvent e) {
        updateCustomParameter();
    }

    public void removeUpdate(DocumentEvent e) {
        updateCustomParameter();
    }

    public void changedUpdate(DocumentEvent e) {
        updateCustomParameter();
    }

    public Study makeSingleOptimization(Study study) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.parametersPanel.jShineCheckBox.setEnabled(selected);
        this.parametersPanel.jKozakCheckBox.setEnabled(selected);
        this.parametersPanel.jACACheckBox.setEnabled(selected);
        this.parametersPanel.jRNaseCheckBox.setEnabled(selected);
        this.parametersPanel.jCustomCheckBox.setEnabled(selected);
        this.parametersPanel.jCustomTextField.setEnabled(selected);
    }

    public void setParameters(ParameterSet parameters) {
        if (this.parametersPanel != null) {
            if (parameters.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jShineCheckBox.setSelected(true);
                } else {
                    this.parametersPanel.jShineCheckBox.setSelected(false);
                }
            }
            if (parameters.getParamDetails(SiteRemovalPlugin.Parameter.KOZAK.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(SiteRemovalPlugin.Parameter.KOZAK.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jKozakCheckBox.setSelected(true);
                } else {
                    this.parametersPanel.jKozakCheckBox.setSelected(false);
                }
            }
            if (parameters.getParamDetails(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jACACheckBox.setSelected(true);
                } else {
                    this.parametersPanel.jACACheckBox.setSelected(false);
                }
            }
            if (parameters.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jShineCheckBox.setSelected(true);
                } else {
                    this.parametersPanel.jShineCheckBox.setSelected(false);
                }
            }
            if (parameters.getParamDetails(SiteRemovalPlugin.Parameter.RNASE.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(SiteRemovalPlugin.Parameter.RNASE.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jRNaseCheckBox.setSelected(true);
                } else {
                    this.parametersPanel.jRNaseCheckBox.setSelected(false);
                }
            }
            if (parameters.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jCustomCheckBox.setSelected(true);
                } else {
                    this.parametersPanel.jRNaseCheckBox.setSelected(false);
                }
            }
            if (parameters.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString()) != null) {
                this.parametersPanel.jCustomTextField.setText((String) this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString()).getValue());
            }
        }
        this.params = parameters;
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    public void calculateBestScore(Study study) {
        this.params.addParameter("Best Score", new ParameterDetails(Float.class, new Float(0.0F), 0, 0));
        this.params.addParameter("Worst Score", new ParameterDetails(Float.class, new Float(study.getResultingGene().getSequenceLength() * 3), 0, 0));
    }

    public float getScoreOfSequence(Study study, String sequence) {
        int numberOfFoundSites = 0;
        float bestScore = ((Float) this.params.getParamDetails("Best Score").getValue()).floatValue();
        float worstScore = ((Float) this.params.getParamDetails("Worst Score").getValue()).floatValue();

        boolean isSDselected = ((Boolean) this.params.getParamDetails(SiteRemovalPlugin.Parameter.SHINE_DARLGARNO.toString()).getValue()).booleanValue();
        boolean isKozakselected = ((Boolean) this.params.getParamDetails(SiteRemovalPlugin.Parameter.KOZAK.toString()).getValue()).booleanValue();
        boolean isACAselected = ((Boolean) this.params.getParamDetails(SiteRemovalPlugin.Parameter.ACA_MAZ_F.toString()).getValue()).booleanValue();
        boolean isRNASEselected = ((Boolean) this.params.getParamDetails(SiteRemovalPlugin.Parameter.RNASE.toString()).getValue()).booleanValue();
        boolean isCustomSiteSelected = ((Boolean) this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString()).getValue()).booleanValue();
        String CUSTOMsite = (String) this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString()).getValue();
        for (int i = 0; i < sequence.length(); i++) {
            if ((isSDselected) && (i + "CCUCCA".length() < sequence.length())) {
                numberOfFoundSites += getShineDalgarnoScore(sequence.substring(i, i + "CCUCCA".length()));
            }

            if ((isKozakselected) && (i + "GCCACCAUGG".length() < sequence.length())
                    && (sequence.substring(i, i + "GCCACCAUGG".length()).equals("GCCACCAUGG"))) {
                numberOfFoundSites++;
            }

            if ((isACAselected) && (i + "ACA".length() < sequence.length())
                    && (sequence.substring(i, i + "ACA".length()).equals("ACA"))) {
                numberOfFoundSites++;
            }

            if ((isRNASEselected) && (i + "".length() < sequence.length())
                    && (sequence.substring(i, i + "".length()).equals(""))) {
                numberOfFoundSites++;
            }

            if ((!isCustomSiteSelected)
                    || (CUSTOMsite == null) || (CUSTOMsite.length() <= 0) || (i + CUSTOMsite.length() >= sequence.length())
                    || (!sequence.substring(i, i + CUSTOMsite.length()).equals(CUSTOMsite))) {
                continue;
            }
            numberOfFoundSites++;
        }

        return 100.0F - Math.abs(numberOfFoundSites - bestScore) / Math.abs(bestScore - worstScore) * 100.0F;
    }

    private int getShineDalgarnoScore(String sequence) {
        assert (sequence != null);
        assert (sequence.length() == "CCUCCA".length());

        int score = 0;
        for (int i = 0; i < sequence.length(); i++) {
            score += getHidrogenBondScore(sequence.charAt(i), "CCUCCA".charAt(i));
        }

        return score >= 12 ? score : 0;
    }

    private int getHidrogenBondScore(char c1, char c2) {
        if (((c1 == 'C') && (c2 == 'G')) || ((c1 == 'G') && (c2 == 'C'))) {
            return 3;
        }
        if (((c1 == 'A') && (c2 == 'U')) || ((c1 == 'U') && (c2 == 'A'))) {
            return 2;
        }
        if (((c1 == 'G') && (c2 == 'U')) || ((c1 == 'U') && (c2 == 'G'))) {
            return 2;
        }

        return 0;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        String sequence = null;
        if (!useAlignedGene) {
            sequence = study.getResultingGene().getCodonSequence();
        } else if (study.getResultingGene().hasOrthologs()) {
            sequence = study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();
        } else if (study.getOriginalGene().hasOrthologs()) {
            sequence = study.getOriginalGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();
        }

        List resultingColorVector = new ArrayList(sequence.length());
        List<Integer> counter = new ArrayList();

        for (int i = 0; i < sequence.length(); i += 3) {
            counter.add(Integer.valueOf(0));
        }

        boolean isCustomSiteSelected = ((Boolean) this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE.toString()).getValue()).booleanValue();
        String CUSTOMsite = (String) this.params.getParamDetails(SiteRemovalPlugin.Parameter.CUSTOM_SITE_VALUE.toString()).getValue();

        for (int i = 0; i < sequence.length(); i++) {
            if (i + "CCUCCA".length() < sequence.length()) {
                int SDScore = getShineDalgarnoScore(sequence.substring(i, i + "CCUCCA".length()));
                if (SDScore > 0) {
                    for (int j = 0; j < "CCUCCA".length(); j++) {
                        counter.set((i + j) / 3, Integer.valueOf(SDScore));
                    }
                }
            }

            if ((i + "GCCACCAUGG".length() < sequence.length())
                    && (sequence.substring(i, i + "GCCACCAUGG".length()).equals("GCCACCAUGG"))) {
                for (int j = 0; j < "GCCACCAUGG".length(); j++) {
                    counter.set((i + j) / 3, Integer.valueOf(100));
                }

            }

            if ((i + "ACA".length() < sequence.length())
                    && (sequence.substring(i, i + "ACA".length()).equals("ACA"))) {
                for (int j = 0; j < "ACA".length(); j++) {
                    counter.set((i + j) / 3, Integer.valueOf(1000));
                }

            }

            if ((i + "".length() < sequence.length())
                    && (sequence.substring(i, i + "".length()).equals(""))) {
                for (int j = 0; j < "".length(); j++) {
                    counter.set((i + j) / 3, Integer.valueOf(10000));
                }

            }

            if ((!isCustomSiteSelected)
                    || (CUSTOMsite == null) || (CUSTOMsite.length() <= 0) || (i + CUSTOMsite.length() >= sequence.length())
                    || (!sequence.substring(i, i + CUSTOMsite.length()).equals(CUSTOMsite))) {
                continue;
            }
            for (int j = 0; j < CUSTOMsite.length(); j++) {
                counter.set((i + j) / 3, Integer.valueOf(100000));
            }

        }

        for (Integer i : counter) {
            switch (i.intValue()) {
                case 0:
                    resultingColorVector.add(Color.LIGHT_GRAY);
                    break;
                case 12:
                case 13:
                case 14:
                case 15:
                case 16:
                    resultingColorVector.add(Color.getHSBColor(0.666F, (i.intValue() - 11.0F) / 10.0F + 0.4F, 1.0F));
                    break;
                case 100:
                    resultingColorVector.add(Color.YELLOW);
                    break;
                case 1000:
                    resultingColorVector.add(Color.GREEN);
                    break;
                case 10000:
                    resultingColorVector.add(Color.MAGENTA);
                    break;
                case 100000:
                    resultingColorVector.add(Color.ORANGE);
            }

        }

        return resultingColorVector;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 6;
            colorScale = new ArrayList(size);

            colorScale.add(Color.LIGHT_GRAY);
            colorScale.add(Color.BLUE);
            colorScale.add(Color.YELLOW);
            colorScale.add(Color.GREEN);
            colorScale.add(Color.MAGENTA);
            colorScale.add(Color.ORANGE);
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "Site color scale";
    }

    public String getScaleMinDescription() {
        return "None, Shine-Dalgarno, Kozak, MazF, RNAse, Custom";
    }

    public String getScaleMaxDescription() {
        return "";
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }

    static {
        colorScale = null;
    }
    
    public enum Parameter {
        SHINE_DARLGARNO, 
        KOZAK, 
        ACA_MAZ_F, 
        RNASE, 
        CUSTOM_SITE, 
        CUSTOM_SITE_VALUE;
    }
}
