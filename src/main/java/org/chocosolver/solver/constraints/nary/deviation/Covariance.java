package org.chocosolver.solver.constraints.nary.deviation;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.mean.Mean;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

public class Covariance {
   public static void decompose(String name,
         IntVar[] observationsA,
         IntVar[] observationsB,
         RealVar variance,
         double precision){
      Solver solver = variance.getSolver();

      int minA = Arrays.stream(observationsA).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxA = Arrays.stream(observationsA).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar meanA = VariableFactory.real("Mean A", minA, maxA, precision, solver);
      Mean.decompose("MeanConstraint A", observationsA, meanA, precision);

      int minB = Arrays.stream(observationsB).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxB = Arrays.stream(observationsB).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar meanB = VariableFactory.real("Mean B", minB, maxB, precision, solver);
      Mean.decompose("MeanConstraint B", observationsB, meanB, precision);

      String exp = "(";
      for(int i = 0; i < observationsA.length; i++){
         if(i < observationsA.length - 1)
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"})+";
         else
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"}))/"+(observationsA.length-1)+"={"+(observationsA.length + observationsB.length)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observationsA.length + observationsB.length + 3];
      RealVar[] realObservationsA = VF.real(observationsA, precision);
      RealVar[] realObservationsB = VF.real(observationsB, precision);
      System.arraycopy(realObservationsA, 0, allRealVariables, 0, observationsA.length);
      System.arraycopy(realObservationsB, 0, allRealVariables, observationsA.length, observationsB.length);
      allRealVariables[observationsA.length + observationsB.length] = variance;
      allRealVariables[observationsA.length + observationsB.length + 1] = meanA;
      allRealVariables[observationsA.length + observationsB.length + 2] = meanB;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }

   public static void decompose(String name,
                                RealVar[] observationsA,
                                RealVar[] observationsB,
                                RealVar variance,
                                double precision){
      Solver solver = variance.getSolver();

      double minA = Arrays.stream(observationsA).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxA = Arrays.stream(observationsA).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar meanA = VariableFactory.real("Mean A", minA, maxA, precision, solver);
      Mean.decompose("MeanConstraint A", observationsA, meanA, precision);

      double minB = Arrays.stream(observationsB).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxB = Arrays.stream(observationsB).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar meanB = VariableFactory.real("Mean B", minB, maxB, precision, solver);
      Mean.decompose("MeanConstraint B", observationsB, meanB, precision);

      String exp = "(";
      for(int i = 0; i < observationsA.length; i++){
         if(i < observationsA.length - 1)
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"})+";
         else
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"}))/"+(observationsA.length-1)+"={"+(observationsA.length + observationsB.length)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observationsA.length + observationsB.length + 3];
      System.arraycopy(observationsA, 0, allRealVariables, 0, observationsA.length);
      System.arraycopy(observationsB, 0, allRealVariables, observationsA.length, observationsB.length);
      allRealVariables[observationsA.length + observationsB.length] = variance;
      allRealVariables[observationsA.length + observationsB.length + 1] = meanA;
      allRealVariables[observationsA.length + observationsB.length + 2] = meanB;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
