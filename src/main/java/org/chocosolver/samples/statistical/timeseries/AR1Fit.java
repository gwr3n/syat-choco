package org.chocosolver.samples.statistical.timeseries;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.PoissonDist;

public class AR1Fit extends AbstractProblem {
   
   public RealVar parameter;
   public RealVar constant;
   
   public RealVar lambda;
   
   public RealVar[] residual;
   
   public IntVar[] binVariable;
   
   double[] observation;
   double[] binBound;
   
   double significance;
   
   public AR1Fit(double[] observation,
                 double[] binBound,
                 double significance){
      this.observation = observation;
      this.binBound = binBound;
      this.significance = significance;
   }
   
   RealVar chiSqStatistic;
   
   RealVar[] allRV;
   
   double precision = 0.05;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("AR(1)");
   }
   
   @Override
   public void buildModel() {
      parameter = VariableFactory.real("Parameter", 0, 2, precision, solver);
      constant = VariableFactory.real("Constant", 0, 20, precision, solver);
      
      residual = new RealVar[this.observation.length];
      for(int i = 0; i < this.residual.length; i++){
         residual[i] = VariableFactory.real("Residual "+(i+1), 0, Arrays.stream(observation).max().getAsDouble(), precision, solver);
         String residualExp = "{0}="+this.observation[i]+"-{1}" + ((i > 0) ? "-{2}*"+this.observation[i-1] : "");
         solver.post(new RealConstraint("residual constraint "+(i+1),
               residualExp,
               Ibex.HC4_NEWTON, 
               new RealVar[]{residual[i],constant,parameter}
               ));
      }
      
      binVariable = new IntVar[this.binBound.length-1];
      for(int i = 0; i < this.binVariable.length; i++)
         binVariable[i] = VariableFactory.bounded("Bin "+(i+1), 0, this.observation.length, solver);
      
      this.chiSqDist = new ChiSquareDist(this.binBound.length - 1);
      
      lambda = VariableFactory.real("lambda 1", 0, 20, precision, solver);
      chiSqStatistic = VF.real("chiSqStatistic", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTest", residual, binVariable, binBound, lambda, chiSqStatistic, precision, false);
   }
   
   @Override
   public void configureSearch() {
      /*solver.plugMonitor(new IMonitorSolution() {
         public void onSolution() {
            // DO SOMETHING
         }
      });*/
      solver.set(
            new RealStrategy(new RealVar[]{constant,parameter,lambda}, new Cyclic(), new RealDomainMiddle()),
            new RealStrategy(new RealVar[]{chiSqStatistic}, new Cyclic(), new RealDomainMiddle())
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, lambda, precision);
     //do{
        st.append("---\n");
        if(solver.isFeasible() == ESat.TRUE) {
           st.append("Curve: "+parameter.toString()+" "+constant.toString());
           st.append("\n");
           st.append("Lambda: "+lambda.toString());
           st.append("\n");
           st.append(chiSqStatistic.getLB()+" "+chiSqStatistic.getUB());
           st.append("\n");
           feasibleCount++;
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     System.out.println(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   public static double[] generateObservations(Random rnd, double c, double phi, double truePoissonRate, int nbObservations){
      PoissonDist dist = new PoissonDist(truePoissonRate);
      double[] observations = new double[nbObservations]; 
      double[] poisson = new double[nbObservations];
      double zt = 0;
      for(int i = 0; i < nbObservations; i++){
         poisson[i] = dist.inverseF(rnd.nextDouble()) + 0.5;
         zt = c + poisson[i] + phi*zt;
         observations[i] = zt;
      }
      return observations;
   }
   
   static int feasibleCount = 0;
   
   public static void fitMostLikelyParameters(){
      String[] str={"-log","SOLUTION"};
      
      double[] observations;
      
      int nbObservations = 100;
      
      double replications = 1;
      
      Random rnd = new Random(123);
      
      for(int k = 0; k < replications; k++){
      
         double truePoissonRate = 5;
         
         observations = generateObservations(rnd, 5, 0.5, truePoissonRate, nbObservations);
         
         int bins = 15;
         double[] binBounds = DoubleStream.iterate(0, i -> i + 1).limit(bins).toArray();
         double significance = 0.05;
      
         AR1Fit regression = new AR1Fit(observations, binBounds, significance);
         regression.execute(str);
         try {
            regression.finalize();
         } catch (Throwable e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
         regression = null;
         System.gc();
         
         System.out.println(feasibleCount/(k+1.0) + "(" + k + ")");
         try {
            Thread.sleep(1000);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      System.out.println(feasibleCount/replications);
   }
   
   public static void main(String[] args) {
      
      fitMostLikelyParameters();
      
   }
}
