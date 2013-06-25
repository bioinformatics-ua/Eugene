package pt.ua.ieeta.geneoptimizer.ExternalTools;

import java.io.*;

/**
 *
 * @author Paulo Gaspar
 * 
 * Stream consumer. This is used when calling external applications (or commands)
 * to read its output and write it to some given printStream, thus avoiding the
 * application to remain stuck waiting for its output to be read.
 */
public class StreamConsumer extends Thread
{
    private InputStream is;
    private PrintStream os;

    public StreamConsumer(InputStream is, PrintStream os)
    {
        assert is != null;
        
        this.is = is;
        this.os = os;
    }

    @Override
    public void run()
    {
        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null)
                if (os != null) os.println(line);
            
            os.flush();
        } 
        catch (IOException ex)
        {
            System.out.println("Exception while consuming input stream: " + ex.getMessage());
        }
    }
}
