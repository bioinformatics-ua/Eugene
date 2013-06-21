package pt.ua.ieeta.geneoptimizer.FileOpeningParsing;

import java.io.*;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI.GenePoolGUI;
import pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI.GenePoolObserverMessage;
import pt.ua.ieeta.geneoptimizer.geneDB.*;

/**
 *
 * @author Paulo Gaspar
 */
public class FastaParser extends IGenomeFileParser {
    /* Vector to keep record of parsed genes. */

    private static Vector<Gene> genes;
//    /* Singleton instance of FastaParser. */
//    private static FastaParser instance = null;

    /* Num rejected genes while reading the file. */
    private int numRejectedGenes = 0;
    private int rejectedSize = 0; //special case of a single rejected gene. Just to see if the gene was really big
    private GenomeFilters filters;

//    public synchronized static FastaParser getInstance()
//    {
//        if (instance == null)
//            instance = new FastaParser();
//
//        return instance;
//    }
    public FastaParser(GenomeFilters filters) {
        this.filters = filters;

        /* Add the GUI as observer of this class. */
        addObserver(GenePoolGUI.getInstance());
    }

    @Override
    public synchronized boolean readGenesFromFile(String filename, GeneticCodeTable geneticCodeTable, Genome genome, Vector<Gene> genes) throws FileNotFoundException {
        assert filename != null;
        assert geneticCodeTable != null;
        assert genome != null;

        /* Create file readers. */
        File genomeFile = new File(filename);
        //BufferedInputStream fis = new BufferedInputStream (new FileInputStream(genomeFile));

        BufferedReader in = new BufferedReader(new FileReader(genomeFile));

        /* If file is too small, ignore it. */
        if (genomeFile.length() < 5) //11, because the minimum string would be <\nATGcodonTAG which is 11 length
        {
            return false;
        }

        /* Warn GUI about beeing about to load file into memory. */
        setChanged();
//        notifyObservers("");
        notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.LOADING_GENOME, null));

        /* Notify GUI of new genome beeing read. */
        setChanged();
        Integer fileSize = (int) (genomeFile.length() / 1000);
//        notifyObservers(fileSize);
        notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.PARSING_GENOME, fileSize));

        /* Create new gene list to save read genes. */
        this.genes = genes; //new Vector<Gene>();

        /* Read and parse genes from file. */
        readGenesFromString(in, genomeFile.length(), geneticCodeTable, genome);
        System.gc();

        /* Check for the problem of reading a whole genome as a single fasta entry. */
        if ((genes.isEmpty()) && (numRejectedGenes == 1) && (rejectedSize > 10000)) {
            String msg = "<html>The genome file you selected has only one single VERY LARGE entry.<br/>It is likely that you are using the wrong fasta format and your genome is not organized by genes.<br/>If that is the case, you should use a correct format.</html>";
//            Thread message = new Thread( new MessageWindow(GenePoolGUI.getInstance(), false, true, msg) );
//            message.start();
            JOptionPane.showMessageDialog(GenePoolGUI.getInstance(), msg);
        }

        /* Return positive. */
        return true;
    }

    //TODO need some refactor
    @Override
    public Gene readGeneFromFile(String filename,
            String geneHeader,
            int size,
            GeneticCodeTable geneticCodeTable,
            Genome genome)
            throws FileNotFoundException, IOException {
        assert filename != null;
        assert geneticCodeTable != null;
        assert genome != null;

        /* Create file readers. */
        File genomeFile = new File(filename);
        BufferedReader in = new BufferedReader(new FileReader(genomeFile));

        /* If file is too small, ignore it. */
        if (genomeFile.length() < 5) //11, because the minimum string would be <\nATGcodonTAG which is 11 length
        {
            return null;
        }

        /* Create new gene to save read gene. */
        Gene resultGene = null;

        /* Read and parse genes from file. */
        resultGene = readGeneFromString(in, geneticCodeTable, genome, geneHeader, size);
        System.gc();

        /* Return reference to list of read genes. */
        return resultGene;
    }

    /* Given a String object with a fasta sequence, read the genes in it and put them into the vector. */
    public synchronized void readGenesFromString(BufferedReader genesStream, long size, GeneticCodeTable geneticCodeTable, Genome genome) {
        /* Pre-conditions */
        assert genesStream != null;
        assert geneticCodeTable != null;
        assert size > 0;
        assert genome != null;

        /* Reset number of rejected genes. */
        numRejectedGenes = 0;

        String geneSequence, readString;
        StringBuilder sequenceBuilder = null;

        String currentName = null, nextName = "";
        try {
            while (((readString = genesStream.readLine()) != null) || (sequenceBuilder != null)) {
                /* When readString is null and it enters the loop, that means the end of the file was reached. */
                /* Therefore, the last gene must be considered and recorded. */
                if (readString != null) {
                    if (readString.isEmpty()) {
                        continue;
                    }

                    /* If is still the same gene, keep building sequence. */
                    if (readString.charAt(0) != '>') {
                        if (sequenceBuilder == null) {
                            sequenceBuilder = new StringBuilder();
                        }

                        sequenceBuilder.append(readString);

                        currentName = nextName;

                        continue;
                    }

                    nextName = readString;

                    if (sequenceBuilder == null) {
                        continue;
                    }
                }

                /* Get gene sequence from bytes */
                geneSequence = sequenceBuilder.toString(); //new String(genesStream.getDataReference(), lastIndex, currentIndex-lastIndex);
                sequenceBuilder = null;

                /* Parse gene sequence. */
                geneSequence = geneSequence.replaceAll("\\s", "");

                /* Avoid empty gene streams */
                if (geneSequence.length() < 1) {
                    continue;
                }

                /* Warn GUI about an update in the reading process. */
                setChanged();
                Float changeDifference = new Float(geneSequence.length() / 1000.0f);
//                notifyObservers(changeDifference);
                notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.UPDATE_PROGRESS, changeDifference));

                /* Check if gene is correct. */
                if (SequenceValidator.isValidCodonSequence(geneSequence, geneticCodeTable, filters) == 0) {
                    /* Create new gene. New gene name is the first line of the gene in the stream after the ">". */
                    //String geneHeader = currentName.replaceAll(">", "").trim();
                    Gene newGene = new Gene(currentName.replaceAll(">", "").trim(), genome);

                    /* Make sequence uppercase, replace Tinamin by Uracil, and set new gene as this sequence. */
                    newGene.createStructure(SequenceValidator.makeCorrectionsToGene(geneSequence), BioStructure.Type.mRNAPrimaryStructure);

                    /**
                     * ************** TESTE *****************
                     */
//                    StringBuilder printSequence = new StringBuilder(SequenceValidator.makeCorrectionsToGene(geneSequence));
//                    for (int i = geneSequence.length()-3; i > 0; i-=3)
//                        printSequence.insert(i, ' ');
//
//                    System.out.println(printSequence);
                    /**
                     * **************************************
                     */

                    /* Add parsed gene to parsed genes list */
                    genes.add(newGene);
                } else {
                    numRejectedGenes++;
                    rejectedSize = geneSequence.length();
                }

                //lastIndex = currentIndex+1;
            }
        } catch (IOException ex) { //TODO: exceptions
            Logger.getLogger(FastaParser.class.getName()).log(Level.SEVERE, null, ex);
        }

