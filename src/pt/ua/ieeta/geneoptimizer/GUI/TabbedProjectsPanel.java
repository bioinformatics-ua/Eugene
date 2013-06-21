package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.Dimension;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import pt.ua.ieeta.geneoptimizer.Main.Project;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class TabbedProjectsPanel extends JTabbedPane {

    private static volatile TabbedProjectsPanel instance = null;

    public static TabbedProjectsPanel getInstance() {
        if (instance == null) {
            synchronized (TabbedProjectsPanel.class) {
                if (instance == null) {
                    instance = new TabbedProjectsPanel();
                    instance.setTabPlacement(JTabbedPane.BOTTOM);

                    /* Listener for when the user selects a project tab. */
                    instance.addChangeListener(
                            new ChangeListener() {
                        public void stateChanged(ChangeEvent evt) {
                            Project selected = ((ContainerPanel) instance.getSelectedComponent()).getProject();
                            ProjectManager.getInstance().setSelectedProject(selected);
                        }
                    });
                }
            }
        }

        return instance;
    }

    private TabbedProjectsPanel() {
    }

    public synchronized int addNewProject(Project project) {
        assert project != null;

        /* Create new container for this project. */
        ContainerPanel newPanel = new ContainerPanel(project);
        newPanel.setMinimumSize(new Dimension(1000, 580));

        /* Add new tab and select it. */
        addTab(project.getName(), newPanel);
        setSelectedIndex(getTabCount() - 1);

        /* Set project container as the newly created one. */
        project.setContainerPanel(newPanel);

//        final JLabel newField = new JLabel(project.getName());
//        MainWindow.projectsPanel.setTabComponentAt(MainWindow.projectsPanel.getTabCount()-1, newField);
//
//        newField.addFocusListener(new FocusListener() {
//            public void focusGained(FocusEvent e){
//                e.g
//            }
//            public void focusLost(FocusEvent e){
//            }
//        });

        return getTabCount() - 1;
    }

    public void removeProject(Project project) {
        remove(project.getContainerPanel());
    }
}
