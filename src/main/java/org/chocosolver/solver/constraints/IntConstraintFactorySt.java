package org.chocosolver.solver.constraints;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.nary.contingency.ContingencyDecompositions;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositions;
import org.chocosolver.solver.constraints.statistical.ArithmeticSt;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions.DistributionVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import umontreal.iro.lecuyer.probdist.Distribution;

public class IntConstraintFactorySt extends IntConstraintFactory {

   public static ArithmeticSt arithmSt(IntVar[] VAR1, IntVar[] VAR2, String OP1, String OP2, double confidence) {
      Operator op1 = Operator.get(OP1);
      Operator op2 = Operator.get(OP2);
      return new ArithmeticSt(VAR1, VAR2, op1, op2, confidence);
   }

   public static ArithmeticSt arithmSt(IntVar[] VAR1, Distribution DIST, String OP1, double confidence) {
      Operator op1 = Operator.get(OP1);
      return new ArithmeticSt(VAR1, DIST, op1, confidence);
   }

   public static ArithmeticSt arithmSt(IntVar[] VAR1, DistributionVar DIST, String OP1, double confidence) {
      Operator op1 = Operator.get(OP1);
      return new ArithmeticSt(VAR1, DIST, op1, confidence);
   }
   
   public static void bincountsDecomposition(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds, BincountsDecompositionType decompositionType){
      switch(decompositionType){
      case Rossi2016:
         BincountsDecompositions.bincountsDecomposition1(valueVariables, binVariables, binBounds);
         break;
      case Agkun2016_1:
         BincountsDecompositions.bincountsDecomposition2(valueVariables, binVariables, binBounds);
         break;
      case Agkun2016_2_EQ:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, true);
         break;
      case Agkun2016_2_LE:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, false);
         break;
      default:
         throw new NullPointerException();
      }
   }
   
   public static void bincountsDecomposition(RealVar[] valueVariables, IntVar[] binVariables, double[] binBounds, double precision, BincountsDecompositionType decompositionType){
      switch(decompositionType){
      case Agkun2016_1:
         BincountsDecompositions.bincountsDecomposition2(valueVariables, binVariables, binBounds, precision);
         break;
      case Agkun2016_2_EQ:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, precision, true);
         break;
      case Agkun2016_2_LE:
         BincountsDecompositions.bincountsDecomposition3(valueVariables, binVariables, binBounds, precision, false);
         break;
      default:
         throw new NullPointerException();
      }
   }
   
   public static void contingencyDecomposition(IntVar[] seriesA, IntVar[] seriesB, IntVar[][] binVariables, int[][] binBounds, IntVar[] marginalsH, IntVar[] marginalsV){
      ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
   }
   
   public static void contingencyDecomposition(RealVar[] seriesA, RealVar[] seriesB, IntVar[][] binVariables, double[][] binBounds, IntVar[] marginalsH, IntVar[] marginalsV){
      ContingencyDecompositions.decompose(seriesA, seriesB, binVariables, binBounds, marginalsH, marginalsV);
   }
}
