/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.codoncorrelation;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
public class CodonCorrelationEffectPlugin implements IOptimizationPlugin, ChangeListener, ActionListener {
    private CodonCorrelationEffectParametersPanel parametersPanel = null;
    private ParameterSet params;
    private Genome host;
    private boolean isSelected;
    private static List<Color> colorScale;

    public CodonCorrelationEffectPlugin() {
        this.params = new ParameterSet();
        this.params.addParameter(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
        this.params.addParameter(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
        this.params.addParameter(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT_VALUE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(50), 0, 100));
    }

    public Enum[] getAvailableParameters() {
        return CodonCorrelationEffectPlugin.Parameter.values();
    }

    public void stateChanged(ChangeEvent e) {
        if (this.parametersPanel != null) {
            this.parametersPanel.jLabelRampEffectValue.setText(this.parametersPanel.jSliderRampEffect.getValue() + "%");
            if (this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT_VALUE.toString()) == null) {
                this.params.addParameter(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT_VALUE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(50), 0, 100));
            }
            this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT_VALUE.toString()).setValue(new Integer(this.parametersPanel.jSliderRampEffect.getValue()));
        }
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.parametersPanel = new CodonCorrelationEffectParametersPanel();
            this.isSelected = false;
            initComponentsListener();
        }
        return this.parametersPanel;
    }

    private void initComponentsListener() {
        this.parametersPanel.jSliderRampEffect.addChangeListener(this);
        this.parametersPanel.jMaximizeRadioButton.addActionListener(this);
        this.parametersPanel.jMinimizeRadioButton.addActionListener(this);
        this.parametersPanel.jRampEffectCheckBox.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (this.parametersPanel != null) {
            if (this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString()) == null) {
                this.params.addParameter(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
            }
            if (this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString()) == null) {
                this.params.addParameter(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
            }

            if (this.parametersPanel.jMaximizeRadioButton.isSelected()) {
                this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString()).setValue(Boolean.valueOf(true));
            } else if (this.parametersPanel.jMinimizeRadioButton.isSelected()) {
                this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString()).setValue(Boolean.valueOf(false));
            }

            if (this.parametersPanel.jRampEffectCheckBox.isSelected()) {
                this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString()).setValue(Boolean.valueOf(true));
            } else {
                this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString()).setValue(Boolean.valueOf(false));
            }
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
        this.parametersPanel.jMaximizeRadioButton.setEnabled(selected);
        this.parametersPanel.jMinimizeRadioButton.setEnabled(selected);
        this.parametersPanel.jRampEffectCheckBox.setEnabled(selected);
        this.parametersPanel.jSliderRampEffect.setEnabled(selected);
        this.parametersPanel.jLabelRampEffectValue.setEnabled(selected);
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    public void setParameters(ParameterSet parameters) {
        if (this.parametersPanel != null) {
            if (parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jMaximizeRadioButton.setSelected(true);
                    this.parametersPanel.jMinimizeRadioButton.setSelected(false);
                } else if (!((Boolean) parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString()).getValue()).booleanValue()) {
                    this.parametersPanel.jMinimizeRadioButton.setSelected(true);
                    this.parametersPanel.jMaximizeRadioButton.setSelected(false);
                }
            }

            if (parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jRampEffectCheckBox.setSelected(true);
                } else if (!((Boolean) parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString()).getValue()).booleanValue()) {
                    this.parametersPanel.jRampEffectCheckBox.setSelected(false);
                }
            }

            if (parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT_VALUE.toString()) != null) {
                String aux = "" + parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT_VALUE.toString()).getValue();
                if (aux.matches("[0-9]+")) {
                    int value = Integer.parseInt(aux);
                    if ((value < 0) || (value > 100)) {
                        System.out.println("Value provived for ramp effect isn't in range [0-100]: " + value + "\n Default value set to 50!");
                        parameters.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT_VALUE.toString()).setValue(Integer.valueOf(50));
                    }
                    this.parametersPanel.jLabelRampEffectValue.setText(value + "%");
                } else {
                    this.parametersPanel.jLabelRampEffectValue.setText("50%");
                }
            }
        }

        this.params = parameters;
    }

    public void calculateBestScore(Study study) {
        assert ((study != null) && (this.params != null));

        boolean isMaximize = ((Boolean) this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.MAXIMIZE_CORRELATION.toString()).getValue()).booleanValue();

        HashMap aaCodonSynFrequence = getAACodonSynFrequency(study.getResultingGene().getCodonSequence());
        Set<String> aaKey = aaCodonSynFrequence.keySet();
        float totalMaxScores = 0.0F;
        float totalMinScores = 0.0F;

        for (String key : aaKey) {
            int[] codonFrequencies = new int[((HashMap) aaCodonSynFrequence.get(key)).size()];
            int idx = 0;
            for (AtomicInteger value : ((HashMap<String, AtomicInteger>) aaCodonSynFrequence.get(key)).values()) {
                codonFrequencies[idx] = value.intValue();
                idx++;
            }
            float maxScore = getAminoAcidMaxScore(codonFrequencies);
            float minScore = getAminoAcidMinScore(codonFrequencies);

            totalMaxScores += maxScore;
            totalMinScores += minScore;
        }

        float averageMax = totalMaxScores / aaKey.size();

        float averageMin = totalMinScores / aaKey.size();
        if (isMaximize) {
            this.params.addParameter("Best Score", new ParameterDetails(Float.class, Float.valueOf(averageMax), 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Float.class, Float.valueOf(averageMin), 0, 0));
        } else {
            this.params.addParameter("Best Score", new ParameterDetails(Float.class, Float.valueOf(averageMin), 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Float.class, Float.valueOf(averageMax), 0, 0));
        }
    }

    public float getScoreOfSequence(Study study, String sequence) {
        assert (sequence != null);
        assert (this.params != null);

        float bestScore = ((Float) this.params.getParamDetails("Best Score").getValue()).floatValue();
        float worstScore = ((Float) this.params.getParamDetails("Worst Score").getValue()).floatValue();

        if (Math.abs(bestScore - worstScore) == 0.0F) {
            return 100.0F;
        }

        HashMap aaCodonSynFrequence = getAACodonSynFrequency(sequence);

        Set<String> aaKey = aaCodonSynFrequence.keySet();
        float totalObsScore = 0.0F;

        for (String key : aaKey) {
            int[] codonFrequencies = new int[((HashMap) aaCodonSynFrequence.get(key)).size()];
            int idx = 0;
            for (AtomicInteger value : ((HashMap<String, AtomicInteger>) aaCodonSynFrequence.get(key)).values()) {
                codonFrequencies[idx] = value.intValue();
                idx++;
            }
            totalObsScore += getAminoAcidObservedScore(codonFrequencies);
        }

        float sequenceScore = totalObsScore / aaKey.size();

        return 100.0F - Math.abs(sequenceScore - bestScore) / Math.abs(bestScore - worstScore) * 100.0F;
    }

    private float getAminoAcidObservedScore(int[] codonFrequencies) {
        assert (codonFrequencies != null);
        return calcStandardDeviation(codonFrequencies);
    }

    private float getAminoAcidMaxScore(int[] codonFrequencies) {
        assert (codonFrequencies != null);
        int sumValue = 0;
        int[] newCodonFrequencies = new int[codonFrequencies.length];
        for (int i = 0; i < codonFrequencies.length; i++) {
            sumValue += codonFrequencies[i];
            newCodonFrequencies[i] = 0;
        }
        newCodonFrequencies[0] = sumValue;
        return calcStandardDeviation(newCodonFrequencies);
    }

    private float getAminoAcidMinScore(int[] codonFrequencies) {
        assert (codonFrequencies != null);
        int totalFrequencies = 0;
        int[] newCodonFrequencies = new int[codonFrequencies.length];
        int idx = 0;

        for (int value : codonFrequencies) {
            totalFrequencies += value;
            newCodonFrequencies[idx] = 0;
            idx++;
        }
        int count = 0;
        while (count < totalFrequencies) {
            for (int i = 0; (i < newCodonFrequencies.length)
                    && (count < totalFrequencies); i++) {
                newCodonFrequencies[i] += 1;
                count++;
            }
        }
        return calcStandardDeviation(newCodonFrequencies);
    }

    private float calcStandardDeviation(int[] occurs) {
        int populationSize = occurs.length;
        int valuesSum = 0;
        float aux = 0.0F;

        for (int i = 0; i < occurs.length; i++) {
            valuesSum += occurs[i];
        }

        float mean = valuesSum / populationSize;

        for (int value : occurs) {
            aux = (float) (aux + Math.pow(Math.abs(mean - value), 2.0D));
        }

        return (float) Math.sqrt(aux / populationSize);
    }

    private HashMap<String, HashMap<String, AtomicInteger>> getAACodonSynFrequency(String codonSequence) {
        assert ((codonSequence != null) && (!codonSequence.isEmpty()));
        int codonsLength = codonSequence.length();

        boolean isRampEffect = ((Boolean) this.params.getParamDetails(CodonCorrelationEffectPlugin.Parameter.RAMP_EFFECT.toString()).getValue()).booleanValue();
        HashMap aaCodonSynFrequence = new HashMap();

        if (isRampEffect) {
            int stopCriteria = Integer.valueOf(this.parametersPanel.jLabelRampEffectValue.getText().replaceAll("%", "")).intValue();
            codonsLength = stopCriteria * codonSequence.length() / 100;
        }

        for (int i = 0; i < codonsLength / 3; i++) {
            String currentCodon = codonSequence.substring(i * 3, i * 3 + 3);
            String currentAA = this.host.getAminoAcidFromCodon(currentCodon);

            if (aaCodonSynFrequence.get(currentAA) != null) {
                ((AtomicInteger) ((HashMap) aaCodonSynFrequence.get(currentAA)).get(currentCodon)).getAndIncrement();
            } else {
                HashMap synonymFrequencyTable = new HashMap();
                for (String syn : this.host.getGeneticCodeTable().getSynonymousFromAA(currentAA)) {
                    if (syn.equalsIgnoreCase(currentCodon)) {
                        synonymFrequencyTable.put(syn, new AtomicInteger(1));
                    } else {
                        synonymFrequencyTable.put(syn, new AtomicInteger(0));
                    }
                }
                aaCodonSynFrequence.put(currentAA, synonymFrequencyTable);
            }
        }
        return aaCodonSynFrequence;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        List resultColorVector = new ArrayList();

        BioStructure codonList = null;
        if (!useAlignedGene) {
            codonList = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);
        } else if (study.getResultingGene().hasOrthologs()) {
            codonList = study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure);
        } else if (study.getOriginalGene().hasOrthologs()) {
            codonList = study.getOriginalGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure);
        }

        HashMap aaCodonSynFrequence = getAACodonSynFrequency(codonList.getSequence());
        Set<String> aaKey = aaCodonSynFrequence.keySet();
        float maxObs = 0.0F;
        float minObs = 0.0F;
        float obsScore = 0.0F;
        for (String key : aaKey) {
            int[] codonFrequencies = new int[((HashMap) aaCodonSynFrequence.get(key)).size()];
            int idx = 0;
            for (AtomicInteger value : ((HashMap<String, AtomicInteger>) aaCodonSynFrequence.get(key)).values()) {
                codonFrequencies[idx] = value.intValue();
                idx++;
            }

            obsScore = getAminoAcidObservedScore(codonFrequencies);
            if (obsScore < minObs) {
                minObs = obsScore;
            }
            if (obsScore > maxObs) {
                maxObs = obsScore;
            }
        }
        float maxDiference = maxObs - minObs;

        HashMap aaColor = new HashMap();

        for (String key : aaKey) {
            int[] codonFrequencies = new int[((HashMap) aaCodonSynFrequence.get(key)).size()];
            int idx = 0;
            for (AtomicInteger value : ((HashMap<String, AtomicInteger>) aaCodonSynFrequence.get(key)).values()) {
                codonFrequencies[idx] = value.intValue();
                idx++;
            }
            obsScore = getAminoAcidObservedScore(codonFrequencies);
            obsScore = Math.min(maxObs, obsScore);
            obsScore = Math.max(minObs, obsScore);
            float intensity = Math.round(255.0F - (maxObs - obsScore) / maxDiference * 255.0F);
            aaColor.put(key, Color.getHSBColor(intensity / 750.0F, 0.7F, 0.8F));
        }

        String codonSeq = codonList.getSequence();

        for (int i = 0; i < codonSeq.length() / 3; i++) {
            String currentCodon = codonSeq.substring(i * 3, i * 3 + 3);
            resultColorVector.add(aaColor.get(this.host.getGeneticCodeTable().getAminoAcidFromCodon(currentCodon)));
        }

        return resultColorVector;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 16;
            colorScale = new ArrayList(size);

            for (float i = 0.0F; i < size; i += 1.0F) {
                float intensity = 255.0F - i / (size - 1) * 255.0F;
                colorScale.add(0, Color.getHSBColor(intensity / 750.0F, 0.7F, 0.8F));
            }
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "Codon Correlation Effect color scale";
    }

    public String getScaleMinDescription() {
        return "zero Corr";
    }

    public String getScaleMaxDescription() {
        return "max Corr";
    }

    public String getPluginName() {
        return "Codon Correlation Effect";
    }

    public String getPluginId() {
        return "CODONCORRELATIONEFFECT1";
    }

    public String getPluginVersion() {
        return "0.8";
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }

    static {
        colorScale = null;
    }
    
    public enum Parameter {
        MAXIMIZE_CORRELATION, 
        RAMP_EFFECT, 
        RAMP_EFFECT_VALUE;
    }
}
