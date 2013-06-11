
package pt.ua.ieeta.geneoptimizer.FileOpeningParsing;

import java.util.regex.Pattern;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import pt.ua.ieeta.geneoptimizer.geneDB.GenomeFilters;

/**
 *
 * @author Paulo Gaspar
 */
public class SequenceValidator
{   
    /* Pattern to recognize invalid letters in codon sequences. */
    private static Pattern pattern = Pattern.compile("^[ACGTUacgtu]+$");
    
    /** Correctness depends on rules: has to have start codon/has to have stop codon/etc. */
    public static synchronized int isValidCodonSequence(String sequence, GeneticCodeTable geneticCodeTable, GenomeFilters filters)
    {        
        assert filters != null;
        
        if (filters.isNoStartCodon() && !geneticCodeTable.isStartCodon(sequence.substring(0, 3).toUpperCase()))
            return 1;
        
        if (filters.isNoStopCodon())
            if (!geneticCodeTable.isStopCodon(sequence.substring(sequence.length()-3, sequence.length()).toUpperCase()))
                return 2;

                
        if (filters.isMultipleOfThree())
            if ((sequence.length() % 3) != 0) return 3;
                        
        if (filters.isNoMiddleStopCodon())
            for (int i=0; i<sequence.length()-5; i+=3)
                if (geneticCodeTable.isStopCodon(sequence.substring(i, i+3).toUpperCase())) return 4;

        boolean checkUnknownLetters = (Boolean) ApplicationSettings.getProperty("checkUnknownLetters", Boolean.class);
        if (checkUnknownLetters)
            if (!pattern.matcher(sequence).matches()) 
                return 5;
        
        return 0;
    }
    
    public static int isValidAASequence(String aaSequence, GeneticCodeTable geneticCodeTable)
    {
        assert aaSequence != null;
        assert geneticCodeTable != null;
        
        /* Check if all characters exist in the genetic code table. */
        for (char c : aaSequence.toCharArray())
            if (!geneticCodeTable.getAminoacidList().contains(Character.toString(c)))
                return 5; 
       
        return 0;
    }
    
    /* Receives a code from the method "isValidCodonSequence" and returns the equivalent error message. */
    public static String getValidationErrorMessage(int errorCode)
    {
        String message = "Unknown error";
        
        switch (errorCode)
        {
            case 0: message = "OK"; break;
            case 1: message = "No start codon found"; break;
            case 2: message = "No stop codon found"; break;
            case 3: message = "There is an incomplete codon (less than 3 nucleotides)"; break;
            case 4: message = "A middle stop codon was found"; break;
            case 5: message = "Unknown symbols in the sequence"; break;
        }
        
        return message;
    }
    
    /* Make sequence uppercase and replace Tinamin by Uracil */
    public static String makeCorrectionsToGene(String sequence)
    {
        sequence = sequence.toUpperCase();
        sequence = sequence.replace('T', 'U');

        return sequence;
    }
    
    /* Remove any stop codons that a sequence might have inside (in-frame). */
    public static String removeInnerStopCodons(String sequence, Genome genome)
    {
        GeneticCodeTable gct = genome.getGeneticCodeTable();
        StringBuilder sb = new StringBuilder();
        String s;
        for (int i = 0; i < sequence.length(); i += 3)
            if (!gct.isStopCodon(s = sequence.substring(i, i + 3)))
                sb.append(s);

        return sb.toString();
    }
}
