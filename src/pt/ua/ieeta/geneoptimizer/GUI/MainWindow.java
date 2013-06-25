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
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        menuOpenGenome = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        menuNewProject = new javax.swing.JMenuItem();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem9 = new javax.swing.JMenuItem();
        menuCloseProject = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        menuQuitApp = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenuItem6 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        menuHideInformationZone = new javax.swing.JMenuItem();
        menuHideStudiesZone = new javax.swing.JMenuItem();
        menuGenePool = new javax.swing.JMenu();
        menuOpenGenePool = new javax.swing.JMenuItem();
        jMenu4 = new javax.swing.JMenu();
        jMenuItem10 = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();

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

        jMenuBar1.setFont(new java.awt.Font("Segoe UI", 0, 13)); // NOI18N
        jMenuBar1.setPreferredSize(new java.awt.Dimension(119, 31));

        fileMenu.setText("File");

        menuOpenGenome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        menuOpenGenome.setText("Open Genome");
        menuOpenGenome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenGenomeActionPerformed(evt);
            }
        });
        fileMenu.add(menuOpenGenome);
        fileMenu.add(jSeparator3);

        menuNewProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        menuNewProject.setText("New Project");
        menuNewProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuNewProjectActionPerformed(evt);
            }
        });
        fileMenu.add(menuNewProject);

        jMenuItem8.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem8.setText("Save Project...");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectEvent(evt);
            }
        });
        fileMenu.add(jMenuItem8);

        jMenuItem9.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem9.setText("Load Project...");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadProjectEvent(evt);
            }
        });
        fileMenu.add(jMenuItem9);

        menuCloseProject.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        menuCloseProject.setText("Close current project");
        menuCloseProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuCloseProjectActionPerformed(evt);
            }
        });
        fileMenu.add(menuCloseProject);
        fileMenu.add(jSeparator4);

        menuQuitApp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        menuQuitApp.setText("Quit");
        menuQuitApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuQuitAppActionPerformed(evt);
            }
        });
        fileMenu.add(menuQuitApp);

        jMenuBar1.add(fileMenu);

        jMenu2.setText("Edit");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.setText("<html>Copy <b>codon</b> sequence</html>");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.setText("<html>Copy <b>amino acid</b> sequence</html>");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem2);
        jMenu2.add(jSeparator2);

        jMenuItem3.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_D, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem3.setText("<html>Calculate <b>protein secondary structure</b></html>");
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem3);

        jMenu5.setText("Orthologs");

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("<html>Show/Hide <b>orthologs</b></html>");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem4);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("<html>View orthologs as <b>codon</b> sequences</html>");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem5);

        jMenuItem6.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem6.setText("<html>View orthologs as <b>aminoacid</b> sequences</html>");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu5.add(jMenuItem6);

        jMenu2.add(jMenu5);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setText("<html>Show <b>diff to another AA</b> sequence</html>");
        jMenuItem12.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem12ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem12);
        jMenu2.add(jSeparator1);

        jMenuItem7.setText("Settings");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem7);

        jMenuBar1.add(jMenu2);

        jMenu1.setText("View");

        menuHideInformationZone.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        menuHideInformationZone.setText("Hide Information Zone");
        menuHideInformationZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuHideInformationZoneActionPerformed(evt);
            }
        });
        jMenu1.add(menuHideInformationZone);

        menuHideStudiesZone.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        menuHideStudiesZone.setText("Hide Operations Zone");
        menuHideStudiesZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuHideStudiesZoneActionPerformed(evt);
            }
        });
        jMenu1.add(menuHideStudiesZone);

        jMenuBar1.add(jMenu1);

        menuGenePool.setText("Gene Pool");
        menuGenePool.setToolTipText("Access the genomes pool.");

        menuOpenGenePool.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
        menuOpenGenePool.setText("Open Gene Pool");
        menuOpenGenePool.setToolTipText("The list of available genes/genomes to work with.");
        menuOpenGenePool.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuOpenGenePoolActionPerformed(evt);
            }
        });
        menuGenePool.add(menuOpenGenePool);

        jMenuBar1.add(menuGenePool);

        jMenu4.setText("Help");

        jMenuItem10.setText("Tutorial");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem10);

        jMenuItem11.setText("About");
        jMenuItem11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem11ActionPerformed(evt);
            }
        });
        jMenu4.add(jMenuItem11);

        jMenuBar1.add(jMenu4);

        setJMenuBar(jMenuBar1);

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

    private void menuOpenGenePoolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenGenePoolActionPerformed
        GenePool.getInstance().showGenePool();
}//GEN-LAST:event_menuOpenGenePoolActionPerformed

    private void menuNewProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuNewProjectActionPerformed
        ProjectManager.getInstance().createNewProject();
    }//GEN-LAST:event_menuNewProjectActionPerformed

    private void menuHideInformationZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuHideInformationZoneActionPerformed
        isInformationZoneVisible = !isInformationZoneVisible;

        if (!isInformationZoneVisible) {
            instance.mainContent.remove(containerPanels.get(1));
            menuHideInformationZone.setText("Show Information Zone");
        } else {
            instance.mainContent.add(containerPanels.get(1), BorderLayout.EAST);
            menuHideInformationZone.setText("Hide Information Zone");
        }

        instance.mainContent.updateUI();
    }//GEN-LAST:event_menuHideInformationZoneActionPerformed

    private void menuHideStudiesZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuHideStudiesZoneActionPerformed
        isStudiesZoneVisible = !isStudiesZoneVisible;

        if (!isStudiesZoneVisible) {
            instance.mainContent.remove(containerPanels.get(0));
            menuHideStudiesZone.setText("Show Operations Zone");
        } else {
            instance.mainContent.add(containerPanels.get(0), BorderLayout.WEST);
            menuHideStudiesZone.setText("Hide Operations Zone");
        }

        instance.mainContent.updateUI();
    }//GEN-LAST:event_menuHideStudiesZoneActionPerformed

    private void menuOpenGenomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuOpenGenomeActionPerformed
        GenePool.getInstance().showGenePool();
    }//GEN-LAST:event_menuOpenGenomeActionPerformed

    private void menuCloseProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuCloseProjectActionPerformed
        if (ProjectManager.getInstance().getSelectedProject() == null) {
            return;
        }

        ProjectManager.getInstance().removeProject(ProjectManager.getInstance().getSelectedProject());
    }//GEN-LAST:event_menuCloseProjectActionPerformed

