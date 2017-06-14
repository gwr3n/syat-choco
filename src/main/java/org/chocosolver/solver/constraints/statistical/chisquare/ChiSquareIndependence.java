package org.chocosolver.solver.constraints.statistical.chisquare;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

public class ChiSquareIndependence {
   public static void decomposition(int values,
                                    IntVar[][] binVariables,
                                    int[] binBounds,
                                    RealVar statistic){
      Solver solver = statistic.getSolver();
      
      IntVar[] flattenedBins = new IntVar[binVariables.length*binVariables.length];
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables.length; j++){
            flattenedBins[i*j + i] = binVariables[i][j]; 
         }
      }
      
      IntVar totalCount = VariableFactory.bounded("Total count", 0, values, solver);
      
      solver.post(IntConstraintFactorySt.sum(flattenedBins, totalCount));
      
      IntVar[] marginalsH = VariableFactory.boundedArray("Marginals H", binVariables.length, 0, values, solver);
      
      IntVar[] marginalsV = VariableFactory.boundedArray("Marginals V", binVariables.length, 0, values, solver);
      
      
      
   }
}
