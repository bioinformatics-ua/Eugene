/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ieeta.geneoptimizer.WebServices;

import java.rmi.RemoteException;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import keggapi.Definition;
import keggapi.KEGGLocator;
import keggapi.KEGGPortType;
import keggapi.SSDBRelation;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.SequenceValidator;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure.Type;
import pt.ua.ieeta.geneoptimizer.geneDB.*;

/**
 *
 * @author Paulo Gaspar
 */
public class KEGGorthoWS extends Thread
{
    private ResultKeeper resultKeeper;
    private String keggID;
    private boolean getOrthologs = true;

    /* Create semaphore to control access to the Kegg service. */
    private static Semaphore fluxControl = new Semaphore((Integer) ApplicationSettings.getInstance().getProperty("maxNumberOfKeggSimultaneousCalls", Integer.class), true);

    public KEGGorthoWS(String keggID, ResultKeeper resultKeeper)
    {
        this.resultKeeper = resultKeeper;
        this.keggID = keggID;
   }

    @Override
    public void run()
    {
        if (getOrthologs)
            obtainOrthologs();
        else
            obtainHouseKeepingOrthologs();
    }
    
    private void obtainOrthologs()
    {
        ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Fetching from Kegg");
        processPanel.setProgress(0);

        KEGGLocator locator  = new KEGGLocator();
        KEGGPortType serv = null;
        SSDBRelation[] results  = null;

        processPanel.setStatus("Waiting.");
        try
        { 
            fluxControl.acquire();
                processPanel.setStatus("Connecting...");
                serv = locator.getKEGGPort();
                processPanel.setStatus("Retrieving.");

                
                /* Get gene entry from Kegg. This will give its kegg entry code. */
                System.out.println("KEGG-WS: Trying to find entry ID for kegg ID " + keggID);
                String geneDefinition = serv.btit(keggID).trim();
                if ((geneDefinition == null) || (geneDefinition.isEmpty()) || (geneDefinition.length() < 3))
                {
                    fluxControl.release();
                    processPanel.setFailed();
                    processPanel.setStatus("Not found.");
                    resultKeeper.setFailed();
                }
                
                String parts[] = geneDefinition.split("\\s");
                String keggEntryID = parts[0];
                System.out.println("KEGG-WS: Found " + keggEntryID + ". Getting neighbours...");
                
                results = serv.get_best_neighbors_by_gene(keggEntryID, 1, 10);
            fluxControl.release();            
        }
        catch (Exception ex) 
        { 
            fluxControl.release();
            Logger.getLogger(KEGGorthoWS.class.getName()).log(Level.SEVERE, null, ex); 
            processPanel.setFailed();
            processPanel.setStatus("Failed.");
            resultKeeper.setFailed();
            
            return;
        } //TODO: exceptions..
        
        /* No results obtained. */
        if (results.length == 0)
        {
            processPanel.setFailed();
            processPanel.setStatus("No result.");
            resultKeeper.setFailed();
            return;
        }
        
        System.out.println("KEGG-WS: Found " + results.length + " neighbours. Retrieving them...");

        //TODO: devia ser criada uma estrutura "OrthologList" especialmente para lidar com isto, em vez de usar um "Genome"
        Genome orthologList = new Genome("ortholog List", new GeneticCodeTable());
        
        try
        {
            processPanel.setStatus("Obtaining genes.");
            for (int i = 0; i < results.length; i++)
            {                   
                    Gene newGene = getGeneFromID(serv, orthologList, results[i].getGenes_id2());
                
                    /* Add ortholog to list of orthologs. */
                    if (newGene != null)
                        orthologList.addGene(newGene);
                    
                    processPanel.setProgress((i+1)*100/results.length);
            }
        }
        catch (Exception ex) 
        {
            Logger.getLogger(KEGGorthoWS.class.getName()).log(Level.SEVERE, null, ex); 
            processPanel.setFailed();
            processPanel.setStatus("Failed.");
            resultKeeper.setFailed();
            
            return;
        } //TODO: exceptions..

        processPanel.setStatus("Done.");
        processPanel.setComplete();
        
        /* Set dummy result to fire synchronoys waiting entities. */
        resultKeeper.setResult(orthologList);
    }
    
