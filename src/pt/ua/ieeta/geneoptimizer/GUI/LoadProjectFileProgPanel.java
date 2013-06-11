/*
 * LoadProjectFileProgPanel.java
 */
package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

/**
 * Panel to show the progress of function load project from file
 * 
 * @author Ricardo Gonzaga
 */
public class LoadProjectFileProgPanel extends JDialog {
    private JPanel panel;
    private JProgressBar progressBar;
    private JTextArea outputArea;
    private JButton okButton;
    
    public LoadProjectFileProgPanel(JFrame parent) {
        super(parent, "Loading project from file...");
        panel = new JPanel(new BorderLayout());
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setString("Initiating...");
        progressBar.setStringPainted(true);
        
        outputArea = new JTextArea(15, 70);
        outputArea.setMargin(new Insets(5, 5, 5, 5));
        outputArea.setEditable(false);
        
        okButton = new JButton("OK");
        okButton.setEnabled(false);
        okButton.setAlignmentX(CENTER_ALIGNMENT);
        okButton.setPreferredSize(new Dimension(100, 50));
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              dispose();  
            }
        });
        

        JPanel centerPanel = new JPanel();
        BoxLayout centerLayout = new BoxLayout(centerPanel, BoxLayout.Y_AXIS);
        centerPanel.setLayout(centerLayout);
        centerPanel.add(new JScrollPane(outputArea));
        centerPanel.add(okButton);
        
        panel.add(progressBar, BorderLayout.PAGE_START);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setResizable(false);
        this.getContentPane().add(panel);
        this.pack();
        //this.setVisible(true);
        this.setLocationRelativeTo(null);
        ////this.validateTree();
        this.repaint();
    }

    /**
     * Update the state of the progress panel
     * 
     * @param value a value between 0 and 100
     * @param message a descriptive message
     */
    public void updateProgress(int value, String message) {
        progressBar.setValue(value);
        outputArea.append(message + "\n");
    }
    
    /**
     * Inform that load project has an error
     */
    public void errorProgress() {
        progressBar.setString("ERROR");
        outputArea.append("ERROR LOADING PROJECT\n");
        okButton.setBackground(Color.red);
        okButton.setOpaque(true);
        okButton.setEnabled(true);
    }
    
    /**
     * Inform that load project has complete
     */
    public void completeProgress() {
        progressBar.setValue(100);
        progressBar.setString("Complete");
        outputArea.append("Load project successfully\n");
        okButton.setBackground(Color.green);
        okButton.setOpaque(true);
        okButton.setEnabled(true);
    }
}
