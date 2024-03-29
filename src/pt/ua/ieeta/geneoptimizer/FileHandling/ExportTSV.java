/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pt.ua.ieeta.geneoptimizer.FileHandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import pt.ua.ieeta.geneoptimizer.geneDB.UsageAndContextTables;

/**
 *
 * @author ed1000
 */
public class ExportTSV implements Runnable{
    @Override
    public void run() { 
        this.createUI();
    }
    
    public ExportTSV() {}
    
    public void createUI() {
        JFileChooser saveFile = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("TSV file", "tsv");
        saveFile.setFileFilter(filter);
        int returnVal = saveFile.showSaveDialog(MainWindow.getInstance());
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            exportToTSV(saveFile.getSelectedFile(), ProjectManager.getInstance().getSelectedProject().getSelectedStudy().getResultingGene());
        } else {
            return;
        }
    }
    
    public void exportToTSV (File tsvFile, Gene printingGene) {
        if(!tsvFile.getAbsolutePath().endsWith(".tsv")) {
            tsvFile = new File(tsvFile.getAbsolutePath()+".tsv");
        }
        
        try(PrintWriter tsvOut = new PrintWriter(tsvFile);){
            Genome printingGenome = printingGene.getGenome();
            UsageAndContextTables printingTables = printingGenome.getUsageAndContextTables();
            boolean availableCAI = printingGene.hasCAI();
            int length = printingGene.getSequenceLength();
            
            tsvOut.println("Genome name: " + "\t" + printingGenome.getName());
            tsvOut.println("Gene name: " + "\t" + printingGene.getName());
            tsvOut.println();
            tsvOut.println("Number of Codons: " + "\t" + printingGene.getSequenceLength());
            tsvOut.println("GC content: " + "\t" + new DecimalFormat("##.##").format(printingGene.getGCContent()*100));
            tsvOut.println("Average RSCU: " + "\t" + new DecimalFormat("##.###").format(printingGene.getAverageRSCU()));
            tsvOut.println("Codon Pair Bias: " + "\t" + new DecimalFormat("##.###").format(printingGene.getCPB()));
            tsvOut.println("Effective Number of Codons: " + "\t" + new DecimalFormat("##.###").format(printingGene.getEffectiveNumberOfCodons()));
            if(availableCAI) {
                tsvOut.println("CAI: " + "\t" + new DecimalFormat("##.###").format(printingGene.getCAI()));
            }
            tsvOut.println();
            
            while(printingGene.getStructure(BioStructure.Type.proteinSecondaryStructure) == null) {}
            
            String secondStruct = printingGene.getStructure(BioStructure.Type.proteinSecondaryStructure).getSequence();
            
            tsvOut.println("Index" + "\t" + "Codon" + "\t" + "Amino Acid" + "\t" + "Secondary Structure"  + "\t" + "Codon Pair Score"  + "\t" + "Codon Usage (RSCU)" + "\t" + "GC content" + (availableCAI ? "\t" + "Codon Usage (CAI)" : ""));
            int idx = 1;
            String actualCodon, actualAminoAcid, nextCodon;
            char struct;
            for(int i = 0; i < length; i++) {
                actualCodon = printingGene.getCodonAt(i);
                actualAminoAcid = printingGenome.getAminoAcidFromCodon(actualCodon);
                struct = secondStruct.charAt(i);
                if(i != length-1) {
                    nextCodon = printingGene.getCodonAt(i+1);
                    tsvOut.println(idx++ + "\t" + actualCodon + "\t" + actualAminoAcid  + "\t" + struct + "\t" + printingTables.getCodonPairScore(actualCodon, nextCodon) + "\t" + printingTables.getCodonUsageRSCU(actualCodon) + "\t" + GCcontent(actualCodon) + (availableCAI ? "\t" + printingGenome.getHouseKeepingGenes().getUsageAndContextTables().getCodonUsageRSCU(actualCodon) : ""));
                } else {
                    tsvOut.println(idx + "\t" + actualCodon + "\t" + "*" + "\t" + struct +  "\t" + "\t" + printingTables.getCodonUsageRSCU(actualCodon) + "\t" + GCcontent(actualCodon) + (availableCAI ? "\t" + printingGenome.getHouseKeepingGenes().getUsageAndContextTables().getCodonUsageRSCU(actualCodon) : ""));
                }
            }
        }catch(FileNotFoundException ex) {
            
        }
    }
    
    /* Counts the GC content in the passed codon */
    private static int GCcontent(String codon) {
        int count = 0;
        
        if(codon.charAt(0) == 'G' || codon.charAt(0) == 'C') count++;
        if(codon.charAt(1) == 'G' || codon.charAt(1) == 'C') count++;
        if(codon.charAt(2) == 'G' || codon.charAt(2) == 'C') count++;
        
        return count;
    }
}
