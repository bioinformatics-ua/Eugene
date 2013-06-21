package pt.ua.ieeta.geneoptimizer.WebServices;

import java.io.StringReader;
import java.util.ArrayList;
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
    private WebService ws1;
    private ResultKeeper resultKeeper;
    private int sequenceLen;
    private static Semaphore fluxControl;

    public PDBBlastWS(String sequence, double eCutOff, ResultKeeper resultKeeper)
    {
        this.resultKeeper = resultKeeper;
        this.sequenceLen = sequence.length();

        ws1 = new WebService();
        ws1.setAddress("http://www.rcsb.org/pdb/services/pdbws?wsdl");
        ws1.setProtocol("SOAP");

        ArrayList<WebServiceParameter> params = new ArrayList<WebServiceParameter>();
        params.add(new WebServiceParameter("method", "blastQueryXml"));
        params.add(new WebServiceParameter("sequence", sequence));
        params.add(new WebServiceParameter("eCutOff", Double.toString(eCutOff)));
        ws1.setParams(params);

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
                result = (String) ws1.call();
            fluxControl.release();

            //System.out.println(result);

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

    public static void main(String [] Args) throws Exception
    {
        ResultKeeper k = new ResultKeeper();
        PDBBlastWS ra = new PDBBlastWS("MLILISPAKTLDYQSPLTTTRYTLPELLDNSQQLIHEARKLTPPQISTLMRISDKLAGINAARFHDWQPDFTPANARQAILAFKGDVYTGLQAETFSEDDFDFAQQHLRMLSGLYGVLRPLDLMQPYRLEMGIRLENARGKDLYQFWGDIITNKLNEALAAQGDNVVINLASDEYFKSVKPKKLNAEIIKPVFLDEKNGKFKIISFYAKKARGLMSRFIIENRLTKPEQLTGFNSEGYFFDEDSSSNGELVFKRYEQR", 1E-30, k);
        ra.start();

        String result = (String) k.getResult();
        System.out.println("Result: " + result);
    }
}