private void menuQuitAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuQuitAppActionPerformed
    //TODO: Handle exit
    System.exit(0);
}//GEN-LAST:event_menuQuitAppActionPerformed

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

    private void jMenuItem11ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem11ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem11ActionPerformed
        new AboutDialog().showMe();
    }//GEN-LAST:event_jMenuItem11ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem10ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem10ActionPerformed
        try {

            java.awt.Desktop.getDesktop().browse(new URI("http://bioinformatics.ua.pt/eugene"));

        } catch (Exception ex) {
            System.out.println("Error while trying to open eugene tutorial web page.");
        }
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem7ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem7ActionPerformed
        ApplicationSettingsGUI.getInstance().setVisible(true);
        ApplicationSettingsGUI.getInstance().setAlwaysOnTop(true);
        ApplicationSettingsGUI.getInstance().setLocationRelativeTo(null);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem12ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem12ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem12ActionPerformed
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
    }//GEN-LAST:event_jMenuItem12ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem6ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem6ActionPerformed
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
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem5ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem5ActionPerformed
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
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem4ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem4ActionPerformed
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
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem3ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem3ActionPerformed
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
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem2ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem2ActionPerformed
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
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItem1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItem1ActionPerformed

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
    }//GEN-LAST:event_jMenuItem1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu4;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPanel mainContent;
    private javax.swing.JMenuItem menuCloseProject;
    private javax.swing.JMenu menuGenePool;
    private javax.swing.JMenuItem menuHideInformationZone;
    private javax.swing.JMenuItem menuHideStudiesZone;
    private javax.swing.JMenuItem menuNewProject;
    private javax.swing.JMenuItem menuOpenGenePool;
    private javax.swing.JMenuItem menuOpenGenome;
    private javax.swing.JMenuItem menuQuitApp;
    // End of variables declaration//GEN-END:variables
}
