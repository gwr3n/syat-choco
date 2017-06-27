package org.chocosolver.solver.constraints.statistical.score;

import org.apache.commons.math3.stat.correlation.Covariance;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.matrix.GaussJordan;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.la4j.Matrix;
import org.la4j.inversion.GaussJordanInverter;
import org.la4j.matrix.dense.Basic2DMatrix;

public class ScoreStatistic {
   public static void decompose(String name,
                                RealVar[] mu, 
                                double[][] sigma, 
                                double[][] observations,
                                RealVar statistic, 
                                double precision){
      Solver solver = statistic.getSolver();
      
      double[] totals = getTotals(observations);
      
      double[][] sigmaM = new double[sigma.length][sigma[0].length];
      for(int i = 0; i < sigmaM.length; i++){
         for(int j = 0; j < sigmaM[i].length; j++){
            sigmaM[i][j] = sigma[i][j] * observations.length;
         }
      }
      
      Matrix a = new Basic2DMatrix(sigmaM);
      Matrix b = new GaussJordanInverter(a).inverse(); 
      
      int M = observations.length;
      
      String statisticString = "";
      for(int i = 0; i < mu.length; i++){
         statisticString += "("+totals[i]+"-"+M+"*{"+i+"})*(";
         for(int j = 0; j < mu.length; j++){
            statisticString += "("+totals[j]+"-"+M+"*{"+j+"})*"+b.get(j, i)+(j == mu.length - 1 ? "" : "+");
         }
         statisticString += i == mu.length - 1 ? ")={"+mu.length+"}" : ")+";
      }
      
      RealVar[] allVars = new RealVar[mu.length + 1];
      System.arraycopy(mu, 0, allVars, 0, mu.length);
      allVars[allVars.length - 1] = statistic;
      
      solver.post(new RealConstraint(name, statisticString, Ibex.HC4_NEWTON, allVars));
   }
   
   public static void decompose(String name,
                                RealVar[] mu, 
                                double[][] observations,
                                RealVar statistic, 
                                double precision){
      Solver solver = statistic.getSolver();
      
      double[][] sigma = computeCovarianceMatrix(observations);
      
      double[] totals = getTotals(observations);

      double[][] sigmaM = new double[sigma.length][sigma[0].length];
      for(int i = 0; i < sigmaM.length; i++){
         for(int j = 0; j < sigmaM[i].length; j++){
            sigmaM[i][j] = sigma[i][j] * (observations.length - 1);
         }
      }
      
      Matrix a = new Basic2DMatrix(sigmaM);
      Matrix b = new GaussJordanInverter(a).inverse(); 
      
      int M = observations.length;
      
      String statisticString = "";
      for(int i = 0; i < mu.length; i++){
         statisticString += "("+totals[i]+"-"+M+"*{"+i+"})*(";
         for(int j = 0; j < mu.length; j++){
            statisticString += "("+totals[j]+"-"+M+"*{"+j+"})*"+b.get(j, i)+(j == mu.length - 1 ? "" : "+");
         }
         statisticString += i == mu.length - 1 ? ")={"+mu.length+"}" : ")+";
      }
      
      RealVar[] allVars = new RealVar[mu.length + 1];
      System.arraycopy(mu, 0, allVars, 0, mu.length);
      allVars[allVars.length - 1] = statistic;
      
      solver.post(new RealConstraint(name, statisticString, Ibex.HC4_NEWTON, allVars));
   }
   
   private static double[] getTotals(double[][] observations){
      double[] totals = new double[observations[0].length];
      for(int i = 0; i < observations.length; i++){
         for(int j = 0; j < observations[i].length; j++){
            totals[j] += observations[i][j];
         }
      }
      return totals;
   }
   
