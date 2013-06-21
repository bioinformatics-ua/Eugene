package pt.ua.ieeta.geneoptimizer.FileOpeningParsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojavax.Note;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.bio.seq.RichSequenceIterator;
import pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI.GenePoolGUI;
import pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI.GenePoolObserverMessage;
import pt.ua.ieeta.geneoptimizer.geneDB.*;

/**
 *
 * @author Paulo Gaspar
 * 
 * This class allows opening a DNA file without knowing its format.
 * Allowed formats are loaded a priori.
 * 
 */
public class DefaultFileParser extends IGenomeFileParser
{
    /* The file to open and parse. */
    private File sourceFile;
    
    /* Destiny genome and gene vector. Genetic code table to use. */
    private GeneticCodeTable geneticCodeTable;
    private Genome genome;
    private Vector<Gene> genes;
    
    /* Total number of genes that was rejected after parsing and validating. */
    private int numRejectedGenes, numAcceptedGenes;
    
    private GenomeFilters genomeFilters;
    
    public DefaultFileParser(GenomeFilters genomeFilters) {
        assert genomeFilters != null;
        this.genomeFilters = genomeFilters;
    }
    
    @Override
    public boolean readGenesFromFile(String filename, GeneticCodeTable geneticCodeTable, Genome genome, Vector<Gene> genes)
    {
        assert filename != null;
        assert !filename.isEmpty();
        assert geneticCodeTable != null;
        assert genome != null;
        assert genes != null;
        
        sourceFile = new File(filename);
        
        /* File must exist. */
        assert sourceFile.exists();
        
        this.geneticCodeTable = geneticCodeTable;
        this.genome = genome;
        this.genes = genes;
        
        this.numRejectedGenes = 0;
        this.numAcceptedGenes = 0;

        /* Add the GUI as observer of this class. */
        addObserver(GenePoolGUI.getInstance());
        
        try
        {
            boolean isOK = loadFile(true, null);
            if (!isOK)
            {
                System.err.println("Unknown file type.");
                return false;
            }
        } 
        catch (Exception ex)
        {
            System.err.println("An exception occurred while trying to read the genome file: " + ex.getMessage());
            return false;
        }
        
        return true;
    }
    
    /** Load a genome file, and create genes from it.
     * @param getGenomeName If there's a genome name, change the input genome name to it.
     * @param geneToFind Annotation of a single gene to find. If is null, get all genes. */
    private boolean loadFile(boolean getGenomeName, Vector<String> genesToFind) throws ClassNotFoundException, IOException, NoSuchElementException, BioException
    {        
        try
        {
            /* Load all known formats. */
            Class.forName("org.biojavax.bio.seq.io.EMBLFormat"); //EMBL
            Class.forName("org.biojavax.bio.seq.io.GenbankFormat"); //GenBank
            Class.forName("org.biojavax.bio.seq.io.EMBLxmlFormat"); //EMBLxml
    //        Class.forName("org.biojavax.bio.seq.io.FastaFormat"); //FASTA
            Class.forName("org.biojavax.bio.seq.io.INSDseqFormat"); //INSD

            /* Notify GUI of new genome beeing read. */
            setChanged();
            notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.LOADING_GENOME, null));

            /* Read any filetype. */
            RichSequenceIterator seqs = RichSequence.IOTools.readFile(sourceFile, null);

