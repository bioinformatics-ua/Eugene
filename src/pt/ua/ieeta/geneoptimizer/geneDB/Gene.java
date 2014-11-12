package pt.ua.ieeta.geneoptimizer.geneDB;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;

/**
 *
 * @author Paulo Gaspar
 */
public class Gene //implements Comparable<Gene>
{
    private String fastaOriginalHeaderText;
    private String geneName;
    private String productName;
    private boolean gotGeneNameConfirmation;
    private boolean gotProductNameConfirmation;
    
    /* Genome where this gene is. */
    private Genome genome;

    /* Structures of this gene. */
    private Map<BioStructure.Type, BioStructure> bioStructures;

    /* List of orthologs for this gene. */
    private Genome orthologList;

    /* PDB (Protein Data Bank) code of this gene resulting protein. */
    private String PDBCode = null;
    private String PDBMemory = null;

    /* Orthologs parameters. Only used if this gene is actually an obtained ortholog of another gene. */
    private int Score;
    private double identity;
    private String orthologId;
    private String genomeName;
    private Map<BioStructure.Type, BioStructure> alignedBioStructures;
    private Float CAIValue = null, CPBValue = null, EffectiveNumberCodons = null;

    public Gene(String iName, Genome genome)
    {
        assert (iName != null);
        assert (iName.length() > 0);
        assert genome != null;

        this.fastaOriginalHeaderText = iName;
        this.geneName = iName;
        this.genome = genome;
        if(genome != null) {
            this.genomeName = genome.getName();
        }
        this.orthologList = null;
        this.PDBCode = null;
        this.bioStructures = new EnumMap<BioStructure.Type, BioStructure>(BioStructure.Type.class);

        this.gotGeneNameConfirmation = false;
        this.gotProductNameConfirmation = false;
        
        assert bioStructures != null;
    }

    public boolean hasGeneNameConfirmation()
    {
        return gotGeneNameConfirmation;
    }

    public void setHasNameConfirmation(boolean gotGeneNameConfirmation)
    {
        this.gotGeneNameConfirmation = gotGeneNameConfirmation;
    }

    public boolean hasProductNameConfirmation()
    {
        return gotProductNameConfirmation;
    }

    public void setHasProductNameConfirmation(boolean gotProductNameConfirmation)
    {
        this.gotProductNameConfirmation = gotProductNameConfirmation;
    }

    public String getProductName()
    {
        return productName;
    }

    public void setProductName(String productName)
    {
        this.productName = productName;
    }
    
    public void setGeneName(String name)
    {
        this.geneName = name;
    }

    public void createStructure(String sequence, BioStructure.Type structureType)
    {
        assert sequence != null;
        assert structureType != null;
        assert sequence.length()> 0;

        bioStructures.put(structureType, new BioStructure(sequence, structureType));
    }

    /* Create copy structure from a given structure. */
    public void createStructure(BioStructure bioStrut)
    {
        assert bioStrut != null;

        BioStructure bioStructure = new BioStructure(bioStrut.getSequence(), bioStrut.getType());
        bioStructures.put(bioStrut.getType(), bioStructure);
    }
    
    /* Calculate and return Codon Adaptation Index. 
     this value must be calculated here instead of UsageAndContextTables to
     save the value for each gene */
    public float getCAI()
    { 
        /* If the CAI value was calculated before, don't calculate again. Instead return the previous calculated value. */
        if (CAIValue != null)
            return CAIValue;
        
        double result = 1;
        double value;
        double accumulator = 1;
        
        Genome highlyExpressed = genome.getHouseKeepingGenes();
        
        assert highlyExpressed != null;
        
        /* Count the number of codons with RSCU equal to 1. Those do not contribute to the CAI. */
        int unityRSCUcounter = 0;
        for (int i=0; i<getSequenceLength()-1; i++)
            if (highlyExpressed.getUsageAndContextTables().getCodonUsageRSCU(getCodonAt(i)) == 1f)
                unityRSCUcounter++;
        
        double exponent = 1.0f/(float)(getSequenceLength()-unityRSCUcounter-1);
        
        /* Multiply the relative adaptiveness of all codons (except stop codons) to calculate CAI. */
        for (int i=0; i<getSequenceLength()-1; i++)
        {
            value = highlyExpressed.getUsageAndContextTables().getCodonRelativeAdaptiveness(getCodonAt(i));
            result *= (value != 0) ?  value : 0.5;
            
            /* If result is too low, save it and reset to 1. */
            if (result < Math.pow(10,-100))
            {
                accumulator = accumulator * Math.pow(result, exponent); 
                result = 1;
            }
        }
        
        accumulator = accumulator * Math.pow(result, exponent); 
        
        /* Save the CAI value for future reference. */
        CAIValue = (float) accumulator;
        
        return (float) accumulator;
    }
    
