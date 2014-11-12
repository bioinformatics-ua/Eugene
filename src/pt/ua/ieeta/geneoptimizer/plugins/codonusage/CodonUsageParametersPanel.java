/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.codonusage;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 *
 * @author Eduardo
 */
public class CodonUsageParametersPanel extends JPanel {
    private ButtonGroup buttonGroup1;
    private ButtonGroup buttonGroup2;
    public JRadioButton jCAIButton;
    public JRadioButton jHarmonizeUsage;
    public JCheckBox jKeepRareCodons;
    private JLabel jLabel1;
    public JRadioButton jMaximizeUsage;
    public JRadioButton jMinimizeUsage;
    public JRadioButton jRSCUButton;

    public CodonUsageParametersPanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.buttonGroup2 = new ButtonGroup();
        this.jMaximizeUsage = new JRadioButton();
        this.jMinimizeUsage = new JRadioButton();
        this.jLabel1 = new JLabel();
        this.jRSCUButton = new JRadioButton();
        this.jHarmonizeUsage = new JRadioButton();
        this.jCAIButton = new JRadioButton();
        this.jKeepRareCodons = new JCheckBox();

        setLayout(new GridBagLayout());

        this.buttonGroup1.add(this.jMaximizeUsage);
        this.jMaximizeUsage.setSelected(true);
        this.jMaximizeUsage.setText("Maximize codon usage");
        this.jMaximizeUsage.setEnabled(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = 18;
        add(this.jMaximizeUsage, gridBagConstraints);

        this.buttonGroup1.add(this.jMinimizeUsage);
        this.jMinimizeUsage.setText("Minimize codon usage");
        this.jMinimizeUsage.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = 18;
        add(this.jMinimizeUsage, gridBagConstraints);

        this.jLabel1.setText("Use:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(5, 10, 4, 6);
        add(this.jLabel1, gridBagConstraints);

        this.buttonGroup2.add(this.jRSCUButton);
        this.jRSCUButton.setSelected(true);
        this.jRSCUButton.setText("RSCU");
        this.jRSCUButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = 18;
        add(this.jRSCUButton, gridBagConstraints);

        this.buttonGroup1.add(this.jHarmonizeUsage);
        this.jHarmonizeUsage.setText("Harmonize codon usage");
        this.jHarmonizeUsage.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = 18;
        add(this.jHarmonizeUsage, gridBagConstraints);

        this.buttonGroup2.add(this.jCAIButton);
        this.jCAIButton.setText("CAI");
        this.jCAIButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = 18;
        add(this.jCAIButton, gridBagConstraints);

        this.jKeepRareCodons.setText("Keep Rare Codons");
        this.jKeepRareCodons.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = 21;
        add(this.jKeepRareCodons, gridBagConstraints);
    }
}
