package org.chocosolver.solver.constraints.nary.contingency;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.LogicalConstraintFactory;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

public class ContingencyDecompositions {
   public static void decompose(IntVar[] seriesA,
                                IntVar[] seriesB,
                                IntVar[][] binVariables,
                                int[][] binBounds,
                                IntVar[] marginalsH,
                                IntVar[] marginalsV){
      Solver solver = seriesA[0].getSolver();
      
      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[i].length; j++){        
            BoolVar[] valueBinVariables = new BoolVar[seriesA.length];
            for(int s = 0; s < seriesA.length; s++){
               valueBinVariables[s] = VariableFactory.bool("Value-Bin "+s+" ("+i+","+j+")", solver);
               
               solver.post(LogicalConstraintFactory.reification_reifiable(
                     valueBinVariables[s], 
                     LogicalConstraintFactory.and(
                           SyatConstraintFactory.arithm(seriesA[s], ">=", binBounds[0][i]),
                           SyatConstraintFactory.arithm(seriesA[s], "<", binBounds[0][i+1]),
                           SyatConstraintFactory.arithm(seriesB[s], ">=", binBounds[1][j]),
                           SyatConstraintFactory.arithm(seriesB[s], "<", binBounds[1][j+1])
                           )));
            }
            solver.post(SyatConstraintFactory.sum(valueBinVariables, binVariables[i][j]));
         }
      }
      
      for(int i = 0; i < marginalsH.length; i++){
         solver.post(SyatConstraintFactory.sum(binVariables[i], marginalsH[i]));
      }
      
      for(int j = 0; j < marginalsV.length; j++){
         IntVar[] tempVArray = new IntVar[binVariables[0].length];
         for(int i = 0; i < marginalsH.length; i++){
            tempVArray[i] = binVariables[i][j];
         }
         solver.post(SyatConstraintFactory.sum(tempVArray, marginalsV[j]));
      }
   }
   
   public static void decompose(RealVar[] seriesA,
                                RealVar[] seriesB,
                                IntVar[][] binVariables,
                                double[][] binBounds,
                                IntVar[] marginalsH,
                                IntVar[] marginalsV){
      Solver solver = seriesA[0].getSolver();

      for(int i = 0; i < binVariables.length; i++){
         for(int j = 0; j < binVariables[i].length; j++){        
            BoolVar[] valueBinVariables = new BoolVar[seriesA.length];
            for(int s = 0; s < seriesA.length; s++){
               valueBinVariables[s] = VariableFactory.bool("Value-Bin "+s+" ("+i+","+j+")", solver);

               String constraintSeriesAGEStr = "{0}>="+binBounds[0][i];
               String constraintSeriesALStr = "{0}<"+binBounds[0][i+1];
               String constraintSeriesBGEStr = "{0}>="+binBounds[1][j];
               String constraintSeriesBLStr = "{0}<"+binBounds[1][j+1];
               
               RealConstraint constraintAGE = new RealConstraint("constraintAGE_"+i+"_"+j+"_"+s,constraintSeriesAGEStr,Ibex.HC4_NEWTON, new RealVar[]{seriesA[s]});
               RealConstraint constraintAL = new RealConstraint("constraintAL_"+i+"_"+j+"_"+s,constraintSeriesALStr,Ibex.HC4_NEWTON, new RealVar[]{seriesA[s]});
               RealConstraint constraintBGE = new RealConstraint("constraintBGE_"+i+"_"+j+"_"+s,constraintSeriesBGEStr,Ibex.HC4_NEWTON, new RealVar[]{seriesB[s]});
               RealConstraint constraintBL = new RealConstraint("constraintBL_"+i+"_"+j+"_"+s,constraintSeriesBLStr,Ibex.HC4_NEWTON, new RealVar[]{seriesB[s]});
               
               
               solver.post(LogicalConstraintFactory.reification_reifiable(
                     valueBinVariables[s], 
                     LogicalConstraintFactory.and(
                           constraintAGE,
                           constraintAL,
                           constraintBGE,
                           constraintBL
                           )));
            }
            solver.post(SyatConstraintFactory.sum(valueBinVariables, binVariables[i][j]));
         }
      }

      for(int i = 0; i < marginalsH.length; i++){
         solver.post(SyatConstraintFactory.sum(binVariables[i], marginalsH[i]));
      }

      for(int j = 0; j < marginalsV.length; j++){
         IntVar[] tempVArray = new IntVar[binVariables[0].length];
         for(int i = 0; i < marginalsH.length; i++){
            tempVArray[i] = binVariables[i][j];
         }
         solver.post(SyatConstraintFactory.sum(tempVArray, marginalsV[j]));
      }
   }
}
