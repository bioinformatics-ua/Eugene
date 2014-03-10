package pt.ua.ieeta.geneoptimizer.geneDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;

/**
 *
 * @author Paulo Gaspar
 */
public class UsageAndContextTables extends Observable implements Runnable
{ 
    /* Codon usage table itself, in form of HashMap. */
    private Map<String, Integer> codonUsageTable;

    /* Codon context table, in form of hash map of hash map. */
    private Map<String, Integer> codonContextTable;

    /* Amino acid frequency table. */
    private Map<String, Integer> aminoAcidFrequencyTable;

    /* Amino acid context table. */
    private Map<String, Integer> aminoAcidPairFrequencyTable;

    /* Reference to the genome this codon usage table refers to. */
    private Genome genome;
    
    /* Genes present in genome */
    private List<Gene> genes;

    /* Flag indicating that this thread hasn't finished yet. */
    private boolean hasFinished = false;
    
    /* Flag indicating if all statistics have been calculated. */
    private boolean hasCalculated = false;

    /* Min and max codon usage and context in the tables. */
    private Float minCodonUsage = null, maxCodonUsage = null, averageCodonUsage = null, stdCodonUsage = null;
    private Float maxCodonPairScore = null, minCodonPairScore = null, averageCodonContext = null, stdCodonContext = null;
    private Float minCodonContextCount = null, maxCodonContextCount = null;

    /* Variable to count the total number of codons and pairs; */
    private int numberOfCodons = 0;
    private int numberOfPairs = 0;
    
    /* Progress panel. */
    ProcessPanel processPanel;

    
    public UsageAndContextTables(Genome genomeReference, List<Gene> genesReference)
    {
        codonUsageTable = new HashMap<String, Integer>(70);
        codonContextTable = new HashMap<String, Integer>(4100);
        aminoAcidFrequencyTable  = new HashMap<String, Integer>(30);
        aminoAcidPairFrequencyTable = new HashMap<String, Integer>(550);
        
        genome = genomeReference;
        genes = genesReference;
    }

    private void fillUsageAndContextTables()
    {
        /* Run through all genes to make counting. */
        int geneCounter = 0;
        for (Gene gen : genes)
        {
            geneCounter++;
            processPanel.setProgress(geneCounter*100/genes.size());

            numberOfCodons += gen.getSequenceLength();

            
            BioStructure codonSequence = gen.getStructure(BioStructure.Type.mRNAPrimaryStructure);
            String codonPair, codon, aminoacid, aminoacidPair;
            
            //count the first codon
            codon = codonSequence.getWordAt(0);
            if (!codonUsageTable.containsKey(codon))
                codonUsageTable.put(codon, 1);
            else
                codonUsageTable.put(codon, codonUsageTable.get(codon)+1);

            //count the first amino acid
            aminoacid = genome.getAminoAcidFromCodon(codon);
            if (!aminoAcidFrequencyTable.containsKey(aminoacid))
                aminoAcidFrequencyTable.put(aminoacid, 1);
            else
                aminoAcidFrequencyTable.put(aminoacid, aminoAcidFrequencyTable.get(aminoacid) + 1);

            /* Run through all codons in this gene, and count each one of them. */
            for (int i=1; i<gen.getSequenceLength(); i++)
            {
                /* Get codon and codon pair. */
                codon = codonSequence.getWordAt(i);
                codonPair = codonSequence.getWordAt(i-1) + codon;
                aminoacid = genome.getAminoAcidFromCodon(codonSequence.getWordAt(i));
                aminoacidPair = genome.getAminoAcidFromCodon(codonSequence.getWordAt(i-1)) + aminoacid;

                /* Count codon. */
                if (!codonUsageTable.containsKey(codon))
                {
//                    if (geneCounter == 396)
//                    {
//                        System.out.println("Codon: " + codon + "   ExtractedCodon: " + gen.getCodonSequence().substring(i*3, i*3+3));
//                        System.out.println("i = " + i);
//                        System.out.println("GENE: " + gen.toString());
//                        System.out.println("GENE: " + gen.getCodonSequence());
//                    }
                    //System.out.println("Codon: " + codon);
                    codonUsageTable.put(codon, 1);
                }
                else
                    codonUsageTable.put(codon, codonUsageTable.get(codon)+1);

                /* Count codon pair. */
                if (!codonContextTable.containsKey(codonPair))
                    codonContextTable.put(codonPair, 1);
                else
                    codonContextTable.put(codonPair, codonContextTable.get(codonPair) + 1);

                /* Count amino acid. */
                if (!aminoAcidFrequencyTable.containsKey(aminoacid))
                    aminoAcidFrequencyTable.put(aminoacid, 1);
                else
                    aminoAcidFrequencyTable.put(aminoacid, aminoAcidFrequencyTable.get(aminoacid) + 1);

                /* Count amino acid pair */
                if (!aminoAcidPairFrequencyTable.containsKey(aminoacidPair))
                    aminoAcidPairFrequencyTable.put(aminoacidPair, 1);
                else
                    aminoAcidPairFrequencyTable.put(aminoacidPair, aminoAcidPairFrequencyTable.get(aminoacidPair) + 1);
            }
        }

        /* Number of codon pairs. */
        numberOfPairs = numberOfCodons - genes.size();

        //TODO: criar outra tabela so com as frequencias, de forma a aumentar o desempenho
        
        processPanel.setStatus("Done.");
    }

