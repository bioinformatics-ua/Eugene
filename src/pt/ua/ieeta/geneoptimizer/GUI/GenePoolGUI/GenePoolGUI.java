/*
 * GenePoolGUI.java 
 *
 * Created on 4/Jan/2010, 16:46:35
 */
package pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI;

import java.awt.Color;
import java.awt.Toolkit;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import static javax.swing.JDialog.setDefaultLookAndFeelDecorated;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import pt.ua.ieeta.geneoptimizer.FileHandling.IGenomeFileParser;
import pt.ua.ieeta.geneoptimizer.FileHandling.SequenceValidator;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 * JFrame to show the user all available genes in the application and allow
 * loading genes to the projects
 *
 * @author Paulo Gaspar
 * @author Ricardo Gonzaga
  * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class GenePoolGUI extends javax.swing.JDialog implements Observer, Runnable {
    /* Singleton instance of GenePoolGUI. */

    private static volatile GenePoolGUI instance = null;

    /* Selected Genome. */
    private static List<GenomeHomePage> tabbedGenomes = null;

    /* Filtering text. */
    private static String filteringText = null;
    private static OpenGenomeWindow openGenomeWindow;
    private static boolean removeGeneLabel;

    /**
     * Creates new form GenePoolGUI
     */
    public static GenePoolGUI getInstance() {
        if (instance == null) {
            synchronized (GenePoolGUI.class) {
                if (instance == null) {
                    instance = new GenePoolGUI();

                    /*
                     * Create new vector to hold loaded genomes info.
                     */
                    tabbedGenomes = Collections.synchronizedList(new ArrayList<GenomeHomePage>());

                    instance.initComponents();

                    /* Create icon for the load genome button. */
                    ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainWindow.getInstance().getClass().getResource("/pt/ua/ieeta/geneoptimizer/resources/addNewGenomeIcon.png")).getScaledInstance(13, 13, 100)); //
                    loadGenomeBt.setIcon(icon);

                    /*
                     * Fill list of available(loaded) genomes.
                     */
                    instance.updateGenomeList();
                    setDefaultLookAndFeelDecorated(true);

                    /* Create the open genome dialog. */
                    openGenomeWindow = new OpenGenomeWindow();

                    /*
                     * Hide loading bar.
                     */
                    instance.showLoadingBar(false);

                    /* Hide "add gene" text button. */
                    jLabel3.setVisible(false);

                    /*
                     * Center the window.
                     */
                    instance.setLocationRelativeTo(null);

                    /*
                     * Set the filtering text and the text box to empty.
                     */
                    filteringText = "";
                    searchText.setText(filteringText);

                    removeGeneLabel = false;

                    /*
                     * Start a thread instance of this GUI.
                     */
                    new Thread(instance).start();
                }
            }
        }

        return instance;
    }

    public static int getSelectedCodeTableId() {
        assert openGenomeWindow != null;
        return openGenomeWindow.getSelectedCodeTableId();
    }

    /* Avoid instantiations. */
    private GenePoolGUI() {
    }

    public void setAddOrRemoveLabel(boolean isAdd) {
        if (isAdd) {
            jLabel3.setText("<html><u>Add a gene to this genome</u></html>");
            jLabel3.setForeground(Color.BLUE);
            removeGeneLabel = false;
        } else {
            jLabel3.setText("<html><u>Remove the selected custom gene</u></html>");
            jLabel3.setForeground(Color.RED);
            removeGeneLabel = true;
        }
    }

    @Override
    public void run() {
        /* Avoid accessing this method when it was already called. */
        if (Thread.currentThread().isAlive()) {
            return;
        }

        boolean isAlive = true;

        while (isAlive) {
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException ex) { //TODO: excepçoes
                    Logger.getLogger(GenePoolGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        /* End this thread. */
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        searchText = new javax.swing.JTextField();
        genomeContainer = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        loadGenomeBt = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        uploadGeneBt = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();

        setTitle("Gene Pool");
        setMinimumSize(new java.awt.Dimension(750, 300));
        setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        getContentPane().setLayout(new javax.swing.BoxLayout(getContentPane(), javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 0, 0));
        jPanel3.setMaximumSize(new java.awt.Dimension(32767, 25));
        jPanel3.setMinimumSize(new java.awt.Dimension(700, 25));
        jPanel3.setPreferredSize(new java.awt.Dimension(700, 25));
        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 10, 0));

        jLabel3.setForeground(new java.awt.Color(0, 51, 255));
        jLabel3.setText("<html><u>Add a gene to this genome</u></html>");
        jLabel3.setMinimumSize(new java.awt.Dimension(129, 14));
        jLabel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel3MouseClicked(evt);
            }
        });
        jPanel3.add(jLabel3);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Search for a gene in this genome:");
        jLabel2.setPreferredSize(new java.awt.Dimension(262, 14));
        jPanel3.add(jLabel2);

        searchText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        searchText.setToolTipText("Filter the available genes by name. Enter the gene name you are looking for.");
        searchText.setMinimumSize(new java.awt.Dimension(100, 20));
        searchText.setPreferredSize(new java.awt.Dimension(100, 20));
        searchText.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                searchTextKeyReleased(evt);
            }
        });
        jPanel3.add(searchText);

        getContentPane().add(jPanel3);

        genomeContainer.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        genomeContainer.setTabPlacement(javax.swing.JTabbedPane.LEFT);
        genomeContainer.setMinimumSize(new java.awt.Dimension(700, 250));
        genomeContainer.setPreferredSize(new java.awt.Dimension(700, 250));
        genomeContainer.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                genomeContainerStateChanged(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 0, 0));
        jPanel2.setMaximumSize(new java.awt.Dimension(2147483647, 242343));
        jPanel2.setMinimumSize(new java.awt.Dimension(700, 190));
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(700, 190));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.Y_AXIS));

        jLabel7.setText("<html>This is the gene pool, where all loaded genomes and corresponding genes are listed.<br/> To start, follow these steps:<br/><br/>1 - First click the \"<b>Load genome</b>\" button below. A new window will pop up.<br/>2 - Select the <u>genetic code</u> of the genome to open, from the drop-down menu in the new window. <br/>3 -  Then select, in the check-boxes, which filters to apply. These will allow ignoring unwanted genes.<br/>4 - Finally, click the \"<b>Open genome</b>\" button and select a genome file to open.<br/><br/>The genome file should be in a supported format (FASTA nucleotide, GenBank).<br/>You can get genome files from here:</html>");
        jPanel2.add(jLabel7);

        jLabel1.setForeground(new java.awt.Color(0, 0, 255));
        jLabel1.setText("<html><u>ftp://ftp.ncbi.nih.gov/genbank/genomes/</u></html>");
        jLabel1.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jLabel1MouseClicked(evt);
            }
        });
        jPanel2.add(jLabel1);

        genomeContainer.addTab("Welcome", jPanel2);

        getContentPane().add(genomeContainer);
        genomeContainer.getAccessibleContext().setAccessibleName("Welcome");

        jPanel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 5, 5));
        jPanel1.setMaximumSize(new java.awt.Dimension(1000, 30));
        jPanel1.setMinimumSize(new java.awt.Dimension(784, 30));
        jPanel1.setPreferredSize(new java.awt.Dimension(700, 30));
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        loadGenomeBt.setText("Load genome");
        loadGenomeBt.setIconTextGap(6);
        loadGenomeBt.setMaximumSize(new java.awt.Dimension(160, 29));
        loadGenomeBt.setMinimumSize(new java.awt.Dimension(160, 29));
        loadGenomeBt.setPreferredSize(new java.awt.Dimension(160, 29));
        loadGenomeBt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGenomeBtActionPerformed(evt);
            }
        });
        jPanel1.add(loadGenomeBt);
        jPanel1.add(jPanel4);

        uploadGeneBt.setText("Upload selected gene to workspace");
        uploadGeneBt.setToolTipText("Click to upload the selected gene to the workspace");
        uploadGeneBt.setEnabled(false);
        uploadGeneBt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadGeneBtActionPerformed(evt);
            }
        });
        jPanel1.add(uploadGeneBt);

        jButton1.setText("Close window");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        getContentPane().add(jPanel1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * When a tab is selected.
     */
    private void genomeContainerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_genomeContainerStateChanged

        assert tabbedGenomes != null;
        assert genomeContainer != null;
        if (tabbedGenomes.isEmpty()) {
            return;
        }

        int selectedIndex = genomeContainer.getSelectedIndex();
        filteringText = tabbedGenomes.get(selectedIndex).getLastSearchingText();
        searchText.setText(filteringText);
        tabbedGenomes.get(selectedIndex).filterTable();

        /* If the table in this tab has no selected row, disable the upload button. */
        if (tabbedGenomes.get(selectedIndex).getGenesTable().getSelectedRow() == -1) {
            uploadGeneBt.setEnabled(false);
        } else {
            uploadGeneBt.setEnabled(true);
        }

//        Genome g = tabbedGenomes.get(selectedIndex).getGenome();
        //jLabel5.setText("<html># of Rejected genes: <b>" + g.getNumRejectedGenes() + "</b></html>");
    }//GEN-LAST:event_genomeContainerStateChanged

    private void jLabel3MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jLabel3MouseClicked
    {//GEN-HEADEREND:event_jLabel3MouseClicked
        assert genomeContainer != null;

        int selectedTab = genomeContainer.getSelectedIndex();
        if (selectedTab < 0) {
            return;
        }

        Genome selectedGenome = tabbedGenomes.get(selectedTab).getGenome();

        if (!removeGeneLabel) {

            /*
             * Ask gene name.
             */
            String geneName = (String) JOptionPane.showInputDialog(null, "Please give a name for the gene", "Gene name", JOptionPane.QUESTION_MESSAGE, null, null, "new Gene");

            /*
             * When cancel pressed.
             */
            if (geneName == null) {
                return;
            }

            /* Empty gene name. */
            if (geneName.isEmpty()) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "An empty name for the new gene is not allowed.", "Empty gene name", JOptionPane.ERROR_MESSAGE);
                return;
            }

            /*
             * Ask gene sequence.
             */
            String geneSequence, plusMessage = "";
            String lastInput = "new RNA nucleotide sequence";
            while (true) {
                geneSequence = (String) JOptionPane.showInputDialog(null, "<html>" + plusMessage + "Please provide the mRNA nucleotide sequence</html>", "", JOptionPane.QUESTION_MESSAGE, null, null, lastInput);

                /*
                 * When cancel pressed.
                 */
                if (geneSequence == null) {
                    return;
                }

                int validationError = SequenceValidator.isValidCodonSequence(geneSequence, selectedGenome.getGeneticCodeTable(), selectedGenome.getFilters());

                if (validationError != 0) {
                    plusMessage = "Invalid sequence: <b>" + SequenceValidator.getValidationErrorMessage(validationError) + "</b>.<br/>";
                } else {
                    break;
                }

                lastInput = geneSequence;
            }

            /*
             * Make final corrections to sequence.
             */
            String finalSequence = SequenceValidator.makeCorrectionsToGene(geneSequence);

            /*
             * Create gene.
             */
            Gene newGene = new Gene(geneName, selectedGenome);
            newGene.createStructure(finalSequence, pt.ua.ieeta.geneoptimizer.geneDB.BioStructure.Type.mRNAPrimaryStructure);

            selectedGenome.addGeneManually(newGene);

            tabbedGenomes.get(selectedTab).fillGenesTable();
        } else {
            /* IS remove gene */
            if (tabbedGenomes.get(selectedTab).removeSelectedGene()) {
                setAddOrRemoveLabel(true);
            }

        }
    }//GEN-LAST:event_jLabel3MouseClicked

    private void uploadGeneBtActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_uploadGeneBtActionPerformed
    {//GEN-HEADEREND:event_uploadGeneBtActionPerformed
        assert genomeContainer != null;

        /* Start a new thread to upload the gene. */
        new Thread(new Runnable() {
            @Override
            public void run() {
                uploadGeneBt.setEnabled(false);

                int selectedTab = genomeContainer.getSelectedIndex();

                if ((selectedTab >= 0) && (selectedTab < tabbedGenomes.size())) {
                    tabbedGenomes.get(selectedTab).uploadSelectedGeneToWorkSpace();
                }

                uploadGeneBt.setEnabled(true);
            }
        }).start();

    }//GEN-LAST:event_uploadGeneBtActionPerformed

    private void loadGenomeBtActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadGenomeBtActionPerformed
    {//GEN-HEADEREND:event_loadGenomeBtActionPerformed
        openGenomeWindow.setLocationRelativeTo(null);
        openGenomeWindow.setVisible(true);
    }//GEN-LAST:event_loadGenomeBtActionPerformed

    private void jLabel1MouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_jLabel1MouseClicked
    {//GEN-HEADEREND:event_jLabel1MouseClicked
        try {
            java.awt.Desktop.getDesktop().browse(new URI("ftp://ftp.ncbi.nih.gov/genbank/genomes/"));

        } catch (Exception ex) {
            System.out.println("Error while trying to open NCBI ftp server for genomes: " + ex.getMessage());
        }
    }//GEN-LAST:event_jLabel1MouseClicked

    private void searchTextKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_searchTextKeyReleased
    {//GEN-HEADEREND:event_searchTextKeyReleased
        int selectedIndex = genomeContainer.getSelectedIndex();
        if (tabbedGenomes != null) {
            if (tabbedGenomes.size() > selectedIndex) {
                tabbedGenomes.get(selectedIndex).filterTable();
            }
        }
    }//GEN-LAST:event_searchTextKeyReleased

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        getInstance().setVisible(false);
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane genomeContainer;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private static javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private static javax.swing.JButton loadGenomeBt;
    private static javax.swing.JTextField searchText;
    private static javax.swing.JButton uploadGeneBt;
    // End of variables declaration//GEN-END:variables

    @Override
    public void update(Observable o, Object arg) {
        /* If GenePool notified this thread, then update the genome list. */
        if (o instanceof GenePool) {
            updateGenomeList();
        } else if (o instanceof IGenomeFileParser) {
            if (!(arg instanceof GenePoolObserverMessage)) {
                return;
            }

            GenePoolObserverMessage message = (GenePoolObserverMessage) arg;
            switch (message.getMessageType()) {
                case LOADING_GENOME:
                    showLoadingBar(true);
                    openGenomeWindow.getProgressLabel().setText("Reading genome file...");
                    openGenomeWindow.getProgressBar().setIndeterminate(true);
                    break;

                case PARSING_GENOME:
                    showLoadingBar(true);
                    openGenomeWindow.getProgressBar().setIndeterminate(false);
                    openGenomeWindow.getProgressLabel().setText("Parsing genes...");
                    openGenomeWindow.getProgressBar().setMaximum((Integer) message.getInformation());
                    openGenomeWindow.getProgressBar().setValue(0);
                    break;

                case UPDATE_PROGRESS:
                    openGenomeWindow.getProgressBar().setValue(openGenomeWindow.getProgressBar().getValue() + Math.round(((Float) message.getInformation())));
                    break;

                case LOAD_COMPLETE:
                    showLoadingBar(false);
                    openGenomeWindow.setVisible(false);
                    break;
            }

        }
    }

    public static JButton getUploadGeneButton() {
        return uploadGeneBt;
    }

    public static JTextField getSearchLabel() {
        return searchText;
    }

    public static String getFilteringString() {
        return filteringText;
    }

    /* Function to update list of genomes in genome pool GUI. */
    private void updateGenomeList() {
        if (!GenePool.getInstance().getGenomes().isEmpty()) {
            /* Create new genome page. */
            GenomeHomePage ghp = new GenomeHomePage(GenePool.getInstance().getLastOpenGenome());

            /* Fill it with the genes. */
            ghp.fillGenesTable();

            /* Remove default tab. */
            if (tabbedGenomes.isEmpty()) {
                genomeContainer.remove(0);
                jLabel3.setVisible(true);
            }

            /* Save reference. */
            tabbedGenomes.add(ghp);

            /* Create icon */
            ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(MainWindow.getInstance().getClass().getResource("/pt/ua/ieeta/geneoptimizer/resources/genomeIcon.png")).getScaledInstance(39, 25, 100)); //

            /* Add tab to GUI. */
            genomeContainer.addTab(GenePool.getInstance().getLastOpenGenome().getName(), icon, ghp.getScrollPane());
        }
    }

    private void showLoadingBar(boolean flag) {
        openGenomeWindow.getProgressLabel().setVisible(flag);
        openGenomeWindow.getProgressBar().setVisible(flag);
    }

    public static void enableButtonLoadGenomes(boolean flag) {
        openGenomeWindow.getLoadButton().setEnabled(flag);
    }
    
    public void updateGenome(Genome temp) {
        Iterator it = tabbedGenomes.listIterator();
        while(it.hasNext()) {
            GenomeHomePage update = (GenomeHomePage) it.next();
            if(temp.getName().equals(update.getGenome().getName())) {
                update.fillGenesTable();
                return;
            }
        }
    }
}
