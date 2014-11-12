/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.siteremoval;

import java.awt.Dimension;
import java.awt.Font;
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
public class SiteRemovalParametersPanel extends JPanel{
    private ButtonGroup buttonGroup1;
    public JCheckBox jACACheckBox;
    public JCheckBox jCustomCheckBox;
    public JTextField jCustomTextField;
    public JCheckBox jKozakCheckBox;
    private JLabel jLabel1;
    public JCheckBox jRNaseCheckBox;
    public JCheckBox jShineCheckBox;

    public SiteRemovalParametersPanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.jShineCheckBox = new JCheckBox();
        this.jKozakCheckBox = new JCheckBox();
        this.jACACheckBox = new JCheckBox();
        this.jRNaseCheckBox = new JCheckBox();
        this.jLabel1 = new JLabel();
        this.jCustomCheckBox = new JCheckBox();
        this.jCustomTextField = new JTextField();

        setPreferredSize(new Dimension(177, 100));
        setLayout(new GridBagLayout());

        this.jShineCheckBox.setText("Shine-Dalg.");
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        add(this.jShineCheckBox, gridBagConstraints);

        this.jKozakCheckBox.setText("Kozak");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        add(this.jKozakCheckBox, gridBagConstraints);

        this.jACACheckBox.setText("\"ACA\" Maz F");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(0, 0, 0, 11);
        add(this.jACACheckBox, gridBagConstraints);

        this.jRNaseCheckBox.setText("RNase");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = 17;
        add(this.jRNaseCheckBox, gridBagConstraints);

        this.jLabel1.setFont(new Font("Tahoma", 1, 11));
        this.jLabel1.setText("Sites to avoid/remove:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 18;
        gridBagConstraints.insets = new Insets(0, 5, 0, 0);
        add(this.jLabel1, gridBagConstraints);

        this.jCustomCheckBox.setText("Custom site:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = 2;
        gridBagConstraints.anchor = 17;
        add(this.jCustomCheckBox, gridBagConstraints);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = 2;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.anchor = 17;
        gridBagConstraints.insets = new Insets(0, 3, 0, 15);
        add(this.jCustomTextField, gridBagConstraints);
    }
}
