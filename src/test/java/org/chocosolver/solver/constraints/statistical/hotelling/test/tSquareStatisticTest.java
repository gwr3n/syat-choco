package org.chocosolver.solver.constraints.statistical.hotelling.test;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.hotelling.tSquareStatistic;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import umontreal.iro.lecuyer.randvar.NormalGen;
import umontreal.iro.lecuyer.randvarmulti.MultinormalCholeskyGen;
import umontreal.iro.lecuyer.rng.MRG32k3a;

public class tSquareStatisticTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      Thread.sleep(1000);
      System.gc();
   }

   @Test
   public void testKnownSigma() {
      String[] str={"-log","SOLUTION"};
      double[] mu = {1, 1, 1};
      double[][] sigma = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      
      int M = 50;
      
      MRG32k3a rng = new MRG32k3a();
      rng.setSeed(new long[]{1,2,3,4,5,6});
      double[][] observations = generateObservations(rng, mu, sigma, M);
      
      double[][] muDomains = {{1,1},{1,1},{1,1}};
      double[] statistic = {-1000,1000};
      
      ScoreRealKnownSigma scoreReal = new ScoreRealKnownSigma(sigma, muDomains, observations, statistic);
      scoreReal.execute(str);
   }
   
   @Test
   public void testUnknownSigma() {
      String[] str={"-log","SOLUTION"};
      double[] mu = {1, 1, 1};
      double[][] sigma = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      
      int M = 50;
      
      MRG32k3a rng = new MRG32k3a();
      rng.setSeed(new long[]{1,2,3,4,5,6});
      double[][] observations = generateObservations(rng, mu, sigma, M);
      
      double[][] muDomains = {{1,1},{1,1},{1,1}};
      double[] statistic = {-1000,1000};
      
      ScoreRealUnknownSigma scoreReal = new ScoreRealUnknownSigma(muDomains, observations, statistic);
      scoreReal.execute(str);
   }
   
   @Test
   public void testKnownSigmaVariableObservations() {
      String[] str={"-log","SOLUTION"};
      double[] mu = {1, 1, 1};
      double[][] sigma = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      
      int M = 50;
      
      MRG32k3a rng = new MRG32k3a();
      rng.setSeed(new long[]{1,2,3,4,5,6});
      double[][] observations = generateObservations(rng, mu, sigma, M);
      
      double[][] muDomains = {{1,1},{1,1},{1,1}};
      double[] statistic = {-1000,1000};
      
      ScoreRealKnownSigmaVariableObservations scoreReal = new ScoreRealKnownSigmaVariableObservations(sigma, muDomains, observations, statistic);
      scoreReal.execute(str);
   }
   
   @Test
   public void testUnknownSigmaVariableObservations() {
      String[] str={"-log","SOLUTION"};
      double[] mu = {1, 1, 1};
      double[][] sigma = new double[][]{
         { 1.0, 0.1, 0.2 },
         { 0.1, 1.0, 0.1 },
         { 0.2, 0.1, 1.0 }
      };
      
      int M = 50;
      
      MRG32k3a rng = new MRG32k3a();
      rng.setSeed(new long[]{1,2,3,4,5,6});
      double[][] observations = generateObservations(rng, mu, sigma, M);
      
      double[][] muDomains = {{1,1},{1,1},{1,1}};
      double[] statistic = {-1000,1000};
      
      ScoreRealUnknownSigmaVariableObservations scoreReal = new ScoreRealUnknownSigmaVariableObservations(muDomains, observations, statistic);
      scoreReal.execute(str);
   }
   
   private static double[][] generateObservations(MRG32k3a rng, double[] mu, double[][] sigma, int nbObservations){
      NormalGen gen = new NormalGen(rng);
      MultinormalCholeskyGen dist = new MultinormalCholeskyGen(gen, mu, sigma);
      double[][] observations = new double[nbObservations][mu.length];
      dist.nextArrayOfPoints(observations, 0, nbObservations);
      return observations;
   }

   class ScoreRealKnownSigma extends AbstractProblem {
      public RealVar[] muVariable;
      public RealVar statisticVariable;
      
      double[][] sigma;
      
      double[][] muDomains;
      double[][] observations;
      double[] statistic;
      
      double precision = 1.e-4;
      
      public ScoreRealKnownSigma(double[][] sigma,
                       double[][] muDomains,
                       double[][] observations,
                       double[] statistic){
         this.sigma = sigma;
         
         this.muDomains = muDomains;
         this.observations = observations;
         this.statistic = statistic;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         muVariable = new RealVar[this.muDomains.length];
         for(int i = 0; i < this.muVariable.length; i++)
            muVariable[i] = VariableFactory.real("Mu "+(i+1), muDomains[i][0], muDomains[i][1], precision, solver);
         
         statisticVariable = VF.real("score", statistic[0], statistic[1], precision, solver);
         
         tSquareStatistic.decompose("scoreConstraint", muVariable, sigma, observations, statisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         RealStrategy strat = new RealStrategy(muVariable, new Cyclic(), new RealDomainMiddle());
         solver.set(strat);
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < muVariable.length; i++){
                 st.append("("+muVariable[i].getLB()+","+muVariable[i].getUB()+"), ");
              }
              st.append("\n");
              st.append(statisticVariable.getLB()+" "+statisticVariable.getUB());
              st.append("\n");
              
              assertTrue(statisticVariable.getLB() <= 2.64 && statisticVariable.getLB() >= 2.63);
              assertTrue(statisticVariable.getUB() <= 2.64 && statisticVariable.getUB() >= 2.63);
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        LoggerFactory.getLogger("bench").info(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
   class ScoreRealUnknownSigma extends AbstractProblem {
      public RealVar[] muVariable;
      public RealVar statisticVariable;
      
      double[][] muDomains;
      double[][] observations;
      double[] statistic;
      
      double precision = 1.e-4;
      
      public ScoreRealUnknownSigma(
                       double[][] muDomains,
                       double[][] observations,
                       double[] statistic){
         
         this.muDomains = muDomains;
         this.observations = observations;
         this.statistic = statistic;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         muVariable = new RealVar[this.muDomains.length];
         for(int i = 0; i < this.muVariable.length; i++)
            muVariable[i] = VariableFactory.real("Mu "+(i+1), muDomains[i][0], muDomains[i][1], precision, solver);
         
         statisticVariable = VF.real("score", statistic[0], statistic[1], precision, solver);
         
         tSquareStatistic.decompose("scoreConstraint", muVariable, observations, statisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         RealStrategy strat = new RealStrategy(muVariable, new Cyclic(), new RealDomainMiddle());
         solver.set(strat);
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < muVariable.length; i++){
                 st.append("("+muVariable[i].getLB()+","+muVariable[i].getUB()+"), ");
              }
              st.append("\n");
              st.append(statisticVariable.getLB()+" "+statisticVariable.getUB());
              st.append("\n");
              
              assertTrue(statisticVariable.getLB() <= 2.71 && statisticVariable.getLB() >= 2.70);
              assertTrue(statisticVariable.getUB() <= 2.71 && statisticVariable.getUB() >= 2.70);
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        LoggerFactory.getLogger("bench").info(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
   class ScoreRealKnownSigmaVariableObservations extends AbstractProblem {
      public RealVar[][] sigmaVariable;
      public RealVar[] muVariable;
      public RealVar[][] observationVariable;
      public RealVar statisticVariable;
      
      double[][] sigma;
      double[][] muDomains;
      double[][] observations;
      double[] statistic;
      
      double precision = 1.e-4;
      
      public ScoreRealKnownSigmaVariableObservations(double[][] sigma,
                                                     double[][] muDomains,
                                                     double[][] observations,
                                                     double[] statistic){
         this.sigma = sigma;
         this.muDomains = muDomains;
         this.observations = observations;
         this.statistic = statistic;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         muVariable = new RealVar[this.muDomains.length];
         for(int i = 0; i < this.muVariable.length; i++)
            muVariable[i] = VariableFactory.real("Mu "+(i+1), muDomains[i][0], muDomains[i][1], precision, solver);
         
         sigmaVariable = new RealVar[this.sigma.length][this.sigma[0].length];
         for(int i = 0; i < this.sigmaVariable.length; i++){
            for(int j = 0; j < this.sigmaVariable[i].length; j++){
               sigmaVariable[i][j] = VariableFactory.real("Sigma_"+(i+1)+"_"+(j+1), sigma[i][j], sigma[i][j], precision, solver);
            }
         }
         
         observationVariable = new RealVar[this.observations.length][this.observations[0].length];
         for(int i = 0; i < this.observations.length; i++){
            for(int j = 0; j < this.observations[i].length; j++){
               observationVariable[i][j] = VariableFactory.real("Obs_"+(i+1)+"_"+(j+1), observations[i][j], observations[i][j], precision, solver);
            }
         }
         
         statisticVariable = VF.real("score", statistic[0], statistic[1], precision, solver);
         
         tSquareStatistic.decompose("scoreConstraint", muVariable, observationVariable, sigmaVariable, statisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         RealStrategy strat1 = new RealStrategy(muVariable, new Cyclic(), new RealDomainMiddle());
         RealStrategy strat2 = new RealStrategy(flatten(observationVariable), new Cyclic(), new RealDomainMiddle());
         solver.set(strat1, strat2);
      }
      
      private RealVar[] flatten(RealVar[][] matrix){
         RealVar[] array = new RealVar[matrix.length*matrix[0].length];
         for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
               array[i*matrix[i].length+j] = matrix[i][j];
            }
         }
         return array;
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < muVariable.length; i++){
                 st.append("("+muVariable[i].getLB()+","+muVariable[i].getUB()+"), ");
              }
              st.append("\n");
              st.append(statisticVariable.getLB()+" "+statisticVariable.getUB());
              st.append("\n");
              
              assertTrue(statisticVariable.getLB() <= 2.64 && statisticVariable.getLB() >= 2.63);
              assertTrue(statisticVariable.getUB() <= 2.64 && statisticVariable.getUB() >= 2.63);
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        LoggerFactory.getLogger("bench").info(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
   class ScoreRealUnknownSigmaVariableObservations extends AbstractProblem {
      public RealVar[] muVariable;
      public RealVar[][] observationVariable;
      public RealVar statisticVariable;
      
      double[][] muDomains;
      double[][] observations;
      double[] statistic;
      
      double precision = 1.e-4;
      
      public ScoreRealUnknownSigmaVariableObservations(
                       double[][] muDomains,
                       double[][] observations,
                       double[] statistic){
         
         this.muDomains = muDomains;
         this.observations = observations;
         this.statistic = statistic;
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         muVariable = new RealVar[this.muDomains.length];
         for(int i = 0; i < this.muVariable.length; i++)
            muVariable[i] = VariableFactory.real("Mu "+(i+1), muDomains[i][0], muDomains[i][1], precision, solver);
         
         observationVariable = new RealVar[this.observations.length][this.observations[0].length];
         for(int i = 0; i < this.observations.length; i++){
            for(int j = 0; j < this.observations[i].length; j++){
               observationVariable[i][j] = VariableFactory.real("Obs_"+(i+1)+"_"+(j+1), observations[i][j], observations[i][j], precision, solver);
            }
         }
         
         statisticVariable = VF.real("score", statistic[0], statistic[1], precision, solver);
         
         tSquareStatistic.decompose("scoreConstraint", muVariable, observationVariable, statisticVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         RealStrategy strat1 = new RealStrategy(muVariable, new Cyclic(), new RealDomainMiddle());
         RealStrategy strat2 = new RealStrategy(flatten(observationVariable), new Cyclic(), new RealDomainMiddle());
         solver.set(strat1, strat2);
      }
      
      private RealVar[] flatten(RealVar[][] matrix){
         RealVar[] array = new RealVar[matrix.length*matrix[0].length];
         for(int i = 0; i < matrix.length; i++){
            for(int j = 0; j < matrix[i].length; j++){
               array[i*matrix[i].length+j] = matrix[i][j];
            }
         }
         return array;
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < muVariable.length; i++){
                 st.append("("+muVariable[i].getLB()+","+muVariable[i].getUB()+"), ");
              }
              st.append("\n");
              st.append(statisticVariable.getLB()+" "+statisticVariable.getUB());
              st.append("\n");
              
              assertTrue(statisticVariable.getLB() <= 2.71 && statisticVariable.getLB() >= 2.70);
              assertTrue(statisticVariable.getUB() <= 2.71 && statisticVariable.getUB() >= 2.70);
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        LoggerFactory.getLogger("bench").info(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
}
