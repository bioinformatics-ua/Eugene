package pt.ua.ieeta.geneoptimizer.geneDB;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 *
 * @author Paulo
 */
public class GeneticCodeTable
{    
    private Map<String, String> codeTable;
    private Map<String, String> startCodons;
    private Map<String, Vector<String>> synonymousList;
    private int id;


    public GeneticCodeTable(String name, int id, String ncbieaa, String sncbieaa, String base1, String base2, String base3)
    {
        assert name != null;
        assert ncbieaa != null;
        assert sncbieaa != null;
        assert base1 != null;
        assert base2 != null;
        assert base3 != null;
        this.id = id; 

        /* Create table. */
        codeTable = new HashMap<String, String>();
        startCodons = new HashMap<String, String>();

        /* Fill table. */
        for(int i=0; i<ncbieaa.length(); i++)
        {
            String codon = base1.substring(i, i+1) + base2.substring(i, i+1) + base3.substring(i, i+1);
            // ncbieaa.substring(i, i+1) -> aminoacid            
            codeTable.put(codon.replaceAll("T", "U"), ncbieaa.substring(i, i+1));
            startCodons.put(codon.replaceAll("T", "U"), sncbieaa.substring(i, i+1));
        }

        synonymousList = new HashMap<String, Vector<String>>();
        buildSynonymousList();
    }
    
    public GeneticCodeTable()
    {}

    /** Converts a codon to the corresponding amino acid decoded from it according to this genetic code table. */
    public synchronized String getAminoAcidFromCodon(String codon)
    {
        assert codon != null;
        assert codeTable != null;

        codon = codon.replace('T', 'U');

        /* If codon not in table, return 'X', meaning ANY aminoacid. */
        if (codeTable.get(codon) == null)
            return "X";

        return codeTable.get(codon);
    }

    public synchronized boolean isStopCodon(String codon)
    {
        assert codon != null;
        assert codeTable != null;

        codon = codon.replace('T', 'U');

        /* If codon not in table, return false. */
        if (codeTable.get(codon) == null)
            return false;

        return codeTable.get(codon).equals("*");
    }

    public synchronized boolean isStartCodon(String codon)
    {
        assert codon != null;
        assert codeTable != null;

        codon = codon.replace('T', 'U');

        /* If codon not in table, return false. */
        if (codeTable.get(codon) == null)
            return false;

        return startCodons.get(codon).equals("M");
    }

    private void buildSynonymousList()
    {
        assert synonymousList != null;
        assert codeTable != null;

        /* For each codon in the code table, add the codon to it's aminoacid
           list of synonymous. */
        for(String codon : codeTable.keySet())
            if (synonymousList.containsKey(codeTable.get(codon)))
                synonymousList.get(codeTable.get(codon)).add(codon);
            else
            {
                Vector<String> newList = new Vector<String>();
                newList.add(codon);
                synonymousList.put(codeTable.get(codon), newList);
            }
    }

    /** Returns synonymous list to the given codon. */
    public synchronized Vector<String> getSynonymousFromCodon(String codon)
    {
        assert codon != null;
        assert synonymousList != null;
        
        return synonymousList.get(getAminoAcidFromCodon(codon));
    }
    
    public synchronized Vector<String> getSynonymousFromAA(String aminoacid)
    {
        assert aminoacid != null;

        return synonymousList.get(aminoacid);
    }

    /** Returns the list of amino acids present in this genetic code table. */
    public synchronized Vector<String> getAminoacidList()
    {
//        Vector<String> newList = new Vector<String>();
//        for(String aminoacid : synonymousList.keySet())
//            newList.add(aminoacid);
        
        return new Vector<String>(synonymousList.keySet());
        //return newList;
    }

    public synchronized int getNumberOfSynonymous(String codon)
    {
        assert codon != null;
        
        return getSynonymousFromCodon(codon).size();
    }

    public synchronized int getId() {
        return id;
    }        
}
