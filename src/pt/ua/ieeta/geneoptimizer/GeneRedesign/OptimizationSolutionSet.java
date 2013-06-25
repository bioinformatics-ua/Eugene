package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Paulo
 */
public class OptimizationSolutionSet
{
    private List<OptimizationSolution> solutionSet;
    private int bestSolution;

    public OptimizationSolutionSet()
    {
        solutionSet = new ArrayList<OptimizationSolution>();
    }
    
    public void addSolution(OptimizationSolution newSolution)
    {
        assert newSolution != null;
        assert solutionSet != null;

        solutionSet.add(newSolution);
    }

    /* Create a XY plot data set with point from the selected redesign methods. */
    public PlotDataSet getPlotDataSet(int redesignIndex1, int redesignIndex2)
    {
        assert redesignIndex1 >= 0;
        assert redesignIndex2 >= 0;
        assert solutionSet != null;
        assert solutionSet.size() > 0;

        PlotDataSet plotDataSet = new PlotDataSet(solutionSet.size());

        /* For each solution in the solution set,  get the values correspondent to each selected redesign. */
        for (OptimizationSolution solution : solutionSet)
            plotDataSet.addNewPoint(solution.getRedesignScore(redesignIndex1), solution.getRedesignScore(redesignIndex2));
        
        return plotDataSet;
    }

    public String getNameOfRedesign(int redesignIndex)
    {
        assert redesignIndex >= 0;
        assert solutionSet != null;
        assert !solutionSet.isEmpty();

        /* All solutions should have the same score names. Therefore, the first solution can be picked to get the name of a redesign. */
        return solutionSet.get(0).getRedesignName(redesignIndex);
    }

    public List<String> getRedesignNames()
    {
        assert solutionSet != null;
        assert !solutionSet.isEmpty();

        return solutionSet.get(0).getRedesignNames();
    }

    public String getSolution(int index)
    {
        assert solutionSet != null;
        assert !solutionSet.isEmpty();
        assert index >= 0;
        assert index < solutionSet.size();

        return solutionSet.get(index).getSequence();
    }
    
    public float getSolutionFinalScore(int index)
    {
        assert solutionSet != null;
        assert !solutionSet.isEmpty();
        assert index >= 0;
        assert index < solutionSet.size();

        int numScores = solutionSet.get(index).getRedesignNames().size();
        
        return solutionSet.get(index).getTotalScore() / numScores;
    }

    public Object[] getSolutionValues(int index)
    {
        assert solutionSet != null;
        assert !solutionSet.isEmpty();

        return solutionSet.get(index).getValues();
    }

    void setBestSolution(int i)
    {
        this.bestSolution = i;
    }

    /**
     * @return the bestSolution
     */
    public int getBestSolutionIndex() {
        return bestSolution;
    }

    public int getSize()
    {
        return solutionSet.size();
    }

    public int getNumValuesPerSolution()
    {
        assert solutionSet != null;
        assert !solutionSet.isEmpty();

        return solutionSet.get(0).getValues().length;
    }

    public Float getSolutionValue(int solutionIndex, int methodIndex)
    {
        assert solutionSet != null;
        assert !solutionSet.isEmpty();

        return solutionSet.get(solutionIndex).getValue(methodIndex);
    }
}
