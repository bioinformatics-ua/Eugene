/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.codoncontext;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import pt.ua.ieeta.geneoptimizer.geneDB.UsageAndContextTables;

/**
 *
 * @author Eduardo
 */
public class CodonContextPlugin implements IOptimizationPlugin, ActionListener {
    private CodonContextParametersPanel parametersPanel = null;
    private ParameterSet params;
    private Genome host;
    private boolean isSelected;
    private static List<Color> colorScale;

    public CodonContextPlugin() {
        this.params = new ParameterSet();

        this.params.addParameter(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
    }

    public Enum[] getAvailableParameters() {
        return CodonContextPlugin.Parameter.values();
    }

    public String getPluginName() {
        return "Codon Context";
    }

    public String getPluginId() {
        return "CODONCONTEXT1";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.parametersPanel = new CodonContextParametersPanel();
            setSelected(false);
            initComponentsListener();
        }
        return this.parametersPanel;
    }

    private void initComponentsListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jMaximizeContextButton.addActionListener(this);
            this.parametersPanel.jMinimizeContextButton.addActionListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()) == null) {
            this.params.addParameter(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
        }

        if (this.parametersPanel.jMaximizeContextButton.isSelected()) {
            this.params.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()).setValue(Boolean.valueOf(true));
        } else if (this.parametersPanel.jMinimizeContextButton.isSelected()) {
            this.params.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()).setValue(Boolean.valueOf(false));
        }
    }

    public Study makeSingleOptimization(Study study) {
        boolean isMaximize = ((Boolean) this.params.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()).getValue()).booleanValue();
        HashMap maximumCodonContext = getMaximumCodonContext(study.getResultingGene().getStructure(BioStructure.Type.proteinPrimaryStructure), isMaximize);

        String redesignedGene = traceBack(maximumCodonContext, study.getResultingGene().getStructure(BioStructure.Type.proteinPrimaryStructure));

        Gene gene = new Gene(study.getResultingGene().getName(), this.host);
        gene.createStructure(redesignedGene, BioStructure.Type.mRNAPrimaryStructure);
        gene.calculateAllStructures();

        Study newStudy = new Study(study.getResultingGene(), gene, new StringBuilder().append("[").append(this.host.getName()).append("]  ").append(gene.getName()).append(" (Redesigned by: Codon Context)").toString());

        return newStudy;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.parametersPanel.jMaximizeContextButton.setEnabled(selected);
        this.parametersPanel.jMinimizeContextButton.setEnabled(selected);
    }

    public void setParameters(ParameterSet parameters) {
        if ((this.parametersPanel != null)
                && (parameters.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()) != null)) {
            if (((Boolean) parameters.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()).getValue()).booleanValue() == true) {
                this.parametersPanel.jMaximizeContextButton.setSelected(true);
            } else {
                this.parametersPanel.jMinimizeContextButton.setSelected(true);
            }

        }

        this.params = parameters;
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    public void calculateBestScore(Study study) {
        UsageAndContextTables UCT = this.host.getUsageAndContextTables();

        BioStructure proteinStructure = study.getResultingGene().getStructure(BioStructure.Type.proteinPrimaryStructure);
        HashMap maximumCodonContext = getMaximumCodonContext(proteinStructure, true);
        HashMap minimumCodonContext = getMaximumCodonContext(proteinStructure, false);

        String maximizedSequence = traceBack(maximumCodonContext, proteinStructure);
        String minimizedSequence = traceBack(minimumCodonContext, proteinStructure);

        float maximizedSequenceScore = 0.0F;
        float minimizedSequenceScore = 0.0F;
        for (int i = 0; i + 6 <= maximizedSequence.length(); i += 3) {
            maximizedSequenceScore += UCT.getCodonPairScore(maximizedSequence.substring(i, i + 3), maximizedSequence.substring(i + 3, i + 6));
            minimizedSequenceScore += UCT.getCodonPairScore(minimizedSequence.substring(i, i + 3), minimizedSequence.substring(i + 3, i + 6));
        }

        if (this.params.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()) != null) {
            if (((Boolean) this.params.getParamDetails(CodonContextPlugin.Parameter.MAXIMIZE_CODON_CONTEXT.toString()).getValue()).booleanValue() == true) {
                this.params.addParameter("Best Score", new ParameterDetails(Float.class, Float.valueOf(maximizedSequenceScore), 0, 0));
                this.params.addParameter("Worst Score", new ParameterDetails(Float.class, Float.valueOf(minimizedSequenceScore), 0, 0));
            } else {
                this.params.addParameter("Best Score", new ParameterDetails(Float.class, Float.valueOf(minimizedSequenceScore), 0, 0));
                this.params.addParameter("Worst Score", new ParameterDetails(Float.class, Float.valueOf(maximizedSequenceScore), 0, 0));
            }
        }
    }

    private HashMap<Integer, List<Float>> getMaximumCodonContext(BioStructure sequence, boolean isMaximizeCodonContext) {
        HashMap maximumCodonContext = new HashMap(sequence.getLength());
        GeneticCodeTable geneticTable = this.host.getGeneticCodeTable();
        UsageAndContextTables uct = this.host.getUsageAndContextTables();

        maximumCodonContext.put(Integer.valueOf(1), new ArrayList());
        for (int i = 0; i < geneticTable.getSynonymousFromAA(sequence.getWordAt(0)).size(); i++) {
            ((List) maximumCodonContext.get(Integer.valueOf(1))).add(Float.valueOf(0.0F));
        }

        for (int i = 2; i <= sequence.getLength(); i++) {
            List previousSynonymous = geneticTable.getSynonymousFromAA(sequence.getWordAt(i - 2));
            List currentSynonymous = geneticTable.getSynonymousFromAA(sequence.getWordAt(i - 1));

            maximumCodonContext.put(Integer.valueOf(i), new ArrayList());
            for (int j = 0; j < currentSynonymous.size(); j++) {
                ((List) maximumCodonContext.get(Integer.valueOf(i))).add(Float.valueOf(0.0F));
            }

            for (int j = 0; j < currentSynonymous.size(); j++) {
                float control = isMaximizeCodonContext ? (1.0F / -1.0F) : (1.0F / 1.0F);

                for (int k = 0; k < previousSynonymous.size(); k++) {
                    float previousMax = ((Float) ((List) maximumCodonContext.get(Integer.valueOf(i - 1))).get(k)).floatValue();
                    float sum = uct.getCodonPairScore((String) previousSynonymous.get(k), (String) currentSynonymous.get(j));

                    previousMax += sum;

                    if ((isMaximizeCodonContext) && (previousMax >= control)) {
                        control = previousMax;
                    }
                    if ((!isMaximizeCodonContext) && (previousMax <= control)) {
                        control = previousMax;
                    }
                }

                ((List) maximumCodonContext.get(Integer.valueOf(i))).set(j, Float.valueOf(control));
            }
        }

        return maximumCodonContext;
    }

    public String traceBack(HashMap<Integer, List<Float>> maximumContext, BioStructure originalSequence) {
        assert (maximumContext != null);
        assert (originalSequence != null);

        UsageAndContextTables uct = this.host.getUsageAndContextTables();
        HashMap sequence = new HashMap();

        float max = (1.0F / -1.0F);
        int index = -1;
        List listOfCodons = (List) maximumContext.get(Integer.valueOf(originalSequence.getLength()));
        for (int j = 0; j < listOfCodons.size(); j++) {
            if (((Float) listOfCodons.get(j)).floatValue() > max) {
                max = ((Float) listOfCodons.get(j)).floatValue();
                index = j;
            }
        }

        assert (index != -1);

        int c = index;
        sequence.put(Integer.valueOf(originalSequence.getLength()), Integer.valueOf(index));

        for (int i = originalSequence.getLength() - 1; i >= 1; i--) {
            List currentSynonymous = this.host.getGeneticCodeTable().getSynonymousFromAA(originalSequence.getWordAt(i - 1));
            List lastSynonymous = this.host.getGeneticCodeTable().getSynonymousFromAA(originalSequence.getWordAt(i));

            for (int j = 0; j < currentSynonymous.size(); j++) {
                String currentSyn = (String) currentSynonymous.get(j);
                String previousSyn = (String) lastSynonymous.get(c);

                float newValue = ((Float) ((List) maximumContext.get(Integer.valueOf(i))).get(j)).floatValue() + uct.getCodonPairScore(currentSyn, previousSyn);
                boolean foundCorrespondance = 0.001D > Math.abs(max - newValue);
                if (foundCorrespondance) {
                    max -= uct.getCodonPairScore(currentSyn, previousSyn);
                    sequence.put(Integer.valueOf(i), Integer.valueOf(j));
                    c = j;
                    break;
                }

            }

            //assert (c == j);
        }

        List codonsVector = new ArrayList();
        List syn = null;
        for (int i = 0; i < originalSequence.getLength(); i++) {
            syn = this.host.getGeneticCodeTable().getSynonymousFromAA(originalSequence.getWordAt(i));
            codonsVector.add(syn.get(((Integer) sequence.get(Integer.valueOf(i + 1))).intValue()));
        }

        assert (codonsVector != null);
        assert (!codonsVector.isEmpty());

        StringBuilder finalSequence = new StringBuilder();
        for (int i = 0; i < originalSequence.getLength(); i++) {
            finalSequence.append((String) codonsVector.get(i));
        }

        return finalSequence.toString();
    }

    public float getScoreOfSequence(Study study, String sequence) {
        UsageAndContextTables UCT = this.host.getUsageAndContextTables();
        float bestContext = ((Float) this.params.getParamDetails("Best Score").getValue()).floatValue();
        float worstContext = ((Float) this.params.getParamDetails("Worst Score").getValue()).floatValue();

        float sequenceContext = 0.0F;
        for (int i = 0; i + 6 <= sequence.length(); i += 3) {
            sequenceContext += UCT.getCodonPairScore(sequence.substring(i, i + 3), sequence.substring(i + 3, i + 6));
        }

        return 100.0F - Math.abs(sequenceContext - bestContext) / Math.abs(bestContext - worstContext) * 100.0F;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        assert (study != null);
        assert (this.params != null);
        assert (study.getResultingGene().getGenome() != null);
        assert (this.host != null);
        assert (study.getResultingGene().getGenome().getUsageAndContextTables() != null);
        assert (study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure) != null);

        List resultColorVector = new ArrayList();

        UsageAndContextTables cut = study.getResultingGene().getGenome().getUsageAndContextTables();
        BioStructure codonList = null;
        if (!useAlignedGene) {
            codonList = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);
        } else if (study.getResultingGene().hasOrthologs()) {
            codonList = study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure);
        } else if (study.getOriginalGene().hasOrthologs()) {
            codonList = study.getOriginalGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure);
        }

        float averageContext = cut.getAverageCodonContext();
        float stdDeviationContext = cut.getStdDevCodonContext();

        float maxCodonContext = averageContext + 2.0F * stdDeviationContext;
        float minCodonContext = averageContext - 2.0F * stdDeviationContext;
        float maxDiference = maxCodonContext - minCodonContext;

        for (int i = 0; i < codonList.getLength() - 1; i++) {
            if (codonList.getWordAt(i).equals("---")) {
                resultColorVector.add(new Color(240, 240, 240));
            } else {
                float contextValue = cut.getCodonPairScore(codonList.getWordAt(i), codonList.getWordAt(i + 1));
                contextValue = Math.min(maxCodonContext, contextValue);
                contextValue = Math.max(minCodonContext, contextValue);
                float intensity = 255 - Math.round((maxCodonContext - contextValue) / maxDiference * 255.0F);

                resultColorVector.add(Color.getHSBColor(intensity / 750.0F, 1.0F, 0.8F));
            }
        }
        resultColorVector.add(new Color(240, 240, 240));

        return resultColorVector;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 20;
            colorScale = new ArrayList(size);

            for (float i = 0.0F; i < size; i += 1.0F) {
                float intensity = 255.0F - i / (size - 1) * 255.0F;
                colorScale.add(0, Color.getHSBColor(intensity / 750.0F, 0.5F, 0.8F));
            }
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "Codon Context color scale";
    }

    public String getScaleMinDescription() {
        return "no CPB";
    }

    public String getScaleMaxDescription() {
        return "max CPB";
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }

    static {
        colorScale = null;
    }
    
    public enum Parameter
    {
        MAXIMIZE_CODON_CONTEXT;
    }
}
