/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ieeta.geneoptimizer.GUI;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import pt.ua.ieeta.geneoptimizer.ExternalTools.NCBIwebFetcher;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.FileHandling.SequenceValidator;
import pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI.GenePoolGUI;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author ed1000
 */
public class ObtainGeneFromWebPanel extends ContentPanel implements Observer{
    
    /* Singleton object */
    private static volatile ObtainGeneFromWebPanel instance = null;
    
    /* Singleton fields */
    private JPanel content;
    private JPanel subcontent;
    private JLabel searchLabel;
    private JTextField searchField;
    private JButton searchButton;
    private NCBIwebFetcher tempFetcher;
    
    /* Created when class is loaded */
    static {
        instance = new ObtainGeneFromWebPanel();
        instance.setLayout(new BoxLayout(instance, BoxLayout.Y_AXIS));
        instance.content = new JPanel();
        instance.content.setLayout(new BoxLayout(instance.content, BoxLayout.Y_AXIS));
        instance.add(instance.content);

        instance.searchLabel = new JLabel("<html><b>No genome opened</b></html>", JLabel.CENTER);

        instance.content.add(Box.createHorizontalGlue());
        instance.content.add(instance.searchLabel);
        instance.content.add(Box.createHorizontalGlue());
    }
    
    private ObtainGeneFromWebPanel() {
        super("Obtain Gene", false);
        tempFetcher = null;
    }
    
    public static ObtainGeneFromWebPanel getInstance() {
        return instance;
    }
    
    private void updateAspect() {
        System.out.println("Update aspect begin");
        content.removeAll();
        content.setLayout(new GridLayout(2, 1));
        searchLabel = new JLabel("Insert Gene Transcript:");
        content.add(searchLabel);

        subcontent = new JPanel();
        subcontent.setLayout(new FlowLayout());
        searchField = new JTextField("NM_");
        searchField.setPreferredSize(new Dimension(150, 23));
        subcontent.add(searchField);

        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                String text = searchField.getText();
                if (text.startsWith("NM_")) {
                    searchGene(text);
                } else {
                    System.out.println("Error: " + text);
                }
            }
        });
        subcontent.add(searchButton);
        content.add(subcontent);
        
        instance.updateUI();
    }
    
    private void fetchResult() {
        String geneName = tempFetcher.getFetchedName();
        String geneSequence = tempFetcher.getFetchedRNASequence();
        
        System.out.println("Gene Name: " + geneName + "\nGene Sequence: " + geneSequence);
        
        if (geneName.equals("NOT_FOUND") && geneSequence.equals("NOT_FOUND")) {
            JOptionPane.showMessageDialog(null, "Gene " + tempFetcher.getValue() + " not found", "Gene not found", JOptionPane.ERROR_MESSAGE);
            updateAspect();
        } else {
            List<Genome> genomes = GenePool.getInstance().getGenomes();
            List<Object> tempGenomeName = new LinkedList<>();
            Iterator it = genomes.listIterator();
            while (it.hasNext()) {
                tempGenomeName.add(((Genome) it.next()).getName());
            }
            
            Object tempGenomeChoosen = JOptionPane.showInputDialog(content, "Choose a genome", "Input", JOptionPane.INFORMATION_MESSAGE, null, tempGenomeName.toArray(), tempGenomeName.get(0));
            it = genomes.listIterator();
            while (it.hasNext()) {
                Genome tempGenome = (Genome) it.next();
                if (tempGenome.getName().equals(tempGenomeChoosen)) {
                    Gene newGene = new Gene(geneName, tempGenome);
                    newGene.createStructure(SequenceValidator.makeCorrectionsToGene(geneSequence), BioStructure.Type.mRNAPrimaryStructure);

                    tempGenome.addGeneManually(newGene);

                    System.out.println("Gene added");

                    GenePoolGUI.getInstance().updateGenome(tempGenome);
                    
                    instance.searchField.setText("NM_");
                    
                    return;
                }
            }
        }
        
        instance.searchField.setText("NM_");
        tempFetcher = null;
    }
    
    @Override
    public void update(Observable obs, Object obj) {
        System.out.println("Update begin");
        if(obs.getClass() == GenePool.class) {
            updateAspect();
        } else if(obs.getClass() == ResultKeeper.class) {
            System.out.println("Fetch result begin");
            fetchResult();
        }
    }
    
    private void searchGene(String text) {
        tempFetcher = new NCBIwebFetcher(new ResultKeeper(this), true);
        tempFetcher.setValue(text);
        new Thread(tempFetcher).start();
    }
    
    
}
