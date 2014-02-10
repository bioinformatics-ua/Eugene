/*
 * MainWindow.java
 *
 * Created on 16/Dez/2009, 18:58:20
 */
package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.BorderLayout;
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.awt.HeadlessException;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import pt.ua.ieeta.geneoptimizer.FileHandling.ExportTSV;
import pt.ua.ieeta.geneoptimizer.GUI.GenePanel.SingleGenePanel;
import pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel.StudyMakerPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class MainWindow extends javax.swing.JFrame {
    /* List of container panels in the main window. */

    private static List<ContainerPanel> containerPanels;
    private static boolean isInformationZoneVisible;
    private static boolean isStudiesZoneVisible;

    /* Singleton instance of MainWindow. */
    private static volatile MainWindow instance = null;

    /**
     * Creates new form MainWindow
     */
    public static MainWindow getInstance() {
        /* If there isn't an instance yet, create one. */

        if (instance == null) {
            synchronized (MainWindow.class) {
                if (instance == null) {
                    instance = new MainWindow();
                    instance.initComponents();
                    instance.setTitle("Eugene - Gene Redesign and Analysis for Heterologous Expression");
                    instance.setExtendedState(MAXIMIZED_BOTH);

                    instance.mainContent.setLayout(new BorderLayout());

                    /* Flags. */
                    isInformationZoneVisible = true;
                    isStudiesZoneVisible = true;

                    /* Instantiate list of container panels. */
                    containerPanels = Collections.synchronizedList(new ArrayList<ContainerPanel>());

                    /* Create three panels to add to the main content panel. These will be the three main areas of the window. */
                    containerPanels.add(new ContainerPanel(null)); //studies panel
                    containerPanels.add(new ContainerPanel(null)); //information panel

                    /* Add the three main areas to the main window. */
                    instance.mainContent.add(containerPanels.get(0), BorderLayout.WEST);
                    instance.mainContent.add(TabbedProjectsPanel.getInstance(), BorderLayout.CENTER);
                    instance.mainContent.add(containerPanels.get(1), BorderLayout.EAST);

                    /* Create a default project. */
                    ProjectManager.getInstance().createNewProject();

                    /* Create protein 3D viewer panel. */
                    new Thread(Protein3DViewerPanel.getInstance()).start();

                    /* Add studies panel and information panel to main window. */
                    toggleContentPanelVisibility(0, ProgressPanel.getInstance(), true);
                    toggleContentPanelVisibility(0, StudyMakerPanel.getInstance(), true);
                    toggleContentPanelVisibility(1, GeneInformationPanel.getInstance(), true);
                    toggleContentPanelVisibility(1, Protein3DViewerPanel.getInstance(), true);

                    instance.pack();
                    instance.setLocationRelativeTo(null);
                }
            }
        }

        return instance;
    }

    /* Avoid instantiations. */
    private MainWindow() {
    }

    public void addC(JComponent c) {
        instance.mainContent.add(c);
    }

    /**
     * Show or hide a given content panel in a container.
     */
    public static void toggleContentPanelVisibility(int containerID, ContentPanel panel, boolean showPanel) {
        assert containerPanels != null;
        assert containerID >= 0;
        assert containerID < containerPanels.size();
        assert panel != null;
        assert containerPanels.get(containerID) != null;



        /* Add or remove panel from container. */
        if (showPanel) {
            containerPanels.get(containerID).addContentPanel(panel);
        } else {
            containerPanels.get(containerID).removeContentPanel(panel);
        }
    }

    /**
     * ************************************************************************
     */
    /*                                  SWING                                  */
    /**
     * ************************************************************************
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainContent = new javax.swing.JPanel();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        fileMenuOpenGenome = new javax.swing.JMenuItem();
        fileMenuSeparator1 = new javax.swing.JPopupMenu.Separator();
        fileMenuNewProject = new javax.swing.JMenuItem();
        fileMenuSaveProject = new javax.swing.JMenuItem();
        fileMenuLoadProject = new javax.swing.JMenuItem();
        fileMenuCloseProject = new javax.swing.JMenuItem();
        fileMenuSeparator2 = new javax.swing.JPopupMenu.Separator();
        fileMenuQuit = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        editMenuCopyCodon = new javax.swing.JMenuItem();
        editMenuCopyAminoAcid = new javax.swing.JMenuItem();
        editMenuSeparator1 = new javax.swing.JPopupMenu.Separator();
        editMenuExportTSV = new javax.swing.JMenuItem();
        editMenuSeparator2 = new javax.swing.JPopupMenu.Separator();
        editMenuCalculateProtein = new javax.swing.JMenuItem();
        editMenuOrthologsMenu = new javax.swing.JMenu();
        orthologsMenuShowHide = new javax.swing.JMenuItem();
        orthologsMenuViewCodonSeq = new javax.swing.JMenuItem();
        orthologsMenuViewAminoAcidSeq = new javax.swing.JMenuItem();
        editMenuShowDiff = new javax.swing.JMenuItem();
        editMenuSeparator3 = new javax.swing.JPopupMenu.Separator();
        editMenuSettings = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        viewMenuHideInfoZone = new javax.swing.JMenuItem();
        viewMenuHideOpsZone = new javax.swing.JMenuItem();
        genePoolMenu = new javax.swing.JMenu();
        genePoolMenuOpenGenePool = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuTutorial = new javax.swing.JMenuItem();
        helpMenuAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Eugene - Gene Redesign and Analysis for Heterologous Expression");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setName("mainWindow"); // NOI18N

        mainContent.setAlignmentX(0.0F);
        mainContent.setAlignmentY(0.0F);

        javax.swing.GroupLayout mainContentLayout = new javax.swing.GroupLayout(mainContent);
        mainContent.setLayout(mainContentLayout);
        mainContentLayout.setHorizontalGroup(
            mainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1017, Short.MAX_VALUE)
        );
        mainContentLayout.setVerticalGroup(
            mainContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 497, Short.MAX_VALUE)
        );

        mainMenuBar.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        mainMenuBar.setPreferredSize(new java.awt.Dimension(119, 31));

        fileMenu.setText("File");

        fileMenuOpenGenome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        fileMenuOpenGenome.setText("Open Genome");
        fileMenuOpenGenome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuOpenGenomeActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuOpenGenome);
        fileMenu.add(fileMenuSeparator1);

        fileMenuNewProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        fileMenuNewProject.setText("New Project");
        fileMenuNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuNewProjectActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuNewProject);

        fileMenuSaveProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        fileMenuSaveProject.setText("Save Project...");
        fileMenuSaveProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectEvent(evt);
            }
        });
        fileMenu.add(fileMenuSaveProject);

        fileMenuLoadProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        fileMenuLoadProject.setText("Load Project...");
        fileMenuLoadProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProjectEvent(evt);
            }
        });
        fileMenu.add(fileMenuLoadProject);

        fileMenuCloseProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        fileMenuCloseProject.setText("Close current project");
        fileMenuCloseProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuCloseProjectActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuCloseProject);
        fileMenu.add(fileMenuSeparator2);

        fileMenuQuit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        fileMenuQuit.setText("Quit");
        fileMenuQuit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileMenuQuitActionPerformed(evt);
            }
        });
        fileMenu.add(fileMenuQuit);

        mainMenuBar.add(fileMenu);

        editMenu.setText("Edit");

        editMenuCopyCodon.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        editMenuCopyCodon.setText("<html>Copy <b>codon</b> sequence</html>");
        editMenuCopyCodon.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuCopyCodonActionPerformed(evt);
            }
        });
        editMenu.add(editMenuCopyCodon);

        editMenuCopyAminoAcid.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        editMenuCopyAminoAcid.setText("<html>Copy <b>amino acid</b> sequence</html>");
        editMenuCopyAminoAcid.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuCopyAminoAcidActionPerformed(evt);
            }
        });
        editMenu.add(editMenuCopyAminoAcid);
        editMenu.add(editMenuSeparator1);

        editMenuExportTSV.setText("<html><b>Export</b> selected gene to TSV</html>");
        editMenuExportTSV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuExportTSVActionPerformed(evt);
            }
        });
        editMenu.add(editMenuExportTSV);
        editMenu.add(editMenuSeparator2);

        editMenuCalculateProtein.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        editMenuCalculateProtein.setText("<html>Calculate <b>protein secondary structure</b></html>");
        editMenuCalculateProtein.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuCalculateProteinActionPerformed(evt);
            }
        });
        editMenu.add(editMenuCalculateProtein);

        editMenuOrthologsMenu.setText("Orthologs");

        orthologsMenuShowHide.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        orthologsMenuShowHide.setText("<html>Show/Hide <b>orthologs</b></html>");
        orthologsMenuShowHide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orthologsMenuShowHideActionPerformed(evt);
            }
        });
        editMenuOrthologsMenu.add(orthologsMenuShowHide);

        orthologsMenuViewCodonSeq.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        orthologsMenuViewCodonSeq.setText("<html>View orthologs as <b>codon</b> sequences</html>");
        orthologsMenuViewCodonSeq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orthologsMenuViewCodonSeqActionPerformed(evt);
            }
        });
        editMenuOrthologsMenu.add(orthologsMenuViewCodonSeq);

        orthologsMenuViewAminoAcidSeq.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        orthologsMenuViewAminoAcidSeq.setText("<html>View orthologs as <b>aminoacid</b> sequences</html>");
        orthologsMenuViewAminoAcidSeq.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orthologsMenuViewAminoAcidSeqActionPerformed(evt);
            }
        });
        editMenuOrthologsMenu.add(orthologsMenuViewAminoAcidSeq);

        editMenu.add(editMenuOrthologsMenu);

        editMenuShowDiff.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        editMenuShowDiff.setText("<html>Show <b>diff to another AA</b> sequence</html>");
        editMenuShowDiff.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuShowDiffActionPerformed(evt);
            }
        });
        editMenu.add(editMenuShowDiff);
        editMenu.add(editMenuSeparator3);

        editMenuSettings.setText("Settings");
        editMenuSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuSettingsActionPerformed(evt);
            }
        });
        editMenu.add(editMenuSettings);

        mainMenuBar.add(editMenu);

        viewMenu.setText("View");

        viewMenuHideInfoZone.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        viewMenuHideInfoZone.setText("Hide Information Zone");
        viewMenuHideInfoZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMenuHideInfoZoneActionPerformed(evt);
            }
        });
        viewMenu.add(viewMenuHideInfoZone);

        viewMenuHideOpsZone.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        viewMenuHideOpsZone.setText("Hide Operations Zone");
        viewMenuHideOpsZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMenuHideOpsZoneActionPerformed(evt);
            }
        });
        viewMenu.add(viewMenuHideOpsZone);

        mainMenuBar.add(viewMenu);

        genePoolMenu.setText("Gene Pool");
        genePoolMenu.setToolTipText("Access the genomes pool.");

        genePoolMenuOpenGenePool.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        genePoolMenuOpenGenePool.setText("Open Gene Pool");
        genePoolMenuOpenGenePool.setToolTipText("The list of available genes/genomes to work with.");
        genePoolMenuOpenGenePool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                genePoolMenuOpenGenePoolActionPerformed(evt);
            }
        });
        genePoolMenu.add(genePoolMenuOpenGenePool);

        mainMenuBar.add(genePoolMenu);

        helpMenu.setText("Help");

        helpMenuTutorial.setText("Tutorial");
        helpMenuTutorial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuTutorialActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuTutorial);

        helpMenuAbout.setText("About");
        helpMenuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuAboutActionPerformed(evt);
            }
        });
        helpMenu.add(helpMenuAbout);

        mainMenuBar.add(helpMenu);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainContent, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(mainContent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void genePoolMenuOpenGenePoolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_genePoolMenuOpenGenePoolActionPerformed
        GenePool.getInstance().showGenePool();
}//GEN-LAST:event_genePoolMenuOpenGenePoolActionPerformed

    private void fileMenuNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuNewProjectActionPerformed
        ProjectManager.getInstance().createNewProject();
    }//GEN-LAST:event_fileMenuNewProjectActionPerformed

    private void viewMenuHideInfoZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMenuHideInfoZoneActionPerformed
        isInformationZoneVisible = !isInformationZoneVisible;

        if (!isInformationZoneVisible) {
            instance.mainContent.remove(containerPanels.get(1));
            viewMenuHideInfoZone.setText("Show Information Zone");
        } else {
            instance.mainContent.add(containerPanels.get(1), BorderLayout.EAST);
            viewMenuHideInfoZone.setText("Hide Information Zone");
        }

        instance.mainContent.updateUI();
    }//GEN-LAST:event_viewMenuHideInfoZoneActionPerformed

    private void viewMenuHideOpsZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_viewMenuHideOpsZoneActionPerformed
        isStudiesZoneVisible = !isStudiesZoneVisible;

        if (!isStudiesZoneVisible) {
            instance.mainContent.remove(containerPanels.get(0));
            viewMenuHideOpsZone.setText("Show Operations Zone");
        } else {
            instance.mainContent.add(containerPanels.get(0), BorderLayout.WEST);
            viewMenuHideOpsZone.setText("Hide Operations Zone");
        }

        instance.mainContent.updateUI();
    }//GEN-LAST:event_viewMenuHideOpsZoneActionPerformed

    private void fileMenuOpenGenomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuOpenGenomeActionPerformed
        GenePool.getInstance().showGenePool();
    }//GEN-LAST:event_fileMenuOpenGenomeActionPerformed

    private void fileMenuCloseProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuCloseProjectActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }

        ProjectManager.getInstance().removeProject(ProjectManager.getInstance().getSelectedProject());
    }//GEN-LAST:event_fileMenuCloseProjectActionPerformed

private void fileMenuQuitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileMenuQuitActionPerformed
    //TODO: Handle exit
    System.exit(0);
}//GEN-LAST:event_fileMenuQuitActionPerformed

    private void saveProjectEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProjectEvent

        System.out.println("SAVING PROJECT...");
        File saveFile = null;

        saveFile = choseSaveFile();

        // save project
        if (saveFile != null) {
            saveFile = verifyFile(saveFile);

            // Save project
            if (ProjectManager.getInstance().saveProject(
                    ProjectManager.getInstance().getSelectedProject(),
                    saveFile)) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Project successfully saved.");
                System.out.println("PROJECT SUCCESSFULLY SAVED.");
            } else {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Project save failed.");
                System.out.println("PROJECT SAVE FAILED.");
            }
        } else {
            System.out.println("File is null! Cancel saving project.");
        }

    }//GEN-LAST:event_saveProjectEvent

    private File choseSaveFile() throws HeadlessException {
        File saveFile = null;

        // create default name
        String defaultName = ProjectManager.getInstance().getSelectedProject().getName()
                + "_"
                + Calendar.getInstance().getTime().toString().substring(4, 16);
        defaultName = defaultName.replace(' ', '_');
        defaultName = defaultName.replace(':', '_');
        // chose file to save project
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String projectsPath = (String) ApplicationSettings.getProperty("projectsPath", String.class);
        JFileChooser chooser = new JFileChooser(eugeneDir + projectsPath);
        FileFilter filter = new FileNameExtensionFilter("EuGene Project File", "euj");
        chooser.setFileFilter(filter);
        chooser.setSelectedFile(new File("eugeneDir + projectsPath", defaultName + ".euj"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            saveFile = chooser.getSelectedFile();
            System.out.println("File: " + saveFile.getAbsolutePath());
        }
        return saveFile;
    }

    private File verifyFile(File file) {
        // verify extention
        String extension = null;
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i > 0 && i < name.length() - 1) {
            extension = name.substring(i + 1).toLowerCase();
        }
        if (extension == null) {
            file = new File(file.getParent(), (file.getName() + ".euj"));
        } else {
            if (!extension.equals("euj")) {
                file = new File(file.getParent(), name.substring(0, i) + ".euj");
            }
        }
        // Verify if file already exists
        if (file.exists()) {
            file = new File(file.getParent(), ("copy_" + file.getName()));
        }

        return file;
    }

    /**
     * Method used to load a project from File Open the file chooser and call
     * ProjectManager to load project
     *
     * @param evt event that cause this action
     */
    private void loadProjectEvent(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadProjectEvent
        System.out.println("OPEN PROJECT...");

        /* Load default directory instance. */
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String projectsPath = (String) ApplicationSettings.getProperty("projectsPath", String.class);
        File projectsDirectory = new File(eugeneDir + projectsPath);
        File projectFile = null;

        /* Choose file to load */
        JFileChooser fileChooser = new JFileChooser(projectsDirectory);
        FileFilter filter = new FileNameExtensionFilter("EuGene Project File", "euj");
        fileChooser.setFileFilter(filter);
        if (fileChooser.showOpenDialog(MainWindow.getInstance()) == JFileChooser.APPROVE_OPTION) {
            projectFile = fileChooser.getSelectedFile();
            System.out.println("Opening project...  " + projectFile.getName());
        } else {
            System.out.println("Cancel opening file...");
            return;
        }
        /* Load project from Project Manager */
        ProjectManager.getInstance().loadProject(projectFile);
    }//GEN-LAST:event_loadProjectEvent

    private void helpMenuAboutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpMenuAboutActionPerformed
    {//GEN-HEADEREND:event_helpMenuAboutActionPerformed
        new AboutDialog().showMe();
    }//GEN-LAST:event_helpMenuAboutActionPerformed

    private void helpMenuTutorialActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpMenuTutorialActionPerformed
    {//GEN-HEADEREND:event_helpMenuTutorialActionPerformed
        try {

            java.awt.Desktop.getDesktop().browse(new URI("http://bioinformatics.ua.pt/eugene"));

        } catch (Exception ex) {
            System.out.println("Error while trying to open eugene tutorial web page.");
        }
    }//GEN-LAST:event_helpMenuTutorialActionPerformed

    private void editMenuSettingsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editMenuSettingsActionPerformed
    {//GEN-HEADEREND:event_editMenuSettingsActionPerformed
        ApplicationSettingsGUI.getInstance().setVisible(true);
        ApplicationSettingsGUI.getInstance().setAlwaysOnTop(true);
        ApplicationSettingsGUI.getInstance().setLocationRelativeTo(null);
    }//GEN-LAST:event_editMenuSettingsActionPerformed

    private void editMenuShowDiffActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editMenuShowDiffActionPerformed
    {//GEN-HEADEREND:event_editMenuShowDiffActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return;
        }
        if (!(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel() instanceof SingleGenePanel)) {
            return;
        }

        SingleGenePanel panel = (SingleGenePanel) ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel();
        panel.addNewAASequence();
    }//GEN-LAST:event_editMenuShowDiffActionPerformed

    private void orthologsMenuViewAminoAcidSeqActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_orthologsMenuViewAminoAcidSeqActionPerformed
    {//GEN-HEADEREND:event_orthologsMenuViewAminoAcidSeqActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return;
        }
        if (!(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel() instanceof SingleGenePanel)) {
            return;
        }

        SingleGenePanel panel = (SingleGenePanel) ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel();
        panel.viewOrthologsAsAminoacids();
    }//GEN-LAST:event_orthologsMenuViewAminoAcidSeqActionPerformed

    private void orthologsMenuViewCodonSeqActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_orthologsMenuViewCodonSeqActionPerformed
    {//GEN-HEADEREND:event_orthologsMenuViewCodonSeqActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return;
        }
        if (!(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel() instanceof SingleGenePanel)) {
            return;
        }

        SingleGenePanel panel = (SingleGenePanel) ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel();
        panel.viewOrthologsAsCodons();
    }//GEN-LAST:event_orthologsMenuViewCodonSeqActionPerformed

    private void orthologsMenuShowHideActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_orthologsMenuShowHideActionPerformed
    {//GEN-HEADEREND:event_orthologsMenuShowHideActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return;
        }
        if (!(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel() instanceof SingleGenePanel)) {
            return;
        }

        SingleGenePanel panel = (SingleGenePanel) ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel();
        panel.showHideOrthologs();
    }//GEN-LAST:event_orthologsMenuShowHideActionPerformed

    private void editMenuCalculateProteinActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editMenuCalculateProteinActionPerformed
    {//GEN-HEADEREND:event_editMenuCalculateProteinActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return;
        }
        if (!(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel() instanceof SingleGenePanel)) {
            return;
        }

        SingleGenePanel panel = (SingleGenePanel) ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel();
        panel.showHideProteinSecondaryStructure();
    }//GEN-LAST:event_editMenuCalculateProteinActionPerformed

    private void editMenuCopyAminoAcidActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editMenuCopyAminoAcidActionPerformed
    {//GEN-HEADEREND:event_editMenuCopyAminoAcidActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return;
        }
        if (!(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel() instanceof SingleGenePanel)) {
            return;
        }

        SingleGenePanel panel = (SingleGenePanel) ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel();
        panel.copyAminoacidSequenceToClipboard();
    }//GEN-LAST:event_editMenuCopyAminoAcidActionPerformed

    private void editMenuCopyCodonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_editMenuCopyCodonActionPerformed
    {//GEN-HEADEREND:event_editMenuCopyCodonActionPerformed

        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }
        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == null) {
            return;
        }
        if (!(ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel() instanceof SingleGenePanel)) {
            return;
        }

        SingleGenePanel panel = (SingleGenePanel) ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getCurrentPanel();
        panel.copyCodonSequenceToClipboard();
    }//GEN-LAST:event_editMenuCopyCodonActionPerformed

    private void editMenuExportTSVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuExportTSVActionPerformed
        new Thread(new ExportTSV()).start();
    }//GEN-LAST:event_editMenuExportTSVActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editMenuCalculateProtein;
    private javax.swing.JMenuItem editMenuCopyAminoAcid;
    private javax.swing.JMenuItem editMenuCopyCodon;
    private javax.swing.JMenuItem editMenuExportTSV;
    private javax.swing.JMenu editMenuOrthologsMenu;
    private javax.swing.JPopupMenu.Separator editMenuSeparator1;
    private javax.swing.JPopupMenu.Separator editMenuSeparator2;
    private javax.swing.JPopupMenu.Separator editMenuSeparator3;
    private javax.swing.JMenuItem editMenuSettings;
    private javax.swing.JMenuItem editMenuShowDiff;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuItem fileMenuCloseProject;
    private javax.swing.JMenuItem fileMenuLoadProject;
    private javax.swing.JMenuItem fileMenuNewProject;
    private javax.swing.JMenuItem fileMenuOpenGenome;
    private javax.swing.JMenuItem fileMenuQuit;
    private javax.swing.JMenuItem fileMenuSaveProject;
    private javax.swing.JPopupMenu.Separator fileMenuSeparator1;
    private javax.swing.JPopupMenu.Separator fileMenuSeparator2;
    private javax.swing.JMenu genePoolMenu;
    private javax.swing.JMenuItem genePoolMenuOpenGenePool;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuAbout;
    private javax.swing.JMenuItem helpMenuTutorial;
    private javax.swing.JPanel mainContent;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JMenuItem orthologsMenuShowHide;
    private javax.swing.JMenuItem orthologsMenuViewAminoAcidSeq;
    private javax.swing.JMenuItem orthologsMenuViewCodonSeq;
    private javax.swing.JMenu viewMenu;
    private javax.swing.JMenuItem viewMenuHideInfoZone;
    private javax.swing.JMenuItem viewMenuHideOpsZone;
    // End of variables declaration//GEN-END:variables
}
