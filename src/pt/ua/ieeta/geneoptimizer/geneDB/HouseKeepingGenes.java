package pt.ua.ieeta.geneoptimizer.geneDB;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class HouseKeepingGenes extends Thread
{
    private String eugeneDir = null;
    private String geneCodesFolder = null;
    private String geneCodesFileName = null;
    private List<String> geneCodesEukaryots, geneCodesProkaryots;
    
    private static volatile HouseKeepingGenes instance = null;
    
    private boolean finished = false;
    
    /* Get singleton instance. */
    public static HouseKeepingGenes getInstance()
    {
        if (instance == null)
            synchronized(HouseKeepingGenes.class){
                if (instance == null){
                 instance = new HouseKeepingGenes();   
                }
            }                    
        return instance;
    }
    
    /* Constructor. Private to avoid outside access. */
    private HouseKeepingGenes()
    {
        eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        geneCodesFolder = (String) ApplicationSettings.getInstance().getProperty("highlyExpressedGenesFolder", String.class);
        geneCodesFileName = (String) ApplicationSettings.getProperty("highlyExpressedGenesFileName", String.class);
        geneCodesEukaryots = new ArrayList<String>(76);
        geneCodesProkaryots = new ArrayList<String>(64);
    }
    
    public boolean isFinished()
    {
        return finished;
    }
    
    @Override
    public void run()
    {
        /* If already read, doesn't need to read again. */
        if (!geneCodesEukaryots.isEmpty()) 
        {
            finish();
            return;
        }
        
        if (geneCodesFileName == null)
                geneCodesFileName = createDefaultHighlyExpressed();
            else if (!new File(eugeneDir + File.pathSeparator + geneCodesFolder, geneCodesFileName).exists() || geneCodesFileName.isEmpty())
                geneCodesFileName = createDefaultHighlyExpressed();
        
        try(FileInputStream fstream = new FileInputStream(eugeneDir +  geneCodesFolder + File.separator + geneCodesFileName);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));){
            
            String strLine;
            int i=0;
            while ((strLine = br.readLine()) != null)
            {
                if (strLine.length() != 0)
                    if (i < 76)
                        geneCodesEukaryots.add("ko:"+strLine.trim());
                    else
                        geneCodesProkaryots.add("ko:"+strLine.trim());
                
                i++;
            }
        }
        catch (Exception ex)
        {
            //TODO: exceptions
            Logger.getLogger(HouseKeepingGenes.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        finish();
    }

    private String createDefaultHighlyExpressed() {

        String eugeneDir = (String) ApplicationSettings.getInstance().getProperty("eugene_dir", String.class);
        String folder = (String) ApplicationSettings.getInstance().getProperty("highlyExpressedGenesFolder", String.class);
        String filename = (String) ApplicationSettings.getProperty("highlyExpressedGenesFileName", String.class);

        if (filename == null) {
            filename = "highly_expressed";
        } else if (filename.isEmpty()) {
            filename = "highly_expressed";
        }

        File heFile = new File(eugeneDir + folder + File.separator + filename);

        System.out.println("Creating default highly expressed codes file.");

        try {
            if(!heFile.createNewFile()) {
                System.out.println("Error creating highly expressed codes file");
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(heFile));
            bw.write(default_highly_expressed);
            bw.flush();
            bw.close();

        } catch (IOException ex) {
            Logger.getLogger(HouseKeepingGenes.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Highly expressed codes file successfully created.");
        
        return filename;
    }
    
    private String default_highly_expressed = "K03231\n"
            + "K02865\n"
            + "K02938\n"
            + "K02925\n"
            + "K02930\n"
            + "K02932\n"
            + "K02934\n"
            + "K02937\n"
            + "K02936\n"
            + "K02940\n"
            + "K13953\n"
            + "K14753\n"
            + "K00873\n"
            + "K01689\n"
            + "K01624\n"
            + "K01834\n"
            + "K00053\n"
            + "K00927\n"
            + "K00134\n"
            + "K01803\n"
            + "K02866\n"
            + "K02868\n"
            + "K02870\n"
            + "K02873\n"
            + "K02875\n"
            + "K02877\n"
            + "K02872\n"
            + "K02880\n"
            + "K02883\n"
            + "K02885\n"
            + "K02882\n"
            + "K02889\n"
            + "K02891\n"
            + "K02894\n"
            + "K02896\n"
            + "K02893\n"
            + "K02901\n"
            + "K02900\n"
            + "K02905\n"
            + "K02908\n"
            + "K02910\n"
            + "K02912\n"
            + "K02917\n"
            + "K02915\n"
            + "K02918\n"
            + "K02920\n"
            + "K02924\n"
            + "K02929\n"
            + "K02921\n"
            + "K02941\n"
            + "K02942\n"
            + "K02943\n"
            + "K02998\n"
            + "K02984\n"
            + "K02981\n"
            + "K02985\n"
            + "K02987\n"
            + "K02989\n"
            + "K02993\n"
            + "K02995\n"
            + "K02997\n"
            + "K02947\n"
            + "K02949\n"
            + "K02951\n"
            + "K02953\n"
            + "K02955\n"
            + "K02958\n"
            + "K02960\n"
            + "K02962\n"
            + "K02964\n"
            + "K02966\n"
            + "K02969\n"
            + "K02957\n"
            + "K02973\n"
            + "K02975\n"
            + "K02977\n"
            + "K02863\n"
            + "K02886\n"
            + "K02906\n"
            + "K02926\n"
            + "K02931\n"
            + "K02933\n"
            + "K02939\n"
            + "K02864\n"
            + "K02867\n"
            + "K02935\n"
            + "K06078\n"
            + "K03286\n"
            + "K09475\n"
            + "K09476\n"
            + "K03553\n"
            + "K04043\n"
            + "K02358\n"
            + "K02358\n"
            + "K02357\n"
            + "K02355\n"
            + "K02871\n"
            + "K02874\n"
            + "K02876\n"
            + "K02878\n"
            + "K02879\n"
            + "K02881\n"
            + "K02884\n"
            + "K02887\n"
            + "K02888\n"
            + "K02890\n"
            + "K02892\n"
            + "K02895\n"
            + "K02897\n"
            + "K02899\n"
            + "K02902\n"
            + "K02904\n"
            + "K02907\n"
            + "K02909\n"
            + "K02911\n"
            + "K02913\n"
            + "K02914\n"
            + "K02916\n"
            + "K02919\n"
            + "K02945\n"
            + "K02967\n"
            + "K02982\n"
            + "K02986\n"
            + "K02988\n"
            + "K02990\n"
            + "K02992\n"
            + "K02994\n"
            + "K02996\n"
            + "K02946\n"
            + "K02948\n"
            + "K02950\n"
            + "K02952\n"
            + "K02954\n"
            + "K02956\n"
            + "K02959\n"
            + "K02961\n"
            + "K02963\n"
            + "K02965\n"
            + "K02968\n"
            + "K02970";
    
    private void waitUntilFinished()
    {
        while (!finished)
        {
            synchronized (this)
            {
                try { wait(); } 
                catch (InterruptedException ex) 
                { Logger.getLogger(HouseKeepingGenes.class.getName()).log(Level.SEVERE, null, ex); }
            }
        }
    }
    
    private void finish()
    {
        /* Notify waiting threads. */
        finished = true;
        synchronized(this)
        { notifyAll(); }
    }
    
    public String getEukaryotKeggCode(int index)
    {
        waitUntilFinished();
        
        assert geneCodesEukaryots != null;
        assert !geneCodesEukaryots.isEmpty();
        
        return geneCodesEukaryots.get(index);
    }
    
    public String getProkaryotKeggCode(int index)
    {
        waitUntilFinished();
        
        assert geneCodesProkaryots != null;
        assert !geneCodesProkaryots.isEmpty();
        
        return geneCodesProkaryots.get(index);
    }
    
    public int getEukaryotNumGenes()
    {
        waitUntilFinished();
        
        assert geneCodesEukaryots != null;
        assert !geneCodesEukaryots.isEmpty();
        
        return geneCodesEukaryots.size();
    }
    
     public int getProkaryotNumGenes()
    {
        waitUntilFinished();
        
        assert geneCodesProkaryots != null;
        assert !geneCodesProkaryots.isEmpty();
        
        return geneCodesProkaryots.size();
    }
    
    /* TESTE */
    public static void main(String [] Args)
    {
        HouseKeepingGenes h = new HouseKeepingGenes();
        h.start();
    }
   
    
}
