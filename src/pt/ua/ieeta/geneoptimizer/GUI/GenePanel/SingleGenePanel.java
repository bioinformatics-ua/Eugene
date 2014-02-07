/*
 * SingleGenePanel.java
 */
package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import pt.ua.ieeta.geneoptimizer.ExternalTools.Psipred;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.FileHandling.SequenceValidator;
import pt.ua.ieeta.geneoptimizer.GUI.ContentPanel;
import pt.ua.ieeta.geneoptimizer.GUI.GeneInformationPanel;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.GUI.MessageWindow;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationModel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.PluginSystem.IOptimizationPlugin;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.PluginSystem.PluginLoader;
import pt.ua.ieeta.geneoptimizer.geneDB.*;

/**
 * Extends ContentPanel to show one sequence
 *
 * @author Paulo Gaspar
 */
public final class SingleGenePanel extends ContentPanel implements ClipboardOwner, MouseListener, Observer {

    /**
     * The gene in this panel.
     */
    private Study study;
    /**
     * This box will take the content directly related to the gene, such as its
     * sequences, protein secondary structure and orthologs.
     */
    private InteriorContentPanel mainContentBox;
    /**
     * This box will take the gene's ortologs.
     */
//    private InteriorContentPanel orthologsBox;
    private boolean showOrthologs;
    private boolean showProtSecondStruct;
    private boolean protSecondStructCalculated;
    private boolean isDetached;

    /* Popup menu of this panel.  */
    private JPopupMenu popup;

    /* Selected structure type to be displayed. */
    private BioStructure.Type orthologSelectedStructureType;

    /* Selected color schemes for main gene and orthologs. */
    private IOptimizationPlugin geneColorSchemePlugin;
    private ParameterSet geneColorSchemeParameters;
    private List<List<Color>> orthologsColorScheme = null;
    /* Extra gene to align its AA sequence with this gene AA sequence and find differences. */
    private Gene extraGene;
    private JFrame detachFrame;
    /* Menu for detached window */
    private JMenuBar detachedWindowMenu;

    /**
     * Constructor to SingleGenePanel
     *
     * @param study study to show
     * @param isDesign If is design or not
     */
    public SingleGenePanel(Study study, boolean isDesign) {
        super(study == null ? "" : study.getName(), true, isDesign ? 1 : 0);

        /* Fail proof. */
        if (study == null) {
            super.deletePanel();
            return;
        }

        super.getContentPanel().setStudy(study);

        this.study = study;
        this.showOrthologs = false;
        this.showProtSecondStruct = false;
        this.protSecondStructCalculated = false;
        this.isDetached = false;
        this.orthologSelectedStructureType = BioStructure.Type.proteinPrimaryStructure;
//        this.orthologsBox = null;
        this.geneColorSchemePlugin = (IOptimizationPlugin) PluginLoader.getInstance().getPluginInstanceByName("CodonUsagePlugin");

        /* Create a new box layout to dispose components. */
        BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
        layout.preferredLayoutSize(this);
        this.setLayout(layout);

        /* Make main interior content panel. */
        remakePanel();

        /* Create context menu. */
        popup = new JPopupMenu();
        buildContextMenu(popup);

        Cursor cursor = new Cursor(Cursor.HAND_CURSOR);
        this.setCursor(cursor);

        /* Mouse listener to enable selection of this panel in free spaces of the panel. */
//        addMouseListener(this);
        //addMouseMotionListener(this);
    }

    @Override
    public void deletePanel() {
        mainContentBox.getScrollingPane().setHorizontalScrollBar(null);
//        if (orthologsBox != null)
//            orthologsBox.getScrollingPane().setHorizontalScrollBar(null);
    }

    @Override
    public synchronized void setPadding(int finalSize) {
        assert mainContentBox != null;

        for (SequencePanel lsp : mainContentBox.getSequencePanels()) {
            lsp.setPaddingLabels(finalSize);
        }

//        if (showOrthologs && (orthologsBox != null))
//            for (SequencePanel lsp: orthologsBox.getSequencePanels())
//                lsp.setPaddingLabels(finalSize);
    }

