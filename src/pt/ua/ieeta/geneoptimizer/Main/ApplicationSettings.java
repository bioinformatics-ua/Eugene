package pt.ua.ieeta.geneoptimizer.Main;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import pt.ua.ieeta.geneoptimizer.GUI.MainWindow;

/**
 * Class to manage application settings
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class ApplicationSettings
{
    /* Settings list: map the name of the setting to an object (with a class). */
    private static Map<String, String> settings;

    /* Fixed application settings. */
    private static String home_dir = System.getProperty("user.home");
    private static String separator = System.getProperty("file.separator");
    private static String eugene_dir = home_dir + separator + "EuGene" + separator;
    private static String settingsFileName = "settings.ini";
    private static Map<String, ImageIcon> imageMapForSecondaryStructure = null;

    private static Properties properties;
    private static volatile ApplicationSettings instance = null;
    
    public static ApplicationSettings getInstance() {
        if (instance == null) {
            synchronized(ApplicationSettings.class){
                if (instance == null){
                    instance = new ApplicationSettings();
                }
            }            
        }
        return instance;
    }
    
    private ApplicationSettings() {
        settings = new HashMap<String, String>();
        properties = new Properties();
        
        /* Verify if setting exists */
        if (!new File(eugene_dir, settingsFileName).exists()) {
            createDefaultSettingsFile();
        } else {
            /* Load all settings from file. */
            try {
                properties.load(new FileInputStream(new File(eugene_dir, settingsFileName)));

                System.out.println("Loading settings file...");
                for (Object key : properties.keySet()) {
                    settings.put((String) key, properties.getProperty((String) key));
                }
                System.out.println("Loading settings file complete!\n");
            }
            catch (IOException ex) {
                System.out.println("Fail loading settings");
                ex.printStackTrace();
             }
        }
        
        /* Verify if settings is current version*/
        verifySettingsFile();

        
//        /* Load all settings from file. */
//        try {
//            properties = new Properties();
//            properties.load(new FileInputStream(new File(eugene_dir, settingsFileName)));
//
//            System.out.println("Loading settings file...");
//            for (Object key : properties.keySet()) {
//                settings.put((String) key, properties.getProperty((String) key));
//            }
//
//
//        } catch (Exception e) {
//            if (!new File(eugene_dir, settingsFileName).exists()) {
//                createDefaultSettingsFile();
//            }
//        }
    }

    
    private void createDefaultSettingsFile() {
        System.out.println("Creating new settings file...");

        List<Setting> defaultSettings = getDefaultSettings();
        File settingsFile = new File(eugene_dir, settingsFileName);

        try {
            settingsFile.createNewFile();

            for (Iterator<Setting> it = defaultSettings.iterator(); it.hasNext();) {
                Setting setting = it.next();
                setProperty(setting.propertyName, setting.value, setting.type);
            }
        } catch (IOException ex) {
            //TODO: exceptions...
            System.out.println("Fail creating settings file !!!");
            ex.printStackTrace();
        }
        
        System.out.println("Creating new settings file complete!");
    }
    
    
    /**
     * Verify that all settings needed and if there are not create them
     */
    private void verifySettingsFile() {
        System.out.println("Verifying the settings file...");
        
        List<Setting> defaultSettings = getDefaultSettings();
        
        for (Iterator<Setting> it = defaultSettings.iterator(); it.hasNext();) {
            Setting setting = it.next();
            
            if ( !settings.containsKey(setting.propertyName) ) {
                setProperty(setting.propertyName, setting.value, setting.type);
            }
        }
        
        System.out.println("Verifying the settings file complete!\n");
    }
    
    
    private List<Setting> getDefaultSettings() {
        List<Setting> defaultSettings = new ArrayList<Setting>(31);
        /* Gene filters. */
        defaultSettings.add(new Setting("checkUnknownLetters", true, Boolean.class));
        defaultSettings.add(new Setting("checkStartCodon", true, Boolean.class));
        defaultSettings.add(new Setting("checkIsMultipleOfThree", true, Boolean.class));
        defaultSettings.add(new Setting("checkMiddleStopCodon", true, Boolean.class));
        defaultSettings.add(new Setting("checkStopCodon", true, Boolean.class));

        /* Folders and files. */
        defaultSettings.add(new Setting("eugene_dir", eugene_dir, String.class));
        defaultSettings.add(new Setting("highlyExpressedGenesFolder", "HighlyExpressed", String.class));
        defaultSettings.add(new Setting("studiesPath", "Studies", String.class));
        defaultSettings.add(new Setting("geneticCodeTableFileName", "geneticCodeTableNCBI.txt", String.class));
        defaultSettings.add(new Setting("toolsPath", "Tools", String.class));
        defaultSettings.add(new Setting("toolsVersion", 0, Integer.class));
        defaultSettings.add(new Setting("pluginPath", "Plugins", String.class));
        defaultSettings.add(new Setting("projectsPath", "Projects", String.class));
        defaultSettings.add(new Setting("imagesPath", "Images", String.class));
        defaultSettings.add(new Setting("lastGeneDirectory", ".", String.class));
        defaultSettings.add(new Setting("highlyExpressedGenesFileName", "highly_expressed.txt", String.class));
        defaultSettings.add(new Setting("userEmail", "bioinfo@ieeta.pt", String.class));

        /* Discovery thresholds. */
        defaultSettings.add(new Setting("NCBIMatchThreshold", 95, Integer.class));
        defaultSettings.add(new Setting("CAIOrthologsThreshold", 30, Integer.class));
        defaultSettings.add(new Setting("AutoDiscoverScoreThreshold", 70, Integer.class));
        defaultSettings.add(new Setting("PDBMatchThreshold", 75, Integer.class));

        /* Web services simulataneous calls. */
        defaultSettings.add(new Setting("maxNumberOfNCBISimultaneousCalls", 1, Integer.class));
        defaultSettings.add(new Setting("maxNumberOfKeggSimultaneousCalls", 1, Integer.class));
        defaultSettings.add(new Setting("maxNumberOfPDBBlastSimultaneousCalls", 1, Integer.class));

        /* Discovery enabling. */
        defaultSettings.add(new Setting("genomeAutoDiscoveryEnabled", true, Boolean.class));
        defaultSettings.add(new Setting("geneAutoDiscoveryEnabled", true, Boolean.class));
        defaultSettings.add(new Setting("autoCalculateProteinPrimaryStructure", true, Boolean.class));
        defaultSettings.add(new Setting("autoCalculateProteinSecondaryStructure", false, Boolean.class));

        /* Optimization parameters. */
        defaultSettings.add(new Setting("probabilityOfMutationRegulator", 40, Integer.class));
        defaultSettings.add(new Setting("obtainParetoFront", true, Boolean.class));

        /* GUI settings. */
        defaultSettings.add(new Setting("sequenceLabelWidthPixel", 26, Integer.class));
        defaultSettings.add(new Setting("sizeOfSpecieNameLabel", 120, Integer.class));
        
        return defaultSettings;
    }
    
    
    public static void setProperty(String propertieName, Object value, Class type) {
        //assert getInstance().settings.containsKey(propertieName);
        String strValue = null;

        if (type == String.class) {
            strValue = (String) value;
        }
        if (type == Integer.class) {
            strValue = Integer.toString((Integer) value);
        }
        if (type == Boolean.class) {
            strValue = Boolean.toString((Boolean) value);
        }
        if (type == Double.class) {
            strValue = Double.toString((Double) value);
        }
        
        settings.put(propertieName, strValue);
        properties.put(propertieName, strValue);

        try {
            FileOutputStream out = new FileOutputStream(new File(eugene_dir, settingsFileName));
            properties.store(out, "/* properties updated on: */");
            out.flush();
            out.close();
        } catch (Exception ex) {
            //TODO: exceptions..
            ex.printStackTrace();
            Logger.getLogger(ApplicationSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    public static Object getProperty(String propertieName, Class returnType) 
    {
        if (!getInstance().settings.containsKey(propertieName)) return null;

        if (returnType == String.class) {
            return String.valueOf(settings.get(propertieName));
        }
        if (returnType == Integer.class) {
            return Integer.valueOf(settings.get(propertieName));
        }
        if (returnType == Boolean.class) {
            return Boolean.parseBoolean(settings.get(propertieName));
        }
        if (returnType == Double.class) {
            return Double.parseDouble(settings.get(propertieName));
        }

        return null;
    }

    //TODO: se isto é feito todas as vezes que é chamada a funçao, é muito ineficiente!
    public static Map<String, ImageIcon> getImageMapForSecondaryStructure() {
        int sequenceLabelWidthPixel = (Integer) ApplicationSettings.getProperty("sequenceLabelWidthPixel", Integer.class);
        Class mainWindow = MainWindow.getInstance().getClass();
        String[] resource = {"/pt/ua/ieeta/geneoptimizer/resources/helix.jpg",
            "/pt/ua/ieeta/geneoptimizer/resources/coil.jpg",
            "/pt/ua/ieeta/geneoptimizer/resources/strand.jpg",
            "/pt/ua/ieeta/geneoptimizer/resources/strand_end.jpg"};

        if (imageMapForSecondaryStructure == null) {
            ImageIcon helix = new ImageIcon(Toolkit.getDefaultToolkit().getImage(mainWindow.getResource(resource[0])).getScaledInstance(sequenceLabelWidthPixel + 1, 15, 100));
            ImageIcon coil = new ImageIcon(Toolkit.getDefaultToolkit().getImage(mainWindow.getResource(resource[1])).getScaledInstance(sequenceLabelWidthPixel + 1, 15, 100));
            ImageIcon strand = new ImageIcon(Toolkit.getDefaultToolkit().getImage(mainWindow.getResource(resource[2])).getScaledInstance(sequenceLabelWidthPixel + 1, 15, 100));
            ImageIcon strand_end = new ImageIcon(Toolkit.getDefaultToolkit().getImage(mainWindow.getResource(resource[3])).getScaledInstance(sequenceLabelWidthPixel + 1, 15, 100));

            imageMapForSecondaryStructure = new HashMap<String, ImageIcon>();
            imageMapForSecondaryStructure.put("C", coil);
            imageMapForSecondaryStructure.put("H", helix);
            imageMapForSecondaryStructure.put("E", strand);
            imageMapForSecondaryStructure.put("Eend", strand_end);
        }

        return imageMapForSecondaryStructure;
    }
    
    
    /**
     * To save a setting in vector
     */
    private class Setting{
        String propertyName;
        Object value;
        Class type;

        public Setting(String propertyName, Object value, Class type) {
            this.propertyName = propertyName;
            this.value = value;
            this.type = type;
        }
    }
}
