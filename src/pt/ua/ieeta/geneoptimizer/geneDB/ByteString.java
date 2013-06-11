package pt.ua.ieeta.geneoptimizer.geneDB;

/**
 *
 * @author Paulo
 */
public class ByteString
{
    private byte[] data = null;

    public ByteString(int sizeInBytes)
    {
        data = new byte[sizeInBytes];
    }

    public ByteString(String dataString)
    {
        setString(dataString);
    }

    public byte[] getDataReference()
    {
        return data;
    }

    public int getLength()
    {
        if (data == null) return 0;

        return data.length;
    }

    public String getString()
    {
        return new String(data);
    }

    public void setString(String newData)
    {
        data = newData.getBytes();
    }

    public ByteString getCopy()
    {
        return new ByteString(this.getString());
    }
}
