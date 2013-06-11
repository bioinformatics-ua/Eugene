/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * HostSelectionPanel.java
 *
 * Created on 16/Set/2010, 15:42:24
 */

package pt.ua.ieeta.geneoptimizer.GUI.RedesignPanel;

import java.util.Observable;
import java.util.Observer;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationModel;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo
 */
public class HostSelectionPanel extends javax.swing.JPanel  implements Observer
{
    private static HostSelectionPanel instance = null;

    /** Creates new form HostSelectionPanel */
    private HostSelectionPanel()
    {
        initComponents();
    }

    public static HostSelectionPanel getInstance()
    {
        if (instance == null)
        {
            instance = new HostSelectionPanel();

            /* Tell gene pool that this class wants to be notified of new genomes added. */
            GenePool.registerObserver(instance);
        }
        
        return instance;
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
        comboBoxHostSpecies = new javax.swing.JComboBox();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));

        jLabel1.setText("Host Species: ");
        add(jLabel1);

        comboBoxHostSpecies.setEnabled(false);
        comboBoxHostSpecies.setMaximumSize(new java.awt.Dimension(110, 27));
        comboBoxHostSpecies.setPreferredSize(new java.awt.Dimension(110, 27));
        comboBoxHostSpecies.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostSelected(evt);
            }
        });
        add(comboBoxHostSpecies);
    }// </editor-fold>//GEN-END:initComponents

private void hostSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostSelected
    OptimizationModel.getInstance().refreshPlugins();
}//GEN-LAST:event_hostSelected


    // Variables declaration - do not modify//GEN-BEGIN:variables
    public static javax.swing.JComboBox comboBoxHostSpecies;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables

    public void update(Observable o, Object arg)
    {
        Genome genome = (Genome) arg;
        comboBoxHostSpecies.addItem( new ComboBoxItem(genome.getName(), genome));
        comboBoxHostSpecies.setEnabled(true);
    }

    private static class ComboBoxItem
    {
       public String item;
       public Genome genome;

       public ComboBoxItem(String item, Genome genome)
       {
           this.item = item;
           this.genome = genome;
       }

       @Override
       public String toString() { return item; }
    }

    public static Genome getSelectedGenome()
    {
        return ((ComboBoxItem)comboBoxHostSpecies.getSelectedItem()).genome;
    }

    public static void setSelectedGenome(Genome genome)
    {
        assert genome != null;

        for (int i=0; i<comboBoxHostSpecies.getItemCount(); i++)
            if (((Genome)comboBoxHostSpecies.getItemAt(i)).getName().equals(genome.getName()))
                comboBoxHostSpecies.setSelectedIndex(i);
    }
}
