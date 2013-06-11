/**
 * GeneticAlgorithm.java
 */
package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure.Type;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;

/**
 * Class that implements a genetic algorithm 
 *
 * @author Paulo Gaspar
 */
public class GeneticAlgorithm
{
    private final static Random rand = new Random();
    private static Vector<Vector<String>> synonymous;

    //high k leads to low probability of mutation
    static int k = (Integer) ApplicationSettings.getInstance().getProperty("probabilityOfMutationRegulator", Integer.class); // 33;

    /**
     * Generate many individual solutions randomly generated 
     * to form an initial population.
     * 
     * @param study base study to generate the population
     * @param populationSize the size of desire population
     * @param plugins plugins to be evaluated
     * @param parametersList parameters of the plugins
     * @param originalScores original scores of the plugins
     * @return the initial population
     */
    public static Population generatePopulationFromSequence(    Study study, 
                                                                int populationSize, 
                                                                Vector<IOptimizationPlugin> plugins, 
                                                                Vector<ParameterSet> parametersList, 
                                                                Vector<Float> originalScores,
                                                                OptimizationRunner.selectionType selecType)
    {
        assert study != null;
        assert plugins != null;
        assert originalScores != null;
        assert populationSize > 0;

        Population population = new Population(populationSize);
        GeneticCodeTable geneticCodeTable = OptimizationRunner.getSelectedHost().getGeneticCodeTable();
        int sequence_length = study.getResultingGene().getSequenceLength();
        
        /* Fill synonymous list. */
        synonymous = new Vector<Vector<String>>();
        for (int i=0; i< sequence_length; i++)
            synonymous.add(i,geneticCodeTable.getSynonymousFromAA(study.getResultingGene().getStructure(Type.proteinPrimaryStructure).getWordAt(i)));

        
        // if NOT_OPTIMIZE_SELECTION (preserve zone) is active save the zone to preserve
        String originalSubSeq = null;
        int startIndex = 0;
        int endIndex = 0;
        if( selecType == OptimizationRunner.selectionType.NOT_OPTIMIZE_SELECTION) {
                  startIndex = study.getSelectedStartIndex();
                  endIndex = study.getSelectedEndIndex()+1;
                  originalSubSeq = study.getOriginalGene().getCodonSubSequence(startIndex, endIndex);
        }
        
        int counter = 0;
        while ((population.getSize() < populationSize) && (counter < 1000000))
        {
            /* Build new sequence based on the seed. */
            StringBuilder newSequence = new StringBuilder();
            for (int i=0; i< sequence_length; i++) {
                    newSequence.append(synonymous.get(i).get( rand.nextInt(synonymous.get(i).size()) ));
            }
            /*
             * If NOT_OPTIMIZE_SELECTION rollback to original subsuquence
             */
            if (selecType == OptimizationRunner.selectionType.NOT_OPTIMIZE_SELECTION) {
                newSequence.replace(startIndex * 3, endIndex * 3 + 3, originalSubSeq);
            }

            Individual newIndividual = new Individual(newSequence.toString(), 0);
            newIndividual.setScore(GeneticAlgorithm.getFitnessScore(
                    study, 
                    newIndividual.getSequence(), 
                    plugins, 
                    parametersList, 
                    originalScores)
                    );
            population.addIndividual(newIndividual);
            counter++;
        }

        //System.out.println("  Population size: " + population.getPopulation().size());

        return population;
    }

    static float score;
    public static float getFitnessScore(Study study, String sequence, Vector<IOptimizationPlugin> selectedPlugins, Vector<ParameterSet> parametersList, Vector<Float> originalScores)
    {
        assert study != null;
        assert sequence != null;
        assert sequence.length() > 0;
        assert selectedPlugins != null;
        assert selectedPlugins.size() > 0;
        assert parametersList != null;
        assert parametersList.size() > 0;

        score = 0;
        for (int i=0; i<selectedPlugins.size(); i++)
            score += ( selectedPlugins.get(i).getScoreOfSequence(study, sequence));
        
        assert score/selectedPlugins.size() <=  100;
        assert score/selectedPlugins.size() >= -100;

        return score/selectedPlugins.size();
    }