            while(seqs.hasNext())
            {
                RichSequence seq = seqs.nextRichSequence();

                int numBasePairs = seq.length();
                System.out.println("Reading " + numBasePairs + " base pairs...");
                if (numBasePairs <= 0)
                    numBasePairs = 0;

                /* Notify GUI of new genome beeing read. */
                setChanged();
                Integer fileSize = (int) (numBasePairs / 1000.0f);
    //            notifyObservers(fileSize);
                notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.PARSING_GENOME, fileSize));

    //            System.out.println("Locus: " + seq.getName());
    //            System.out.println("Definition: " + seq.getDescription());
    //            System.out.println("Accession: " + seq.getAccession());

                if ((seq.getTaxon() != null) && (getGenomeName))
                {
                    genome.setName(seq.getTaxon().getDisplayName());
                    genome.setHasNameConfirmation(true);
                }

                for (Iterator<Feature> it = seq.getFeatureSet().iterator(); it.hasNext();)
                {
                    RichFeature f = (RichFeature) it.next();
                    if (f.getType().equals("CDS"))
                    {
                        String sequence = f.getSymbols().seqString();

                        /* Ignore invalid sequences. */
                        int rejectCode = SequenceValidator.isValidCodonSequence(sequence, geneticCodeTable, genomeFilters);
                        if (rejectCode != 0) { numRejectedGenes++; continue; }

                        String geneName = null, productName = null, locus_tag = null;

                        /* Find gene and product names. */
                        for (Object o : f.getNoteSet())
                            if (((Note) o).getTerm().getName().equals("gene"))
                                geneName = ((Note) o).getValue();
                            else
                                if (((Note) o).getTerm().getName().equals("product"))
                                    productName = ((Note) o).getValue();
                                else
                                    if (((Note) o).getTerm().getName().equals("locus_tag"))
                                        locus_tag = ((Note) o).getValue();

                        if (genesToFind != null)
                            if (!genesToFind.contains(geneName) && !genesToFind.contains(productName) && !genesToFind.contains(locus_tag)) continue;

                        /* Create gene. */
                        String nameToUse = geneName != null ? geneName : (productName != null ? productName : (locus_tag != null ? locus_tag : "gene"));
                        Gene newGene = new Gene(nameToUse, genome);

                        /* We assume that structured files such as GenBank files are always correct in terms of naming. */
                        if (geneName != null)
                            newGene.setHasNameConfirmation(true);
                        if (productName != null) 
                        {
                            newGene.setProductName(productName);
                            newGene.setHasProductNameConfirmation(true);
                        }
                        genes.add(newGene);

                        /* Make sequence uppercase, replace Tinamin by Uracil, and set new gene as this sequence. */
                        newGene.createStructure(SequenceValidator.makeCorrectionsToGene(sequence), BioStructure.Type.mRNAPrimaryStructure);

                        /* Warn GUI about an update in the reading process. */
                        setChanged();
                        Float changeDifference = new Float(sequence.length() / 1000.0f); 
                        notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.UPDATE_PROGRESS, changeDifference));

                        numAcceptedGenes++;

                        /* If looking for specific genes, check the termination creterion. */
                        if (genesToFind != null)
                        {
                            genesToFind.remove(nameToUse);
                            if (genesToFind.isEmpty()) return true;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println("Exception while loading file: " + e.getLocalizedMessage());
            return false;
        }
        
        return true;
    }
    
    @Override
    public Gene readGeneFromFile(String fileName, String targetGeneAnnotation, int size, GeneticCodeTable geneticCodeTable, Genome genome) throws FileNotFoundException, IOException
    {
        assert fileName != null;
        assert !fileName.isEmpty();
        assert geneticCodeTable != null;
        assert genome != null;
        
        sourceFile = new File(fileName);
        
        /* File must exist. */
        assert sourceFile.exists();
        
        this.geneticCodeTable = geneticCodeTable;
        this.genome = genome;
        this.genes = new Vector<Gene>();
        
        Vector<String> genesToFind = new Vector<String>();
        genesToFind.add(targetGeneAnnotation);
        try
        {
            
            boolean isOK = loadFile(false, genesToFind);
            if (!isOK)
            {
                System.err.println("Unknown file type.");
                return null;
            }
        } catch (Exception ex)
        {
            System.err.println("An exception occurred while trying to read the genome file: " + ex.getMessage());
            return null;
        }
        
        return genes.firstElement();
    }
    
    @Override
    public int getNumRejectedGenes()
    {
        return numRejectedGenes;
    }

    public int getNumAcceptedGenes()
    {
        return numAcceptedGenes;
    }
    
    
    
    public static void main(String [] args)
    {
        DefaultFileParser loader = new DefaultFileParser(null);
               
//        loader.sourceFile = new File("../Genomes/Escherichia coli.gbk");
//        loader.sourceFile = new File("../Genomes/Escherichia coli.fa");
//        loader.sourceFile = new File("../Genomes/emblGeneExample2.embl");
        
        try
        {
//            loader.readGenesFromFile("../Genomes/cor.bmp", GeneticCodeTableParser.getInstance().getCodeTableByID(1), new Genome(), new Vector<Gene>(50, 50));
//            loader.readGenesFromFile("../Genomes/Escherichia coli.gbk", GeneticCodeTableParser.getInstance().getCodeTableByID(1), new Genome(), new Vector<Gene>(50, 50));
            loader.readGenesFromFile("../Genomes/Escherichia coli.fa", GeneticCodeTableParser.getInstance().getCodeTableByID(1), new Genome(), new Vector<Gene>(50, 50));
            System.out.println("# Rejected genes: " + loader.getNumRejectedGenes());
            System.out.println("# Accepted genes: " + loader.getNumAcceptedGenes());
        } 
        catch (Exception ex)
        {
            System.out.println("Exception: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }

    @Override
    public void setLoadingEnded()
    {
        /* Warn GUI about end of reading. */
        setChanged();
        notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.LOAD_COMPLETE, null));
    }
}
