package pt.ua.ieeta.geneoptimizer.FileHandling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.geneDB.GeneticCodeTable;

/** 
 * @author Paulo
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class GeneticCodeTableParser {

    private static volatile GeneticCodeTableParser instance = null;        

    public static GeneticCodeTableParser getInstance() {
        if (instance == null) {
            synchronized (GeneticCodeTable.class) {
                if (instance == null) {
                    instance = new GeneticCodeTableParser();
                }
            }
        }
        return instance;
    }

    private GeneticCodeTableParser() {
    }

    /* Parses the genetic code table file to return the code table for a given ID. */
    public GeneticCodeTable getCodeTableByID(int ID) throws FileNotFoundException, IOException {
        assert ID >= 1;

        /* Create file readers. */
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String gctFileName = (String) ApplicationSettings.getProperty("geneticCodeTableFileName", String.class);

        if (gctFileName == null) {
            gctFileName = createDefaultGeneticCodeTable();
        } else if (!new File(eugeneDir, gctFileName).exists() || gctFileName.isEmpty()) {
            gctFileName = createDefaultGeneticCodeTable();
        }

        BufferedReader br = new BufferedReader(new FileReader(eugeneDir + gctFileName));

        String line;
        String name = null, id = null, ncbieaa = null, sncbieaa = null, base1 = null, base2 = null, base3 = null;
        boolean hasName = false;
        while ((line = br.readLine()) != null) {
            /* Erase trailing spaces. */
            line = line.trim();

            /* If it's an empty or comment line, ignore it. */
            if (line.isEmpty()
                    || line.startsWith("--") && !line.startsWith("-- Base")
                    || line.startsWith("Genetic-code-table")
                    || line.startsWith("{")
                    || line.startsWith("}")) {
                continue;
            }

            if (line.startsWith("name") && !hasName) {
                hasName = true;
                if (line.lastIndexOf('"') != line.indexOf('"')) {
                    name = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
                } else {
                    name = line.substring(line.indexOf('"') + 1);
                }
            } else if (line.startsWith("id")) {
                hasName = false;
                id = line.substring(line.indexOf(' ') + 1, line.lastIndexOf(',')).trim();
            } else if (line.startsWith("ncbieaa")) {
                ncbieaa = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
            } else if (line.startsWith("sncbieaa")) {
                sncbieaa = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
            } else if (line.startsWith("-- Base")) {
                if (line.charAt(7) == '1') {
                    base1 = line.substring(8).trim();
                } else if (line.charAt(7) == '2') {
                    base2 = line.substring(8).trim();
                } else {
                    base3 = line.substring(8).trim();
                    if (Integer.parseInt(id) == ID) {
                        break;
                    }
                }


            }
        }

        br.close();

        return new GeneticCodeTable(name, ID, ncbieaa, sncbieaa, base1, base2, base3);
    }

    public HashMap<Integer, String> getGeneticCodeTableNames() throws FileNotFoundException, IOException {
        HashMap<Integer, String> names = new HashMap<Integer, String>();

        /* Create file readers. */
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String gctFileName = (String) ApplicationSettings.getProperty("geneticCodeTableFileName", String.class);

        if (gctFileName == null) {
            gctFileName = createDefaultGeneticCodeTable();
        } else if (!new File(eugeneDir, gctFileName).exists() || gctFileName.isEmpty()) {
            gctFileName = createDefaultGeneticCodeTable();
        }

        BufferedReader br = new BufferedReader(new FileReader(eugeneDir + gctFileName));

        String line;
        String name = null, id = null;
        boolean hasName = false;
        while ((line = br.readLine()) != null) {
            /* Erase trailing spaces. */
            line = line.trim();

            /* If it's an empty or comment line, ignore it. */
            if (line.isEmpty()
                    || line.startsWith("--") && !line.startsWith("-- Base")
                    || line.startsWith("Genetic-code-table")
                    || line.startsWith("{")
                    || line.startsWith("}")) {
                continue;
            }

            if (line.startsWith("name") && !hasName) {
                hasName = true;
                if (line.lastIndexOf('"') != line.indexOf('"')) {
                    name = line.substring(line.indexOf('"') + 1, line.lastIndexOf('"'));
                } else {
                    name = line.substring(line.indexOf('"') + 1);
                }
            } else if (line.startsWith("id")) {
                hasName = false;
                id = line.substring(line.indexOf(' ') + 1, line.lastIndexOf(',')).trim();
                names.put(Integer.parseInt(id), name);
            }
        }

        br.close();

        return names;
    }

    private String createDefaultGeneticCodeTable() {
        String eugeneDir = (String) ApplicationSettings.getProperty("eugene_dir", String.class);
        String filename = (String) ApplicationSettings.getProperty("geneticCodeTableFileName", String.class);

        if (filename == null) {
            filename = "geneticCodeTableNCBI";
        } else if (filename.isEmpty()) {
            filename = "geneticCodeTableNCBI";
        }

        File gctFile = new File(eugeneDir, filename);

        System.out.println("Creating default genetic code table.");

        try {
            if(!gctFile.createNewFile()) {
                System.out.println("Error creating default genetic code table file");
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(gctFile));
            bw.write(defaultGeneticCodeTable);
            bw.flush();
            bw.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(GeneticCodeTableParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        return filename;
    }
    private static String defaultGeneticCodeTable = "Genetic-code-table ::= {\n"
            + " {\n"
            + "  name \"Standard\" ,\n"
            + "  name \"SGC0\" ,\n"
            + "  id 1 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"---M---------------M---------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Vertebrate Mitochondrial\" ,\n"
            + "  name \"SGC1\" ,\n"
            + "  id 2 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSS**VVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"--------------------------------MMMM---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Yeast Mitochondrial\" ,\n"
            + "  name \"SGC2\" ,\n"
            + "  id 3 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCWWTTTTPPPPHHQQRRRRIIMMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"----------------------------------MM----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "    name \"Mold Mitochondrial; Protozoan Mitochondrial; Coelenterate\n"
            + " Mitochondrial; Mycoplasma; Spiroplasma\" ,\n"
            + "  name \"SGC3\" ,\n"
            + "  id 4 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"--MM---------------M------------MMMM---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Invertebrate Mitochondrial\" ,\n"
            + "  name \"SGC4\" ,\n"
            + "  id 5 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSSSSVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"---M----------------------------MMMM---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Ciliate Nuclear; Dasycladacean Nuclear; Hexamita Nuclear\" ,\n"
            + "  name \"SGC5\" ,\n"
            + "  id 6 ,\n"
            + "  ncbieaa  \"FFLLSSSSYYQQCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Echinoderm Mitochondrial; Flatworm Mitochondrial\" ,\n"
            + "  name \"SGC8\" ,\n"
            + "  id 9 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNNKSSSSVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Euplotid Nuclear\" ,\n"
            + "  name \"SGC9\" ,\n"
            + "  id 10 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCCWLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Bacterial and Plant Plastid\" ,\n"
            + "  id 11 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"---M---------------M------------MMMM---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Alternative Yeast Nuclear\" ,\n"
            + "  id 12 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CC*WLLLSPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-------------------M---------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Ascidian Mitochondrial\" ,\n"
            + "  id 13 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNKKSSGGVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"---M------------------------------MM---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " },\n"
            + " {\n"
            + "  name \"Alternative Flatworm Mitochondrial\" ,\n"
            + "  id 14 ,\n"
            + "  ncbieaa  \"FFLLSSSSYYY*CCWWLLLLPPPPHHQQRRRRIIIMTTTTNNNKSSSSVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " } ,\n"
            + " {\n"
            + "  name \"Blepharisma Macronuclear\" ,\n"
            + "  id 15 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY*QCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " } ,\n"
            + " {\n"
            + "  name \"Chlorophycean Mitochondrial\" ,\n"
            + "  id 16 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY*LCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " } ,\n"
            + " {\n"
            + "  name \"Trematode Mitochondrial\" ,\n"
            + "  id 21 ,\n"
            + "  ncbieaa  \"FFLLSSSSYY**CCWWLLLLPPPPHHQQRRRRIIMMTTTTNNNKSSSSVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " } ,\n"
            + " {\n"
            + "  name \"Scenedesmus obliquus Mitochondrial\" ,\n"
            + "  id 22 ,\n"
            + "  ncbieaa  \"FFLLSS*SYY*LCC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"-----------------------------------M----------------------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " } ,\n"
            + " {\n"
            + "  name \"Thraustochytrium Mitochondrial\" ,\n"
            + "  id 23 ,\n"
            + "  ncbieaa  \"FF*LSSSSYY**CC*WLLLLPPPPHHQQRRRRIIIMTTTTNNKKSSRRVVVVAAAADDEEGGGG\",\n"
            + "  sncbieaa \"--------------------------------M--M---------------M------------\"\n"
            + "  -- Base1  TTTTTTTTTTTTTTTTCCCCCCCCCCCCCCCCAAAAAAAAAAAAAAAAGGGGGGGGGGGGGGGG\n"
            + "  -- Base2  TTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGGTTTTCCCCAAAAGGGG\n"
            + "  -- Base3  TCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAGTCAG\n"
            + " }\n"
            + "}";
}
