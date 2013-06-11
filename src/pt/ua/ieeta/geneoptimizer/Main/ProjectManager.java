package pt.ua.ieeta.geneoptimizer.Main;

import java.io.File;
import java.util.Vector;
import pt.ua.ieeta.geneoptimizer.GUI.LoadProjectFileProgPanel;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.GUI.TabbedProjectsPanel;

/**
 *
 * @author Paulo Gaspar
 */
public class ProjectManager {
    /* Singleton instance. */

    private static ProjectManager instance = null;

    /* Global project ID. */
    private static int globalProjectID = 1;

    /* List of open projects. */
    private static Vector<Project> projectList;

    /* Reference to the active project (the project selected in the tabs. */
    private static Project selectedProject = null;

    public static ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
            projectList = new Vector<Project>();
        }

        return instance;
    }

    private ProjectManager() {
    }

    public synchronized Project createNewProject() {
        /* Create new project. */
        Project newProject = new Project(globalProjectID++);

        /* Add new project to list of projects. */
        projectList.add(newProject);

        /* Create new Tab to accomodate project contents. */
        TabbedProjectsPanel.getInstance().addNewProject(newProject);

        System.out.println("Created new project Tab: " + newProject.getName());

        return newProject;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        assert selectedProject != null;

        ProjectManager.selectedProject = selectedProject;
    }

    public void removeProject(Project selectedProject) {
        assert selectedProject != null;
        assert projectList != null;

        /* Remove project from the list of projects. */
        projectList.remove(selectedProject);

        /* If there is only one project, create a default project. */
        if (getNumberOfProjects() == 0) {
            createNewProject();
        }

        /* Create new Tab to accomodate project contents. */
        TabbedProjectsPanel.getInstance().removeProject(selectedProject);

        System.gc();
    }

    public boolean saveProject(Project project, File file) {
        if (project == null || file == null) {
            throw new IllegalArgumentException("Project to save is null");
        }

        boolean result = true;

        if (!SaveXMLProject.getInstance().saveFile(ProjectManager.getInstance().getSelectedProject(), file)) {
            result = false;
        }
        return result;
    }

    /**
     * Method that creates a new project from file.
     *
     * @param projectFile file to load the project
     * @return true if project is successful created false otherwise
     */
    public synchronized void loadProject(File projectFile) {
        if (projectFile == null
                || !projectFile.isFile()
                || !projectFile.getName().substring(projectFile.getName().lastIndexOf(".")).equalsIgnoreCase(".euj")) {
            throw new AssertionError("Project to load is incorrect");
        }

        LoadProjectFileProgPanel progressPanel = new LoadProjectFileProgPanel(MainWindow.getInstance());

        /* Create new project. */
        Project newProject = new Project(globalProjectID++);
        /* Add new project to list of projects. */
        projectList.add(newProject);
        /* Create new Tab to accomodate project contents. */
        TabbedProjectsPanel.getInstance().addNewProject(newProject);

        /* Load all data from file (Genome, Genes, Studies, etc) */
        LoadXMLProject.getInstance().setDetails(newProject, projectFile, progressPanel);
        LoadXMLProject.getInstance().start();        
    }

    private boolean handleException(Exception exception, LoadProjectFileProgPanel progresPanel) {
        progresPanel.errorProgress();
        exception.printStackTrace();
        return false;
    }

    public int getNumberOfProjects() {
        assert projectList != null;

        return projectList.size();
    }
}
