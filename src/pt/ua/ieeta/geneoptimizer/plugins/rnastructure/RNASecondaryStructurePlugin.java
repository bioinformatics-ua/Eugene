/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.rnastructure;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Eduardo
 */
public class RNASecondaryStructurePlugin implements IOptimizationPlugin, ActionListener {
    private RNASecondaryStructurePanel parametersPanel = null;
    private ParameterSet params;
    private boolean isSelected;
    private Genome host;
    private int AU = Character.valueOf('A').hashCode() + Character.valueOf('U').hashCode();
    private int CG = Character.valueOf('C').hashCode() + Character.valueOf('G').hashCode();
    private int UG = Character.valueOf('U').hashCode() + Character.valueOf('G').hashCode();

    public RNASecondaryStructurePlugin() {
        this.params = new ParameterSet();

        this.params.addParameter(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
    }

    public Enum[] getAvailableParameters() {
        return RNASecondaryStructurePlugin.Parameter.values();
    }

    public JPanel getParametersPanel() {
        if (this.parametersPanel == null) {
            this.isSelected = false;
            this.parametersPanel = new RNASecondaryStructurePanel();
            initComponentListener();
        }
        return this.parametersPanel;
    }

    private void initComponentListener() {
        if (this.parametersPanel != null) {
            this.parametersPanel.jMaximizeFreeEnergy.addActionListener(this);
            this.parametersPanel.jMinimizeFreeEnergy.addActionListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (this.params.getParamDetails(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString()) == null) {
            this.params.addParameter(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString(), new ParameterDetails(Boolean.class, Boolean.valueOf(true), 0, 0));
        }

        if (this.parametersPanel.jMaximizeFreeEnergy.isSelected()) {
            this.params.getParamDetails(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString()).setValue(Boolean.valueOf(true));
        } else if (this.parametersPanel.jMinimizeFreeEnergy.isSelected()) {
            this.params.getParamDetails(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString()).setValue(Boolean.valueOf(false));
        }
    }

    public Study makeSingleOptimization(Study study) {
        boolean isMaximize = ((Boolean) this.params.getParamDetails(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString()).getValue()).booleanValue();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
        this.parametersPanel.jMinimizeFreeEnergy.setEnabled(selected);
        this.parametersPanel.jMaximizeFreeEnergy.setEnabled(selected);
    }

    public ParameterSet getParameters() {
        return this.params;
    }

    public void setParameters(ParameterSet parameters) {
        if ((this.parametersPanel != null)
                && (parameters.getParamDetails(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString()) != null)) {
            if (((Boolean) parameters.getParamDetails(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString()).getValue()).booleanValue() == true) {
                this.parametersPanel.jMaximizeFreeEnergy.setSelected(true);
            } else {
                this.parametersPanel.jMinimizeFreeEnergy.setSelected(true);
            }
        }

        this.params = parameters;
    }

    public void calculateBestScore(Study study) {
        int sequenceLen = study.getResultingGene().getSequenceLength() * 3;

        int max = (3 * sequenceLen / 2 + 6) * ((sequenceLen / 2 - 1) / 2) * 2;
        int min = 0;

        boolean isMaximize = ((Boolean) this.params.getParamDetails(RNASecondaryStructurePlugin.Parameter.MAXIMIZE_FREE_ENERGY.toString()).getValue()).booleanValue();

        if (isMaximize) {
            this.params.addParameter("Best Score", new ParameterDetails(Integer.class, Integer.valueOf(max), 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(min), 0, 0));
        } else {
            this.params.addParameter("Best Score", new ParameterDetails(Integer.class, Integer.valueOf(min), 0, 0));
            this.params.addParameter("Worst Score", new ParameterDetails(Integer.class, Integer.valueOf(max), 0, 0));
        }
    }

    public float getScoreOfSequence(Study study, String sequence) {
        int pseudo_energy = getPseudoEnergie(sequence);

        float c1 = pseudo_energy - ((Integer) this.params.getParamDetails("Best Score").getValue()).intValue();
        float c2 = ((Integer) this.params.getParamDetails("Worst Score").getValue()).intValue() - ((Integer) this.params.getParamDetails("Best Score").getValue()).intValue();

        return 100.0F - Math.abs(c1 / c2) * 100.0F;
    }

    private int getPseudoEnergie(String sequence) {
        int pseudo_energy = 0;
        int seqLen = sequence.length();

        final List seqChar = new ArrayList(seqLen);
        for (int i = 0; i < sequence.length(); i++) {
            seqChar.add(new Character(sequence.charAt(i)));
        }

        RunnableFuture foldingBegin = new FutureTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int pseudo_energy = 0;

                for (int block_size = 2; block_size <= seqChar.size() / 2; block_size++) {
                    pseudo_energy += attraction(seqChar.subList(0, block_size), seqChar.subList(block_size, 2 * block_size));
                }

                return pseudo_energy;
            }
        });

