package pt.ua.ieeta.geneoptimizer.FileHandling;

/**
 *
 * @author Paulo Gaspar
 *
 * Enumerator of information that can be found in FASTA headers
 */
public enum HeaderInfo
{
    /* Information regarding the gene. */
    GENE_GI, GENE_ACC, GENE_NAME, GENE_START_INDEX, GENE_END_INDEX, LOCUS_TAG,

    /* Information regarding the organism */
    ORGANIM_GI, ORGANIM_ACC, ORGANISM_NAME,

    /* Information regarding the protein */
    PROTEIN_GI, PROTEIN_ACC, PROTEIN_NAME, PROTEIN_PDB,

    /* Kegg 3 letter organism identifier. */
    KEGG_ORGANISM;
}
