package pt.ua.ieeta.geneoptimizer.Main;

import java.util.*;
import pt.ua.ieeta.geneoptimizer.GUI.*;
import pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel.HostSelectionPanel;
import pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel.StudyMakerPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.WebServices.GeneAutoDiscover;
import pt.ua.ieeta.geneoptimizer.geneDB.*;

/**
 *
 * @author Paulo Gaspar
 */
public class Project extends Observable
{
    /* ID of this project. */
    private int ID;

    /* Name of the project. */
    private String name;
    
    /* Container panel of this project (the one inside the corresponding tabbed pane). */
    private ContainerPanel containerPanel;

    /* List of studies in this project. */
    private List<Study> studiesList;

    /* Selected study. The selected study will serve as input to new studies. */
    private Study selectedStudy;
    
    private final Object selectedStudyLock = new Object();

    public Project(int ID)
    {
        assert ID >= 0;

        this.name = "Project " + ID;
        this.ID = ID;
        this.studiesList = new ArrayList<Study>();
        this.selectedStudy = null;

        /* Add information panel as an observer, to change information every time a panel is selected. */
        this.addObserver(GeneInformationPanel.getInstance());
        
        /* Add 3D viewer as an observer to show the correct protein every time a panel is selected. */
        this.addObserver(Protein3DViewerPanel.getInstance());
    }

    public synchronized void setContainerPanel(ContainerPanel newPanel)
    {
        assert newPanel != null;

        this.containerPanel = newPanel;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized void setName(String name)
    {
        assert name != null;

        this.name = name;
    }

    /**
     * Add new study to the project.
     * 
     * @param study study to add
     */
    public void addNewStudy(Study study)
    {
        assert study != null;
        assert containerPanel != null;
        assert studiesList != null;

        /* Add study to list of studies in this project. */
        this.studiesList.add(study);

        /* Set new study project as this project. */
        study.setProject(this);

        /* Create a new content panel from the given study, and add it to the container panel of this project. */
        if (study.getCurrentPanel() != null)
            containerPanel.addContentPanel(study.getCurrentPanel());
        else
            if (study.getResultingGenes().size() > 1)
                containerPanel.addContentPanel(study.makePanelFromResultingGenes());
            else
                containerPanel.addContentPanel(study.makePanelFromResultingGene());

        /* If no study selected yet, select this one. */
        synchronized (selectedStudyLock)
        {
            if (selectedStudy == null)
                setSelectedStudy(study);
        }
        
        /* Update redesign panel buttons. */
        StudyMakerPanel.getInstance().getButtonsPanel().enableDisableButtons();
        
        /* Update tabbed projects panel. */
        TabbedProjectsPanel.getInstance().updateUI();
    }

    public synchronized void importGeneToCurrentProject(Gene gene)
    {
        assert gene != null;

        /* Create title. */
        String genomeName = gene.getGenome() != null ? gene.getGenome().getSmallName() : gene.getGenomeName();
        String geneName = gene.getProductName() != null? (gene.getProductName() + " ("+gene.getName()+")") : gene.getName();
        
        /* Create study */
        Study newStudy = new Study(gene, gene, "["+ genomeName +"]  " + geneName);
        
        /* Add study to project (and create panel). */
        addNewStudy(newStudy);
        
        /* EXPERIMENTAL */
        StudyMakerPanel.getInstance().setPluginsGenome(HostSelectionPanel.getSelectedGenome());
        
//        List<Gene> list = gene.generateRandomSynonymousGenes(20);
//        GeneProfileList pList = new GeneProfileList(gene);
//        for(Gene g : list){
//            pList.addOptimizedGene(g);
//        }
//        Gene g = pList.getHighestScoreGene();
//        System.out.println(pList.getProfileScore(g.getName()));
        
        /* Start auto discover when importing genes */
        new GeneAutoDiscover(newStudy).start(); 
    }

    public synchronized void importGenesToCurrentProject(List<Gene> genes)
    {
        assert genes != null;
        assert genes.size() > 0;

        Study newStudy = new Study(genes.get(0), genes, "Multiple genes");
        addNewStudy(newStudy);
    }
    
    public void setSelectedStudy(Study study)
    {
        assert study != null;
        assert study.getCurrentPanel() != null;

        synchronized (selectedStudyLock)
        {
            selectedStudy = study;
        
            /* Deselect all studies. */
            for (Study s : studiesList)
            {
                if (s.getCurrentPanel() != null)
                    s.getCurrentPanel().setSelected(false);
            }

            /* Select the study. */
            study.getCurrentPanel().setSelected(true);
        }
        
        /* Notify information panel that a new study was selected. */
        setChanged();
        notifyObservers(study);
    }

    public Study getSelectedStudy()
    {
        synchronized (selectedStudyLock)
        {
            return selectedStudy;
        }
    }

    public synchronized ContainerPanel getContainerPanel()
    {
        return containerPanel;
    }

    public synchronized List<Study> getStudiesList() {
        return studiesList;
    }

    public synchronized void removeStudy(Study study)
    {
        assert study != null;

        studiesList.remove(study);
        containerPanel.removeContentPanel(study.getCurrentPanel());

        if (study.equals(getSelectedStudy()))
            if (!studiesList.isEmpty())
                setSelectedStudy(studiesList.get(studiesList.size() - 1));
            else
                selectedStudy = null;
        
        /* Update panels. */
        GeneInformationPanel.getInstance().updateInformationForSelectedStudy();
        Protein3DViewerPanel.getInstance().updateViewerForSelectedStudy();
        
        /* Update redesign panel buttons. */
        StudyMakerPanel.getInstance().getButtonsPanel().enableDisableButtons();
    }
}
