package pt.ua.ieeta.geneoptimizer.WebServices;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Semaphore;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;

/**
 *
 * @author Paulo Gaspar
 */
public class PDBBlastWS extends Thread
{
    private String seq;
    private double eCut;
    private ResultKeeper resultKeeper;
    private int sequenceLen;
    private static Semaphore fluxControl;

    public PDBBlastWS(String sequence, double eCutOff, ResultKeeper resultKeeper)
    {
        this.resultKeeper = resultKeeper;
        this.sequenceLen = sequence.length();
        this.seq = sequence;
        this.eCut = eCutOff;
        
        if (fluxControl == null)
            fluxControl = new Semaphore((Integer) ApplicationSettings.getInstance().getProperty("maxNumberOfPDBBlastSimultaneousCalls", Integer.class), true);
    }
    
    @Override
    public void run()
    {
        System.out.println("PDB WebService started.");
        String result = null;
        ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Fetching from PDB");
        processPanel.setIndeterminated();
        
        try
        {
            fluxControl.acquire();
            processPanel.setStatus("Connecting...");
            result = (String) call();
            fluxControl.release();

            // System.out.println(result);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(result));
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            NodeList nodeLst = doc.getElementsByTagName("Hit");

            //Map<String,String> blastResults = new HashMap<String, String>();
            String chosenPDBId = null;
            int maxScore = 0;

            processPanel.setStatus("Parsing results...");
            for (int i = 0; i < nodeLst.getLength(); i++)
            {
                Element element = (Element) nodeLst.item(i);

                String score = element.getElementsByTagName("Hsp_score").item(0).getTextContent();
                String pdbID = element.getElementsByTagName("Hit_def").item(0).getTextContent().trim();
                String sequence = element.getElementsByTagName("Hsp_hseq").item(0).getTextContent().replaceAll("-", "");
                String positives = element.getElementsByTagName("Hsp_positive").item(0).getTextContent();

                int PDBMatchThreshold = (Integer) ApplicationSettings.getProperty("PDBMatchThreshold", Integer.class);
                if ((maxScore < Integer.parseInt(score)) && (((Double.parseDouble(positives)/sequenceLen)*100) > PDBMatchThreshold))
                {
                    System.out.println("Found a PDB match!");
                    maxScore = Integer.parseInt(score);
                    chosenPDBId = pdbID;
                }
                //blastResults.put(pdbID, sequence);
            }

            if (chosenPDBId == null)
            {
                processPanel.setStatus("No result.");
                processPanel.setFailed();
                resultKeeper.setFailed();
                System.out.println("No match for PDB blast! " + nodeLst.getLength());
            }
            else
            {
                resultKeeper.setResult(chosenPDBId.substring(0, chosenPDBId.indexOf(':')));
                processPanel.setStatus("Done.");
                processPanel.setComplete();
            }
        }
        catch (Exception ex) { //TODO: excepçoes..
            System.out.println(ex.getMessage() + " : " + ex.getLocalizedMessage());
            processPanel.setStatus("Error.");
            processPanel.setFailed();
            resultKeeper.setFailed();
            fluxControl.release();
        }
        System.out.println("PDB WebService started.");
    }
    
    private String call() throws Exception{
        StringBuilder sb = new StringBuilder();

        URL url = new URL("http://www.rcsb.org/pdb/rest/getBlastPDB1?sequence=" + this.seq + "&eCutOff=" + this.eCut + "&matrix=BLOSUM62&outputFormat=XML");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/xml");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        
        br.close();
        conn.disconnect();
        
        return sb.toString();
    }
    
    public static void main(String [] Args) throws Exception
    {
        ResultKeeper k = new ResultKeeper();
        PDBBlastWS ra = new PDBBlastWS("MLILISPAKTLDYQSPLTTTRYTLPELLDNSQQLIHEARKLTPPQISTLMRISDKLAGINAARFHDWQPDFTPANARQAILAFKGDVYTGLQAETFSEDDFDFAQQHLRMLSGLYGVLRPLDLMQPYRLEMGIRLENARGKDLYQFWGDIITNKLNEALAAQGDNVVINLASDEYFKSVKPKKLNAEIIKPVFLDEKNGKFKIISFYAKKARGLMSRFIIENRLTKPEQLTGFNSEGYFFDEDSSSNGELVFKRYEQR", 1E-30, k);
        ra.start();

        String result = (String) k.getResult();
        System.out.println("Result: " + result);
    }
}
