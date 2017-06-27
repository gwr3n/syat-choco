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

public class ScoreTest {

   double[] mu;
   double[][] sigma;
   double[][] observations;
   
   private boolean estimatedCovariance;
   
   public ScoreTest(double[] mu, double[][] sigma, double[][] observations){
      this.mu = mu;
      this.sigma = sigma;
      this.observations = observations;
      estimatedCovariance = false;
   }
   
   public ScoreTest(double[] mu, double[][] observations){
      this.mu = mu;
      this.sigma = computeCovarianceMatrix(observations);
      this.observations = observations;
      estimatedCovariance = true;
   }
   
   public double scoreStatistic(){
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
   
   public double scoreTestPValue(){
      double p;
      if(estimatedCovariance){
         ChiSquareDist dist = new ChiSquareDist(this.mu.length);
         p = dist.cdf(scoreStatistic());
      }else{
         FisherFDist dist = new FisherFDist(this.mu.length, this.observations.length - this.mu.length);
         double statistic = scoreStatistic()*(this.observations.length - this.mu.length)/((this.observations.length - 1) * this.mu.length);
         p = dist.cdf(statistic);
      }
      return 1 - p;
   }
   
   public boolean scoreTest(double significance){
      return scoreTestPValue() > significance;
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
      
      ScoreTest test = new ScoreTest(mu, observations);
      
      System.out.println(test.scoreStatistic());
      System.out.println(test.scoreTestPValue());
      System.out.println(test.scoreTest(0.05));
   }
   
}
