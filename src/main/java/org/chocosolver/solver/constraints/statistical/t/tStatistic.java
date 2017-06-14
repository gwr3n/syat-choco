package org.chocosolver.solver.constraints.statistical.t;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.deviation.StandardError;
import org.chocosolver.solver.constraints.nary.mean.Mean;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

public class tStatistic {
   public static void decompose(String name,
                                IntVar[] observations,
                                RealVar mean,
                                RealVar t,
                                double precision){

      Solver solver = t.getSolver();

      int min = Arrays.stream(observations).mapToInt(o -> o.getLB()).min().getAsInt();
      int max = Arrays.stream(observations).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar sampleMean = VariableFactory.real("Mean", min, max, precision, solver);
      Mean.decompose("Mean", observations, sampleMean, precision);
      RealVar standardError = VariableFactory.real("StandardError", 0, max-min, precision, solver);
      StandardError.decompose("StandardError", observations, standardError, precision);
      
      String exp = "{0}=({1}-{2})/{3}";

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, new RealVar[]{t,sampleMean,mean,standardError}));
   }

   public static void decompose(String name,
                                RealVar[] observations,
                                RealVar mean,
                                RealVar t,
                                double precision){

      Solver solver = t.getSolver();

      double min = Arrays.stream(observations).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double max = Arrays.stream(observations).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar sampleMean = VariableFactory.real("Mean", min, max, precision, solver);
      Mean.decompose("Mean", observations, sampleMean);
      RealVar standardError = VariableFactory.real("StandardError", 0, (1/Math.sqrt(observations.length))*Math.sqrt(Math.pow(max-min,2)), precision, solver);
      StandardError.decompose("StandardError", observations, standardError, precision);

      String exp = "{0}=({1}-{2})/{3}";

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, new RealVar[]{t,sampleMean,mean,standardError}));
   }
}
