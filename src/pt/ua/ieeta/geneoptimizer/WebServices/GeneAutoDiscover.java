package pt.ua.ieeta.geneoptimizer.WebServices;

import java.util.logging.Level;
import java.util.logging.Logger;
import pt.ua.ieeta.geneoptimizer.ExternalTools.Muscle;
import pt.ua.ieeta.geneoptimizer.ExternalTools.NCBIwebFetcher;
import pt.ua.ieeta.geneoptimizer.ExternalTools.ResultKeeper;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.FastaHeaderParser;
import pt.ua.ieeta.geneoptimizer.FileOpeningParsing.HeaderInfo;
import pt.ua.ieeta.geneoptimizer.GUI.GeneInformationPanel;
import pt.ua.ieeta.geneoptimizer.GUI.GenePanel.SequencePaintingPool;
import pt.ua.ieeta.geneoptimizer.GUI.Protein3DViewerPanel;
import pt.ua.ieeta.geneoptimizer.Main.ApplicationSettings;
import pt.ua.ieeta.geneoptimizer.Main.ProjectManager;
import pt.ua.ieeta.geneoptimizer.GeneRedesign.Study;
import pt.ua.ieeta.geneoptimizer.geneDB.BioStructure;
import pt.ua.ieeta.geneoptimizer.geneDB.Gene;
import pt.ua.ieeta.geneoptimizer.geneDB.Genome;

/**
 *
 * @author Paulo Gaspar
 */
public class GeneAutoDiscover extends Thread {

    private Study study;
    private NCBIwebFetcher ncbiParser;

    public GeneAutoDiscover(Study study) {
        this.study = study;
    }

    public GeneAutoDiscover() {
    }

