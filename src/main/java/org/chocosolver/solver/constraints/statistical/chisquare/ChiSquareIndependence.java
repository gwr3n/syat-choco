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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.nary.contingency.ContingencyDecompositions;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Chi squared test of independence statistical constraint decomposition. 
 * 
 * @author Roberto Rossi
 * @see <a href="https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3900058/">The Chi-square test of independence</a>
 */

public class ChiSquareIndependence {

   /**
    * Chi squared test of independence statistical constraint decomposition for integer valued observations.
    * 
    * @param name constraint name
    * @param seriesA population A observations
    * @param seriesB population B observations
    * @param binBounds bin bounds expressed as a list of breakpoints
    * @param statistic chi squared statistic
    * @param precision Ibex precision
    * @param allowOutOfBinObservations allow observations to fall outside bin bounds
    */
   public static void decomposition(String name,
                                    IntVar[] seriesA,
                                    IntVar[] seriesB,
                                    int[][] binBounds,
                                    RealVar statistic,
                                    double precision,
                                    boolean allowOutOfBinObservations){
      
      Solver solver = statistic.getSolver();
      
      int observations = seriesA.length;

      IntVar[][] binVariables = VariableFactory.boundedMatrix("BinMatrix", binBounds[0].length - 1, binBounds[1].length - 1, 0, observations, solver);
      
      /*      new IntVar[binBounds[0].length - 1][binBounds[1].length - 1];
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            binVariables[i][j] = VariableFactory.bounded(name+"_Bin "+(i+1)+","+(j+1), 0, observations, solver);
         }
      }*/
      
      IntVar[] marginalsH = VariableFactory.boundedArray(name+"_Marginals H", binVariables.length, 0, observations, solver);
      
      IntVar[] marginalsV = VariableFactory.boundedArray(name+"_Marginals V", binVariables[0].length, 0, observations, solver);
      
      ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
      
      IntVar[] flattenedBins = new IntVar[binVariables.length*binVariables[0].length];
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            flattenedBins[binVariables[0].length*i + j] = binVariables[i][j]; 
         }
      }
      
      if(!allowOutOfBinObservations){
         IntVar totalCount = VariableFactory.fixed(name+"_total count", observations, solver);
         solver.post(SyatConstraintFactory.sum(flattenedBins, totalCount));
      }

      String chiSqExp = "";
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            int n = binVariables.length*binVariables[0].length;
            String expected = "({"+(n+i)+"}*{"+(n+binVariables.length+j)+"}/"+observations+")";
            chiSqExp += "({"+(binVariables[0].length*i + j)+"}-"+expected+")^2/"+expected;
            if(i != binVariables.length - 1 || j != binVariables[0].length - 1) 
               chiSqExp += "+";
            else
               chiSqExp += "={"+(n+binVariables.length+binVariables[0].length)+"}";
         }
      }

      RealVar[] allRealVariables = new RealVar[flattenedBins.length + binVariables.length + binVariables[0].length + 1];
      System.arraycopy(VF.real(flattenedBins, precision), 0, allRealVariables, 0, flattenedBins.length);
      System.arraycopy(VF.real(marginalsH, precision), 0, allRealVariables, flattenedBins.length, marginalsH.length);
      System.arraycopy(VF.real(marginalsV, precision), 0, allRealVariables, flattenedBins.length + marginalsH.length, marginalsV.length);
      allRealVariables[flattenedBins.length + binVariables.length + binVariables[0].length] = statistic;

      solver.post(new RealConstraint(name, chiSqExp, Ibex.HC4_NEWTON, allRealVariables));
   }
   
   /**
    * Chi squared test of independence statistical constraint decomposition for real valued observations.
    * 
    * @param name constraint name
    * @param seriesA population A observations
    * @param seriesB population B observations
    * @param binBounds bin bounds expressed as a list of breakpoints
    * @param statistic chi squared statistic
    * @param precision Ibex precision
    * @param allowOutOfBinObservations allow observations to fall outside bin bounds
    */
   public static void decomposition(String name,
                                    RealVar[] seriesA,
                                    RealVar[] seriesB,
                                    double[][] binBounds,
                                    RealVar statistic,
                                    double precision,
                                    boolean allowOutOfBinObservations){
      
      Solver solver = statistic.getSolver();

      int observations = seriesA.length;

      IntVar[][] binVariables = VariableFactory.boundedMatrix("BinMatrix", binBounds[0].length - 1, binBounds[1].length - 1, 0, observations, solver);
            /*new IntVar[binBounds[0].length - 1][binBounds[1].length - 1];
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            binVariables[i][j] = VariableFactory.bounded(name+"_Bin "+(i+1)+","+(j+1), 0, observations, solver);
         }
      }*/
      
      IntVar[] marginalsH = VariableFactory.boundedArray(name+"_Marginals H", binVariables.length, 0, observations, solver);

      IntVar[] marginalsV = VariableFactory.boundedArray(name+"_Marginals V", binVariables[0].length, 0, observations, solver);

      ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);

      IntVar[] flattenedBins = new IntVar[binVariables.length*binVariables[0].length];
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            flattenedBins[binVariables[0].length*i + j] = binVariables[i][j]; 
         }
      }

      if(!allowOutOfBinObservations){
         IntVar totalCount = VariableFactory.fixed(name+"_total count", observations, solver);
         solver.post(SyatConstraintFactory.sum(flattenedBins, totalCount));
      }

      String chiSqExp = "";
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            int n = binVariables.length*binVariables[0].length;
            String expected = "({"+(n+i)+"}*{"+(n+binVariables.length+j)+"}/"+observations+")";
            chiSqExp += "({"+(binVariables[0].length*i + j)+"}-"+expected+")^2/"+expected;
            if(i != binVariables.length - 1 || j != binVariables[0].length - 1) 
               chiSqExp += "+";
            else
               chiSqExp += "={"+(n+binVariables.length+binVariables[0].length)+"}";
         }
      }

      RealVar[] flattenedBinsReal = VariableFactory.real(flattenedBins, precision);
      RealVar[] marginalsHReal = VariableFactory.real(marginalsH, precision);
      RealVar[] marginalsVReal = VariableFactory.real(marginalsV, precision);
      
      RealVar[] allRealVariables = new RealVar[flattenedBins.length + binVariables.length + binVariables[0].length + 1];
      System.arraycopy(flattenedBinsReal, 0, allRealVariables, 0, flattenedBins.length);
      System.arraycopy(marginalsHReal, 0, allRealVariables, flattenedBins.length, marginalsH.length);
      System.arraycopy(marginalsVReal, 0, allRealVariables, flattenedBins.length + marginalsH.length, marginalsV.length);
      allRealVariables[flattenedBins.length + binVariables.length + binVariables[0].length] = statistic;

      solver.post(new RealConstraint(name, chiSqExp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
