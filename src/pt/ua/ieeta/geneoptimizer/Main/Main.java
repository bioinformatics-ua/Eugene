package pt.ua.ieeta.geneoptimizer.Main;

import java.awt.Color;
import java.awt.Insets;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import pt.ua.ieeta.geneoptimizer.ExternalTools.StreamConsumer;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.RedesignProtocolReaderWriter;
import pt.ua.ieeta.geneoptimizer.PluginSystem.PluginLoader;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;

/**
 * Gene Optimizer main class.
 * @author Paulo Gaspar
 */
public class Main
{
    private static String version;
        
    public static void main(String[] args) throws FileNotFoundException, IOException
    {          
        /* Application version. */
        version = "v1.3.2";
        
        /* Check directoy tree. */
        verifyDirectoryTree();
        
        //(args.length > 0) && args[0].equals("-o")
        if (false) {
            String home_dir = System.getProperty("user.home");
            String separator = System.getProperty("file.separator");
            String eugene_dir = home_dir.concat(separator + "EuGene" + separator);

            /* Select standard output file. */
            File output = new File(eugene_dir, "stdout.log");
            PrintStream newps = new PrintStream(new FileOutputStream(output, (output.length() < 1000000)), true);
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime());
            newps.println();
            newps.println();
            newps.println("********************************");
            newps.println("   Date: " + date);
            newps.println("   Version: " + version);
            newps.println("********************************");
            newps.println();

            /* Set standard output and standard error to use the file. */
            System.setOut(newps);
            System.setErr(newps);
        }

        /* Verify operating system. */
        if (!(isWindows() || isMac() || isUnix()))
        {
            System.out.println("Unsupported operating system: " + System.getProperty("os.name"));
            JOptionPane.showMessageDialog(MainWindow.getInstance(), "EuGene is currently only available for Windows, Mac and Linux operating systems.", "Unsupported operating system", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        /* Load application settings. */
        ApplicationSettings.getInstance();
        
        /* Extrat external tools from JAR */
        extractToolsFromJAR();

        /* Select look and feel. */
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ex) {
        Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex); }

        /* Set UI settings (background colours). */
        UIManager.put("TaskPane.useGradient", Boolean.TRUE);
        UIManager.put("TaskPane.backgroundGradientStart", new Color(153, 180, 209));
        UIManager.put("TaskPane.backgroundGradientEnd", new Color(190, 204, 218));
        UIManager.put("TaskPaneGroup.background", new Color(240, 240, 240));
        UIManager.put("TabbedPane.tabInsets", new Insets(0, 0, 0, 0));
        
        if(!version.equals(ApplicationSettings.getProperty("EugeneVersion", String.class))) {
            showChangelogs();
            
            ApplicationSettings.setProperty("EugeneVersion", version, String.class);
        }
        
        /* Create gene pool. */
        GenePool.getInstance();

        /* Create and show main window. */
        MainWindow.getInstance().setVisible(true);

        /* Create and start plugin loader. */
        new Thread(PluginLoader.getInstance()).start();

