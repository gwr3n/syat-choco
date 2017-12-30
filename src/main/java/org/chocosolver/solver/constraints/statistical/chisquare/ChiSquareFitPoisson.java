/*
 * syat-choco: a Choco extension for Declarative Statistics.
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.chocosolver.solver.constraints.statistical.chisquare;

import java.math.BigDecimal;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;

/**
 * Chi square goodness-of-fit statistical constraint decomposition. 
 * The goodness-of-fit is computed against target frequencies obtained 
 * from a Poisson distribution. 
 * 
 * These decompositions exploits Stirling's approximation to the factorial.
 * 
 * @author Roberto Rossi
 *
 */

public class ChiSquareFitPoisson {
   
   /**
    * Chi square goodness-of-fit statistical constraint decomposition;
    * Poisson distribution with real valued observations. 
    * 
    * @param name constraint name
    * @param observations observations
    * @param binCounts bin counts
    * @param binBounds bin bounds expressed as a list of breakpoints
    * @param lambda Poisson mean
    * @param statistic chi squared statistic
    * @param precision Ibex precision
    * @param allowOutOfBinObservations allow observations to fall outside bin bounds
    */
   public static void decomposition(String name,
                                    RealVar[] observations, 
                                    IntVar[] binCounts, 
                                    double[] binBounds, 
                                    RealVar lambda, 
                                    RealVar statistic, 
                                    double precision,
                                    boolean allowOutOfBinObservations){
      Solver solver = statistic.getSolver();

      //RealVar[] realBinViews = VF.real(binCounts, precision);
      //solver.post(IntConstraintFactorySt.bincounts(observations, realBinViews, binBounds, BincountsPropagatorType.EQFast));
      SyatConstraintFactory.bincountsDecomposition(observations, 
                                                    binCounts, 
                                                    binBounds, 
                                                    precision, 
                                                    allowOutOfBinObservations ? BincountsDecompositionType.Agkun2016_2_LE :
                                                                                BincountsDecompositionType.Agkun2016_2_EQ);

      RealVar[] realBinCounts = VF.real(binCounts, precision);

      String[] targetFrequencies = new String[binBounds.length-1];
      for(int b = 0; b < binBounds.length-1; b++){
         targetFrequencies[b] = "";
         for(int i = (int) binBounds[b] ; i < (int) binBounds[b+1] ; i++){
            // Stirling's approximation to the factorial
            targetFrequencies[b] += i == 0 ?  
                  observations.length+"*2.718^(-{"+(binBounds.length)+"})" : 
                     observations.length+"*{"+(binBounds.length)+"}^"+i+"*2.718^(-{"+(binBounds.length)+"})/(sqrt(2*3.14159*"+i+")*("+i+"/2.718)^"+i+")";
            if(i < (int) binBounds[b+1] - 1)
               targetFrequencies[b] += " + ";
         }
      }

      String chiSqExp = "";
      for(int i = 0; i < binCounts.length; i++)
         if(i == binCounts.length - 1)
            chiSqExp += "(({"+i+"}-max("+targetFrequencies[i]+","+(new BigDecimal(precision).toPlainString())+"))^2)/max("+targetFrequencies[i]+","+(new BigDecimal(precision).toPlainString())+")={"+(binCounts.length)+"}";
         else
            chiSqExp += "(({"+i+"}-max("+targetFrequencies[i]+","+(new BigDecimal(precision).toPlainString())+"))^2)/max("+targetFrequencies[i]+","+(new BigDecimal(precision).toPlainString())+")+";

      RealVar[] allRealVariables = new RealVar[realBinCounts.length + 2];
      System.arraycopy(realBinCounts, 0, allRealVariables, 0, realBinCounts.length);
      allRealVariables[realBinCounts.length] = statistic;
      allRealVariables[realBinCounts.length+1] = lambda;
      solver.post(new RealConstraint(name, chiSqExp, Ibex.HC4_NEWTON, allRealVariables));
   }
   
   /**
    * This decomposition exploits Stirling's approximation to the factorial
    * 
    * @param name constraint name
    * @param observations observations
    * @param binCounts bin counts
    * @param binBounds bin bounds expressed as a list of breakpoints
    * @param lambda Poisson mean
    * @param statistic chi squared statistic
    * @param precision Ibex precision
    * @param allowOutOfBinObservations allow observations to fall outside bin bounds
    */
   public static void decomposition(String name,
                                    IntVar[] observations, 
                                    IntVar[] binCounts, 
                                    int[] binBounds, 
                                    RealVar lambda, 
                                    RealVar statistic, 
                                    double precision,
                                    boolean allowOutOfBinObservations){
      Solver solver = statistic.getSolver();
      
      //solver.post(IntConstraintFactorySt.bincounts(observations, binCounts, binBounds, BincountsPropagatorType.EQFast));
      SyatConstraintFactory.bincountsDecomposition(observations, 
                                                    binCounts, 
                                                    binBounds, 
                                                    allowOutOfBinObservations ? BincountsDecompositionType.Agkun2016_2_LE :
                                                                                BincountsDecompositionType.Agkun2016_2_EQ);
      
      RealVar[] realBinCounts = VF.real(binCounts, precision);
      
      String[] targetFrequencies = new String[binBounds.length-1];
      for(int b = 0; b < binBounds.length-1; b++){
         targetFrequencies[b] = "";
         for(int i = (int) binBounds[b] ; i < (int) binBounds[b+1] ; i++){
            // Stirling's approximation to the factorial
            targetFrequencies[b] += i == 0 ?  
                  observations.length+"*2.718^(-{"+(binBounds.length)+"})" : 
                     observations.length+"*{"+(binBounds.length)+"}^"+i+"*2.718^(-{"+(binBounds.length)+"})/(sqrt(2*3.14159*"+i+")*("+i+"/2.718)^"+i+")";
            if(i < (int) binBounds[b+1] - 1)
               targetFrequencies[b] += " + ";
         }
      }
      
      String chiSqExp = "";
      for(int i = 0; i < binCounts.length; i++)
         if(i == binCounts.length - 1)
            chiSqExp += "(({"+i+"}-("+targetFrequencies[i]+"))^2)/("+targetFrequencies[i]+")={"+(binCounts.length)+"}";
         else
            chiSqExp += "(({"+i+"}-("+targetFrequencies[i]+"))^2)/("+targetFrequencies[i]+")+";
      
      RealVar[] allRealVariables = new RealVar[realBinCounts.length + 2];
      System.arraycopy(realBinCounts, 0, allRealVariables, 0, realBinCounts.length);
      allRealVariables[realBinCounts.length] = statistic;
      allRealVariables[realBinCounts.length+1] = lambda;
      solver.post(new RealConstraint(name, chiSqExp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