    /** Obtain gene from Kegg. A Kegg ID is given, and the complete gene is returned (AA sequence and codon sequence). */
    private Gene getGeneFromID(KEGGPortType serv, Genome genome, String ID) throws RemoteException, InterruptedException, Exception
    {
        assert serv != null;
        assert genome != null;
        assert ID != null;
        assert !ID.isEmpty();
        
        String sequence_result;
        try
        {
            fluxControl.acquire();
                sequence_result = serv.bget("-f a " + ID);
            fluxControl.release();
        }
        catch(Exception ex)
        {
            fluxControl.release();
            throw new Exception("Exception while using bget web service (Kegg). ");
        }
        
        if (sequence_result == null) return null;
        if (sequence_result.isEmpty()) return null;
        
        String []sequences = sequence_result.substring(sequence_result.indexOf("\n")+1).split(">");
        String aaSequence = sequences[0].replaceAll("\\s+", "");
        String codonSequence = SequenceValidator.makeCorrectionsToGene(sequences[1].substring(sequence_result.indexOf("\n")).replaceAll("\\s+", ""));

        /* Parse information. */
        String geneID =  sequences[1].substring(sequence_result.indexOf(":"), sequence_result.indexOf(" "));
        String genomeID = sequences[1].substring(0, sequence_result.indexOf(":")-1);
        String geneName = sequences[1].substring(sequence_result.indexOf(" "), sequence_result.indexOf("\n")).trim();

        String genomeName;
        try 
        {
            fluxControl.acquire();
                genomeName = serv.binfo(genomeID);
            fluxControl.release();
        }
        catch(Exception ex)
        {
            fluxControl.release();
            throw new Exception("Exception while using binfo web service (Kegg). ");
        }
        
        /* Empty name? Strange behaviour. */
        if (genomeName == null) genomeName = "";
        if (!genomeName.isEmpty())
        {
            genomeName = genomeName.replaceAll("\\s+", " ");
            int startIndex = Math.max(0, genomeName.indexOf(" ")+1);
            int endIndex = Math.max(startIndex, genomeName.indexOf(" ", genomeName.indexOf(" ", startIndex)+1 ) );
            genomeName = genomeName.substring(startIndex, endIndex);
            //genomeName = genomeName.substring(0, genomeName.indexOf(" ",genomeName.indexOf(" ")+1));
        }
        else
            genomeName = genome.getName().isEmpty()? genomeID : genome.getName();

        /* Create new ortholog gene. */
        Gene newGene = new Gene(geneName, genome);
        newGene.setOrthologInfo(0, 0, geneID, genomeName);
        newGene.createStructure(new ByteString(codonSequence), Type.mRNAPrimaryStructure);
        newGene.createStructure(new ByteString(aaSequence+"*"), Type.proteinPrimaryStructure);
        
        return newGene;
    }
    
