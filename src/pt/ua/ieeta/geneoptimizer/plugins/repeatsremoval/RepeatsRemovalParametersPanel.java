/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.repeatsremoval;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Eduardo
 */
public class RepeatsRemovalParametersPanel extends JPanel{
    private ButtonGroup buttonGroup1;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    public JTextField jRemoveRepeatCodonTextField;
    public JCheckBox jRemoveRepeatCodonlButton;
    public JCheckBox jRemoveRepeatNuclButton;
    public JTextField jRemoveRepeatNuclTextField;

    public RepeatsRemovalParametersPanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.jRemoveRepeatNuclButton = new JCheckBox();
        this.jRemoveRepeatCodonlButton = new JCheckBox();
        this.jRemoveRepeatNuclTextField = new JTextField();
        this.jLabel1 = new JLabel();
        this.jLabel2 = new JLabel();
        this.jLabel3 = new JLabel();
        this.jRemoveRepeatCodonTextField = new JTextField();
        this.jLabel4 = new JLabel();

        setPreferredSize(new Dimension(177, 100));
        setLayout(new GridBagLayout());

        this.jRemoveRepeatNuclButton.setText("Remove repeated nucleotides");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 18;
        add(this.jRemoveRepeatNuclButton, gridBagConstraints);

        this.jRemoveRepeatCodonlButton.setText("Remove repeated codons");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 18;
        add(this.jRemoveRepeatCodonlButton, gridBagConstraints);

        this.jRemoveRepeatNuclTextField.setHorizontalAlignment(0);
        this.jRemoveRepeatNuclTextField.setText("5");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = 18;
        add(this.jRemoveRepeatNuclTextField, gridBagConstraints);

        this.jLabel1.setText("Threshold:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(2, 21, 2, 10);
        add(this.jLabel1, gridBagConstraints);

        this.jLabel2.setText("repetitions");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(3, 4, 3, 4);
        add(this.jLabel2, gridBagConstraints);

        this.jLabel3.setText("Threshold:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(3, 22, 3, 9);
        add(this.jLabel3, gridBagConstraints);

        this.jRemoveRepeatCodonTextField.setHorizontalAlignment(0);
        this.jRemoveRepeatCodonTextField.setText("2");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(1, 0, 1, 0);
        add(this.jRemoveRepeatCodonTextField, gridBagConstraints);

        this.jLabel4.setText("repetitions");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(4, 4, 2, 3);
        add(this.jLabel4, gridBagConstraints);
    }
}