    public synchronized void run()
    {
        processPanel = ProgressPanel.getInstance().newProgressProcess("Usage and Context");
        
        fillUsageAndContextTables();
        hasFinished = true;

        /* Calculate averages and standard deviations. */
        calculateStatistics();
        hasCalculated = true;
        
        this.notifyAll();
        setChanged();
        notifyObservers();
        
        processPanel.setComplete();

        //printUsageTable();
        //printContextTable();
        //printAminoAcidTable();*/
        
    }

    private synchronized void calculateStatistics()
    {
        System.out.println("  Started calculating statistics...");
        
        processPanel.setStatus("Calculating statistics");
        processPanel.setProgress(0);
        
        /* Calculate average codon usage. */
        float sum = 0;
        for(String key: codonUsageTable.keySet())
            sum += getCodonUsageRSCU(key);
        averageCodonUsage = sum / codonUsageTable.keySet().size();

        processPanel.setProgress(25);
        
        /* Calculate codon usage standard deviation. */
        sum = 0;
        for(String key: codonUsageTable.keySet())
            sum += Math.abs(averageCodonUsage - getCodonUsageRSCU(key));
        stdCodonUsage = sum / codonUsageTable.keySet().size();

        processPanel.setProgress(50);
        
        /* Calculate average codon context. */
        sum = 0;
        for(String key: codonContextTable.keySet())
            sum += getCodonPairScore(key.substring(0, 3), key.substring(3, 6));
        averageCodonContext = sum / codonContextTable.keySet().size();

        processPanel.setProgress(75);
        
        /* Calculate codon context standard deviation. */
        sum = 0;
        for(String key: codonContextTable.keySet())
            sum += Math.abs(averageCodonContext - getCodonPairScore(key.substring(0, 3), key.substring(3, 6)));
        stdCodonContext = sum / codonContextTable.keySet().size();

        processPanel.setProgress(100);
        processPanel.setStatus("Done.");
        
        System.out.println("  Done calculating statistics.");
    }

    /* Return RSCU (Relative Synonymous Codon Usage) value for some codon. */
    public synchronized float getCodonUsageRSCU(String codon)
    {
        assert codonUsageTable != null;
        assert genome != null;
        assert codon != null;

        waitUntilFinished();
        
        int numOcc = getCodonUsageCount(codon);
        float aaCount = getAminoAcidCount(genome.getAminoAcidFromCodon(codon));
        float numSyns = genome.getGeneticCodeTable().getNumberOfSynonymous(codon);
        
        return numOcc / (aaCount / numSyns);
    }    
    
    
    /* Get codon relative adaptiveness for some codon. This is used to calculate CAI. */
    public synchronized float getCodonRelativeAdaptiveness(String codon)
    {
        assert codonUsageTable != null;
        assert genome != null;
        assert codon != null;

        waitUntilFinished();
        
        List<String> syn = genome.getGeneticCodeTable().getSynonymousFromCodon(codon);
        int maxIndex = 0;
        for (int i=1; i<syn.size(); i++)
            if (getCodonUsageFrequency(syn.get(i)) > getCodonUsageFrequency(syn.get(maxIndex)))
                maxIndex = i;
        
        return getCodonUsageFrequency(codon) / getCodonUsageFrequency(syn.get(maxIndex));
    }
    
    
    /**
     * Return codon usage frequency per thousand 
     * @param codon
     * @return 
     */
    public synchronized float getCodonUsageFrequency(String codon)
    {
        assert codonUsageTable != null;
        assert codon != null;

        waitUntilFinished();
        
        /* Make frequency calculation (per thousand). */
        return (getCodonUsageCount(codon)*1000) / (float)numberOfCodons;
    }
    
