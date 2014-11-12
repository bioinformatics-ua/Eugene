/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.gccontent;

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
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Eduardo
 */
public class GCContentPlugin implements IOptimizationPlugin, ActionListener, DocumentListener {
    private GCContentParametersPanel parametersPanel = null;
    private ParameterSet params;
    private Genome host;
    private boolean isSelected;
    private static List<Color> colorScale = null;

    public GCContentPlugin() {
        this.params = new ParameterSet();

        this.params.addParameter(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 2));
        this.params.addParameter(GCContentPlugin.Parameter.CUSTOM_VALUE.toString(), new ParameterDetails(Float.class, Integer.valueOf(100), 0, 100));
    }

    public Enum[] getAvailableParameters() {
        return GCContentPlugin.Parameter.values();
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.isSelected = false;
            this.parametersPanel = new GCContentParametersPanel();
            initComponentListener();
        }
        return this.parametersPanel;
    }

    private void initComponentListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jCustomGCButton.addActionListener(this);
            this.parametersPanel.jMaximizeGCButton.addActionListener(this);
            this.parametersPanel.jMinimizeGCButton.addActionListener(this);
            this.parametersPanel.jCustomGCTextField.getDocument().addDocumentListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()) == null) {
            this.params.addParameter(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 2));
        }

        if (this.parametersPanel.jMaximizeGCButton.isSelected()) {
            this.params.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).setValue(Integer.valueOf(0));
        } else if (this.parametersPanel.jMinimizeGCButton.isSelected()) {
            this.params.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).setValue(Integer.valueOf(1));
        } else if (this.parametersPanel.jCustomGCButton.isSelected()) {
            this.params.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).setValue(Integer.valueOf(2));
            this.params.getParamDetails(GCContentPlugin.Parameter.CUSTOM_VALUE.toString()).setValue(new Float(this.parametersPanel.jCustomGCTextField.getText()));
        }
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

    private void updateCustomParameter() {
        if (this.params.getParamDetails(GCContentPlugin.Parameter.CUSTOM_VALUE.toString()) == null) {
            this.params.addParameter(GCContentPlugin.Parameter.CUSTOM_VALUE.toString(), new ParameterDetails(Float.class, Integer.valueOf(100), 0, 100));
        }
        if ((this.parametersPanel.jCustomGCTextField.getText() != null) && (!this.parametersPanel.jCustomGCTextField.getText().isEmpty())) {
            this.params.getParamDetails(GCContentPlugin.Parameter.CUSTOM_VALUE.toString()).setValue(new Float(this.parametersPanel.jCustomGCTextField.getText()));
        }
    }

    public Study makeSingleOptimization(Study study) {
        boolean maximizeGCcontent = true;
        if (((Integer) this.params.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).getValue()).intValue() == 0) {
            maximizeGCcontent = true;
        } else if (((Integer) this.params.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).getValue()).intValue() == 1) {
            maximizeGCcontent = false;
        }

        Gene gene = new Gene(study.getResultingGene().getName(), this.host);
        String sequence = getMaximizedSequence(study, maximizeGCcontent);
        gene.createStructure(sequence, BioStructure.Type.mRNAPrimaryStructure);
        gene.calculateAllStructures();

        Study newStudy = new Study(study.getResultingGene(), gene, new StringBuilder().append("[").append(this.host.getName()).append("]  ").append(gene.getName()).append(" (Redesigned by: GC content)").toString());

        return newStudy;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.parametersPanel.jMaximizeGCButton.setEnabled(selected);
        this.parametersPanel.jMinimizeGCButton.setEnabled(selected);
        this.parametersPanel.jCustomGCButton.setEnabled(selected);
        this.parametersPanel.jCustomGCTextField.setEnabled(selected);
    }

    public String getPluginName() {
        return "GC Content";
    }

    public String getPluginId() {
        return "GCCONTENT1";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public void setParameters(ParameterSet parameters) {
        if ((this.parametersPanel != null)
                && (parameters.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()) != null)) {
            if (((Integer) parameters.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).getValue()).intValue() == 0) {
                this.parametersPanel.jMaximizeGCButton.setSelected(true);
            } else if (((Integer) parameters.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).getValue()).intValue() == 1) {
                this.parametersPanel.jMinimizeGCButton.setSelected(true);
            } else if (((Integer) parameters.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).getValue()).intValue() == 2) {
                this.parametersPanel.jCustomGCButton.setSelected(true);
                this.parametersPanel.jCustomGCTextField.setText(Float.toString(((Float) parameters.getParamDetails(GCContentPlugin.Parameter.CUSTOM_VALUE.toString()).getValue()).floatValue()));
            }
        }

        this.params = parameters;
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    private String getMaximizedSequence(Study study, boolean getMaximumGCcontent) {
        BioStructure AAs = study.getResultingGene().getStructure(BioStructure.Type.proteinPrimaryStructure);
        GeneticCodeTable GCT = this.host.getGeneticCodeTable();

        StringBuilder finalSequence = new StringBuilder();

        for (int i = 0; i < AAs.getLength(); i++) {
            String AA = AAs.getWordAt(i);
            int currentGCcontent = -1;
            String chosenSyn = (String) GCT.getSynonymousFromAA(AA).get(0);
            for (String syn : GCT.getSynonymousFromAA(AA)) {
                if ((getGCContentOfCodon(syn) > getGCContentOfCodon(chosenSyn)) && (getMaximumGCcontent)) {
                    currentGCcontent = getGCContentOfCodon(syn);
                    chosenSyn = syn;
                } else if ((getGCContentOfCodon(syn) < getGCContentOfCodon(chosenSyn)) && (!getMaximumGCcontent)) {
                    currentGCcontent = getGCContentOfCodon(syn);
                    chosenSyn = syn;
                }
            }

            finalSequence.append(chosenSyn);
        }

        return finalSequence.toString();
    }

    private int getGCcontentOfSequence(String sequence) {
        int totalGCContent = 0;
        for (int i = 0; i < sequence.length(); i += 3) {
            totalGCContent += getGCContentOfCodon(sequence.substring(i, i + 3));
        }

        return totalGCContent;
    }

    private int getGCContentOfCodon(String codon) {
        int gcContent = 0;
        for (int i = 0; i < codon.length(); i++) {
            if ((codon.charAt(i) == 'G') || (codon.charAt(i) == 'C')) {
                gcContent++;
            }
        }

        return gcContent;
    }

    public void calculateBestScore(Study study) {
        Float max = Float.valueOf(getGCcontentOfSequence(getMaximizedSequence(study, true)));

        Float min = Float.valueOf(getGCcontentOfSequence(getMaximizedSequence(study, false)));

        int chosenValue = ((Integer) this.params.getParamDetails(GCContentPlugin.Parameter.GC_CONTENT_TYPE.toString()).getValue()).intValue();

        if (chosenValue == 0) {
            this.params.addParameter("Best Score", new ParameterDetails(Float.class, max, 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Float.class, min, 0, 0));
        } else if (chosenValue == 1) {
            this.params.addParameter("Best Score", new ParameterDetails(Float.class, min, 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Float.class, max, 0, 0));
        } else {
            float percentage = ((Float) this.params.getParamDetails(GCContentPlugin.Parameter.CUSTOM_VALUE.toString()).getValue()).floatValue();
            float numNucleotides = percentage * study.getResultingGene().getSequenceLength() * 3.0F / 100.0F;

            if ((max.floatValue() > numNucleotides) && (min.floatValue() < numNucleotides)) {
                this.params.addParameter("Best Score", new ParameterDetails(Float.class, Float.valueOf(numNucleotides), 0, 0));
                if (Math.abs(max.floatValue() - numNucleotides) > Math.abs(numNucleotides - min.floatValue())) {
                    this.params.addParameter("Worst Score", new ParameterDetails(Float.class, max, 0, 0));
                } else {
                    this.params.addParameter("Worst Score", new ParameterDetails(Float.class, min, 0, 0));
                }
            } else if (Math.abs(max.floatValue() - numNucleotides) > Math.abs(numNucleotides - min.floatValue())) {
                this.params.addParameter("Best Score", new ParameterDetails(Float.class, min, 0, 0));
                this.params.addParameter("Worst Score", new ParameterDetails(Float.class, max, 0, 0));
            } else {
                this.params.addParameter("Best Score", new ParameterDetails(Float.class, max, 0, 0));
                this.params.addParameter("Worst Score", new ParameterDetails(Float.class, min, 0, 0));
            }
        }
    }

    public float getScoreOfSequence(Study study, String sequence) {
        int sequenceGCcontent = getGCcontentOfSequence(sequence);
        float targetGCcontent = ((Float) this.params.getParamDetails("Best Score").getValue()).floatValue();
        float worstGCcontent = ((Float) this.params.getParamDetails("Worst Score").getValue()).floatValue();

        float score = 100.0F - Math.abs(sequenceGCcontent - targetGCcontent) / Math.abs(worstGCcontent - targetGCcontent) * 100.0F;

        return score;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        List resultColorVector = new ArrayList();

        BioStructure codonSequence = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);
        for (int i = 0; i < study.getResultingGene().getSequenceLength(); i++) {
            switch (getGCContentOfCodon(codonSequence.getWordAt(i))) {
                case 0:
                    resultColorVector.add(new Color(188, 199, 188));
                    break;
                case 1:
                    resultColorVector.add(new Color(140, 200, 141));
                    break;
                case 2:
                    resultColorVector.add(new Color(117, 223, 120));
                    break;
                case 3:
                    resultColorVector.add(new Color(53, 244, 63));
                    break;
                default:
                    resultColorVector.add(new Color(240, 240, 240));
            }

        }

        return resultColorVector;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 4;
            colorScale = new ArrayList(size);

            colorScale.add(new Color(188, 199, 188));
            colorScale.add(new Color(140, 200, 141));
            colorScale.add(new Color(117, 223, 120));
            colorScale.add(new Color(53, 244, 63));
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "GC content color scale";
    }

    public String getScaleMinDescription() {
        return "no G/C";
    }

    public String getScaleMaxDescription() {
        return "3 G/C";
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }
    
    public enum Parameter{
        GC_CONTENT_TYPE, 
        CUSTOM_VALUE;
    }
}
