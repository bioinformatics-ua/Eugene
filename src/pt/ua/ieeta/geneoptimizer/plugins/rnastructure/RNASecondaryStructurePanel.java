/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.rnastructure;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Eduardo
 */
public class RNASecondaryStructurePanel extends JPanel {
    private ButtonGroup buttonGroup1;
    public JRadioButton jMaximizeFreeEnergy;
    public JRadioButton jMinimizeFreeEnergy;

    public RNASecondaryStructurePanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.jMinimizeFreeEnergy = new JRadioButton();
        this.jMaximizeFreeEnergy = new JRadioButton();

        setPreferredSize(new Dimension(177, 100));
        setLayout(new GridBagLayout());

        this.buttonGroup1.add(this.jMinimizeFreeEnergy);
        this.jMinimizeFreeEnergy.setText("Minimize free-energie");
        this.jMinimizeFreeEnergy.setEnabled(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 18;
        add(this.jMinimizeFreeEnergy, gridBagConstraints);

        this.buttonGroup1.add(this.jMaximizeFreeEnergy);
        this.jMaximizeFreeEnergy.setSelected(true);
        this.jMaximizeFreeEnergy.setText("Maximize free-energie");
        this.jMaximizeFreeEnergy.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 18;
        add(this.jMaximizeFreeEnergy, gridBagConstraints);
    }
}
