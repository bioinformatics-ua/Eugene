/*
 * GeneLoader.java
 */
package pt.ua.ieeta.geneoptimizer.FileOpeningParsing;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI.GenePoolGUI;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.WebServices.GenomeAutoDiscovery;
import pt.ua.ieeta.geneoptimizer.geneDB.*;

/**
 * Class to load new genomes to the application
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 * @author Ricardo Gonzaga
 */
public class GeneLoader extends Thread {

    private String genomeName = null;
    private int genomeID;
    private int geneticCodeTableID;
    private GenomeFilters genomeFilters;
    private String[] genomeFiles = null;
    private boolean isGenePoolGUI = false;
    private HashMap<String, String> additionsMap;

    public GeneLoader(String genomeName, int genomeID, int geneticCodeTableID, GenomeFilters filters, String[] genomeFiles, HashMap<String, String> additionsMap) {

        assert genomeName != null && !genomeName.isEmpty();
        assert genomeFiles != null;
        assert additionsMap != null;
        assert filters != null;

        this.genomeName = genomeName;
        this.genomeID = genomeID;
        this.geneticCodeTableID = geneticCodeTableID;
        this.genomeFilters = filters;
        this.genomeFiles = genomeFiles;
        this.additionsMap = additionsMap;
    }

    public GeneLoader(GenomeFilters filters) {
        assert filters != null;
        isGenePoolGUI = true;
        this.genomeFilters = filters;
    }

    /* Parser to read fasta file format. */
    private IGenomeFileParser parser;

    @Override
    public void run() {        
        if (isGenePoolGUI) {
            GenePoolGUI.enableButtonLoadGenomes(false);
            System.out.println("GeneLoader started.");
            openGenomesToPool();
            System.out.println("GeneLoader ended.");
            GenePoolGUI.enableButtonLoadGenomes(true);
        } else {
            openGenomesToPool();
        }
    }

    /**
     * Shows dialog to select genome files to open, and then opens/parses them.
     */
    private synchronized void openGenomesToPool() {
        if (isGenePoolGUI) {
            if (showOpenDialog() == JFileChooser.APPROVE_OPTION) {
                loadChromossomesFromFiles();
            }
        } else {
            loadGenomeFromFile();
        }
    }

    private int showOpenDialog() {
        /* Option chosen on file chooser (Open or Cancel). */
        int chosenOption = JFileChooser.CANCEL_OPTION;

        /* Create a file chooser. */
        final JFileChooser filechooser = new JFileChooser();

        /* Enable multi-selection to allow opening several chromosomes at the same time. */
        filechooser.setMultiSelectionEnabled(true);

        /* Start at the last opened directory. */
        filechooser.setCurrentDirectory(new File((String) ApplicationSettings.getProperty("lastGeneDirectory", String.class)));

        /* Set file filters. */
        FileNameExtensionFilter fastaFilter = new FileNameExtensionFilter("Fasta Nucleotide Genomes (.fa, .ffn, .fasta)", "fasta", "fa", "ffn");
        FileNameExtensionFilter genBankFilter = new FileNameExtensionFilter("GenBank Genomes (.gbk, .gb)", "gbk", "gb");
        filechooser.addChoosableFileFilter(genBankFilter);
        filechooser.addChoosableFileFilter(fastaFilter);
        filechooser.setFileFilter(fastaFilter);

        /* Show file choser window to allow user to select files. */
        filechooser.setDialogTitle("Open genome file in nucleotide fasta format");

        boolean proceed = false;

        startofwhile:
        while (!proceed) {
            chosenOption = filechooser.showDialog(null, "Load Genome");

            /* User canceled. */
            if (chosenOption == JFileChooser.CANCEL_OPTION) {
                return chosenOption;
            }

            /* If "Open" selected. */
            genomeFiles = null;
            if (chosenOption == JFileChooser.APPROVE_OPTION) {
                /* Get files selected by user. */
                File[] sf = filechooser.getSelectedFiles();
                genomeFiles = new String[sf.length];

                ApplicationSettings.setProperty("lastGeneDirectory", filechooser.getCurrentDirectory().getAbsolutePath(), String.class);

                /* Verify their existence. */
                for (File f : sf) {
                    if (!f.exists()) {
                        continue startofwhile;
                    }
                }

                /* Save list of chosen files. Add it's directory to each file string. */
                int index = 0;
                for (File f : sf) {
                    genomeFiles[index++] = filechooser.getCurrentDirectory() + File.separator + f.getName();
                }

                proceed = true;
            }
        }

        return chosenOption;
    }

