package pt.ua.ieeta.geneoptimizer.FileOpeningParsing;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Paulo Gaspar
 */
public class FastaHeaderParser
{
    /* Booleans to know what kind of information was extracted. */
    

    private Map<HeaderInfo, String> extractedInfo;
    private String organismGI;
    private String geneGI;
    private String organismAccession;
    private String geneStart;
    private String geneEnd;

    private static Vector<Pattern> patterns = null;
    private Matcher matcher;

    public FastaHeaderParser()
    {
        /* If not created yet, compile all patterns. */
        if (patterns == null)
        {
            System.out.println("Compiling FASTA header patterns for the first time.");
            patterns = new Vector<Pattern>();
            patterns.add(Pattern.compile("gb[|]([A-Z]+\\d+)[|][A-Z]+\\d+:\\D?(\\d+)-(\\d+).*", Pattern.DOTALL));
            patterns.add(Pattern.compile("[(]?gi[|](\\d+):\\s*\\D?(\\d+)-(\\d+).*", Pattern.DOTALL));
            patterns.add(Pattern.compile(".*[|].*[|].*[|].*[|].*[|].*[|].*[|].*[|].*", Pattern.DOTALL));
            patterns.add(Pattern.compile(".*:.*:.*:.*:.*:.*:.*:.*", Pattern.DOTALL));
            patterns.add(Pattern.compile("gi[|](\\d+)[|](gb|dbj|emb)[|]([A-Z]+\\d+)[|].*", Pattern.DOTALL));
            patterns.add(Pattern.compile("ref[|]([^|]+)[|]:\\D?(\\d+)-(\\d+)", Pattern.DOTALL));
        }
    }

    /* Parse a given FASTA header and extract available information. */
    public boolean parseHeader(String header)
    {
        System.out.println("Parsing FASTA header: <" + header + ">");

        extractedInfo = new HashMap<HeaderInfo, String>();

        /* Case 1:   gb|AE000657|AE000657:1-2100,fusA */
        if (patterns.get(0).matcher(header).matches())
        {
            matcher = patterns.get(0).matcher(header);
            matcher.find();

            addExtractedInfo(HeaderInfo.ORGANIM_ACC, matcher.group(1));
            String start = matcher.group(2);
            String end = matcher.group(3);
            if (Integer.parseInt(start) > Integer.parseInt(end))
            {
                addExtractedInfo(HeaderInfo.GENE_START_INDEX, end);
                addExtractedInfo(HeaderInfo.GENE_END_INDEX, start);
            }
            else
            {
                addExtractedInfo(HeaderInfo.GENE_START_INDEX, start);
                addExtractedInfo(HeaderInfo.GENE_END_INDEX, end);
            }

            
            return true;
        }

        /* Case 2:    gi|7276232: c4097-264 */
        if (patterns.get(1).matcher(header).matches())
        {
            matcher = patterns.get(1).matcher(header);
            matcher.find();

            addExtractedInfo(HeaderInfo.ORGANIM_GI, matcher.group(1));
            String start = matcher.group(2);
            String end = matcher.group(3);
            if (Integer.parseInt(start) > Integer.parseInt(end))
            {
                addExtractedInfo(HeaderInfo.GENE_START_INDEX, end);
                addExtractedInfo(HeaderInfo.GENE_END_INDEX, start);
            }
            else
            {
                addExtractedInfo(HeaderInfo.GENE_START_INDEX, start);
                addExtractedInfo(HeaderInfo.GENE_END_INDEX, end);
            }

            return true;
        }

        /* Case 3:    PFL1165w ||2277.t00233|hypothetical protein|Plasmodium falciparum|chr 12|STANFORD||Manual */
        if (patterns.get(2).matcher(header).matches())
        {
            System.out.println("Tipo 3: TRUE!!");
        }

        /* Case 4:    ENSMUST00000082392 cdna:known chromosome:NCBIM37:MT:2751:3707:1 gene:ENSMUSG00000064341 */
        if (patterns.get(3).matcher(header).matches())
        {
            System.out.println("Tipo 4: TRUE!!");
        }

        /* Case 5:    gi|digits|gb|accession|locus */
        if (patterns.get(4).matcher(header).matches())
        {
            matcher = patterns.get(4).matcher(header);
            matcher.find();

            addExtractedInfo(HeaderInfo.GENE_GI, matcher.group(1));
            addExtractedInfo(HeaderInfo.GENE_ACC, matcher.group(2));

            return true;
        }
        
        /* Case 6:    ref|NC_000913.2|:3734-5020 */
        if (patterns.get(5).matcher(header).matches())
        {
            matcher = patterns.get(5).matcher(header);
            matcher.find();

            addExtractedInfo(HeaderInfo.ORGANIM_ACC, matcher.group(1));
            String start = matcher.group(2);
            String end = matcher.group(3);
            if (Integer.parseInt(start) > Integer.parseInt(end))
            {
                addExtractedInfo(HeaderInfo.GENE_START_INDEX, end);
                addExtractedInfo(HeaderInfo.GENE_END_INDEX, start);
            }
            else
            {
                addExtractedInfo(HeaderInfo.GENE_START_INDEX, start);
                addExtractedInfo(HeaderInfo.GENE_END_INDEX, end);
            }

            return true;
        }

        return false;
    }

    public boolean informationExtracted()
    { return !extractedInfo.isEmpty(); }

    public boolean hasInformation(HeaderInfo infoType)
    { return extractedInfo.containsKey(infoType); }

    private void addExtractedInfo(HeaderInfo headerInformation, String info)
    { extractedInfo.put(headerInformation, info); }

    public String getInformation(HeaderInfo infoType)
    {
        assert hasInformation(infoType);

        return extractedInfo.get(infoType);
    }

    
     /* TEST */
   public static void main(String [] Args)
   {
       //   DONE:
       //gb|AE000657|AE000657:1-2100,fusA
       //gb|U00096|U00096:337-2799, thrA
       //gi|7276232: c4097-264
       //gi|7276232:4870-5481
       //(gi|6319354:606229-606239, 606628-607099)
       //PF13_0360 |||hypothetical protein|Plasmodium falciparum|chr 13|SANGER||Manual
       //PFL1165w ||2277.t00233|hypothetical protein|Plasmodium falciparum|chr 12|STANFORD||Manual
       //PFD0035c |STEVOR||STEVOR|Plasmodium falciparum|chr 4|SANGER||Manual
       //ENSMUST00000082392 cdna:known chromosome:NCBIM37:MT:2751:3707:1 gene:ENSMUSG00000064341
       //ENST00000400685 cdna:known supercontig::NT_113903:9607:12778:1 gene:ENSG00000215618
       //gi|digits|gb|accession|locus
       //gi|digits|emb|accession|locus
       //gi|digits|dbj|accession|locus
       //ref|NC_000913.2|:3734-5020

       //   TO DO:
       
        
        //gi|digits|pir||entry
        //gi|digits|prf||name
        //gi|digits|sp|accession|entry name
        //gi|digits|pdb|entry|chain
        //gi|digits|pat|country|number
        //gi|digits|bbs|number
        //gi|digits|ref|accession|
        //gnl|database|identifier
        //lcl|identifier
        FastaHeaderParser parser = new FastaHeaderParser();
        System.out.println( parser.parseHeader("ref|NC_000913.2|:c3232477-3232163") );
   }

}