    /**
     * Get the number of characters in the longest sequence in this panel.
     */
    @Override
    public synchronized int getMaxCharacters() {
        assert mainContentBox != null;

        return mainContentBox.getMaxCharacters();
    }

    @Override
    public synchronized void remakePanel() {
        /* Backup current contents, just in case something fails. */
        InteriorContentPanel content = this.mainContentBox;

        /* clan panel */
        this.removeAll();

        try {
            /* Create main interior panel, with genes sequence. */
            List<GenePanelEntry> entries = Collections.synchronizedList(new ArrayList<GenePanelEntry>(2));

            /* Main codon sequence entry. */
            List<Color> codonColorScheme = (geneColorSchemePlugin != null) ? geneColorSchemePlugin.makeAnalysis(study, showOrthologs) : null;
            GenePanelEntry codonEntry = new GenePanelEntry(study.getResultingGene().getName(), study.getResultingGene(), codonColorScheme, BioStructure.Type.mRNAPrimaryStructure, GenePanelEntry.EntryType.MAIN_CODON_SEQ);
            entries.add(codonEntry);

            /* Main amino acid sequence entry. */
            if (!isDetached) {
                GenePanelEntry aaEntry = new GenePanelEntry("", study.getResultingGene(), null, BioStructure.Type.proteinPrimaryStructure, GenePanelEntry.EntryType.MAIN_AA_SEQ);
                entries.add(aaEntry);
            }

            /* Extra amino acid sequence to align and compare with the normal amino acid sequence. */
            if (!isDetached && (extraGene != null) && (!showOrthologs)) {
                GenePanelEntry extraAA = new GenePanelEntry("", extraGene, SequencePaintingPool.getInstance().getAADifferences(extraGene.getAminoacidSequence(), study.getResultingGene().getAminoacidSequence()), BioStructure.Type.proteinPrimaryStructure, GenePanelEntry.EntryType.SECOND_AA_SEQ);
                entries.add(extraAA);
            }

            /* Protein secondary structure. */
            if (!isDetached && showProtSecondStruct && study.getResultingGene().hasSequenceOfType(BioStructure.Type.proteinSecondaryStructure)) {
                GenePanelEntry secEntry = new GenePanelEntry("", study.getResultingGene(), null, BioStructure.Type.proteinSecondaryStructure, GenePanelEntry.EntryType.MAIN_SEC_STRUCT);
                entries.add(secEntry);
            }

            /* Orthologs. */
            if (showOrthologs && !isDetached && (study.getResultingGene().hasOrthologs() || study.getOriginalGene().hasOrthologs())) {
                /* Get ortholog list from study gene, or from original gene. */
                Genome orthologList = null;
                if (study.getResultingGene().hasOrthologs()) {
                    orthologList = study.getResultingGene().getOrthologList();
                } else if (study.getOriginalGene().hasOrthologs()) {
                    orthologList = study.getOriginalGene().getOrthologList();
                }

                /* Build sequence panel entries based on ortholog list. */
                if (orthologList != null) {
                    for (int i = 0; i < orthologList.getGenes().size(); i++) {
                        Gene gene = orthologList.getGenes().get(i);
                        GenePanelEntry secEntry = new GenePanelEntry(gene.getGenomeName(), gene, orthologsColorScheme.get(i), orthologSelectedStructureType, GenePanelEntry.EntryType.ORTHOLOG);
                        entries.add(secEntry);
                    }
                }
            }

            /* Create or recreate main interior panel. */
            if (this.mainContentBox != null) {
                this.mainContentBox.setNewInfo(entries, showOrthologs, showProtSecondStruct, true, isDetached);
            } else {
                this.mainContentBox = new InteriorContentPanel(study, this, entries, showOrthologs, showProtSecondStruct, true, isDetached);
            }

            content = mainContentBox;
        } catch (Exception e) {
            System.out.println("An error occured while remaking the gene panel: " + e.getLocalizedMessage());
        }

        this.add(content);
        this.add(Box.createHorizontalGlue());

        this.repaint();
        ProjectManager.getInstance().getSelectedProject().getContainerPanel().updatePanelsPadding();
        updateUI();
    }

