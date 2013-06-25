package pt.ua.ieeta.geneoptimizer.geneDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Paulo
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class GeneticCodeTable {

//    private Map<String, String> codeTable;
//    private Map<String, String> startCodons;
//    private Map<String, List<String>> synonymousList;
    private Map<String, Integer> numberedCodonTable;
    
    /* Key -> AA ids ; Values -> codon ids */
    private Map<Integer, List<Integer>> synonymousListIDs;
    private Map<String, String> codonAATable;
    private Map<String, Integer> aaTable;
    private int id;

    public GeneticCodeTable(String name, int id, String ncbieaa, String sncbieaa, String base1, String base2, String base3) {
        assert name != null;
        assert ncbieaa != null;
        assert sncbieaa != null;
        assert base1 != null;
        assert base2 != null;
        assert base3 != null;
        this.id = id;

        /* Create table. */
//        codeTable = new HashMap<String, String>();
//        startCodons = new HashMap<String, String>();

        /* EXPERIMENTAL */
        numberedCodonTable = new HashMap<String, Integer>(64);
        codonAATable = new HashMap<String, String>(64);
        aaTable = new HashMap<String, Integer>(21);

        int aaIndex = 0;
        /* Fill table. */
        for (int i = 0; i < ncbieaa.length(); i++) {
            String codon = base1.substring(i, i + 1) + base2.substring(i, i + 1) + base3.substring(i, i + 1);
            // ncbieaa.substring(i, i+1) -> aminoacid            
//            codeTable.put(codon.replaceAll("T", "U"), ncbieaa.substring(i, i + 1));
//            startCodons.put(codon.replaceAll("T", "U"), sncbieaa.substring(i, i + 1));

            /* EXPERIMENTAL */
            numberedCodonTable.put(codon.replaceAll("T", "U"), i);
            codonAATable.put(codon.replaceAll("T", "U"), ncbieaa.substring(i, i + 1));
            if (!aaTable.containsKey(ncbieaa.substring(i, i + 1))) {
                aaTable.put(ncbieaa.substring(i, i + 1), aaIndex);
                aaIndex++;
            }
        }
        
        synonymousListIDs = new HashMap<Integer, List<Integer>>();
        buildIDSSynonymousList();
    }

    public GeneticCodeTable() {
    }

    /**
     * Gets the aminoacid id for the given codon id
     *
     * @param codonID
     * @return
     */
    public synchronized int getAminoAcidIDFromCodonID(int codonID) {
        assert numberedCodonTable != null;

        if (!numberedCodonTable.containsValue(codonID)) {
            return -1;
        }

        String codon = getCodonFromID(codonID);

        if (codon == null) {
            return -1;
        }

        String aa = codonAATable.get(codon);

        /* AA not int aa Table */
        if (aaTable.get(aa) == null) {
            return -1;
        }
        return aaTable.get(aa);
    }

    /**
     * Gets the integer value of the decoded aminoacid for the given codon
     * accordint to this genetic code table
     *
     * @param codon
     * @return Amino Acid integer code or -1 if amino acid not present
     */
    public synchronized int getAminoAcidIDFromCodon(String codon) {
        assert codon != null;
        assert codon.length() == 3;

        codon = codon.replace('T', 'U');

        /* If codon not in table, return '-1', meaning ANY aminoacid. */
        if (codonAATable.get(codon) == null) {
            return -1;
        }
        String aa = codonAATable.get(codon);

        /* AA not int aa Table */
        if (aaTable.get(aa) == null) {
            return -1;
        }
        return aaTable.get(aa);
    }

    /**
     * Gets the AminoAcid string representation for the given codon id
     *
     * @param codonID
     * @return
     */
    public synchronized String getAminoAcidFromCodonID(int codonID) {

        if (!numberedCodonTable.containsValue(codonID)) {
            return null;
        }

        String codon = getCodonFromID(codonID);
        if (codon == null) {
            return null;
        }

        return codonAATable.get(codon);
    }

    /**
     * Gets the decoded AA sequence for the given codon according to this
     * genetic code table.
     *
     * @param codon
     * @return
     */
    public synchronized String getAminoAcidFromCodon(String codon) {
        assert codon != null;
        assert codon.length() == 3;

        codon = codon.replace('T', 'U');

        /* If codon not in table, return 'X', meaning ANY aminoacid. */
        if (codonAATable.get(codon) == null) {
            return "X";
        }

        return codonAATable.get(codon);
    }

    /**
     * Gets the codon integer representation
     *
     * @param codon
     * @return
     */
    public synchronized int getCodonID(String codon) {
        assert numberedCodonTable != null;
        assert codon != null && codon.length() == 3;

        codon = codon.replace('T', 'U');

        /* If codon not in table, return 'X', meaning ANY aminoacid. */
        if (numberedCodonTable.get(codon) == null) {
            return -1;
        }

        return numberedCodonTable.get(codon);
    }

    /**
     * Gets the aminoacid integer representation
     *
     * @param AA
     * @return
     */
    public synchronized int getAminoAcidID(String AA) {
        assert AA != null;
        assert AA.length() == 1;

        if (aaTable.get(AA) == null) {
            return -1;
        }

        return aaTable.get(AA);
    }

    /**
     * Gets the codon id string representation
     *
     * @param codonID
     * @return
     */
    public synchronized String getCodonFromID(int codonID) {

        if (!numberedCodonTable.containsValue(codonID)) {
            return null;
        }

        for (Map.Entry<String, Integer> entry : numberedCodonTable.entrySet()) {
            if (entry.getValue() == codonID) {
                return entry.getKey();
            }
        }

        return null;
    }

    /**
     * Gets the aminoacid id string representation
     *
     * @param aaID
     * @return
     */
    public synchronized String getAminoAcidFromID(int aaID) {
        if (!aaTable.containsValue(aaID)) {
            return null;
        }

        for (Map.Entry<String, Integer> entry : aaTable.entrySet()) {
            if (entry.getValue() == aaID) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Check if the given codon id is a stop codon
     *
     * @param codonID
     * @return
     */
    public synchronized boolean isStopCodon(int codonID) {
        /* If codon not in table, return false. */
        if (!numberedCodonTable.containsValue((int) codonID)) {
            return false;
        }

        int aaNumber = getAminoAcidIDFromCodonID(codonID);

        if (aaNumber == -1) {
            return false;
        }

        return getAminoAcidFromID(aaNumber).equals("*");
    }

    /**
     * Check if the given codon id is a start codon
     *
     * @param codonID
     * @return
     */
    public synchronized boolean isStartCodon(int codonID) {
        /* If codon not in table, return false. */
        if (!numberedCodonTable.containsValue((int) codonID)) {
            return false;
        }
        
        int aaNumber = getAminoAcidIDFromCodonID(codonID);

        if (aaNumber == -1) {
            return false;
        }
        
        return getAminoAcidFromID(aaNumber).equals("M");
    }


    /**
     * Check if the given code id represents a stop codon for the used genetic
     * code table
     *
     * @param codonIntCode
     * @return
     */
    public synchronized boolean isStopCodon(String codon) {
        assert codon != null;
//        assert codeTable != null;

        codon = codon.replace('T', 'U');
        
        int codonID = getCodonID(codon);
        
        if (codonID == -1){
            return false;            
        }
        
        return isStopCodon(codonID);

//        /* If codon not in table, return false. */
//        if (codeTable.get(codon) == null) {
//            return false;
//        }
//
//        return codeTable.get(codon).equals("*");
    }

    /**
     * Check if the given code id represents a start codon for the used genetic code table
     * @param codon
     * @return 
     */
    public synchronized boolean isStartCodon(String codon) {
        assert codon != null;
//        assert codeTable != null;

        codon = codon.replace('T', 'U');
        
        int codonID = getCodonID(codon);
        
        if (codonID == -1){
            return false;
        }
        
        return isStartCodon(codonID);

//        /* If codon not in table, return false. */
//        if (codeTable.get(codon) == null) {
//            return false;
//        }
//
//        return startCodons.get(codon).equals("M");
    }
    
    
    /**
     * Builds a list of all codons synonynous of each amino acid
     */
    private void buildIDSSynonymousList(){
        assert synonymousListIDs != null;
        assert codonAATable != null;
        assert numberedCodonTable != null;
        
        int aaID;
        for(int codonID : numberedCodonTable.values()){
            aaID = getAminoAcidIDFromCodonID(codonID);            
            if (synonymousListIDs.containsKey(aaID)){
                synonymousListIDs.get(aaID).add(codonID);
            }else{
                List<Integer> newList = new ArrayList<Integer>();
                newList.add(codonID);
                synonymousListIDs.put(aaID, newList);
            }
        }
                        
    }
    
//    private void buildSynonymousList() {
//        assert synonymousList != null;
//        assert codeTable != null;
//
//        /* For each codon in the code table, add the codon to it's aminoacid
//         list of synonymous. */
//        for (String codon : codeTable.keySet()) {
//            if (synonymousList.containsKey(codeTable.get(codon))) {
//                synonymousList.get(codeTable.get(codon)).add(codon);
//            } else {
//                List<String> newList = new ArrayList<String>();
//                newList.add(codon);
//                synonymousList.put(codeTable.get(codon), newList);
//            }
//        }
//    }

    public synchronized List<Integer> getSynonymousIdsFromCodonID(int codonID){
        assert synonymousListIDs != null;
        
        return synonymousListIDs.get(getAminoAcidIDFromCodonID(codonID));
    }
    
    public synchronized List<String> getSynonymousFromCodonID(int codonID){
        assert synonymousListIDs != null;
        
        List<Integer> synIds = getSynonymousIdsFromCodonID(codonID);
        
        List<String> synCodonsStrings = new ArrayList<String>(synIds.size());
        
        for(int i : synIds){
            synCodonsStrings.add(getCodonFromID(i));
        }
        
        return synCodonsStrings;
    }
    
    public synchronized List<Integer> getSynonymousIdsFromCodon(String codon){
        assert codon != null;
        assert codon.length() == 3;        
                        
        return getSynonymousIdsFromCodonID(getCodonID(codon));
    }
    
    public synchronized List<String> getSynonymousFromCodon(String codon){
        assert codon != null;
        assert codon.length() == 3;        
        
        return getSynonymousFromCodonID(getCodonID(codon));
    }
    
    
    public synchronized  List<Integer> getSynonymousIDsFromAAID(int aaID){
        assert synonymousListIDs != null;
        return synonymousListIDs.get(aaID);
        
    }
    
    public synchronized  List<Integer> getSynonymousIDsFromAA(String aminoAcid){
        assert aminoAcid != null;
        assert aminoAcid.length() == 1;
        assert synonymousListIDs != null;
        
        return synonymousListIDs.get(getAminoAcidID(aminoAcid));
        
    }
    
    public synchronized List<String> getSynonymousFromAAId(int aaID){        
        assert synonymousListIDs != null;
        
        List<Integer> synList = getSynonymousIDsFromAAID(aaID);
        List<String> newList = new ArrayList<String>(synList.size());
        for(int i : synList){
            newList.add(getCodonFromID(i));
        }
        return newList;
    }
    
    public synchronized List<String> getSynonymousFromAA(String aaID){
        assert aaID != null;
        assert aaID.length() == 1;
        assert synonymousListIDs != null;
        
        return getSynonymousFromAAId(getAminoAcidID(aaID));
    }
    
//    /**
//     * Returns synonymous list to the given codon.
//     */
//    public synchronized List<String> getSynonymousFromCodon(String codon) {
//        assert codon != null;
//        assert synonymousList != null;
//
//        return synonymousList.get(getAminoAcidFromCodon(codon));
//    }
//
//    public synchronized List<String> getSynonymousFromAA(String aminoacid) {
//        assert aminoacid != null;
//
//        return synonymousList.get(aminoacid);
//    }

    /**
     * Returns the list of amino acids present in this genetic code table.
     */
    public synchronized List<String> getAminoacidList() {
//        Vector<String> newList = new Vector<String>();
//        for(String aminoacid : synonymousList.keySet())
//            newList.add(aminoacid);
        
        return new ArrayList<String>(aaTable.keySet());
        //return newList;
    }

    public synchronized int getNumberOfSynonymous(String codon) {
        assert codon != null;

        return getSynonymousFromCodon(codon).size();
    }
    
    public synchronized int getNumberOfSynonymousFromCodonID(int codonID){
        return getNumberOfSynonymous(getCodonFromID(codonID));
    }

    public synchronized int getId() {
        return id;
    }
}
