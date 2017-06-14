package org.chocosolver.samples.statistical.traffic;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

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

/*
 * *********************** DEPRECATED ***********************
 */

public class Traffic extends AbstractProblem {
   
   public RealVar lambdaIn;
   
   public RealVar lambdaAB;
   public RealVar lambdaAD;
   
   public RealVar lambdaBA;
   public RealVar lambdaBC;
   public RealVar lambdaBD;
   
   public RealVar lambdaCB;
   
   public RealVar lambdaDC;
   
   public RealVar lambdaOut;
   
   public RealVar totalChiSqStatistics;
   
   public RealVar chiSqStatisticsIn;
   
   public RealVar chiSqStatisticsBA;
   public RealVar chiSqStatisticsBC;
   public RealVar chiSqStatisticsBD;
   
   
   public RealVar chiSqStatisticsOut;
   
   
   public IntVar[] binVariablesIn;
   public IntVar[] binVariablesBA;
   public IntVar[] binVariablesBD;
   public IntVar[] binVariablesBC;
   public IntVar[] binVariablesOut;
   
   int[][] observations;
   int[] binBounds;
   double significance;
   
   public Traffic(int[][] observations,
                  int[] binBounds,
                  double significance){
      this.observations = observations;
      this.binBounds = binBounds.clone();
      this.significance = significance;
   }
   
   double precision = 0.1;
   
   ChiSquareDist chiSqDist;
   
   @Override
   public void createSolver() {
       solver = new Solver("Regression");
   }
   
   @Override
   public void buildModel() {
      lambdaIn = VariableFactory.real("LambdaIn", 10, 10, precision, solver);
      
      lambdaAB = VariableFactory.real("LambdaAB", 0, 20, precision, solver);
      lambdaAD = VariableFactory.real("LambdaAD", 0, 20, precision, solver);
      
      lambdaBA = VariableFactory.real("LambdaBA", 0, 10, precision, solver);
      lambdaBC = VariableFactory.real("LambdaBC", 0, 10, precision, solver);
      lambdaBD = VariableFactory.real("LambdaBD", 0, 20, precision, solver);
      
      lambdaCB = VariableFactory.real("LambdaCB", 0, 20, precision, solver);
      
      lambdaDC = VariableFactory.real("LambdaDC", 0, 20, precision, solver);
      
      lambdaOut = VariableFactory.real("LambdaOut", 10, 10, precision, solver);
      
      
      
      // ***NODES***
      
      // A
      RealVar[] varA = {lambdaIn, lambdaBA, lambdaAD, lambdaAB};
      String strA = "{0}+{1}-{2}-{3}=0";
      solver.post(new RealConstraint("Flow consdervation A",
            strA,
            Ibex.HC4_NEWTON, 
            varA
            ));
      
      // B
      RealVar[] varB = {lambdaAB, lambdaCB, lambdaBA, lambdaBC, lambdaBD};
      String strB = "{0}+{1}-{2}-{3}-{4}=0";
      solver.post(new RealConstraint("Flow consdervation B",
            strB,
            Ibex.HC4_NEWTON, 
            varB
            ));
      
      // C
      RealVar[] varC = {lambdaBC, lambdaDC, lambdaCB, lambdaOut};
      String strC = "{0}+{1}-{2}-{3}=0";
      solver.post(new RealConstraint("Flow consdervation C",
            strC,
            Ibex.HC4_NEWTON, 
            varC
            ));
      
      // D
      RealVar[] varD = {lambdaAD, lambdaBD, lambdaDC};
      String strD = "{0}+{1}-{2}=0";
      solver.post(new RealConstraint("Flow consdervation D",
            strD,
            Ibex.HC4_NEWTON, 
            varD
            ));
      
      // ***ARCS***
      
      this.chiSqDist = new ChiSquareDist(this.binBounds.length-2);
      
      // In
      
      IntVar[] observationsIn = IntStream.iterate(0, i -> i + 1)
                                         .limit(this.observations[0].length) 
                                         .mapToObj(i -> VF.fixed((int) this.observations[0][i], solver))
                                         .toArray(IntVar[]::new);
      
      binVariablesIn = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariablesIn.length; i++)
         binVariablesIn[i] = VariableFactory.bounded("Bin In"+(i+1), 0, this.observations[0].length, solver);
      
      chiSqStatisticsIn = VF.real("chiSqStatisticsIn", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTestIn", observationsIn, binVariablesIn, binBounds, lambdaIn, chiSqStatisticsIn, precision);
      
      // B,A
      
      IntVar[] observationsBA = IntStream.iterate(0, i -> i + 1)
                                         .limit(this.observations[1].length) 
                                         .mapToObj(i -> VF.fixed((int) this.observations[1][i], solver))
                                         .toArray(IntVar[]::new);
      
      binVariablesBA = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariablesBA.length; i++)
         binVariablesBA[i] = VariableFactory.bounded("Bin BA"+(i+1), 0, this.observations[1].length, solver);
      