    public float getSelectedCAI(int beginIdx, int endIdx) {
        double result, accumulator, value, exponent;
        int counterRSCU, size;
        Genome highlyExpressed;
        
        highlyExpressed = genome.getHouseKeepingGenes();
        counterRSCU = 0;
        size = endIdx - beginIdx;
        
        if(endIdx == getSequenceLength()-1) {
            size--;
            endIdx--;
        }
        
        for(int i = beginIdx; i <= endIdx; i++)
            if(highlyExpressed.getUsageAndContextTables().getCodonUsageRSCU(getCodonAt(i)) == 1f)
                counterRSCU++;
        
        exponent = 1f / (float) (size-counterRSCU);
        result = 1;
        accumulator = 1;
        
        for(int i = beginIdx; i <= endIdx; i++) {
            value = highlyExpressed.getUsageAndContextTables().getCodonRelativeAdaptiveness(getCodonAt(i));
            result *= (value != 0) ? value : 0.5;
            
            if(result < Math.pow(10, -100)) {
                accumulator = accumulator * Math.pow(result, exponent);
                result = 1;
            }
        }
        
        accumulator = accumulator * Math.pow(result, exponent);
        
        return (float) accumulator;
    }
    
    public boolean hasCAI()
    {
        return genome.getHouseKeepingGenes() != null;
    }
    
    public float getCPB()
    {
        assert genome != null;
        
        if (CPBValue != null)
            return CPBValue;
        
        float result = 0;
        
        for (int i=0; i<getSequenceLength()-1; i++)
            result += genome.getUsageAndContextTables().getCodonPairScore(getCodonAt(i), getCodonAt(i+1));
        
        CPBValue = result/(getSequenceLength()-1);
        
        return CPBValue;
    }
    
    public float getAverageRSCU()
    {
        assert genome != null;
        
        float result = 0;
        
        for (int i=0; i<getSequenceLength(); i++)
            result += genome.getUsageAndContextTables().getCodonUsageRSCU(getCodonAt(i));
        
        return result / getSequenceLength();
    }
    
    /* Calculate the effective number of codons according to Wright's form. 
       The result should be between 20 and 61. */
    public float getEffectiveNumberOfCodons()
    {
        if (EffectiveNumberCodons != null)
            return EffectiveNumberCodons;
        
        float sum = 0;
        for (int i=1; i<8; i++)
        {
            float aux = getNormalizedAverageHomozygosity(i);
            sum += aux;
            
        }
        
      //  System.out.println("**************** NC = " + sum);
        
        EffectiveNumberCodons = sum;
        
        return sum;
    }
    
