package pt.ua.ieeta.geneoptimizer.geneDB;

/**
 *
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class GenomeFilters {
    
    private boolean noStopCodon;
    private boolean noStartCodon;
    private boolean noMiddleStopCodon;
    private boolean multipleOfThree;    
    
    public GenomeFilters(boolean noStopCodon, boolean noStartCodon, boolean noMiddleStopCodon, boolean multipleOfThree){
        this.noStopCodon = noStopCodon;
        this.noStartCodon = noStartCodon;
        this.noMiddleStopCodon = noMiddleStopCodon;
        this.multipleOfThree = multipleOfThree;        
    }

    public boolean isNoStopCodon() {
        return noStopCodon;
    }

    public boolean isNoStartCodon() {
        return noStartCodon;
    }

    public boolean isNoMiddleStopCodon() {
        return noMiddleStopCodon;
    }

    public boolean isMultipleOfThree() {
        return multipleOfThree;
    }

    public void setNoStopCodon(boolean noStopCodon) {
        this.noStopCodon = noStopCodon;
    }

    public void setNoStartCodon(boolean noStartCodon) {
        this.noStartCodon = noStartCodon;
    }

    public void setNoMiddleStopCodon(boolean noMiddleStopCodon) {
        this.noMiddleStopCodon = noMiddleStopCodon;
    }

    public void setMultipleOfThree(boolean multipleOfThree) {
        this.multipleOfThree = multipleOfThree;
    }        
}
