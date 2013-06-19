
package pt.ua.ieeta.geneoptimizer.PluginSystem;

import java.awt.Color;
import java.util.List;
import java.util.Vector;
import javax.swing.JPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo Gaspar
 */
public interface IOptimizationPlugin extends IPlugin
{
    /* Returns the necessary parameters to run the optimization. */
    public JPanel getParametersPanel();

    /* Makes an optimization to the given study, alone (without other parallel optimizations). with the given parameters */
    public Study makeSingleOptimization(Study study);

    /* Tells wether this optimization plugin was chosen by the user to be used. */
    public boolean isSelected();

    /* Set the optimization plugin as selected to be used or not by the user. */
    public void setSelected(boolean selected);

    /* Returns the user chosen parameters for the plugin in an object array fashion. */
    public ParameterSet getParameters();

    /* Sets the parameters in the plugin with the given vector.
     The order of the parameters is assumed to be the same as given by getParameters. */
    public void setParameters(ParameterSet parameters);

    /* Make the plugin calculate the best score it can achieve in a single optimization. */
    public void calculateBestScore(Study study);

    /* For a given codon sequence, return its score from 0 to 100 (where 100 is optimum). */
    public float getScoreOfSequence(Study study, String sequence);

    /* Returns wether or not the optimization should be run with a genetic algorithm when called for a single optimization. */
    public boolean needsGeneticAlgorithm();
    
    
    /******************************************************************/
    /* Color and analysis contract  (soon to be in another interface) */
    /******************************************************************/
    
    
    /* Make an analysis to the sequence and return a vector of colors representing the result. */
    public Vector<Color> makeAnalysis(Study study, boolean useAlignedGene);

    /* Returns the available colors*/
    public List<Color> colorScale();
    
    /* Returns a brief description of the color scale. */
    public String getScaleDescription();
    
    /* Returns a brief description of the minimum value of the color scale. */
    public String getScaleMinDescription();
    
    /* Returns a brief description of the maximum value of the color scale. */
    public String getScaleMaxDescription();
    
    /**
     * Returns an enum array with all the possible parameters used by the plugin
     * Parameters regarding score should be discarded here
     * @return Enum[] with all enum values
     */
    public Enum[] getAvailableParameters();
    
    /* Sets the genome host used, when needed */
    public void setHost(Genome genome);    
}
