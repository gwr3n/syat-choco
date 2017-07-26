package org.chocosolver.samples.statistical.regression;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactorySt;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsPropagatorType;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitNormal;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
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
import umontreal.iro.lecuyer.probdist.PoissonDist;

public class RegressionNormal extends AbstractProblem {
   
   public RealVar slope;
   public RealVar intercept;
   public RealVar mean;
   public RealVar stDeviation;
   
   public RealVar[] residual;
   public IntVar[] binVariables;
   public RealVar[] realBinViews;
   
   public double[] residualBounds;
   
   double[] observations;
   double[] binBounds;
   double significance;
   
   public RegressionNormal(double[] observations,
                           double[] residualBounds,
                           double[] binBounds,
                           double significance){
      this.observations = observations;
      this.residualBounds = residualBounds;
      this.binBounds = binBounds;
      this.significance = significance;
   }
   
   RealVar chiSqStatistics;
   RealVar[] allRV;
   
   double precision = 0.01;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Regression");
   }
   
   @Override
   public void buildModel() {
      slope = VariableFactory.real("Slope", 2, 2, precision, solver);
      intercept = VariableFactory.real("Intercept", -20, 20, precision, solver);
      mean = VariableFactory.real("Mean", 0, 0, precision, solver);
      /** CAREFUL, VARIANCE CANNOT BE TOO SMALL ***/
      stDeviation = VariableFactory.real("stDeviation", 1, 10, precision, solver);
      
      residual = new RealVar[this.observations.length];
      for(int i = 0; i < this.residual.length; i++){
         residual[i] = VariableFactory.real("Residual "+(i+1), this.residualBounds[0], this.residualBounds[1], precision, solver);
         String residualExp = "{0}="+this.observations[i]+"-{1}*"+i+"+{2}";
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
      
      //chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      chiSqStatistics = VF.real("chiSqStatistics", 0, 1000, precision, solver);
      ChiSquareFitNormal.decomposition("chiSqTest", residual, binVariables, binBounds, mean, stDeviation, chiSqStatistics, precision);
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(new RealVar[]{slope,intercept}, new Cyclic(), new RealDomainMiddle()),
            //new RealStrategy(residual, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{mean,stDeviation}, new Cyclic(), new RealDomainMiddle()),
            //IntStrategyFactory.minDom_LB(binVariables),
            new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistics, precision);
     //do{
        st.append("---\n");
        if(solver.isFeasible() == ESat.TRUE) {
           st.append(slope.toString()+", "+intercept.toString()+", "+mean.toString()+", "+stDeviation.toString()+"\n");
           for(int i = 0; i < residual.length; i++){
              st.append(residual[i].toString()+", ");
           }
           st.append("\n");
           for(int i = 0; i < binVariables.length; i++){
              st.append(binVariables[i].toString()+", ");
           }
           st.append("\n");
           st.append(chiSqStatistics.getLB()+" "+chiSqStatistics.getUB());
           st.append("\n");
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     LoggerFactory.getLogger("bench").info(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   public static double[] generateObservations(Random rnd, double normalMean, double normalstd, int nbObservations){
      NormalDist dist = new NormalDist(normalMean, normalstd);
      return DoubleStream.iterate(0, i -> i + 1).map(i -> 2*i - 10 + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   public static void fitMostLikelyParameters(){
      String[] str={"-log","SOLUTION"};
      
      double[] observations = {4, 6, 10, 6, 10, 11, 16, 19, 18, 15, 16, 17, 16, 17, 20, 19, 24, 24, 
            26, 25, 23, 26, 25, 30, 28, 32, 32, 35, 32, 31, 37, 37, 40, 41, 39, 
            42, 42, 45, 42, 50, 46, 47, 49, 48, 49, 52, 53, 53, 55, 54};
      
      int nbObservations = 50;
      
      double normalMean = 0;
      double normalstd = 3;
      
      double[] residualBounds = {normalMean-4*normalstd,normalMean+4*normalstd};
      
      Random rnd = new Random(123);
      observations = generateObservations(rnd, normalMean, normalstd, nbObservations);
      Arrays.stream(observations).forEach(k -> System.out.print(k+"\t"));
      System.out.println();
      
      int bins = 8;
      double[] binBounds = DoubleStream.iterate(-3, i -> i + 1).limit(bins + 1).toArray();                                 
      double significance = 0.05;
   
      RegressionNormal regression = new RegressionNormal(observations, residualBounds, binBounds, significance);
      regression.execute(str);
   }
   
   public static void main(String[] args) {
      
      fitMostLikelyParameters();
      
   }
}
