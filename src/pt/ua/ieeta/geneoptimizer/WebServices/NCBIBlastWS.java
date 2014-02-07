package pt.ua.ieeta.geneoptimizer.WebServices;

import java.io.StringReader;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pt.ua.ieeta.geneoptimizer.ExternalTools.NCBIBlastClient;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.FileHandling.SequenceValidator;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel;
import pt.ua.ieeta.geneoptimizer.GUI.ProgressPanel.ProcessPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure.Type;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import uk.ac.ebi.webservices.jaxws.stubs.ncbiblast.InputParameters;
import uk.ac.ebi.webservices.jaxws.stubs.ncbiblast.ObjectFactory;

/**
 *
 * @author Paulo Gaspar
 */
public class NCBIBlastWS extends Thread
{
    private CommandLine cli;
    private NCBIBlastClient blast;
    private InputParameters params;
    private ResultKeeper resultKeeper;
    
    private final Gene gene;

    private static Semaphore fluxControl = null;

    public NCBIBlastWS(Gene gene, double eCutOff, int numbeOfResults, ResultKeeper resultKeeper)
    {
        this.gene = gene;
        String sequence = gene.getCodonSequence();

        this.resultKeeper = resultKeeper;

        /* Build arguments for blast. */
        String userEmail = (String) ApplicationSettings.getProperty("userEmail", String.class);
        String [] args = new String[]{"-email", userEmail, "-p", "blastn", "-D", "emblcds", "-n", Integer.toString(numbeOfResults), sequence};
        CommandLineParser cliParser = new GnuParser();
        Options options = new Options();
        
        options.addOption("p", "program", true, "Program to use");
	options.addOption("D", "database", true, "Database to search");
        options.addOption("e", "exp", true, "Expectation value threshold");
        options.addOption("n", "alignments", true, "Maximum number of alignments to display");
        options.addOption("s", "scores", true, "Maximum number of scores to display");
        options.addOption("email", "email", true, "E-Mail");
        options.addOption("sequence", "sequence", true, "Query sequence");

        /* Create web service client. */
        blast = new NCBIBlastClient();

        try
        {
            cli = cliParser.parse(options, args);
            params = blast.loadParams(cli);
            ObjectFactory objFactory = new ObjectFactory();
            params.setSequence(objFactory.createInputParametersSequence(sequence));
            params.setStype("dna");
        }
        catch (Exception ex) {
            Logger.getLogger(NCBIBlastWS.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (fluxControl == null)
            fluxControl = new Semaphore((Integer) ApplicationSettings.getProperty("maxNumberOfNCBISimultaneousCalls", Integer.class), true);
    }

    @Override
    public void run()
    {
        String result = null;
        Genome orthologList = null;
        ProcessPanel processPanel = ProgressPanel.getInstance().newProgressProcess("Fetching from EBI");
        processPanel.setIndeterminated();

        try
        {
            String jobId = null;
            fluxControl.acquire();
                processPanel.setStatus("Connecting...");
                /* Run the web service. */
                blast.srvProxyConnect(); // Ensure the service proxy exists
                jobId = blast.srvProxy.run(cli.getOptionValue("email"), "", params);

                /* Wait for result. */
                processPanel.setStatus("Performing BLAST");
                blast.srvProxyConnect(); // Ensure the service proxy exists
                blast.clientPoll(jobId);
            fluxControl.release();

            /* Get results. */
            byte[] resultbytes = blast.srvProxy.getResult(jobId, "xml", null);
            if(resultbytes == null)
            {
                processPanel.setStatus("No result!");
                processPanel.setFailed();
                System.err.println("Null result for xml!"); //TODO: ERRO ao obter blast
                return;
            }
            else
                result = new String(resultbytes);
        } 
        catch (Exception ex) 
        {
            processPanel.setStatus("Error connecting!");
            processPanel.setFailed();
            System.err.println("Error connecting to NCBI blast client (or retrieving data).");
            return;
        }

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();

            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(result));
            Document doc = db.parse(is);
            doc.getDocumentElement().normalize();

            /* Get list of possible orthologs. */
            NodeList nodeLst = doc.getElementsByTagName("hit");

            orthologList = new Genome("ortholog List", gene.getGenome().getGeneticCodeTable());

            //System.out.println("\n\n\n\n\n\n\n"+result+"\n\n\n\n\n\n\n\n\n");

            processPanel.setStatus("Parsing results.");
            for (int i = 0; i < nodeLst.getLength(); i++)
            {
                /* Get a single hit (ortholog). */
                Element element = (Element) nodeLst.item(i);

                /* Before anything else, get the sequence and check if it is length-valid (multiple of 3). */
                String sequence = element.getElementsByTagName("matchSeq").item(0).getTextContent().replaceAll("-", "");
                if ((sequence.length() % 3) != 0) continue;
                
                /* Parse ortholog information. */
                String database = element.getAttribute("database");
                String description = element.getAttribute("description");
                if (description.contains("#"))
                {
                    /* Run through all possible descriptions, and choose a non hypothetical one. (if it exists) */
                    String [] descriptionsList = description.split("#");
                    for (String desc : descriptionsList)
                        if (!desc.contains("hypothetical protein"))
                            description = desc.trim();

                    /* If only hypothetical proteins found, choose the first one. */
                    if (description.contains("#"))
                        description = description.substring(0, description.indexOf('#')).trim();
                }
                description = description.substring(description.indexOf('.')+2); /* Remove initial codes from description. */
                int separationIndex = description.indexOf(' ', description.indexOf(' ', 1)+1); /* Find separation between genome name and protein name. */
                String genomeName = description.substring(0, separationIndex);
                String geneName = description.substring(separationIndex);
                String id = element.getAttribute("id");
                String score = element.getElementsByTagName("score").item(0).getTextContent();
                String identity = element.getElementsByTagName("identity").item(0).getTextContent().trim();
                String length = element.getAttribute("length");

                /* Correct sequences, and remove any inner stop codons. */
                sequence = SequenceValidator.makeCorrectionsToGene(sequence);
                sequence = SequenceValidator.removeInnerStopCodons(sequence, orthologList);
                
                //System.out.println("Length: " + length + "  Score: "+score+"    ID: "+ id + "   Identity: "+identity+"   Description: "+description+"  Sequence: " + sequence);
                Gene newGene = new Gene(geneName, orthologList);
                newGene.setOrthologInfo(Integer.parseInt(score), Double.parseDouble(identity), id, genomeName);
                newGene.createStructure(sequence, Type.mRNAPrimaryStructure);
                newGene.calculateAllStructures();

                orthologList.addGene(newGene);
            }
        }
        catch (Exception ex)
        { //TODO: excepçoes.
            System.out.println(ex.getMessage());
            processPanel.setStatus("No result.");
            processPanel.setFailed();
        }
        
        if ((orthologList == null) || (orthologList.getGenes().isEmpty()))
            resultKeeper.setFailed();
        else
        {
            resultKeeper.setResult(orthologList);
            processPanel.setStatus("Done.");
            processPanel.setComplete();
        }
    }
}