        /* Create and start study loader. */
        RedesignProtocolReaderWriter.getInstance().start();
    }
    
    public static String getVersion() {
        return version;
    }

    /* Verify if necessary directories are present, and if not, create them. */
    private static void verifyDirectoryTree() {
        System.out.println("Verify directory tree ....");

        File dir;
        String home_dir = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        String eugene_dir = home_dir.concat(separator + "EuGene" + separator);

        String[] folders = {"HighlyExpressed", "PDBFiles", "Plugins", "Studies", "Images", "Tools", "Projects"};

        for (String folder : folders) {
            dir = new File(eugene_dir, folder);
            if (!dir.exists()) {
                if (dir.mkdirs()) {
                    System.out.println("Created directory " + dir.getPath());
                } else {
                    System.out.println("Failed to create directory " + dir.getPath());
                }
            } else {
//                System.out.println("Directory alredy exists " + dir.getPath());
            }
        }
        System.out.println("Verify directory tree complete!\n");
    }

    private static void extractToolsFromJAR() 
    {
        System.out.println("Extracting tools from JAR...");

        String[] toolsFiles = {
            "mus38.exe",
            "musNix",
            "musMac",
            
            "psipass2.exe",
            "psipass2Mac",
            "psipass2Nix",
            
            "psipred.exe",
            "psipredMac",
            "psipredNix",
            
            "seq2mtx.exe",
            "seq2mtxMac",
            "seq2mtxNix",
            
            "weights_p2.dat",
            "weights.dat",
            "weights.dat2",
            "weights.dat3"
        };
        
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String toolsDirectory = "Tools/";
        BufferedInputStream inputStream;
        File file;
        BufferedOutputStream bufferedOutputStream;
        byte[] buffer;

        // verify if Tools JAR is a new version
        System.out.println("Verifying tools version...");
        boolean upToDate = true;
        try 
        {
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            inputStream = new BufferedInputStream(Thread.currentThread().getContextClassLoader().getResourceAsStream(toolsDirectory + "version"));

            byte[] versBuf = new byte[1024];
            while (true) 
            {
                int n = inputStream.read(versBuf);
                if (n < 0) break;
                arrayOutputStream.write(versBuf, 0, n);
            }

            Integer jarVersion = Integer.parseInt(new String(arrayOutputStream.toByteArray()));
            Integer localVersion = (Integer) ApplicationSettings.getProperty("toolsVersion", Integer.class);

            /* Our version of tools is lower? Skip. */
            if (localVersion < jarVersion)
            {
                ApplicationSettings.setProperty("toolsVersion", jarVersion, Integer.class); // Modify tools version in settings.ini
                System.out.println("New version of tools available! Extracting...");
                upToDate = false;
            }
            else
                System.out.println("Tools are already up to date.");
                

        } 
        catch (Exception e) 
        {
            System.out.println("Error reading tools version: " + e.getLocalizedMessage());
            e.printStackTrace();
            return;
        }

        boolean canSetExecutable = true;
        if (!upToDate)
        {
            // Update tools only if is new version
            for (String fileName : toolsFiles) {
                try {
                    inputStream = new BufferedInputStream(
                            Thread.currentThread().getContextClassLoader().
                            getResourceAsStream(toolsDirectory + fileName));

                    if (inputStream != null) {
                        file = new File((eugeneDir + toolsDirectory), fileName);

                        /* Trick to make executables work in Unix OS's. */
                        if (!fileName.contains("weights")) {
                            if (!file.setExecutable(true)) {
                                canSetExecutable = false;
                            }
                        }

                        if (file.exists()) {
                            file.delete();
                        }

                        buffer = new byte[2048];

                        bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));

                        for (;;) {
                            int nBytes = inputStream.read(buffer);
                            if (nBytes <= 0) {
                                break;
                            }
                            bufferedOutputStream.write(buffer, 0, nBytes);
                        }

                        bufferedOutputStream.flush();
                        bufferedOutputStream.close();


                        inputStream.close();
                    } else {
                        System.out.println("Error extracting tool file from JAR: " + fileName);
                    }

                } catch (IOException ioe) {
                    System.out.println("Error reading tools file.");
                    ioe.printStackTrace();
                }

            }
        }
        
        /* Second try at making tools executable. */
        if (!canSetExecutable && !isWindows())
        {
            String cmdArgs1[];
            
            if (isMac())
                cmdArgs1 = new String[]{"chmod", "0755", "musMac", "seq2mtxMac", "psipredMac", "psipass2Mac"}; //mac 
            else
                cmdArgs1 = new String[]{"chmod", "0755", "musNix", "seq2mtxNix", "psipredNix", "psipass2Nix"}; //linux
            try 
            {
                String path = eugeneDir + "Tools" + File.separator;
                Process p = Runtime.getRuntime().exec(cmdArgs1, null, new File(path));
                new StreamConsumer(p.getInputStream(), System.out).start();
                new StreamConsumer(p.getErrorStream(), System.out).start();
                p.waitFor();                
            } 
            catch (Exception ex) 
            {
                System.out.println("An error occured while trying to set permissions using chmod: " + ex.getLocalizedMessage());
            }
        }
        
        /* Confirm executability of tools. */
        if (!isWindows())
            for (String fileName : toolsFiles) 
            {
                if (fileName.contains("weights")) continue; //not executable
                if (isMac() && !fileName.contains("Mac")) continue; //on mac, and file not for mac
                if (isUnix() && !fileName.contains("Nix")) continue; //on linux, and file not for linux

                file = new File(eugeneDir + toolsDirectory + fileName);
                if (!file.canExecute())
                {
                    System.out.println(file.getAbsolutePath());
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "You do not have enought permissions to set Eugene's tools as executables.\nPlease go to the Tools folder and manually set all tools as executables.\nElse, 'Psipred' predictor and 'Muscle' aligner won't work.\n\nFollow these steps to solve:\n\na) Open terminal.\nb) Write: cd ~/EuGene/Tools\nc) Write: chmod 0755 *");
                    break;
                }
            }
        
        System.out.println("Done extracting tools from JAR!");
    }
    
    public static boolean isWindows()
    {

        String os = System.getProperty("os.name").toLowerCase();
        // windows
        return (os.indexOf("win") >= 0);

    }

    public static boolean isMac()
    {

        String os = System.getProperty("os.name").toLowerCase();
        // Mac
        return (os.indexOf("mac") >= 0);

    }

    public static boolean isUnix()
    {

        String os = System.getProperty("os.name").toLowerCase();
        // linux or unix
        return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

    }

    private static void showChangelogs() {
        Object changes = "Eugene was updated to a newer versions. The new version includes this new features:\n"
                + "*** Changelogs have been added\n"
                + "*** Calculates CAI from selected zone";
        
        JOptionPane.showMessageDialog(null, changes, "Updated to version " + version, JOptionPane.INFORMATION_MESSAGE);
    }
}