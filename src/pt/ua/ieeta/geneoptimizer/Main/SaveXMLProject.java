package pt.ua.ieeta.geneoptimizer.Main;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport.Optimization;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class SaveXMLProject {

    /* Sigleton instance for saving projects - Thread safe */
    private static volatile SaveXMLProject instance = null;
    /* XML writer */
    private XMLStreamWriter outWriter;
    /* Project to be saved */
    private Project project;
    /* Project version */
    private final static String PROJECT_VERSION = "1.0";

    /* Double-Check locking with volatile instance grant the thread safe instantiation */
    public static SaveXMLProject getInstance() {
        if (instance == null) {
            synchronized (SaveXMLProject.class) {
                if (instance == null) {
                    instance = new SaveXMLProject();
                }
            }
        }
        return instance;
    }

    /**
     * Save the current project in XML format
     *
     * @param project current project state to be save
     * @param file output file where the project will be saved
     * @return <li>true if file was correctly saved <li>false if error occur
     * while saving project
     */
    public boolean saveFile(Project project, File file) {
        if (project == null || file == null) {
            throw new IllegalArgumentException("Project to save or file is null ");
        }
        this.project = project;

        /* Check if the provided file has the .euj extension - if not add it */
        if (!file.getName().toLowerCase().endsWith(".euj")) {
            file = new File(file.getParent(), (file.getName() + ".euj"));
        }

        try {
            /* initialization of the stream writer */
            outWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileWriter(file));

            /* write headers */
            outWriter.writeStartDocument();
            outWriter.writeStartElement("eugeneproject");

            /* create info element */
            if (!writeInfoElement()) {
                outWriter.close();
                return false;
            }

            /* create genepool element */
            if (!writeGenePoolElement()) {
                outWriter.close();
                return false;
            }

            /* create studies element */
            if (!writeStudies()) {
                outWriter.close();
                return false;
            }

            /* close xml document */
            outWriter.writeEndElement();
            outWriter.writeEndDocument();

            /* write data and free associated resources */
            outWriter.flush();
            outWriter.close();


        } catch (IOException ex) {
            System.out.println("Error saving file: " + ex.getMessage());
            return false;
        } catch (XMLStreamException ex) {
            System.out.println("Error saving file: " + ex.getMessage());
            return false;
        }

        try {
            byte[] fileBytes = new byte[(int) file.length()];
            FileInputStream inputStream = new FileInputStream(file);
            inputStream.read(fileBytes);

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", Integer.valueOf(2));

            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            t.transform(new StreamSource(new ByteArrayInputStream(fileBytes)), new StreamResult(file));

            inputStream.close();

        } catch (IOException ex) {
            System.out.println("File '" + file.getName() + "' may not exist! : " + ex.getMessage());
            System.out.println("XML not indented!!!");
        } catch (TransformerException ex) {
            System.out.println("Error indenting euj file: " + ex.getMessage());
        }

        return true;
    }

    /**
     * Creates info element and all inner elements
     *
     * @return true if successful created <li>false if error occur
     */
    private boolean writeInfoElement() {
        try {
            /* create start info element */
            outWriter.writeStartElement("info");

            outWriter.writeEmptyElement("eugene_version");
            outWriter.writeAttribute("value", Main.getVersion());

            outWriter.writeEmptyElement("project_version");
            outWriter.writeAttribute("value", PROJECT_VERSION);

            /* close info element */
            outWriter.writeEndElement();
        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'info' element: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Creates genepool element and all inner elements
     *
     * @return <li>true if successful created <li>false if error occur
     */
    private boolean writeGenePoolElement() {
        try {
            /* create start genepool element */
            outWriter.writeStartElement("genepool");

            for (Genome genome : GenePool.getInstance().getGenomes()) {
                /*create genome element */
                outWriter.writeStartElement("genome");

                String source = new String();
                /* save hash from sourcefiles */
                if (genome.getGenesFiles().length > 0) {
                    for (String file : genome.getGenesFiles()) {
                        source += file;
                    }
                } else {
                    return false;
                }
                outWriter.writeAttribute("id", Integer.toString(source.hashCode()));

                /* create genome name element */
                outWriter.writeEmptyElement("name");
                outWriter.writeAttribute("value", genome.getName());

                /* create genome genetic code table element */
                outWriter.writeEmptyElement("geneticcodetable");
                outWriter.writeAttribute("id", String.valueOf(genome.getGeneticCodeTable().getId()));

                /* write start filter element */
                outWriter.writeStartElement("filters");

                outWriter.writeEmptyElement("noStopCodon");
                outWriter.writeAttribute("value", String.valueOf(genome.getFilters().isNoStopCodon()));

                outWriter.writeEmptyElement("noStartCodon");
                outWriter.writeAttribute("value", String.valueOf(genome.getFilters().isNoStartCodon()));

                outWriter.writeEmptyElement("noMiddleStopCodon");
                outWriter.writeAttribute("value", String.valueOf(genome.getFilters().isNoMiddleStopCodon()));

                outWriter.writeEmptyElement("multipleOfThree");
                outWriter.writeAttribute("value", String.valueOf(genome.getFilters().isMultipleOfThree()));

                /*close filter element */
                outWriter.writeEndElement();

                /* create sourcefile element */
                if (!writeSourceFiles(genome)) {
                    /* close genome element */
                    outWriter.writeEndElement();
                    /* close genepool element */
                    outWriter.writeEndElement();
                    return false;
                }

                if (!writeAdditions(genome)) {
                    /* close genome element */
                    outWriter.writeEndElement();
                    /* close genepool element */
                    outWriter.writeEndElement();
                    return false;
                }

                /* close genome element */
                outWriter.writeEndElement();
            }
            /* close genepool element */
            outWriter.writeEndElement();
        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'genepool' element: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Creates sourcefiles elements for the given genome
     *
     * @param genome genome info
     * @return <li>true if successful created<li>false if error occur
     */
    private boolean writeSourceFiles(Genome genome) {
        assert genome != null;
        try {
            if (genome.getGenesFiles().length > 0) {
                for (String file : genome.getGenesFiles()) {
                    outWriter.writeEmptyElement("sourcefile");
                    outWriter.writeAttribute("value", file);
                }
            }
        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'sourcefile' element: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Create additions element for all genes provided in the given genome
     *
     * @param genome gene with all genes
     * @return <li>true if successful created<li>false if error occur
     */
    private boolean writeAdditions(Genome genome) {
        assert genome != null;
        try {

            outWriter.writeStartElement("additions");
            if (genome.getGenesAdded() != null) {
                for (Gene gene : genome.getGenesAdded()) {
                    outWriter.writeStartElement("gene");
                    outWriter.writeAttribute("name", gene.getName());
                    outWriter.writeCharacters(gene.getCodonSequence());
                    outWriter.writeEndElement();
                }
            }
            outWriter.writeEndElement();


        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'additions' element: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Create studies element and fill it it all studies existing the the
     * current project
     *
     * @return <li>true if successful created<li>false if error occur
     */
    private boolean writeStudies() {
        try {
            /* Create studies element */
            outWriter.writeStartElement("studies");

            /* Save information from all studies presented in the current project */
            for (Study study : project.getStudiesList()) {
                if (!writeSingleStudy(study)) {
                    return false;
                }
            }
            /* close studies element */
            outWriter.writeEndElement();
        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'studies' element: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Create single study element for the given study. This element got the
     * study title, original and resulting gene as well as optimizations reports
     * and orthologs
     *
     * @param study study
     * @return <li>true if successful created<li>false if error occur
     */
    private boolean writeSingleStudy(Study study) {
        assert study != null;

        try {
            /* Create study element */
            outWriter.writeStartElement("study");

            /* Create title element of the study */
            outWriter.writeEmptyElement("title");
            outWriter.writeAttribute("value", study.getName());

            /* create original gene element with name, genome id and sequence */
            outWriter.writeStartElement("originalgene");
            outWriter.writeAttribute("name", study.getOriginalGene().getName());

            //writer original genomeid hash
            StringBuilder genomeID = new StringBuilder();
            /* save hash from sourcefiles */
            if (study.getOriginalGene().getGenome().getGenesFiles().length > 0) {
                for (String file : study.getOriginalGene().getGenome().getGenesFiles()) {
                    genomeID.append(file);
                }
            } else {
                return false;
            }
            outWriter.writeAttribute("genomeid", Integer.toString(genomeID.hashCode()));
            outWriter.writeCharacters(study.getOriginalGene().getCodonSequence());
            outWriter.writeEndElement();
            /* create resulting genes element with name, genome id and sequence */
            outWriter.writeStartElement("resultinggene");
            outWriter.writeAttribute("name", study.getResultingGene().getName());

            //writer resulting genomeid hash
            genomeID = new StringBuilder();
            if (study.getResultingGene().getGenome().getGenesFiles().length > 0) {
                for (String file : study.getResultingGene().getGenome().getGenesFiles()) {
                    genomeID.append(file);
                }
            } else {
                return false;
            }
            outWriter.writeAttribute("genomeid", Integer.toString(genomeID.hashCode()));
            outWriter.writeCharacters(study.getResultingGene().getCodonSequence());
            outWriter.writeEndElement();


            /* Write protein data */
            outWriter.writeStartElement("protein");
            outWriter.writeStartElement("pdb");
            if (study.getResultingGene().getPDBCode() != null && !study.getResultingGene().getPDBCode().isEmpty()) {
                outWriter.writeAttribute("id", study.getResultingGene().getPDBCode());
            }
            outWriter.writeEndElement();
            outWriter.writeEndElement();
            /* End of protein data */

            /* Create optimizations report element */
            outWriter.writeStartElement("optimization_report");
            for (int i = 0; i < study.getOptimizationReports().size(); i++) {
                if (!writeOptimization(study.getOptimizationReports().get(i), i + 1)) {
                    return false;
                }
            }

            outWriter.writeEndElement();

            /* write orthologs */
            outWriter.writeStartElement("orthologs");
            writeOrthologs(study);
            outWriter.writeEndElement();

            /* Close study element </study>*/
            outWriter.writeEndElement();
        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'study' element: " + ex.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Write the orthologs presented in the study if they exist
     *
     * @param study
     * @return
     */
    private void writeOrthologs(Study study) {
        try {
            boolean aligned = study.getResultingGene().hasAlignedStructure(BioStructure.Type.mRNAPrimaryStructure);

            if (study.getResultingGene() == null || study.getResultingGene().getOrthologList() == null || study.getResultingGene().getOrthologList().getGenes() == null) {
                return;
            }

            List<Gene> orthologs = study.getResultingGene().getOrthologList().getGenes();

            if (aligned) {
                outWriter.writeAttribute("aligned", Boolean.TRUE.toString());

                outWriter.writeEmptyElement("resulting_gene");
                outWriter.writeAttribute("value", study.getResultingGene().getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence());

                outWriter.writeEmptyElement("protein");
                outWriter.writeAttribute("value", study.getResultingGene().getAlignedStructure(BioStructure.Type.proteinPrimaryStructure).getSequence());

                for (Gene gene : orthologs) {
                    outWriter.writeStartElement("ortholog");
                    outWriter.writeAttribute("genome_name", gene.getGenomeName());
                    outWriter.writeAttribute("gene_name", gene.getName());
                    outWriter.writeAttribute("id", gene.getOrthologId());
                    outWriter.writeAttribute("identity", String.valueOf(gene.getIdentity()));
                    outWriter.writeAttribute("score", String.valueOf(gene.getScore()));
                    outWriter.writeAttribute("sequence", gene.getAlignedStructure(BioStructure.Type.mRNAPrimaryStructure).getSequence());
                    outWriter.writeAttribute("protein", gene.getAlignedStructure(BioStructure.Type.proteinPrimaryStructure).getSequence());
                    outWriter.writeEndElement();
                }
            } else {
                outWriter.writeAttribute("aligned", Boolean.FALSE.toString());
                for (Gene gene : orthologs) {
                    outWriter.writeStartElement("ortholog");
                    outWriter.writeAttribute("genome_name", gene.getGenomeName());
                    outWriter.writeAttribute("gene_name", gene.getName());
                    outWriter.writeAttribute("id", gene.getOrthologId());
                    outWriter.writeAttribute("identity", String.valueOf(gene.getIdentity()));
                    outWriter.writeAttribute("score", String.valueOf(gene.getScore()));
                    outWriter.writeAttribute("sequence", gene.getCodonSequence());
                    outWriter.writeAttribute("protein", gene.getAminoacidSequence());
                    outWriter.writeEndElement();
                }
            }
        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'orthologs' element: " + ex.getMessage());
        }
    }

    /**
     * Creates the optimization element with all optimization info, including
     * method, score, improvement as well as redesign parameters
     *
     * @param optReport report
     * @param order order of the optimization
     * @return <li>true if successful created<li>false if error occur
     */
    private boolean writeOptimization(OptimizationReport report, int order) {
        assert report != null;
        if (order < 1) {
            return false;
        }
        try {
            /* Create single optimization element */
            outWriter.writeStartElement("optimization");
            outWriter.writeAttribute("order", String.valueOf(order));

            /* Create redesign list element */
            for (Optimization optimization : report.getOptimizations()) {
                outWriter.writeStartElement("redesign");
                outWriter.writeAttribute("method", optimization.getName());
                outWriter.writeAttribute("uid", optimization.getPlugin().getPluginId());
                outWriter.writeAttribute("version", optimization.getPlugin().getPluginVersion());
                outWriter.writeAttribute("totalscore", optimization.getResult());
                outWriter.writeAttribute("improvement", optimization.getImprovement());

                
                /* FIX ME */
                /* Create all redesign methods element */                
                for(Entry<String, ParameterDetails> entry : optimization.getParameters().getParamList().entrySet()){
                    outWriter.writeEmptyElement("redesign_parameter");                    
                    outWriter.writeAttribute("name", entry.getKey());
                    outWriter.writeAttribute("value_type", entry.getValue().getType() + "");
                    outWriter.writeAttribute("value", entry.getValue().getValue().toString());
                    outWriter.writeAttribute("minRange", String.valueOf(entry.getValue().getMinRange()));
                    outWriter.writeAttribute("maxRange", String.valueOf(entry.getValue().getMaxRange()));
                }
                
                /* close redesign element */
                outWriter.writeEndElement();
            }
            /* Close optimization element */
            outWriter.writeEndElement();

        } catch (XMLStreamException ex) {
            System.out.println("Error writing 'optimization_report': " + ex.getMessage());
            return false;
        }
        return true;
    }
}
