/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.hiddenstops;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
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
public class HiddenStopCodonsPlugin implements IOptimizationPlugin, ActionListener, DocumentListener {
    private HiddenStopCodonsParametersPanel parametersPanel = null;
    private ParameterSet params;
    private boolean isSelected;
    private Genome host;
    private static List<Color> colorScale;

    public HiddenStopCodonsPlugin() {
        this.params = new ParameterSet();

        this.params.addParameter(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 2));
        this.params.addParameter(HiddenStopCodonsPlugin.Parameter.CUSTOM_VALUE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(100), 0, 100));
    }

    public Enum[] getAvailableParameters() {
        return HiddenStopCodonsPlugin.Parameter.values();
    }

    public String getPluginName() {
        return "Hidden Stop Codons";
    }

    public String getPluginId() {
        return "HIDDENSTOPCODONS1";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.isSelected = false;
            this.parametersPanel = new HiddenStopCodonsParametersPanel();
            initComponentListener();
        }
        return this.parametersPanel;
    }

    private void initComponentListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jCustomHiddenButton.addActionListener(this);
            this.parametersPanel.jMaximizeHiddenButton.addActionListener(this);
            this.parametersPanel.jMinimizeHiddenButton.addActionListener(this);
            this.parametersPanel.jCustomHiddenTextField.getDocument().addDocumentListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()) == null) {
            this.params.addParameter(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 2));
        }

        if (this.parametersPanel.jMaximizeHiddenButton.isSelected()) {
            this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).setValue(Integer.valueOf(0));
        } else if (this.parametersPanel.jMinimizeHiddenButton.isSelected()) {
            this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).setValue(Integer.valueOf(1));
        } else if (this.parametersPanel.jCustomHiddenButton.isSelected()) {
            this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).setValue(Integer.valueOf(2));
        }
    }

    private void updateCustomParameter() {
        if (this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.CUSTOM_VALUE.toString()) == null) {
            this.params.addParameter(HiddenStopCodonsPlugin.Parameter.CUSTOM_VALUE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(100), 0, 100));
        }
        if ((this.parametersPanel.jCustomHiddenTextField.getText() != null) && (!this.parametersPanel.jCustomHiddenTextField.getText().isEmpty())) {
            this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.CUSTOM_VALUE.toString()).setValue(new Integer(this.parametersPanel.jCustomHiddenTextField.getText()));
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

    public Study makeSingleOptimization(Study study) {
        boolean isToAddHiddenStopCodons = true;
        if (((Integer) this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).getValue()).intValue() == 0) {
            isToAddHiddenStopCodons = true;
        } else if (((Integer) this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).getValue()).intValue() == 1) {
            isToAddHiddenStopCodons = false;
        }

        Gene gene = new Gene(study.getResultingGene().getName(), this.host);
        String sequence = traceBack(getMaximumHiddenCodons(study.getResultingGene(), isToAddHiddenStopCodons), study.getResultingGene(), isToAddHiddenStopCodons);
        gene.createStructure(sequence, BioStructure.Type.mRNAPrimaryStructure);
        gene.calculateAllStructures();

        Study newStudy = new Study(study.getResultingGene(), gene, new StringBuilder().append("[").append(this.host.getName()).append("]  ").append(gene.getName()).append(" (Redesigned by: Hidden Stop Codons)").toString());

        return newStudy;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public String traceBack(HashMap<Integer, List<Integer>> maximumHiddenCodons, Gene gene, boolean isToAddHiddenStopCodons) {
        assert (maximumHiddenCodons != null);
        assert (gene != null);
        assert (gene.getStructure(BioStructure.Type.mRNAPrimaryStructure) != null);

        int max = -1;
        int index = -1;
        List listOfCodons = (List) maximumHiddenCodons.get(Integer.valueOf(gene.getSequenceLength()));
        for (int j = 0; j < listOfCodons.size(); j++) {
            if (((Integer) listOfCodons.get(j)).intValue() > max) {
                max = ((Integer) listOfCodons.get(j)).intValue();
                index = j;
            }
        }

        assert (index != -1);

        BioStructure originalSequence = gene.getStructure(BioStructure.Type.proteinPrimaryStructure);
        HashMap sequence = new HashMap();
        int c = index;
        sequence.put(Integer.valueOf(gene.getSequenceLength()), Integer.valueOf(index));

        for (int i = gene.getSequenceLength() - 1; i >= 1; i--) {
            List currentSynonymous = this.host.getGeneticCodeTable().getSynonymousFromAA(originalSequence.getWordAt(i - 1));
            List lastSynonymous = this.host.getGeneticCodeTable().getSynonymousFromAA(originalSequence.getWordAt(i));
            for (int j = 0; j < currentSynonymous.size(); j++) {
                if (((Integer) ((List) maximumHiddenCodons.get(Integer.valueOf(i))).get(j)).intValue() + isThereAStopCodonBetween((String) currentSynonymous.get(j), (String) lastSynonymous.get(c), isToAddHiddenStopCodons) == max) {
                    max -= isThereAStopCodonBetween((String) currentSynonymous.get(j), (String) lastSynonymous.get(c), isToAddHiddenStopCodons);
                    sequence.put(Integer.valueOf(i), Integer.valueOf(j));
                    c = j;
                    break;
                }
            }

        }

        List codonsVector = new ArrayList();
        List syn = null;
        for (int i = 0; i < gene.getSequenceLength(); i++) {
            syn = this.host.getGeneticCodeTable().getSynonymousFromAA(originalSequence.getWordAt(i));
            codonsVector.add(syn.get(((Integer) sequence.get(Integer.valueOf(i + 1))).intValue()));
        }

        StringBuilder finalSequence = new StringBuilder();
        for (int i = 0; i < gene.getSequenceLength(); i++) {
            finalSequence.append((String) codonsVector.get(i));
        }

        return finalSequence.toString();
    }

    private HashMap<Integer, List<Integer>> getMaximumHiddenCodons(Gene gene, boolean isToAddHiddenStopCodons) {
        assert (gene != null);
        assert (gene.getGenome() != null);
        assert (gene.getGenome().getGeneticCodeTable() != null);
        assert (gene.getStructure(BioStructure.Type.mRNAPrimaryStructure) != null);

        HashMap maximumHiddenCodons = new HashMap(gene.getSequenceLength());
        BioStructure sequence = gene.getStructure(BioStructure.Type.proteinPrimaryStructure);
        GeneticCodeTable geneticTable = this.host.getGeneticCodeTable();

        maximumHiddenCodons.put(Integer.valueOf(1), new ArrayList());
        String aminoAcid = sequence.getWordAt(0);
        int numberOfSynonymous = geneticTable.getSynonymousFromAA(aminoAcid).size();
        for (int i = 0; i < numberOfSynonymous; i++) {
            ((List) maximumHiddenCodons.get(Integer.valueOf(1))).add(Integer.valueOf(0));
        }

        for (int i = 2; i <= gene.getSequenceLength(); i++) {
            List previousSynonymous = geneticTable.getSynonymousFromAA(sequence.getWordAt(i - 2));
            List currentSynonymous = geneticTable.getSynonymousFromAA(sequence.getWordAt(i - 1));

            maximumHiddenCodons.put(Integer.valueOf(i), new ArrayList());
            for (int j = 0; j < currentSynonymous.size(); j++) {
                ((List) maximumHiddenCodons.get(Integer.valueOf(i))).add(Integer.valueOf(0));
            }

            for (int j = 0; j < currentSynonymous.size(); j++) {
                int max = 0;
                for (int k = 0; k < previousSynonymous.size(); k++) {
                    int previousMax = ((Integer) ((List) maximumHiddenCodons.get(Integer.valueOf(i - 1))).get(k)).intValue();
                    previousMax += isThereAStopCodonBetween((String) previousSynonymous.get(k), (String) currentSynonymous.get(j), isToAddHiddenStopCodons);
                    if (previousMax >= max) {
                        max = previousMax;
                    }
                }

                ((List) maximumHiddenCodons.get(Integer.valueOf(i))).set(j, Integer.valueOf(max));
            }
        }

        return maximumHiddenCodons;
    }

    private int isThereAStopCodonBetween(String codon1, String codon2, boolean isMaximizeHiddenStopCodons) {
        assert (codon1 != null);
        assert (codon2 != null);

        String possibleStop1 = codon1.substring(1).concat(codon2.substring(0, 1));
        String possibleStop2 = codon1.substring(2).concat(codon2.substring(0, 2));

        if ((this.host.getGeneticCodeTable().isStopCodon(possibleStop1)) || (this.host.getGeneticCodeTable().isStopCodon(possibleStop2))) {
            return isMaximizeHiddenStopCodons ? 1 : 0;
        }

        return isMaximizeHiddenStopCodons ? 0 : 1;
    }

    public int countNumberOfHiddenStopCodons(String sequence) {
        int counter = 0;
        for (int i = 0; i < sequence.length() - 3; i += 3) {
            counter += isThereAStopCodonBetween(sequence.substring(i, i + 3), sequence.substring(i + 3, i + 6), true);
        }

        return counter;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.parametersPanel.jMaximizeHiddenButton.setEnabled(selected);
        this.parametersPanel.jMinimizeHiddenButton.setEnabled(selected);
        this.parametersPanel.jCustomHiddenButton.setEnabled(selected);
        this.parametersPanel.jCustomHiddenTextField.setEnabled(selected);
    }

    public void setParameters(ParameterSet parameters) {
        if ((this.parametersPanel != null)
                && (parameters.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()) != null)) {
            if (((Integer) parameters.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).getValue()).intValue() == 0) {
                this.parametersPanel.jMaximizeHiddenButton.setSelected(true);
            } else if (((Integer) parameters.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).getValue()).intValue() == 1) {
                this.parametersPanel.jMinimizeHiddenButton.setSelected(true);
            } else if (((Integer) parameters.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).getValue()).intValue() == 2) {
                this.parametersPanel.jCustomHiddenButton.setSelected(true);
                this.parametersPanel.jCustomHiddenTextField.setText(Integer.toString(((Integer) parameters.getParamDetails(HiddenStopCodonsPlugin.Parameter.CUSTOM_VALUE.toString()).getValue()).intValue()));
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

        List maxNumberOfHiddenCodons = (List) getMaximumHiddenCodons(study.getResultingGene(), true).get(Integer.valueOf(study.getResultingGene().getSequenceLength()));
        List minNumberOfHiddenCodons = (List) getMaximumHiddenCodons(study.getResultingGene(), false).get(Integer.valueOf(study.getResultingGene().getSequenceLength()));

        int max = -1;
        int min = -1;
        for (int j = 0; j < maxNumberOfHiddenCodons.size(); j++) {
            if (((Integer) maxNumberOfHiddenCodons.get(j)).intValue() > max) {
                max = ((Integer) maxNumberOfHiddenCodons.get(j)).intValue();
            }
            if (((Integer) minNumberOfHiddenCodons.get(j)).intValue() > min) {
                min = ((Integer) minNumberOfHiddenCodons.get(j)).intValue();
            }
        }
        min = study.getResultingGene().getSequenceLength() - min - 1;

        assert (max >= min);

        int chosenValue = 0;
        if (this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()) != null) {
            chosenValue = ((Integer) this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.HIDDEN_STOP_CODON_FUNCTION.toString()).getValue()).intValue();
        }

        if (chosenValue == 0) {
            this.params.addParameter("Best Score", new ParameterDetails(Integer.class, Integer.valueOf(max), 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(min), 0, 0));
        } else if (chosenValue == 1) {
            this.params.addParameter("Best Score", new ParameterDetails(Integer.class, Integer.valueOf(min), 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(max), 0, 0));
        } else {
            int numHiddenStopCodons = ((Integer) this.params.getParamDetails(HiddenStopCodonsPlugin.Parameter.CUSTOM_VALUE.toString()).getValue()).intValue();

            if ((max > numHiddenStopCodons) && (min < numHiddenStopCodons)) {
                this.params.addParameter("Best Score", new ParameterDetails(Integer.class, Integer.valueOf(numHiddenStopCodons), 0, 0));
                if (Math.abs(max - numHiddenStopCodons) > Math.abs(numHiddenStopCodons - min)) {
                    this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(max), 0, 0));
                } else {
                    this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(min), 0, 0));
                }
            } else if (Math.abs(max - numHiddenStopCodons) > Math.abs(numHiddenStopCodons - min)) {
                this.params.addParameter("Best Score", new ParameterDetails(Integer.class, Integer.valueOf(min), 0, 0));
                this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(max), 0, 0));
            } else {
                this.params.addParameter("Best Score", new ParameterDetails(Integer.class, Integer.valueOf(max), 0, 0));
                this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(min), 0, 0));
            }
        }
    }

    public float getScoreOfSequence(Study study, String sequence) {
        assert (study != null);
        assert (sequence != null);
        assert (this.params != null);

        int numberOfHiddenStopCodons = countNumberOfHiddenStopCodons(sequence);

        float c1 = numberOfHiddenStopCodons - ((Integer) this.params.getParamDetails("Best Score").getValue()).intValue();
        float c2 = ((Integer) this.params.getParamDetails("Worst Score").getValue()).intValue() - ((Integer) this.params.getParamDetails("Best Score").getValue()).intValue();
        float score = 100.0F - Math.abs(c1 / c2) * 100.0F;

        assert (score >= 0.0F);
        assert (score <= 100.0F);

        return score;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        assert (study != null);

        List resultColorVector = new ArrayList();
        List numberOfHiddenCodons = new ArrayList();

        BioStructure resultingGene = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);
        BioStructure codonList = null;
        if (!useAlignedGene) {
            codonList = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);
        } else if (study.getResultingGene().hasOrthologs()) {
            codonList = study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure);
        } else if (study.getOriginalGene().hasOrthologs()) {
            codonList = study.getOriginalGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure);
        }

        numberOfHiddenCodons.add(Integer.valueOf(0));
        for (int i = 1; i < codonList.getLength(); i++) {
            if (codonList.getWordAt(i).equals("---")) {
                numberOfHiddenCodons.add(Integer.valueOf(0));
            }

            if (isThereAStopCodonBetween(resultingGene.getWordAt(i - 1), resultingGene.getWordAt(i), true) == 1) {
                numberOfHiddenCodons.add(Integer.valueOf(1));
                numberOfHiddenCodons.set(i - 1, Integer.valueOf(((Integer) numberOfHiddenCodons.get(i - 1)).intValue() + 1));
            } else {
                numberOfHiddenCodons.add(Integer.valueOf(0));
            }
        }

        for (int i = 0; i < codonList.getLength(); i++) {
            switch (((Integer) numberOfHiddenCodons.get(i)).intValue()) {
                case 0:
                    resultColorVector.add(Color.LIGHT_GRAY);
                    break;
                case 1:
                    resultColorVector.add(Color.DARK_GRAY);
                    break;
                case 2:
                    resultColorVector.add(Color.BLACK);
            }

        }

        return resultColorVector;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 3;
            colorScale = new ArrayList(size);

            colorScale.add(Color.LIGHT_GRAY);
            colorScale.add(Color.DARK_GRAY);
            colorScale.add(Color.BLACK);
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "Hidden Stop Codons color scale";
    }

    public String getScaleMinDescription() {
        return "No stop codons";
    }

    public String getScaleMaxDescription() {
        return "2 stop codons";
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }

    static {
        colorScale = null;
    }
    
    public enum Parameter {
        HIDDEN_STOP_CODON_FUNCTION, 
        CUSTOM_VALUE;
    }
}
