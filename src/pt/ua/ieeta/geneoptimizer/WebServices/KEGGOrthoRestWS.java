package pt.ua.ieeta.geneoptimizer.WebServices;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.SequenceValidator;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import pt.ua.ieeta.geneoptimizer.geneDB.HouseKeepingGenes;

/**
 *
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class KEGGOrthoRestWS extends Thread {

    private ResultKeeper resultKeeper;
    private String keggID;
    private boolean getOrthologs = true;
    private static String keggURL = "http://rest.kegg.jp";
    private final static int MAX_ORTHOLOGS = 10;
    private final static int MAX_BYTES_HTML_PAGE = 35000; // should be enough for around 50 orthologs
    /* Create semaphore to control access to the Kegg service. */
    private static Semaphore fluxControl = new Semaphore((Integer) ApplicationSettings.getInstance().getProperty("maxNumberOfKeggSimultaneousCalls", Integer.class), true);

    public KEGGOrthoRestWS(String keggID, ResultKeeper resultKeeper) {
        this.resultKeeper = resultKeeper;
        this.keggID = keggID;
    }

    @Override
    public void run() {
        if (getOrthologs) {
            obtainOrthologs();
        } else {
            obtainHouseKeepingOrthologs();
        }
    }

    private void obtainOrthologs() {
        ProgressPanel.ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Fetching from Kegg");
        processPanel.setProgress(0);
        processPanel.setStatus("Waiting.");
        WebService ws;
        ArrayList<WebServiceParameter> param = new ArrayList<WebServiceParameter>();

        Document doc;
        try {
            fluxControl.acquire();
            processPanel.setStatus("Connecting...");
            processPanel.setStatus("Retrieving.");

            /* Get gene entry from Kegg. This will give its kegg entry code. */
            System.out.println("KEGG-WS: Trying to find entry for kegg ID '" + keggID + "'");
            doc = Jsoup.connect("http://www.kegg.jp/ssdb-bin/ssdb_best?org_gene=" + keggID).timeout(0).maxBodySize(MAX_BYTES_HTML_PAGE).get();
            fluxControl.release();
        } catch (Exception ex) {
            fluxControl.release();
            Logger.getLogger(KEGGOrthoRestWS.class.getName()).log(Level.SEVERE, null, ex);
            processPanel.setFailed();
            processPanel.setStatus("Failed.");
            resultKeeper.setFailed();
            return;
        }

        System.out.println("KEGG-WS: Found " + keggID + ". Getting neighbours...");
        LinkedHashMap<String, Double> orthIdAndIdentityMap = new LinkedHashMap<String, Double>();
        Element element = doc.select("pre").last();
        Pattern p = Pattern.compile("\\d+\\.\\d+");
        if (element == null) {
            processPanel.setFailed();
            processPanel.setStatus("No result.");
            resultKeeper.setFailed();
            return;
        }

        if (element.children().isEmpty()) {
            processPanel.setFailed();
            processPanel.setStatus("No result.");
            resultKeeper.setFailed();
            return;
        }

        String[] aux = element.text().split("\n");
        
        for(int i = 2; i < MAX_ORTHOLOGS + 2; i++){            
            if (aux[i] != null){                
                Matcher m = p.matcher(aux[i].subSequence(aux[i].length()- 50, aux[i].length()));
                double identity = 0;
                if (m.find()) {
                    identity = Double.parseDouble(m.group());
                }
                assert identity != 0;                
                String[] code = aux[i].split(" ");                
                orthIdAndIdentityMap.put(code[0], identity);
            }
            
        }

        if (orthIdAndIdentityMap.isEmpty()) {
            processPanel.setFailed();
            processPanel.setStatus("No result.");
            resultKeeper.setFailed();
            return;
        }
        System.out.println("KEGG-WS: Found " + orthIdAndIdentityMap.size() + " neighbours. Retrieving them...");

        Genome orthologList = new Genome("ortholog List", new GeneticCodeTable());
        processPanel.setStatus("Obtaining genes.");
        int i = 0;
        try {
            fluxControl.acquire();
            for (Map.Entry entry : orthIdAndIdentityMap.entrySet()) {
                ws = new WebService("REST", keggURL + "/get/" + (String) entry.getKey(), param);
                String wsCall = (String) ws.call();
                Gene newGene = getGeneFromID(wsCall, orthologList, (String) entry.getKey());
                /* Add ortholog to list of orthologs. */
                if (newGene != null) {
                    orthologList.addGene(newGene);
                }
                processPanel.setProgress((i + 1) * 100 / orthIdAndIdentityMap.size());
                i++;
            }
            fluxControl.release();

        } catch (Exception ex) {
            fluxControl.release();
            Logger.getLogger(KEGGOrthoRestWS.class.getName()).log(Level.SEVERE, null, ex);
            processPanel.setFailed();
            processPanel.setStatus("Failed.");
            resultKeeper.setFailed();
        }

        processPanel.setStatus("Done.");
        processPanel.setComplete();

        /* Set dummy result to fire synchronoys waiting entities. */
        resultKeeper.setResult(orthologList);
    }

    /**
     * Obtain orthologs of housekeeping genes from a given species.
     */
    private void obtainHouseKeepingOrthologs() {

        ProgressPanel.ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Getting H.Keeping genes");
        processPanel.setProgress(0);

        /* Read base house keeping genes from file. */
        HouseKeepingGenes hkg = HouseKeepingGenes.getInstance();
        synchronized (HouseKeepingGenes.getInstance()) {
            if (!HouseKeepingGenes.getInstance().isFinished() && !hkg.isAlive()) {
                hkg.start();
            }
        }

        processPanel.setStatus("Waiting.");

        /* New list of housekeeping genes. */
        Genome houseKeepingOrthologs = new Genome("Highly Expressed Genes");
        processPanel.setStatus("Connecting...");
        processPanel.setStatus("Retrieving.");

        int maxAvailableEukaryotHKG = hkg.getEukaryotNumGenes(), maxAvailableProkaryotHKG = hkg.getProkaryotNumGenes();
        int minimumNeededGenes = (Integer) ApplicationSettings.getProperty("CAIOrthologsThreshold", Integer.class);
        int prokaryotCount = 0, eukaryotCount = 0;
        Gene newGene1 = null, newGene2 = null;
        List<Gene> prokaryots = new ArrayList<Gene>(), eukaryots = new ArrayList<Gene>();

        /* Test for eukaryots. */
        for (int i = 0; i < MAX_ORTHOLOGS; i++) {
            /* Reset new genes. */
            newGene1 = newGene2 = null;

            try {
                newGene1 = getKeggGeneByKO(hkg.getEukaryotKeggCode(i), houseKeepingOrthologs, processPanel);
                newGene2 = getKeggGeneByKO(hkg.getProkaryotKeggCode(i), houseKeepingOrthologs, processPanel);
            } catch (Exception ex) { //TODO: exceptions
                Logger.getLogger(KEGGOrthoRestWS.class.getName()).log(Level.SEVERE, null, ex);
            }

            /* If no result, go on to next gene. */
            if ((newGene1 == null) && (newGene2 == null)) {
                continue;
            }

            /* Add orthologs to list of orthologs. */
            if (newGene1 != null) {
                eukaryotCount++;
                eukaryots.add(newGene1);
            }
            if (newGene2 != null) {
                prokaryotCount++;
                eukaryots.add(newGene2);
            }

            processPanel.setProgress((eukaryots.size() + eukaryots.size()) * 50 / minimumNeededGenes);
        }

        int maxAvailableHKG[] = new int[2];
        if (prokaryotCount > eukaryotCount) {
            System.out.println("Got more results from Prokaryots");
            maxAvailableHKG[0] = maxAvailableProkaryotHKG;
            maxAvailableHKG[1] = maxAvailableEukaryotHKG;
            houseKeepingOrthologs.setGenes(prokaryots);
        } else {
            System.out.println("Got more results from Eukaryots");
            maxAvailableHKG[0] = maxAvailableEukaryotHKG;
            maxAvailableHKG[1] = maxAvailableProkaryotHKG;
            houseKeepingOrthologs.setGenes(eukaryots);
        }

        /* Repeat the process for the remaining genes, but starting with the group with the most already retrieved genes. */
        for (int j = 0; j < 2; j++) {
            for (int i = MAX_ORTHOLOGS; i < maxAvailableHKG[j]; i++) {
                newGene1 = null;
                try {
                    if ((prokaryotCount > eukaryotCount) == (j == 1)) {
                        newGene1 = getKeggGeneByKO(hkg.getEukaryotKeggCode(i), houseKeepingOrthologs, processPanel);
                    } else {
                        newGene1 = getKeggGeneByKO(hkg.getProkaryotKeggCode(i), houseKeepingOrthologs, processPanel);
                    }
                } catch (Exception ex) { //TODO: exceptions
                    Logger.getLogger(KEGGOrthoRestWS.class.getName()).log(Level.SEVERE, null, ex);
                }

                /* If no result, go on to next gene. */
                if (newGene1 == null) {
                    continue;
                }

                /* Add ortholog to list of orthologs. */
                houseKeepingOrthologs.addGene(newGene1);

                processPanel.setProgress((houseKeepingOrthologs.getGenes().size()) * 100 / minimumNeededGenes);

                /* If already has the necessary number of genes, stop fetching. */
                if (houseKeepingOrthologs.getGenes().size() >= minimumNeededGenes) {
                    break;
                }
            }

            /* If already has the necessary number of genes, stop fetching. */
            if (houseKeepingOrthologs.getGenes().size() >= minimumNeededGenes) {
                break;
            }

            /* If finished one class of species, add the first ten of the other. (already previously obtained. */
            if (prokaryotCount > eukaryotCount) {
                houseKeepingOrthologs.getGenes().addAll(eukaryots);
            } else {
                houseKeepingOrthologs.getGenes().addAll(prokaryots);
            }
        }

        if (houseKeepingOrthologs.getGenes().size() < minimumNeededGenes) {
            resultKeeper.setFailed();
            processPanel.setFailed();
        } else {
            resultKeeper.setResult(houseKeepingOrthologs);
            processPanel.setComplete();
            processPanel.setStatus("Done.");
        }
    }

    public Gene getKeggGeneByKO(String code, Genome houseKeepingOrthologs, ProgressPanel.ProcessPanel processPanel) throws RemoteException, InterruptedException, Exception {
        Document doc;
        try {
            fluxControl.acquire();
            System.out.println("Getting genes for ortholog " + code + " with keggID " + keggID);
            doc = Jsoup.connect(keggURL + "/get/" + code).get();
            fluxControl.release();
        } catch (Exception ex) {
            fluxControl.release();
            System.out.println("An exception occured while trying to retrieve gene " + code + " from KEGG web-services.");
            //Logger.getLogger(KEGGOrthoRestWS.class.getName()).log(Level.SEVERE, null, ex); 
            processPanel.setFailed();
            processPanel.setStatus("Failed.");
            resultKeeper.setFailed();
            return null;
        }
        Element element = doc.body();
        Pattern p = Pattern.compile("\\b" + keggID.toUpperCase() + ": \\b+[a-zA-Z0-9]+[_]*[a-zA-Z0-9]*");
        Matcher m = p.matcher(element.text());
        String newKeggID = null;
        while (m.find()) {
            newKeggID = m.group();
            break;
        }
        if (newKeggID == null) {
            System.out.println("Not found " + code + "! Came empty");
            return null;
        } else {
            System.out.println("Found Gene KeggID: " + newKeggID);
        }

        newKeggID = newKeggID.toLowerCase().trim().replaceAll(" ", "");

        String wsCall;
        ArrayList<WebServiceParameter> param = new ArrayList<WebServiceParameter>();
        WebService ws = new WebService("REST", keggURL + "/get/" + newKeggID, param);

        fluxControl.acquire();
        wsCall = (String) ws.call();
        fluxControl.release();

        /* Obtain ortholog gene. */
        Gene ortholog = getGeneFromID(wsCall, houseKeepingOrthologs, newKeggID);

        if (ortholog != null) {
            System.out.println("Found: " + ortholog.getName());
        } else {
            System.out.println("Not found (bget came empty) !");
        }

        return ortholog;
    }

    public void setObtainHousekeepingGenes(boolean b) {
        getOrthologs = !b;
    }

    /**
     * Obtain gene from Kegg. A Kegg ID is given, and the complete gene is
     * returned (AA sequence and codon sequence).
     */
    private Gene getGeneFromID(String wsCall, Genome genome, String id) throws RemoteException, InterruptedException, Exception {
        //assert genome != null;
        assert id != null && !id.isEmpty();


        String codonSequence;
        String aaSequence;
        if (wsCall == null) {
            return null;
        }
        if (wsCall.isEmpty()) {
            return null;
        }

        codonSequence = getNSequence(wsCall);
        aaSequence = getAASequence(wsCall);

        if (codonSequence == null) {
            return null;
        }
        if (codonSequence.isEmpty()) {
            return null;
        }
        if (aaSequence == null) {
            return null;
        }
        if (aaSequence.isEmpty()) {
            return null;
        }

        codonSequence = SequenceValidator.makeCorrectionsToGene(codonSequence);

        String geneName = "";
        String geneID = getFieldContent(wsCall, "ENTRY")[1];
        String genomeName = "";
        String[] aux = getFieldContent(wsCall, "DEFINITION");
        if (aux != null) {
            for (int i = 1; i < aux.length; i++) {
                if (i == (aux.length - 1)) {
                    geneName += aux[i];
                } else {
                    geneName += aux[i] + " ";
                }
            }
        }

        aux = getFieldContent(wsCall, "ORGANISM");
        if (aux != null) {
            for (int i = 2; i < aux.length; i++) {
                if (i == (aux.length - 1)) {
                    genomeName += aux[i];
                } else {
                    genomeName += aux[i] + " ";
                }
            }
        }

        if (genomeName.isEmpty()) {
            genomeName = genome.getName();
        }
        /* Create new ortholog gene. */
        Gene newGene = new Gene(geneName, genome);
        newGene.setOrthologInfo(0, 0, geneID, genomeName);
        newGene.createStructure(codonSequence, BioStructure.Type.mRNAPrimaryStructure);
        newGene.createStructure(aaSequence + "*", BioStructure.Type.proteinPrimaryStructure);
        return newGene;
    }

    private String getAASequence(String webServiceResult) {
        assert webServiceResult != null && !webServiceResult.isEmpty();
        String[] lines = webServiceResult.split("\n");
        int aaSequenceSize = 0;
        StringBuilder aaSequence = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("AASEQ")) {
                aaSequenceSize = Integer.parseInt(lines[i].replaceAll("\\D+", ""));
                boolean hasMoreLines = true;

                while (hasMoreLines) {
                    i++;
                    aaSequence.append(lines[i].trim());
                    if (aaSequence.length() == aaSequenceSize) {
                        hasMoreLines = false;
                    }
                }
                if (!hasMoreLines) {
                    break;
                }
            }
        }
        if (aaSequenceSize == 0) {
            return null;
        }
        return aaSequence.toString();
    }

    private String getNSequence(String webServiceResult) {
        assert webServiceResult != null && !webServiceResult.isEmpty();
        String[] lines = webServiceResult.split("\n");
        int nucleotideSequenceSize = 0;
        StringBuilder nucleotideSequence = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("NTSEQ")) {
                nucleotideSequenceSize = Integer.parseInt(lines[i].replaceAll("\\D+", ""));
                boolean hasMoreLines = true;

                while (hasMoreLines) {
                    i++;
                    nucleotideSequence.append(lines[i].trim());
                    if (nucleotideSequence.length() == nucleotideSequenceSize) {
                        hasMoreLines = false;
                    }
                }
                if (!hasMoreLines) {
                    break;
                }
            }
        }
        if (nucleotideSequenceSize == 0) {
            return null;
        }
        return nucleotideSequence.toString();
    }

    private String[] getFieldContent(String webServiceResult, String field) {
        assert webServiceResult != null;
        assert field != null && !field.isEmpty();

        String[] lines = webServiceResult.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith(field)) {
                return lines[i].split("[\\s\"]+");
            }
        }
        return null;
    }

    public static void main(String args[]) {
        ResultKeeper k = new ResultKeeper();
        KEGGOrthoRestWS ra = new KEGGOrthoRestWS("pfa:PF10_0071", k);
        ra.start();
    }
}
