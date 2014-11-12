/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.codonusage;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.Main.Tuple;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import pt.ua.ieeta.geneoptimizer.geneDB.UsageAndContextTables;

/**
 *
 * @author Eduardo
 */
public class CodonUsagePlugin implements IOptimizationPlugin, ActionListener {
    private CodonUsageParametersPanel parametersPanel = null;
    private ParameterSet params;
    private Genome host;
    private boolean isSelected;
    private final int RARE_ORTH_PERCENTAGE = 80;
    private static List<Color> colorScale;

    public CodonUsagePlugin() {
        this.params = new ParameterSet();

        this.params.addParameter(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 2));
        this.params.addParameter(CodonUsagePlugin.Parameter.USAGE_TYPE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 1));
        this.params.addParameter(CodonUsagePlugin.Parameter.KEEP_RARE.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(false), 0, 0));
    }

    public Enum[] getAvailableParameters() {
        return CodonUsagePlugin.Parameter.values();
    }

    public String getPluginName() {
        return "Codon Usage";
    }

    public String getPluginId() {
        return "CODONUSAGE1";
    }

    public String getPluginVersion() {
        return "1.5";
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.parametersPanel = new CodonUsageParametersPanel();
            initComponentListener();
            this.isSelected = false;
        }
        return this.parametersPanel;
    }

    private void initComponentListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jCAIButton.addActionListener(this);
            this.parametersPanel.jHarmonizeUsage.addActionListener(this);
            this.parametersPanel.jMaximizeUsage.addActionListener(this);
            this.parametersPanel.jMinimizeUsage.addActionListener(this);
            this.parametersPanel.jRSCUButton.addActionListener(this);
            this.parametersPanel.jKeepRareCodons.addActionListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()) == null) {
            this.params.addParameter(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 2));
        }

        if (this.parametersPanel.jHarmonizeUsage.isSelected()) {
            this.params.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).setValue(Integer.valueOf(2));
        } else if (this.parametersPanel.jMaximizeUsage.isSelected()) {
            this.params.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).setValue(Integer.valueOf(0));
        } else if (this.parametersPanel.jMinimizeUsage.isSelected()) {
            this.params.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).setValue(Integer.valueOf(1));
        }

        if (this.params.getParamDetails(CodonUsagePlugin.Parameter.USAGE_TYPE.toString()) == null) {
            this.params.addParameter(CodonUsagePlugin.Parameter.USAGE_TYPE.toString(), new ParameterDetails(Integer.class, Integer.valueOf(0), 0, 2));
        }
        if (this.parametersPanel.jRSCUButton.isSelected()) {
            this.params.getParamDetails(CodonUsagePlugin.Parameter.USAGE_TYPE.toString()).setValue(Integer.valueOf(0));
        } else if (this.parametersPanel.jCAIButton.isSelected()) {
            this.params.getParamDetails(CodonUsagePlugin.Parameter.USAGE_TYPE.toString()).setValue(Integer.valueOf(1));
        }

        if (this.parametersPanel.jKeepRareCodons.isSelected()) {
            this.params.getParamDetails(CodonUsagePlugin.Parameter.KEEP_RARE.toString()).setValue(Boolean.valueOf(true));
        } else {
            this.params.getParamDetails(CodonUsagePlugin.Parameter.KEEP_RARE.toString()).setValue(Boolean.valueOf(false));
        }
    }

    public Study makeSingleOptimization(Study study) {
        assert (study != null);
        assert (study.getResultingGene() != null);
        assert (study.getResultingGene().getGenome() != null);
        assert (this.host != null);
        assert (study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure) != null);

        Integer codonUsageFunction = (Integer) this.params.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).getValue();
        boolean isKeepRare = ((Boolean) this.params.getParamDetails(CodonUsagePlugin.Parameter.KEEP_RARE.toString()).getValue()).booleanValue();

        List rareCodons = new ArrayList();
        if (isKeepRare) {
            rareCodons = buildRareCodonList(study);
        }
        int usageType;
        if (this.parametersPanel.jRSCUButton.isSelected()) {
            usageType = 0;
        } else {
            usageType = 1;
        }

        BioStructure codonSequence = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure);

        Genome originalGenome = study.getResultingGene().getGenome();

        StringBuilder finalSequence = new StringBuilder("");

        for (int i = 0; i < codonSequence.getSequence().length() / 3; i++) {
            String currentCodon = codonSequence.getSequence().substring(i * 3, i * 3 + 3);
            List<String> synonymous = this.host.getGeneticCodeTable().getSynonymousFromCodon(currentCodon);
            float maxCodonUsage = -1.0F;
            float minCodonUsage = 3.4028235E+38F;
            String maxCodonUsageCodon = null;
            String minCodonUsageCodon = null;

            float optimalScore = originalGenome.getCodonRSCU(currentCodon);
            float bestScore = (1.0F / 1.0F);
            String bestCodon = null;

            for (String codon : synonymous) {
                float codonUsage;
                switch (usageType) {
                    case 0:
                        codonUsage = this.host.getCodonRSCU(codon);
                        break;
                    case 1:
                        codonUsage = this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(codon);
                        break;
                    default:
                        codonUsage = 0.0F;
                }

                if (codonUsageFunction.intValue() == 2) {
                    System.out.print(new StringBuilder().append(codon).append("(").append(codonUsage).append(")   ").toString());

                    if (Math.abs(codonUsage - optimalScore) < Math.abs(bestScore - optimalScore)) {
                        bestScore = codonUsage;
                        bestCodon = codon;
                        continue;
                    }

                }

                if (codonUsage > maxCodonUsage) {
                    maxCodonUsage = codonUsage;
                    maxCodonUsageCodon = codon;
                }

                if (codonUsage < minCodonUsage) {
                    minCodonUsage = codonUsage;
                    minCodonUsageCodon = codon;
                }
            }
            if ((isKeepRare) && (!rareCodons.isEmpty())) {
                if ((((Boolean) ((Tuple) rareCodons.get(i)).getY()).booleanValue() == true) && (!((Tuple) rareCodons.get(i)).getX().equals(currentCodon))) {
                    bestCodon = (String) ((Tuple) rareCodons.get(i)).getX();
                    maxCodonUsageCodon = (String) ((Tuple) rareCodons.get(i)).getX();
                    minCodonUsageCodon = (String) ((Tuple) rareCodons.get(i)).getX();
                }

            }

            if (codonUsageFunction.intValue() == 2) {
                finalSequence.append(bestCodon);
            } else if (codonUsageFunction.intValue() == 0) {
                finalSequence.append(maxCodonUsageCodon);
            } else if (codonUsageFunction.intValue() == 1) {
                finalSequence.append(minCodonUsageCodon);
            }

        }

        Gene gene = new Gene(study.getResultingGene().getName(), this.host);
        gene.createStructure(finalSequence.toString(), BioStructure.Type.mRNAPrimaryStructure);
        gene.calculateAllStructures();

        Study newStudy = new Study(study.getResultingGene(), gene, new StringBuilder().append("[").append(this.host.getName()).append("]  ").append(gene.getName()).append(" (Redesigned by: Codon Usage)").toString());

        return newStudy;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        Study selectedStudy = ProjectManager.getInstance().getSelectedProject().getSelectedStudy();

        this.isSelected = selected;
        this.parametersPanel.jMaximizeUsage.setEnabled(selected);
        this.parametersPanel.jMinimizeUsage.setEnabled(selected);
        this.parametersPanel.jRSCUButton.setEnabled(selected);

        this.parametersPanel.jHarmonizeUsage.setEnabled(selected);
        this.parametersPanel.jKeepRareCodons.setEnabled(selected);

        if ((selectedStudy != null) && (selectedStudy.getResultingGene().hasCAI()) && (this.host != null) && (this.host.getHouseKeepingGenes() != null)) {
            this.parametersPanel.jCAIButton.setEnabled(selected);
        } else {
            this.parametersPanel.jCAIButton.setEnabled(false);
        }
    }

    public void setParameters(ParameterSet parameters) {
        if (this.parametersPanel != null) {
            if (parameters.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()) != null) {
                if (((Integer) parameters.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).getValue()).intValue() == 0) {
                    this.parametersPanel.jMaximizeUsage.setSelected(true);
                } else if (((Integer) parameters.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).getValue()).intValue() == 1) {
                    this.parametersPanel.jMinimizeUsage.setSelected(true);
                } else {
                    this.parametersPanel.jHarmonizeUsage.setSelected(true);
                }
            }
            if (this.params.getParamDetails(CodonUsagePlugin.Parameter.USAGE_TYPE.toString()) != null) {
                if (((Integer) parameters.getParamDetails(CodonUsagePlugin.Parameter.USAGE_TYPE.toString()).getValue()).intValue() == 0) {
                    this.parametersPanel.jRSCUButton.setSelected(true);
                    this.parametersPanel.jCAIButton.setSelected(false);
                } else {
                    this.parametersPanel.jCAIButton.setSelected(true);
                    this.parametersPanel.jRSCUButton.setSelected(false);
                }
            }
            if (this.params.getParamDetails(CodonUsagePlugin.Parameter.KEEP_RARE.toString()) != null) {
                if (((Boolean) parameters.getParamDetails(CodonUsagePlugin.Parameter.KEEP_RARE.toString()).getValue()).booleanValue() == true) {
                    this.parametersPanel.jKeepRareCodons.setSelected(true);
                } else {
                    this.parametersPanel.jKeepRareCodons.setSelected(false);
                }
            }
        }
        this.params = parameters;
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    private List<Tuple> buildRareCodonList(Study study) {
        assert (study != null);

        if (!study.getResultingGene().hasOrthologs()) {
            return new ArrayList();
        }
        if (!study.getResultingGene().hasAlignedStructure(BioStructure.Type.mRNAPrimaryStructure)) {
            return new ArrayList();
        }

        List rareCodons = new ArrayList();
        List orthologs = study.getResultingGene().getOrthologList().getGenes();
        String alignedSequence = study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();

        String codonSeq = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();
        int minNumberOrthsForRare = 80 * study.getResultingGene().getOrthologList().getGenes().size() / 100;

        for (int i = 0; i < alignedSequence.length() / 3; i++) {
            String currentCodon = alignedSequence.substring(i * 3, i * 3 + 3);
            if (!currentCodon.equals("---")) {
                if (study.getResultingGene().getGenome().getUsageAndContextTables().isCodonRare(currentCodon)) {
                    if (isRareConserved(currentCodon, orthologs, i, minNumberOrthsForRare)) {
                        rareCodons.add(new Tuple(currentCodon, Boolean.valueOf(true)));
                    } else {
                        rareCodons.add(new Tuple(currentCodon, Boolean.valueOf(false)));
                    }
                } else {
                    rareCodons.add(new Tuple(currentCodon, Boolean.valueOf(false)));
                }
            }
        }

        return rareCodons;
    }

    public void calculateBestScore(Study study) {
        assert (study != null);
        assert (this.params != null);

        Integer codonUsageFunction = (Integer) this.params.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).getValue();
        boolean isKeepRare = ((Boolean) this.params.getParamDetails(CodonUsagePlugin.Parameter.KEEP_RARE.toString()).getValue()).booleanValue();

        int usageType = ((Integer) this.params.getParamDetails(CodonUsagePlugin.Parameter.USAGE_TYPE.toString()).getValue()).intValue();
        StringBuilder sequenceDebug = new StringBuilder();

        String aaSequence = study.getResultingGene().getStructure(BioStructure.Type.proteinPrimaryStructure).getSequence();
        String codonSequence = study.getResultingGene().getStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence();

        List rareCodons = new ArrayList();
        if (isKeepRare) {
            rareCodons = buildRareCodonList(study);
        }

        Genome sourceGenome = study.getResultingGene().getGenome();
        List maxScore = new ArrayList();

        maxScore.add(Float.valueOf(0.0F));
        List minScore = new ArrayList();

        minScore.add(Float.valueOf(0.0F));
        List bestScore = new ArrayList(aaSequence.length());
        List worstScore = new ArrayList(aaSequence.length());

        for (int i = 0; i < codonSequence.length() / 3; i++) {
            String currentCodon = codonSequence.substring(i * 3, i * 3 + 3);

            List<String> synonymous = this.host.getGeneticCodeTable().getSynonymousFromAA(String.valueOf(aaSequence.charAt(i)));
            float maxCodonUsage = (1.0F / -1.0F);
            float minCodonUsage = (1.0F / 1.0F);
            float optimalScore;
            switch (usageType) {
                case 0:
                    optimalScore = sourceGenome.getCodonRSCU(currentCodon);
                    break;
                case 1:
                    optimalScore = sourceGenome.getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon);
                    break;
                default:
                    optimalScore = 0.0F;
            }

            worstScore.add(Float.valueOf(0.0F));
            bestScore.add(Float.valueOf((1.0F / 1.0F)));
            String codonDebug = null;
            for (String codon : synonymous) {
                float codonUsage;
                switch (usageType) {
                    case 0:
                        codonUsage = this.host.getCodonRSCU(codon);
                        break;
                    case 1:
                        codonUsage = this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(codon);
                        break;
                    default:
                        codonUsage = 0.0F;
                }

                if (codonUsageFunction.intValue() == 2) {
                    if (Math.abs(codonUsage - optimalScore) > Math.abs(((Float) worstScore.get(i)).floatValue() - optimalScore)) {
                        worstScore.set(i, Float.valueOf(codonUsage));
                    }

                    if (Math.abs(codonUsage - optimalScore) < Math.abs(((Float) bestScore.get(i)).floatValue() - optimalScore)) {
                        bestScore.set(i, Float.valueOf(codonUsage));
                        continue;
                    }
                }

                if (codonUsage > maxCodonUsage) {
                    maxCodonUsage = codonUsage;
                    codonDebug = new String(codon);
                } else if (codonUsage < minCodonUsage) {
                    minCodonUsage = codonUsage;
                }

            }

            if (codonUsageFunction.intValue() != 2) {
                if (maxCodonUsage != (1.0F / -1.0F)) {
                    maxScore.set(0, Float.valueOf(((Float) maxScore.get(0)).floatValue() + maxCodonUsage));
                    sequenceDebug.append(codonDebug);
                }

                if (minCodonUsage != (1.0F / 1.0F)) {
                    minScore.set(0, Float.valueOf(((Float) minScore.get(0)).floatValue() + minCodonUsage));
                }
            }

            if ((!isKeepRare) || (rareCodons.isEmpty())
                    || (((Boolean) ((Tuple) rareCodons.get(i)).getY()).booleanValue() != true)) {
                continue;
            }
            if (usageType == 1) {
                worstScore.set(i, Float.valueOf(sourceGenome.getCodonRSCU(currentCodon)));
                bestScore.set(i, Float.valueOf(sourceGenome.getCodonRSCU(currentCodon)));
                maxScore.set(0, Float.valueOf(sourceGenome.getCodonRSCU(currentCodon)));
                minScore.set(0, Float.valueOf(sourceGenome.getCodonRSCU(currentCodon)));
            } else {
                if (usageType != 2) {
                    continue;
                }
                worstScore.set(i, Float.valueOf(this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon)));
                bestScore.set(i, Float.valueOf(this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon)));
                maxScore.set(0, Float.valueOf(this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon)));
                minScore.set(0, Float.valueOf(this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon)));
            }

        }

        if (codonUsageFunction.intValue() == 2) {
            this.params.addParameter("Best Score", new ParameterDetails(ArrayList.class, bestScore, 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(ArrayList.class, worstScore, 0, 0));
        } else if (codonUsageFunction.intValue() == 0) {
            this.params.addParameter("Best Score", new ParameterDetails(ArrayList.class, maxScore, 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(ArrayList.class, minScore, 0, 0));
        } else {
            this.params.addParameter("Best Score", new ParameterDetails(ArrayList.class, minScore, 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(ArrayList.class, maxScore, 0, 0));
        }
    }

    public float getScoreOfSequence(Study study, String sequence) {
        assert (sequence != null);
        assert (this.params != null);
        assert (this.host != null);

        Integer codonUsageFunction = (Integer) this.params.getParamDetails(CodonUsagePlugin.Parameter.CODON_USAGE_FUNCTION.toString()).getValue();

        int usageType = ((Integer) this.params.getParamDetails(CodonUsagePlugin.Parameter.USAGE_TYPE.toString()).getValue()).intValue();
        boolean isKeepRare = ((Boolean) this.params.getParamDetails(CodonUsagePlugin.Parameter.KEEP_RARE.toString()).getValue()).booleanValue();

        List bestScore = null;
        List worstScore = null;
        float totalScore = 0.0F;
        List bestScoreH = null;
        List worstScoreH = null;

        List rareCodons = new ArrayList();
        if (isKeepRare) {
            rareCodons = buildRareCodonList(study);
        }

        if (codonUsageFunction.intValue() == 2) {
            bestScoreH = (ArrayList) this.params.getParamDetails("Best Score").getValue();
            worstScoreH = (ArrayList) this.params.getParamDetails("Worst Score").getValue();

            assert (bestScoreH != null);
            assert (worstScoreH != null);
            assert (bestScoreH.size() == sequence.length() / 3);
            assert (worstScoreH.size() == sequence.length() / 3);

            for (int i = 0; i < sequence.length() / 3; i++) {
                String currentCodon = sequence.substring(i * 3, i * 3 + 3);

                float codonUsage = 0.0F;
                switch (usageType) {
                    case 0:
                        if ((isKeepRare) && (!rareCodons.isEmpty())) {
                            if ((((Boolean) ((Tuple) rareCodons.get(i)).getY()).booleanValue() == true) && (!((Tuple) rareCodons.get(i)).getX().equals(currentCodon))) {
                                codonUsage = 0.0F;
                            } else {
                                codonUsage += this.host.getCodonRSCU(currentCodon);
                            }
                        } else {
                            codonUsage += this.host.getCodonRSCU(currentCodon);
                        }
                        break;
                    case 1:
                        if ((isKeepRare) && (!rareCodons.isEmpty())) {
                            if ((((Boolean) ((Tuple) rareCodons.get(i)).getY()).booleanValue() == true) && (!((Tuple) rareCodons.get(i)).getX().equals(currentCodon))) {
                                codonUsage = 0.0F;
                            } else {
                                codonUsage += this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon);
                            }
                        } else {
                            codonUsage += this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon);
                        }
                        break;
                    default:
                        codonUsage = 0.0F;
                }

                float value = 0.0F;
                if (((Float) bestScoreH.get(i)).floatValue() - ((Float) worstScoreH.get(i)).floatValue() != 0.0F) {
                    value = Math.abs(codonUsage - ((Float) bestScoreH.get(i)).floatValue()) * 100.0F / Math.abs(((Float) bestScoreH.get(i)).floatValue() - ((Float) worstScoreH.get(i)).floatValue());
                }

                totalScore += 100.0F - value;
            }

            totalScore /= sequence.length() / 3.0F;
        } else {
            bestScore = (ArrayList) this.params.getParamDetails("Best Score").getValue();
            worstScore = (ArrayList) this.params.getParamDetails("Worst Score").getValue();

            for (int i = 0; i < sequence.length() / 3; i++) {
                String currentCodon = sequence.substring(i * 3, i * 3 + 3);

                switch (usageType) {
                    case 0:
                        if ((isKeepRare) && (!rareCodons.isEmpty())) {
                            if ((((Boolean) ((Tuple) rareCodons.get(i)).getY()).booleanValue() == true) && (!((Tuple) rareCodons.get(i)).getX().equals(currentCodon))) {
                                totalScore = 0.0F;
                            } else {
                                totalScore += this.host.getCodonRSCU(currentCodon);
                            }
                        } else {
                            totalScore += this.host.getCodonRSCU(currentCodon);
                        }
                        break;
                    case 1:
                        if ((isKeepRare) && (!rareCodons.isEmpty())) {
                            if ((((Boolean) ((Tuple) rareCodons.get(i)).getY()).booleanValue() == true) && (!((Tuple) rareCodons.get(i)).getX().equals(currentCodon))) {
                                totalScore = 0.0F;
                            } else {
                                totalScore += this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon);
                            }
                        } else {
                            totalScore += this.host.getHouseKeepingGenes().getUsageAndContextTables().getCodonRelativeAdaptiveness(currentCodon);
                        }

                        break;
                    default:
                        totalScore += 0.0F;
                }

            }

            totalScore = 100.0F - Math.abs(totalScore - ((Float) bestScore.get(0)).floatValue()) * 100.0F / Math.abs(((Float) bestScore.get(0)).floatValue() - ((Float) worstScore.get(0)).floatValue());
        }

        assert (totalScore >= 0.0F);
        assert (totalScore <= 100.0F);

        return totalScore;
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        assert (study != null);
        assert (study.getResultingGene().getGenome() != null);
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

        float averageUsage = cut.getAverageRSCU();
        float stdDeviationUsage = cut.getStdDevRSCU();

        float maxCodonUsage = averageUsage + 2.0F * stdDeviationUsage;
        float minCodonUsage = averageUsage - 2.0F * stdDeviationUsage;
        float maxDiference = maxCodonUsage - minCodonUsage;

        assert (codonList != null);

        for (int i = 0; i < codonList.getLength(); i++) {
            if (codonList.getWordAt(i).equals("---")) {
                resultColorVector.add(new Color(240, 240, 240));
            } else {
                float usageValue = cut.getCodonUsageRSCU(codonList.getWordAt(i));
                usageValue = Math.min(maxCodonUsage, usageValue);
                usageValue = Math.max(minCodonUsage, usageValue);
                float intensity = Math.round(255.0F - (maxCodonUsage - usageValue) / maxDiference * 255.0F);

                resultColorVector.add(Color.getHSBColor(intensity / 750.0F, 0.7F, 0.8F));
            }
        }
        return resultColorVector;
    }

    public List<Color> colorScale() {
        if (colorScale == null) {
            int size = 20;
            colorScale = new ArrayList(size);

            for (float i = 0.0F; i < size; i += 1.0F) {
                float intensity = 255.0F - i / (size - 1) * 255.0F;
                colorScale.add(0, Color.getHSBColor(intensity / 750.0F, 0.7F, 0.8F));
            }
        }

        return colorScale;
    }

    public String getScaleDescription() {
        return "Codon Usage color scale";
    }

    public String getScaleMinDescription() {
        return "zero RSCU";
    }

    public String getScaleMaxDescription() {
        return "max RSCU";
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }

    private boolean isRareConserved(String codon, List<Gene> orthologs, int orthIdx, int minRareOrth) {
        int rareOrthcount = 0;
        for (int i = 0; i < orthologs.size(); i++) {
            if ((!((Gene) orthologs.get(i)).getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence().substring(orthIdx * 3, orthIdx * 3 + 3).equals("---")) && (((Gene) orthologs.get(i)).getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence().substring(orthIdx * 3, orthIdx * 3 + 3).equals(codon))) {
                rareOrthcount++;
            }

        }

        return rareOrthcount >= minRareOrth;
    }

    static {
        colorScale = null;
    }
    
    public enum Parameter {
        CODON_USAGE_FUNCTION,
        USAGE_TYPE,
        KEEP_RARE;
    }
}
