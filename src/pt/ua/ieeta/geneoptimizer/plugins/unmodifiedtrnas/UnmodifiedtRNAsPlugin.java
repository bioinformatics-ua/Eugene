/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.unmodifiedtrnas;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.swing.JPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Eduardo
 */
public class UnmodifiedtRNAsPlugin implements IOptimizationPlugin, ActionListener {
    private UnmodifiedtRNAsParametersPanel parametersPanel = null;
    private boolean isSelected;
    private ParameterSet params;
    private final List<String> codonsAvoidBacterias;
    private final List<String> codonsAvoidEukaryote;
    private Genome host;
    private static List<Color> colorScale;

    public UnmodifiedtRNAsPlugin() {
        this.codonsAvoidBacterias = new ArrayList(5);
        this.codonsAvoidBacterias.add("ACC");
        this.codonsAvoidBacterias.add("CCC");
        this.codonsAvoidBacterias.add("GCC");
        this.codonsAvoidBacterias.add("GUC");
        this.codonsAvoidBacterias.add("CGG");

        this.codonsAvoidEukaryote = new ArrayList(6);
        this.codonsAvoidEukaryote.add("ACG");
        this.codonsAvoidEukaryote.add("CCG");
        this.codonsAvoidEukaryote.add("GCG");
        this.codonsAvoidEukaryote.add("GUG");
        this.codonsAvoidEukaryote.add("UCG");
        this.codonsAvoidEukaryote.add("CGG");

        this.params = new ParameterSet();
        this.params.addParameter(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
    }

    public Enum[] getAvailableParameters() {
        return UnmodifiedtRNAsPlugin.Parameter.values();
    }

    public String getPluginName() {
        return "Unmodified tRNAs";
    }

    public String getPluginId() {
        return "UNMODIFIEDTRNAS1";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.parametersPanel = new UnmodifiedtRNAsParametersPanel();
            setSelected(false);
            initComponentListener();
        }
        return this.parametersPanel;
    }

