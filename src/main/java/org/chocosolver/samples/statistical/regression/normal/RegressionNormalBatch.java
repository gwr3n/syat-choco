package org.chocosolver.samples.statistical.regression.normal;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitNormal;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;
import org.slf4j.LoggerFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.NormalDist;

public class RegressionNormalBatch extends AbstractProblem {
   
   private RealVar slope;
   private RealVar intercept;
   private RealVar mean;
   private RealVar stDeviation;
   
   private RealVar[] residual;
   private IntVar[] binVariables;
   
   private RealVar chiSqStatistics;
   
   private double[] residualBounds;
   
   private double[] observations;
   private double[] binBounds;
   private double significance;
   
   private double precision = 1.e-2;
   
   private ChiSquareDist chiSqDist;
   
   public RegressionNormalBatch(double[] observations,
                                double[] residualBounds,
                                double[] binBounds,
                                double significance){
      this.observations = observations;
      this.residualBounds = residualBounds;
      this.binBounds = binBounds;
      this.significance = significance;
   }
   
   @Override
   public void createSolver() {
       solver = new Solver("Regression Normal");
   }
   
   @Override
   public void buildModel() {
      slope = VariableFactory.real("Slope", -2, 2, precision, solver);
      intercept = VariableFactory.real("Intercept", -10, 10, precision, solver);
      mean = VariableFactory.real("Mean", 0, 0, precision, solver);
      stDeviation = VariableFactory.real("stDeviation", 0, 10, precision, solver);
      
      residual = new RealVar[this.observations.length];
      for(int i = 0; i < this.residual.length; i++){
         residual[i] = VariableFactory.real("Residual "+(i+1), this.residualBounds[0], this.residualBounds[1], precision, solver);
         String residualExp = "{0}="+this.observations[i]+"-{1}*"+(i+1)+"+{2}";
         solver.post(new RealConstraint("residual "+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{residual[i],slope,intercept}
               ));
      }
      
      binVariables = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariables.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observations.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitNormal.decomposition("chiSqTest", residual, binVariables, binBounds, mean, stDeviation, chiSqStatistics, precision, false);
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(new RealVar[]{slope,intercept,stDeviation}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistics, precision);
     //do{
        if(solver.isFeasible() == ESat.TRUE) {
           st.append(slope.getLB()+", "+intercept.getLB()+", "+mean.toString()+", "+stDeviation.getLB());
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     //LoggerFactory.getLogger("bench").info(st.toString());
     System.out.println(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   public static double[] generateObservations(Random rnd, double slope, double intercept, double normalMean, double normalstd, int nbObservations){
      NormalDist dist = new NormalDist(normalMean, normalstd);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> slope*i - intercept + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   public static void fitMostLikelyParameters(){
      String[] str={"-log","SILENT"};
      
      int nbObservations = 25;
      
      double slope = 1;
      double intercept = 5;
      double normalMean = 0;
      double normalstd = 5;
      
      double[] residualBounds = {normalMean-4*normalstd,normalMean+4*normalstd};
      
      Random rnd = new Random(1234);
      
      int batchSize = 30;
      double[][] observations = new double[batchSize][];
      
      for(int i = 0; i < batchSize; i++){
         observations[i] = generateObservations(rnd, slope, intercept, normalMean, normalstd, nbObservations);
         Arrays.stream(observations[i]).forEach(k -> System.out.print(k+", "));
         System.out.println();
      }
      
      for(int i = 0; i < batchSize; i++){
         int bins = 5;
         double[] binBounds = DoubleStream.iterate(-10, a -> a + 4).limit(bins + 1).toArray();                                 
         double significance = 0.05;
      
         RegressionNormalBatch regression = new RegressionNormalBatch(observations[i], residualBounds, binBounds, significance);
         regression.execute(str);
         try {
            regression.finalize();
         } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         regression = null;
         System.gc();
         
         try {
            Thread.sleep(100);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
   }
   
   public static void main(String[] args) {
      
      fitMostLikelyParameters();
      
   }
}
