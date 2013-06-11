/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ieeta.geneoptimizer.GeneRedesign;

/**
 *
 * @author Paulo
 */
public class HillClimbing
{
//
//
//    /************* HILL CIMBING WITH RANDOM RESTARTS TEST ************/
//        int cicleCounter = 0;
//        float tmpScore1, tmpScore2, tempScore = 0, tempScoreBest = 0;
//        int position;
//        parent1 = population.getPopulation().last();
//        Population pop2;
//        GeneticCodeTable geneticCodeTable = OptimizationRunner.getSelectedHost().getGeneticCodeTable();
//        Vector<Vector<String>> synonymous;
//
//        /* Fill synonymous list. */
//        synonymous = new Vector<Vector<String>>();
//        for (int i=0; i<study.getResultingGene().getSequenceLength(); i++)
//            synonymous.add(i,geneticCodeTable.getSynonymousFromAA(study.getResultingGene().getStructure(BioStructure.Type.proteinPrimaryStructure).getWordAt(i)));
//        String bestSequence = null;
//        while (cicleCounter < 10)
//        {
//             cicleCounter++;
//
//             pop2 = GeneticAlgorithm.generatePopulationFromSequence(study, 10, selectedPlugins, parametersList, originalScores);
//             parent2 = pop2.getPopulation().first();
//             StringBuilder newSequence, oldSequence;
//             newSequence = new StringBuilder(parent2.getSequence());
//             oldSequence = new StringBuilder(parent2.getSequence());
//             float bestSubScore = 0;
//             position = 0;
//             while (position < parent2.getSequence().length()/3)
//             {
//                for (int j=0; j < synonymous.get(position).size(); j++)
//                {
//                    newSequence = new StringBuilder(oldSequence);
//                    newSequence.replace(position*3, position*3+3, synonymous.get(position).get(j));
//                    tempScore = 0;
//                    for (int k=0; k<selectedPlugins.size(); k++)
//                       tempScore +=  selectedPlugins.get(k).getScoreOfSequence(study, newSequence.toString(), parametersList.get(k));
//                    tempScore /= selectedPlugins.size();
//
//                    if (tempScore > bestSubScore)
//                    {
//                        bestSubScore = tempScore;
//                        oldSequence = new StringBuilder(newSequence);
//                        //System.out.println(newSequence.toString());
//                        if (tempScore > tempScoreBest)
//                        {
//                            tempScoreBest = tempScore;
//                            cicleCounter = 0;
//                            bestSequence = newSequence.toString();
//                            //System.out.println(new DecimalFormat("##.##").format(tempScore));
//                        }
//                    }
//                }
//
//                position++;
//             }
//        }
//
//        tempScore = 0;
//        for (int k=0; k<selectedPlugins.size(); k++)
//           tempScore +=  selectedPlugins.get(k).getScoreOfSequence(study, bestSequence, parametersList.get(k));
//        tempScore /= selectedPlugins.size();
//        System.out.println(new DecimalFormat("##.##").format(tempScore));
////        tmpScore1 = selectedPlugins.get(0).getScoreOfSequence(study, bestSequence, parametersList.get(0));
////        tmpScore2 = selectedPlugins.get(1).getScoreOfSequence(study, bestSequence, parametersList.get(1));
////        System.out.println(new DecimalFormat("##.##").format(tmpScore1) + " " + new DecimalFormat("##.##").format(tmpScore2));
//
//        System.out.println("ACABOU O HC! \n");
//        /********************************************/


}