        RunnableFuture foldingEnd = new FutureTask(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int pseudo_energy = 0;

                for (int block_size = 2; block_size <= seqChar.size() / 2; block_size++) {
                    pseudo_energy += attraction(seqChar.subList(seqChar.size() - block_size, seqChar.size()), seqChar.subList(seqChar.size() - block_size * 2, seqChar.size() - block_size));
                }

                return pseudo_energy;
            }
        });

        new Thread(foldingBegin).start();
        new Thread(foldingEnd).start();
        try {
            pseudo_energy = ((Integer) foldingBegin.get()).intValue() + ((Integer) foldingEnd.get()).intValue();
        } catch (InterruptedException ex) {
            Logger.getLogger(RNASecondaryStructurePlugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(RNASecondaryStructurePlugin.class.getName()).log(Level.SEVERE, null, ex);
        }

        assert (pseudo_energy != 0);

        return pseudo_energy;
    }

    private synchronized int attraction(List<Character> seq1, List<Character> seq2) {
        int pseudoEnergie = 0;
        int len = seq1.size();

        for (int i = 0; i < len; i++) {
            if (((Character) seq1.get(i)).hashCode() + ((Character) seq2.get(len - i - 1)).hashCode() == this.AU) {
                pseudoEnergie += 2;
            } else if (((Character) seq1.get(i)).hashCode() + ((Character) seq2.get(len - i - 1)).hashCode() == this.CG) {
                pseudoEnergie += 3;
            } else if (((Character) seq1.get(i)).hashCode() + ((Character) seq2.get(len - i - 1)).hashCode() == this.UG) {
                pseudoEnergie++;
            }
        }
        return pseudoEnergie;
    }

    public List<Color> makeAnalysis(Study study, boolean useAlignedGene) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean needsGeneticAlgorithm() {
        return true;
    }

    public String getPluginName() {
        return "RNA Secondary Structure";
    }

    public String getPluginId() {
        return "RNASECONDARYSTRUCTURE1";
    }

    public String getPluginVersion() {
        return "1.0";
    }

    public static void main(String[] args) {
        RNASecondaryStructurePlugin p = new RNASecondaryStructurePlugin();

        String sequence = "AUGCACACGGGCAGCACGACGCUGCCCGAUUUUUUUGCAGGGAUGAGCGAUGAUUUCACGCCGCCGAUUUUUGCAGGCUACUGCCGCGACGAUAGCCACGAGCUCAGGUUUCGGUUAUAUGCAUUACUGUGA";
        String sequence2 = "GCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGCGC";
        String sequence3 = "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC";
        String sequence4 = "GCGCGC";
        String sequence5 = "AUGAGUGAAGCAAGAAGGGGUAUCUUCCCCUUCUCAAAAAUUAAAUUGAUGCUCGCUUCUCCCGAGGAUAUCAGAAGCUGGUCUCACGGUGAAGUUAAGAGACCUGAAACUCUAAACUACAGAACUCUGAAACCCGAAAAGGACGGUCUCUUCUGCGCUAAGAUUUUUGGUCCCAUAAAAGAUUACGAGUGUCUCUGCGGAAAGUACAGGGGAAAGAGGUACGAAGGAAAGAUAUGUGAAAAGUGCGGUGUUGAAGUUACAACUUCUUACGUAAGGAGACAGAGGUUCGGUCACAUAGAACUCGCUGCUCCCGUCGUUCACAUAUGGUUCUUAAAGAGCACACCCUCCAAGAUAGGAACGCUCCUUAACCUCACUUCAAGAGACGUUGAAAGGGUCGCUUACUUUGAGUCUUACCUCGUUAUUGAGUAUCCAAACGAGGAAGAAGAGGAAAAGUUUGAGAAGGACGAGCACACGAUACCACUCAACGACGGCAUUUCCACCAAGUGGGUAAAACUCCACGUGGUGAACGAGGAGGAGUUUGAGGAGAAGUACGCCUUCACCAUAGACGAAAAGUACGAGCACGGAAUGGGGGCGGAAAUACUCAAAGAAGUUCUAUCAAAACUCGACCUAGACGCCUACUCCAGAAAACUGAAGGAAAUAGUAAAGCCCUACUCAAUAGGAUUUGAAGACCUCGGAAAAGAAAUUGAACAGAAGUACAAGAACCUUUACCAGAAACUUAUCAAGGUUAUCGCGGACGACUUCAGGGCUUACGGCGUUGAGAUAAAAGGACUUGAAGAUCACGGACUCAGCCUAGAACAGGCUAUCCACAGGAUUCUAAACGAAGAACUGUACCUCAAUGUAGAGACGGGAGAAAUUUCCCUUGAAGACUGCGGAGACAGCUGUCUCACGGGAAGGGACGCUCUGAAGGAGUACUACGAAAGGGUAAGGGAACACAAAAAGGACAUCCCCAUCUUUGAAAAGAUAAAGGAAGACAUAAGAUCCACGGUACUGAGGGAAAUAUCCGAAGCCAGAAUCAGGAAGGCGCUCAGAACUCUCCAGCUAGUAGAGGGCUUCAAGAAGAGCGGAAACAGACCAGAGUGGAUGAUACUUGAAGUUCUUCCAGUUCUUCCUCCGGAACUACGUCCCCUCGUUGCACUGGACGGAGGAAGGUUUGCAACUUCUGAUCUGAACGACUUCUACAGAAGGGUUAUAAACAGGAAUAACAGACUAAAGAGAUUAAUAGAACUGAACGCCCCCGACAUAAUCAUCAGAAACGAAAAGAGAAUGCUUCAGGAGGCGGUUGAUGCCUUAAUAGACAACGGAAAGAGAGGAAACCCUGUAAAGCAAAAUGGAAGACCUUUAAAGUCUCUUGCGGACUACCUGAAAGGUAAACAGGGAAGGUUUAGGCAAAACCUCCUCGGAAAGAGAGUUGACUAUUCAGGACGUUCCGUUAUAGUCGUCGGUCCAGAACUCCAGAUGCACCAGUGCGGUCUUCCCAAGAUAAUGGCCCUCGAACUCUUCAAACCCUUCGUAUACAGGAGACUGGAAGAAAAAGGGUAUGCGACUUCCAUAAAACACGCAAAGAGACUCGUAGAGCAAAAGACACCCGAAGUAUGGGAGUGCCUUGAAGAAGUGGUCAAAGAACACCCCGUUCUGCUAAACCGUGCUCCAACGCUCCACAGACCUUCAAUUCAGGCUUUUGAACCCGUUCUCGUAGAAGGAAAAGCCAUACAGCUACACCCCCUCGUUUGCCCGCCCUUUAACGCGGACUUUGACGGAGACCAAAUGGCCGUACACGUCCCCCUCGGAAUAGAGGCUCAGCUUGAGUCUUACAUACUUAUGCUCUCCACGCAAAACGUACUUUCCCCCGCACACGGAAAACCCCUCACGAUGCCCUCACAGGACAUGGUUCUUGGGACUUACUACAUCACCCACGACCCCAUACCCGGAAGGAAAGGAGAAGGAAAAGCUUUCGGUACCUUUGAAGAAGUCCUCAAAGCUCUUGAACUCGGACACGUUGACAUACACGCGAAGAUAAAGGUAAAAGUUGGAAACGAGUGGAUAGAAACAACUCCCGGAAGGGUACUCUUCAACUCCAUAAUGCCCGAAGGACAGCCCUUCGUUAACAAAACCCUUGACAAGAAAGGACUUUCAAAGCUAAUCACUGAACUCUAUAUAAGGGUAGGAAACGAGGAGACUGUUAAGUUCCUUGACCGUGUGAAAGAACUUGGAUUCCUCAGGUCAACGCUUGCGGGUAUUUCCAUUGGUGUGGAAGACCUGCAGGUUCCCAAGGCAAAGAAGAAGAUAAUAGAGGAAGCCCUCAAGAAGACGGAAGAGAUAUGGAACCAGUACGUUCAGGGUAUCAUCACCAACAAGGAGAGGUACAACAGGAUAAUAGACGUCUGGUCCGAAGCCACGAACCUCGUUUCCAAGGCGAUGUUUGAAGAGAUAGAAAAGUCUAAGAGAAUAGAAAACGGAAAGGAAUACCCGGGAACCUUUAACCCCAUAUACAUGAUGGCAAUCUCUGGAGCGAGAGGUAACAGGGACCAGAUAAGACAGCUUGCAGGAAUGAGAGGUCUUAUGGCUAAGCACUCUGGUGAGUUUAUAGAAACGCCGAUUAUCUCAAACUUCAGGGAAGGUCUUUCCGUUCUCGAGUACUUUAUAUCCACUUACGGUGCGAGAAAGGGUCUCGCGGACACCGCCCUGAAAACCGCAUUUGCCGGAUAUCUGACGAGAAGACUCGUUGACGUAGCUCAGGACAUAACCAUUACUGAACGUGACUGCGGAACCGUUAAAGGCUUUGAAAUGGAACCCAUAGUAGAAGCGGGUGAAGAGAGGGUUCCCCUCAAGGACAGAAUAUUCGGAAGGGUUCUCGCGGAAGACGUAAAAGAUCCCUACACCGGUGAGAUAAUCGCAAGGAGAAACGAGGUAAUAGACGAAAAACUCGCGGAAAAGAUAACGAAGGCGGGAAUAGAAAAGGUAAGGGUACGCUCACCUCUCACCUGUGAGGCAAAACACGGCGUUUGUGCUAUGUGCUACGGAUGGGAUCUCUCCCAGAGGAAGAUAGUAUCCGUGGGUGAGGCGGUUGGAAUUAUAGCAGCACAAUCCAUAGGUGAGCCCGGAACACAGCUCACCAUGAGAACCUUCCACAUAGGUGGUGCAGCAACAGCUCAGAAGGUUCAGAGCUUCGUAAAGGCUGAAAGUGACGGUAAAGUCAAGUUCUACAACGUAAAACUCAUAGUAAACAGAAAGGGUGAAAAGAUAAACAUCUCCAAGGACGCAGCUAUCGGAAUAGUGGACGAAGAAGGCAGGCUCCUUGAAAGACACACUAUACCUUACGGUGCGAGAAUUCUCGUAGAAGAAGGCCAGGAAGUUAAAGCAGAAACUAAACUCGCAGAUUGGGAUCCCUUCAACACUUACAUAAUCGCUGAAGUAGGCGGAAAGGUAGAACUCAGGGACAUAAUCCUCGACGUUACCGUAAGGGAAGAAAGGGAUCCGAUAACAGGAAAGACGGCGAGCGUAAUAUCCUUCAUGAGACCAAGAGACGCUAUGCUACACACUCCCAGAAUAGCUGUCAUCACAGAAGACGGGAAAGAGUACAUCUACGACCUUCCUGUUAACGCCAUUCUCAACAUACCGCCCGAAAAGAUAUCCCUCGAGUGGAGAGUAUGCCCCACCUGCUCCGAGUCCGAAGAGACCACUAUACAGCACCAGUACUACGUGGUUAAAGACCUUGAAGUUCAACCCGGUGACAUACUCGCGAGAAUUCCAAAAGAAACUGCUAAGGUUAGGGACAUCGUCGGUGGUCUUCCGAGGGUUGAAGAGCUCUUUGAGGCGAGAAAGCCCAAGAAUCCCGCAAUACUCAGCGAAAUAGACGGUUACGUUAAGAUUUACGAGGACGCGGACGAGGUGAUCAUAUUCAACCCCAGAACCGGUGAAACCGCUAAGUACUCCAUAAAGAAGGACGAGCUAAUCCUCGUAAGACACGGUCAAUUCGUCAAGAAAGGUCAAAAGAUUACGGAAACAAAGGUUGCGGAAAUAGACGGUCAGGUAAGGAUUAAGGGAAGAGGCUUUAAGGUAAUCGUUUACAACCCUGAAACAGGACUACAGAGGGAGUACUUCGUUCCCAAAGGUAAAUUCCUGCUCGUUAAGGAAGGUGACUUCGUAAAGGCAGGGGACCAGCUCACAGACGGAACACCCGUACCCGAAGAAAUACUCAGGAUAAAGGGAAUAGAAGAGCUGGAAAAAUUCCUCCUUAAAGAAGUCCAGAUGGUUUACAAAUUGCAAGGUGUUGACAUAAACGACAAACACUUUGAAAUCAUAAUCAAGCAGAUGCUCAAGAAGGUGAGGAUUAUAGACCCCGGAGACAGCAGAUUCCUCGUAGGCGAAGAAGUGGACAAAGAAGAACUCGAAGAAGAAAUUCAAAGGAUCAAGCUCGAAGGCGGAAAACUGCCCAAGGCUGAACCCGUACUCGUCGGUAUUACGAGAGCAGCACUUUCCACGAGAAGCUGGAUUUCCGCUGCAUCCUUCCAGGAAACUACGAGAGUACUCACAGACGCUUCCGUUGAAGGUAAGAUAGACGAACUCCGAGGUCUUAAAGAGAACGUAAUAAUCGGAAACAUAAUACCCGCCGGAACGGGUGUGGACGAGUACAGAGAAGUGGACGUAAUCCCUGCGGAAGAAAAGGUUCUCGAAGAGAAGAAAGAACCCAAAGAGGGCUCUUAA";

        int sequenceLen = sequence.length();

        int aux = 0;
        long start = System.currentTimeMillis();
        int e = 0;
        while (aux < 500) {
            e += p.getPseudoEnergie(sequence5);
            aux++;
        }
        System.out.println("e: " + e);
        System.out.println("total = " + (System.currentTimeMillis() - start));
    }

    public List<Color> colorScale() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getScaleDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getScaleMinDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getScaleMaxDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setHost(Genome genome) {
        this.host = genome;
    }

    public enum Parameter {
        MAXIMIZE_FREE_ENERGY;
    }
}