    /* Calculate average homozygosity for the aminoacids whose codons have N synonymous.
       N is the input for this function. This is calculated according to Wright's way. 
       The final average homozygosity is then normalized dividing the class size by the 
       average homozygosity:  K/average(Fi) */
    public float getNormalizedAverageHomozygosity(int synonymousNumber)
    {
        assert genome != null;
        assert genome.getGeneticCodeTable() != null;
        
        GeneticCodeTable gct = genome.getGeneticCodeTable();
        List<String> aaList = gct.getAminoacidList();
        
        /* Keep track of the number of amino acids that have N synonymous codons (size of the class N). */
        int numberOfAminoAcids = 0;
        float sum = 0;
        
        /* Find amino acids with N synonymous codons and do the math. */
        for (String aa : aaList)
            if (gct.getSynonymousFromAA(aa).size() == synonymousNumber)
            {
                /* First class case. */
                if (synonymousNumber == 1)
                {
                    numberOfAminoAcids++;
                    continue;
                }
                
                int aaCount = countOfAminoAcid(aa);
                if (aaCount <= 1)
                    continue; //TODO: ha metodos para ultrapassar o problema de nao haver um AA no gene
                
                numberOfAminoAcids++;
                
                float aux = 0;
                for (String codon : gct.getSynonymousFromAA(aa))
                {
                    /* Square the frequency of each synoynymous, and add to accumulator. */
                    float codonFrequency = ((float)countOfCodon(codon))  / (float)aaCount;
                    aux +=  codonFrequency * codonFrequency; 
                }
                
                /* Terminate calculations on this amino acid's homozygosity. */
                aux *= aaCount;
                aux -= 1;
                aux /= (aaCount-1);
                
                sum += aux;
            }
        
        /* First class is a special case. */
        if (synonymousNumber == 1)
        {
          //  System.out.println("For class " + synonymousNumber + ", Average Fi = " + numberOfAminoAcids + "  and  result = " + numberOfAminoAcids);
            return numberOfAminoAcids;
        }
        
        /* No amino acids in this class. Return zero. */
        if (numberOfAminoAcids == 0)
        {
          //  System.out.println("For class " + synonymousNumber + ", Average Fi = " + 0 + "  and  result = " + 0);
            return 0;
        }
        
        /* Normalize the result. */
      //  System.out.println("For class " + synonymousNumber + ", Average Fi = " + sum/numberOfAminoAcids + "  and  result = " + numberOfAminoAcids/(sum/numberOfAminoAcids));
        return numberOfAminoAcids/(sum/numberOfAminoAcids);
    }
    
    /* Returns number of occurences of an amino acid in this gene's protein sequence. */
    public int countOfAminoAcid(String AA)
    {
        String AASequence = this.getAminoacidSequence();
        
        int c = 0;
        for (int i=0; i<AASequence.length(); i++)
            if (AASequence.toUpperCase().charAt(i) == AA.toUpperCase().charAt(0))
                c++;
        
        return c;
    }
    
    /* Returns number of occurences of a codon in this gene's protein sequence. */
    public int countOfCodon(String codon)
    {
        BioStructure codonSequence = this.getStructure(BioStructure.Type.mRNAPrimaryStructure);
        
        int c = 0;
        for (int i=0; i < getSequenceLength(); i++)
            if (codonSequence.getWordAt(i).toUpperCase().equals(codon.toUpperCase()))
                c++;
        
        return c;
    }

    public String getName() {
        assert geneName != null;
        return geneName;
    }

    public String getGeneHeader()
    {
        assert fastaOriginalHeaderText != null;
        return fastaOriginalHeaderText;
    }

    /** Returns number of codons/aminoacids. */
    public int getSequenceLength()
    {
        assert ((bioStructures.get(BioStructure.Type.mRNAPrimaryStructure) != null) || (bioStructures.get(BioStructure.Type.proteinSecondaryStructure) != null));

        if (bioStructures.get(BioStructure.Type.mRNAPrimaryStructure) != null)
            return bioStructures.get(BioStructure.Type.mRNAPrimaryStructure).getLength();

        if (bioStructures.get(BioStructure.Type.proteinSecondaryStructure) != null)
            return bioStructures.get(BioStructure.Type.proteinSecondaryStructure).getLength();

        assert false;

        return -1;
    }

    /* Returns the codon at a given position of the aligned sequence. */
    public String getAlignedCodonAt(int position)
    {
        assert alignedBioStructures != null;
        assert alignedBioStructures.get(BioStructure.Type.mRNAPrimaryStructure) != null;

        return alignedBioStructures.get(BioStructure.Type.mRNAPrimaryStructure).getWordAt(position);
    }
    
    public String getCodonAt(int position)
    {
        assert bioStructures != null;
        assert bioStructures.get(BioStructure.Type.mRNAPrimaryStructure) != null;
        
        return bioStructures.get(BioStructure.Type.mRNAPrimaryStructure).getWordAt(position);
    }

