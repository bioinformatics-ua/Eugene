package pt.ua.ieeta.geneoptimizer.WebServices;

import java.util.ArrayList;
import org.apache.axis.Constants;
import org.apache.axis.utils.XMLUtils;
import org.apache.axis.encoding.ser.SimpleDeserializer;
import org.apache.axis.encoding.ser.ElementSerializerFactory;
import org.apache.axis.encoding.ser.ElementDeserializerFactory;
import org.apache.axis.encoding.ser.ElementDeserializer;
import org.apache.axis.wsdl.gen.Parser;
import org.apache.axis.wsdl.symbolTable.BaseType;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.axis.wsdl.symbolTable.ServiceEntry;
import org.apache.axis.wsdl.symbolTable.SymTabEntry;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.w3c.dom.Element;
import javax.wsdl.Binding;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.encoding.Deserializer;
import javax.xml.rpc.encoding.DeserializerFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author David Campos
 */
public class Soap {
    // Parser to validate the WSDL URI

    private static Parser wsdlParser = null;

    /**
     * Call a given WSDL Web Service address with specific parameters
     * @param address WSDL URI
     * @param params Web Service parameters, including method name
     * @return Web Service returned parameter
     * 
     * @throws java.lang.Exception
     */
    public static Object callWebService(String address, ArrayList<WebServiceParameter> params) throws Exception {
        Object result = null;

        try {
            // Method Name is the 1st parameter
            String methodName = params.get(0).getValue();

            wsdlParser = new Parser();
            wsdlParser.run(address);

            // Invoke Web Service Method
            HashMap map = invokeMethod(methodName, params);
            
            // Get Web Service Result
            Iterator it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = (Map.Entry) it.next();
                //String key = (String) entry.getKey();
                Object value = entry.getValue();

                // Verify if the result is XML
                if (value instanceof Element) {
                    result = XMLUtils.ElementToString((Element) value);
                } else {
                    result = value;
                }
            }
        } catch (Exception ex) {
//            ex.printStackTrace();
            throw new RuntimeException("Error calling the Web Service on the address \""+address+"\"");
        }
        return result;
    }

    /**
     * Invoke a method of an SOAP Web Service
     * @param operationName 
     * @param portName
     * @param params
     * @return
     * @throws java.lang.Exception
     */
    private static HashMap invokeMethod(String methodName, ArrayList<WebServiceParameter> params) throws Exception {
        String serviceNS = null;
        String serviceName = null;
        String portName = null;

        //System.out.println("Preparing Axis dynamic invocation");
        Service service = selectService(serviceNS, serviceName);
        Operation operation = null;
        org.apache.axis.client.Service dpf = new org.apache.axis.client.Service(wsdlParser, service.getQName());


        // Get Web Service Port
        Port port = selectPort(service.getPorts(), portName);
        if (portName == null) {
            portName = port.getName();
        }
        Binding binding = port.getBinding();

        // Create Call
        Call call = dpf.createCall(QName.valueOf(portName),QName.valueOf(methodName));
        ((org.apache.axis.client.Call) call).setTimeout(new Integer(15 * 1000));
        ((org.apache.axis.client.Call) call).setProperty(ElementDeserializer.DESERIALIZE_CURRENT_ELEMENT, Boolean.TRUE);

        // Output types and names
        List<String> outNames = new ArrayList<String>();
        // Input types and names
        List<String> inNames = new ArrayList<String>();
        List<Parameter> inTypes = new ArrayList<Parameter>();

        SymbolTable symbolTable = wsdlParser.getSymbolTable();
        BindingEntry bEntry = symbolTable.getBindingEntry(binding.getQName());
        Parameters parameters = null;
        Iterator i = bEntry.getParameters().keySet().iterator();

        // Get Method to Call
        while (i.hasNext()) {
            Operation o = (Operation) i.next();
            if (o.getName().equals(methodName)) {
                operation = o;
                parameters = (Parameters) bEntry.getParameters().get(o);
                break;
            }
        }
        // If the method wasn't found, throw Exception
        if ((operation == null) || (parameters == null)) {
            throw new RuntimeException(methodName + " was not found.");
        }

        // Loop over Parameters and set up in/out params
        for (int j = 0; j < parameters.list.size(); ++j) {
            Parameter p = (Parameter) parameters.list.get(j);

            if (p.getMode() == 1) {           // IN
                inNames.add(p.getQName().getLocalPart());
                inTypes.add(p);
            } else if (p.getMode() == 2) {    // OUT
                outNames.add(p.getQName().getLocalPart());
            } else if (p.getMode() == 3) {    // INOUT
                inNames.add(p.getQName().getLocalPart());
                inTypes.add(p);
                outNames.add(p.getQName().getLocalPart());
            }
        }

        // Set output type
        if (parameters.returnParam != null) {
            if (!parameters.returnParam.getType().isBaseType()) {
                ((org.apache.axis.client.Call) call).registerTypeMapping(org.w3c.dom.Element.class,
                                                                         parameters.returnParam.getType().getQName(),
                                                                         new ElementSerializerFactory(),
                                                                         new ElementDeserializerFactory());
            }
            // Get the QName for the return Type
            QName returnType = org.apache.axis.wsdl.toJava.Utils.getXSIType(parameters.returnParam);
            QName returnQName = parameters.returnParam.getQName();
            outNames.add(returnQName.getLocalPart());
        }

        // Check if the number of the input parameter is correct
        if (inNames.size() != params.size() - 1) {
            throw new RuntimeException("Needs " + inNames.size() + " arguments.");
        }

        // Set input arguments
        List<Object> inputs = new ArrayList<Object>();
        for (int j = 0; j < inNames.size(); j++) {

            String arg = (String) params.get(j + 1).getValue();
            Parameter p = (Parameter) inTypes.get(j);
            inputs.add(getParamData((org.apache.axis.client.Call) call, p, arg));
        }

        // Call the Method and Return the Results on a Map
        Object ret = call.invoke(inputs.toArray());
        Map outputs = call.getOutputParams();
        HashMap<String, Object> map = new HashMap<String, Object>();

        for (int j = 0; j < outNames.size(); j++) {
            String name = (String) outNames.get(j);
            Object value = outputs.get(name);

            if ((value == null) && (j == 0)) {
                map.put(name, ret);
            } else {
                map.put(name, value);
            }
        }
        return map;
    }

    /**
     * Get parameter Data
     * @param c Web Service Call
     * @param p Parameter to get Data
     * @param arg Argument Name
     * @return Data of the Parameter
     * @throws java.lang.Exception
     */
    private static Object getParamData(org.apache.axis.client.Call c, Parameter p, String arg) throws Exception {
        // Get the QName representing the parameter type
        QName paramType = org.apache.axis.wsdl.toJava.Utils.getXSIType(p);

        TypeEntry type = p.getType();
        if (type instanceof BaseType && ((BaseType) type).isBaseType()) {
            DeserializerFactory factory = c.getTypeMapping().getDeserializer(paramType);
            Deserializer deserializer = factory.getDeserializerAs(Constants.AXIS_SAX);
            if (deserializer instanceof SimpleDeserializer) {
                return ((SimpleDeserializer) deserializer).makeValue(arg);
            }
        }
        throw new RuntimeException("Wasn't possible to convert '" + arg + "' into " + c);
    }

    
    /**
     * Select Service to invoke
     * @param serviceNS 
     * @param serviceName Name of the Service
     * @return Selected Service
     * @throws java.lang.Exception
     */
    private static Service selectService(String serviceNS, String serviceName) throws Exception {
        QName serviceQName = (((serviceNS != null) && (serviceName != null))
                ? new QName(serviceNS, serviceName)
                : null);
        ServiceEntry serviceEntry = (ServiceEntry) getSymTabEntry(serviceQName, ServiceEntry.class);
        return serviceEntry.getService();
    }

    /**
     *
     * @param qname
     * @param cls
     * @return
     */
    private static SymTabEntry getSymTabEntry(QName qname, Class cls) {
        HashMap map = wsdlParser.getSymbolTable().getHashMap();

        Iterator iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            QName key = (QName) entry.getKey();
            List v = (List) entry.getValue();

            if ((qname == null) || qname.equals(key)) {
                for (int i = 0; i < v.size(); ++i) {
                    SymTabEntry symTabEntry = (SymTabEntry) v.get(i);
                    if (cls.isInstance(symTabEntry)) {
                        return symTabEntry;
                    }
                }
            }
        }
        return null;
    }


    /**
     * Select Web Service Port
     * @param ports 
     * @param portName
     * @return Port to Bind
     * @throws java.lang.Exception
     */
    private static Port selectPort(Map ports, String portName) throws Exception {
        Iterator valueIterator = ports.entrySet().iterator();
        while (valueIterator.hasNext()) {
            Map.Entry portEntry = (Map.Entry) valueIterator.next();

            if ((portName == null) || (portName.length() == 0)) {
                Port port = (Port) portEntry.getValue();
                List list = port.getExtensibilityElements();

                for (int i = 0; (list != null) && (i < list.size()); i++) {
                    Object obj = list.get(i);
                    if (obj instanceof SOAPAddress) {
                        return port;
                    }
                }
            } else if ((portEntry.getKey() != null) && portEntry.getKey().equals(portName)) {
                return (Port) portEntry.getValue();
            }
        }
        return null;
    }
}
