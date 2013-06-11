/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ieeta.geneoptimizer.GUI;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationSolutionSet;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 */
public final class ParetoResultsTableModel  extends AbstractTableModel
{
       private Vector<String> columnNames;;
       private Vector<Object[]> data;
       private int numColumns;

       public ParetoResultsTableModel(Vector<String> columnNames, OptimizationSolutionSet solutionSet)
       {
            this.columnNames = new Vector<String>(columnNames.size()+1);
            this.columnNames.addAll(columnNames);
            this.columnNames.add(0, "Total Score");
            data = new Vector<Object[]>();
            numColumns = this.columnNames.size();
            for (int i=0; i < solutionSet.getSize(); i++)
                insertRow(solutionSet.getSolutionValues(i), solutionSet.getSolutionFinalScore(i));
       }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount() {
            return numColumns;
        }

       @Override
       public String getColumnName(int col) {
           return columnNames.get(col);
       }

       public Object getValueAt(int rowIndex, int columnIndex)
       {
           return ((Object[])data.get(rowIndex))[columnIndex];
       }

       public Gene getGeneAt(int rowIndex)
       {
           return (Gene)((Object[])data.get(rowIndex))[2];
       }

       @Override
       public Class getColumnClass(int c) 
       {
           return Float.class;
       }

       public void insertRow(Object[] rowData, float totalScore)
       {
           /* Add */
           Object [] newData = new Object[rowData.length+1];
           System.arraycopy(rowData, 0, newData, 1, rowData.length);
           newData[0] = totalScore;
           
           data.add(newData);
       }

       public Vector getDataVector()
       {
            return data;
       }

       public void clearData()
       {
            data.clear();
       }

    
}