    /**
     * Public method to allow JTaskPaneGroup detach the panel
     *
     * @param detach
     */
    public void detachWindow(boolean detach) {
        /* Detach the window */
        if (detach) {
            isDetached = true;


            if (detachFrame == null) {
                detachFrame = new JFrame("Gene Viewer - " + study.getName());

                /* set menu bar */
                detachedWindowMenu = new JMenuBar();
                JMenu menu = new JMenu("File");
                JMenuItem export = new JMenuItem("Export as image");
                export.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        File file = saveFileDialog();
                        
                        if (file != null) {
                        
                            try {                                
                                BufferedImage i = new BufferedImage(detachFrame.getBounds().width, detachFrame.getBounds().height, BufferedImage.TYPE_INT_RGB);
                                Graphics2D g = i.createGraphics();
                                g.setColor(detachFrame.getForeground());
                                g.setFont(detachFrame.getFont());
                                detachFrame.paintAll(g);
                                g.dispose();
                                ImageIO.write(i, "png", file);                                
                                JOptionPane.showMessageDialog(detachFrame, "The file '" + file.getName() + "' was successful saved!");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                });
                menu.add(export);

                JMenuItem quit = new JMenuItem("Quit");
                quit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        detachWindow(false);
                    }
                });
                menu.add(quit);

                detachedWindowMenu.add(menu);
                detachFrame.setJMenuBar(detachedWindowMenu);

                detachFrame.setPreferredSize(new Dimension(400, 250));
                detachFrame.setLocationRelativeTo(null); // center frame
                detachFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                detachFrame.setAlwaysOnTop(true);
                detachFrame.addWindowListener(new WindowListener() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        detachWindow(false);
                    }

                    @Override
                    public void windowClosed(WindowEvent we) {
                    }

                    @Override
                    public void windowOpened(WindowEvent we) {
                    }

                    @Override
                    public void windowIconified(WindowEvent we) {
                    }

                    @Override
                    public void windowDeiconified(WindowEvent we) {
                    }

                    @Override
                    public void windowActivated(WindowEvent we) {
                    }

