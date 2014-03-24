package pt.ua.ieeta.geneoptimizer.ExternalTools;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.ieeta.geneoptimizer.FileHandling.HeaderInfo;

/**
 *
 * @author Paulo Gaspar
 */
public class NCBIwebFetcher implements Runnable {

    private static String ncbiBaseLink = "http://www.ncbi.nlm.nih.gov/sviewer/viewer.fcgi?";
    private static String[] parameters = {"val=", "&db=", "&from=", "&to=", "&retmode=", "&maxdownloadsize="};

    private StringBuilder requestBuild;
    private ResultKeeper resultKeeper;

    /* Parameter values */
    private String database = "nuccore";
    private String from = "1";
    private String to = "1";
    private String returnMode = "text";
    private String maxDownloadSize = "1000000";
    private String value = "";
    private boolean onlyGene = false;

    /* Fetched information. */
    private Map<HeaderInfo, String> fetchedInformation;
    private String sequenceName = null;
    private String sequenceRNA = null;
    
    public NCBIwebFetcher(ResultKeeper resultKeeper) {
        this(resultKeeper, false);
    }
    
    public NCBIwebFetcher(ResultKeeper resultKeeper, boolean onlyGene) {
        this.onlyGene = onlyGene;
        this.resultKeeper = resultKeeper;
        this.fetchedInformation = new EnumMap<>(HeaderInfo.class);
    }
    