   private static double[][] computeCovarianceMatrix(double[][] observations){
      double[][] matrix = new double[observations[0].length][observations[0].length];
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix.length; j++){
            matrix[i][j] = (new Covariance()).covariance(getArray(i, observations), getArray(j, observations));
         }
      }
      return matrix;
   }
   
   private static double[] getArray(int j, double[][] observations){
      double[] result = new double[observations.length];
      for(int i = 0; i < observations.length; i++){
         result[i] = observations[i][j];
      }
      return result;
   }
   
   /***********************************/
   
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
            inverseVariable[i][j] = VariableFactory.real("InverseCov_"+(i+1)+"_"+(j+1), -1000, 1000, precision, solver);
         }
      }

      RealVar[][] matrixM = new RealVar[n][n];
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix.length; j++){
            matrixM[i][j] = VariableFactory.real("CovM_"+(i+1)+"_"+(j+1), -1000, 1000, precision, solver);
            solver.post(new RealConstraint(name, "{0}="+M+"*{1}", Ibex.HC4_NEWTON, new RealVar[]{matrixM[i][j],matrix[i][j]}));
         }
      }

      RealVar[] totals = new RealVar[observations[0].length];
      for(int i = 0; i < totals.length; i++){
         totals[i] = VariableFactory.real("Total_"+(i+1), -1000, 1000, precision, solver);
      }

      decomposeTotals(observations, totals);

      GaussJordan.decompose("GaussJordan", matrixM, inverseVariable);

      String statisticString = "";
      for(int i = 0; i < mu.length; i++){
         statisticString += "({"+(i+mu.length)+"}-"+M+"*{"+i+"})*(";
         for(int j = 0; j < mu.length; j++){
            statisticString += "({"+(j+mu.length)+"}-"+M+"*{"+j+"})*({"+(mu.length + totals.length + j*n+i)+(j == mu.length - 1 ? "})" : "})+");
         }
         statisticString += i == mu.length - 1 ? ")={"+(mu.length+totals.length+n*n)+"}" : ")+";
      }

      RealVar[] allVars = new RealVar[mu.length + totals.length + n*n + 1];
      System.arraycopy(mu, 0, allVars, 0, mu.length);
      System.arraycopy(totals, 0, allVars, mu.length, totals.length);
      System.arraycopy(flatten(inverseVariable), 0, allVars, mu.length + totals.length, n*n);
      allVars[allVars.length - 1] = statistic;

      solver.post(new RealConstraint(name, statisticString, Ibex.HC4_NEWTON, allVars));
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
            matrix[i][j] = VariableFactory.real("Cov_"+(i+1)+"_"+(j+1), -1000, 1000, precision, solver);
            inverseVariable[i][j] = VariableFactory.real("InverseCov_"+(i+1)+"_"+(j+1), -1000, 1000, precision, solver);
         }
      }
      
      computeCovarianceMatrix(observations, matrix, precision);
      
      RealVar[][] matrixM = new RealVar[n][n];
      for(int i = 0; i < matrix.length; i++){
         for(int j = 0; j < matrix.length; j++){
            matrixM[i][j] = VariableFactory.real("CovM_"+(i+1)+"_"+(j+1), -1000, 1000, precision, solver);
            solver.post(new RealConstraint(name, "{0}="+(M-1)+"*{1}", Ibex.HC4_NEWTON, new RealVar[]{matrixM[i][j],matrix[i][j]}));
         }
      }

      RealVar[] totals = new RealVar[observations[0].length];
      for(int i = 0; i < totals.length; i++){
         totals[i] = VariableFactory.real("Total_"+(i+1), -1000, 1000, precision, solver);
      }
      
      decomposeTotals(observations, totals);

      GaussJordan.decompose("GaussJordan", matrixM, inverseVariable);

      String statisticString = "";
      for(int i = 0; i < mu.length; i++){
         statisticString += "({"+(i+mu.length)+"}-"+M+"*{"+i+"})*(";
         for(int j = 0; j < mu.length; j++){
            statisticString += "({"+(j+mu.length)+"}-"+M+"*{"+j+"})*({"+(mu.length + totals.length + j*n+i)+(j == mu.length - 1 ? "})" : "})+");
         }
         statisticString += i == mu.length - 1 ? ")={"+(mu.length+totals.length+n*n)+"}" : ")+";
      }

      RealVar[] allVars = new RealVar[mu.length + totals.length + n*n + 1];
      System.arraycopy(mu, 0, allVars, 0, mu.length);
      System.arraycopy(totals, 0, allVars, mu.length, totals.length);
      System.arraycopy(flatten(inverseVariable), 0, allVars, mu.length + totals.length, n*n);
      allVars[allVars.length - 1] = statistic;

      solver.post(new RealConstraint(name, statisticString, Ibex.HC4_NEWTON, allVars));
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
   
   private static void decomposeTotals(RealVar[][] observations, RealVar[] totals){
      Solver solver = observations[0][0].getSolver();
      
      String totalStr = "";
      for(int i = 0; i < observations.length; i++){
         totalStr += "{"+i+ (i == observations.length - 1 ? "}={"+observations.length+"}" : "}+");
      }
      
      for(int j = 0; j < observations[0].length; j++){
         RealVar[] allVars = new RealVar[observations.length+1];
         System.arraycopy(getArray(j, observations), 0, allVars, 0, observations.length);
         allVars[observations.length] = totals[j];
         solver.post(new RealConstraint("Total_"+(j+1), totalStr, Ibex.HC4_NEWTON, allVars));    
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
