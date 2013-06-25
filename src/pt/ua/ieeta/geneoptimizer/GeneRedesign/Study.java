package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pt.ua.ieeta.geneoptimizer.GUI.ContentPanel;
import pt.ua.ieeta.geneoptimizer.GUI.GenePanel.MultiSequencePanel;
import pt.ua.ieeta.geneoptimizer.GUI.GenePanel.SingleGenePanel;
import pt.ua.ieeta.geneoptimizer.Main.Project;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 */
public class Study
{
    /* Study name. */
    private String name;

    /* Original gene from where the study was made. */
    private final Gene originalGene;

    /* Resulting gene of the study. */
    private final List<Gene> resultingGenes;

    /* Content Panel where the study is shown. */
    private ContentPanel studyPanel = null;

    /* Report of the studies. The last report corresponds to the last study. */
    private List<OptimizationReport> optimizationReports;
       
    /* Project where this study is inserted. */
    private Project project;

    private int selectedStartIndex, selectedEndIndex;
    private boolean hasSelection = false;

    /* Creates a new Study structure, keeping the original gene of the study, and the optimized one (resulting gene). */
    public Study(Gene original, Gene resulting, String studyName)
    {
        assert original != null;
        assert studyName != null;
        assert resulting != null;

        this.originalGene = original;
        this.resultingGenes = Collections.synchronizedList(new ArrayList<Gene>());
        this.resultingGenes.add(resulting);
        this.name = studyName;
        this.optimizationReports = Collections.synchronizedList(new ArrayList<OptimizationReport>());

        /* Ask gene to generate all possible structures. */
        resulting.calculateAllStructures();
    }

    public Study(Gene original, List<Gene> resulting, String studyName)
    {
        assert original != null;
        assert studyName != null;
        assert resulting != null;

        this.originalGene = original;
        this.resultingGenes = resulting;
        this.name = studyName;
        this.optimizationReports = Collections.synchronizedList(new ArrayList<OptimizationReport>());

        /* Ask gene to generate all possible structures. */
        for(Gene gene : resulting)
            gene.calculateAllStructures();
    }

    /* Constructor to join two studies. */
    public Study(Study study1, Study study2)
    {
        assert study1 != null;
        assert study2 != null;

        this.originalGene = study1.getOriginalGene();
        this.resultingGenes = Collections.synchronizedList(new ArrayList<Gene>());
        this.name = study1.getName();
        this.optimizationReports = Collections.synchronizedList(new ArrayList<OptimizationReport>());

        /* Add genes from first study. */
        for(Gene gene : study1.getResultingGenes())
            this.resultingGenes.add(gene);

        /* Add genes from second study. */
        for(Gene gene : study2.getResultingGenes())
            this.resultingGenes.add(gene);
    }

    /* Creates the study panel from the resulting gene. */
    public ContentPanel makePanelFromResultingGene()
    {
        assert resultingGenes != null;
        assert resultingGenes.size() > 0;

        return makePanelFromGene();
    }

    /** Creates the study panel from the resulting genes.*/
    public ContentPanel makePanelFromResultingGenes()
    {
        assert resultingGenes != null;
        assert resultingGenes.size() > 0;

        this.studyPanel = new MultiSequencePanel(this, "Multi-gene panel");
        return this.studyPanel;
    }

    /** Creates the study panel from a given gene. */
    private ContentPanel makePanelFromGene()
    {
        this.studyPanel = new SingleGenePanel(this, originalGene != resultingGenes.get(0));
        return this.studyPanel;
    }

    public ContentPanel getCurrentPanel()
    {
        return this.studyPanel;
    }

    public Gene getOriginalGene()
    {
        return originalGene;
    }

    public Gene getResultingGene()
    {
        assert resultingGenes != null;
        assert resultingGenes.size() > 0;

        return resultingGenes.get(0);
    }

    public List<Gene> getResultingGenes()
    {
        assert resultingGenes != null;

        return resultingGenes;
    }

    public void addResultingGene(Gene gene)
    {
        assert gene != null;

        gene.calculateAllStructures();
        this.resultingGenes.add(gene);
    }

    public boolean isMultiGeneStudy()
    {
        assert resultingGenes != null;

        return resultingGenes.size() > 1;
    }

    public String getName() {
        return name;
    }

    public List<OptimizationReport> getOptimizationReports() {
        return optimizationReports;
    }
    
    public void setOptimizationReport(List<OptimizationReport> report){
        assert report != null;
        
        this.optimizationReports = report;
    }

    public void addOptimizationReport(OptimizationReport report, Study previousStudy)
    {
        assert report != null;
        assert this.optimizationReports != null;
        assert previousStudy != null;

        List<OptimizationReport> previousReport = previousStudy.getOptimizationReports();
        List<OptimizationReport> newReport = new ArrayList<OptimizationReport>(previousReport);

        newReport.add(report);

        this.optimizationReports = newReport;
    }

    public void setPanel(ContentPanel panel)
    {
        this.studyPanel = panel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProject(Project project)
    {
        this.project = project;
    }

    public Project getProject() {
        return project;
    }

    public void setSelection(int startIndex, int endIndex)
    {
        assert startIndex >= 0;
        assert endIndex >= 0;
        assert startIndex < endIndex;

        selectedStartIndex = startIndex;
        selectedEndIndex = endIndex;
        hasSelection = true;
    }

    public void removeSelection()
    {
        hasSelection = false;
    }

    public boolean hasCodonSelection()
    {
        return hasSelection;
    }

    /**
     * @return the selectedStartIndex
     */
    public int getSelectedStartIndex() {
        return selectedStartIndex;
    }

    /**
     * @return the selectedEndIndex
     */
    public int getSelectedEndIndex() {
        return selectedEndIndex;
    }      
}
