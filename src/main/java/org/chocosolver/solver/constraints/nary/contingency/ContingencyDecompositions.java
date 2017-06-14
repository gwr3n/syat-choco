package org.chocosolver.solver.constraints.nary.contingency;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;

public class ContingencyDecompositions {
   public static void decomposition(IntVar[] seriesA,
                                               IntVar[] seriesB,
                                               IntVar[][] binVariables,
                                               int[] binBounds){
      Solver solver = seriesA[0].getSolver();
      
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables.length; j++){        
            BoolVar[] valueBinVariables = new BoolVar[seriesA.length];
            for(int s = 0; s < seriesA.length; s++){
               valueBinVariables[s] = VariableFactory.bool("Value-Bin "+s+" ("+i+","+j+")", solver);
               
               solver.post(LogicalConstraintFactory.reification_reifiable(
                     valueBinVariables[s], 
                     LogicalConstraintFactory.and(
                           IntConstraintFactorySt.arithm(seriesA[s], ">=", binBounds[i]),
                           IntConstraintFactorySt.arithm(seriesA[s], "<", binBounds[i+1]),
                           IntConstraintFactorySt.arithm(seriesB[s], ">=", binBounds[j]),
                           IntConstraintFactorySt.arithm(seriesB[s], "<", binBounds[j+1])
                           )));
            }
            solver.post(IntConstraintFactorySt.sum(valueBinVariables, binVariables[i][j]));
         }
      }
   }
}
