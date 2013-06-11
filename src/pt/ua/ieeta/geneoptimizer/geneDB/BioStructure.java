

package pt.ua.ieeta.geneoptimizer.geneDB;

/**
 *
 * @author Paulo Gaspar
 */
public class BioStructure
{
    public static enum Type
    {
        mRNAPrimaryStructure(3,3, false),
        proteinPrimaryStructure(3,1, false),
        proteinSecondaryStructure(3,1, true);

        private int wordOccupation, wordSize;
        private boolean joinEqualWords;
        Type(int wordOccupation, int wordSize, boolean joinEqualWords)
        {
            assert wordOccupation >= wordSize;

            this.wordOccupation = wordOccupation; /* How many letters should a word occupy. */
            this.wordSize = wordSize; /* Word size in number of letters. */
            this.joinEqualWords = joinEqualWords;
        }

        public int getWordOccupation()
        {   return wordOccupation; }

        public int  getWordSize()
        {   return wordSize; }

        public boolean joinEqualWords()
        {   return joinEqualWords; }
    }

    /* Structure data. Sequence of biological words. */
    private ByteString sequence;

    /* List of the global positions of each word. Each pos identifies where each word is positioned in relation to its respective codon. */
    private int[] globalPos;

    /* List of the global letter occupation of each word. */
    private int[] globalSpan;

    /* Which type of structure is this. */
    private Type type;

    /* Size of this sequence in words. */
    private int length;

    /* Size of this sequence in characters. */
    private int sequenceOccupation;

    //TODO: ESTA CLASSE NECESSITA REFACTORING !!! algumas coisas ja nao sao usadas!
    public BioStructure(ByteString nucSequence, Type type)
    {
        assert nucSequence != null;
        //assert (nucSequence.getLength() % type.getWordSize()) == 0;
        assert type != null;

        int numberOfWords = (nucSequence.getLength() / type.getWordSize());

        this.sequence = nucSequence;
        this.globalPos = new int[numberOfWords];
        this.globalSpan = new int[numberOfWords];
        this.type = type;
        this.length = numberOfWords;
        this.sequenceOccupation = numberOfWords * type.getWordOccupation();

        for (int i=0; i<numberOfWords; i++)
        {
            globalPos[i] = i*type.getWordSize();
            globalSpan[i] = type.getWordSize();
        }

        assert globalPos.length == numberOfWords;
        assert globalSpan.length == numberOfWords;
    }

    //TODO: ainda a redefenir/verificar/juntar com a de cima
//    BioStructure(ByteString nucSequence, Type type, int BRAAAA)
//    {
//        assert nucSequence != null;
//
//        this.sequence = nucSequence;
//        this.globalPos = new int[0];
//        this.globalSpan = new int[0];
//        this.type = type;
//
//
//        this.sequenceOccupation = 320;
//
//
//        //TODO: ISTO AINDA NAO FOI VERIFICADO SE ESTA A FUNCIONAR
//        /* For the tertiary structure, repeated words must be counted and identified as a whole. */
//        byte [] data = nucSequence.getDataReference();
//        int j=0, contador=1;
//
//        globalPos[0] = 0;
//        for (int i=0; i<nucSequence.getLength()-1; i++)
//            if (data[i] != data[i+1])
//            {
//                /* Resize array. */
//                int [] tmp = new int[globalSpan.length+1];
//                System.arraycopy(globalSpan, 0, tmp, 0, globalSpan.length+1);
//                globalSpan = tmp;
//
//                /* Resize array. */
//                int [] tmp2 = new int[globalPos.length+1];
//                System.arraycopy(globalPos, 0, tmp2, 0, globalPos.length+1);
//                globalPos = tmp2;
//
//                globalSpan[j++] = contador;
//                globalPos[j] = i+1;
//
//                contador = 1;
//            } else contador++;
//
//        this.length = globalSpan.length;
//
//        assert globalSpan.length == globalPos.length;
//    }

    /** Returns structure sequence. */
    public String getSequence()
    {
        return sequence.getString();
    }

    /** Returns structure sequence. */
    public ByteString getByteSequence()
    {
        return sequence;
    }

    /** Returns length of structure in number of words. */
    public int getLength()
    {
        return length;
    }

    /** Returns the n-position word, where n is given by parameter. */
    public String getWordAt(int wordNumber)
    {
        assert wordNumber >= 0;
        assert wordNumber < getLength();

        int wordPosition = globalPos[wordNumber];
        int wordLength = globalSpan[wordNumber];

        return new String(sequence.getDataReference(), wordPosition, wordLength);
    }

    /* Return sub-string from the sequence. From start (inclusive) to end (exclusive). */
    public String getSubSequence(int start, int end)
    {
        assert start >= 0;
        assert end > 0;
        assert start < end;

        StringBuilder sb = new StringBuilder();

        for (int i=start; i<end; i++)
            sb.append(getWordAt(i));

        return sb.toString();
    }

    /* Returns the number of characters in a given word. */
    public int getWordSize(int wordNumber)
    {
        assert wordNumber >= 0;

        if (type.getWordSize() >= 0)
            return type.getWordSize();

        return globalSpan[wordNumber];
    }

    public int getWordOccupation(int wordNumber)
    {
        assert wordNumber >= 0;

        if (type.getWordOccupation() >= 0)
            return type.getWordOccupation();

        return getWordSize(wordNumber);
    }

    public int getSequenceOccupation()
    {
        return sequenceOccupation;
    }

    public Type getType()
    {
        return type;
    }
}
