/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.hiddenstops;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author Eduardo
 */
public class HiddenStopCodonsParametersPanel extends JPanel {
    private ButtonGroup buttonGroup1;
    public JRadioButton jCustomHiddenButton;
    public JTextField jCustomHiddenTextField;
    public JRadioButton jMaximizeHiddenButton;
    public JRadioButton jMinimizeHiddenButton;

    public HiddenStopCodonsParametersPanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.jMaximizeHiddenButton = new JRadioButton();
        this.jMinimizeHiddenButton = new JRadioButton();
        this.jCustomHiddenTextField = new JTextField();
        this.jCustomHiddenButton = new JRadioButton();

        setPreferredSize(new Dimension(177, 100));
        setLayout(new GridBagLayout());

        this.buttonGroup1.add(this.jMaximizeHiddenButton);
        this.jMaximizeHiddenButton.setSelected(true);
        this.jMaximizeHiddenButton.setText("Maximize hidden stop codons");
        this.jMaximizeHiddenButton.setEnabled(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = 18;
        add(this.jMaximizeHiddenButton, gridBagConstraints);

        this.buttonGroup1.add(this.jMinimizeHiddenButton);
        this.jMinimizeHiddenButton.setText("Minimize hidden stop codons");
        this.jMinimizeHiddenButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = 18;
        add(this.jMinimizeHiddenButton, gridBagConstraints);

        this.jCustomHiddenTextField.setHorizontalAlignment(0);
        this.jCustomHiddenTextField.setText("3");
        this.jCustomHiddenTextField.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 34;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(2, 2, 2, 2);
        add(this.jCustomHiddenTextField, gridBagConstraints);

        this.buttonGroup1.add(this.jCustomHiddenButton);
        this.jCustomHiddenButton.setText("Custom number");
        this.jCustomHiddenButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = 18;
        add(this.jCustomHiddenButton, gridBagConstraints);
    }
}
