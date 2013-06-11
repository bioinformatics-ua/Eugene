package pt.ua.ieeta.geneoptimizer.WebServices;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *
 * @author David Campos
 */
public class Rest {

    public static String callWebService(String address, ArrayList<WebServiceParameter> params) throws Exception {
        return callWebService(address, params, null, 80);
    }

    public static String callWebService(String address, ArrayList<WebServiceParameter> params, String proxy, String port) throws Exception {
        return callWebService(address, params, proxy, Integer.parseInt(port));
    }

    public static String callWebService(String address, ArrayList<WebServiceParameter> params, String proxy, int port) throws Exception {

        // Get Proxy if needed
        Proxy proxyObject = null;
        if (proxy != null) {
            if ((!proxy.equals("")) && (port > 0)) {
                InetSocketAddress proxyAddress = new InetSocketAddress(proxy, port);
                proxyObject = new Proxy(Proxy.Type.HTTP, proxyAddress);
            }
        }

        String response = "";
        String query = buildWebQuery(params);
        URL url = new URL(address);

        // Make post mode connection
        URLConnection urlc = null;
        if (proxyObject == null) {
            urlc = url.openConnection();
        } else {
            urlc = url.openConnection(proxyObject);
        }
        urlc.setDoOutput(true);
        urlc.setAllowUserInteraction(false);

//        System.out.println(urlc.getOutputStream());

        // Send query
        PrintStream ps = new PrintStream(urlc.getOutputStream());
        ps.print(query);
        ps.close();

        // Retrieve result
        BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\n");
        }
        br.close();
        response = sb.toString();

        return response;
    }

    /**
     * Convert the input Parameters into a Web Query
     * @param params Web Service Parameters
     * @return String Web Query with all parameters
     * @throws java.lang.Exception
     */
    private static String buildWebQuery(ArrayList<WebServiceParameter> params) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (params.size() != 0) {
            for (int i = 0; i < params.size(); i++) {
                String key = URLEncoder.encode(params.get(i).getName(), "UTF-8");
                String value = URLEncoder.encode(params.get(i).getValue(), "UTF-8");
                sb.append(key).append("=").append(value).append("&");
            }
            return sb.toString().substring(0, sb.length() - 1);
        } else {
            return sb.toString();
        }
    }
}
