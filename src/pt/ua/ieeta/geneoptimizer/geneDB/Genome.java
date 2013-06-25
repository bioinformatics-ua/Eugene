package pt.ua.ieeta.geneoptimizer.geneDB;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Paulo Gaspar
 */
public class Genome {
    /* List of genes in this genome. */

    private List<Gene> genes = null;          // used in housekeeping and orthologs
    private String[] genesFiles = null;         // used in normal genome - reference to files
    private List<String> genesHeaders = null; // used in normal genome - headers of genes
    private List<Integer> genesLength = null; // used in normal genome - lengths of genes
    private List<Gene> genesAdded = null;     // used to save genes added manually

    /* ID of this genome. To identify it in the pool. */
    private int genomeID;

    /* Name of this genome. Might be the specie. */
    private String genomeName = "GENOME";
    private String genomeSmallName = "GENOME";
    private boolean hasNameConfirmation = false;

    /* Codon Usage Table for this genome. */
    private UsageAndContextTables codonUsageTable;

    /* Genetic Code Table ID used for this genome. */
    private GeneticCodeTable geneticCodeTable;
    /* Filters used in the genome */
    private GenomeFilters filters;

    /* If this genome is a set of orthologs, this variable tells wether they have an alignement or not. */
    private boolean orthologsAligned = false;

    /* Number of rejected genes. */
    private int numRejectedGenes = 0;
    private Genome houseKeepingGenes = null;

    public Genome() {
        genes = Collections.synchronizedList(new ArrayList<Gene>());
    }

    public Genome(List<Gene> iGenes, GeneticCodeTable geneticCodeTable) {
        assert iGenes != null;
        assert geneticCodeTable != null;

        this.genes = iGenes;
        this.geneticCodeTable = geneticCodeTable;
    }

    public Genome(List<Gene> iGenes, String name, GeneticCodeTable geneticCodeTable) {
        assert iGenes != null;
        assert name != null;
        assert geneticCodeTable != null;

        this.genes = iGenes;
        this.genomeName = name;
        this.geneticCodeTable = geneticCodeTable;

        makeSmallGenomeName();
    }

    /**
     * Create new empty genome.
     *
     * @param name name of genome
     * @param geneticCodeTable genetic code table to use
     */
    public Genome(String name, GeneticCodeTable geneticCodeTable) {
        assert name != null;
        assert geneticCodeTable != null;

        this.genes = Collections.synchronizedList(new ArrayList<Gene>());
        this.genomeName = name;
        this.geneticCodeTable = geneticCodeTable;

        makeSmallGenomeName();
    }

    public Genome(String name) {
        assert name != null;

        this.genes = Collections.synchronizedList(new ArrayList<Gene>());
        this.genomeName = name;

        makeSmallGenomeName();
    }

    public boolean hasNameConfirmation() {
        return hasNameConfirmation;
    }

    public void setHasNameConfirmation(boolean hasNameConfirmation) {
        this.hasNameConfirmation = hasNameConfirmation;
    }

    /* If a genome name is too big, use only the first two words. */
    private void makeSmallGenomeName() {
        int MAX_CHARS = 10;  //16

        if (genomeName.length() > MAX_CHARS) {
            String splitChar;
            if (genomeName.contains(" ")) {
                splitChar = " ";
            } else if (genomeName.contains("_")) {
                splitChar = "_";
            } else if (genomeName.contains("-")) {
                splitChar = "-";
            } else {
                genomeSmallName = genomeName.substring(0, Math.min(MAX_CHARS, genomeName.length()));
                return;
            }

            String words[] = genomeName.split(splitChar);
            if (words.length >= 2) {
                genomeSmallName = words[0] + " " + words[1];
            } else {
                genomeSmallName = genomeName.substring(0, Math.min(MAX_CHARS, genomeName.length()));
            }
        } else {
            genomeSmallName = genomeName;
        }
    }

    public synchronized String getName() {
        return genomeName;
    }

    public String getSmallName() {
        return genomeSmallName;
    }

    public void setFilters(GenomeFilters filters) {
        this.filters = filters;
    }

    public synchronized int getGenomeID() {
        return genomeID;
    }

    public synchronized void setGenomeID(int genomeID) {
        this.genomeID = genomeID;
    }

    public synchronized List<Gene> getGenes() {
        return genes;
    }

    public synchronized void setGenes(List<Gene> genes) {
        this.genes = genes;
    }

