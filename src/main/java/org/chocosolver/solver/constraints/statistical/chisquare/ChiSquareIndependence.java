package org.chocosolver.solver.constraints.statistical.chisquare;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3900058/
 * 
 * @author Roberto Rossi
 *
 */

public class ChiSquareIndependence {
   public static void decomposition(String name,
                                    int observations,
                                    IntVar[][] binVariables,
                                    IntVar[] marginalsH,
                                    IntVar[] marginalsV,
                                    RealVar statistic,
                                    double precision){
      Solver solver = statistic.getSolver();
      
      IntVar[] flattenedBins = new IntVar[binVariables.length*binVariables[0].length];
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[0].length; j++){
            flattenedBins[binVariables[0].length*i + j] = binVariables[i][j]; 
         }
      }
      
      IntVar totalCount = VariableFactory.fixed("Total count", observations, solver);
      
      solver.post(IntConstraintFactorySt.sum(flattenedBins, totalCount));
            
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
}