    private synchronized void loadGenomeFromFile() {        
        GeneticCodeTable geneticCodeTable = null;

        try {
            geneticCodeTable = GeneticCodeTableParser.getInstance().getCodeTableByID(geneticCodeTableID);
        } catch (Exception ex) {
            System.out.println("Exception before trying to load files. Error obtaining the genetic code table: " + ex.getMessage());
            return;
        }
        /* Check for existance of files to be loaded. */
        if (genomeFiles.length == 0) {
            return;
        }

        /* Create new genome. */
        Genome genome = new Genome(genomeName, geneticCodeTable);
        genome.setFilters(genomeFilters);
        /* Create new set of genes. */
        Vector<Gene> genes = new Vector<Gene>(200, 100);

        /* For each file, read its genes, and create a new entry in the gene pool. */
        for (String path : genomeFiles) {
            try {
                /* Create a new file parser to read the genes of each genome file. */
                /* If it's a Fasta file, use the Fasta parser. For structured files (e.g. genbank) use the default parser. */
                String fileExtension = path.substring(path.lastIndexOf(".") + 1, path.length());
                if (fileExtension.equals("fa") || fileExtension.equals("ffn") || fileExtension.equals("fna") || fileExtension.equals("fasta") || fileExtension.equals("txt")) {
                    parser = new FastaParser(genomeFilters);
                } else {
                    parser = new DefaultFileParser(genomeFilters);
                }

                /* Read genes from file, and put them into a vector. */
                parser.readGenesFromFile(path, geneticCodeTable, genome, genes);
                genome.addNumRejectedGenes(parser.getNumRejectedGenes());

                /* At this point, genes can't be null! */
                assert genes != null;

                /* Read file genes. */
                if (genes.isEmpty()) {
                    continue; //no genes in file.   
                }

            } catch (Exception ex) {
                System.out.println("Error reading file '" + path + ": " + ex.getMessage());
            }
        }

        /* No genes? Quit. */
        if (genes.isEmpty()) {
            String message = "The opened genome file had no valid genes. Note: The opened file should be in a supported format (FASTA, GenBank).";
            JOptionPane.showMessageDialog(MainWindow.getInstance(), message, "No valid genes in genome", JOptionPane.ERROR_MESSAGE);
            if (parser != null) {
                parser.setLoadingEnded();
            }
            return;
        }

        genome.setGenesRef(genomeFiles, genes);

        /* set additions if they exists */        
        if (!additionsMap.isEmpty()) {
            //store entry (key/value) of hashmap in set
            Set mapSet = (Set) additionsMap.entrySet();
            //create iterator on set
            Iterator additionsIterator = mapSet.iterator();
            while (additionsIterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) additionsIterator.next();
                //get key 
                String geneName = (String) mapEntry.getKey();
                //get value of the key
                String sequence = (String) mapEntry.getValue();
                Gene additionGene = new Gene(geneName, genome);
                additionGene.createStructure(new ByteString(sequence), BioStructure.Type.mRNAPrimaryStructure);
                additionGene.calculateAllStructures();
                genome.addGeneManually(additionGene);
            }
        }

        /* Create a codon usage table for this genome, and calculate it. */
        UsageAndContextTables cut = new UsageAndContextTables(genome, genes);
        genome.setCodonUsageContextTables(cut);
        new Thread(cut).start();

        /* Add genome to pool. */
        genome.setGenomeID(genomeID);
        GenePool.getInstance().addGenomeToPool(genome);

        if (parser != null) {
            parser.setLoadingEnded();
        }

        /* Show a loading report message. */
        int numNucs = 0;
        for (Gene g : genes) {
            numNucs += (g.getSequenceLength() * 3);
        }

        String message = "<html>"
                + "<b>You genome was successfully loaded</b><br/><br/>"
                + "Genome name: <i>" + genome.getName() + "</i><br/>"
                + "Number of files read: <i>" + genomeFiles.length + "</i><br/>"
                + "Number of valid genes: <b>" + genes.size() + "</b><br/>"
                + "Number of rejected genes: <b>" + parser.getNumRejectedGenes() + "</b><br/>"
                + "Number of nucleotides: <b>" + numNucs + "</b>"
                + "</html>";

        if (isGenePoolGUI)
            JOptionPane.showMessageDialog(GenePoolGUI.getInstance(), message);

//        new Thread(new MessageWindow(GenePoolGUI.getInstance(), false, true, message)).start();

