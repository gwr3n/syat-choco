package org.chocosolver.solver.constraints.statistical.chisquare;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;

public class ChiSquareFitEmpirical {
   public static void decomposition(String name,
                                    RealVar[] observations, 
                                    IntVar[] binCounts, 
                                    double[] binBounds, 
                                    IntVar[] targetFrequencies,
                                    RealVar statistic, 
                                    double precision){
      
      Solver solver = statistic.getSolver();

      //RealVar[] realBinViews = VF.real(binCounts, precision);
      //solver.post(IntConstraintFactorySt.bincounts(observations, realBinViews, binBounds, BincountsPropagatorType.EQFast));
      SyatConstraintFactory.bincountsDecomposition(observations, binCounts, binBounds, precision, BincountsDecompositionType.Agkun2016_1);

      RealVar[] realBinCounts = VF.real(binCounts, precision);
      RealVar[] realTargetFrequencies = VF.real(targetFrequencies, precision);

      String chiSqExp = "";
      for(int i = 0; i < binCounts.length; i++)
         if(i == binCounts.length - 1)
            chiSqExp += "(({"+i+"}-{"+(i+binCounts.length)+"})^2)/{"+(i+binCounts.length)+"}={"+(2*binCounts.length)+"}";
         else
            chiSqExp += "(({"+i+"}-{"+(i+binCounts.length)+"})^2)/{"+(i+binCounts.length)+"}+";

      RealVar[] allRealVariables = new RealVar[realBinCounts.length*2 + 1];
      System.arraycopy(realBinCounts, 0, allRealVariables, 0, realBinCounts.length);
      System.arraycopy(realTargetFrequencies, 0, allRealVariables, realBinCounts.length, realTargetFrequencies.length);
      allRealVariables[2*realBinCounts.length] = statistic;
      solver.post(new RealConstraint(name, chiSqExp, Ibex.HC4_NEWTON, allRealVariables));
   }
   
   public static void decomposition(String name,
                                    IntVar[] observations, 
                                    IntVar[] binCounts, 
                                    int[] binBounds, 
                                    IntVar[] targetFrequencies,
                                    RealVar statistic, 
                                    double precision){
      
      Solver solver = statistic.getSolver();

      //solver.post(IntConstraintFactorySt.bincounts(observations, binCounts, binBounds, BincountsPropagatorType.EQFast));
      SyatConstraintFactory.bincountsDecomposition(observations, binCounts, binBounds, BincountsDecompositionType.Agkun2016_1);

      RealVar[] realBinCounts = VF.real(binCounts, precision);
      RealVar[] realTargetFrequencies = VF.real(targetFrequencies, precision);

      String chiSqExp = "";
      for(int i = 0; i < binCounts.length; i++)
         if(i == binCounts.length - 1)
            chiSqExp += "(({"+i+"}-{"+(i+binCounts.length)+"})^2)/{"+(i+binCounts.length)+"}={"+(2*binCounts.length)+"}";
         else
            chiSqExp += "(({"+i+"}-{"+(i+binCounts.length)+"})^2)/{"+(i+binCounts.length)+"}+";

      RealVar[] allRealVariables = new RealVar[realBinCounts.length*2 + 1];
      System.arraycopy(realBinCounts, 0, allRealVariables, 0, realBinCounts.length);
      System.arraycopy(realTargetFrequencies, 0, allRealVariables, realBinCounts.length, realTargetFrequencies.length);
      allRealVariables[2*realBinCounts.length] = statistic;
      solver.post(new RealConstraint(name, chiSqExp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
