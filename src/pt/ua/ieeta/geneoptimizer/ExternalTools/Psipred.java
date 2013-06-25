package pt.ua.ieeta.geneoptimizer.ExternalTools;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.Main;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;

/**
 *
 * @author Paulo Gaspar
 */
public class Psipred extends Thread
{
    private String aminoAcidSequence;
    private Study study;

    public Psipred(String aminoAcidSequence, Study study)
    {
        this.aminoAcidSequence = aminoAcidSequence;
        this.study = study;
    }

    @Override
    public void run()
    {
        System.out.println("PsiPred started.");
        ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Sec. Structure prediction");
        processPanel.setIndeterminated();

        /* Get correct paths. */
        String toolsPath = (String) ApplicationSettings.getProperty("toolsPath", String.class);
        String path = (String) ApplicationSettings.getProperty("eugene_dir", String.class) + toolsPath + File.separator;

        /* Prepare input files. */
        processPanel.setStatus("Preparing...");
        if (!prepareInputFile(path))
        {
            processPanel.setStatus("Error writing file.");
            processPanel.setFailed();
            return;
        }
        
        /* Run the application. */
        processPanel.setStatus("Running...");
        if (!runPsipred(path))
        {
            processPanel.setStatus("Error executing.");
            processPanel.setFailed();
            return;
        }
                
        /* Parse final output file. */
        processPanel.setStatus("Parsing results...");
        String result;
        if ((result = parseOutputFile(path)) == null)
        {
            processPanel.setStatus("Error reading file.");
            processPanel.setFailed();
        }

        /* Delete input file. */
        File seq = new File(path + "sequence");
        seq.delete();
        
        processPanel.setStatus("Done.");
        processPanel.setComplete();

        study.getResultingGene().createStructure(new BioStructure(result, BioStructure.Type.proteinSecondaryStructure));
        study.getCurrentPanel().remakePanel();

        System.out.println("PsiPred ended.");
    }

    private boolean prepareInputFile(String path)
    {
        assert path != null;
        assert !path.isEmpty();
        
        /* Try to create fasta sequence file. */
        File file = new File(path + "sequence");

        /* Write aminoacid sequence to file. */
        try
        {
            file.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            out.write("> sequence");
            out.newLine();
            out.write(aminoAcidSequence);
            out.close();
        }
        catch (IOException ex)
        {
            System.out.println("Error writing fasta file: " + ex.getLocalizedMessage());
            file.delete();
            return false;
        }
        
        return true;
    }

