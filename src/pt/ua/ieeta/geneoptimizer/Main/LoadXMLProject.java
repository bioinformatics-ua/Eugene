package pt.ua.ieeta.geneoptimizer.Main;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jmol.export.dialog.FileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.GeneLoader;
import pt.ua.ieeta.geneoptimizer.GUI.LoadProjectFileProgPanel;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.GenePool;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;
import pt.ua.ieeta.geneoptimizer.geneDB.GenomeFilters;

/**
 *
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class LoadXMLProject extends Thread {

    /* Sigleton instance for loading projects - Thread safe */
    private static volatile LoadXMLProject instance = null;
    /* project to load the information */
    private Project project;
    /* XML file with saved copy of an Eugene project */
    private File eugeneXMLFile;
    /* Progress panel of the current load status */
    private LoadProjectFileProgPanel progressPanel;
    /* Control is thea ll steps while loading are valid */
    private boolean isValid;

    /* Double-Check locking with volatile instance grant the thread safe instantiation */
    public static LoadXMLProject getInstance() {
        if (instance == null) {
            synchronized (LoadXMLProject.class) {
                if (instance == null) {
                    instance = new LoadXMLProject();
                }
            }
        }
        return instance;
    }

    public void setDetails(Project project, File eugeneXMLFile, LoadProjectFileProgPanel progressPanel) {
        assert project != null;
        assert eugeneXMLFile != null;
        assert progressPanel != null;

        this.project = project;
        this.eugeneXMLFile = eugeneXMLFile;
        this.progressPanel = progressPanel;
    }

    @Override
    public void run() {
        assert project != null;
        assert eugeneXMLFile != null;
        assert progressPanel != null;

        isValid = true;

        /* initiate progress panel */
        progressPanel.setVisible(true);

        /* Check if the XML is well-formed */
        progressPanel.updateProgress(0, "Checking if project file '" + eugeneXMLFile.getName() + "' is valid!");
        Document document = isValidXML(eugeneXMLFile);
        if (document == null) {
            progressPanel.errorProgress();
            isValid = false;
        }

        /* Gets all nodes inside eugeneproject */
        Node[] nodeList = getAllSubNodes(document.getDocumentElement());

        /* Get the main elements inside the xml file */
        Node infoNode = null, genePoolNode = null, studiesNode = null;
        for (Node node : nodeList) {
            if (node.getNodeName().equalsIgnoreCase("info")) {
                infoNode = node;
            } else if (node.getNodeName().equalsIgnoreCase("genepool")) {
                genePoolNode = node;
            } else if (node.getNodeName().equalsIgnoreCase("studies")) {
                studiesNode = node;
            }
        }

        /* check if all nodes exists in the provided xml file */
        if (infoNode == null || genePoolNode == null || studiesNode == null) {
            progressPanel.errorProgress();
            isValid = false;
        }
        progressPanel.updateProgress(15, "Project file is Valid...");

        /* Load the gene pool from the provided xml file */
        progressPanel.updateProgress(20, "Loading genomes...");
        if (!loadGenePool(genePoolNode)) {
            progressPanel.errorProgress();
            isValid = false;
        }


        /* Load the studies from the provided xml file */
        progressPanel.updateProgress(60, "Loading studies...");
        if (!loadStudies(studiesNode)) {
            progressPanel.errorProgress();
            isValid = false;
        }

        //fix genome ids in case of file locations have been changed
        progressPanel.updateProgress(90, "Loading final steps...");
        fixGenomeIDs();


        if (isValid) {
            progressPanel.completeProgress();
        }

        project = null;
        eugeneXMLFile = null;
        progressPanel = null;
        instance = null;
    }

    /**
     * Check if the given XML file is well-formed
     *
     * @param file XML file file
     * @return <li>Document containing the XML file well formed<li> return null
     * if the file is not well-formed
     */
    private Document isValidXML(File file) {
        assert file != null;

        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            /* XML document containing all the Eugene project information */
            Document document = docBuilder.parse(file);

            //normalize text representation
            document.getDocumentElement().normalize();

            return document;

        } catch (SAXException ex) {
            System.out.println("Error loading Eugene project file: " + ex.getMessage());
            return null;
        } catch (IOException ex) {
            System.out.println("Error reading file: " + ex.getMessage());
            return null;
        } catch (ParserConfigurationException ex) {
            System.out.println("Error parsing file: " + ex.getMessage());
            return null;
        }
    }

    /**
     * Reads all child nodes of the gene pool node and load them to the project
     *
     * @param genePoolNode Node object that contains the genepool element
     * @return <li>True if the operation was successful<li>if error occur
     */
    private synchronized boolean loadGenePool(Node genePoolNode) {
        assert genePoolNode != null;

        /* Get all genomes inside the genepool */
        Node[] genomeList = getAllSubNodes(genePoolNode);

        List<GeneLoader> loadGenome = new ArrayList<GeneLoader>();

        /* No genomes found */
        if (genomeList.length < 1) {
            return false;
        }

        /* Load available genomes */
        Vector<Genome> avaiGenomes = GenePool.getInstance().getGenomes();

        int progressAUX;
        int beginIdx = 20;

        /* check all genomes in the provided document and add those genomes to the gene pool */
        for (int i = 0; i < genomeList.length; i++) {
            String genomeID = getAttributeValue(genomeList[i], "id");
            if (genomeID.equals("")) {
                System.out.println("No id found in the Genome '" + genomeList[i].getNodeName() + "'");
                continue;
            }
            boolean exists = false;
            /* Check if the current genome already exists in the genepool */
            for (Genome g : avaiGenomes) {
                if (g.getGenomeID() == Integer.parseInt(genomeID)) {
                    exists = true;
                }
            }

            boolean allfilesExists = true;
            if (!exists) {
                /* Get all information of the current genome */
                Node[] genomeSubNodesList = getAllSubNodes(genomeList[i]);
                if (genomeSubNodesList.length < 1) {
                    System.out.println("No genome info found for genome with id: " + genomeID + " skipping it...");
                    continue;
                }

                String genomeName = null, geneticCodeTableID = null,
                        noStopCodon = null, noStartCodon = null,
                        noMiddleStopCodon = null, multipleOfThree = null;

                List<String> sourceFiles = new ArrayList<String>();
                HashMap<String, String> additionsMap = new HashMap<String, String>();

                for (int j = 0; j < genomeSubNodesList.length; j++) {

                    if (genomeSubNodesList[j].getNodeName().equalsIgnoreCase("name")) {
                        genomeName = getAttributeValue(genomeSubNodesList[j], "value");

                    } else if (genomeSubNodesList[j].getNodeName().equalsIgnoreCase("geneticcodetable")) {
                        geneticCodeTableID = getAttributeValue(genomeSubNodesList[j], "id");

                    } else if (genomeSubNodesList[j].getNodeName().equalsIgnoreCase("filters")) {
                        Node[] filterNodes = getAllSubNodes(genomeSubNodesList[j]);
                        if (filterNodes.length < 1) {
                            System.out.println("Genome '" + genomeName + "' doesn't have filters!! Skipping it... ");
                            continue;
                        }
                        /* Get filters */
                        for (int filterCount = 0; filterCount < filterNodes.length; filterCount++) {
                            if (filterNodes[filterCount].getNodeName().equalsIgnoreCase("noStopCodon")) {
                                noStopCodon = getAttributeValue(filterNodes[filterCount], "value");
                            } else if (filterNodes[filterCount].getNodeName().equalsIgnoreCase("noStartCodon")) {
                                noStartCodon = getAttributeValue(filterNodes[filterCount], "value");
                            } else if (filterNodes[filterCount].getNodeName().equalsIgnoreCase("noMiddleStopCodon")) {
                                noMiddleStopCodon = getAttributeValue(filterNodes[filterCount], "value");
                            } else if (filterNodes[filterCount].getNodeName().equalsIgnoreCase("multipleOfThree")) {
                                multipleOfThree = getAttributeValue(filterNodes[filterCount], "value");
                            }
                        }
                    } else if (genomeSubNodesList[j].getNodeName().equalsIgnoreCase("sourcefile")) {
                        File f = new File(getAttributeValue(genomeSubNodesList[j], "value"));
                        if (!f.exists()) {
                            System.out.println("Genome file does not exists");
                            JOptionPane.showMessageDialog(MainWindow.getInstance(), "Can't find the genome file «" + f.getName() + "» for genome " + genomeName + ".");
                            JFileChooser fileChooser = new FileChooser();
                            int result = fileChooser.showOpenDialog(MainWindow.getInstance());
                            if (result == JFileChooser.APPROVE_OPTION) {
                                f = fileChooser.getSelectedFile();
                            } else {
                                allfilesExists = false;
                                break;
                            }
                        }
                        sourceFiles.add(f.getAbsolutePath());
                    } else if (genomeSubNodesList[j].getNodeName().equalsIgnoreCase("additions")) {
                        Node[] additionGenes = getAllSubNodes(genomeSubNodesList[j]);
                        if (additionGenes.length > 0) {
                            for (int aCount = 0; aCount < additionGenes.length; aCount++) {
                                additionsMap.put(getAttributeValue(additionGenes[aCount], "name"), additionGenes[aCount].getTextContent());
                            }
                        }
                    }
                }

                if (allfilesExists) {
                    assert genomeName != null;
                    assert geneticCodeTableID != null;
                    assert noStopCodon != null;
                    assert noStartCodon != null;
                    assert noMiddleStopCodon != null;
                    assert multipleOfThree != null;
                    assert !sourceFiles.isEmpty();
                    assert !genomeID.isEmpty();

                    /* Creates an array of files that has all files presented in the input file */
                    String[] newSource = new String[sourceFiles.size()];
                    sourceFiles.toArray(newSource);

                    GenomeFilters filters = new GenomeFilters(Boolean.parseBoolean(noStopCodon), Boolean.parseBoolean(noStartCodon), Boolean.parseBoolean(noMiddleStopCodon), Boolean.parseBoolean(multipleOfThree));

                    loadGenome.add(new GeneLoader(genomeName, Integer.parseInt(genomeID), Integer.parseInt(geneticCodeTableID), filters, newSource, additionsMap));

                    try {
                        Thread t = loadGenome.get(loadGenome.size() - 1);
                        t.start();
                        while (t.isAlive()) {
                            t.join();
                            progressAUX = (60 - 20) / loadGenome.size();
                            beginIdx += progressAUX;
                            progressPanel.updateProgress(beginIdx, "\tLoaded genome '" + loadGenome.get(i).getGenomeName() + "' !");
                        }
                    } catch (InterruptedException ex) {
                        System.out.println("Error loading genome: " + loadGenome.get(loadGenome.size() - 1).getGenomeName());
                    }
                } else {
                    System.out.println("Giving up loading project file: could not find genome file.");
                    progressPanel.errorProgress();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Reads all child nodes of the studies node and load them to the project
     *
     * @param studiesNode Node object that contains the studies element
     * @return <li>True if the operation was successful<li>if error occur
     */
    private boolean loadStudies(Node studiesNode) {
        assert studiesNode != null;

        Node[] study = getAllSubNodes(studiesNode);
        int progress = (90 - 60) / study.length;
        int beginIdx = 60;


        for (int i = 0; i < study.length; i++) {

            Vector<OptimizationReport> optList = null;
            Gene originalGene = null, resultingGene = null;
            Genome orthologList = null;
            String studyName = null, pdb = null;
            String resulting_gene = null, protein = null;

            Node[] studyInfo = getAllSubNodes(study[i]);
            if (studyInfo.length < 1) {
                return false;
            } else {
                for (int j = 0; j < studyInfo.length; j++) {
                    if (studyInfo[j].getNodeName().equalsIgnoreCase("title")) {
                        studyName = getAttributeValue(studyInfo[j], "value");
                    } else if (studyInfo[j].getNodeName().equalsIgnoreCase("protein")) {
                        if (resultingGene != null) {
                            Node[] node = getAllSubNodes(studyInfo[j]);
                            for (int proteinIdx = 0; proteinIdx < node.length; proteinIdx++) {
                                if (node[proteinIdx].getNodeName().equalsIgnoreCase("pdb")) {
                                    pdb = getAttributeValue(node[proteinIdx], "id");
                                    break;
                                }
                            }
                        }
                    } else if (studyInfo[j].getNodeName().equalsIgnoreCase("originalgene")) {
                        //ensure all genomes are already loaded
                        Genome genome = GenePool.getInstance().getGenome(Integer.parseInt(getAttributeValue(studyInfo[j], "genomeid")));
                        originalGene = new Gene(getAttributeValue(studyInfo[j], "name"), genome);
                        originalGene.createStructure(studyInfo[j].getTextContent(), BioStructure.Type.mRNAPrimaryStructure);
                        originalGene.calculateAllStructures();

                    } else if (studyInfo[j].getNodeName().equalsIgnoreCase("resultinggene")) {
                        //ensure all genomes are already loaded
                        Genome genome = GenePool.getInstance().getGenome(Integer.parseInt(getAttributeValue(studyInfo[j], "genomeid")));
                        resultingGene = new Gene(getAttributeValue(studyInfo[j], "name"), genome);
                        resultingGene.createStructure(studyInfo[j].getTextContent(), BioStructure.Type.mRNAPrimaryStructure);
                        resultingGene.calculateAllStructures();

                    } else if (studyInfo[j].getNodeName().equalsIgnoreCase("optimization_report")) {
                        Node[] optimizationList = getAllSubNodes(studyInfo[j]);
                        if (optimizationList.length > 0) {
                            optList = new Vector<OptimizationReport>();
                            for (int optimizationIdx = 0; optimizationIdx < optimizationList.length; optimizationIdx++) {
                                optList.add(getOptimizationReport(optimizationList[optimizationIdx]));
                            }

                            //force optimization list to be sorted by order
                            Collections.sort(optList, new Comparator<OptimizationReport>() {
                                @Override
                                public int compare(OptimizationReport o1, OptimizationReport o2) {
                                    return o1.getOrder() - o2.getOrder();
                                }
                            });
                        }
                    } else if (studyInfo[j].getNodeName().equalsIgnoreCase("orthologs")) {
                        Node[] orthologInfo = getAllSubNodes(studyInfo[j]);
                        if (orthologInfo.length != 0) {
                            boolean aligned = Boolean.parseBoolean(getAttributeValue(studyInfo[j], "aligned"));
                            orthologList = getOrthologs(orthologInfo, aligned);

                            //set resulting and protein sequence if orthlogs are aligned
                            if (aligned) {
                                for (int orthIdx = 0; orthIdx < orthologInfo.length; orthIdx++) {
                                    if (orthologInfo[orthIdx].getNodeName().equals("resulting_gene")) {
                                        resulting_gene = getAttributeValue(orthologInfo[orthIdx], "value");
                                    } else if (orthologInfo[orthIdx].getNodeName().equals("protein")) {
                                        protein = getAttributeValue(orthologInfo[orthIdx], "value");
                                    }
                                }

                            }
                        }
                    }
                }
            }
            assert originalGene != null && resultingGene != null && studyName != null;

            Study newStudy = new Study(originalGene, resultingGene, studyName);

            if (optList != null) {
                newStudy.setOptimizationReport(optList);
            }

            if (pdb != null) {
                newStudy.getResultingGene().setPDBCode(pdb);
            }

            newStudy.getResultingGene().setOrthologList(orthologList);

            if (protein != null && resulting_gene != null && !protein.isEmpty() && !resulting_gene.isEmpty()) {
                newStudy.getResultingGene().setAlignedStructure(resulting_gene, BioStructure.Type.mRNAPrimaryStructure);
                newStudy.getResultingGene().setAlignedStructure(protein, BioStructure.Type.proteinPrimaryStructure);
            }

            project.addNewStudy(newStudy);

//            new GeneAutoDiscover(newStudy).start();
            beginIdx += progress;
            progressPanel.updateProgress(beginIdx, "\tLoading study '" + newStudy.getName() + "' !");
        }
        return true;
    }

    /**
     * Reads the value of the node and get all optimization info for the given
     * optimization node
     *
     * @param study Node object that contains the optimization info
     * @return Optimization
     */
    private OptimizationReport getOptimizationReport(Node optimizationNode) {
        assert optimizationNode != null;
        int order = Integer.parseInt(getAttributeValue(optimizationNode, "order"));
        OptimizationReport optimization = new OptimizationReport(order);
        Node[] redesign = getAllSubNodes(optimizationNode);
        for (int i = 0; i < redesign.length; i++) {
            String methodName = getAttributeValue(redesign[i], "method");
            String uid = getAttributeValue(redesign[i], "uid");
            String version = getAttributeValue(redesign[i], "version");
            String totalScore = getAttributeValue(redesign[i], "totalscore");
            String improvement = getAttributeValue(redesign[i], "improvement");
            ParameterSet parameters = new ParameterSet();
            Node[] redesign_parameter = getAllSubNodes(redesign[i]);

            for (int j = 0; j < redesign_parameter.length; j++) {
                Object type = getAttributeValue(redesign_parameter[j], "value_type");
                Integer minRange = Integer.parseInt(getAttributeValue(redesign_parameter[j], "minRange"));
                Integer maxRange = Integer.parseInt(getAttributeValue(redesign_parameter[j], "maxRange"));
                ParameterDetails p = null;
                if (type.equals("class java.lang.Integer")) {
                    p = new ParameterDetails(Integer.class, Integer.valueOf(getAttributeValue(redesign_parameter[j], "value")), minRange, maxRange);
                } else if (type.equals("class java.lang.Float")) {
                    p = new ParameterDetails(Float.class, Float.valueOf(getAttributeValue(redesign_parameter[j], "value")), minRange, maxRange);
                } else if (type.equals("class java.lang.Boolean")) {
                    p = new ParameterDetails(Boolean.class, Boolean.valueOf(getAttributeValue(redesign_parameter[j], "value")), minRange, maxRange);
                } else if (type.equals("class java.lang.String")) {
                    p = new ParameterDetails(String.class, String.valueOf(getAttributeValue(redesign_parameter[j], "value")), minRange, maxRange);
                }
                assert p != null;
                parameters.addParameter(getAttributeValue(redesign_parameter[j], "name"), p);
            }
            // TODO FIX IOptimization PLUGIN.. NULL?????            
            optimization.addOptimization(methodName, parameters, totalScore, improvement, null);
        }
        return optimization;
    }

    /**
     * Fix genomes ID's in gene pool need to be updated and update them
     */
    private void fixGenomeIDs() {
        Vector<Genome> genomeList = GenePool.getInstance().getGenomes();
        for (Genome g : genomeList) {
            String id = new String();
            for (String geneFile : g.getGenesFiles()) {
                id += geneFile;
            }
            GenePool.getInstance().getGenome(g.getGenomeID()).setGenomeID(id.hashCode());
        }
    }

    /**
     * Reads the value of an attribute inside a node
     *
     * @param node Node object that contains the attribute
     * @param attributeName Name of the attribute
     * @return Value of the attribute inside the node or an Empty String if the
     * attribute doesn't exists
     */
    private String getAttributeValue(Node node, String attributeName) {
        assert node != null;
        assert attributeName != null;

        if (node.getAttributes() != null && !attributeName.isEmpty() && node.getAttributes().getNamedItem(attributeName) != null) {
            return node.getAttributes().getNamedItem(attributeName).getNodeValue();
        }
        return "";
    }

    /**
     * Gets an array of all the sub-nodes, that are elements, within the
     * provided node
     *
     * @param node Node object that may contain sub-nodes
     * @return Node[] an array of all sub-nodes of the given node or an array of
     * size 0 if no nodes were found
     */
    private Node[] getAllSubNodes(Node node) {
        assert node != null;

        ArrayList<Node> nodeList = new ArrayList<Node>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                nodeList.add(childNode);
            }
        }
        return (Node[]) nodeList.toArray(new Node[nodeList.size()]);
    }

    /**
     * Convert a String value to an Object of the correct type according to the
     * String value
     *
     * @param value Value of the redesign parameter
     * @return Value object in the correct type
     */
    private Object getOptimizationValueType(String value) {
        //remove white spaces
        value = value.trim();

        //check if value is a number
        Pattern p = Pattern.compile("((-|\\+)?[0-9]+(\\.[0-9]+)?)+");
        Matcher match = p.matcher(value);

        // if its a number return the value as an integer or a float
        if (match.matches()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {
                return Float.parseFloat(value);
            }
        }

        //check if it's a boolean type otherwise return string
        p = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
        match = p.matcher(value);
        if (match.matches()) {
            return Boolean.parseBoolean(value);
        }
        //last case return value as a String
        return value;
    }

    /**
     * Get the list of all orthologs
     *
     * @param orthologInfo node containing orthologs info
     * @return Genome containing all orthlogs
     */
    private Genome getOrthologs(Node[] orthologInfo, boolean aligned) {

        Genome genome = new Genome("ortholog List", new GeneticCodeTable());
        if (aligned) {
            for (int i = 0; i < orthologInfo.length; i++) {
                if (orthologInfo[i].getNodeName().equals("ortholog")) {
                    Gene g = new Gene(getAttributeValue(orthologInfo[i], "gene_name"), genome);
                    g.setOrthologInfo(Integer.parseInt(getAttributeValue(orthologInfo[i], "score")),
                            Double.parseDouble(getAttributeValue(orthologInfo[i], "identity")),
                            getAttributeValue(orthologInfo[i], "id"),
                            getAttributeValue(orthologInfo[i], "genome_name"));
                    g.setAlignedStructure(getAttributeValue(orthologInfo[i], "sequence"), BioStructure.Type.mRNAPrimaryStructure);
                    g.setAlignedStructure(getAttributeValue(orthologInfo[i], "protein"), BioStructure.Type.proteinPrimaryStructure);
                    g.createStructure(getAttributeValue(orthologInfo[i], "sequence").replaceAll("-", ""), BioStructure.Type.mRNAPrimaryStructure);
                    g.createStructure(getAttributeValue(orthologInfo[i], "protein").replaceAll("-", ""), BioStructure.Type.proteinPrimaryStructure);
                    genome.addGene(g);
                }
            }
        } else {
            for (int i = 0; i < orthologInfo.length; i++) {
                if (orthologInfo[i].getNodeName().equals("ortholog")) {
                    Gene g = new Gene(getAttributeValue(orthologInfo[i], "gene_name"), genome);
                    g.setOrthologInfo(Integer.parseInt(getAttributeValue(orthologInfo[i], "score")),
                            Double.parseDouble(getAttributeValue(orthologInfo[i], "identity")),
                            getAttributeValue(orthologInfo[i], "id"),
                            getAttributeValue(orthologInfo[i], "genome_name"));
                    g.createStructure(getAttributeValue(orthologInfo[i], "sequence"), BioStructure.Type.mRNAPrimaryStructure);
                    g.createStructure(getAttributeValue(orthologInfo[i], "protein"), BioStructure.Type.proteinPrimaryStructure);
                    genome.addGene(g);
                }
            }
        }
        return genome;
    }

    public static boolean isValid() {
        return isValid();
    }
}
