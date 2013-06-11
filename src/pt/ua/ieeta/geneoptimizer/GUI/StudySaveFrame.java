/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * StudySaveFrame.java
 *
 * Created on 30/Mar/2010, 20:19:23
 */
package pt.ua.ieeta.geneoptimizer.GUI;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel.StudyMakerPanel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationModel;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.RedesignProtocolReaderWriter;

/**
 *
 * @author Paulo
 */
public class StudySaveFrame extends javax.swing.JFrame {

    private static StudySaveFrame instance = null;

    public static StudySaveFrame getInstance() {
        if (instance == null) {
            instance = new StudySaveFrame();
        }

        return instance;
    }

    /**
     * Creates new form StudySaveFrame
     */
    private StudySaveFrame() {
        initComponents();
        this.setLocationRelativeTo(null);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();

        setTitle("Save study");
        setAlwaysOnTop(true);
        setResizable(false);

        jLabel1.setText("Choose a name for the study:");

        jTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField1.setText("newStudy");

        jButton1.setText("Save");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        this.dispose();

        OptimizationReport report = OptimizationModel.getInstance().getStudyParameters();
        if (report == null) {
            return;
        }

        report.setReportName(jTextField1.getText());
        if (RedesignProtocolReaderWriter.getInstance().saveParametersToFile(report)) {
            JOptionPane.showMessageDialog(this, "The current selected plugins parameters were successfully saved!", "Parameters sucessfully saved", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Couldn't save the plugins options!", "Error saving plugin options", JOptionPane.ERROR_MESSAGE);
        }
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
