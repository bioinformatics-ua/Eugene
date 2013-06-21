package pt.ua.ieeta.geneoptimizer.WebServices;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo Gaspar
 */
public class WSRunner 
{

    
    public static void main(String [] Args) throws Exception
    {
        pause(80);
        callWithParams("100000205484262", "1", "190666", "102087", "10703", "100", "0");
        pause(80);
        callWithParams("100000205484262", "2", "190666", "112208", "11106", "82", "0");
        pause(80);
        callWithParams("100000205484262", "3", "190666", "96028", "11102", "88", "0");
        pause(80);
        callWithParams("100000205484262", "4", "190666", "105243", "39423", "52", "0");
        pause(80);
        callWithParams("100000205484262", "5", "190666", "512028", "65485", "59", "1");
    }
    
    private static void callWithParams(String id_fb, String level, String id_game, String score, String distance, String timestamp, String etat)
    {
        WebService ws1;
        
        ws1 = new WebService();
        ws1.setAddress("https://facebook.mrmworldwide.fr/neonfu/intl/launch/webservice/index.php?wsdl");
        ws1.setProtocol("SOAP");
        
        ArrayList<WebServiceParameter> params = new ArrayList<WebServiceParameter>();
        params.add(new WebServiceParameter("method", "set_score_level"));
        params.add(new WebServiceParameter("id_fb", id_fb));
        params.add(new WebServiceParameter("level", level));
        params.add(new WebServiceParameter("id_game", id_game));
        params.add(new WebServiceParameter("score", score));
        params.add(new WebServiceParameter("distance", distance));
        params.add(new WebServiceParameter("timestamp", timestamp));
        params.add(new WebServiceParameter("etat", etat));
        
        ws1.setParams(params);

        String result = null;
        try
        {
            result = (String) ws1.call();
        } catch (Exception ex)
        {
            Logger.getLogger(WSRunner.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (result == null)
        {
            System.out.println("Null result! :(");
            return;
        }
        
        System.out.println("Result:");
        System.out.println(result);
        System.out.println("Done!");
        System.out.println("");
        System.out.println("");
    }
    
    public static void pause(int seconds)
    {
         Object bra = new Object();
        
         synchronized (bra)
         {
            try
            {
                bra.wait(seconds * 1000);
            } catch (InterruptedException ex)
            {
                Logger.getLogger(WSRunner.class.getName()).log(Level.SEVERE, null, ex);
            }
         }
    }
    
}