    @Override
    public void run() {
        boolean autoDiscoveryEnabled = (Boolean) ApplicationSettings.getProperty("geneAutoDiscoveryEnabled", Boolean.class);
        if (!autoDiscoveryEnabled) {
            return;
        }

        /* Original gene. */
        final Gene gene = study.getResultingGene();

        /* Start fetching informatio from PDB. */
        String aaSequence;
        final ResultKeeper PDBBlastResultKeeper = new ResultKeeper();
        if (gene.hasSequenceOfType(BioStructure.Type.proteinPrimaryStructure)) {
            if (gene.getPDBCode() == null) {
                aaSequence = gene.getAminoacidSequence();
                PDBBlastWS pdbBlast = new PDBBlastWS(aaSequence, 1E-30, PDBBlastResultKeeper);
                pdbBlast.start();
            } else {
                PDBBlastResultKeeper.setResult(gene.getPDBCode());
            }

            /* Throw a new thread to wait for the PDB response and deal with it. */
            new Thread(new Runnable() {
                public void run() {
                    String PDBcode = (String) PDBBlastResultKeeper.getResult();
                    /* Start fetching information from PDB. */
                    if (!PDBBlastResultKeeper.isFail()) {
                        System.out.println("PDBCode: " + PDBcode);

                        gene.setPDBCode(PDBcode);
                        updatePanels();
                    } else {
                        System.out.println("Erro ao obter ortologos do blast PDB.");
                    }
                }
            }).start();
        }

        boolean gotOrthologsFromKegg = false;

        if (gene.hasGeneNameConfirmation() && gene.getGenome().hasNameConfirmation()) {
            ncbiParser = new NCBIwebFetcher(new ResultKeeper());
            ncbiParser.setFetchedInformation(HeaderInfo.GENE_NAME, gene.getName());
            ncbiParser.setFetchedInformation(HeaderInfo.ORGANISM_NAME, gene.getGenome().getName());

            /* Calculate KEGG_ORGANISM. */
            String names[] = gene.getGenome().getName().trim().split("\\s");
            if (names.length >= 2) {
                if (names[1].length() >= 2) {
                    String keggOrgID = names[0].toLowerCase().charAt(0) + names[1].toLowerCase().substring(0, 2);
                    ncbiParser.setFetchedInformation(HeaderInfo.KEGG_ORGANISM, keggOrgID);
                }
            }
        }

        /* Try to parse original header first. */
        if (parseHeader(study.getResultingGene().getGeneHeader()) == null) {
            System.out.println("Failed to get any information through parsing the headers.");
        }

        /* If parsing was successfull, try to find kegg orthologs (if there is enough information. */
        if (!gene.hasOrthologs() && (ncbiParser != null)) {
            setGeneInformationFromNCBIWebParser(study);

            if (ncbiParser.hasInformation(HeaderInfo.KEGG_ORGANISM) && (ncbiParser.hasInformation(HeaderInfo.LOCUS_TAG) || ncbiParser.hasInformation(HeaderInfo.GENE_NAME))) {
                String keggOrg = ncbiParser.getFetchedInformation(HeaderInfo.KEGG_ORGANISM);
                String geneLocusTag = ncbiParser.hasInformation(HeaderInfo.LOCUS_TAG)
                        ? ncbiParser.getFetchedInformation(HeaderInfo.LOCUS_TAG)
                        : ncbiParser.getFetchedInformation(HeaderInfo.GENE_NAME);

                ResultKeeper KeggResultKeeper = new ResultKeeper();
//                KEGGorthoWS keggService = new KEGGorthoWS(keggOrg + ":" + geneLocusTag, KeggResultKeeper);
                KEGGOrthoRestWS keggService = new KEGGOrthoRestWS(keggOrg + ":" + geneLocusTag, KeggResultKeeper);
                keggService.start();


                /* Wait for result. */
                KeggResultKeeper.getResult();
                if (!KeggResultKeeper.isFail()) {

                    if (!study.getResultingGene().hasOrthologs()) {
                        study.getOriginalGene().setOrthologList((Genome) KeggResultKeeper.getResult());
                    }

                    Genome orthologs = study.getResultingGene().hasOrthologs() ? study.getResultingGene().getOrthologList() : study.getOriginalGene().getOrthologList();
                    orthologs.addGene(study.getResultingGene());
                    study.getResultingGene().setOrthologInfo(0, 0, "randomID", "");
                    ResultKeeper alignmentResult = new ResultKeeper();
                    Muscle muscleTool = new Muscle(orthologs.getGenes(), alignmentResult);
                    new Thread(muscleTool).start();


                    
                    alignmentResult.getResult();
                    orthologs.removeGene(study.getResultingGene());
                    /* Get aligned orthologs. */
                    orthologs.setOrthologsAligned(true);

                    gotOrthologsFromKegg = true;
                }
            }
        } else if (ncbiParser != null) {
            setGeneInformationFromNCBIWebParser(study);
            gotOrthologsFromKegg = true;
        }

        ResultKeeper NCBIBlastResultKeeper = new ResultKeeper();

        /* Start NCBI blast to find orthologs and information about the gene. */
        NCBIBlastWS ncbiBlast;
        if (!gotOrthologsFromKegg) {
            ncbiBlast = new NCBIBlastWS(gene, 1E-10, 10, NCBIBlastResultKeeper);
            ncbiBlast.start();
        }


        /* Get ortholog list from NCBI. */
        Genome orthologs = null;
        if (!gotOrthologsFromKegg) {
            orthologs = (Genome) NCBIBlastResultKeeper.getResult();
        }

        /* No result, or problem occured. */
        if (!gotOrthologsFromKegg) {
            if (NCBIBlastResultKeeper.isFail()) {
                System.out.println("Erro ao obter ortologos do blast NCBI."); //TODO: dar erro para algum lado!
            } else {
                /* Find out what ortholog had the maximum score. */
                int maxScore = 0;
                Gene maxScoreOrtholog = null;
                for (Gene ortholog : orthologs.getGenes()) {
                    if ((ortholog.getScore() > maxScore) && (ortholog.getIdentity() >= (Double) ApplicationSettings.getProperty("AutoDiscoverScoreThreshold", Double.class))) {
                        maxScore = ortholog.getScore();
                        maxScoreOrtholog = ortholog;
                    }
                }

                int NCBIMatchThreshold = (Integer) ApplicationSettings.getProperty("NCBIMatchThreshold", Integer.class);

                if (maxScoreOrtholog != null) /* Remove it from list, since it isn't an ortholog, it is the gene itelf. */ {
                    if ((maxScoreOrtholog.getIdentity() >= NCBIMatchThreshold) && (Math.abs(maxScoreOrtholog.getSequenceLength() - study.getResultingGene().getSequenceLength()) <= 1)) {
                        orthologs.getGenes().remove(maxScoreOrtholog);

                        if (ncbiParser != null) {
                            if (!ncbiParser.hasInformation(HeaderInfo.PROTEIN_NAME) && !ncbiParser.hasInformation(HeaderInfo.GENE_NAME)) {
                                gene.setGeneName(maxScoreOrtholog.getName().trim());
                                study.getCurrentPanel().setTitle("[" + maxScoreOrtholog.getGenomeName().trim() + "]   " + maxScoreOrtholog.getName().trim());
                            }

                            if (!ncbiParser.hasInformation(HeaderInfo.ORGANISM_NAME)) {
                                gene.getGenome().setSpecieName(maxScoreOrtholog.getGenomeName().trim());
                            }
                        } else {
                            gene.setGeneName(maxScoreOrtholog.getName().trim());
                            study.getCurrentPanel().setTitle("[" + maxScoreOrtholog.getGenomeName().trim() + "]   " + maxScoreOrtholog.getName().trim());
                            gene.getGenome().setSpecieName(maxScoreOrtholog.getGenomeName().trim());
                        }



                        System.out.println("Found a match for NCBI blast.");
                    } else {
                        System.out.println("No match found for NCBI blast.");
                    }
                }


                gene.setOrthologList(orthologs);

                updatePanels();
            }
        }
    }

