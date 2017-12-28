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

package org.chocosolver.solver.constraints.statistical.hotelling;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.matrix.GaussJordan;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.la4j.Matrix;
import org.la4j.inversion.GaussJordanInverter;
import org.la4j.matrix.dense.Basic2DMatrix;

/**
 * https://en.wikipedia.org/wiki/Hotelling%27s_T-squared_distribution
 * 
 * @author Roberto Rossi
 *
 */

public class tSquareStatistic {
   
   /***************************************/
   /* Observations are decision variables */
   /***************************************/
   
   public static void decompose(String name,
                                RealVar[] mu, 
                                RealVar[][] observations,
                                RealVar[][] sigma,
                                RealVar statistic, 
                                double precision){
      Solver solver = statistic.getSolver();

      int n = observations[0].length;
      int M = observations.length;

      RealVar[][] matrix = sigma;
      RealVar[][] inverseVariable = new RealVar[n][n];

      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix.length; j++){
            inverseVariable[i][j] = VariableFactory.real(name+"_inverseCov_"+(i+1)+"_"+(j+1), -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
         }
      }

      RealVar[] means = new RealVar[observations[0].length];
      for(int i = 0; i < means.length; i++){
         means[i] = VariableFactory.real(name+"_Mean_"+(i+1), -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
      }

      decomposeMeans(observations, means);

      GaussJordan.decompose(name+"_GaussJordan", matrix, inverseVariable);

      String statisticString = M+"*(";
      for(int i = 0; i < mu.length; i++){
         statisticString += "({"+(i+mu.length)+"}-{"+i+"})*(";
         for(int j = 0; j < mu.length; j++){
            statisticString += "({"+(j+mu.length)+"}-{"+j+"})*({"+(mu.length + means.length + j*n+i)+(j == mu.length - 1 ? "})" : "})+");
         }
         statisticString += i == mu.length - 1 ? "))={"+(mu.length+means.length+n*n)+"}" : ")+";
      }

      RealVar[] allVars = new RealVar[mu.length + means.length + n*n + 1];
      System.arraycopy(mu, 0, allVars, 0, mu.length);
      System.arraycopy(means, 0, allVars, mu.length, means.length);
      System.arraycopy(flatten(inverseVariable), 0, allVars, mu.length + means.length, n*n);
      allVars[allVars.length - 1] = statistic;

      solver.post(new RealConstraint(name+"_chiSq", statisticString, Ibex.HC4_NEWTON, allVars));
   }
   
   public static void decompose(String name,
                                RealVar[] mu, 
                                RealVar[][] observations,
                                RealVar statistic, 
                                double precision){
      Solver solver = statistic.getSolver();

      int n = observations[0].length;
      int M = observations.length;
      
      RealVar[][] matrix = new RealVar[n][n];
      RealVar[][] inverseVariable = new RealVar[n][n];
      
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix.length; j++){
            matrix[i][j] = VariableFactory.real(name+"_Cov_"+(i+1)+"_"+(j+1), -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
            inverseVariable[i][j] = VariableFactory.real(name+"_InverseCov_"+(i+1)+"_"+(j+1), -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
         }
      }
      
      computeCovarianceMatrix(observations, matrix, precision);

      RealVar[] means = new RealVar[observations[0].length];
      for(int i = 0; i < means.length; i++){
         means[i] = VariableFactory.real(name+"_Mean_"+(i+1), -Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, precision, solver);
      }
      
      decomposeMeans(observations, means);

      GaussJordan.decompose(name+"_GaussJordan", matrix, inverseVariable);

      String statisticString = M+"*(";
      for(int i = 0; i < mu.length; i++){
         statisticString += "({"+(i+mu.length)+"}-{"+i+"})*(";
         for(int j = 0; j < mu.length; j++){
            statisticString += "({"+(j+mu.length)+"}-{"+j+"})*({"+(mu.length + means.length + j*n+i)+(j == mu.length - 1 ? "})" : "})+");
         }
         statisticString += i == mu.length - 1 ? "))={"+(mu.length+means.length+n*n)+"}" : ")+";
      }

      RealVar[] allVars = new RealVar[mu.length + means.length + n*n + 1];
      System.arraycopy(mu, 0, allVars, 0, mu.length);
      System.arraycopy(means, 0, allVars, mu.length, means.length);
      System.arraycopy(flatten(inverseVariable), 0, allVars, mu.length + means.length, n*n);
      allVars[allVars.length - 1] = statistic;

      solver.post(new RealConstraint(name+"_T2Dist", statisticString, Ibex.HC4_NEWTON, allVars));
   }
   
   private static void computeCovarianceMatrix(RealVar[][] observations, RealVar[][] matrix, double precision){
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix.length; j++){
            org.chocosolver.solver.constraints.nary.deviation.Covariance.decompose("Covariance_"+i+"_"+j, getArray(i, observations), getArray(j, observations), matrix[i][j], precision);
         }
      }
   }
   
   private static RealVar[] getArray(int j, RealVar[][] observations){
      RealVar[] result = new RealVar[observations.length];
      for(int i = 0; i < observations.length; i++){
         result[i] = observations[i][j];
      }
      return result;
   }
   
   private static void decomposeMeans(RealVar[][] observations, RealVar[] means){
      Solver solver = observations[0][0].getSolver();
      
      String totalStr = "";
      for(int i = 0; i < observations.length; i++){
         totalStr += "{"+i+ (i == observations.length - 1 ? "}="+observations.length+"*{"+observations.length+"}" : "}+");
      }
      
      for(int j = 0; j < observations[0].length; j++){
         RealVar[] allVars = new RealVar[observations.length+1];
         System.arraycopy(getArray(j, observations), 0, allVars, 0, observations.length);
         allVars[observations.length] = means[j];
         solver.post(new RealConstraint("Mean_"+(j+1), totalStr, Ibex.HC4_NEWTON, allVars));    
      }
   }
   
   private static RealVar[] flatten(RealVar[][] matrix){
      int n = matrix.length;
      RealVar[] array = new RealVar[n*n];
      for(int i = 0; i < n; i++){
         for(int j = 0; j < n; j++){
            array[i*n+j] = matrix[i][j];
         }
      }
      return array;
   }
}
