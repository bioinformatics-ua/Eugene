package pt.ua.ieeta.geneoptimizer.FileHandling;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo Gaspar
 */
public abstract class IGenomeFileParser extends Observable
{
    public abstract boolean readGenesFromFile(String filename, GeneticCodeTable geneticCodeTable, Genome destinationGenome, List<Gene> destinationGeneVector) throws FileNotFoundException;
    public abstract Gene readGeneFromFile(String fileName, String targetGeneAnnotation, int size, GeneticCodeTable geneticCodeTable, Genome genome) throws FileNotFoundException, IOException;
    public abstract int getNumRejectedGenes();
    public abstract void setLoadingEnded();
}