    public String getAlignedAminoAcidAt(int position)
    {
        assert alignedBioStructures != null;
        assert alignedBioStructures.get(BioStructure.Type.proteinPrimaryStructure) != null;

        return alignedBioStructures.get(BioStructure.Type.proteinPrimaryStructure).getWordAt(position);
    }

    /* Returns codon sequence. */
    public String getCodonSequence()
    {
        assert bioStructures != null;
        assert bioStructures.get(BioStructure.Type.mRNAPrimaryStructure) != null;

        return bioStructures.get(BioStructure.Type.mRNAPrimaryStructure).getSequence();
    }

    /* Returns aminoacid sequence. */
    public String getAminoacidSequence()
    {
        assert bioStructures != null;
        assert bioStructures.get(BioStructure.Type.proteinPrimaryStructure) != null;

        return bioStructures.get(BioStructure.Type.proteinPrimaryStructure).getSequence();
    }

    /* Returns codon sequence. From indexStart (inclusive) to indexEnd (exclusive). 
     * Indices in number of codons. */
    public String getCodonSubSequence(int indexStart, int indexEnd)
    {
        assert bioStructures != null;
        assert bioStructures.get(BioStructure.Type.mRNAPrimaryStructure) != null;
        assert indexStart >= 0;
        assert indexEnd >= 0;
        
        if (indexStart == indexEnd) return "";

        return bioStructures.get(BioStructure.Type.mRNAPrimaryStructure).getSubSequence(indexStart, indexEnd);
    }

    public int numberOfAvailableStructures()
    {
        assert bioStructures != null;

        return bioStructures.keySet().size();
    }

    public boolean hasSequenceOfType(BioStructure.Type type)
    {
        assert bioStructures != null;
        
        return bioStructures.get(type) != null;
    }

    public BioStructure getStructure(BioStructure.Type type)
    {
        assert bioStructures != null;
        assert hasSequenceOfType(type);

        return bioStructures.get(type);
    }

    public void setGenome(Genome genome) {
        this.genome = genome;
        this.genomeName = genome.getName();
    }
    
    public Genome getGenome() {
        return genome;
    }

    public void calculateAllStructures()
    {
        assert bioStructures != null;
        assert genome != null;

        if (!hasSequenceOfType(BioStructure.Type.mRNAPrimaryStructure))
            return;

        boolean calculateProteinPrimStructure = (Boolean) ApplicationSettings.getProperty("autoCalculateProteinPrimaryStructure", Boolean.class);
        if (calculateProteinPrimStructure)
        {
            BioStructure struct = bioStructures.get(BioStructure.Type.mRNAPrimaryStructure);
            StringBuilder proteinPrimaryStructure = new StringBuilder(); //"";
            for (int i=0; i<struct.getLength(); i++)
                proteinPrimaryStructure.append(genome.getAminoAcidFromCodon(struct.getWordAt(i)));

            bioStructures.put(BioStructure.Type.proteinPrimaryStructure, new BioStructure(proteinPrimaryStructure.toString(), BioStructure.Type.proteinPrimaryStructure));
        }

        boolean calculateProteinSeconStructure = (Boolean) ApplicationSettings.getProperty("autoCalculateProteinSecondaryStructure", Boolean.class);
        if (calculateProteinSeconStructure)
        {
            //TODO: calcula estrutura secundaria
        }
        
    }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder("");
        for (int i=0; i<getSequenceLength(); i++)
            str.append(getStructure(BioStructure.Type.mRNAPrimaryStructure).getWordAt(i) + " ");