    public void setValue(String value) {
        this.value = value;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setReturnMode(String returnMode) {
        this.returnMode = returnMode;
    }

    public void setMaxDownloadSize(String maxDownloadSize) {
        this.maxDownloadSize = maxDownloadSize;
    }
    
    public void setOnlyGene(boolean onlyGene) {
        this.onlyGene = onlyGene;
    }
    
    private void fetchInfo() {
        System.out.println("Started fetching information from NCBI web pages.");
        
        if(!onlyGene) {
            /* Fetch all information regarding the organism. */
            fetchInfoFromOrganism();
        } else {
            fetchInfoFromGene();
        }
        
        if (fetchedInformation.isEmpty() && (sequenceName == null || sequenceName.equals("NOT_FOUND"))) {
            /* Dummy result, just to allow the calling class to come and fetch the results. */
            resultKeeper.setResult(false);
        } else {
            resultKeeper.setResult(true);
        }
    }

    private void fetchInfoFromGene() {
        requestBuild = new StringBuilder();
        
        /* Add base link to request */
        requestBuild.append(ncbiBaseLink);
        
        /* Add parameters to request*/
        requestBuild.append(parameters[0]).append(value);
        requestBuild.append(parameters[1]).append(database);
        requestBuild.append(parameters[4]).append(returnMode);
        
        System.out.println("FETCHING FROM: " + requestBuild.toString());
        
        URL url = null;
        try {
            url = new URL(requestBuild.toString());
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            InputStream is = connection.getInputStream();
            BufferedReader buffReader = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));
            
            String line, name = null;
            boolean cdsFound = false, originFound = false, nameFound = false;
            int i, cdsBegin = 0, cdsEnd = 0;
            
            while ((line = buffReader.readLine()) != null) {
                if(name == null) {
                    if(line.indexOf("gene") != 0) {
                        line = buffReader.readLine();
                        if(line.contains("/gene=\"")) {
                            name = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")) + " (manually added)";
                            nameFound = true;
                        }
                    }
                }
                if (cdsBegin == 0 && cdsEnd == 0) {
                    if ((i = line.lastIndexOf("CDS")) != -1) {
                        for (; i < line.length(); ++i) {
                            if (line.charAt(i) >= '0' && line.charAt(i) <= '9') {
                                cdsBegin = Integer.parseInt(line.substring(i, line.indexOf(".")));
                                cdsEnd = Integer.parseInt(line.substring(line.lastIndexOf(".") + 1));
                                cdsFound = true;
                                break;
                            }
                        }
                    }
                } else if(line.contains("ORIGIN")) {
                    originFound = true;
                    break;
                }
            }
            
            if(nameFound && cdsFound && originFound) {
                StringBuilder geneBuffer = new StringBuilder();
                while((line = buffReader.readLine()) != null) {
                    line = line.trim();
                    String[] temp = line.split(" ");
                    for(int j = 1; j < temp.length; j++) {
                        geneBuffer.append(temp[j]);
                    }
                    if(geneBuffer.length() > cdsEnd) {
                        break;
                    }
                }
                
                sequenceName = name;
                sequenceRNA = geneBuffer.substring(cdsBegin-1, cdsEnd);
                System.out.println("Ended fetching gene");
                return;
            }
        } catch(MalformedURLException ex) {
            Logger.getLogger(NCBIwebFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch(SocketTimeoutException ex) {
            Logger.getLogger(NCBIwebFetcher.class.getName()).log(Level.SEVERE, null, ex);
        } catch(IOException ex) {
            Logger.getLogger(NCBIwebFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        notFound();
    }

    private void notFound() {
        sequenceName = "NOT_FOUND";
        sequenceRNA = "NOT_FOUND";
    }
    
    private void fetchInfoFromOrganism() {
        if ((!to.matches("\\D\\d+")) && (!from.matches("\\D\\d+"))) {
            assert Integer.parseInt(to) >= Integer.parseInt(from);
        }
        assert !value.isEmpty();
        assert !database.isEmpty();
        assert Integer.parseInt(maxDownloadSize) <= 1000000;

        requestBuild = new StringBuilder();

        /* Add base link to request. */
        requestBuild.append(ncbiBaseLink);

        /* Add parameters to request. */
        requestBuild.append(parameters[0]).append(value);
        requestBuild.append(parameters[1]).append(database);
        requestBuild.append(parameters[2]).append(from);
        requestBuild.append(parameters[3]).append(to);
        requestBuild.append(parameters[4]).append(returnMode);
        requestBuild.append(parameters[5]).append(maxDownloadSize);

        System.out.println("FETCHING FROM: " + requestBuild.toString());

        URL url = null;
        try {
            url = new URL(requestBuild.toString());
        } catch (MalformedURLException ex) {
            Logger.getLogger(NCBIwebFetcher.class.getName()).log(Level.SEVERE, null, ex);
        }
        String line;

        try (InputStream is = url.openStream();
                BufferedReader dis = new BufferedReader(new InputStreamReader(new BufferedInputStream(is)));) {

            while ((line = dis.readLine()) != null) {
                if (line.contains("VERSION ")) {
                    String newString = line.replace("VERSION", "").trim();
                    fetchedInformation.put(HeaderInfo.ORGANIM_ACC, newString.substring(0, newString.indexOf(" ")).trim());
                    fetchedInformation.put(HeaderInfo.ORGANIM_GI, newString.substring(newString.indexOf("GI:") + 3, newString.length()).trim());
                }

                if ((line.contains(" ORGANISM ")) && !hasInformation(HeaderInfo.ORGANISM_NAME)) {
                    String organism = line.replace("ORGANISM", "").trim();
                    fetchedInformation.put(HeaderInfo.ORGANISM_NAME, organism);
                    String names[] = organism.split("\\s");
                    String keggOrgID = names[0].toLowerCase().charAt(0) + names[1].toLowerCase().substring(0, 2);
                    fetchedInformation.put(HeaderInfo.KEGG_ORGANISM, keggOrgID);
                }

                if (line.contains(" /product=\"") && (!hasInformation(HeaderInfo.PROTEIN_NAME)
                        || (fetchedInformation.get(HeaderInfo.PROTEIN_NAME).contains("putative protein"))
                        || (fetchedInformation.get(HeaderInfo.PROTEIN_NAME).contains("hypothetical protein")))) {
                    /* Check if name is split into several lines. If it is, read all lines and join them.*/
                    StringBuilder sb = new StringBuilder(line);
                    if (line.indexOf("\"") == line.lastIndexOf("\"")) {
                        while (!(line = dis.readLine()).contains("\"")) {
                            sb.append(line);
                        }
                        sb.append(line);
                    }
                    fetchedInformation.put(HeaderInfo.PROTEIN_NAME, sb.toString().replaceAll("\\s+", " ").replaceAll("[^\"]+\"([^\"]+)\"", "$1"));
                }

                if ((line.contains(" /db_xref=\"GI:")) && !hasInformation(HeaderInfo.GENE_GI)) {
                    fetchedInformation.put(HeaderInfo.GENE_GI, line.replaceAll("[^\"]+\"GI:([^\"]+)\"", "$1"));
                }

                if ((line.contains(" /gene=\"")) && !hasInformation(HeaderInfo.GENE_NAME)) {
                    fetchedInformation.put(HeaderInfo.GENE_NAME, line.replaceAll("[^\"]+\"([^\"]+)\"", "$1"));
                }

                if ((line.contains(" /protein_id=\"")) && !hasInformation(HeaderInfo.PROTEIN_ACC)) {
                    fetchedInformation.put(HeaderInfo.PROTEIN_ACC, line.replaceAll("[^\"]+\"([^\"]+)\"", "$1"));
                }

                if ((line.contains(" /locus_tag=\"")) && !hasInformation(HeaderInfo.LOCUS_TAG)) {
                    fetchedInformation.put(HeaderInfo.LOCUS_TAG, line.replaceAll("[^\"]+\"([^\"]+)\"", "$1"));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        } //TODO: exceptions..

//       fetchedInformation.keySet().toArray().toString();
    }

    public String getFetchedInformation(HeaderInfo info) {
        assert fetchedInformation.containsKey(info);

        return fetchedInformation.get(info);
    }

    public void setFetchedInformation(HeaderInfo infotype, String info) {
        assert infotype != null;
        assert info != null;

        fetchedInformation.put(infotype, info);
    }
    
    public String getFetchedRNASequence() {
        return sequenceRNA;
    }
    
    public String getFetchedName() {
        return sequenceName;
    }
    
    public String getValue() {
        return value;
    }
    
    public boolean hasInformation(HeaderInfo info) {
        return fetchedInformation.containsKey(info);
    }

    public synchronized void run() {
        fetchInfo();
    }

    /* TEST */
    public static void main(String[] Args) {
        NCBIwebFetcher parser = new NCBIwebFetcher(new ResultKeeper(), true);
        parser.setValue("NM_031418");
        parser.fetchInfo();
    }
}