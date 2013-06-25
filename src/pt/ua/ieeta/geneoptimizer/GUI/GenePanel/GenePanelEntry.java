package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.Color;
import java.util.List;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure.Type;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 * This class wraps information to be passed to the InteriorContentPanel to 
 * build the sequence panels.
 * 
 * @author Paulo Gaspar
 */
public class GenePanelEntry 
{
    /* The type of this entry. */
    public enum EntryType
    {
        MAIN_CODON_SEQ, // the main codon sequence panel
        MAIN_AA_SEQ, // the main amino acid sequence panel
        MAIN_SEC_STRUCT, // the main protein secondary structure
        SECOND_AA_SEQ,
        ORTHOLOG, // ortholog entry
    }
    
    private String name; // entry label
    private Gene gene; // entry gene
    private List<Color> colorScheme; // color scheme to paint the sequence panel
    private BioStructure.Type sequenceType;
    private EntryType entryType;

    /** Create a new gene panel entry with a specific name, sequence, colour scheme, sequence type and entry type.
     * @param name The name of the sequence panel. Will be shown in a label.
     * @param gene The gene structure for this sequence panel. This will supply the information to be shown (likely the sequence).
     * @param colorScheme The colour scheme for this sequence panel. The size must be equal to the sequence size.
     * @param The type of sequence panel. Can be a codon sequence, AA sequence, ortholog, etc.
     */
    public GenePanelEntry(String name, Gene gene, List<Color> colorScheme, Type sequenceType, EntryType entryType)
    {
        assert name != null;
        assert gene != null;
        assert sequenceType != null;
        assert entryType != null;
        
        this.name = name;
        this.gene = gene;
        this.colorScheme = colorScheme;
        this.sequenceType = sequenceType;
        this.entryType = entryType;
    }

    public List<Color> getColorScheme()
    {
        return colorScheme;
    }

    public void setColorScheme(List<Color> colorScheme)
    {
        this.colorScheme = colorScheme;
    }

    public EntryType getEntryType()
    {
        return entryType;
    }

    public void setEntryType(EntryType entryType)
    {
        this.entryType = entryType;
    }

    public Gene getGene()
    {
        return gene;
    }

    public void setGene(Gene gene)
    {
        this.gene = gene;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Type getSequenceType()
    {
        return sequenceType;
    }

    public void setSequenceType(Type sequenceType)
    {
        this.sequenceType = sequenceType;
    }
}
