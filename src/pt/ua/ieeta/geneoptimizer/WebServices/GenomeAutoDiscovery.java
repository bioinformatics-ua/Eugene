package pt.ua.ieeta.geneoptimizer.WebServices;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import pt.ua.ieeta.geneoptimizer.ExternalTools.NCBIwebFetcher;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.FileHandling.FastaParser;
import pt.ua.ieeta.geneoptimizer.FileHandling.HeaderInfo;
import pt.ua.ieeta.geneoptimizer.GUI.GeneInformationPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationModel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.WebServices.KeggGenomeCodes.Pair;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import pt.ua.ieeta.geneoptimizer.geneDB.UsageAndContextTables;

/**
 *
 * @author Paulo Gaspar
 */
public class GenomeAutoDiscovery extends Observable implements Runnable
{ 
    private Genome genome;
    private List<Gene> genes;
    
    public GenomeAutoDiscovery(Genome genome, List<Gene> genesRef)
    {
        this.genome = genome;
        this.genes = genesRef;
        
        /* Add StudyMakerPanel as an observer so when all information is available plugins are automatacally updated */
        this.addObserver(OptimizationModel.getInstance());
    }
    
    @Override
    public void run()
    {
        boolean autoDiscoveryEnabled = (Boolean) ApplicationSettings.getProperty("genomeAutoDiscoveryEnabled", Boolean.class);
        if (!autoDiscoveryEnabled) return;
     
        System.out.println("GenomeAutoDiscovery started.");
        
        /* Number of genes to pick from genome. */
        int numGenesToUse = 3;
        
        /* Randomly pick a few genes from the genome, to use as reference. */
        List<Gene> someGenes = new ArrayList<Gene>(numGenesToUse);
        Random rand = new Random();
        for (int i=0; i<numGenesToUse; i++)
            someGenes.add(genes.get(rand.nextInt(genes.size())));
        
        /* Parse those genes' headers. */
        HashMap concordance = new HashMap<String, Integer>();
        NCBIwebFetcher ncbiParser = null;
        for (int i=0; i<numGenesToUse; i++)
        {
            /* Try parsing the FASTA header and extracting information. */
            GeneAutoDiscover gad = new GeneAutoDiscover();
            ncbiParser = gad.parseHeader(someGenes.get(i).getGeneHeader());
            
            /* Could not parse header. */
            if (ncbiParser == null)
                continue;
            
            if (ncbiParser.hasInformation(HeaderInfo.KEGG_ORGANISM))
            {
                String KeggName = ncbiParser.getFetchedInformation(HeaderInfo.KEGG_ORGANISM).toLowerCase();
                if (concordance.containsKey(KeggName))
                    concordance.put(KeggName, (Integer)concordance.get(KeggName) +1 );
                else
                    concordance.put(KeggName, 1);
            }
        }
        
        /* Could not get any information about genes. Ask user. */
        if (concordance.isEmpty())
        {
            String genomeName = "";
            if (genome.getName().trim().contains(" "))
                genomeName = genome.getName().trim();
            else
            {
                String message = "The genome name you entered could not be identified as a species.\n" 
                                + "To contact some online services (such as KEGG), the species name is required.\n"
                                + "Please enter the species name (two names separated by a space, and possibly the strand):";
                while (!genomeName.trim().contains(" "))
                {
                    genomeName = (String) JOptionPane.showInputDialog(null,
                                                                      message, "Genome name", JOptionPane.QUESTION_MESSAGE, 
                                                                      null, null, genome.getName());
                    
                    /* User refuses to give species name. */
                    if (genomeName == null) break;
                }
            }
            
            /* No genome supplied? Give up. */
            if ((genomeName == null) || genomeName.isEmpty())
            {
                System.out.println("Error while trying to find the name of the genome.");
                return;
            }
            
            Pair mostLikelyGenome = KeggGenomeCodes.getInstance().getMostLikelyGenomeName(genomeName);
            String keggCode;
            if (mostLikelyGenome != null)
            {
                System.out.println("Most likely genome: " + mostLikelyGenome.getRight());
                keggCode = mostLikelyGenome.getLeft();
            }
            else
                keggCode = genomeName.charAt(0) + genomeName.substring(genomeName.indexOf(" ")+1, genomeName.indexOf(" ")+3);
            
            /* Extract genome code from the given species name. */
            concordance.put(keggCode.toLowerCase(), 1);
        }
        
        
        /* Count how many kegg keys are in agreement. */
        String keggName = null;
        for (Iterator it = concordance.entrySet().iterator(); it.hasNext();) {
            Map.Entry k = (Map.Entry) it.next();
            System.out.println("  Concordance: " + k + " occured " + k.getValue() + " times out of " + numGenesToUse);
            if (keggName == null)
                keggName = (String) k.getKey();
            else
                if ((Integer)concordance.get(k) > (Integer)concordance.get(keggName))
                    keggName = (String) k.getKey();
        }
        
        /* If there was unanimity, set the genome name. */
        if (ncbiParser != null)
            if ((concordance.size() == 1) && (ncbiParser.hasInformation(HeaderInfo.ORGANISM_NAME)))
            {
                String genomeName = ncbiParser.getFetchedInformation(HeaderInfo.ORGANISM_NAME);
                genome.setName(genomeName);
            }
        
        /* Genome that will take the housekeeping genes. */
        Genome result = null;
        
        /* Check if the housekeeping genes were already downloaded. */
        String eugeneDir = (String)ApplicationSettings.getProperty("eugene_dir", String.class);
        String filename = eugeneDir + (String)ApplicationSettings.getProperty("highlyExpressedGenesFolder", String.class) + File.separator + keggName + ".txt";
        File hkFile = new File(filename);
        
        if (!hkFile.exists())
        {
            System.out.println("File not exists..." + hkFile.getPath() );
            
            /* Obtain house keeping genes for this genome. */
            ResultKeeper KeggResultKeeper = new ResultKeeper();
//            KEGGorthoWS keggService = new KEGGorthoWS(keggName, KeggResultKeeper);
            KEGGOrthoRestWS keggService = new KEGGOrthoRestWS(keggName, KeggResultKeeper);
            keggService.setObtainHousekeepingGenes(true);
            keggService.start();

            /* Wait for result. */
            result = (Genome) KeggResultKeeper.getResult();
            if (KeggResultKeeper.isFail())
            {
                /*genomeName = (String) JOptionPane.showInputDialog(null,
                            "Could not retrieve enough housekeeping genes for this genome.", "Genome name",
                            JOptionPane.QUESTION_MESSAGE, null, null, genome.getName()); */
                return;
            }
            else
            /* Save housekeeping genes in file. */
            result.writeToFile(filename);
        }
        else
        {
            System.out.println("Loading housekeeping genes from " + hkFile.getPath() );
            
            result = new Genome("Highly Expressed Genes");
            
            /* Read housekeeping genes from file. */
            FastaParser parser = new FastaParser(genome.getFilters());
            try {
                parser.readGenesFromFile(filename, genome.getGeneticCodeTable(), result, new ArrayList<Gene>());
            } catch (Exception ex) 
            {
                //TODO: exceptions
                ex.printStackTrace();
                Logger.getLogger(GenomeAutoDiscovery.class.getName()).log(Level.SEVERE, null, ex);
                parser.setLoadingEnded();
                return;
            }
            
            result.setGenes(parser.getGenes());
            parser.setLoadingEnded();
        }
        
        /** Define genetic code table for the set of highly expressed genes. */
        result.setGeneticCodeTable(genome.getGeneticCodeTable());

        /** Make calculations and statistics for codon usage and context. */
        UsageAndContextTables uct = new UsageAndContextTables(result, result.getGenes());
        new Thread(uct).start();

        /* Wait until everything is calculated. */
        uct.waitUntilFinished();

        result.setCodonUsageContextTables(uct);
        
        /* Set the housekeeping genes for this genome. */
        genome.setHouseKeepingGenes(result);
        
        System.out.println("GenomeAutoDiscovery ended.");
        
        /* Update selected study information panel. */
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() != null)
            GeneInformationPanel.getInstance().updateInformationSingleGene(ProjectManager.getInstance().getSelectedProject().getSelectedStudy());
        
        /* Notify observers that all information is available. */
        setChanged();
        notifyObservers(Boolean.valueOf(true));
    }

    public Genome getGenome() {
        return genome;
    }
    
    
}
