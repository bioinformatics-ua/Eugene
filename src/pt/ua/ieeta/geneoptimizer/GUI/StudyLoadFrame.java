/*
 * StudyLoadFrame.java
 *
 * Created on 30/Mar/2010, 21:02:24
 */

package pt.ua.ieeta.geneoptimizer.GUI;

import javax.swing.table.DefaultTableModel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationModel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.RedesignProtocolReaderWriter;

/**
 *
 * @author Paulo
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class StudyLoadFrame extends javax.swing.JFrame
{
    private static volatile StudyLoadFrame instance = null;

    public static StudyLoadFrame getInstance()
    {
        if (instance == null)
        {
            synchronized(StudyLoadFrame.class){
                if (instance == null){
                    instance = new StudyLoadFrame();
                }
            }            
        }
        reloadTable();
        return instance;
    }

    /** Creates new form StudyLoadFrame */
    private StudyLoadFrame() {
        initComponents();
        this.setLocationRelativeTo(null);
    }

    private static void reloadTable()
    {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.getDataVector().removeAllElements();
        
        RedesignProtocolReaderWriter.getInstance().loadParametersFromFile();
        
        for (OptimizationReport report : RedesignProtocolReaderWriter.getInstance().getStudies())
            model.insertRow(jTable1.getRowCount(),  new Object[] {new LoadElement(report)});

        /* Update GUI. */
        jTable1.updateUI();
    }

    private static class LoadElement
    {
        OptimizationReport report;
        LoadElement(OptimizationReport report)
        {
            this.report = report;            
        }

        @Override
        public String toString()
        {
            return report.getReportName();
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);

        jLabel1.setText("Choose a redesign protocol to load:");

        jButton1.setText("Load Selected");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Study Name"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                    .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        /* Obtain selected study report. */        
        OptimizationReport report = (OptimizationReport) ((LoadElement)jTable1.getModel().getValueAt(jTable1.getSelectedRow(), 0)).report;
        
        OptimizationModel.getInstance().setStudyParameters(report);
    }//GEN-LAST:event_jButton1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private static javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables

}
