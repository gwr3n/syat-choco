package org.chocosolver.solver.constraints.statistical.chisquare;

import java.math.BigDecimal;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;

public class ChiSquareFitPoisson {
   
   /**
    * This decomposition exploits Stirling's approximation to the factorial
    * 
    * @param name
    * @param observations
    * @param binCounts
    * @param binBounds
    * @param lambda
    * @param statistic
    * @param precision
    * @param allowOutOfBinObservations
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
      IntConstraintFactorySt.bincountsDecomposition(observations, 
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
    * @param name
    * @param observations
    * @param binCounts
    * @param binBounds
    * @param lambda
    * @param statistic
    * @param precision
    * @param allowOutOfBinObservations
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
      IntConstraintFactorySt.bincountsDecomposition(observations, 
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
