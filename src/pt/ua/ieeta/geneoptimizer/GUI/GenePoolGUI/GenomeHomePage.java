package pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.DefaultFileParser;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.FastaParser;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.GeneticCodeTableParser;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.IGenomeFileParser;
import pt.ua.ieeta.geneoptimizer.GUI.MessageWindow;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo Gaspar
 */
public class GenomeHomePage implements Observer {

    private Genome genome;
    private GenesTableModel model = null;

    /* Sorting the table. */
    private TableRowSorter<TableModel> sorter = null;
    /* Genes table. */
    private javax.swing.JTable genesTable = null;
    private javax.swing.JScrollPane scrollPane = null;
    private String lastSearchingText = "";
    private List<String> manuallyAddedGenes;

    public GenomeHomePage(Genome geno) {
        this.genome = geno;

        this.genesTable = new javax.swing.JTable();

        this.genesTable.setToolTipText("Double click to open sequence");
        this.genesTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        this.genesTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        this.genesTable.setShowVerticalLines(false);
        this.genesTable.getTableHeader().setReorderingAllowed(false);
        this.scrollPane = new javax.swing.JScrollPane();
        this.scrollPane.setViewportView(genesTable);

        /* Create the table model. */
        this.model = new GenesTableModel();
        this.genesTable.setModel(this.model);

        /* Create sorter. */
        this.sorter = new TableRowSorter<TableModel>(model);
        this.sorter.setRowFilter(null);
        this.genesTable.setRowSorter(sorter);

        this.manuallyAddedGenes = new ArrayList<String>();

        genesTable.getSelectionModel().setSelectionInterval(0, 0);

        /* Create event to handle the double click on a gene of the list. */
        this.genesTable.addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                /* check if clicked row is a custom gene */
                String geneHeader = model.getHeaderAt(sorter.convertRowIndexToModel(genesTable.getSelectedRow()));
                if (manuallyAddedGenes.contains(geneHeader)) {
                    GenePoolGUI.getInstance().setAddOrRemoveLabel(false);
                } else {
                    GenePoolGUI.getInstance().setAddOrRemoveLabel(true);
                }


                GenePoolGUI.getUploadGeneButton().setEnabled(true);

                if ((e.getClickCount() == 2) && (ProjectManager.getInstance().getSelectedProject() != null) && genesTable.isEnabled()) {
                    uploadSelectedGeneToWorkSpace();
                }
            }
        });

        genome.getUsageAndContextTables().addObserver(this);
    }

    public JTable getGenesTable() {
        return genesTable;
    }

    public Genome getGenome() {
        return genome;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public void uploadSelectedGeneToWorkSpace() {
        assert genesTable.getSelectedRow() >= 0;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Thread messageW = new Thread(new MessageWindow(GenePoolGUI.getInstance(), true, false, "Loading gene and creating panel, please wait."));
                messageW.start();

                try {
                    Genome selectedGenome = model.getGenomeAt(sorter.convertRowIndexToModel(genesTable.getSelectedRow()));
                    String geneHeader = model.getHeaderAt(sorter.convertRowIndexToModel(genesTable.getSelectedRow()));
                    int size = model.getSizeAt(sorter.convertRowIndexToModel(genesTable.getSelectedRow()));
                    GeneticCodeTable geneticCodeTable = GeneticCodeTableParser.getInstance().getCodeTableByID(GenePoolGUI.getSelectedCodeTableId());
                    String[] genesFiles = selectedGenome.getGenesFiles();

                    IGenomeFileParser parser;
                    Gene selectedGene;                    
                    selectedGene = selectedGenome.getManuallyAddedGene(geneHeader, size);
                    if (selectedGene == null) {
                        for (int i = 0; i < genesFiles.length; i++) {
                            String fileExtension = genesFiles[i].substring(genesFiles[i].lastIndexOf(".") + 1, genesFiles[i].length());
                            if (fileExtension.equals("fa") || fileExtension.equals("ffn") || fileExtension.equals("fna") || fileExtension.equals("fasta") || fileExtension.equals("txt")) {
                                parser = new FastaParser(genome.getFilters());
                            } else {
                                parser = new DefaultFileParser(genome.getFilters());
                            }
                            selectedGene = parser.readGeneFromFile(genesFiles[i], geneHeader, size, geneticCodeTable, genome);
                            if (selectedGene != null) {
                                break;
                            }
                        }
                    }
                    if (selectedGene == null) {
                        System.out.println("An error occured while uploading the gene to the workspace.");
                        messageW.interrupt();
                        messageW = new Thread(new MessageWindow(GenePoolGUI.getInstance(), false, true, "An error occured while uploading the gene to the workspace."));
                        messageW.start();

                        return;
                    }
                    ProjectManager.getInstance().getSelectedProject().importGeneToCurrentProject(selectedGene);
                } catch (IOException ex) {
                    messageW.interrupt();
                    GenePoolGUI.getUploadGeneButton().setEnabled(true);

                    System.out.println("An exception occured while uploading the gene to the workspace: " + ex.getMessage());
                }

                messageW.interrupt();
                GenePoolGUI.getUploadGeneButton().setEnabled(true);

            }
        }).start();
    }

    public void fillGenesTable() {
        assert model != null;
        assert GenePoolGUI.getSearchLabel() != null;
        assert genome != null;
        assert genesTable != null;

        GenePoolGUI.getSearchLabel().setText("");

        /* Clear everything in table.
         */
        model.clearData();

        /*
         * Add selected genome genes to table.
         */
//    			for (Gene g : genome.getGenes()) //model.insertRow(genesTable.getRowCount(), new Object[]{g.getName(), g.getSequenceLength(), g});
//    			{
//    				model.insertRow(new Object[]{g.getGeneHeader(), g.getSequenceLength(), g.getGenome()});
//    			}
        int i = 0;        
        for (String g : genome.getGenesHeaders()) //model.insertRow(genesTable.getRowCount(), new Object[]{g.getName(), g.getSequenceLength(), g});
        {
            if (genome.getManuallyAddedGene(g, genome.getGeneLength(i)) != null) {
                manuallyAddedGenes.add(g);
            }
            model.insertRow(new Object[]{g, genome.getGeneLength(i), genome});
            i++;
        }

        filterTable();

        if (!genome.getUsageAndContextTables().isComplete()) {
            genesTable.setEnabled(false);
            genesTable.setBackground(Color.LIGHT_GRAY);
        }
        genesTable.getColumnModel().getColumn(0).setCellRenderer(new CustomTableCellRenderer(manuallyAddedGenes, 0));
        genesTable.getColumnModel().getColumn(1).setCellRenderer(new CustomTableCellRenderer(manuallyAddedGenes, 1));



        /* Update GUI. */
        genesTable.updateUI();
    }

    public boolean removeSelectedGene() {
        String geneHeader = model.getHeaderAt(sorter.convertRowIndexToModel(genesTable.getSelectedRow()));
        int size = model.getSizeAt(sorter.convertRowIndexToModel(genesTable.getSelectedRow()));
        if (!manuallyAddedGenes.contains(geneHeader)) {
            return false;
        }
        Gene g = genome.getManuallyAddedGene(geneHeader, size);
        genome.removeManuallyAddedGenes(g);
        manuallyAddedGenes.remove(geneHeader);

        fillGenesTable();

        return true;
    }

    public void filterTable() {
        assert GenePoolGUI.getSearchLabel() != null;

        String filteringText = GenePoolGUI.getSearchLabel().getText();

        if (filteringText.length() != 0) {
            sorter.setRowFilter(RowFilter.regexFilter(filteringText));
        } else {
            sorter.setRowFilter(null);
        }

        lastSearchingText = filteringText;
    }

    public String getLastSearchingText() {
        return lastSearchingText;
    }

    @Override
    public void update(Observable o, Object o1) {
        System.out.println("Notified that calculations are done.");
        genesTable.setEnabled(true);
        genesTable.setBackground(Color.WHITE);
        genesTable.updateUI();
    }
}
