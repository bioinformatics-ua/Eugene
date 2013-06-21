package pt.ua.ieeta.geneoptimizer.GeneRedesign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationReport.Optimization;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.Main;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterDetails;
import pt.ua.ieeta.geneoptimizer.PluginSystem.ParameterSet;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class RedesignProtocolReaderWriter extends Thread {
    /* List of loaded studies. */

    private static Vector<OptimizationReport> studyList;

    /* Singleton instance. */
    private static volatile RedesignProtocolReaderWriter instance = null;
    /* Project version */
    private final static String SCHEMA_VERSION = "1.0";

    public static RedesignProtocolReaderWriter getInstance() {
        if (instance == null) {
            synchronized (RedesignProtocolReaderWriter.class) {
                if (instance == null) {
                    instance = new RedesignProtocolReaderWriter();
                }
            }
        }
        return instance;
    }

    private RedesignProtocolReaderWriter() {
    }

    @Override
    public void run() {
        System.out.println("RedesignProtocolReaderWriter started.");


        loadParametersFromFile();

        System.out.println("RedesignProtocolReaderWriter ended.");
    }

    public synchronized boolean loadParametersFromFile() {

        studyList = new Vector<OptimizationReport>();

        /* Load default directory instance. */
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String studiesPath = (String) ApplicationSettings.getProperty("studiesPath", String.class);

        File directory = new File(eugeneDir + studiesPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("RedesignProtocolReaderWriter error reading directory");
            return false;
        }

        File[] fileList = new File[directory.list().length];
        for (int i = 0; i < directory.list().length; i++) {
            fileList[i] = new File(directory.listFiles()[i].getAbsolutePath());
        }

        /* Get optimizations of each xml file */
        for (File file : fileList) {
            /* Ignore non-class files. */
            if (!file.getAbsolutePath().endsWith(".study")) {
                continue;
            }
            Document document = isValidXML(file);
            if (document == null) {
                continue;
            }

            /* Gets all nodes inside redesign_protocol */
            Node[] nodeList = getAllSubNodes(document.getDocumentElement());
            Node info = null, redesign_list = null;
            for (Node node : nodeList) {
                if (node.getNodeName().equalsIgnoreCase("info")) {
                    info = node;
                } else if (node.getNodeName().equalsIgnoreCase("redesign_list")) {
                    redesign_list = node;
                } else {
                    //next file
                    continue;
                }
            }

            //ignore.. no info or redesign list found
            if (info == null || redesign_list == null) {
                //next file
                continue;
            }

            Node[] infoList = getAllSubNodes(info);
            if (infoList.length == 0) {
                continue;
            }


            // get info from info node
            String schema_version = null, eugene_version = null, name = null;
            for (Node node : infoList) {
                if (node.getNodeName().equalsIgnoreCase("schema_version")) {
                    schema_version = getAttributeValue(node, "value");
                } else if (node.getNodeName().equalsIgnoreCase("eugene_version")) {
                    eugene_version = getAttributeValue(node, "value");
                } else if (node.getNodeName().equalsIgnoreCase("name")) {
                    name = getAttributeValue(node, "value");
                }
            }
            if (eugene_version.isEmpty() || !eugene_version.equalsIgnoreCase(Main.getVersion())
                    || schema_version.isEmpty() || !schema_version.equalsIgnoreCase(SCHEMA_VERSION)) {
                continue;
            }

            OptimizationReport report = new OptimizationReport();
            report.setReportName(file.getName().replaceFirst("[.][^.]+$", ""));
            Node[] redesign = getAllSubNodes(redesign_list);
            for (Node redesign_node : redesign) {
                String redesign_name = getAttributeValue(redesign_node, "method_name");
                String redesign_id = getAttributeValue(redesign_node, "id");
                String redesign_version = getAttributeValue(redesign_node, "version");
                ParameterSet parameters = new ParameterSet();
                Node[] redesign_parameter = getAllSubNodes(redesign_node);

                /* FIX ME */
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
                report.addOptimization(redesign_name, parameters, null, null, null); //TODO COMPLETAR COM O PLUGIN
            }
            if (!report.getOptimizations().isEmpty()) {
                studyList.add(report);
            }
        }
        return true;
    }

    public boolean saveParametersToFile(OptimizationReport report) {
        /* Create directory instance. If directory does not exist, exit function. */
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String studiesPath = (String) ApplicationSettings.getProperty("studiesPath", String.class);
        File directory = new File(eugeneDir + studiesPath);
        if (!directory.exists() || !directory.isDirectory()) {
            return false;
        }



        /* Create new file instance. If file already exists, erase it. */
        File newFile = new File(directory.getPath() + File.separator + report.getReportName().replaceAll("\\s", "_") + ".study");
        if (newFile.exists() && !newFile.isDirectory()) {
            System.out.println("FILE EXISTS.... REMOVING IT... FIX ME");
            newFile.delete();
        }

        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileWriter(newFile));

            /* write headers */
            writer.writeStartDocument();
            writer.writeStartElement("redesign_protocol");

            writer.writeStartElement("info");
            writer.writeEmptyElement("schema_version");
            writer.writeAttribute("value", SCHEMA_VERSION);
            writer.writeEmptyElement("eugene_version");
            writer.writeAttribute("value", Main.getVersion());
            writer.writeEmptyElement("name");
            writer.writeAttribute("value", newFile.getName());
            writer.writeEndElement(); //info element

            writer.writeStartElement("redesign_list");


            for (Optimization opt : report.getOptimizations()) {
                writer.writeStartElement("redesign");

                writer.writeAttribute("method_name", opt.getName());
                writer.writeAttribute("id", opt.getPlugin().getPluginId());
                writer.writeAttribute("version", opt.getPlugin().getPluginVersion());

                for (Entry<String, ParameterDetails> entry : opt.getParameters().getParamList().entrySet()) {
                    writer.writeEmptyElement("redesign_parameter");
                    writer.writeAttribute("name", entry.getKey());
                    writer.writeAttribute("value_type", entry.getValue().getType() + "");
                    writer.writeAttribute("value", entry.getValue().getValue().toString());
                    writer.writeAttribute("minRange", String.valueOf(entry.getValue().getMinRange()));
                    writer.writeAttribute("maxRange", String.valueOf(entry.getValue().getMaxRange()));
                }


                /**
                 * FIX ME *
                 */
//                for (Map.Entry<String, Object> redesign_parameter : opt.getParameters().getParamList().entrySet()) {
//                    writer.writeEmptyElement("redesign_parameter");
//                    writer.writeAttribute("name", redesign_parameter.getKey());
//                    writer.writeAttribute("value", redesign_parameter.getValue().toString());
//                }
                writer.writeEndElement(); //end redesign element
            }

            writer.writeEndElement(); //redesign list element            
            /* close xml document */
            writer.writeEndElement();
            writer.writeEndDocument();

            /* write data and free associated resources */
            writer.flush();
            writer.close();

        } catch (IOException ex) {
            System.out.println("Error saving file: " + ex.getMessage());
            return false;
        } catch (XMLStreamException ex) {
            System.out.println("Error saving file: " + ex.getMessage());
            return false;
        }

        try {
            byte[] fileBytes = new byte[(int) newFile.length()];
            FileInputStream inputStream = new FileInputStream(newFile);
            inputStream.read(fileBytes);

            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setAttribute("indent-number", new Integer(2));

            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");

            t.transform(new StreamSource(new ByteArrayInputStream(fileBytes)), new StreamResult(newFile));

            inputStream.close();

        } catch (IOException ex) {
            System.out.println("File '" + newFile.getName() + "' may not exist! : " + ex.getMessage());
            System.out.println("XML not indented!!!");
            return false;
        } catch (TransformerException ex) {
            System.out.println("Error indenting xml file: " + ex.getMessage());
            return false;
        }
        return true;
    }

    public Vector<OptimizationReport> getStudies() {
        return studyList;
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
}
