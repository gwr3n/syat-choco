package org.chocosolver.samples.statistical.regression.poisson;

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
import umontreal.iro.lecuyer.probdist.PoissonDist;

public class RegressionPoisson extends AbstractProblem {
   
   public RealVar slope;
   public RealVar quadratic;
   public RealVar poissonRate;
   
   public RealVar[] residual;
   public IntVar[] binVariables;
   public RealVar[] realBinViews;
   
   double[] observations;
   double[] binBounds;
   double significance;
   
   public RegressionPoisson(double[] observations,
                     double[] binBounds,
                     double significance){
      this.observations = observations;
      this.binBounds = binBounds.clone();
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
      slope = VariableFactory.real("Slope", 0, 10, precision, solver);
      quadratic = VariableFactory.real("Quadratic", 0, 10, precision, solver);
      poissonRate = VariableFactory.real("Rate", 0, 50, precision, solver);
      
      residual = new RealVar[this.observations.length];
      for(int i = 0; i < this.residual.length; i++){
         residual[i] = VariableFactory.real("Residual "+(i+1), 0, this.binBounds[this.binBounds.length-2], precision, solver);
         String residualExp = "{0}="+this.observations[i]+"-{1}*"+(i+1.0)+"-"+(i+1.0)+"^{2}";
         solver.post(new RealConstraint("residual "+i,
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{residual[i],slope,quadratic}
               ));
      }
      
      binVariables = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariables.length; i++)
         binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observations.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binVariables.length-1);
      
      chiSqStatistics = VF.real("chiSqStatistics", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTest", residual, binVariables, binBounds, poissonRate, chiSqStatistics, precision);
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(new RealVar[]{slope,quadratic}, new Cyclic(), new RealDomainMiddle()),
            //new RealStrategy(residual, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{poissonRate}, new Cyclic(), new RealDomainMiddle())
            //IntStrategyFactory.minDom_LB(binVariables),
            //new RealStrategy(new RealVar[]{chiSqStatistics}, new Cyclic(), new RealDomainMiddle())
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
           st.append(slope.toString()+", "+quadratic.toString()+", "+poissonRate.toString()+"\n");
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
   
   public static double[] generateObservations(Random rnd, double truePoissonRate, int nbObservations){
      PoissonDist dist = new PoissonDist(truePoissonRate);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> i+Math.pow(i, 0.5)).map(i -> i + dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   public static void fitMostLikelyParameters(){
      String[] str={"-log","SOLUTION"};
      
      double[] observations = {4, 6, 10, 6, 10, 11, 16, 19, 18, 15, 16, 17, 16, 17, 20, 19, 24, 24, 
            26, 25, 23, 26, 25, 30, 28, 32, 32, 35, 32, 31, 37, 37, 40, 41, 39, 
            42, 42, 45, 42, 50, 46, 47, 49, 48, 49, 52, 53, 53, 55, 54};
      
      int nbObservations = 100;
      
      double truePoissonRate = 30;
      
      Random rnd = new Random(123);
      observations = generateObservations(rnd, truePoissonRate, nbObservations);
      Arrays.stream(observations).forEach(k -> System.out.print(k+"\t"));
      System.out.println();
      
      int bins = 10;
      double[] binBounds = DoubleStream.iterate(0, i -> i + 5).limit(bins+1).toArray();                                 
      double significance = 0.05;
   
      RegressionPoisson regression = new RegressionPoisson(observations, binBounds, significance);
      regression.execute(str);
   }
   
   public static void main(String[] args) {
      
      fitMostLikelyParameters();
      
   }
}
