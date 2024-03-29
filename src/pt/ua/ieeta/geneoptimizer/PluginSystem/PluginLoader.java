package pt.ua.ieeta.geneoptimizer.PluginSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.OptimizationModel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;

/**
 *
 * @author Paulo Gaspar
 * @author Nuno Silva <nuno.mogas@ua.pt>
 */
public class PluginLoader extends Observable implements Runnable {

    private static List<Class> pluginList;

    /* Singleton instance. */
    private static volatile PluginLoader instance = null;

    private PluginLoader() {
    }

    /* Flag that tell wether all pluglins were already loaded or not. */
    private boolean doneLoading = false;

    public static PluginLoader getInstance() {
        if (instance == null) {
            synchronized (PluginLoader.class) {
                if (instance == null) {
                    instance = new PluginLoader();
                    pluginList = Collections.synchronizedList(new ArrayList<Class>());

                    /* Add StudyMakerPanel as an observer so each optimization plugin is automatically loaded into the Panel. */
                    instance.addObserver(OptimizationModel.getInstance());                    
                }
            }
        }

        return instance;
    }

    /* Load all available plugins not yet loaded, in the jar folder. */
    public synchronized void loadPlugins() {
        /* Create a class loader. */
        String eugeneDir = (String) ApplicationSettings.getInstance().getProperty("eugene_dir", String.class);
        String pluginPath = (String) ApplicationSettings.getProperty("pluginPath", String.class);
        File directory = new File(eugeneDir + pluginPath);
        ClassLoader classLoader = new PluginClassLoader(directory);

        /* If it's a valid directory, load classes in it. */
        if (directory.exists() && directory.isDirectory()) /* Read all plugins from file list. */ {
            if (directory.list().length != 0) {
                readFromFileList(classLoader, directory.list());
            }
        }

        /* Then try reading from the default plugins JAR file. */
        classLoader = new PluginClassLoader(new File(eugeneDir + pluginPath + "/"));

        String[] jarFiles = {
            "GCContentParametersPanel.class",
            "GCContentPlugin$Parameter.class",
            "GCContentPlugin.class",
            "CodonCorrelationEffectParametersPanel.class",
            "CodonCorrelationEffectPlugin$Parameter.class",
            "CodonCorrelationEffectPlugin.class",
            "SiteRemovalParametersPanel.class",
            "SiteRemovalPlugin$Parameter.class",
            "SiteRemovalPlugin.class",
            "CodonContextParametersPanel.class",
            "CodonContextPlugin$Parameter.class",
            "CodonContextPlugin.class",
            "RepeatsRemovalParametersPanel.class",
            "RepeatsRemovalPlugin$Parameter.class",
            "RepeatsRemovalPlugin.class",
            "CodonUsageParametersPanel.class",
            "CodonUsagePlugin$Parameter.class",
            "CodonUsagePlugin.class",
            "HiddenStopCodonsParametersPanel.class",
            "HiddenStopCodonsPlugin$Parameter.class",
            "HiddenStopCodonsPlugin.class",
            "UnmodifiedtRNAsParametersPanel.class",
            "UnmodifiedtRNAsPlugin$Parameter.class",
            "UnmodifiedtRNAsPlugin.class",
            "RNASecondaryStructurePanel.class",
            "RNASecondaryStructurePlugin$Parameter.class",
            "RNASecondaryStructurePlugin$1.class",
            "RNASecondaryStructurePlugin$2.class",
            "RNASecondaryStructurePlugin.class"
        };
        /* Read all plugins from JAR file list. */
        if (jarFiles.length != 0) {
            readFromFileList(classLoader, jarFiles);
        }

        doneLoading = true;
        notifyAll();
    }

    public synchronized void readFromFileList(ClassLoader classLoader, String[] fileList) {
        for (String fileName : fileList) {
            /* Ignore non-class files. */
            if (!fileName.endsWith(".class")) {
                continue;
            }

            try {
                /* Load class files, instantiate them, and add to the list of plugins. */
                Class pluginClass = classLoader.loadClass(fileName.substring(0, fileName.indexOf(".")));
                Class[] interfaces = pluginClass.getInterfaces();

                for (Class interf : interfaces) {
                    for (PluginType type : PluginType.values()) {
                        if (interf.getName().equals(type.getClassName())) {
                            pluginList.add(pluginClass);
                            System.out.println("  Loaded class " + pluginClass.getSimpleName() + " : " + interf.getSimpleName());

                            /* Notify observers. */
                            instance.setChanged();
                            instance.notifyObservers(pluginClass);
                            break;
                        }
                    }
                }
            } //TODO: excepçoes..
            catch (Exception ex) {
                System.err.println("  Error while reading plugin " + fileName + ": " + ex.getLocalizedMessage());
            }
        }
    }

    public synchronized List<Class> getPluginList(PluginType type) {
        assert pluginList != null;

        while (!doneLoading) {
            try {
                wait();
            } catch (InterruptedException ex) { //TODO: excepçoes
                Logger.getLogger(PluginLoader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        List<Class> list = Collections.synchronizedList(new ArrayList<Class>());
        for (Class c : pluginList) {
            for (Class cl : c.getInterfaces()) {
                if (type == null) {
                    list.add(c);
                } else if (cl == type.getPluginClass()) {
                    list.add(c);
                }
            }
        }

        return list;

    }

    //TODO: mudar isto para devolver classe em vez de instancia, e tornar alguns metodos dos plugins estaticos!
    public synchronized Object getPluginInstanceByName(String name) {
        for (Class pluginClass : PluginLoader.getInstance().getPluginList(null)) {
            if (pluginClass.getSimpleName().equals(name)) {
                try {
                    return pluginClass.newInstance();
                } catch (Exception ex) //TODO: excepçoes
                {
                    System.out.println("Error instantiating plugin");
                }
            } else {
                System.out.println(pluginClass.getSimpleName());
            }
        }


        return null;
    }

    @Override
    public void run() {
        System.out.println("PluginLoader started.");
        loadPlugins();
        System.out.println("PluginLoader ended.");
    }
}
