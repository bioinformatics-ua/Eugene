package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.swing.*;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.biojava.bio.structure.io.PDBFileReader;
import org.jmol.adapter.smarter.SmarterJmolAdapter;
import org.jmol.api.JmolAdapter;
import org.jmol.api.JmolSimpleViewer;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class Protein3DViewerPanel extends ContentPanel implements Observer, Runnable
{

    private static PDBFileReader pdbr;
    private JmolPanel jmolPanel;
    private String newCode;
    private boolean isShowingProtein = false;
    private JmolSimpleViewer viewer;
    private JLabel textLink;
    private boolean isDetached = false;
    private JFrame newFrame;
    private static volatile Protein3DViewerPanel instance = null;

    public static Protein3DViewerPanel getInstance()
    {
        if (instance == null)
            synchronized(Protein3DViewerPanel.class){
                if (instance == null){
                    instance = new Protein3DViewerPanel();
                }
            }
            
        return instance;
    }

    private Protein3DViewerPanel()
    {
        super("Protein Viewer", false);

        /* Set a vertical layout. */
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        /* Create JMol panel. */
        jmolPanel = new JmolPanel();
        jmolPanel.setPreferredSize(new Dimension(250, 250));
        this.add(jmolPanel);

        /* Create detaching button. */
        textLink = new JLabel("<html><U>Detach this panel</U></HTML>", JLabel.CENTER);
        textLink.setForeground(new Color(0, 0, 153));
        textLink.addMouseListener(new MouseListener()
        {

            @Override
            public void mouseClicked(MouseEvent me)
            {
            }

            @Override
            public void mousePressed(MouseEvent me)
            {
            }

            @Override
            public void mouseReleased(MouseEvent me)
            {
                detachWindow(!isDetached);
            }

            @Override
            public void mouseEntered(MouseEvent me)
            {
            }

            @Override
            public void mouseExited(MouseEvent me)
            {
            }
        });
        textLink.setVisible(false);
        this.add(Box.createHorizontalGlue());
        this.add(textLink);
        this.add(Box.createHorizontalGlue());

        /* Create PDB file reader. */
        // TODO not working at this moment: need to be reviewed and tested again
        pdbr = new PDBFileReader();
        pdbr.setPath("PDBFiles");
        pdbr.setAutoFetch(true);
        pdbr.setParseSecStruc(true);

        /* Create PDB viewer. */
        viewer = jmolPanel.getViewer();

        /* jMol parameters. Turn off output to console. */
        viewer.evalString("set showScript OFF; set logLevel 0; set debugScript OFF; set scriptReportingLevel -1; set antialiasDisplay ON;");

        newCode = null;
        setPDBCode(newCode);
    }

    public synchronized void updateViewerForSelectedStudy()
    {
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null)
            setPDBCode(null);
        else
            setPDBCode(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene().getPDBCode());
    }

    public synchronized void setPDBCode(String PDBcode)
    {
        if (PDBcode == null)
        {
            viewer.evalString("hide *; background [240,240,240]; font echo 12 tahoma bold; set echo top center; color echo black; echo No protein to display");
            jmolPanel.setPreferredSize(new Dimension(250, 40));

            isShowingProtein = false;

            updateUI();

            return;
        }

        try
        {
            String PDB;
            jmolPanel.setPreferredSize(new Dimension(250, 250));
            PDB = retrievePDBfile(PDBcode); //pdbr.getStructureById(PDBcode).toPDB(); //
            viewer.openStringInline(PDB);

            viewer.evalString("select *; color structure; background black; zoom 150; cartoon only; spin on;");

//            viewer.evalString("color structure; background black; zoom 150; cartoon only;");
//            viewer.evalString("select *; color gray; select (1, 2, 3); color red; cartoon 100;"); // set display selected;
            isShowingProtein = true;

            textLink.setVisible(true);

            updateUI();
        } catch (Exception ex)
        { //TODO: excepçoes
            Logger.getLogger(Protein3DViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
            displayErrorMessage();
            textLink.setVisible(false);
        }
    }

    public void selectAminoAcid(int aminoAcidIndexStart, int aminoAcidIndexEnd)
    {
        if (!isShowingProtein)
            return;

        StringBuilder builder = new StringBuilder();
        for (int i = Math.min(aminoAcidIndexStart, aminoAcidIndexEnd); i <= Math.max(aminoAcidIndexStart, aminoAcidIndexEnd); i++)
            builder.append(Integer.toString(i + 1)).append(",");

        builder.deleteCharAt(builder.length() - 1);

        jmolPanel.getViewer().evalString("select *; color gray; select " + builder.toString() + "; color red; cartoons;");
        //jmolPanel.getViewer().evalString("select *; color translucent; select "+builder.toString()+"; color red; cartoons;");
    }

    public void unselectAll()
    {
        if (!isShowingProtein)
            return;

        jmolPanel.getViewer().evalString("select *; color structure; cartoon only;");
    }

    private void displayErrorMessage()
    {
        viewer.evalString("hide *; background [240,240,240]; font echo 13 tahoma bold; set echo top center; color echo red; echo Error displaying protein");
        jmolPanel.setPreferredSize(new Dimension(250, 40));
        ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene().setPDBCode(null);
        updateUI();
    }

    private String retrievePDBfile(String pdbCode)
    {
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String localFileName = eugeneDir + File.separator + "PDBFiles" + File.separator + pdbCode.toLowerCase() + ".pdb.gz";
        File tempFile = new File(localFileName);


        if (!tempFile.exists())
            // file doesn't exists
            downloadPDBFileFromFTP(pdbCode, localFileName);

        // read local file
        System.out.println("PDB: Read local file");
        StringBuilder sb = new StringBuilder();

        try
        {
            FileInputStream is = new FileInputStream(localFileName);
            GZIPInputStream gzipis = new GZIPInputStream(is);
            InputStreamReader reader = new InputStreamReader(gzipis);
            BufferedReader buffered = new BufferedReader(reader);

            String line;
            while ((line = buffered.readLine()) != null)
                sb.append(line).append("\n");
            gzipis.close();
        } catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * Download the pdb file from EBI server and save it localy
     *
     * @param pdbCode          the code of pdb file to download (not correspond to the name file)
     * @param fileCompleteName name of the local file where to save
     */
    private void downloadPDBFileFromFTP(String pdbCode, String fileCompleteName)
    {
        System.out.println("PDB: File doesn't exist localy: downloading filefrom ftp.");

        String ftpServer = "ftp.ebi.ac.uk";
        String workingDirectory = "pub/databases/msd/pdb_uncompressed";
        String fileName = "pdb" + pdbCode.toLowerCase() + ".ent";
        FTPClient ftp = null;

        /* FTP TRANSFERE */
        try
        {
            // CONNECT TO FTP
            ftp = new FTPClient();
            ftp.connect(ftpServer);

            if (!FTPReply.isPositiveCompletion(ftp.getReplyCode()))
            {
                System.out.println("PDB FTP download: Connection Failed.");
                return;
            }

            if (!ftp.login("anonymous", ""))
            {
                System.out.println("PDB FTP download: Login as anonymous failed.");
                return;
            }

            if (!ftp.changeWorkingDirectory(workingDirectory))
            {
                System.out.println("PDB FTP download: Failed to change working directory");
                return;
            }

            /* Write to file. */
            FileOutputStream outPut = new FileOutputStream(fileCompleteName);
            BufferedOutputStream bos = new BufferedOutputStream(outPut);
            GZIPOutputStream gzOutPut = new GZIPOutputStream(bos);

            // to avoid firewall problems
            ftp.enterLocalPassiveMode();

            if (!ftp.retrieveFile(fileName, gzOutPut))
            {
                System.out.println("PDB FTP download: Failed to retrieve file.");
                return;
            }

            gzOutPut.finish();
            gzOutPut.close();
        } catch (IOException ex)
        {
            System.out.println("PDB FTP download: Error while connecting to ftp EBI server: " + ex.getMessage());
        } finally
        {
            try
            {
                ftp.disconnect();
            } catch (IOException e)
            {
                System.out.println("PDB ftp download: Exception finally");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run()
    {
        while (true)
        {
            synchronized (this)
            {
                try
                {
                    wait();
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(Protein3DViewerPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
                //TODO: excepçoes
            }

            setPDBCode(newCode);
        }
    }

    @Override
    public void update(Observable o, Object arg)
    {
        assert arg != null;

        Study study = (Study) arg;

        if (study.isMultiGeneStudy())
        {
            newCode = null;
            synchronized (this)
            {
                notifyAll();
            }
            return;
        }

        /* Change protein being shown. */
        if ((study.getResultingGene().getPDBCode() == null) || (!study.getResultingGene().getPDBCode().equals(newCode)))
        {
            newCode = study.getResultingGene().getPDBCode();
            synchronized (this)
            {
                notifyAll();
            }
        }

    }

    static class JmolPanel extends JPanel
    {

        JmolSimpleViewer iViewer;
        JmolAdapter adapter;

        public JmolPanel()
        {
            adapter = new SmarterJmolAdapter();
            iViewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
        }

        public JmolSimpleViewer getViewer()
        {
            return iViewer;
        }

        public void executeCmd(String rasmolScript)
        {
            iViewer.evalString(rasmolScript);
        }
        final Dimension currentSize = new Dimension();
        final Rectangle rectClip = new Rectangle();

        @Override
        public void paint(Graphics g)
        {
            getSize(currentSize);
            g.getClipBounds(rectClip);
            iViewer.renderScreenImage(g, currentSize, rectClip);
        }
    }

    private void detachWindow(boolean detach)
    {
        if (detach)
        {
            if (newFrame == null)
            {
                newFrame = new JFrame("Protein Viewer");

                newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                newFrame.setAlwaysOnTop(true);
                
                newFrame.addWindowListener(new WindowListener()
                {

                    public void windowClosing(WindowEvent we)
                    {
                        detachWindow(false);
                    }

                    public void windowClosed(WindowEvent we)
                    {
                    }

                    public void windowOpened(WindowEvent we)
                    {
                    }

                    public void windowIconified(WindowEvent we)
                    {
                    }

                    public void windowDeiconified(WindowEvent we)
                    {
                    }

                    public void windowActivated(WindowEvent we)
                    {
                    }

                    public void windowDeactivated(WindowEvent we)
                    {
                    }
                });
            }

            textLink.setText("<html><FONT color=\"#000099\"><U>Reattach this panel</U></font></HTML>");
            isDetached = true;

            MainWindow.getInstance().toggleContentPanelVisibility(1, this, false);

            /* Add contents and show window. */
            newFrame.add(getInstance());
            newFrame.pack();
            newFrame.setVisible(true);
        } else
        {
            /* Clear detached window. */
            if (newFrame != null)
                newFrame.dispose();

            textLink.setText("<html><FONT color=\"#000099\"><U>Detach this panel</U></font></HTML>");
            isDetached = false;

            /* Hack to make this work. Somehow, when placing this instance in a new frame, the content panel gets empty. So we re-add this to the content panel. */
            getInstance().getContentPanel().add(this);

            /* Place the Protein Viewer in its default place. */
            MainWindow.getInstance().toggleContentPanelVisibility(1, this, true);
        }
    }


    /* TESTE */
    public static void main(String[] args)
    {

        Protein3DViewerPanel ex = new Protein3DViewerPanel();
        ex.setPDBCode("1ktv");

        JFrame newFrame = new JFrame();
        newFrame.getContentPane().add(ex);

        newFrame.pack();
        newFrame.setVisible(true);
    }
}