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

package org.syat.statistics;

import org.la4j.Matrix;
import org.la4j.matrix.dense.Basic2DMatrix;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.FisherFDist;

import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvarmulti.MultinormalCholeskyGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

import org.apache.commons.math3.stat.correlation.Covariance;

import org.la4j.inversion.GaussJordanInverter;

/**
 * https://en.wikipedia.org/wiki/Hotelling%27s_T-squared_distribution
 * 
 * @author Roberto Rossi
 *
 */

public class tSquareTest {

   double[] mu;
   double[][] sigma;
   double[][] observations;
   
   private boolean estimatedCovariance;
   
   public tSquareTest(double[] mu, double[][] sigma, double[][] observations){
      this.mu = mu;
      this.sigma = sigma;
      this.observations = observations;
      estimatedCovariance = false;
   }
   
   public tSquareTest(double[] mu, double[][] observations){
      this.mu = mu;
      this.sigma = computeCovarianceMatrix(observations);
      this.observations = observations;
      estimatedCovariance = true;
   }
   
   public double tSquareStatistic(){
      double[] totals = getTotals(observations, mu);
      double[][] sigmaM = new double[sigma.length][sigma[0].length];
      for(int i = 0; i < sigmaM.length; i++){
         for(int j = 0; j < sigmaM[i].length; j++){
            sigmaM[i][j] = sigma[i][j] * (estimatedCovariance ? (observations.length-1) : observations.length);
         }
      }
      Matrix a = new Basic2DMatrix(sigmaM);
      Matrix b = new GaussJordanInverter(a).inverse(); 
      double[] result = dotProduct(totals, toArray(b));
      double statistic = dotProduct(result, totals);
      return statistic;
   }
   
   public double tSquareTestPValue(){
      double p;
      if(estimatedCovariance){
         ChiSquareDist dist = new ChiSquareDist(this.mu.length);
         p = dist.cdf(tSquareStatistic());
      }else{
         FisherFDist dist = new FisherFDist(this.mu.length, this.observations.length - this.mu.length);
         double statistic = tSquareStatistic()*(this.observations.length - this.mu.length)/((this.observations.length - 1) * this.mu.length);
         p = dist.cdf(statistic);
      }
      return 1 - p;
   }
   
   public boolean tSquareTestBoolean(double significance){
      return tSquareTestPValue() > significance;
   }
   
   double[] getTotals(double[][] observations, double[] mu){
      double[] totals = new double[observations[0].length];
      for(int i = 0; i < observations.length; i++){
         for(int j = 0; j < observations[i].length; j++){
            totals[j] += observations[i][j] - mu[j];
         }
      }
      return totals;
   }
   
   double[][] toArray(Matrix matrix){
      double[][] result = new double[matrix.rows()][matrix.columns()];
      for(int i = 0; i < matrix.rows(); i++){
         for(int j = 0; j < matrix.columns(); j++){
            result[i][j] = matrix.get(i, j);
         }
      }
      return result;
   }
   
   double[] dotProduct(double[] vect, double[][] matrix){
      double [] result = new double[matrix[0].length];
      for(int j = 0; j < matrix[0].length; j++){
         for(int i = 0; i < vect.length; i++){
            result[j] += vect[i]*matrix[i][j];
         }
      }
      return result;
   }
   
   double dotProduct(double[] vect1, double[] vect2){
      double result = 0;
      for(int i = 0; i < vect1.length; i++){
         result += vect1[i]*vect2[i];
      }
      return result;
   }
   
   double[] getArray(int j, double[][] observations){
      double[] result = new double[observations.length];
      for(int i = 0; i < observations.length; i++){
         result[i] = observations[i][j];
      }
      return result;
   }
   
   double[][] computeCovarianceMatrix(double[][] observations){
      double[][] matrix = new double[observations[0].length][observations[0].length];
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix.length; j++){
            matrix[i][j] = (new Covariance()).covariance(getArray(i, observations), getArray(j, observations));
         }
      }
      return matrix;
   }
   
   private static double[][] generateObservations(double[] mu, double[][] sigma, int nbObservations){
      MRG32k3a rng = new MRG32k3a();
      rng.setSeed(new long[]{1,2,3,4,5,6});
      NormalGen gen = new NormalGen(rng);
      MultinormalCholeskyGen dist = new MultinormalCholeskyGen(gen, mu, sigma);
      double[][] observations = new double[nbObservations][mu.length];
      dist.nextArrayOfPoints(observations, 0, nbObservations);
      return observations;
   }
   
   public static void main(String args[]){
      double[] mu = {1, 1, 1};
      double[][] sigma = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      
      int M = 50;
      
      double[][] observations = generateObservations(new double[]{1,1,1}, sigma, M);
      
      tSquareTest test = new tSquareTest(mu, observations);
      
      System.out.println(test.tSquareStatistic());
      System.out.println(test.tSquareTestPValue());
      System.out.println(test.tSquareTestBoolean(0.05));
   }
   
}
