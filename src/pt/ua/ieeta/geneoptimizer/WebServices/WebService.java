package pt.ua.ieeta.geneoptimizer.WebServices;

import java.util.ArrayList;

/**
 *
 * @author David Campos
 */
public class WebService {

    private String address;
    private ArrayList<WebServiceParameter> params;
    private String protocol;

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<WebServiceParameter> getParams() {
        return params;
    }

    public void setParams(ArrayList<WebServiceParameter> params) {
        this.params = params;
    }

    public WebService() {
    }

    public WebService(String protocol, String address, ArrayList<WebServiceParameter> params) {
        this.address = address;
        this.params = params;
        this.protocol = protocol;
    }

    public Object call() throws Exception 
    {
        if (this.protocol.equals("SOAP")) 
        {
//            Service service = new Service();
//            Call call = (Call) service.createCall();
//            call.setTargetEndpointAddress(new java.net.URL(this.address));
//            call.setOperationName(new QName("http://soapinterop.org/", "blastQueryXml"));
//            call.addParameter("method", org.apache.axis.Constants.XSD_STRING, ParameterMode.IN);
//            call.addParameter("sequence", org.apache.axis.Constants.XSD_STRING, ParameterMode.IN);
//            call.addParameter("eCutOff", org.apache.axis.Constants.XSD_STRING, ParameterMode.IN);
//            call.setReturnType(org.apache.axis.Constants.XSD_STRING);
//            call.setReturnClass(String.class);
////            params.add(new WebServiceParameter("method", "blastQueryXml"));
////            params.add(new WebServiceParameter("sequence", sequence));
////            params.add(new WebServiceParameter("eCutOff", Double.toString(eCutOff)));
//            
//            String ret = (String) call.invoke(new Object[] { "blastQueryXml", "MFGKLPTY", "1E-30" });
//            return ret;
            return Soap.callWebService(this.address, this.params);
        } 
        else if (this.protocol.equals("REST")) {
            return Rest.callWebService(this.address, this.params);
        }
        throw new RuntimeException("Web Service Protocol wasn't found: " + this.protocol);
    }
}
