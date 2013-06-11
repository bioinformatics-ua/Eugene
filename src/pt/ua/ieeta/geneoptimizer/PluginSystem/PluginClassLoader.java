package pt.ua.ieeta.geneoptimizer.PluginSystem;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class PluginClassLoader extends ClassLoader 
{
    /* Directory to load the files from. */
    private File dir;

    public PluginClassLoader(File dir) {
        this.dir = dir;
    }

    @Override
    public Class loadClass(String classname) throws ClassNotFoundException 
    {
        // Verify if system class loader can load the class
        try 
        {
            Class c = Thread.currentThread().getContextClassLoader().loadClass(classname);
            return c;
        } 
        catch (ClassNotFoundException cnf) 
        {
            //TODO: hmmmm.. what to do?
        }

        /* Look for an already loaded class with the same name. */
        Class c = findLoadedClass(classname);

        /* Look for a system class with the same name. */
        if (c == null) {
            try {
                c = findSystemClass(classname);
            } catch (Exception ex) { /* Not a system class. Everything alright. */ }
        }
        try {
            if (c == null) {
                /* Get file. */
                String filename = classname.replace('.', File.separatorChar) + ".class";
                File f = new File(dir, filename);
                DataInputStream in = null;
                byte[] classbytes;
                int length;

                /*********** EXPERIMENTAL **********/
                if (!f.exists()) {
                    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("Plugins/" + classname + ".class");
                    if(stream == null)
                        return null;
                    in = new DataInputStream(stream);
                    if (in == null) return null;
                    length = in.available();
                    classbytes = new byte[in.available()];
                } /**********************************/
                else {
                    /* Load entire file into memory. */
                    length = (int) f.length();
                    classbytes = new byte[length];
                    in = new DataInputStream(new FileInputStream(f));
                }

                in.readFully(classbytes);
                in.close();

                /* Make the class. */
//                c = defineClass(classname, classbytes, 0, length);
                c = defineClass(null, classbytes, 0, length);
            }
        } //TODO: excepçoes...
        catch (Exception ex) {
            System.out.println("  Error reading plugin file.");
            ex.printStackTrace();
            return null;
        }

        /* Make class linkage. */
        resolveClass(c);

        return c;
    }
}
