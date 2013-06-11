
package pt.ua.ieeta.geneoptimizer.FileOpeningParsing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Observable;
import java.util.Vector;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo Gaspar
 */
public abstract class IGenomeFileParser extends Observable
{
    public abstract boolean readGenesFromFile(String filename, GeneticCodeTable geneticCodeTable, Genome destinationGenome, Vector<Gene> destinationGeneVector) throws FileNotFoundException;
    public abstract Gene readGeneFromFile(String fileName, String targetGeneAnnotation, int size, GeneticCodeTable geneticCodeTable, Genome genome) throws FileNotFoundException, IOException;
    public abstract int getNumRejectedGenes();
    public abstract void setLoadingEnded();
}