    /* Parse FASTA header and try to use NCBI to fetch organism and gene information. */
    public synchronized NCBIwebFetcher parseHeader(String originalHeader) {
        /* Try parsing the FASTA header and extracting information. */
        FastaHeaderParser hp = new FastaHeaderParser();
        if (!hp.parseHeader(originalHeader)) {
            return null;
        }

        ResultKeeper result = new ResultKeeper();
        ncbiParser = new NCBIwebFetcher(result);

        /* Add information extracted from header to the web fetcher. */
        if (hp.hasInformation(HeaderInfo.GENE_GI)) {
            ncbiParser.setValue(hp.getInformation(HeaderInfo.GENE_GI));
        } else if (hp.hasInformation(HeaderInfo.GENE_ACC)) {
            ncbiParser.setValue(hp.getInformation(HeaderInfo.GENE_ACC));
        } else if ((hp.hasInformation(HeaderInfo.ORGANIM_ACC)) || (hp.hasInformation(HeaderInfo.ORGANIM_GI))) {
            if (hp.hasInformation(HeaderInfo.ORGANIM_ACC)) {
                ncbiParser.setValue(hp.getInformation(HeaderInfo.ORGANIM_ACC));
            } else {
                ncbiParser.setValue(hp.getInformation(HeaderInfo.ORGANIM_GI));
            }
            if (hp.hasInformation(HeaderInfo.GENE_END_INDEX)) {
                ncbiParser.setFrom(hp.getInformation(HeaderInfo.GENE_START_INDEX));
                ncbiParser.setTo(hp.getInformation(HeaderInfo.GENE_END_INDEX));
            }
        }

        /* Start fetching information from the NCBI page. */
        new Thread(ncbiParser).start();

        /* Wait for the result. */
        if ((Boolean) result.getResult()) {
            return ncbiParser;
        } else {
            return null;
        }
    }

    private void setGeneInformationFromNCBIWebParser(Study study) {
        if (ncbiParser.hasInformation(HeaderInfo.PROTEIN_NAME)) {
            study.getResultingGene().setProductName(ncbiParser.getFetchedInformation(HeaderInfo.PROTEIN_NAME));
            study.getResultingGene().setHasProductNameConfirmation(true);
        }

        if (ncbiParser.hasInformation(HeaderInfo.GENE_NAME)) {
            study.getResultingGene().setGeneName(ncbiParser.getFetchedInformation(HeaderInfo.GENE_NAME));
            study.getResultingGene().setHasNameConfirmation(true);
        }

        String geneName = study.getResultingGene().getName();

        if (ncbiParser.hasInformation(HeaderInfo.ORGANISM_NAME)) {
            String organismName = ncbiParser.getFetchedInformation(HeaderInfo.ORGANISM_NAME);
            study.getResultingGene().getGenome().setSpecieName(organismName);
            study.getResultingGene().getGenome().setHasNameConfirmation(true);

            /* In case the web fetch is too fast and the panel is still not created. */
            //TODO: badddd... baddd... programming ! Very bad !
            if (study.getCurrentPanel() == null) {
                try {
                    wait(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(GeneAutoDiscover.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (study.getResultingGene().getProductName() != null) {
                study.getCurrentPanel().setTitle("[" + organismName + "]   " + study.getResultingGene().getProductName() + " (" + geneName + ")");
            } else {
                study.getCurrentPanel().setTitle("[" + organismName + "]   " + geneName);
            }
        }

        System.out.println("Fetched information from NCBI using FASTA headers.");
    }

    private void updatePanels() {
        /* It might be the case where the gene auto discovery was launched and finished before the study panel was even created. */
        if (study.getCurrentPanel() != null) {
            study.getCurrentPanel().remakePanel();
        }

        /* If this study is selected, update information in other panels by re-selecting it. */
        GeneInformationPanel.getInstance().updateInformationForSelectedStudy();
        Protein3DViewerPanel.getInstance().updateViewerForSelectedStudy();

        if (ProjectManager.getInstance().getSelectedProject().getSelectedStudy() == study) {
            ProjectManager.getInstance().getSelectedProject().setSelectedStudy(study);
        }
    }
}