    /** Obtain orthologs of housekeeping genes from a given species. */
    private void obtainHouseKeepingOrthologs() 
    {
        ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Getting H.Keeping genes");
        processPanel.setProgress(0);
        
        /* Read base house keeping genes from file. */
        HouseKeepingGenes hkg = HouseKeepingGenes.getInstance();
        synchronized (HouseKeepingGenes.getInstance())
        {
            if (!HouseKeepingGenes.getInstance().isFinished() && !hkg.isAlive())
                hkg.start();
        }
        
        KEGGLocator locator  = new KEGGLocator();
        KEGGPortType serv = null;
        Definition[] results = null;

        processPanel.setStatus("Waiting.");
        
        /* New list of housekeeping genes. */
        Genome houseKeepingOrthologs = new Genome("Highly Expressed Genes");
        
        /* Create Kegg server connection. */
        try {
            fluxControl.acquire();
                    processPanel.setStatus("Connecting...");
                    serv = locator.getKEGGPort();
                    processPanel.setStatus("Retrieving.");
            fluxControl.release();
        } 
        catch (Exception ex) 
        { //TODO: excepçoes
            fluxControl.release();
            Logger.getLogger(KEGGorthoWS.class.getName()).log(Level.SEVERE, null, ex);
            processPanel.setFailed();
            processPanel.setStatus("Failed.");
            resultKeeper.setFailed();

            return;
        }
        
        
        int maxAvailableEukaryotHKG = hkg.getEukaryotNumGenes(), maxAvailableProkaryotHKG = hkg.getProkaryotNumGenes();
        int minimumNeededGenes = (Integer) ApplicationSettings.getProperty("CAIOrthologsThreshold", Integer.class);
        int prokaryotCount = 0, eukaryotCount = 0;
        Gene newGene1 = null, newGene2 = null;
        Vector<Gene> prokaryots = new Vector<Gene>(), eukaryots = new Vector<Gene>();
        
        /* Test for eukaryots. */
        for (int i=0; i<10; i++)
        {
            /* Reset new genes. */
            newGene1 = newGene2 = null;
            
            try {
                newGene1 = getKeggGeneByKO(hkg.getEukaryotKeggCode(i), houseKeepingOrthologs, serv, processPanel);
                newGene2 = getKeggGeneByKO(hkg.getProkaryotKeggCode(i), houseKeepingOrthologs, serv, processPanel);
            } 
            catch (Exception ex) { //TODO: exceptions
                Logger.getLogger(KEGGorthoWS.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            /* If no result, go on to next gene. */
            if ((newGene1 == null) && (newGene2 == null)) continue;
            
            /* Add orthologs to list of orthologs. */
            if (newGene1 != null)
            {
                eukaryotCount++;
                eukaryots.add(newGene1);
            }
            if (newGene2 != null)
            {
                prokaryotCount++;
                eukaryots.add(newGene2);
            }
            
            processPanel.setProgress((eukaryots.size() + eukaryots.size())*50/minimumNeededGenes);
        }
        
        int maxAvailableHKG[] = new int[2];
        if (prokaryotCount > eukaryotCount)
        {
            System.out.println("Got more results from Prokaryots");
            maxAvailableHKG[0] = maxAvailableProkaryotHKG;
            maxAvailableHKG[1] = maxAvailableEukaryotHKG;
            houseKeepingOrthologs.setGenes(prokaryots);
        }
        else
        {
            System.out.println("Got more results from Eukaryots");
            maxAvailableHKG[0] = maxAvailableEukaryotHKG;
            maxAvailableHKG[1] = maxAvailableProkaryotHKG;
            houseKeepingOrthologs.setGenes(eukaryots);
        }
        
        /* Repeat the process for the remaining genes, but starting with the group with the most already retrieved genes. */
        for (int j = 0; j < 2; j++)
        {
            for (int i=10; i<maxAvailableHKG[j]; i++)
            {
                newGene1 = null;
                try {
                    if ((prokaryotCount > eukaryotCount) == (j == 1))
                            newGene1 = getKeggGeneByKO(hkg.getEukaryotKeggCode(i), houseKeepingOrthologs, serv, processPanel);
                    else
                            newGene1 = getKeggGeneByKO(hkg.getProkaryotKeggCode(i), houseKeepingOrthologs, serv, processPanel);
                } 
                catch (Exception ex) { //TODO: exceptions
                    Logger.getLogger(KEGGorthoWS.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                /* If no result, go on to next gene. */
                if (newGene1 == null) continue;
                
                /* Add ortholog to list of orthologs. */
                houseKeepingOrthologs.addGene(newGene1);
                
                processPanel.setProgress((houseKeepingOrthologs.getGenes().size())*100/minimumNeededGenes);
                
                /* If already has the necessary number of genes, stop fetching. */
                if (houseKeepingOrthologs.getGenes().size() >= minimumNeededGenes)
                    break;
            }
            
            /* If already has the necessary number of genes, stop fetching. */
            if (houseKeepingOrthologs.getGenes().size() >= minimumNeededGenes)
                break;
            
            /* If finished one class of species, add the first ten of the other. (already previously obtained. */
            if (prokaryotCount > eukaryotCount)
                houseKeepingOrthologs.getGenes().addAll(eukaryots);
            else
                houseKeepingOrthologs.getGenes().addAll(prokaryots);
        }
        
        if (houseKeepingOrthologs.getGenes().size() < minimumNeededGenes)
        {
            resultKeeper.setFailed();
            processPanel.setFailed();
        }
        else
        {
            resultKeeper.setResult(houseKeepingOrthologs);
            processPanel.setComplete();
            processPanel.setStatus("Done.");
        }
    }
    
    public void setObtainHousekeepingGenes(boolean b)
    {
        getOrthologs = !b;
    }
    
    public Gene getKeggGeneByKO(String code, Genome houseKeepingOrthologs, KEGGPortType serv, ProcessPanel processPanel) throws RemoteException, InterruptedException, Exception
    {
        Definition []results;
        try
        {
            fluxControl.acquire();
                System.out.print("Getting ortholog for " +code+" in genome " + keggID + "... ");
                results = serv.get_genes_by_ko(code, keggID);
            fluxControl.release();
        }
        catch (Exception ex) 
        {
            fluxControl.release();
            System.out.println("An exception occured while trying to retrieve gene " + code + " from KEGG web-services.");
//            Logger.getLogger(KEGGorthoWS.class.getName()).log(Level.SEVERE, null, ex); 
            processPanel.setFailed();
            processPanel.setStatus("Failed.");
            resultKeeper.setFailed();

            return null;
        }
        
        if (results.length == 0)
        {
            System.out.println("Not found (get_genes_by_ko() came empty) !");
            return null;
        }
        
        /* Obtain ortholog gene. */
        Gene ortholog = getGeneFromID(serv, houseKeepingOrthologs, results[0].getEntry_id());
        
        if (ortholog != null)
            System.out.println("Found: " + ortholog.getName());
        else
            System.out.println("Not found (bget came empty) !");
        
        return ortholog;
    }

    /* TESTE */
     public static void main(String [] Args)
    {
        ResultKeeper k = new ResultKeeper();
//        KEGGorthoWS ra = new KEGGorthoWS("pfa", k); //pfa:PF10_0071
        KEGGorthoWS ra = new KEGGorthoWS("eco:b3058", k);
//        ra.setObtainHousekeepingGenes(true);
        ra.start();

        String result = (String) k.getResult();
        System.out.println("Result: " + result);
    }

    
}
