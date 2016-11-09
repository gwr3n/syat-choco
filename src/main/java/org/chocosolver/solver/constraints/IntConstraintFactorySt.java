package org.chocosolver.solver.constraints;

import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.statistical.ArithmeticSt;
import org.chocosolver.solver.constraints.statistical.bincounts.BincountsSt;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.statistical.distributions.DistributionVar;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.statistical.sum.ScalarSt;
import umontreal.iro.lecuyer.probdist.Distribution;

public class IntConstraintFactorySt extends IntConstraintFactory {
   public static ArithmeticSt arithmSt(IntVar[] VAR, String OP1, String OP2, double confidence, int CSTE) {
      Operator op1 = Operator.get(OP1);
      Operator op2 = Operator.get(OP2);
      return new ArithmeticSt(VAR, op1, op2, CSTE, confidence);
   }

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

   public static Constraint scalarSt(IntVar[] VARS, int[][] SAMPLES, int[] SCALAR, double confidence) {
      return new ScalarSt(VARS, SAMPLES, SCALAR, confidence);
   }

   public static Constraint frequencySt(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      return new BincountsSt(valueVariables, binVariables, binBounds);
   }
}
