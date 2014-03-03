package pt.ua.ieeta.geneoptimizer.ExternalTools;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.Main;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 */
public class Muscle extends AlignmentTool
{
    private ResultKeeper resultKeeper;
    private List<Gene> sequences;

    public Muscle(List<Gene> sequences, ResultKeeper resultKeeper)
    {
        assert sequences != null;
        assert resultKeeper != null;
        assert sequences.size() > 1;

        this.resultKeeper = resultKeeper;
        this.sequences = sequences;
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void run()
    {
        System.out.println("MUSCLE started.");
        
        ProgressPanel.ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("MUSCLE alignment");
        processPanel.setIndeterminated();
        processPanel.setStatus("Preparing...");
        
        String toolsPath = (String) ApplicationSettings.getProperty("toolsPath", String.class);
        String path = (String)ApplicationSettings.getProperty("eugene_dir", String.class) + toolsPath + File.separator;
        
        /* Try to create fasta sequence inputFile. */
        if (!prepareInputFile(path))
        {
            System.out.println("Muscle: Error writing fasta file.");
            resultKeeper.setFailed();
            processPanel.setStatus("Error preparing file.");
            processPanel.setFailed();
            return;
        }

        processPanel.setStatus("Running...");
        
        /* Run MUSCLE. */
        if (!runMuscle(path))
        {
            System.out.println("Muscle: Error executing.");
            resultKeeper.setFailed();
            processPanel.setStatus("Error running muscle");
            processPanel.setFailed();
            return;
        }

        processPanel.setStatus("Reading output...");
        
        /* Parse final output file. */
        if (parseOutputFile(path) == null)
        {
            System.out.println("Muscle: Error reading fasta file.");
            resultKeeper.setFailed();
            processPanel.setStatus("Error reading file.");
            processPanel.setFailed();
            return;
        }

        processPanel.setStatus("Done.");
        processPanel.setComplete();
        
        /* Warn waiting thread that the result is available. */
        resultKeeper.setResult(sequences);

        System.out.println("MUSCLE ended.");
    }
    
    private boolean runMuscle(String path)
    {
        assert path != null;
        assert !path.isEmpty();
        
        /* Arguments to create and run a new MUSCLE process. */
        String cmdArgs[] = new String[11];
        
        if (Main.isWindows())
            cmdArgs[0] = path+"mus38.exe";
        else if (Main.isMac())
            cmdArgs[0] = path+"musMac";
        else if (Main.isUnix())
            cmdArgs[0] = path+"musNix";
        else return false; //unsupported operating system

        /* Check executability of the files. */
        File exe = new File(cmdArgs[0]);
        if (!exe.canExecute())
        {
            JOptionPane.showMessageDialog(MainWindow.getInstance(), "The Muscle application is not set as executable file, and therefore cannot be run.\nPlease go to Eugene's tools folder (in the user home folder), and give execution permission to all files.");
            return false;
        }
        
        cmdArgs[1] = "-in";
        cmdArgs[2] = "alignment_in.txt";
        cmdArgs[3] = "-out";
        cmdArgs[4] = "alignment_out.txt";
        cmdArgs[5] = "-diags"; 
        cmdArgs[6] = "-maxiters"; 
        cmdArgs[7] = "1";
        cmdArgs[8] = "-quiet";
        cmdArgs[9] = "-maxhours";
        cmdArgs[10] = "1";
        
        Process p = null;
        try
        {
            /* Run the application with the specified arguments, in the specified path. */
            p = Runtime.getRuntime().exec(cmdArgs, null, new File(path));
            
            /* Consume all output from MUSCLE. */
            new StreamConsumer(p.getInputStream(), System.out).start();
            new StreamConsumer(p.getErrorStream(), System.out).start();
            
            //TODO: must make sure the stream is completely writen to its output!!! see PsiPred.java
            
            /* Wait for the application to terminate. */
            int returnValue = p.waitFor();
            System.out.println("MUSCLE terminated with return value " + returnValue);
        }
        catch (Exception ex)
        { //TODO: excepçoes.
            System.out.println("An exception occured while trying to run MUSCLE: " + ex.getMessage());
            if (p != null) p.destroy();
            return false;
        }
        
        return true;
    }
    
    private HashMap<String,String> parseOutputFile(String path)
    {
        HashMap<String, String> results = new HashMap<String,String>();
        
        File outputFile = new File(path + "alignment_out.txt");
        
        try(BufferedReader in = new BufferedReader(new FileReader(outputFile));) {
            
            String line;
            StringBuilder sb = new StringBuilder("");
            String id, newID;

            /* Read first ID in file. */
            id = in.readLine().substring(1);

            /* While there are lines to read, parse them. */
            while((line = in.readLine()) != null)
            {
                /* Ignore empty lines. */
                if (line.length() < 1) continue;

                /* Join each line, until a '>' is found. */
                if (!line.startsWith(">"))
                {
                    sb.append(line);
                    //sequence += line;
                    continue;
                }

                /* Save new found ID. It will be the ID of the next sequence. */
                newID = line.substring(line.indexOf('>')+1);

                /* Create an aligned bio-structure for the read gene. */
                createAlignedStructure(id, sb.toString());
                
                id = newID;
                //sequence = "";
                sb = new StringBuilder("");
            }
            
            /* Same operation for the last one. */
            createAlignedStructure(id, sb.toString());
        }
        catch (Exception ex)
        { //TODO: exceptions
            Logger.getLogger(Psipred.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return results;
    }
    
    private boolean prepareInputFile(String path)
    {
        assert path != null;
        assert !path.isEmpty();
        
        File inputFile = new File(path + "alignment_in.txt");
        
        /* Write aminoacid sequence to inputFile. */
        try
        {
            inputFile.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(inputFile));
            for (Gene gene : sequences)
            {                
                out.write(">"+gene.getOrthologId()); //getName().hashCode());
                out.newLine();
                out.write(gene.getAminoacidSequence().replaceAll("[*]", ""));
                out.newLine();
            }
            out.close();
        }
        catch (IOException ex)
        {
            //TODO: excepçoes...
            inputFile.delete();
            return false;
        }
        
        return true;
    }

    

    private void createAlignedStructure(String id, String ialignedAAsequence)
    {
        String alignedAASequence = ialignedAAsequence;
        
        /* Results may be scrambled, so find each ortholog by its ID. */
        for (Gene gene : sequences)
            if (gene.getOrthologId().equals(id))
            {
                if (gene.getAminoacidSequence().contains("*"))
                    alignedAASequence += "*";
                
                gene.setAlignedStructure(alignedAASequence, BioStructure.Type.proteinPrimaryStructure);
                StringBuilder alignedCodonSequence = new StringBuilder();
                int i=0, j=0; //i to count aligned AA structure;  j to count original codon sequence
                for (; i<alignedAASequence.length(); i++)                
                {
                    if (gene.getAlignedAminoAcidAt(i).equals("-"))
                    {
                        alignedCodonSequence.append("---");
                        continue;
                    }
                    
                    alignedCodonSequence.append(gene.getStructure(BioStructure.Type.mRNAPrimaryStructure).getWordAt(j++));
                }

                gene.setAlignedStructure(alignedCodonSequence.toString(), BioStructure.Type.mRNAPrimaryStructure);
                
//                System.out.println(gene.getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence());
                
                System.out.println("Created new aligned structure for gene " + gene.getName() + " for genome " + gene.getGenomeName());
                 
                assert alignedAASequence.length()*3 == alignedCodonSequence.length();
                
                break;
            }
    }
}
