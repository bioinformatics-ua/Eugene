/**
 * OptimizationRunner.java
 */
package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.JOptionPane;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.GUI.ParetoResultsGUI;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;
import pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel.HostSelectionPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.GeneticAlgorithm.Population;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class OptimizationRunner extends Thread
{
    private List<IOptimizationPlugin> selectedPlugins;
    private List<ParameterSet> parametersList;
    private List<Float> originalScores;
    private Study study, originalStudy;
    private boolean obtainParetoFront;
    private boolean useQuickOptimization;
    private selectionType selecType;
    private static boolean interruptFlag;
    private static boolean isRunning;

    public enum selectionType 
    {
        NO_SELECTION,
        OPTIMIZE_SELECTION,
        NOT_OPTIMIZE_SELECTION
    }

    public OptimizationRunner(List<IOptimizationPlugin> selectedPlugins, Study study, boolean useQuickOptimization)
    {
        this.selectedPlugins = selectedPlugins;
        this.originalStudy = study;
        this.study = study;
        this.useQuickOptimization = useQuickOptimization;
        
        this.parametersList = new ArrayList<ParameterSet>(selectedPlugins.size());
        this.originalScores = new ArrayList<Float>(selectedPlugins.size());

        this.obtainParetoFront = (Boolean) ApplicationSettings.getProperty("obtainParetoFront", Boolean.class);
        
        /* Flag to externally order the interruption of the optimization. */
        this.interruptFlag = false;
        this.isRunning = false;
    }

    @Override
    public void run()
    {
        isRunning = true;
        
        /* Verify that a host species was selected. */
        if (getSelectedHost() == null) 
        {
            isRunning = false;
            OptimizationModel.getInstance().optimizationEnded(this);
            return;
        } 
        //TODO: precisa de uma warning message        
                
        System.out.println("New optimization started...");
        
        /* verify if has selection zone */
        if (originalStudy.hasCodonSelection())
        {
            Object[] options = { "Optimize Selection", "Optimize everything else", "Cancel" };
            int n = JOptionPane.showOptionDialog(
                        MainWindow.getInstance(),
                        "Would you like to optimize only the selection, or everything except the selection ?",
                        "Selection",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[2]
                    );
            
            /* Optimize selected zone */
            if (n == 0) 
            {
                selecType = selectionType.OPTIMIZE_SELECTION;
                
                Gene newGene = new Gene("SubGene", originalStudy.getOriginalGene().getGenome());
                String subGeneSequence = originalStudy.getOriginalGene().getCodonSubSequence(study.getSelectedStartIndex(), study.getSelectedEndIndex()+1);
                newGene.createStructure(subGeneSequence, BioStructure.Type.mRNAPrimaryStructure);
                study = new Study(newGene, newGene, "temp study");
            }
            else
            /* No optimize selected zone */
            if (n == 1) {
                selecType = selectionType.NOT_OPTIMIZE_SELECTION;
                
            }
            else
            /* Cancel optimization */
            if ((n == 2) || (n == JOptionPane.CLOSED_OPTION)) 
            {
                isRunning = false;
                OptimizationModel.getInstance().optimizationEnded(this);
                return;
            }
        }
        else
        {   /* No zone selected */
            selecType = selectionType.NO_SELECTION;
        }
        
        
        /* Initialize process panel */
        ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Redesigning");
        processPanel.setIndeterminated();

//        /* Obtain all parameters from all plugins. */
//        processPanel.setStatus("Getting parameters...");
//        for (IOptimizationPlugin plugin : selectedPlugins)            
//            parametersList.add(plugin.getParameters());

        /* Make the plugins calculate the best score they can achieve in a single optimization. */                
        processPanel.setStatus("Calculating best scores...");
        for (int i=0; i<selectedPlugins.size(); i++)
            this.selectedPlugins.get(i).calculateBestScore(study);        
  
        /* Calculate the fitness score of the original sequence for each plugin. */
        processPanel.setStatus("Getting original score...");
        for (int i=0; i<selectedPlugins.size(); i++)
            this.originalScores.add(selectedPlugins.get(i).getScoreOfSequence(study, study.getResultingGene().getCodonSequence()));        
        
        /* ------------------------------------------------------------------------ */
        /* Avoid using genetic algorithms when dealing with single algorithm cases. */
        if ((selectedPlugins.size() == 1)
                && (!selectedPlugins.get(0).needsGeneticAlgorithm())
                && (selecType != selectionType.NOT_OPTIMIZE_SELECTION))
        {
            System.out.println(selectedPlugins.get(0).getPluginName() + " plugin started.");
            processPanel.setIndeterminated();
            processPanel.setStatus("Running optimization...");

            /* Make optimization. */
            Study newStudy = selectedPlugins.get(0).makeSingleOptimization(study);
            
            /* Create new gene in case this an optimization of a slice. 
             * No need to verify that the selection is of type OPTIMIZE_SEPLECTION 
             * because this verification is done previously. */
            if (originalStudy.hasCodonSelection())
            {
                 /* Create new gene. */
                Gene gene = new Gene(originalStudy.getResultingGene().getName(), getSelectedHost());

                /* Build its sequence with original parts and redesigned parts. */
                StringBuilder newGeneSequence = new StringBuilder();
                newGeneSequence.append(originalStudy.getResultingGene().getCodonSubSequence(0, originalStudy.getSelectedStartIndex()));
                newGeneSequence.append(newStudy.getResultingGene().getCodonSequence());
                newGeneSequence.append(originalStudy.getResultingGene().getCodonSubSequence(originalStudy.getSelectedEndIndex()+1, originalStudy.getResultingGene().getSequenceLength()));

                /* Create remaining structures. */
                gene.createStructure(newGeneSequence.toString(), BioStructure.Type.mRNAPrimaryStructure);
                gene.calculateAllStructures();
                
                /* Create study. */
                newStudy = new Study(originalStudy.getResultingGene(), gene, "");
            }
            
            
            newStudy.setName("[" + getSelectedHost().getName()+"]   " + originalStudy.getResultingGene().getName() + " (Redesigned by: " + selectedPlugins.get(0).getPluginName() + ")");
            
            processPanel.setStatus("Done.");
            processPanel.setComplete();

            /* Add to project. */
            addNewStudyToProject(newStudy);

            isRunning = false;
            OptimizationModel.getInstance().optimizationEnded(this);
            
            return;
        }
        /* ------------------------------------------------------------------------ */
       
        
        /* JUST FOR MULTIPLE OPTIMIZATION OR NOT_OPTIMIZE_SELECTION OPTION */
        System.out.println("Optimization started for " + selectedPlugins.size() + " plugins."
                + "Selection mode is: " + selecType.toString());
        
        /* Create empty set to take the pareto optimal set. */
        List<String> paretoOptimalSet = new ArrayList<String>(10);
        
        String finalSolutionSequence = null;
        float finalSolutionScore = 0;
        
        /* Main life cicle. */
        if (useQuickOptimization)
        {
            int k = 51200;
            long start;            
            do{
                int aux = 0;
                float score = 0;
                start  = System.currentTimeMillis();                
                do{                    
                finalSolutionSequence = SimulatedAnnealing.runSimulatedAnnealing(   study, 
                                                                                study.getResultingGene().getCodonSequence(), 
                                                                                k, 
                                                                                selectedPlugins,                                                                                 
                                                                                selecType, 
                                                                                processPanel);
                finalSolutionScore = SimulatedAnnealing.getScore();
                score += finalSolutionScore;
                aux++;
                }while (aux < 5);
                
                score = score / aux;
                
                System.out.println(k + "\t" + String.valueOf(score).replaceAll("\\.", ",") + "\t" + (System.currentTimeMillis() - start) / aux);
                
                k = k * 2;
            }while(k <= 102400);
        }
        else
        {   
            /* Some needed parameters. */
            final int populationSize = 60;
            final int maxGenerations = 1000;
            final float reproductivePercentage = 0.40f;
    
//            /* Calculate original score. */
//            float sum = 0;
//            for (float f : originalScores)
//                sum += f;
//            
//            /* Use simulated annealing to create a seed for the genetic algorithm. */
//            String seed = SimulatedAnnealing.runSimulatedAnnealing(study, study.getResultingGene().getCodonSequence(), 1000, selectedPlugins, parametersList, processPanel);
//            System.out.println("OriginalScore: " + (sum/4.0) + "    Seed Score: " + SimulatedAnnealing.getScore());
 
            /* Population pointer, for genetic algorithm. */
            Population population;

            /* Generate an initial population. */
            processPanel.setStatus("Generating population...");
            population = GeneticAlgorithm.generatePopulationFromSequence(   study, 
                                                                            populationSize, 
                                                                            selectedPlugins, 
                                                                            parametersList, 
                                                                            originalScores,
                                                                            selecType);

            GeneticAlgorithm.runGeneticAlgorithm(   maxGenerations, 
                                                    population, 
                                                    reproductivePercentage, 
                                                    study, 
                                                    selectedPlugins, 
                                                    originalScores, 
                                                    parametersList,  
                                                    obtainParetoFront, 
                                                    paretoOptimalSet, 
                                                    processPanel,
                                                    selecType);

            finalSolutionSequence = population.getBestIndividual().getSequence();
            finalSolutionScore = population.getBestIndividual().getScore();
        }
        
        /************ TEST ************/
//        Individual ind = (Individual) population.getBestIndividual(); // best individual in population
//        float tempScore = 0;
//        for (int k=0; k<selectedPlugins.size(); k++)
//           tempScore +=  selectedPlugins.get(k).getScoreOfSequence(study, ind.getSequence(), parametersList.get(k));
//        tempScore /= selectedPlugins.size();
//        System.out.println(new DecimalFormat("##.##").format(tempScore));
//        System.out.println("ACABOU O GA! \n");
        /************ END OF TEST ************/
        
        /* Transform Pareto front into a solution set. */
        if (obtainParetoFront && (selectedPlugins.size()>1) && !useQuickOptimization)
        {
            /* TO avoid repeated scores in the final table, use this HashSet to verify which scores were already inserted. */
            HashSet<Float> scoreRecorded = new HashSet<Float>();

            OptimizationSolutionSet solutionSet = new OptimizationSolutionSet();
            ResultKeeper result = new ResultKeeper();
            int j = 0;
            for (String paretoPoint : paretoOptimalSet)
            {
                OptimizationSolution solution = new OptimizationSolution(paretoPoint);

                /* Get score for each redesign method that was used. */
                for (int i=0; i<selectedPlugins.size(); i++)
                {
                    float tmpScore = selectedPlugins.get(i).getScoreOfSequence(study, paretoPoint);
                    solution.addRedesignScore(selectedPlugins.get(i).getPluginName(), tmpScore);
                }

                /* If this is a new solution, keep it. */
                if (!scoreRecorded.contains(solution.getTotalScore()))
                {
                    solutionSet.addSolution(solution);
                    scoreRecorded.add(solution.getTotalScore());

                    /* Keep record of the best solution. */
                    if (solution.getTotalScore() >= finalSolutionScore)
                        solutionSet.setBestSolution(j);
                }

                j++;
            }
            ParetoResultsGUI plotter = new ParetoResultsGUI(solutionSet, result);
            new Thread(plotter).start();

            /* Wait for the result: the user has to select a sequence from the plot. */
            finalSolutionSequence = (String) result.getResult();
        }

        /* Create new gene. This is the gene resulting from the optimization. */
        Gene gene = new Gene(originalStudy.getResultingGene().getName(), getSelectedHost());
        StringBuilder newGene = new StringBuilder();
        newGene.append(finalSolutionSequence);
        if (selecType == selectionType.OPTIMIZE_SELECTION)
        {
            String before = originalStudy.getResultingGene().getCodonSubSequence(0, originalStudy.getSelectedStartIndex());
            String after = originalStudy.getResultingGene().getCodonSubSequence(originalStudy.getSelectedEndIndex()+1, originalStudy.getResultingGene().getSequenceLength());
            newGene.insert(0, before);
            newGene.append(after);
        }
        gene.createStructure(newGene.toString(), BioStructure.Type.mRNAPrimaryStructure);
        gene.calculateAllStructures();

        /* define study name */
        String studyName;
        switch (selecType) {
            case NO_SELECTION:
                studyName = "[" + getSelectedHost().getName() + "]   " + gene.getName() + " (Optimized by: Multiple Redesigns)";
                break;
            case OPTIMIZE_SELECTION:
                studyName = "[" + getSelectedHost().getName() + "]   " + gene.getName() + " (Optimized by: Multiple Redesigns "
                        + "optimize zone:[" + (originalStudy.getSelectedStartIndex()+1) + ";" + (originalStudy.getSelectedEndIndex()+1) + "] )";
                break;
            case NOT_OPTIMIZE_SELECTION:
                studyName = "[" + getSelectedHost().getName() + "]   " + gene.getName() + " (Optimized by: Multiple Redesigns "
                        + "Preserve zone:[" + (originalStudy.getSelectedStartIndex()+1) + ";" + (originalStudy.getSelectedEndIndex()+1) + "] )";
                break;
            default:
                studyName = "";
        }
        
        /* Create new study. */
        Study newStudy = new Study(study.getResultingGene(), gene, studyName);
        addNewStudyToProject(newStudy);

        processPanel.setStatus("Done.");
        processPanel.setComplete();
        
        isRunning = false;
        OptimizationModel.getInstance().optimizationEnded(this);

        System.out.println("Optimization ended.");
    }
    
    /* Estimate the k max (simulated annealing max iterations) based on codons length */
    private int calculateKMaxValue(){
        //y = 14,779x + 1606,8;
        int nCodons = study.getResultingGene().getCodonSequence().length();
        
        return 100000;
    }
    
    public static boolean isRunning()
    {
        return isRunning;
    }

    public static boolean isStopOptimization()
    {
        return interruptFlag;
    }
    
    public static void stopOptimization()
    {
        interruptFlag = true;
    }
    

    /* Adds a given study to the project. */
    private void addNewStudyToProject(Study newStudy)
    {
        /* Make report of the optimization process. */
        OptimizationReport report = new OptimizationReport();

        /* Tweek to avoid wrong score when optimizing partial sequences. */
        String sequence;
        if (selecType!=selectionType.OPTIMIZE_SELECTION)
            sequence = newStudy.getResultingGene().getCodonSequence();
        else
            sequence = newStudy.getResultingGene().getCodonSubSequence(originalStudy.getSelectedStartIndex(), originalStudy.getSelectedEndIndex()+1);

        for (int i=0; i<selectedPlugins.size(); i++)
        {
            Float score = selectedPlugins.get(i).getScoreOfSequence(study, sequence);
            String score_string = Float.toString(score);
            String improvement = Float.toString(selectedPlugins.get(i).getScoreOfSequence(study, sequence) - originalScores.get(i));
            report.addOptimization(selectedPlugins.get(i).getPluginName(), selectedPlugins.get(i).getParameters(), score_string, improvement, selectedPlugins.get(i));
        }

        /* Save report in new study. */
        newStudy.addOptimizationReport(report, study);

        /* Set new gene aligned structures if they were available in the original gene. */
        if(study.getResultingGene().hasAlignedStructure(BioStructure.Type.mRNAPrimaryStructure))
        {
            int score = study.getResultingGene().getScore();
            double identity =  study.getResultingGene().getIdentity();
            String ID = study.getResultingGene().getOrthologId();
            String genomeName = study.getResultingGene().getGenomeName();
            newStudy.getResultingGene().setOrthologInfo(score, identity, ID, genomeName);
            newStudy.getResultingGene().setAlignedStructure(study.getResultingGene().getAlignedStructure(BioStructure.Type.proteinPrimaryStructure).getSequence(), BioStructure.Type.proteinPrimaryStructure);
            newStudy.getResultingGene().setAlignedStructure(study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence(), BioStructure.Type.mRNAPrimaryStructure);
        }

        /* Add study to project. */
        originalStudy.getProject().addNewStudy(newStudy);
        //ProjectManager.getInstance().getSelectedProject().addNewStudy(newStudy);
    }

    public static Genome getSelectedHost()
    {
        return HostSelectionPanel.getSelectedGenome();
    }
}
