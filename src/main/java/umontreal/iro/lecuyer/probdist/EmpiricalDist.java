

/*
 * Class:        EmpiricalDist
 * Description:  empirical discrete distribution function
 * Environment:  Java
 * Software:     SSJ
 * Copyright (C) 2001  Pierre L'Ecuyer and Universit�� de Montr��al
 * Organization: DIRO, Universit�� de Montr��al
 * @author
 * @since

 * SSJ is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License (GPL) as published by the
 * Free Software Foundation, either version 3 of the License, or
 * any later version.

 * SSJ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * A copy of the GNU General Public License is available at
   <a href="http://www.gnu.org/licenses">GPL licence site</a>.
 */

package umontreal.iro.lecuyer.probdist;

import java.util.Formatter;
import java.util.Locale;
import java.util.Arrays;
import java.io.IOException;
import java.io.Reader;
import java.io.BufferedReader;
import umontreal.iro.lecuyer.util.Misc;
import umontreal.iro.lecuyer.util.PrintfFormat;


/**
 * Extends {@link DiscreteDistribution} to an <SPAN  CLASS="textit">empirical</SPAN>
 * distribution function,
 * based on the observations 
 * <SPAN CLASS="MATH"><I>X</I><SUB>(1)</SUB>,..., <I>X</I><SUB>(n)</SUB></SPAN> (sorted by increasing order).
 * The distribution is uniform over the <SPAN CLASS="MATH"><I>n</I></SPAN> observations, so the
 * distribution function has a jump of <SPAN CLASS="MATH">1/<I>n</I></SPAN> at each of the <SPAN CLASS="MATH"><I>n</I></SPAN> observations.
 * 
 */
public class EmpiricalDist extends DiscreteDistribution {
   private int n = 0;
   private double sampleMean;
   private double sampleVariance;
   private double sampleStandardDeviation;



   /**
    * Constructs a new empirical distribution using
    *   all the observations stored in <TT>obs</TT>, and which are  assumed
    *   to have been sorted in increasing numerical order. <A NAME="tex2html1"
    *   HREF="#foot62"><SUP><SPAN CLASS="arabic">1</SPAN></SUP></A>  These observations are copied into an internal array.
    * 
    */
   public EmpiricalDist (double[] obs) {
      if (obs.length <= 1)
         throw new IllegalArgumentException
            ("Two or more observations are needed");
      nVal = n = obs.length;
      sortedVal = new double[n];
      System.arraycopy (obs, 0, sortedVal, 0, n);
      init();
   }


   /**
    * Constructs a new empirical distribution using
    *   the observations read from the reader <TT>in</TT>. This constructor
    *   will read the first <TT>double</TT> of each line in the stream.
    *   Any line that does not start with a <TT>+</TT>, <TT>-</TT>, or a decimal digit,
    *   is ignored.  One must be careful about lines starting with a blank.
    *   This format is the same as in UNURAN. The observations read are  assumed
    *   to have been sorted in increasing numerical order.
    * 
    */
   public EmpiricalDist (Reader in) throws IOException {
      BufferedReader inb = new BufferedReader (in);
      double[] data = new double[5];
      n = 0;
      String li;
      while ((li = inb.readLine()) != null) {
        li = li.trim();

         // look for the first non-digit character on the read line
         int index = 0;
         while (index < li.length() &&
            (li.charAt (index) == '+' || li.charAt (index) == '-' ||
             li.charAt (index) == 'e' || li.charAt (index) == 'E' ||
             li.charAt (index) == '.' || Character.isDigit (li.charAt (index))))
           ++index;

         // truncate the line
         li = li.substring (0, index);
         if (!li.equals ("")) {
            try {
               data[n++] = Double.parseDouble (li);
               if (n >= data.length) {
                  double[] newData = new double[2*n];
                  System.arraycopy (data, 0, newData, 0, data.length);
                  data = newData;
               }
            }
            catch (NumberFormatException nfe) {}
         }
      }
      sortedVal = new double[n];
      System.arraycopy (data, 0, sortedVal, 0, n);
      nVal = n;
      init();
   }


   public double prob (int i) {
      if (i >= 0 && i < n)
         return 1.0 / n;
      throw new IllegalStateException();
   }

   public double cdf (double x) {
      if (x < sortedVal[0])
         return 0;
      if (x >= sortedVal[n-1])
         return 1;
      for (int i = 0; i < (n-1); i++) {
         if (x >= sortedVal[i] && x < sortedVal[i+1])
            return (double)(i + 1)/n;
      }
      throw new IllegalStateException();
   }

   public double barF (double x) {
      if (x <= sortedVal[0])
         return 1;
      if (x > sortedVal[n-1])
         return 0;
      for (int i = 0; i < (n-1); i++) {
         if (x > sortedVal[i] && x <= sortedVal[i+1])
            return ((double)n-1-i)/n;
      }
      throw new IllegalStateException();
   }

   public double inverseF (double u) {
      if (u < 0 || u > 1)
         throw new IllegalArgumentException ("u is not in [0,1]");
      if (u == 1.0)
         return sortedVal[n-1];
      int i = (int)Math.floor ((double)n * u);
      return sortedVal[i];
   }

