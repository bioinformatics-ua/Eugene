package pt.ua.ieeta.geneoptimizer.GeneRedesign;


import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;


/**
 *
 * @author Paulo
 */
public class PlotDataSet extends AbstractXYDataset implements XYDataset, DomainInfo, RangeInfo
{
  private Double[] xValues;
  private Double[] yValues;
  private int itemCount;
  private Number xMin;
  private Number xMax;
  private Number yMin;
  private Number yMax;
  private Range xRange;
  private Range yRange;

    public String getName()
    {
        return "lsidfosdh";
    }

  public PlotDataSet(int numPoints)
  {
    this.xValues = new Double[numPoints];
    this.yValues = new Double[numPoints];
    this.itemCount = 0;
    double d1 = (1.0D / 0.0D);
    double d2 = (-1.0D / 0.0D);
    double d3 = (1.0D / 0.0D);
    double d4 = (-1.0D / 0.0D);
//
//      for (int j = 0; j < numPoints; j++)
//      {
//        double d5 = (Math.random() - 0.5D) * 200.0D;
//        this.xValues[j] = new Double(d5);
//        if (d5 < d1)
//          d1 = d5;
//        if (d5 > d2)
//          d2 = d5;
//        double d6 = (Math.random() + 0.5D) * 6.0D * d5 + d5;
//        this.yValues[j] = new Double(d6);
//        if (d6 < d3)
//          d3 = d6;
//        if (d6 <= d4)
//          continue;
//        d4 = d6;
//      }

    this.xMin = new Double(d1);
    this.xMax = new Double(d2);
    this.xRange = new Range(d2, d1);
    this.yMin = new Double(d3);
    this.yMax = new Double(d4);
    this.yRange = new Range(d4, d3);
  }


  public void addNewPoint(double x, double y)
  {
      xValues[this.itemCount] = x;
      yValues[this.itemCount] = y;

      if (x > xMax.doubleValue())
          xMax = x;
      if (x < xMin.doubleValue())
          xMin = x;
      
      if (y > yMax.doubleValue())
          yMax = y;
      if (y < yMin.doubleValue())
          yMin = y;
      
      this.xRange = new Range(xMin.doubleValue(), xMax.doubleValue());
      this.yRange = new Range(yMin.doubleValue(), yMax.doubleValue());

      this.itemCount++;
  }




  /*** ABSTRACT IMPLEMENTATIONS ***/

  public Number getX(int paramInt1, int paramInt2)
  {
    return this.xValues[paramInt2];
  }

  public Number getY(int paramInt1, int paramInt2)
  {
    return this.yValues[paramInt2];
  }

  public int getSeriesCount()
  {
    return 1;
  }

  public Comparable getSeriesKey(int paramInt)
  {
    return "Sample " + paramInt;
  }

  public int getItemCount(int paramInt)
  {
    return this.itemCount;
  }

  public double getDomainLowerBound()
  {
    return this.xMin.doubleValue();
  }

  public double getDomainLowerBound(boolean paramBoolean)
  {
    return this.xMin.doubleValue();
  }

  public double getDomainUpperBound()
  {
    return this.xMax.doubleValue();
  }

  public double getDomainUpperBound(boolean paramBoolean)
  {
    return this.xMax.doubleValue();
  }

  public Range getDomainBounds()
  {
    return this.xRange;
  }

  public Range getDomainBounds(boolean paramBoolean)
  {
    return this.xRange;
  }

  public Range getDomainRange()
  {
    return this.xRange;
  }

  public double getRangeLowerBound()
  {
    return this.yMin.doubleValue();
  }

  public double getRangeLowerBound(boolean paramBoolean)
  {
    return this.yMin.doubleValue();
  }

  public double getRangeUpperBound()
  {
    return this.yMax.doubleValue();
  }

  public double getRangeUpperBound(boolean paramBoolean)
  {
    return this.yMax.doubleValue();
  }

  public Range getRangeBounds(boolean paramBoolean)
  {
    return this.yRange;
  }

  public Range getValueRange()
  {
    return this.yRange;
  }

  public Number getMinimumDomainValue()
  {
    return this.xMin;
  }

  public Number getMaximumDomainValue()
  {
    return this.xMax;
  }

  public Number getMinimumRangeValue()
  {
    return this.xMin;
  }

  public Number getMaximumRangeValue()
  {
    return this.xMax;
  }

}