    public synchronized void setGenesRef(String[] filelist, List<Gene> genes) {
        genesFiles = new String[filelist.length];
        for (int i = 0; i < filelist.length; i++) {
            genesFiles[i] = new String(filelist[i]);
        }

        genesHeaders = new ArrayList<String>(100);
        genesLength = new ArrayList<Integer>(100);
        for (Iterator<Gene> it = genes.iterator(); it.hasNext();) {
            Gene gene = it.next();
            genesHeaders.add(gene.getGeneHeader());
            genesLength.add(gene.getSequenceLength());
        }
    }

    public synchronized List<String> getGenesHeaders() {
        return genesHeaders;
    }

    public synchronized String[] getGenesFiles() {
        return genesFiles;
    }

    public synchronized int getGeneLength(String geneHeader) {
        int index = genesHeaders.indexOf(geneHeader);
        if (index == -1) {
            throw new AssertionError("Gene not found in genome");
        }

        return genesLength.get(index);
    }

    public synchronized int getGeneLength(int index) {
        return genesLength.get(index);
    }

    public synchronized String getAminoAcidFromCodon(String codon) {
        assert geneticCodeTable != null;
        assert codon != null;

        return geneticCodeTable.getAminoAcidFromCodon(codon);
    }

    public synchronized void setCodonUsageContextTables(UsageAndContextTables cut) {
        codonUsageTable = cut;
    }

    public synchronized UsageAndContextTables getUsageAndContextTables() {
        return codonUsageTable;
    }

    public synchronized GeneticCodeTable getGeneticCodeTable() {
        return geneticCodeTable;
    }

    public synchronized void setGeneticCodeTable(GeneticCodeTable geneticCodeTable) {
        assert geneticCodeTable != null;

        this.geneticCodeTable = geneticCodeTable;
    }

    /**
     * To rapidly access the codon usage table and return the RSCU value for
     * some codon.
     */
    public synchronized float getCodonRSCU(String codon) {
        assert codon != null;
        assert codonUsageTable != null;

        return codonUsageTable.getCodonUsageRSCU(codon);
    }

    public void addGene(Gene newGene) {
        assert newGene != null;
        assert genes != null;

        genes.add(newGene);
    }

    public void addGeneManually(Gene newGene) {
        assert newGene != null;

        if (genesAdded == null) {
            genesAdded = new ArrayList<Gene>();
        }

        genesHeaders.add(newGene.getGeneHeader());        
        genesLength.add(newGene.getSequenceLength());
        genesAdded.add(newGene);
    }

    public List<Gene> getGenesAdded() {
        return genesAdded;
    }

    public GenomeFilters getFilters() {
        return filters;
    }

    public void removeManuallyAddedGenes(Gene gene) {
        if (genesAdded == null) {
            return;
        }
        genesAdded.remove(gene);                
        
        int index = genesHeaders.indexOf(gene.getGeneHeader());
        genesLength.remove(index);
        genesHeaders.remove(gene.getGeneHeader());
    }

    public Gene getManuallyAddedGene(String geneHeader, int sequenceLength) {
        if (genesAdded == null) {
            return null;
        }

        Gene geneRes = null;

        for (Gene gene : genesAdded) {
            if (gene.getGeneHeader().equals(geneHeader)
                    && gene.getSequenceLength() == sequenceLength) {
                geneRes = gene;
                break;
            }
        }
        return geneRes;
    }

    public boolean OrthologsAreAligned() {
        return orthologsAligned;
    }

    public void setOrthologsAligned(boolean orthologsAligned) {
        this.orthologsAligned = orthologsAligned;
    }

    public void removeGene(Gene gene) {
        genes.remove(gene);
    }

    public void setSpecieName(String name) {
        genomeName = name;
    }

    /**
     * @return the numRejectedGenes
     */
    public int getNumRejectedGenes() {
        return numRejectedGenes;
    }

    /**
     * @param numRejectedGenes the numRejectedGenes to set
     */
    public void addNumRejectedGenes(int numRejectedGenes) {
        this.numRejectedGenes += numRejectedGenes;
    }

    public void setName(String genomeName) {
        this.genomeName = genomeName;
        makeSmallGenomeName();
    }

    public void setHouseKeepingGenes(Genome result) {
        assert result != null;

        this.houseKeepingGenes = result;
    }

    public Genome getHouseKeepingGenes() {
        return this.houseKeepingGenes;
    }

    public void writeToFile(String filename) {
        try {
            PrintWriter out = new PrintWriter(new FileWriter(filename, false));

            for (Gene g : getGenes()) {
                out.println(">" + g.getName());
                out.println(g.getCodonSequence());
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            //TODO: exceptions
        }
    }
}
