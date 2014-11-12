/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.harmonization;

import java.util.Observable;
import java.util.Observer;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Eduardo
 */
public class HarmonizationParametersPanel extends JPanel implements Observer{
    public JComboBox comboBoxHostSpecie;
    private JLabel jLabel1;

    public HarmonizationParametersPanel(GenePool genePool) {
        initComponents();

        GenePool.registerObserver(this);

        for (Genome genome : genePool.getGenomes()) {
            this.comboBoxHostSpecie.addItem(new HarmonizationParametersPanel.ComboBoxItem(genome.getName(), genome));
        }
    }

    public void update(Observable o, Object arg) {
        Genome genome = (Genome) arg;
        this.comboBoxHostSpecie.addItem(new HarmonizationParametersPanel.ComboBoxItem(genome.getName(), genome));
    }

    public Genome getSelectedGenome() {
        return ((HarmonizationParametersPanel.ComboBoxItem) this.comboBoxHostSpecie.getSelectedItem()).genome;
    }

    public void setSelectedGenome(Genome genome) {
        assert (genome != null);

        for (int i = 0; i < this.comboBoxHostSpecie.getItemCount(); i++) {
            if (((Genome) this.comboBoxHostSpecie.getItemAt(i)).getName().equals(genome.getName())) {
                this.comboBoxHostSpecie.setSelectedIndex(i);
            }
        }
    }

    private void initComponents() {
        this.comboBoxHostSpecie = new JComboBox();
        this.jLabel1 = new JLabel();

        this.comboBoxHostSpecie.setEnabled(false);

        this.jLabel1.setText("Host Species: ");

        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.comboBoxHostSpecie, -2, 117, -2).addComponent(this.jLabel1)).addContainerGap(-1, 32767)));

        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.comboBoxHostSpecie, -2, -1, -2).addContainerGap(-1, 32767)));
    }

    class ComboBoxItem {

        public String item;
        public Genome genome;

        public ComboBoxItem(String item, Genome genome) {
            this.item = item;
            this.genome = genome;
        }

        public String toString() {
            return this.item;
        }
    }
}
