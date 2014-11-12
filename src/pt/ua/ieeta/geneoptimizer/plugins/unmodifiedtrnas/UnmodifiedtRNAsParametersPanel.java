/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.unmodifiedtrnas;

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
public class UnmodifiedtRNAsParametersPanel extends JPanel{
    private ButtonGroup buttonGroup1;
    public JRadioButton jBacteriasButton;
    public JRadioButton jEukaryotesButton;

    public UnmodifiedtRNAsParametersPanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.jBacteriasButton = new JRadioButton();
        this.jEukaryotesButton = new JRadioButton();

        setPreferredSize(new Dimension(177, 60));
        setLayout(new GridBagLayout());

        this.buttonGroup1.add(this.jBacteriasButton);
        this.jBacteriasButton.setSelected(true);
        this.jBacteriasButton.setText("Bacterias");
        this.jBacteriasButton.setEnabled(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        add(this.jBacteriasButton, gridBagConstraints);

        this.buttonGroup1.add(this.jEukaryotesButton);
        this.jEukaryotesButton.setText("Eukaryotes");
        this.jEukaryotesButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        add(this.jEukaryotesButton, gridBagConstraints);
    }
}