      chiSqStatisticsBA = VF.real("chiSqStatisticsBA", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTestBA", observationsBA, binVariablesBA, binBounds, lambdaBA, chiSqStatisticsBA, precision);
      
      // B,C
      
      IntVar[] observationsBC = IntStream.iterate(0, i -> i + 1)
                                         .limit(this.observations[2].length) 
                                         .mapToObj(i -> VF.fixed((int) this.observations[2][i], solver))
                                         .toArray(IntVar[]::new);
      
      binVariablesBC = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariablesBC.length; i++)
         binVariablesBC[i] = VariableFactory.bounded("Bin BC"+(i+1), 0, this.observations[2].length, solver);
      
      chiSqStatisticsBC = VF.real("chiSqStatisticsBC", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTestBC", observationsBC, binVariablesBC, binBounds, lambdaBC, chiSqStatisticsBC, precision);
      
      // B,D
      
      IntVar[] observationsBD = IntStream.iterate(0, i -> i + 1)
                                         .limit(this.observations[3].length) 
                                         .mapToObj(i -> VF.fixed((int) this.observations[3][i], solver))
                                         .toArray(IntVar[]::new);
      
      binVariablesBD = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariablesBD.length; i++)
         binVariablesBD[i] = VariableFactory.bounded("Bin BD"+(i+1), 0, this.observations[3].length, solver);
      
      chiSqStatisticsBD = VF.real("chiSqStatisticsBD", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTestBD", observationsBD, binVariablesBD, binBounds, lambdaBD, chiSqStatisticsBD, precision);
      
      // Out
      
      IntVar[] observationsOut = IntStream.iterate(0, i -> i + 1)
                                         .limit(this.observations[4].length) 
                                         .mapToObj(i -> VF.fixed((int) this.observations[4][i], solver))
                                         .toArray(IntVar[]::new);
      
      binVariablesOut = new IntVar[this.binBounds.length-1];
      for(int i = 0; i < this.binVariablesOut.length; i++)
         binVariablesOut[i] = VariableFactory.bounded("Bin Out"+(i+1), 0, this.observations[4].length, solver);
      
      chiSqStatisticsOut = VF.real("chiSqStatisticsOut", 0, this.chiSqDist.inverseF(1-significance), precision, solver);
      ChiSquareFitPoisson.decomposition("chiSqTestOut", observationsOut, binVariablesOut, binBounds, lambdaOut, chiSqStatisticsOut, precision);
      
      // Objective
      //https://onlinecourses.science.psu.edu/stat414/node/171
      ChiSquareDist totalDist = new ChiSquareDist((this.binBounds.length-2)*5);
      totalChiSqStatistics = VF.real("totalChiSqStatistics", 0, totalDist.inverseF(1-significance), precision, solver);
      solver.post(new RealConstraint("objective", "{0}={1}+{2}+{3}", Ibex.HC4_NEWTON, new RealVar[]{totalChiSqStatistics, chiSqStatisticsIn, chiSqStatisticsBA, chiSqStatisticsOut}));
   }
   
   @Override
   public void configureSearch() {
      
      solver.set(
            new RealStrategy(new RealVar[]{lambdaIn,lambdaAB,lambdaAD,lambdaBA,lambdaBD,lambdaBC,lambdaCB,lambdaDC,lambdaOut}, new Cyclic(), new RealDomainMiddle())
            //new RealStrategy(realBinVariablesIn, new Cyclic(), new RealDomainMiddle()),
            //IntStrategyFactory.activity(binVariablesBC,1234)
       );
       //SearchMonitorFactory.limitTime(solver,10000);
   }
   
   @Override
   public void solve() {
     StringBuilder st = new StringBuilder();
     solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, totalChiSqStatistics, precision);
     //do{
        RealVar[] lambda = new RealVar[]{lambdaIn,lambdaAB,lambdaAD,lambdaBA,lambdaBC,lambdaBD,lambdaCB,lambdaDC,lambdaOut};
        st.append("---\n");
        if(solver.isFeasible() == ESat.TRUE) {
           Arrays.stream(lambda).forEach(l -> st.append(l.toString()+", "));
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
   
   public static int[] generateObservations(Random rnd, double lambda, int nbObservations){
      PoissonDist dist = new PoissonDist(lambda);
      return IntStream.iterate(1, i -> i + 1).map(i -> (int) dist.inverseF(rnd.nextDouble())).limit(nbObservations).toArray();
   }
   
   public static void fitMostLikelyParameters(){
      String[] str={"-log","SOLUTION"};
      
      int nbObservations = 50;
      
      Random rnd = new Random(1234);
      double lambda[] = {10,5,5,18,10};
      int[][] observations = IntStream.iterate(0, i -> i+1)
                                      .limit(lambda.length)
                                      .mapToObj(i -> generateObservations(rnd, lambda[i], nbObservations))
                                      .toArray(int[][]::new);
      System.out.println(Arrays.deepToString(observations));
      
      int bins = 10;
      int[] binBounds = IntStream.iterate(0, i -> i + 4).limit(bins+1).toArray();                                 
      double significance = 0.05;
   
      Traffic regression = new Traffic(observations, binBounds, significance);
      regression.execute(str);
   }
   
   public static void main(String[] args) {
      
      fitMostLikelyParameters();
      
   }
}