    static StringBuilder offSpring1 = new StringBuilder();
    static StringBuilder offSpring2 = new StringBuilder();
    public static Vector<String> makeCrossover(Vector<String> parents, int generation, int maxGeneration)
    {
        assert parents != null;
        assert parents.size() >= 2;
        assert generation >= 0;
        assert maxGeneration >= 0;

        offSpring1.delete(0, Integer.MAX_VALUE);
        offSpring2.delete(0, Integer.MAX_VALUE);
        int chosenParent;

        //System.out.println(100/Math.pow(generation, 0.6));

        for (int i=0; i<parents.firstElement().length(); i+=3)
        {
            /* Introduce random change with a variable probability */
            if (rand.nextInt(100) <= ((maxGeneration-generation)*100)/(k*maxGeneration))
            {
                offSpring1.append(synonymous.get(i/3).get(rand.nextInt(synonymous.get(i/3).size())));
                offSpring2.append(synonymous.get(i/3).get(rand.nextInt(synonymous.get(i/3).size())));
            }
            else
            {
                chosenParent = rand.nextInt(parents.size());
                offSpring1.append(parents.get(chosenParent).substring(i, i+3));
                offSpring2.append(parents.get(1-chosenParent).substring(i, i+3));
            }
        }

        Vector<String> offSpring = new Vector<String>(2);
        offSpring.add(offSpring1.toString());
        offSpring.add(offSpring2.toString());

//        System.out.println("Parent1:    " + parents.get(0));
//        System.out.println("Parent2:    " + parents.get(1));
//        System.out.println("Offspring1: " + offSpring1.toString());
//        System.out.println("Offspring2: " + offSpring2.toString());
        
        return offSpring;
    }

