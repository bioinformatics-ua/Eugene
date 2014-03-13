/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;

/**
 *
 * @author ed1000
 */
public class ObtainGeneFromWebPanel extends ContentPanel implements Observer{
    
    private static volatile ObtainGeneFromWebPanel instance = null;
    private static JPanel content;
    
    /* Created when class is loaded */
    static {
        instance = new ObtainGeneFromWebPanel();
        instance.setLayout(new BoxLayout(instance, BoxLayout.Y_AXIS));
        content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        instance.add(content);

        JLabel label = new JLabel("<html><b>No genome opened</b></html>", JLabel.CENTER);

        content.add(Box.createHorizontalGlue());
        content.add(label);
        content.add(Box.createHorizontalGlue());
    }
    
    private ObtainGeneFromWebPanel() {
        super("Obtain Gene", false);
    }
    
    public static ObtainGeneFromWebPanel getInstance() {
        return instance;
    }
    
    @Override
    public void update(Observable obs, Object obj) {
        content.removeAll();
        
        content.add(Box.createHorizontalGlue());
        JLabel label = new JLabel("Insert Gene Locus (ex. NM_031418):");
        content.add(label);
        content.add(Box.createHorizontalGlue());
        
        JTextField text = new JTextField();
        content.add(text);

        JButton search = new JButton("Search");
        search.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String text = ((JTextField) content.getComponent(1)).getText();
                if(text.startsWith("NM_"))
                    searchGene(text);
                else
                    System.out.println("Error: " + text);
            }
        });
        content.add(search);
        content.add(Box.createHorizontalGlue());
    }
    
    private void searchGene(String text) {
        System.out.println(text);
    }
}
