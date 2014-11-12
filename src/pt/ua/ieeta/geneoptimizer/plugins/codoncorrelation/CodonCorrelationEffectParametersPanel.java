/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.ua.ieeta.geneoptimizer.plugins.codoncorrelation;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;

/**
 *
 * @author Eduardo
 */
public class CodonCorrelationEffectParametersPanel extends JPanel{
    private ButtonGroup buttonGroup1;
    private ButtonGroup buttonGroup2;
    public JLabel jLabelRampEffectValue;
    public JRadioButton jMaximizeRadioButton;
    public JRadioButton jMinimizeRadioButton;
    public JCheckBox jRampEffectCheckBox;
    public JSlider jSliderRampEffect;

    public CodonCorrelationEffectParametersPanel() {
        initComponents();
    }

    private void initComponents() {
        this.buttonGroup1 = new ButtonGroup();
        this.buttonGroup2 = new ButtonGroup();
        this.jMaximizeRadioButton = new JRadioButton();
        this.jMinimizeRadioButton = new JRadioButton();
        this.jRampEffectCheckBox = new JCheckBox();
        this.jSliderRampEffect = new JSlider();
        this.jLabelRampEffectValue = new JLabel();

        setLayout(new GridBagLayout());

        this.buttonGroup1.add(this.jMaximizeRadioButton);
        this.jMaximizeRadioButton.setSelected(true);
        this.jMaximizeRadioButton.setText("Maximize codon correlation");
        this.jMaximizeRadioButton.setEnabled(false);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = 18;
        add(this.jMaximizeRadioButton, gridBagConstraints);

        this.buttonGroup1.add(this.jMinimizeRadioButton);
        this.jMinimizeRadioButton.setText("Minimize codon correlation");
        this.jMinimizeRadioButton.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.ipadx = 2;
        gridBagConstraints.anchor = 18;
        add(this.jMinimizeRadioButton, gridBagConstraints);

        this.jRampEffectCheckBox.setText("Ramp effect");
        this.jRampEffectCheckBox.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = 23;
        add(this.jRampEffectCheckBox, gridBagConstraints);

        this.jSliderRampEffect.setMajorTickSpacing(10);
        this.jSliderRampEffect.setMinorTickSpacing(5);
        this.jSliderRampEffect.setPaintTicks(true);
        this.jSliderRampEffect.setEnabled(false);
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(this.jSliderRampEffect, gridBagConstraints);

        this.jLabelRampEffectValue.setHorizontalAlignment(0);
        this.jLabelRampEffectValue.setText("50%");
        this.jLabelRampEffectValue.setEnabled(false);
        this.jLabelRampEffectValue.setHorizontalTextPosition(0);
        this.jLabelRampEffectValue.setPreferredSize(new Dimension(32, 14));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        add(this.jLabelRampEffectValue, gridBagConstraints);
    }
}