//        System.out.println("  w/o stop codon: " +a+"     w/o start codon: " + b + "    UnknownLetters: " + c + "    Total rejected: " + getNumRejectedGenes() + "     GenesSize: " + genes.size());
    }

    // TODO refactoring this method
    public synchronized Gene readGeneFromString(BufferedReader genesStream,
            GeneticCodeTable geneticCodeTable,
            Genome genome,
            String geneHeader,
            int size) {
        /* Pre-conditions */
        assert genesStream != null;
        assert geneticCodeTable != null;
        assert genome != null;

        Gene newGene = null;

        String geneSequence, readString;
        StringBuilder sequenceBuilder = null;

        String currentName = null, nextName = "";
        try {
            while (((readString = genesStream.readLine()) != null) || (sequenceBuilder != null)) {
                /* When readString is null and it enters the loop, that means the end of the file was reached. */
                /* Therefore, the last gene must be considered and recorded. */
                if (readString != null) {
                    if (readString.isEmpty()) {
                        continue;
                    }

                    /* If is still the same gene, keep building sequence. */
                    if (readString.charAt(0) != '>') {
                        if (sequenceBuilder == null) {
                            sequenceBuilder = new StringBuilder();
                        }

                        sequenceBuilder.append(readString);

                        currentName = nextName;

                        continue;
                    }

                    nextName = readString;

                    if (sequenceBuilder == null) {
                        continue;
                    }
                }

                /* Get gene sequence from bytes */
                geneSequence = sequenceBuilder.toString(); //new String(genesStream.getDataReference(), lastIndex, currentIndex-lastIndex);
                sequenceBuilder = null;

                /* Parse gene sequence. */
                geneSequence = geneSequence.replaceAll("\\s", "");

                /* Avoid empty gene streams */
                if (geneSequence.length() < 1) {
                    continue;
                }

                /* Check if gene is correct. */
                if (currentName.replaceAll(">", "").trim().equalsIgnoreCase(geneHeader)
                        && (SequenceValidator.isValidCodonSequence(geneSequence, geneticCodeTable, genome.getFilters()) == 0)) {
                    /* Create new gene. New gene name is the first line of the gene in the stream after the ">". */
                    //String geneHeader = currentName.replaceAll(">", "").trim();
                    newGene = new Gene(currentName.replaceAll(">", "").trim(), genome);

                    /* Make sequence uppercase, replace Tinamin by Uracil, and set new gene as this sequence. */
                    newGene.createStructure(SequenceValidator.makeCorrectionsToGene(geneSequence), BioStructure.Type.mRNAPrimaryStructure);

                    if (newGene.getSequenceLength() == size) {
                        /* Add parsed gene to parsed genes list */
                        System.out.println("Gene read from file.");
                        return newGene;
                    } else {
                        newGene = null;
                    }
                }

            }
        } catch (IOException ex) { //TODO: exceptions
            ex.printStackTrace();
            Logger.getLogger(FastaParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("ERROR: gene not read from file");
        return newGene;
    }

    @Override
    public void setLoadingEnded() {
        /* Warn GUI about end of reading. */
        setChanged();
        notifyObservers(new GenePoolObserverMessage(GenePoolObserverMessage.MessageType.LOAD_COMPLETE, null));
    }

    public Vector<Gene> getGenes() {
        return genes;
    }

    /**
     * @return the numRejectedGenes
     */
    public int getNumRejectedGenes() {
        return numRejectedGenes;
    }
}
