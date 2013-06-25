package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;

/**
 *
 * @author Paulo Gaspar
 */
public class SimulatedAnnealing
{
    private static float score;    
    

    /**
     * 
     * 
     * @param study Study to be optimized
     * @param seed Initial sequence to be optimized
     * @param kmax
     * @param selectedPlugins
     * @param parametersList
     * @param selecType
     * @param processPanel
     * @return 
     */
    public static String runSimulatedAnnealing( Study study, 
                                                String seed, 
                                                int kmax, 
                                                List<IOptimizationPlugin> selectedPlugins,                                                 
                                                OptimizationRunner.selectionType selecType,
                                                ProcessPanel processPanel)
    {
        Random rand = new Random();
        String s = seed;
        String sbest;
        StringBuilder snew;
        float e = 0, enew, ebest;
        int convergenceCounter = 0;        

        e = calculateEnergy(study, s, selectedPlugins);

        sbest  = s; ebest = e;
        int k = 0, randomPos; //kmax = 100000,
        float emax = 99.999f;
        double pacceptance;

        GeneticCodeTable geneticCodeTable = OptimizationRunner.getSelectedHost().getGeneticCodeTable();
        List<List<String>> synonymous = new ArrayList<List<String>>();
        for (int i=0; i<study.getResultingGene().getSequenceLength(); i++)
            synonymous.add(i,geneticCodeTable.getSynonymousFromAA(study.getResultingGene().getStructure(BioStructure.Type.proteinPrimaryStructure).getWordAt(i)));

        // if NOT_OPTIMIZE_SELECTION (preserve zone) is active save the zone to preserve
        String originalSubSeq = null;
        int startIndex = 0;
        int endIndex = 0;
        if( selecType == OptimizationRunner.selectionType.NOT_OPTIMIZE_SELECTION)
        {
                  startIndex = study.getSelectedStartIndex();
                  endIndex = study.getSelectedEndIndex()+1;
                  originalSubSeq = study.getOriginalGene().getCodonSubSequence(startIndex, endIndex);
        }
        
        /* Main loop. Don't stop while the counter doesn't reach the max, and while the max energy isn't achieved and while there isn't convergence. */
        while ((k < kmax) && (e < emax) && (convergenceCounter < 0.1*kmax) && !OptimizationRunner.isStopOptimization())
        {
              /* Obtain random neighbour. */
              snew = new StringBuilder(s);
              randomPos = rand.nextInt(s.length()/3);
              snew.replace(randomPos*3, randomPos*3+3, synonymous.get(randomPos).get(rand.nextInt(synonymous.get(randomPos).size())));
              
              /* If NOT_OPTIMIZE_SELECTION rollback to original subsequence */
              if( selecType == OptimizationRunner.selectionType.NOT_OPTIMIZE_SELECTION)
                  snew.replace(startIndex*3, endIndex*3, originalSubSeq);
              
              /* Calculate energy. */
//              enew = 0;
//              for (int i=0; i<selectedPlugins.size(); i++)
//                enew += selectedPlugins.get(i).getScoreOfSequence(study, snew.toString(), parametersList.get(i));
//              enew /= selectedPlugins.size();
              enew = calculateEnergy(study, snew.toString(), selectedPlugins);

              pacceptance = Math.exp(-(e - enew)/(kmax*Math.pow(0.90, k)));
              if ((enew > e) || (pacceptance > rand.nextFloat()))
              {
                  s = snew.toString();
                  e = enew;
              }

              /* Count number of repeated consecutive best scores to find convergence. */
              if (enew <= ebest)
                  convergenceCounter++;
              else
                  convergenceCounter = 0;

              if (enew > ebest)
              {
                  sbest = snew.toString();
                  ebest = enew;
                  processPanel.setStatus("Best score is " + new DecimalFormat("##.##").format(ebest)+"%");
              }

              k = k + 1;
        }

        score = ebest;

        return sbest;
    }
        
    private static float calculateEnergy(   Study study, 
                                            String sequence, 
                                            List<IOptimizationPlugin> selectedPlugins)
    {
        float e = 0;
        
        for (int k=0; k<selectedPlugins.size(); k++){            
            e += selectedPlugins.get(k).getScoreOfSequence(study, sequence);
        }
        
        e /= selectedPlugins.size(); //TODO: unnecessary operation.

        return e;
    }

    public static float getScore() {
        return score;
    }
}
