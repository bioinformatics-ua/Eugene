/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.repeatsremoval;

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
public class RepeatsRemovalPlugin implements IOptimizationPlugin, ActionListener, DocumentListener{
    private RepeatsRemovalParametersPanel parametersPanel = null;
    private ParameterSet params;
    private boolean isSelected;
    private Genome host;
    private static List<Color> colorScale;

    public RepeatsRemovalPlugin() {
        this.params = new ParameterSet();

        this.params.addParameter(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_NUCLEOT.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString(), new ParameterDetails(Integer.class, Integer.valueOf(5), 2, 10));
        this.params.addParameter(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_CODON.toString(), new ParameterDetails(Integer.class, Integer.valueOf(2), 2, 10));
    }

    public Enum[] getAvailableParameters() {
        return RepeatsRemovalPlugin.Parameter.values();
    }

    public String getPluginName() {
        return "Repeats Removal";
    }

    public String getPluginId() {
        return "REPEATSREMOVAL1";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.parametersPanel = new RepeatsRemovalParametersPanel();
            initComponentListener();
            setSelected(false);
        }
        return this.parametersPanel;
    }

    private void initComponentListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jRemoveRepeatCodonlButton.addActionListener(this);
            this.parametersPanel.jRemoveRepeatNuclButton.addActionListener(this);
            this.parametersPanel.jRemoveRepeatCodonTextField.getDocument().addDocumentListener(this);
            this.parametersPanel.jRemoveRepeatNuclTextField.getDocument().addDocumentListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_NUCLEOT.toString()) == null) {
            this.params.addParameter(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_NUCLEOT.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        }
        if (this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString()) == null) {
            this.params.addParameter(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        }
        if (this.parametersPanel.jRemoveRepeatNuclButton.isSelected()) {
            this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_NUCLEOT.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_NUCLEOT.toString()).setValue(Boolean.valueOf(false));
        }
        if (this.parametersPanel.jRemoveRepeatCodonlButton.isSelected()) {
            this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString()).setValue(Boolean.valueOf(false));
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
        if (this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString()) == null) {
            this.params.addParameter(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString(), new ParameterDetails(Integer.class, Integer.valueOf(5), 0, 0));
        }
        if (this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString()) == null) {
            this.params.addParameter(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_CODON.toString(), new ParameterDetails(Integer.class, Integer.valueOf(5), 2, 0));
        }
        if ((this.parametersPanel.jRemoveRepeatCodonTextField.getText() != null) && (!this.parametersPanel.jRemoveRepeatCodonTextField.getText().isEmpty())) {
            this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_CODON.toString()).setValue(new Integer(this.parametersPanel.jRemoveRepeatCodonTextField.getText()));
        }
        if ((this.parametersPanel.jRemoveRepeatNuclTextField.getText() != null) && (!this.parametersPanel.jRemoveRepeatNuclTextField.getText().isEmpty())) {
            this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString()).setValue(new Integer(this.parametersPanel.jRemoveRepeatNuclTextField.getText()));
        }
    }

    public Study makeSingleOptimization(Study study) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.parametersPanel.jRemoveRepeatNuclButton.setEnabled(selected);
        this.parametersPanel.jRemoveRepeatCodonlButton.setEnabled(selected);
        this.parametersPanel.jRemoveRepeatNuclTextField.setEnabled(selected);
        this.parametersPanel.jRemoveRepeatCodonTextField.setEnabled(selected);
    }

    public void setParameters(ParameterSet parameters) {
        if (this.parametersPanel != null) {
            if (parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_NUCLEOT.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_NUCLEOT.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jRemoveRepeatNuclButton.setSelected(true);
                } else {
                    this.parametersPanel.jRemoveRepeatNuclButton.setSelected(false);
                }
            }
            if (parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString()) != null) {
                this.parametersPanel.jRemoveRepeatNuclTextField.setText(Integer.toString(((Integer) parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString()).getValue()).intValue()));
            }
            if (parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.REMOVE_REPEATED_CODONS.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jRemoveRepeatCodonlButton.setSelected(true);
                } else {
                    this.parametersPanel.jRemoveRepeatCodonlButton.setSelected(false);
                }
            }
            if (parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_CODON.toString()) != null) {
                this.parametersPanel.jRemoveRepeatCodonTextField.setText(Integer.toString(((Integer) parameters.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_CODON.toString()).getValue()).intValue()));
            }
        }
        this.params = parameters;
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    public void calculateBestScore(Study study) {
        assert (study != null);
        assert (this.params != null);

        this.params.addParameter("Best Score Nuc", new ParameterDetails(Float.class, Float.valueOf(0.0F), 0, 0));
        this.params.addParameter("Worst Score Nuc", new ParameterDetails(Float.class, Float.valueOf(study.getResultingGene().getSequenceLength() * 3.0F), 0, 0));
        this.params.addParameter("Best Score Cod", new ParameterDetails(Float.class, Float.valueOf(0.0F), 0, 0));
        this.params.addParameter("Worst Score Cod", new ParameterDetails(Float.class, Float.valueOf(study.getResultingGene().getSequenceLength() * 1.0F), 0, 0));
    }

    public float getScoreOfSequence(Study study, String sequence) {
        int nThreshold = ((Integer) this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString()).getValue()).intValue();
        int cThreshold = ((Integer) this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_CODON.toString()).getValue()).intValue();
        float nBestValue = ((Float) this.params.getParamDetails("Best Score Nuc").getValue()).floatValue();
        float nWorstValue = ((Float) this.params.getParamDetails("Worst Score Nuc").getValue()).floatValue();
        float cBestValue = ((Float) this.params.getParamDetails("Best Score Cod").getValue()).floatValue();
        float cWorstValue = ((Float) this.params.getParamDetails("Worst Score Cod").getValue()).floatValue();

        int nRepeats = 0;
        if (nThreshold != -1) {
            int consequentRepeats = 0;
            char lastNucleotide = sequence.charAt(0);
            for (int i = 1; i < sequence.length(); i++) {
                if (sequence.charAt(i) == lastNucleotide) {
                    consequentRepeats++;
                } else {
                    consequentRepeats = 0;
                }

                if (consequentRepeats >= nThreshold) {
                    nRepeats++;
                }

                lastNucleotide = sequence.charAt(i);
            }

        }

        int cRepeats = 0;
        if ((cThreshold != -1)
                && (cThreshold != -1)) {
            int consequentRepeats = 0;
            String lastCodon = sequence.substring(0, 3);
            for (int i = 0; i < sequence.length(); i += 3) {
                if (sequence.substring(i, i + 3).equals(lastCodon)) {
                    consequentRepeats++;
                } else {
                    consequentRepeats = 0;
                }

                if (consequentRepeats >= cThreshold) {
                    nRepeats++;
                }

                lastCodon = sequence.substring(i, i + 3);
            }

        }

        float nucleotideScore = 100.0F - Math.abs(nRepeats - nBestValue) / Math.abs(nBestValue - nWorstValue) * 100.0F;
        float codonScore = 100.0F - Math.abs(cRepeats - cBestValue) / Math.abs(cBestValue - cWorstValue) * 100.0F;

        if ((nThreshold != -1) && (cThreshold != -1)) {
            return nucleotideScore * 0.5F + codonScore * 0.5F;
        }

        if (nThreshold != -1) {
            return nucleotideScore;
        }

        if (cThreshold != -1) {
            return codonScore;
        }

        return 0.0F;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        List resultColorVector = new ArrayList();
        List<Integer> repeated = new ArrayList();

        int nucRepeats = ((Integer) this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_NUCL.toString()).getValue()).intValue();
        int codRepeats = ((Integer) this.params.getParamDetails(RepeatsRemovalPlugin.Parameter.CUSTOM_VALUE_REP_CODON.toString()).getValue()).intValue();
        int nThreshold = nucRepeats != -1 ? nucRepeats : 5;
        int cThreshold = codRepeats != -1 ? codRepeats : 2;

        String sequence = null;
        if (!useAlignedGene) {
            sequence = study.getResultingGene().getCodonSequence();
        } else if (study.getResultingGene().hasOrthologs()) {
            sequence = study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();
        } else if (study.getOriginalGene().hasOrthologs()) {
            sequence = study.getOriginalGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();
        }

        for (int i = 0; i < sequence.length(); i += 3) {
            repeated.add(Integer.valueOf(0));
        }

        if (nThreshold != -1) {
            int consequentRepeats = 0;
            char lastNucleotide = sequence.charAt(0);
            for (int i = 1; i < sequence.length(); i++) {
                if (sequence.charAt(i) == '-') {
                    repeated.set(i / 3, Integer.valueOf(0));
                } else {
                    if (sequence.charAt(i) == lastNucleotide) {
                        consequentRepeats++;
                    } else {
                        if (consequentRepeats >= nThreshold) {
                            for (int j = i - consequentRepeats - 1; j < i; j += 3) {
                                repeated.set(j / 3, Integer.valueOf(1));
                            }
                        }

                        consequentRepeats = 0;
                    }

                    lastNucleotide = sequence.charAt(i);
                }
            }
        }

        if (cThreshold != -1) {
            int consequentRepeats = 0;
            String lastCodon = sequence.substring(0, 3);
            for (int i = 3; i < sequence.length(); i += 3) {
                if (sequence.substring(i, i + 3).equals("---")) {
                    repeated.set(i, Integer.valueOf(0));
                } else {
                    if (sequence.substring(i, i + 3).equals(lastCodon)) {
                        consequentRepeats++;
                    } else {
                        if (consequentRepeats >= cThreshold) {
                            for (int j = i - (consequentRepeats + 1) * 3; j < i; j += 3) {
                                repeated.set(j / 3, Integer.valueOf(((Integer) repeated.get(j / 3)).intValue() + 2));
                            }
                        }

                        consequentRepeats = 0;
                    }

                    lastCodon = sequence.substring(i, i + 3);
                }
            }
        }
        for (Integer i : repeated) {
            switch (i.intValue()) {
                case 0:
                    resultColorVector.add(Color.LIGHT_GRAY);
                    break;
                case 1:
                    resultColorVector.add(Color.BLUE);
                    break;
                case 2:
                    resultColorVector.add(Color.YELLOW);
                    break;
                case 3:
                    resultColorVector.add(Color.GREEN);
            }

        }

        return resultColorVector;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 4;
            colorScale = new ArrayList(size);

            colorScale.add(Color.LIGHT_GRAY);
            colorScale.add(Color.BLUE);
            colorScale.add(Color.YELLOW);
            colorScale.add(Color.GREEN);
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "Repeats color scale";
    }

    public String getScaleMinDescription() {
        return "No repeat, Codon repeat, Nucleotide repeat, Both";
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
    
    
    public enum Parameter{
        REMOVE_REPEATED_NUCLEOT, 
        REMOVE_REPEATED_CODONS, 
        CUSTOM_VALUE_REP_NUCL, 
        CUSTOM_VALUE_REP_CODON;
    }
}