        /* Start Genome auto discovery module. */
//        GenomeAutoDiscovery genomeAD = new GenomeAutoDiscovery(genome, genes);        
//        new Thread(genomeAD).start();
    }

    private synchronized void loadChromossomesFromFiles() {
        GeneticCodeTable geneticCodeTable = null;
        Genome genome;
        Vector<Gene> genes;
        

        try {
            geneticCodeTable = GeneticCodeTableParser.getInstance().getCodeTableByID(GenePoolGUI.getSelectedCodeTableId());
        } catch (Exception ex) {
            System.out.println("Exception before trying to load files. Error obtaining the genetic code table: " + ex.getMessage());
            //TODO: DEAL with errors!
            return;
        }

        /* Check for existance of files to be loaded. */
        if (genomeFiles.length == 0) {
            return;
        }

        /* Create new empty genome. */
        String genomeNameFromFile = genomeFiles[0].substring(genomeFiles[0].lastIndexOf(System.getProperty("file.separator")) + 1, genomeFiles[0].lastIndexOf("."));
        genome = new Genome(genomeNameFromFile, geneticCodeTable);
        genome.setFilters(genomeFilters);

        /* Create new set of genes. */
        genes = new Vector<Gene>(200, 100);

        /* For each file, read its genes, and create a new entry in the gene pool. */
        for (String path : genomeFiles) {
            try {
                /* Create a new file parser to read the genes of each genome file. */
                /* If it's a Fasta file, use the Fasta parser. For structured files (e.g. genbank) use the default parser. */
                String fileExtension = path.substring(path.lastIndexOf(".") + 1, path.length());
                if (fileExtension.equals("fa") || fileExtension.equals("ffn") || fileExtension.equals("fna") || fileExtension.equals("fasta") || fileExtension.equals("txt")) {
                    parser = new FastaParser(genomeFilters);
                } else {
                    parser = new DefaultFileParser(genomeFilters);
                }

                /* Read genes from file, and put them into a vector. */
                parser.readGenesFromFile(path, geneticCodeTable, genome, genes);
                genome.addNumRejectedGenes(parser.getNumRejectedGenes());

                /* At this point, genes can't be null! */
                assert genes != null;

                /* Read file genes. */
                if (genes.isEmpty()) {
                    continue; //no genes in file.   
                }
                /**
                 * ************************** TESTE **************************
                 */
//                Collections.sort(genes);
//                for (Gene g : genes)
//                {
//                    float GCContent = g.getGCContent();
//                    System.out.println(g.getName() + "\t" + GCContent);
//                }
                /**
                 * ***********************************************************
                 */
            } catch (Exception ex) {
                //TODO: lidar com estes problemas de excepçoes
                Logger.getLogger(GeneLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /* No genes? Quit. */
        if (genes.isEmpty()) {
            String message = "The opened genome file had no valid genes. Note: The opened file should be in a supported format (FASTA, GenBank).";
            JOptionPane.showMessageDialog(MainWindow.getInstance(), message, "No valid genes in genome", JOptionPane.ERROR_MESSAGE);

            if (parser != null) {
                parser.setLoadingEnded();
            }

            return;
        }
//        // Genes are no longer kept in memory
//        /* Put read genes in the created genome. */
//        genome.setGenes(genes);        
        genome.setGenesRef(genomeFiles, genes);

        /* Create a codon usage table for this genome, and calculate it. */
        UsageAndContextTables cut = new UsageAndContextTables(genome, genes);
        genome.setCodonUsageContextTables(cut);
        new Thread(cut).start();

        /* Get a name for the genome. */
        if (genomeName == null) {
            /* Create pop-up window */
            String genomeName = (String) JOptionPane.showInputDialog(null,
                    "Please choose a name for the opened genome:", "Genome name",
                    JOptionPane.QUESTION_MESSAGE, null, null, genome.getName());

            if (genomeName != null) {
                genome.setName(genomeName);
            }
        } else {
            genome.setName(genomeName);
        }

        /* Add genome to pool. */
        GenePool.getInstance().addGenomeToPool(genome);

        if (parser != null) {
            parser.setLoadingEnded();
        }

        /* Show a loading report message. */
        int numNucs = 0;
        for (Gene g : genes) {
            numNucs += (g.getSequenceLength() * 3);
        }

        String message = "<html>"
                + "<b>You genome was successfully loaded</b><br/><br/>"
                + "Genome name: <i>" + genome.getName() + "</i><br/>"
                + "Number of files read: <i>" + genomeFiles.length + "</i><br/>"
                + "Number of valid genes: <b>" + genes.size() + "</b><br/>"
                + "Number of rejected genes: <b>" + parser.getNumRejectedGenes() + "</b><br/>"
                + "Number of nucleotides: <b>" + numNucs + "</b>"
                + "</html>";

        JOptionPane.showMessageDialog(GenePoolGUI.getInstance(), message);

//        new Thread(new MessageWindow(GenePoolGUI.getInstance(), false, true, message)).start();

        /* Start Genome auto discovery module. */
        GenomeAutoDiscovery genomeAD = new GenomeAutoDiscovery(genome, genes);
        new Thread(genomeAD).start();

    }

    public synchronized String getGenomeName() {
        return genomeName;
    }
}