    public static void runGeneticAlgorithm( int maxGenerations, 
                                            Population population, 
                                            float reproductivePercentage, 
                                            Study study, 
                                            Vector<IOptimizationPlugin> selectedPlugins, 
                                            Vector<Float> originalScores, 
                                            Vector<ParameterSet> parametersList, 
                                            boolean obtainParetoFront, 
                                            Vector<String> paretoOptimalSet, 
                                            ProcessPanel processPanel,
                                            OptimizationRunner.selectionType selecType)
    {
        int generationNumber = 1;
        int convergenceCounter = 0;
        float newScore = 0, bestScoreSoFar = 0;
        float originalScore = 0;
        int populationSize = population.getSize();
        int parentsCounter;

        Individual parent1, parent2, offspring1, offspring2;
        int parentIndex1, parentIndex2;

        for (int i=0; i<selectedPlugins.size(); i++)
            originalScore += originalScores.get(i);
        originalScore /= originalScores.size();

        Vector<String> parents = new Vector<String>(2);
        parents.add(""); parents.add("");
        Vector<String> reproductionResult;

        /* In the main loop, stop only when there is no evolution during 50 generations, or a fraction of maxGenerations and the current generation (see formula). */
        while ((convergenceCounter < maxGenerations*40/generationNumber) && (convergenceCounter<50) && !OptimizationRunner.isStopOptimization())
        {
            parentsCounter = 0;

//            if ((generationNumber % 10) == 0)
//                System.out.println("Gen["+generationNumber+"]:     ConvergenceCounter: " + convergenceCounter + "          TerminationThreshold: " + (maxGenerations*40/generationNumber));

            /* Count number of repeated consecutive best scores to find convergence. */
            if (population.getBestIndividual().getScore() <= bestScoreSoFar)
                convergenceCounter++;
            else
                convergenceCounter = 0;
            
            
            // if NOT_OPTIMIZE_SELECTION (preserve zone) is active save the zone to preserve
            String originalSubSeq = null;
            int startIndex = 0;
            int endIndex = 0;
            if (selecType == OptimizationRunner.selectionType.NOT_OPTIMIZE_SELECTION) {
                startIndex = study.getSelectedStartIndex();
                endIndex = study.getSelectedEndIndex()+1;
                originalSubSeq = study.getOriginalGene().getCodonSubSequence(startIndex, endIndex);
            }

            while (parentsCounter < Math.round(populationSize * (reproductivePercentage))+1)
            {
                parentsCounter += 2;

                /* Select parents. Randomly choose each parent, proportionatly to its score. */
                do {parentIndex1 = rand.nextInt(populationSize);}
                while (rand.nextInt(100) < population.getIndividual(parentIndex1).getScore());

                /* Select the second parent the same way, but avoid choosing the same parent as before. */
                do { do {parentIndex2 = rand.nextInt(populationSize);} while (parentIndex1 == parentIndex2); }
                while (rand.nextInt(100) < population.getIndividual(parentIndex1).getScore());

                parent1 = population.getIndividual(parentIndex1);
                parent2 = population.getIndividual(parentIndex2);

                parents.set(0, parent1.getSequence());
                parents.set(1, parent2.getSequence());

                /* Create offspring */
                reproductionResult = GeneticAlgorithm.makeCrossover(parents,generationNumber, 600);
                
                /* If NOT_OPTIMIZE_SELECTION (preserve zone) is active rollback to original subsuquence */
                if (selecType == OptimizationRunner.selectionType.NOT_OPTIMIZE_SELECTION) {
                    StringBuilder seqBuilder;
                    for (int i = 0; i < reproductionResult.size(); i++) {
                        seqBuilder = new StringBuilder(reproductionResult.get(i));
                        seqBuilder.replace(startIndex * 3, endIndex * 3 + 3, originalSubSeq);
                        reproductionResult.setElementAt(seqBuilder.toString(), i);
                    }
                }

                offspring1 = new Individual(reproductionResult.get(0), getFitnessScore(study, reproductionResult.get(0), selectedPlugins, parametersList, originalScores));
                offspring2 = new Individual(reproductionResult.get(1), getFitnessScore(study, reproductionResult.get(1), selectedPlugins, parametersList, originalScores));

                /* Manage the pareto optimal set. */
                if (obtainParetoFront)
                {
                    addToParetoFront(paretoOptimalSet, offspring1.getSequence(), selectedPlugins, study, parametersList);
                    addToParetoFront(paretoOptimalSet, offspring2.getSequence(), selectedPlugins, study, parametersList);
                }
                
                /* Replace population with offspring, or add new offspring to population. */
                if ((offspring1.getScore() > parent1.getScore()) || (offspring1.getScore() > parent2.getScore()))
                    if (!population.containsIndividual(offspring1))
                    {
                        population.removeIndividual(parent1);
                        population.addIndividual(offspring1);
                    }

                if ((offspring2.getScore() > parent1.getScore()) || (offspring2.getScore() > parent2.getScore()))
                    if (!population.containsIndividual(offspring2))
                    {
                        population.removeIndividual(parent2);
                        population.addIndividual(offspring2);
                    }
            }

            generationNumber++;

            bestScoreSoFar = newScore;
            newScore = population.getBestIndividual().getScore();

            /* New best score? Update progress panel. */
            if (newScore > bestScoreSoFar)
                processPanel.setStatus("Best score is " + new DecimalFormat("##.##").format(newScore+originalScore)+"%");
        
            /* Best possible score achieved. */
            if (newScore >= 99.99)
                break;
        }
    }

