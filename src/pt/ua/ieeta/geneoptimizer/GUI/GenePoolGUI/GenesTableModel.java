package pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI;

import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo
 */
@SuppressWarnings("UseOfObsoleteCollectionType")
public class GenesTableModel extends AbstractTableModel {

    private String[] columnNames = {"Gene Name", "Size (codons)"};
    private List<Object[]> data = new ArrayList<Object[]>();

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return ((Object[]) data.get(rowIndex))[columnIndex];
    }

    public String getHeaderAt(int rowIndex) {
        return (String) ((Object[]) data.get(rowIndex))[0];
    }

    public int getSizeAt(int rowIndex) {
        return (Integer) ((Object[]) data.get(rowIndex))[1];
    }

    public Genome getGenomeAt(int rowIndex) {
        return (Genome) ((Object[]) data.get(rowIndex))[2];
    }

    @Override
    public Class getColumnClass(int c) {
        switch (c) {
            case 0:
                return String.class;
            case 1:
                return Integer.class;
            case 2:
                return Gene.class;
        }

        return Object.class;
    }

    public void insertRow(Object[] rowData) {
        data.add(rowData);
    }

    public List getDataList() {
        return data;
    }

    public void clearData() {
        data.clear();
    }
}
