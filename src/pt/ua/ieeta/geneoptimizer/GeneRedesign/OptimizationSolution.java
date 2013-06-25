package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Paulo
 */
public class OptimizationSolution
{
    private List<String> redesignNames;
    private List<Float> redesignScores;

    private String sequence;
    private int ID;

    public OptimizationSolution(String sequence)
    {
        assert sequence != null;

        this.sequence = sequence;

        redesignNames = new ArrayList<String>();
        redesignScores = new ArrayList<Float>();
    }

    public void addRedesignScore(String name, float score)
    {
        assert name != null;

        redesignNames.add(name);
        redesignScores.add(score);
    }

    public float getRedesignScore(int redesignMethodIndex)
    {
        assert redesignMethodIndex >= 0;
        assert redesignScores != null;

        return redesignScores.get(redesignMethodIndex);
    }

    public String getRedesignName(int redesignMethodIndex)
    {
        assert redesignMethodIndex >= 0;
        assert getRedesignNames() != null;

        return getRedesignNames().get(redesignMethodIndex);
    }

    /**
     * @return the redesignNames
     */
    public List<String> getRedesignNames() 
    {
        assert redesignScores != null;

        return redesignNames;
    }

    /**
     * @return the sequence
     */
    public String getSequence() {
        return sequence;
    }

    public Object[] getValues()
    {
        assert redesignScores != null;
        assert !redesignScores.isEmpty();

        return redesignScores.toArray();
    }

    public Float getValue(int methodIndex)
    {
        assert redesignScores != null;
        assert !redesignScores.isEmpty();

        return redesignScores.get(methodIndex);
    }

    public float getTotalScore()
    {
        assert redesignScores != null;
        assert !redesignScores.isEmpty();

        float sum = 0;
        for (float f : redesignScores)
            sum += f;

        return sum;
    }


}
