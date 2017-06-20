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

public class StandardError {
   public static void decompose(String name,
                                IntVar[] observations,
                                RealVar standardError,
                                double precision){
      Solver solver = standardError.getSolver();

      int min = Arrays.stream(observations).mapToInt(o -> o.getLB()).min().getAsInt();
      int max = Arrays.stream(observations).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar mean = VariableFactory.real("Mean", min, max, precision, solver);
      Mean.decompose("MeanConstraint", observations, mean, precision);
      
      String exp = "(1/sqrt("+observations.length+"))*sqrt((";
      for(int i = 0; i < observations.length; i++){
         if(i < observations.length - 1)
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2+";
         else
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2)/"+(observations.length-1)+")={"+(i+1)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observations.length + 2];
      RealVar[] realObservations = VF.real(observations, precision);
      System.arraycopy(realObservations, 0, allRealVariables, 0, observations.length);
      allRealVariables[observations.length] = standardError;
      allRealVariables[observations.length+1] = mean;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }

   public static void decompose(String name,
                                RealVar[] observations,
                                RealVar standardError,
                                double precision){
      Solver solver = standardError.getSolver();

      double min = Arrays.stream(observations).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double max = Arrays.stream(observations).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar mean = VariableFactory.real("Mean", min, max, precision, solver);
      Mean.decompose("MeanConstraint", observations, mean, precision);

      String exp = "(1/sqrt("+observations.length+"))*sqrt((";
      for(int i = 0; i < observations.length; i++){
         if(i < observations.length - 1)
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2+";
         else
            exp += "({"+i+"}-{"+(observations.length+1)+"})^2)/"+(observations.length-1)+")={"+(i+1)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observations.length + 2];
      System.arraycopy(observations, 0, allRealVariables, 0, observations.length);
      allRealVariables[observations.length] = standardError;
      allRealVariables[observations.length+1] = mean;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
