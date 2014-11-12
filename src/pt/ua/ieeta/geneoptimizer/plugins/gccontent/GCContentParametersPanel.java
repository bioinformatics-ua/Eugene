/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.gccontent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author Eduardo
 */
public class GCContentParametersPanel extends JPanel {
    private ButtonGroup buttonGroup1;
    public JRadioButton jCustomGCButton;
    public JTextField jCustomGCTextField;
    private JLabel jLabel1;
    public JRadioButton jMaximizeGCButton;
    public JRadioButton jMinimizeGCButton;

    public GCContentParametersPanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.jMaximizeGCButton = new JRadioButton();
        this.jMinimizeGCButton = new JRadioButton();
        this.jCustomGCButton = new JRadioButton();
        this.jCustomGCTextField = new JTextField();
        this.jLabel1 = new JLabel();

        setLayout(new GridBagLayout());

        this.buttonGroup1.add(this.jMaximizeGCButton);
        this.jMaximizeGCButton.setSelected(true);
        this.jMaximizeGCButton.setText("Maximize GC content");
        this.jMaximizeGCButton.setEnabled(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = 18;
        add(this.jMaximizeGCButton, gridBagConstraints);

        this.buttonGroup1.add(this.jMinimizeGCButton);
        this.jMinimizeGCButton.setText("Minimize GC content");
        this.jMinimizeGCButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 18;
        add(this.jMinimizeGCButton, gridBagConstraints);

        this.buttonGroup1.add(this.jCustomGCButton);
        this.jCustomGCButton.setText("Custom GC content:");
        this.jCustomGCButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = 18;
        add(this.jCustomGCButton, gridBagConstraints);

        this.jCustomGCTextField.setHorizontalAlignment(0);
        this.jCustomGCTextField.setText("100");
        this.jCustomGCTextField.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.ipadx = 18;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(1, 3, 0, 0);
        add(this.jCustomGCTextField, gridBagConstraints);

        this.jLabel1.setText("%");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(4, 1, 1, 7);
        add(this.jLabel1, gridBagConstraints);
    }
}
