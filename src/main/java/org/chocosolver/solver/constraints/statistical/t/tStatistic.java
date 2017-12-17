package org.chocosolver.solver.constraints.statistical.t;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.deviation.PooledVariance;
import org.chocosolver.solver.constraints.nary.deviation.StandardError;
import org.chocosolver.solver.constraints.nary.mean.Mean;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

public class tStatistic {
   
   /**
    * One sample
    */
   
   public static void decompose(IntVar[] observations,
                                RealVar mean,
                                RealVar t,
                                double precision){

      Solver solver = t.getSolver(); 
      
      int min = Arrays.stream(observations).mapToInt(o -> o.getLB()).min().getAsInt();
      int max = Arrays.stream(observations).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar sampleMean = VariableFactory.real("SampleMean", min, max, precision, solver);
      Mean.decompose("SampleMeanDecomposition", observations, sampleMean, precision);
      RealVar standardError = VariableFactory.real("StandardError", 0, max-min, precision, solver);
      StandardError.decompose("StandardErrorDecomposition", observations, standardError, precision);
      
      String exp = "{0}+[-"+String.format("%f", precision)+","+String.format("%f", precision)+"]=({1}-{2})/{3}";

      solver.post(new RealConstraint(exp, new RealVar[]{t,sampleMean,mean,standardError}));
   }

   public static void decompose(RealVar[] observations,
                                RealVar mean,
                                RealVar t,
                                double precision){
      
      Solver solver = t.getSolver();

      double min = Arrays.stream(observations).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double max = Arrays.stream(observations).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar sampleMean = VariableFactory.real("Mean", min, max, precision, solver);
      Mean.decompose("Mean", observations, sampleMean, precision);
      RealVar standardError = VariableFactory.real("StandardError", 0, (1/Math.sqrt(observations.length))*Math.sqrt(Math.pow(max-min,2)), precision, solver);
      StandardError.decompose("StandardError", observations, standardError, precision);

      String exp = "{0}+[-"+String.format("%f", precision)+","+String.format("%f", precision)+"]=({1}-{2})/{3}";

      solver.post(new RealConstraint(exp, new RealVar[]{t,sampleMean,mean,standardError}));
   }
   
   /**
    * Two samples
    */
   
   public static void decompose(IntVar[] observationsA,
                                IntVar[] observationsB,
                                RealVar t,
                                double precision){
      
      Solver solver = t.getSolver();

      int minA = Arrays.stream(observationsA).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxA = Arrays.stream(observationsA).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar sampleMeanA = VariableFactory.real("SampleMeanA", minA, maxA, precision, solver);
      Mean.decompose("SampleMeanConstraintA", observationsA, sampleMeanA, precision);
      
      int minB = Arrays.stream(observationsB).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxB = Arrays.stream(observationsB).mapToInt(o -> o.getUB()).max().getAsInt();
      
      RealVar sampleMeanB = VariableFactory.real("SampleMeanB", minB, maxB, precision, solver);
      Mean.decompose("SampleMeanConstraintB", observationsB, sampleMeanB, precision);
      
      RealVar pooledVariance = VariableFactory.real("PooledVariance", 0, Math.pow(Math.max(maxA, maxB) - Math.min(minA, minB),2), precision, solver);
      PooledVariance.decompose("PooledVarianceDecomposition", observationsA, observationsB, pooledVariance, precision);

      String exp = "{0}+[-"+String.format("%f", precision)+","+String.format("%f", precision)+"]=({1}-{2})/sqrt({3}*(1/"+observationsA.length+"+1/"+observationsB.length+"))";

      solver.post(new RealConstraint(exp, new RealVar[]{t,sampleMeanA, sampleMeanB, pooledVariance}));
   }

   public static void decompose(RealVar[] observationsA,
                                RealVar[] observationsB,
                                RealVar t,
                                double precision){
      
      Solver solver = t.getSolver();
      
      double minA = Arrays.stream(observationsA).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxA = Arrays.stream(observationsA).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar sampleMeanA = VariableFactory.real("SampleMeanA", minA, maxA, precision, solver);
      Mean.decompose("SampleMeanConstraintA", observationsA, sampleMeanA, precision);
      
      double minB = Arrays.stream(observationsB).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxB = Arrays.stream(observationsB).mapToDouble(o -> o.getUB()).max().getAsDouble();
      
      RealVar sampleMeanB = VariableFactory.real("SampleMeanB", minB, maxB, precision, solver);
      Mean.decompose("SampleMeanConstraintB", observationsB, sampleMeanB, precision);
      
      RealVar pooledVariance = VariableFactory.real("PooledVariance", 0, Math.pow(Math.max(maxA, maxB) - Math.min(minA, minB),2), precision, solver);
      PooledVariance.decompose("PooledVarianceDecomposition", observationsA, observationsB, pooledVariance, precision);

      String exp = "{0}+[-"+String.format("%f", precision)+","+String.format("%f", precision)+"]=({1}-{2})/sqrt({3}*(1/"+observationsA.length+"+1/"+observationsB.length+"))";

      solver.post(new RealConstraint(exp, new RealVar[]{t,sampleMeanA, sampleMeanB, pooledVariance}));
   }
}
