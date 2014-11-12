/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.codoncontext;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Eduardo
 */
public class CodonContextParametersPanel extends JPanel {
    
    private ButtonGroup buttonGroup1;
    public JRadioButton jMaximizeContextButton;
    public JRadioButton jMinimizeContextButton;

    public CodonContextParametersPanel()
    {
        initComponents();
    }

    private void initComponents()
    {
    this.buttonGroup1 = new ButtonGroup();
    this.jMaximizeContextButton = new JRadioButton();
    this.jMinimizeContextButton = new JRadioButton();

    setLayout(new GridBagLayout());

    this.buttonGroup1.add(this.jMaximizeContextButton);
    this.jMaximizeContextButton.setSelected(true);
    this.jMaximizeContextButton.setText("Maximize codon context");
    this.jMaximizeContextButton.setEnabled(false);
    GridBagConstraints gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.anchor = 18;
    add(this.jMaximizeContextButton, gridBagConstraints);

    this.buttonGroup1.add(this.jMinimizeContextButton);
    this.jMinimizeContextButton.setText("Minimize codon context");
    this.jMinimizeContextButton.setEnabled(false);
    gridBagConstraints = new GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = 18;
    add(this.jMinimizeContextButton, gridBagConstraints);
  }
}
