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
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.slf4j.LoggerFactory;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;
import umontreal.iro.lecuyer.probdist.PoissonDist;

public class Regression extends AbstractProblem {
   
   public RealVar slope;
   public RealVar quadratic;
   public RealVar poissonRate;
   
   public RealVar[] residual;
   public IntVar[] binVariables;
   public RealVar[] realBinViews;
   
   double[] observations;
   double[] binBounds;
   double significance;
   
   public Regression(double[] observations,
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
      for(int i = 0; i < this.observations.length; i++)
         residual[i] = VariableFactory.real("Residual "+(i+1), 0, this.binBounds[this.binBounds.length-2], precision, solver);
      
      for(int i = 0; i < this.residual.length; i++){
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
      
      realBinViews = VF.real(binVariables, precision);
      
      //solver.post(IntConstraintFactorySt.bincounts(residual, realBinViews, binBounds, BincountsPropagatorType.EQFast));
      IntConstraintFactorySt.bincountsDecomposition(residual, binVariables, binBounds, precision, BincountsDecompositionType.Agkun2016_1);
      
      allRV = new RealVar[realBinViews.length+2];
      System.arraycopy(realBinViews, 0, allRV, 0, realBinViews.length);
      allRV[realBinViews.length] = chiSqStatistics;
      allRV[realBinViews.length+1] = poissonRate;
      
      String[] targetFrequencies = new String[this.binBounds.length-1];
      for(int i = (int) binBounds[0] ; i < targetFrequencies.length + (int) binBounds[0] ; i++){
         targetFrequencies[i - (int) binBounds[0]] = i == 0 ?  
               this.observations.length+"*2.718^(-{"+(binVariables.length+1)+"})" : 
                  this.observations.length+"*{"+(binVariables.length+1)+"}^"+i+"*2.718^(-{"+(binVariables.length+1)+"})/(sqrt(2*3.14159*"+i+")*("+i+"/2.718)^"+i+")";
      }
      
      String chiSqExp = "";
      for(int i = 0; i < binVariables.length; i++)
         if(i == binVariables.length - 1)
            chiSqExp += "(({"+i+"}-("+targetFrequencies[i]+"))^2)/("+targetFrequencies[i]+")={"+(binVariables.length)+"}";
         else
            chiSqExp += "(({"+i+"}-("+targetFrequencies[i]+"))^2)/("+targetFrequencies[i]+")+";
      
      solver.post(new RealConstraint("chiSqTest", chiSqExp, Ibex.HC4_NEWTON, allRV));
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(new RealVar[]{poissonRate,slope,quadratic}, new Cyclic(), new RealDomainMiddle())
            //new RealStrategy(residual, new Cyclic(), new RealDomainMiddle()),
            //IntStrategyFactory.activity(binVariables,1234)
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     boolean solution = true;
     //solution = solver.findSolution();
     solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, chiSqStatistics, precision);
     //do{
        st.append("---\n");
        if(solution) {
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
           feasibleCount++;
        }else{
           st.append("No solution!");
        }
     //}while(solution = solver.nextSolution());
     LoggerFactory.getLogger("bench").info(st.toString());
   }

   @Override
   public void prettyOut() {
       
   }
   
   public static double[] generateObservations(Random rnd){
      PoissonDist dist = new PoissonDist(20);
      return DoubleStream.iterate(1, i -> i + 1).map(i -> i+Math.pow(i, 0.5)).map(i -> i + dist.inverseF(rnd.nextDouble())).limit(100).toArray();
   }
   
   static int feasibleCount = 0;
   
   public static void main(String[] args) {
      String[] str={"-log","SOLUTION"};
      
      double[] observations = {4, 6, 10, 6, 10, 11, 16, 19, 18, 15, 16, 17, 16, 17, 20, 19, 24, 24, 
            26, 25, 23, 26, 25, 30, 28, 32, 32, 35, 32, 31, 37, 37, 40, 41, 39, 
            42, 42, 45, 42, 50, 46, 47, 49, 48, 49, 52, 53, 53, 55, 54};
      
      Random rnd = new Random(123);
      observations = generateObservations(rnd);
      Arrays.stream(observations).forEach(k -> System.out.print(k+"\t"));
      System.out.println();
      
      int bins = 30;
      double[] binBounds = DoubleStream.iterate(5, i -> i + 1).limit(bins).toArray();                                 
      double significance = 0.05;
   
      Regression regression = new Regression(observations, binBounds, significance);
      regression.execute(str);
      
      /*Random rnd = new Random(1234);
      
      for(int k = 0; k < 1000; k++){
         observations = generateObservations(rnd);
         int bins = 16;
         double[] binBounds = DoubleStream.iterate(0, i -> i + 1).limit(bins).toArray();                                 
         double significance = 0.05;
      
         Regression regression = new Regression(observations, binBounds, significance);
         regression.execute(str);
         
         System.out.println(feasibleCount/(k+1.0) + "(" + k + ")");
         System.gc();
         try {
            Thread.sleep(200);
         } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
      }
      System.out.println(feasibleCount/1000.0);*/
   }
}