    /**
     * Check if a codon is rare in the genome (appearance < 5 in 1000)
     * @param codon
     * @return 
     */
    public synchronized boolean isCodonRare(String codon){
        assert codonUsageTable != null;
        assert codon != null;
        
        waitUntilFinished();
        
        return (getCodonUsageFrequency(codon) <= 5);
    }

    public synchronized float getCodonContextFrequency(String codon1, String codon2)
    {
        assert codonContextTable != null;
        assert codon1 != null;
        assert codon2 != null;

        waitUntilFinished();

        /* Make frequency calculation (per thousand). */
        return getCodonContextCount(codon1,codon2) * 1000 / (float)numberOfCodons;
    }

    /** Codon context according to the CPS formula. From the paper "Virus Attenuation by Genome-Scale Changes in Codon Pair Bias". */
    public synchronized float getCodonPairScore(String codon1, String codon2)
    {
        assert codonContextTable != null;
        assert codon1 != null;
        assert codon2 != null;

        waitUntilFinished();

        if (!codonContextTable.containsKey(codon1+codon2)) return 0;

        String aminoAcid1 = genome.getAminoAcidFromCodon(codon1);
        String aminoAcid2 = genome.getAminoAcidFromCodon(codon2);

        if (!aminoAcidPairFrequencyTable.containsKey(aminoAcid1+aminoAcid2)) return 0;

        //TODO: this can DEFINITILY be pre-calculated for performance improvement.
        double aux1 = (double)getCodonUsageCount(codon1) * (double)getCodonUsageCount(codon2);
        double aux2 = (double)aminoAcidFrequencyTable.get(aminoAcid1) * (double)aminoAcidFrequencyTable.get(aminoAcid2);
        double aux3 = (aux1 * aminoAcidPairFrequencyTable.get(aminoAcid1 + aminoAcid2)) / aux2;
        float result = (float) Math.log(getCodonContextCount(codon1,codon2) / aux3);

        assert aminoAcidFrequencyTable.get(aminoAcid1) >= getCodonUsageCount(codon1);
        assert aminoAcidPairFrequencyTable.get(aminoAcid1 + aminoAcid2) >= getCodonContextCount(codon1,codon2);
        assert aminoAcidFrequencyTable.get(aminoAcid1) + aminoAcidFrequencyTable.get(aminoAcid2) >= aminoAcidPairFrequencyTable.get(aminoAcid1 + aminoAcid2);
        assert getCodonUsageCount(codon1) + getCodonUsageCount(codon2) >= codonContextTable.get(codon1+codon2);

        
//        if (aminoAcidTable.get(aminoAcid2) < codonUsageTable.get(codon2))
//            System.out.println("ERRO: " + aminoAcidTable.get(aminoAcid2) +  " < "+ codonUsageTable.get(codon2));
//        
//        if (Float.isNaN(result))
//            System.out.println("Result: " + result + "\t\t-->  ln(" + codonContextTable.get(codon1+codon2) + " / ( ("+codonUsageTable.get(codon1)+"*"+codonUsageTable.get(codon2)+")/("+aminoAcidTable.get(aminoAcid1)+"*"+aminoAcidTable.get(aminoAcid2)+") * " + aminoAcidContextTable.get(aminoAcid1 + aminoAcid2) + "))");
        
        assert !Float.isNaN(result);
        
        return result;
    }

    public synchronized int getCodonUsageCount(String codon)
    {
        assert codonUsageTable != null;
        
        waitUntilFinished();
        
        if (codonUsageTable.containsKey(codon))
            return codonUsageTable.get(codon);
        else
            return 0;
    }
    
    /** Returns the number of times a codon-pair is used in the genome. */
    public synchronized int getCodonContextCount(String codon1, String codon2)
    {
        assert codonContextTable != null;
        assert codon1 != null;
        assert codon2 != null;

        waitUntilFinished();

        if (!codonContextTable.containsKey(codon1+codon2)) return 0;

        return codonContextTable.get(codon1+codon2);
    }

    public synchronized int getAminoAcidCount(String aminoacid)
    {
        assert aminoAcidFrequencyTable != null;
        assert aminoacid != null;

        waitUntilFinished();
        
        if (aminoAcidFrequencyTable.containsKey(aminoacid))
            return aminoAcidFrequencyTable.get(aminoacid);
        else
            return 0;
    }