    /* Add an individual to the pareto optimal set, if that individual fills the requirements. */
    private static void addToParetoFront(Vector<String> paretoOptimalSet, String newIndividual, Vector<IOptimizationPlugin> selectedPlugins, Study study, Vector<ParameterSet> parametersList)
    {
        if (paretoOptimalSet.isEmpty())
        {
            paretoOptimalSet.add(newIndividual);
            return;
        }

        Vector<Float> newIndividualScores = new Vector<Float>();
         for (int j=0; j<selectedPlugins.size(); j++)
             newIndividualScores.add(selectedPlugins.get(j).getScoreOfSequence(study, newIndividual));

        /* Run through all current pareto optimal set */
        for (int i=0; i<paretoOptimalSet.size(); i++)
            /* Verify if any point in the current pareto optimal set dominates the new individual. */
            if (dominates(paretoOptimalSet.get(i), newIndividual, selectedPlugins, study, parametersList))
                return;

        /* New individual is not dominated by any individual in the pareto optimal set. Add to the set. */
        if (!paretoOptimalSet.contains(newIndividual))
        {
            boolean inserted = false;

            if (paretoOptimalSet.size() < 50)
            {
                paretoOptimalSet.add(newIndividual);
                inserted = true;
            }

            for (int i=0; i<paretoOptimalSet.size(); i++)
                if (dominates(newIndividual, paretoOptimalSet.get(i), selectedPlugins, study, parametersList))
                {
                    if (!inserted)
                    {
                        inserted = true;
                        paretoOptimalSet.add(newIndividual);
                    }
                    paretoOptimalSet.remove(i);
                }
        }
    }

    /* Returns true if sequence in solution1 dominates sequence in solution2. */
    private static boolean dominates(String solution1, String solution2, Vector<IOptimizationPlugin> selectedPlugins, Study study, Vector<ParameterSet> parametersList)
    {
        boolean dominates = false;
        for (int j=0; j<selectedPlugins.size(); j++)
        {
            float solution1Score = selectedPlugins.get(j).getScoreOfSequence(study, solution1);
            float solution2Score = selectedPlugins.get(j).getScoreOfSequence(study, solution2);

            if (solution1Score < solution2Score)
                return false; //it does not dominate solution 2

            if (solution1Score > solution2Score)
                dominates = true;
        }

        return dominates;
    }

    public static class Population
    {
        private Vector<Individual> population;
        private Individual bestIndividual;
        private float bestScore = Float.NEGATIVE_INFINITY;

        //Set syncSet = Collections.synchronizedSet(new HashSet());

        public Population(int initialCapacity)
        {
            assert initialCapacity > 0;

            //population = new Vector<Individual>();
            population = new Vector<Individual>(initialCapacity);
        }

        public void addIndividual(String individualSequence, int score)
        {
            assert individualSequence != null;

            Individual newInd = new Individual(individualSequence, score);
            population.add(newInd);
            if (bestScore < score)
            {
                bestIndividual = newInd;
                bestScore = score;
            }
        }

        public void addIndividual(Individual individual)
        {
            assert individual != null;

            population.add(individual);
            if (bestScore < individual.getScore())
            {
                bestIndividual = individual;
                bestScore = individual.getScore();
            }

            //System.out.println("Added (score: "+individual.getScore()+"): " + individual.getSequence());
        }

        public void removeIndividual(Individual individual)
        {
            assert population != null;

            population.remove(individual);
        }

        public Individual getIndividual(int index)
        {
            assert population != null;

            return population.get(index);
        }

        public Individual getBestIndividual() 
        {
            assert bestIndividual != null;

            return bestIndividual;
        }

        public boolean containsIndividual(Individual ind)
        {
            assert population != null;

            return population.contains(ind);
        }

        public int getSize()
        {
            assert population != null;

            return population.size();
        }
    }


    public static class IndividualComparator implements Comparator<Individual>
    {

        public int compare(Individual o1, Individual o2)
        {
            if (o1.getScore() > o2.getScore())
                return -1;

            if (o1.getScore() < o2.getScore())
                return 1;

            /* Avoid equality just by score. */
            if (o1.equals(o2))
                return 0;
            else
                return 1;
        }

    }

    public static class Individual
    {
        public Individual(String sequence, float score)
        {
            this.sequence = sequence;
            this.score = score;
        }

        @Override
        public boolean equals(Object individual)
        {
            assert individual instanceof Individual;

            return this.sequence.equals(((Individual)individual).getSequence());
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 83 * hash + (this.sequence != null ? this.sequence.hashCode() : 0);
            return hash;
        }

        public float getScore() {
            return score;
        }

        public float setScore(float score) {
            this.score = score;
            return score;
        }

        public String getSequence() {
            return sequence;
        }

        public void setSequence(String sequence) {
            this.sequence = sequence;
        }

        public String sequence;
        public float score;
    }
}