    private boolean runPsipred(String path)
    {
        assert path != null;
        assert !path.isEmpty();

        /* Choose correct files for the operating system. */
        String execs[] = new String[3];
        if (Main.isWindows())
        {
            execs[0] = path+"seq2mtx.exe";
            execs[1] = path+"psipred.exe";
            execs[2] = path+"psipass2.exe";
        }
        else if (Main.isMac())
        {
            execs[0] = path+"seq2mtxMac";
            execs[1] = path+"psipredMac";
            execs[2] = path+"psipass2Mac";
        }
        else if (Main.isUnix())
        {
            execs[0] = path+"seq2mtxNix";
            execs[1] = path+"psipredNix";
            execs[2] = path+"psipass2Nix";
        }
        else return false; //unsupported operating system
    
        /* Check executability of the files. */
        File exe1 = new File(execs[0]);
        File exe2 = new File(execs[1]);
        File exe3 = new File(execs[2]);
        if (!exe1.canExecute() || !exe2.canExecute() || !exe3.canExecute())
        {
            System.out.println(exe1.getAbsolutePath());
            System.out.println(exe2.getAbsolutePath());
            System.out.println(exe3.getAbsolutePath());            
            JOptionPane.showMessageDialog(MainWindow.getInstance(), "The PsiPred application is not set as executable file, and therefore cannot be run.\nPlease go to Eugene's tools folder (in the user home folder), and give execution permission to all files.");
            return false;
        }
        
        /* Delete temporary files. */
        File f = new File(path+"result1");
        f.delete();
        f = new File(path+"result2");
        f.delete();
        f = new File(path+"result3");
        f.delete();
        f = new File(path+"result4");
        f.delete();    
        
        /* Arguments to create and run the first PsiPred process. */
        String cmdArgs1[] = {execs[0], path + "sequence"};
        
        /* Arguments to create and run the second PsiPred process. */
        String cmdArgs2[] = {execs[1], path + "result1", path + "weights.dat", path + "weights.dat2", path + "weights.dat3"};
        
        /* Arguments to create and run the second PsiPred process. */
        String cmdArgs3[] = {execs[2], path + "weights_p2.dat", "1", "1.0", "1.0", path + "result3", path + "result2"};
        
        System.out.println("Running first PsiPred command...");
        Process p = null;
        try
        {
            p = Runtime.getRuntime().exec(cmdArgs1, null, new File(path));
            
            /* Consume all output from seq2mtx. */
            PrintStream ps = new PrintStream(new FileOutputStream(path + "result1"));
            Thread consumer = new StreamConsumer(p.getInputStream(), ps);
            consumer.start();
            new StreamConsumer(p.getErrorStream(), System.out).start();
            
            int returnValue = p.waitFor();
            System.out.println("PsiPred terminated with return value " + returnValue);
            if (returnValue != 0) return false;
            
            /* Make sure the stream is completely writen to its output. */
            consumer.join();
            ps.close();
        }
        catch (Exception ex)
        { 
            System.out.println("An exception occured while trying to run PsiPred: " + ex.getMessage());
            if (p != null) p.destroy();
            return false;
        }
        
        System.out.println("Running second PsiPred command...");
        try
        {
            p = Runtime.getRuntime().exec(cmdArgs2, null, new File(path));
            
            /* Consume all output from psipred. */
            PrintStream ps = new PrintStream(new FileOutputStream(path + "result2"));
            Thread consumer = new StreamConsumer(p.getInputStream(), ps);
            consumer.start();
            new StreamConsumer(p.getErrorStream(), System.out).start();
            
            /* Wait for application to stop. */
            int returnValue = p.waitFor();
            System.out.println("PsiPred terminated with return value " + returnValue);
            if (returnValue != 0) return false;
            
            /* Make sure the stream is completely writen to its output. */
            consumer.join();
            ps.close();
        }
        catch (Exception ex)
        {
            System.out.println("An exception occured while trying to run PsiPred: " + ex.getMessage());
            if (p != null) p.destroy();
            return false;
        }
        
        System.out.println("Running third PsiPred command...");
        try
        {
            p = Runtime.getRuntime().exec(cmdArgs3, null, new File(path));
            
            /* Consume all output from psipass. */
            PrintStream ps = new PrintStream(new FileOutputStream(path + "result4"));
            Thread consumer = new StreamConsumer(p.getInputStream(), ps);
            consumer.start();
            new StreamConsumer(p.getErrorStream(), System.out).start();
            
            int returnValue = p.waitFor();
            System.out.println("PsiPred terminated with return value " + returnValue);
            if (returnValue != 0) return false;
            
            /* Make sure the stream is completely writen to its output. */
            consumer.join();
            ps.close();
        }
        catch (Exception ex)
        { 
            System.out.println("An exception occured while trying to run PsiPred: " + ex.getMessage());
            if (p != null) p.destroy();
            return false;
        }
        
        return true;
    }

    private String parseOutputFile(String path)
    {
        StringBuilder finalSequence = new StringBuilder("");
        try 
        {
            BufferedReader in = new BufferedReader(new FileReader(new File(path + "result4")));
            String line;
            while((line = in.readLine()) != null)
            {
                /* Ignore comments. */
                if (line.startsWith("#")) continue;

                /* Ignore empty lines. */
                if (line.length() < 1) continue;

                if (line.startsWith("Pred: "))
                    finalSequence.append(line.substring(line.indexOf("Pred: ")+6));
            }
        }
        catch (Exception ex)
        { //TODO: exceptions
            
            Logger.getLogger(Psipred.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        finalSequence.append(" ");
        
        return finalSequence.toString();
    }
    
    public static void main(String [] args)
    {
        Psipred p = new Psipred("LFSGKFREKTWETPFTCFAVIPKKAARIAEKVLKSVIANAEQKGLDLDRLYIKKAVADDGPILKKWIPRAHGRATMVRKRLSHITIVLEEKPEGKEEELFSGKFREKTWETPFTCFAVIPKKAARIAEKVLKLFSGKFREKTWETPFTCFAVIPKKAARIAEKVLKSVIANAEQKGLDLDRLYIKKAVADDGPILKKWIPRAHGRATMVRKRLSHITIVLEEKPEGKEEELFSGKFREKTWETPFTCFAVIPKKAARIAEKVLKSVIANAEQKGLDLDRLYIKKAVADDGPILKKWIPRAHGRATMVRKRLSHITIVLEEKPEGKEEELFSGKFREKTWETPFTCFAVIPKKAARIAEKVLKSVIANAEQKGLDLDRLYIKKAVADDGPILKKWIPRAHGRATMVRKRLSHITIVLEEKPEGKEEELFSGKFREKTWETPFTCFAVIPKKAARIAEKVLKSVIANAEQKGLDLDRLYIKKAVADDGPILKKWIPRAHGRATMVRKRLSHITIVLEEKPEGKEEESVIANAEQKGLDLDRLYIKKAVADDGPILKKWIPRAHGRATMVRKRLSHITIVLEEKPEGKEEE*", null);
        p.start();
    }
    
}