    public float getMaxCodonContextCount()
    {
        waitUntilFinished();

        if (maxCodonContextCount == null)
        {
            float max = Float.NEGATIVE_INFINITY;
            float m;
            for (String codonPair : codonContextTable.keySet())
                if ((m = getCodonContextCount(codonPair.substring(0, 3), codonPair.substring(3, 6))) > max)
                    max = m;

            maxCodonContextCount = max;
        }

        return maxCodonContextCount;
    }

    public float getMinCodonContextCount()
    {
        waitUntilFinished();

        if (minCodonContextCount == null)
        {
            float min = Float.POSITIVE_INFINITY;
            float m;

            for (String codonPair : codonContextTable.keySet())
                if ((m = getCodonContextCount(codonPair.substring(0, 3), codonPair.substring(3, 6))) < min)
                    min = m;

            minCodonContextCount = min;
        }

        return minCodonContextCount;
    }

    public float getMaxCodonPairScore()
    {
        waitUntilFinished();

        if (maxCodonPairScore == null)
        {
            float max = Float.NEGATIVE_INFINITY;
            float m;
            for (String codonPair : codonContextTable.keySet())
                if ((m = getCodonPairScore(codonPair.substring(0, 3), codonPair.substring(3, 6))) > max)
                    max = m;

            maxCodonPairScore = max;
        }

        return maxCodonPairScore;
    }

    public float getMinCodonPairScore()
    {
        waitUntilFinished();

        if (minCodonPairScore == null)
        {
            float min = Float.POSITIVE_INFINITY;
            float m;

            for (String codonPair : codonContextTable.keySet())
                if ((m = getCodonPairScore(codonPair.substring(0, 3), codonPair.substring(3, 6))) < min)
                    min = m;

            minCodonPairScore = min;
        }

        return minCodonPairScore;
    }

    public float getMaxCodonUsageFrequency()
    {
        waitUntilFinished();
        if (maxCodonUsage == null)
        {
            float max = 0;

            for (String codon : codonUsageTable.keySet())
                if (getCodonUsageRSCU(codon) > max)
                    max = getCodonUsageRSCU(codon);
            
            maxCodonUsage = max;
        }

        return maxCodonUsage;
    }

    public float getMinCodonUsageFrequency()
    {
        waitUntilFinished();
        if (minCodonUsage == null)
        {
            float min = Float.MAX_VALUE;

            for (String codon : codonUsageTable.keySet())
                if (getCodonUsageRSCU(codon) < min)
                    min = getCodonUsageRSCU(codon);
            
            minCodonUsage = min;
        }

        return minCodonUsage;
    }

    public float getAverageRSCU()
    {
        waitUntilFinished();
        
        assert averageCodonUsage != null;
        
        return averageCodonUsage;
    }

    public float getStdDevRSCU()
    {
        waitUntilFinished();

        return stdCodonUsage;
    }

    public float getAverageCodonContext()
    {
        waitUntilFinished();

        return averageCodonContext;
    }

    public float getStdDevCodonContext()
    {
        waitUntilFinished();

        return stdCodonContext;
    }
    

    public synchronized void waitUntilFinished()
    {
        /* Avoid calling functions when this thread hasn't finished processing yet. */
        while (!hasFinished && !hasCalculated)
        {
            try
            {
                /* Wait until it finishes. */
                this.wait();
            }
            catch (InterruptedException ex)
            { //TODO: excepção
                Logger.getLogger(UsageAndContextTables.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /** Returns wether all counting and calculations are complete. */
    public boolean isComplete()
    {
        return hasFinished && hasCalculated;
    }

    /* DEBUGGING */

    public void printUsageTable()
    {
        waitUntilFinished();

        System.out.println("*** Codon Usage Table ***");

        for(String key: codonUsageTable.keySet())
            System.out.println(key+": " + getCodonUsageRSCU(key));
    }

    public void printContextTable()
    {
        waitUntilFinished();

        System.out.println("*** Codon Context Table ***");

        for(String key: codonContextTable.keySet())
            //System.out.println(key+": " + getCodonContextCount(key.substring(0, 3), key.substring(3, 6)));
            System.out.println(getCodonPairScore(key.substring(0, 3), key.substring(3, 6)));
    }

    public void printAminoAcidTable()
    {
        waitUntilFinished();

        System.out.println("*** Amino Acid Table ***");

        for(String key: aminoAcidFrequencyTable.keySet())
            System.out.println(key+": " + getAminoAcidCount(key));
    }
    
}
