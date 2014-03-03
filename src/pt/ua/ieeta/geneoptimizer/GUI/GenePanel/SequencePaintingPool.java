package pt.ua.ieeta.geneoptimizer.GUI.GenePanel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure.Type;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class SequencePaintingPool {

    private static volatile SequencePaintingPool instance = null;

    public static SequencePaintingPool getInstance() {
        if (instance == null) {
            synchronized (SequencePaintingPool.class) {
                if (instance == null) {
                    instance = new SequencePaintingPool();
                }
            }
        }
        return instance;
    }

    private SequencePaintingPool() {
    }

    public List<Color> getAADifferences(String aaSequence1, String aaSequence2) {
        assert aaSequence1 != null;
        assert aaSequence2 != null;
        assert aaSequence1.length() == aaSequence2.length();

        List<Color> colourScheme = Collections.synchronizedList(new ArrayList<Color>());
        Color gray = new Color(212, 212, 212);
        Color dif = new Color(215, 125, 125);
        for (int i = 0; i < aaSequence1.length(); i++) {
            if (aaSequence1.charAt(i) != aaSequence2.charAt(i)) {
                colourScheme.add(dif);
            } else {
                colourScheme.add(gray);
            }
        }

        assert colourScheme.size() == aaSequence1.length();

        return colourScheme;
    }

    public List<List<Color>> getOrthologsColorSchemes(Study study) {
        assert study != null;
        assert study.getResultingGene().hasOrthologs();
        assert study.getResultingGene().hasAlignedStructure(Type.proteinPrimaryStructure);

        List<List<Color>> colorSchemes = new ArrayList<List<Color>>();

        List<Gene> orthologs = study.getResultingGene().getOrthologList().getGenes();
//        BioStructure originalGene = study.getResultingGene().getAlignedStructure(Type.proteinPrimaryStructure);

        for (int i = 0; i < orthologs.size(); i++) {
            colorSchemes.add(new ArrayList<Color>());
        }

        Color gray = new Color(240, 240, 240);
        HashMap<String, Integer> identityCounter = new HashMap<String, Integer>();
        for (int i = 0; i < orthologs.get(0).getAlignedStructure(Type.mRNAPrimaryStructure).getLength(); i++) {
            identityCounter.clear();
            for (Gene ortholog : orthologs) {
                if (ortholog.getAlignedCodonAt(i).equals("---")) {
                    continue;
                } else if (identityCounter.containsKey(ortholog.getAlignedCodonAt(i))) {
                    identityCounter.put(ortholog.getAlignedCodonAt(i), identityCounter.get(ortholog.getAlignedCodonAt(i)) + 1);
                } else {
                    identityCounter.put(ortholog.getAlignedCodonAt(i), 1);
                }
            }


            for (int j = 0; j < orthologs.size(); j++) {
                if (orthologs.get(j).getAlignedCodonAt(i).equals("---")) {
                    colorSchemes.get(j).add(gray);
                    continue;
                }

                //System.out.println("Codon: " + orthologs.get(j).getAlignedCodonAt(i) + "   Count: "+ identityCounter.get(orthologs.get(j).getAlignedCodonAt(i)));

                /* zero score means 100% identity. 253 means 0% identity. */
                int score = 253 - identityCounter.get(orthologs.get(j).getAlignedCodonAt(i)) * 253 / orthologs.size();

                colorSchemes.get(j).add(new Color(0, score, 255));
            }
        }

        /* All the orthologs, after aligned, must have the same size. */
        int alignedSize = orthologs.get(0).getAlignedStructure(Type.mRNAPrimaryStructure).getLength(); //study.getResultingGene().getAlignedStructure(Type.proteinPrimaryStructure).getLength();
        for (List<Color> vector : colorSchemes) {
            assert vector.size() == alignedSize;
        }

        return colorSchemes;
    }
}