        return str.toString();
    }

    public float getGCContent()
    {
        if (!hasSequenceOfType(BioStructure.Type.mRNAPrimaryStructure))
            return 0;

        BioStructure struct = bioStructures.get(BioStructure.Type.mRNAPrimaryStructure);

        float GCcontent = 0f;
        for (int i=0; i<getSequenceLength(); i++)
        {
            String codon = struct.getWordAt(i);

            if (codon.charAt(0) == 'G') GCcontent++;
            else if (codon.charAt(0) == 'C') GCcontent++;

            if (codon.charAt(1) == 'G') GCcontent++;
            else if (codon.charAt(1) == 'C') GCcontent++;

            if (codon.charAt(2) == 'G') GCcontent++;
            else if (codon.charAt(2) == 'C') GCcontent++;
        }

        GCcontent = GCcontent/(getSequenceLength()*3);
        return GCcontent;
    }

    public synchronized void setOrthologList(Genome list)
    {
        orthologList = list;
        this.alignedBioStructures = new HashMap<BioStructure.Type, BioStructure>();
    }

    public Genome getOrthologList() 
    { return orthologList; }

    public void setPDBCode(String PDBcode)
    { this.PDBCode = PDBcode; }

    public String getPDBCode()
    { return PDBCode; }

    public boolean hasOrthologs()
    {
        if (orthologList == null) return false;

        return orthologList.getGenes().size() > 0;
    }

    
    /********************************************************/
    /*             If this gene is an ortholog              */
    /********************************************************/

    public void setOrthologInfo(int Score, double identity, String ID, String genomeName)
    {
        this.Score = Score;
        this.identity = identity;
        this.orthologId = ID;
        this.genomeName = genomeName;
        this.alignedBioStructures = new HashMap<BioStructure.Type, BioStructure>();
    }

    public void setAlignedStructure(String sequence, BioStructure.Type structureType)
    {
        assert sequence != null;
        assert structureType != null;
        assert sequence.length()  > 0;

        alignedBioStructures.put(structureType, new BioStructure(sequence, structureType));
    }

    public BioStructure getAlignedStructure(BioStructure.Type type)
    {
        assert alignedBioStructures != null;
        assert alignedBioStructures.get(type) != null;

        return alignedBioStructures.get(type);
    }

    public boolean hasAlignedStructure(BioStructure.Type type)
    {
        if (alignedBioStructures == null) return false;

        return alignedBioStructures.containsKey(type);
    }

    public int getScore()
    { return Score; }

    public double getIdentity()
    { return identity; }

    public String getGenomeName()
    { return genomeName; }

    public String getOrthologId()
    { return orthologId; }

    
    public List<Gene> generateRandomSynonymousGenes(int maxSynonymous){
        // deal with this case
        if (maxSynonymous < 1)
            return null;
        
        List<Gene> randomGeneList = new ArrayList<Gene>();
        
        final String codonSequence = getCodonSequence();
        
        int count = 0;
        
        while(count < maxSynonymous){
            StringBuilder newGeneSequence = new StringBuilder();
            for(int i = 0; i < codonSequence.length(); i=i+3){
                newGeneSequence.append(getRandomsSynonymous(codonSequence.substring(i, i+3)));
            }
            //ensure that the resulting sequence as the same length as the original
            assert newGeneSequence.length() == codonSequence.length();
            
            Gene g = new Gene("Random " + (count+1) + " -> " + geneName, genome);
            g.createStructure(newGeneSequence.toString(), BioStructure.Type.mRNAPrimaryStructure);
            g.calculateAllStructures();
            randomGeneList.add(g);
            count++;
        }
        return randomGeneList;
    }
    
    private String getRandomsSynonymous(String codon){
        assert codon != null && !codon.isEmpty() && codon.length() == 3;
        
        List<String> synonymCodonList = genome.getGeneticCodeTable().getSynonymousFromCodon(codon);
        Random randomGenerator = new Random();
        int randomSelected = randomGenerator.nextInt(synonymCodonList.size());
        return synonymCodonList.get(randomSelected);
    }
    
    /************************* TESTE **************************/
//    @Override
//    public int compareTo(Gene t)
//    {
//        if (getGCContent() > t.getGCContent()) return 1;
//        else
//            if (getGCContent() == t.getGCContent()) return 0;
//            else
//                return -1;
//    }
    
    public void setPDBMemory(String pdb) {
        if(hasPDBMemory())
            PDBMemory = pdb;
    }
    
    public boolean hasPDBMemory() {
        return PDBCode.equals("_manual_");
    }
    
    public String getPDBMemory() {
        return PDBMemory;
    }
}
