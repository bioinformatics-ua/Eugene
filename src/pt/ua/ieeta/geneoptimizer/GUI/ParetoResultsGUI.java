/*
 * ParetoFrontResultsTable.java
 *
 * Created on 26/Abr/2011, 15:56:04
 */

package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationSolution;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationSolutionSet;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;

/**
 *
 * @author Paulo
 */
public class ParetoResultsGUI extends javax.swing.JFrame implements Runnable
{
    private ParetoResultsTableModel model;

    private List<Float> highest, lowest;

    private OptimizationSolutionSet solutionSet;
    private ResultKeeper resultKeeper;
    private int selectedSolutionIndex;
    private String resultSequence;

    /** Creates new form ParetoFrontResultsTable */
    public ParetoResultsGUI(final OptimizationSolutionSet solutionSet, ResultKeeper result)
    {
         initComponents();

         List<String> columnNames = solutionSet.getRedesignNames();
         this.solutionSet = solutionSet;
         this.selectedSolutionIndex = solutionSet.getBestSolutionIndex();

         /* Create table model. */
         model = new ParetoResultsTableModel(columnNames, solutionSet);

         int numberOfColumns = solutionSet.getNumValuesPerSolution();

         highest = new ArrayList<Float>(numberOfColumns);
         lowest = new ArrayList<Float>(numberOfColumns);

         /* Create sorter.*/
         final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
         sorter.setRowFilter(null);
         paretoTable.setRowSorter(sorter);

         /* Populate table. Also, find the highest and lowest values (for colours). */
         for (int i=0; i < solutionSet.getSize(); i++)
            /* Find higher and lower values. */
            for (int j=0; j<numberOfColumns; j++)
                /* For the first solution. */
                if (highest.size() < numberOfColumns)
                {
                    highest.add(j, solutionSet.getSolutionValue(i, j));
                    lowest.add(j, solutionSet.getSolutionValue(i, j));
                }
                /* For the remaining solutions. */
                else
                {
                    if (solutionSet.getSolutionValue(i, j) > highest.get(j))
                        highest.set(j, solutionSet.getSolutionValue(i, j));
                    if (solutionSet.getSolutionValue(i, j) < lowest.get(j))
                        lowest.set(j, solutionSet.getSolutionValue(i, j));
                }

         /* Find the highest and lowest total scores. */
         int high_index = 0, low_index = 0;
         for (int i=1; i < solutionSet.getSize(); i++)
         {
             if (solutionSet.getSolutionFinalScore(i) > solutionSet.getSolutionFinalScore(high_index))
                 high_index = i;
             if (solutionSet.getSolutionFinalScore(i) < solutionSet.getSolutionFinalScore(low_index))
                 low_index = i;
         }
         highest.add(0, solutionSet.getSolutionFinalScore(high_index));
         lowest.add(0, solutionSet.getSolutionFinalScore(low_index));

         paretoTable.setModel(model);
         paretoTable.setDefaultRenderer(Float.class, new CellRenderer());
         paretoTable.setRowSelectionAllowed(true);
         paretoTable.setColumnSelectionAllowed(false);
         paretoTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         
         
         // Sort results and select the first (best result)
         ArrayList sortKeysList = new ArrayList();
         sortKeysList.add(new TableRowSorter.SortKey(0, SortOrder.DESCENDING));
         sorter.setSortKeys(sortKeysList);
         sorter.sort();
         
         paretoTable.getSelectionModel().setSelectionInterval(0, 0);
         

         /* Create event to handle the double click on a gene of the list. */
         paretoTable.addMouseListener(
                 new MouseAdapter()
                 {
                    @Override
                     public void mouseClicked(MouseEvent e)
                     {
                        selectedSolutionIndex = sorter.convertRowIndexToModel(paretoTable.getSelectedRow());

                        if ((e.getClickCount() == 2) && (ProjectManager.getInstance().getSelectedProject() != null))
                             submitResult();
                     }
                 });

         ParetoPlotter plotter = new ParetoPlotter(solutionSet);

         /********* TESTE *********/
            JFrame frame = new JFrame();
            JPanel panel = new JPanel();

            panel.add(plotter);
            frame.add(panel);

            frame.pack();
         /*************************/

         this.chartPanel.add(plotter);

         this.resultKeeper = result;
         
         
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        paretoTable = new javax.swing.JTable();
        chartPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Optimization results - Pareto front solutions");

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        paretoTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        paretoTable.setToolTipText("Pareto front results from the redesign process. Each column shows the score for each selected redesign method. Double click a row to upload the gene to the workspace.");
        jScrollPane1.setViewportView(paretoTable);

        jTabbedPane1.addTab("Solution Table", jScrollPane1);

        chartPanel.setMaximumSize(new java.awt.Dimension(300, 400));
        chartPanel.setMinimumSize(new java.awt.Dimension(300, 400));
        chartPanel.setPreferredSize(new java.awt.Dimension(300, 400));

        javax.swing.GroupLayout chartPanelLayout = new javax.swing.GroupLayout(chartPanel);
        chartPanel.setLayout(chartPanelLayout);
        chartPanelLayout.setHorizontalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 346, Short.MAX_VALUE)
        );
        chartPanelLayout.setVerticalGroup(
            chartPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Solution Chart", chartPanel);

        jButton1.setText("Upload selected solution to workspace");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 351, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(jButton1)
                .addContainerGap(75, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        submitResult();
    }//GEN-LAST:event_jButton1ActionPerformed

    public void submitResult()
    {
        /* Upload result to result keeper. */
        resultKeeper.setResult(solutionSet.getSolution(selectedSolutionIndex));

        /* Kill this thread. */
        synchronized (this)
        { notifyAll(); }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel chartPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable paretoTable;
    // End of variables declaration//GEN-END:variables

    public void run()
    {
        pack();
        setVisible(true);

        /* Wait for user to select a sequence. */
        synchronized (this)
        {
            try { wait(); }
            catch (InterruptedException ex) {} //TODO: exceptions
        }
        
        this.dispose();
    }

    private class CellRenderer extends DefaultTableCellRenderer
    {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
        {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (isSelected)
            {
                c.setBackground(table.getSelectionBackground());
                c.setForeground(table.getSelectionForeground());
            }
            else {
                float colourValue = 100-100*((Float)value - lowest.get(column))/(highest.get(column) - lowest.get(column));
                c.setBackground(new Color(155 + (int)colourValue, 255, 155 + (int)colourValue));
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }
    
    /* TESTE */
    public static void main(String[] paramArrayOfString) throws InterruptedException
    {
        /* Create a random set for testing. */
        OptimizationSolutionSet set = new OptimizationSolutionSet();
        Random rand = new Random();

        for (int i=0; i<5; i++)
        {
            OptimizationSolution solution = new OptimizationSolution("Solution" + i);
            for (int j=0; j<4; j++)
                solution.addRedesignScore("Redesign"+j, 80 + 10*rand.nextFloat());

            set.addSolution(solution);
        }

        ParetoResultsGUI scatterPlot = new ParetoResultsGUI(set, new ResultKeeper());
        Thread newThread = new Thread(scatterPlot);
        newThread.start();
    }

}