                    @Override
                    public void windowDeactivated(WindowEvent we) {
                    }
                });
            }


            // remove from main window
            ProjectManager.getInstance().getSelectedProject().getContainerPanel().removeContentPanel(this);
            /* Add contents to panel. */
            showOrthologs = false;
            showProtSecondStruct = false;
            this.remakePanel();

            // add this content to new window (frame)
            this.detachFrame.add(this);
            detachFrame.pack();
            detachFrame.setVisible(true);
            this.repaint();
            updateUI();
        } /* Reattach the window */ else {
            isDetached = false;

            /* Clear detached window. */
            if (detachFrame != null) {
                detachFrame.dispose();
            }

            this.remakePanel();

            /* Hack to make this work. 
             * Somehow, when placing this instance in a new frame, the content panel 
             * gets empty. So we re-add this to the content panel. */
            this.getContentPanel().add(this);

            /* Place the Protein Viewer in its default place. */
            ProjectManager.getInstance().getSelectedProject().getContainerPanel().addContentPanel(this);
            this.repaint();
        }
    }

    private File saveFileDialog() {
        File saveImage = null;
        String defaultName = study.getResultingGene().getGenome().getSmallName() + "_" + Calendar.getInstance().getTime().toString().substring(4, 16);
        defaultName = defaultName.replace(' ', '_');
        defaultName = defaultName.replace(':', '_');

        // chose file to save project
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String projectsPath = (String) ApplicationSettings.getProperty("imagesPath", String.class);

        JFileChooser chooser = new JFileChooser(eugeneDir + projectsPath) {
            @Override
            public void approveSelection() {
                File f = getSelectedFile();
                if (f.exists() && getDialogType() == SAVE_DIALOG) {
                    int result = JOptionPane.showConfirmDialog(this, "The file exists, overwrite?", "Existing file", JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result) {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection();
                            return;
                    }
                }
                super.approveSelection();
            }
        };
        FileFilter filter = new FileNameExtensionFilter("Portable Network Graphics", "png");
        chooser.setFileFilter(filter);

        chooser.setSelectedFile(new File(eugeneDir + projectsPath, defaultName + ".png"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            saveImage = chooser.getSelectedFile();
        }
        if (!saveImage.getAbsolutePath().endsWith(".png")){            
            saveImage = new File(eugeneDir + projectsPath, chooser.getSelectedFile().getName() + ".png");
        }               
        return saveImage;
    }

    /* Return study of this panel. */
    public Study getStudy() {
        return study;
    }

    /* Returns the plugin currently being used to colour the codon sequence. */
    public IOptimizationPlugin getColouringPlugin() {
        return geneColorSchemePlugin;
    }

    /**
     * **********************************************
     */
    /*                 POPUP STUFF                   */
    /**
     * **********************************************
     */
    public void copyCodonSequenceToClipboard() {
        copyStructureToClipBoard(BioStructure.Type.mRNAPrimaryStructure);
    }

    public void copyAminoacidSequenceToClipboard() {
        copyStructureToClipBoard(BioStructure.Type.proteinPrimaryStructure);
    }

    /**
     * Show or hide Protein Secondary Structure At first time calculate the
     * protein secondary structure usng Psipred
     */
    public void showHideProteinSecondaryStructure() {
        showProtSecondStruct = !showProtSecondStruct;

        if (!protSecondStructCalculated) {
            System.out.println("Calculating Protein Secondary Structure");
            new Psipred(study.getResultingGene().getAminoacidSequence(), study).start();
            protSecondStructCalculated = true;
        } else {
            System.out.println("Show/Hide Protein Secondary Structure: " + showProtSecondStruct);
            remakePanel();
        }

    }

    /**
     * Add a new line with a protein sequence, align it, and show differences to
     * the original AA sequence.
     */
    public void addNewAASequence() {
        String aaSequence = null;
        String preMessage = "";

        GeneticCodeTable gct = null;
        try {
            gct = this.study.getResultingGene().getGenome().getGeneticCodeTable();
        } catch (Exception e) {
            System.out.println("Could not find genetic code table: " + e.getMessage());
        }

        /* Ask the user for an amino acid sequence. */
        while (aaSequence == null) {
            /* Read AA sequence from input. */
            aaSequence = (String) JOptionPane.showInputDialog(this, preMessage + "Please enter a valid amino acid sequence", "Align amino acids", JOptionPane.PLAIN_MESSAGE, null, null, "");
            if (aaSequence == null) {
                return;
            }

            if (aaSequence.length() != study.getResultingGene().getAminoacidSequence().length()) {
                aaSequence = null;
                preMessage = "The sequence must have the same size as this gene's amino acid sequence (" + study.getResultingGene().getAminoacidSequence().length() + " AAs).\n";
                continue;
            }

            /* Upper case the sequence. */
            aaSequence = aaSequence.toUpperCase();

            /* If we have a genetic code table, we can validate the sequence. */
            if (gct != null) {
                int valid = SequenceValidator.isValidAASequence(aaSequence, gct);
                if (valid != 0) {
                    preMessage = "There was an error in the sequence: " + SequenceValidator.getValidationErrorMessage(valid) + "\n";
                    aaSequence = null;
                }
            } else //basic verifier
            {
                if (!aaSequence.matches("[A-Z\\*]+")) {
                    preMessage = "There was an error in the sequence: Invalid characters.\n";
                    aaSequence = null;
                }
            }
        }

        MessageWindow msgWin = new MessageWindow(null, true, false, "Adding your sequence to the gene panel. Please wait...");
        new Thread(msgWin).start();

        extraGene = new Gene("new gene", study.getResultingGene().getGenome());
        extraGene.createStructure(aaSequence, BioStructure.Type.proteinPrimaryStructure);
        remakePanel();

        msgWin.closeWindow();
    }

    public void showHideOrthologs() {
        /* Toggle showing orthologs. */
        if (study.getResultingGene().hasOrthologs()) {
            showOrthologs = !showOrthologs;
            Genome orthologs = study.getResultingGene().hasOrthologs() ? study.getResultingGene().getOrthologList() : study.getOriginalGene().getOrthologList();


            if (showOrthologs) {
                if (orthologs.getGenes().size() >= 0) {
                    orthologsColorScheme = SequencePaintingPool.getInstance().getOrthologsColorSchemes(study);
                }
            }
            remakePanel();


//            if ((showOrthologs) && (!orthologs.OrthologsAreAligned())) 
//            {
//                orthologs.addGene(study.getResultingGene());
//                study.getResultingGene().setOrthologInfo(0, 0, "randomID", "");
//                ResultKeeper alignmentResult = new ResultKeeper(this);
//                Muscle muscleTool = new Muscle(orthologs.getGenes(), alignmentResult);
//                new Thread(muscleTool).start();
//            } 
//            else {
//                remakePanel();
//            }
        } else {
            if (study.getOriginalGene().hasOrthologs()) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "This is an optimized version of the gene. To see the orthologs, please use the original gene.");
            } else {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "There are no orthologs available yet.\nPlease wait until EuGene downloads them (if GeneAutoDiscovery is enabled in Settings).");
            }
        }
    }

    public void viewOrthologsAsCodons() {
        orthologSelectedStructureType = BioStructure.Type.mRNAPrimaryStructure;
        remakePanel();
    }

    public void viewOrthologsAsAminoacids() {
        orthologSelectedStructureType = BioStructure.Type.proteinPrimaryStructure;
        remakePanel();
    }

    public JPopupMenu getPopup() {
        return popup;
    }

    /* Build menu that popups when user right clicks this panel. */
    private void buildContextMenu(JPopupMenu popup) {
        /* MENU ITEM: copy codon sequence. */
        JMenuItem menuItem = new JMenuItem("<html>Copy <b>codon</b> sequence</html>");
        menuItem.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyCodonSequenceToClipboard();
            }
        });
        popup.add(menuItem);

        /* MENU ITEM: copy aminoacid sequence. */
        menuItem = new JMenuItem("<html>Copy <b>aminoacid</b> sequence</html>");
        menuItem.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyAminoacidSequenceToClipboard();
            }
        });
        popup.add(menuItem);

        popup.add(new JSeparator());

        /**
         * ** PROTEIN SECONDARY STRUCTURE ***
         */
