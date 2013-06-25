package pt.ua.ieeta.geneoptimizer.geneDB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import pt.ua.ieeta.geneoptimizer.GUI.GenePoolGUI.GenePoolGUI;

/**
 * Class responsible to manage available genomes in application
 *
 * @author Paulo Gaspar
 * @author Ricardo Gonzaga
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class GenePool extends Observable {
    /* List of genomes available on the gene pool. Genome pool itself. */

    private static List<Genome> genomes = null;

    /* Singleton instance of genome pool. */
    private static volatile GenePool instance = null;

    /* Variable to set ID of new genomes. */
    private static int lastGenomeID = 0;

    /* Create/return the only instance of this class. */
    public static GenePool getInstance() {
        if (instance == null) {
            synchronized (GenePool.class) {
                if (instance == null) {
                    instance = new GenePool();

                    /* Create genome list (the pool itself). */
                    genomes = Collections.synchronizedList(new ArrayList<Genome>());

                    /* Create instance of gene pool. */
                    GenePoolGUI.getInstance();

                    /* Add the GUI as observer of this class. */
                    registerObserver(GenePoolGUI.getInstance());
                }
            }
        }
        return instance;
    }

    public static void registerObserver(Observer observer) {
        getInstance().addObserver(observer);
    }

    /* Add a genome to the genome pool. */
    public synchronized void addGenomeToPool(Genome genome) {

        /* Add genome to pool. */
        genomes.add(genome);

        /* Notify GUI to update gene pool graphic interface. */
        setChanged();
        notifyObservers(genome);
    }

    /* Open gene pool window. */
    public void showGenePool() {
        GenePoolGUI.getInstance().setVisible(true);
        GenePoolGUI.getInstance().setLocationRelativeTo(null);
    }

    public synchronized Genome getLastOpenGenome() {
        assert genomes != null;
        assert !genomes.isEmpty();

        return genomes.get(genomes.size() - 1);
    }

    /* Return genome list. */
    public synchronized List<Genome> getGenomes() {
        return genomes;
    }

    public synchronized Genome getGenome(int genomeID) {
        assert genomes != null;

        for (Genome g : genomes) {
            if (g.getGenomeID() == genomeID) {
                return g;
            }
        }

        return null;
    }
}