   private void init() {
      Arrays.sort (sortedVal);
      double sum = 0.0;
      for (int i = 0; i < sortedVal.length; i++) {
         sum += sortedVal[i];
      }
      sampleMean = sum / n;
      sum = 0.0;
      for (int i = 0; i < n; i++) {
         double coeff = (sortedVal[i] - sampleMean);
         sum += coeff * coeff;
      }
      sampleVariance = sum / (n-1);
      sampleStandardDeviation = Math.sqrt (sampleVariance);
      supportA = sortedVal[0];
      supportB = sortedVal[n-1];
      xmin = 0;
      xmax = n - 1;
   }

   public double getMean() {
      return sampleMean;
   }

   public double getStandardDeviation() {
      return sampleStandardDeviation;
   }

   public double getVariance() {
      return sampleVariance;
   }

   /**
    * Returns the median.
    *    Returns the 
    * <SPAN CLASS="MATH"><I>n</I>/2<SUP>th</SUP></SPAN> item of the sorted observations when the number
    *    of items is odd, and the mean of the 
    * <SPAN CLASS="MATH"><I>n</I>/2<SUP>th</SUP></SPAN> and the
    *    
    * <SPAN CLASS="MATH">(<I>n</I>/2 + 1)<SUP>th</SUP></SPAN> items when the number of items is even.
    * 
    */
   public double getMedian () {
      if ((n % 2) == 0)
         return ((sortedVal[n / 2 - 1] + sortedVal[n / 2]) / 2.0);
      else
         return sortedVal[(n - 1) / 2];
   }


   /**
    * Returns the median.
    *    Returns the 
    * <SPAN CLASS="MATH"><I>n</I>/2<SUP>th</SUP></SPAN> item of the array <TT>obs</TT> when the number
    *    of items is odd, and the mean of the 
    * <SPAN CLASS="MATH"><I>n</I>/2<SUP>th</SUP></SPAN> and the
    *    
    * <SPAN CLASS="MATH">(<I>n</I>/2 + 1)<SUP>th</SUP></SPAN> items when the number of items is even.
    *    The array does not have to be sorted.
    * 
    * @param obs the array of observations
    * 
    *    @param n the number of observations
    * 
    *    @return return the median of the observations
    * 
    */
   public static double getMedian (double obs[], int n) {
      if ((n % 2) == 0)
         return ((Misc.quickSelect (obs, n, (n / 2 - 1)) +
                  Misc.quickSelect (obs, n, (n / 2))) / 2.0);
      else
         return Misc.quickSelect (obs, n, ((n - 1) / 2));
   }


   /**
    * Returns <SPAN CLASS="MATH"><I>n</I></SPAN>, the number of observations.
    * 
    */
   public int getN() {
      return n;
   }


   /**
    * Returns the value of <SPAN CLASS="MATH"><I>X</I><SUB>(i)</SUB></SPAN>, for 
    * <SPAN CLASS="MATH"><I>i</I> = 0, 1,&#8230;, <I>n</I> - 1</SPAN>.
    * 
    */
   public double getObs (int i) {
      return sortedVal[i];
   }


   /**
    * Returns the sample mean of the observations.
    * 
    */
   public double getSampleMean() {
      return sampleMean;
   }


   /**
    * Returns the sample variance of the observations.
    * 
    */
   public double getSampleVariance() {
      return sampleVariance;
   }


   /**
    * Returns the sample standard deviation of the observations.
    * 
    */
   public double getSampleStandardDeviation() {
      return sampleStandardDeviation;
   }


   /**
    * Returns the <SPAN  CLASS="textit">interquartile range</SPAN> of the observations,
    *    defined as the difference between the third and first quartiles.
    * 
    */
   public double getInterQuartileRange() {
      int j = n/2;
      double lowerqrt=0, upperqrt=0;
      if (j % 2 == 1) {
         lowerqrt = sortedVal[(j+1)/2-1];
         upperqrt = sortedVal[n-(j+1)/2];
      }
      else {
         lowerqrt = 0.5 * (sortedVal[j/2-1] + sortedVal[j/2+1-1]);
         upperqrt = 0.5 * (sortedVal[n-j/2] + sortedVal[n-j/2-1]);
      }
      double h =upperqrt - lowerqrt;
      if (h < 0)
         throw new IllegalStateException("Observations MUST be sorted");
      return h;
   }


   /**
    * Return a table containing parameters of the current distribution.
    * 
    */
   public double[] getParams () {
      double[] retour = new double[n];
      System.arraycopy (sortedVal, 0, retour, 0, n);
      return retour;
   }


   /**
    * Returns a <TT>String</TT> containing information about the current distribution.
    * 
    */
   public String toString () {
      StringBuilder sb = new StringBuilder();
      Formatter formatter = new Formatter(sb, Locale.US);
      formatter.format(getClass().getSimpleName() + PrintfFormat.NEWLINE);
      for(int i = 0; i<n; i++) {
         formatter.format("%f%n", sortedVal[i]);
      }
      return sb.toString();
   }

}