//        JMenu subMenuProtSecondStruture = new JMenu("<html><b>Protein secondary structure</b></html>");
//        popup.add(subMenuProtSecondStruture);
        /* MENU ITEM: show/hide ProtSecondStruture */
        menuItem = new JMenuItem("<html>Show/Hide <b>Protein secondary structure</b></html>");
        menuItem.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHideProteinSecondaryStructure();
            }
        });
        popup.add(menuItem);

        popup.add(new JSeparator());

        /**
         * ** ORTHOLOGS ***
         */
        JMenu subMenuOrthologs = new JMenu("<html><b>Orthologs</b></html>");
        popup.add(subMenuOrthologs);

        /* MENU ITEM: get/show orthologs */
        menuItem = new JMenuItem("<html>Show/Hide <b>orthologs</b></html>");
        menuItem.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHideOrthologs();
            }
        });
        subMenuOrthologs.add(menuItem);

        /* MENU ITEM: show codon sequence. */
        menuItem = new JMenuItem("<html>View orthologs as <b>codon</b> sequences</html>");
        menuItem.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewOrthologsAsCodons();
            }
        });
        subMenuOrthologs.add(menuItem);

        /* MENU ITEM: show aminoacid sequence. */
        menuItem = new JMenuItem("<html>View orthologs as <b>aminoacid</b> sequences</html>");
        menuItem.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewOrthologsAsAminoacids();
            }
        });
        subMenuOrthologs.add(menuItem);

        popup.add(new JSeparator());

        /* MENU: select codon color scheme */
        JMenu subMenu = new JMenu("<html>Select <b>codon color scheme</b></html>");
        ButtonGroup colorSchemeGroup = new ButtonGroup();
        for (IOptimizationPlugin plugin : OptimizationModel.getInstance().getOptimizationMethods()) {
            /* Create colour scheme option. */
            JRadioButtonMenuItem schemeButton = new JRadioButtonMenuItem("<html>" + plugin.getPluginName() + "</html>");

            /* If it is the codon usage plugin, select it. That is the default option. */
            schemeButton.setSelected(plugin.getPluginName().equals("Codon Usage"));

            final IOptimizationPlugin colorPlugin = plugin;
            schemeButton.addActionListener(
                    new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    geneColorSchemePlugin = colorPlugin;
                    geneColorSchemeParameters = colorPlugin.getParameters();
                    remakePanel();

                    /* Update information panel if this is the selected gene panel. */
                    if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == study) {
                        GeneInformationPanel.getInstance().updateInformationForSelectedStudy();
                    }
                }
            });
            colorSchemeGroup.add(schemeButton);
            subMenu.add(schemeButton);
        }
        popup.add(subMenu);

        /* MENU ITEM: show codon sequence. */
        menuItem = new JMenuItem("<html>Show <b>diff to another AA</b> sequence</html>");
        menuItem.addActionListener(
                new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewAASequence();
            }
        });
        popup.add(menuItem);
    }

    private void copyStructureToClipBoard(BioStructure.Type type) {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        StringSelection stringSelection = new StringSelection(study.getResultingGene().getStructure(type).getSequence());
        clipboard.setContents(stringSelection, this);
    }

    /**
     * ***********************************************************
     */
    /*                      MOUSE LISTENING                       */
    /**
     * ***********************************************************
     */
    @Override
    public void mousePressed(MouseEvent e) {
        /* If left mouse button cliked, select the clicked study. 
         * However, if CTRL is also pressed (in MAC), popup the trigger. */
        if (!tryShowPopup(e) && (e.getButton() == MouseEvent.BUTTON1)) {
            ProjectManager.getInstance().getSelectedProject().setSelectedStudy(study);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        tryShowPopup(e);
    }

    public boolean tryShowPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            popup.show(e.getComponent(), e.getX(), e.getY());
        }

        return e.isPopupTrigger();
    }

    /**
     * ***********************************************************
     */
    /*                    CLIPBOARD LISTENING                     */
    /**
     * ***********************************************************
     */
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    /**
     * ***********************************************************
     */
    /*                       OBSERVER UPDATES                     */
    /**
     * ***********************************************************
     */
    @Override
    public void update(Observable o, Object arg) {
        if (!(o instanceof ResultKeeper)) {
            return;
        }

        ResultKeeper rk = (ResultKeeper) o;

        if (rk.isFail()) {
            return;
        }

        /* Get aligned orthologs. */
        Genome orthologs = study.getResultingGene().hasOrthologs() ? study.getResultingGene().getOrthologList() : study.getOriginalGene().getOrthologList();

        orthologs.setOrthologsAligned(true);
        orthologs.removeGene(study.getResultingGene());

        if (orthologs.getGenes().size() >= 0) {
            orthologsColorScheme = SequencePaintingPool.getInstance().getOrthologsColorSchemes(study);
        }

        remakePanel();
    }
}