    private void initComponentListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jBacteriasButton.addActionListener(this);
            this.parametersPanel.jEukaryotesButton.addActionListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString()) == null) {
            this.params.addParameter(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
        }

        if (this.parametersPanel.jBacteriasButton.isSelected()) {
            this.params.getParamDetails(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString()).setValue(Boolean.valueOf(true));
        } else if (this.parametersPanel.jEukaryotesButton.isSelected()) {
            this.params.getParamDetails(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString()).setValue(Boolean.valueOf(false));
        }
    }

    public Study makeSingleOptimization(Study study) {
        List codons = new ArrayList();
        List synonymous = new ArrayList();
        try {
            initializeSynonymous(codons, synonymous);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BioStructure condonSequence = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);
        StringBuilder sequence = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < study.getResultingGene().getSequenceLength(); i++) {
            if (codons.contains(condonSequence.getWordAt(i))) {
                int index = codons.indexOf(condonSequence.getWordAt(i));
                int randInt = random.nextInt(((List) synonymous.get(index)).size());
                String newCodon = (String) ((List) synonymous.get(index)).get(randInt);
                sequence.append(newCodon);
            } else {
                sequence.append(condonSequence.getWordAt(i));
            }
        }
        Gene gene = new Gene(study.getResultingGene().getName(), this.host);
        gene.createStructure(sequence.toString(), BioStructure.Type.mRNAPrimaryStructure);
        gene.calculateAllStructures();

        Study newStudy = new Study(study.getResultingGene(), gene, new StringBuilder().append("[").append(this.host.getName()).append("]  ").append(gene.getName()).append(" (Redesigned by: ").append(getPluginName()).append(")").toString());

        return newStudy;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.parametersPanel.jBacteriasButton.setEnabled(selected);
        this.parametersPanel.jEukaryotesButton.setEnabled(selected);
    }

    public void setParameters(ParameterSet parameters) {
        if ((this.parametersPanel != null)
                && (parameters.getParamDetails(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString()) != null)) {
            if (((Boolean) parameters.getParamDetails(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString()).getValue()).booleanValue() == true) {
                this.parametersPanel.jBacteriasButton.setSelected(true);
                this.parametersPanel.jEukaryotesButton.setSelected(false);
            } else {
                this.parametersPanel.jBacteriasButton.setSelected(false);
                this.parametersPanel.jEukaryotesButton.setSelected(true);
            }
        }

        this.params = parameters;
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    public void calculateBestScore(Study study) {
        List codons = new ArrayList();
        List synonymous = new ArrayList();
        try {
            initializeSynonymous(codons, synonymous);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        BioStructure condonSequence = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);

        float bestScore = 0.0F;

        for (int i = 0; i < study.getResultingGene().getSequenceLength(); i++) {
            if (codons.contains(condonSequence.getWordAt(i))) {
                int index = codons.indexOf(condonSequence.getWordAt(i));
                if (((List) synonymous.get(index)).isEmpty()) {
                    bestScore += 1.0F;
                }

            }

        }

        float worstScore = 0.0F;
        List allCodons = new ArrayList();
        allCodons.addAll(codons);
        for (Iterator it = synonymous.iterator(); it.hasNext();) {
            allCodons.addAll((Collection) it.next());
        }
        for (int i = 0; i < study.getResultingGene().getSequenceLength(); i++) {
            if (allCodons.contains(condonSequence.getWordAt(i))) {
                worstScore += 1.0F;
            }

        }

        this.params.addParameter("Best Score", new ParameterDetails(Float.class, Float.valueOf(bestScore), 0, 0));
        this.params.addParameter("Worst Score", new ParameterDetails(Float.class, Float.valueOf(worstScore), 0, 0));
    }

    private void initializeSynonymous(List<String> codons, List<List<String>> synonymous)
            throws Exception {
        Iterator it;
        if (((Boolean) this.params.getParamDetails(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString()).getValue()).booleanValue() == true) {
            codons.addAll(this.codonsAvoidBacterias.subList(0, this.codonsAvoidBacterias.size()));
            for (it = codons.iterator(); it.hasNext();) {
                String codon = (String) it.next();
                List aux = new ArrayList();
                for (int i = 0; i < this.host.getGeneticCodeTable().getSynonymousFromCodon(codon).size(); i++) {
                    aux.add(this.host.getGeneticCodeTable().getSynonymousFromCodon(codon).get(i));
                }
                synonymous.add(aux);
            }
        } else {
            if (!((Boolean) this.params.getParamDetails(UnmodifiedtRNAsPlugin.Parameter.BACTERIAS.toString()).getValue()).booleanValue()) {
                codons.addAll(this.codonsAvoidEukaryote.subList(0, this.codonsAvoidEukaryote.size()));
                for (it = codons.iterator(); it.hasNext();) {
                    String codon = (String) it.next();
                    List aux = new ArrayList();
                    for (int i = 0; i < this.host.getGeneticCodeTable().getSynonymousFromCodon(codon).size(); i++) {
                        aux.add(this.host.getGeneticCodeTable().getSynonymousFromCodon(codon).get(i));
                    }
                    synonymous.add(aux);
                }
            } else {
                throw new Exception("Unmodified tRNAs Plugin: Invalid options");
            }
        }
        for (int i = 0; i < synonymous.size(); i++) {
            for (int j = 0; j < ((List) synonymous.get(i)).size(); j++) {
                if (((String) ((List) synonymous.get(i)).get(j)).equalsIgnoreCase((String) codons.get(i))) {
                    ((List) synonymous.get(i)).remove(j);
                }
            }
        }

        assert (!codons.isEmpty());
        assert (!synonymous.isEmpty());
    }

    public float getScoreOfSequence(Study study, String sequence) {
        int numberOfFoundCodons = 0;
        float bestScore = ((Float) this.params.getParamDetails("Best Score").getValue()).floatValue();
        float worstScore = ((Float) this.params.getParamDetails("Worst Score").getValue()).floatValue();

        if (bestScore == worstScore) {
            return 100.0F;
        }

        List codons = new ArrayList();
        List synonymous = new ArrayList();
        try {
            initializeSynonymous(codons, synonymous);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        for (int i = 0; i < sequence.length(); i += 3) {
            if (codons.contains(sequence.substring(i, i + 3))) {
                numberOfFoundCodons++;
            }
        }

        float result = 100.0F - Math.abs(numberOfFoundCodons - bestScore) / Math.abs(bestScore - worstScore) * 100.0F;
        return result;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        List codons = new ArrayList();
        List synonymous = new ArrayList();
        try {
            initializeSynonymous(codons, synonymous);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String sequence = null;
        if (!useAlignedGene) {
            sequence = study.getResultingGene().getCodonSequence();
        } else if (study.getResultingGene().hasOrthologs()) {
            sequence = study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();
        } else if (study.getOriginalGene().hasOrthologs()) {
            sequence = study.getOriginalGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();
        }

        List resultingColorVector = new ArrayList(sequence.length());

        for (int i = 0; i < sequence.length(); i += 3) {
            if (codons.contains(sequence.substring(i, i + 3))) {
                resultingColorVector.add(Color.DARK_GRAY);
            } else {
                resultingColorVector.add(Color.LIGHT_GRAY);
            }
        }

        return resultingColorVector;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 2;
            colorScale = new ArrayList(size);

            colorScale.add(Color.LIGHT_GRAY);
            colorScale.add(Color.DARK_GRAY);
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "Unmodified tRNAs color scale";
    }

    public String getScaleMinDescription() {
        return "normal";
    }

    public String getScaleMaxDescription() {
        return "unmodified";
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }

    static {
        colorScale = null;
    }
    
    public enum Parameter {
        BACTERIAS;
    }
}
