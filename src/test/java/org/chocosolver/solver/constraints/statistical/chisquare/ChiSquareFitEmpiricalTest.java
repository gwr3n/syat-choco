/*
 * syat-choco: a Choco extension for Declarative Statistics.
 * 
 * MIT License
 * 
 * Copyright (c) 2016 Roberto Rossi
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy 
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.chocosolver.solver.constraints.statistical.chisquare;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitEmpirical;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChiSquareFitEmpiricalTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      System.gc();
      Thread.sleep(3000);
   }
   
   /**
    * Run one test at a time. Multiple Solver instances seem to create conflicts.
    */

   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] values = {{2,3,4},{1,2},{0,3,5},{3,4},{1},{2,3,4}};
      int[][] binCounts = {{0,6},{0,6},{0,6}};
      int[] binBounds = {0,2,4,6};
      int[][] targetFrequencies = {{1,1},{4,4},{1,1}};
      
      double[] chiSqStatistic = {0,0};
      
      ChiSquareFitEmpiricalInteger cs = new ChiSquareFitEmpiricalInteger(values, binCounts, binBounds, targetFrequencies, chiSqStatistic);
      cs.execute(str);
      cs.getSolver().getIbex().release();
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] values = {{2,4},{1,2},{0,5},{3,4},{1,1},{2,4}};
      int[][] binCounts = {{0,6},{0,6},{0,6}};
      double[] binBounds = {0,1.5,3.5,5.5};
      int[][] targetFrequencies = {{1,1},{4,4},{1,1}};
      
      double[] chiSqStatistic = {0,0};
      
      ChiSquareFitEmpiricalReal cs = new ChiSquareFitEmpiricalReal(values, binCounts, binBounds, targetFrequencies, chiSqStatistic);
      cs.execute(str);
      cs.getSolver().getIbex().release();
   }

   class ChiSquareFitEmpiricalInteger extends AbstractProblem {
      public IntVar[] valueVariables;
      public IntVar[] binVariables;
      public IntVar[] targetFrequencyVariables;
      public RealVar chiSqstatisticVariable;
      
      int[][] binCounts;
      int[][] values;
      int[] binBounds;
      int[][] targetFrequencies;
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareFitEmpiricalInteger(int[][] values,
                                          int[][] binCounts, 
                                          int[] binBounds,
                                          int[][] targetFrequencies,
                                          double[] chiSqStatistic){
         this.values = values.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
         this.targetFrequencies = targetFrequencies.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new IntVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.enumerated("Value "+(i+1), values[i], solver);
         
         binVariables = new IntVar[this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++)
            binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
         
         targetFrequencyVariables = new IntVar[this.targetFrequencies.length];
         for(int i = 0; i < this.targetFrequencies.length; i++)
            targetFrequencyVariables[i] = VariableFactory.bounded("Target "+(i+1), this.targetFrequencies[i][0], this.targetFrequencies[i][1], solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareFitEmpirical.decomposition("chiSqConstraint", valueVariables, binVariables, binBounds, targetFrequencyVariables, chiSqstatisticVariable, precision, false);
      }
      
      @Override
      public void configureSearch() {
         AbstractStrategy<IntVar> strat = IntStrategyFactory.activity(valueVariables,1234);
         solver.set(strat);
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append(valueVariables[i].getValue()+", ");
              }
              st.append("\n");
              for(int i = 0; i < binVariables.length; i++){
                 st.append(binVariables[i].getValue()+", ");
              }
              st.append("\n");
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
              st.append("\n");
              
              assertTrue(chiSqstatisticVariable.getLB() <= 0);
              assertTrue(chiSqstatisticVariable.getUB() >= 0);
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
   class ChiSquareFitEmpiricalReal extends AbstractProblem {
      public RealVar[] valueVariables;
      public IntVar[] binVariables;
      public IntVar[] targetFrequencyVariables;
      public RealVar chiSqstatisticVariable;
      
      int[][] binCounts;
      double[][] values;
      double[] binBounds;
      int[][] targetFrequencies;
      double[] chiSqStatistic;
      
      double precision = 1.e-4;
      
      public ChiSquareFitEmpiricalReal(double[][] values,
                                       int[][] binCounts, 
                                       double[] binBounds,
                                       int[][] targetFrequencies,
                                       double[] chiSqStatistic){
         this.values = values.clone();
         this.binCounts = binCounts.clone();
         this.binBounds = binBounds.clone();
         this.targetFrequencies = targetFrequencies.clone();
         this.chiSqStatistic = chiSqStatistic.clone();
      }
      
      @Override
      public void createSolver() {
          solver = new Solver("ChiSquare");
      }
      
      @Override
      public void buildModel() {
         valueVariables = new RealVar[this.values.length];
         for(int i = 0; i < this.values.length; i++)
            valueVariables[i] = VariableFactory.real("Value "+(i+1), values[i][0], values[i][1], precision, solver);
         
         binVariables = new IntVar[this.binCounts.length];
         for(int i = 0; i < this.binCounts.length; i++)
            binVariables[i] = VariableFactory.bounded("Bin "+(i+1), this.binCounts[i][0], this.binCounts[i][1], solver);
         
         targetFrequencyVariables = new IntVar[this.targetFrequencies.length];
         for(int i = 0; i < this.targetFrequencies.length; i++)
            targetFrequencyVariables[i] = VariableFactory.bounded("Target "+(i+1), this.targetFrequencies[i][0], this.targetFrequencies[i][1], solver);
         
         chiSqstatisticVariable = VF.real("chiSqStatistics", chiSqStatistic[0], chiSqStatistic[1], precision, solver);
         
         ChiSquareFitEmpirical.decomposition("chiSqConstraint", valueVariables, binVariables, binBounds, targetFrequencyVariables, chiSqstatisticVariable, precision, false);
      }
      
      @Override
      public void configureSearch() {
         RealStrategy strat = new RealStrategy(valueVariables, new Cyclic(), new RealDomainMiddle());
         solver.set(strat);
      }
      
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariables.length; i++){
                 st.append("("+valueVariables[i].getLB()+","+valueVariables[i].getUB()+"), ");
              }
              st.append("\n");
              for(int i = 0; i < binVariables.length; i++){
                 st.append(binVariables[i].getValue()+", ");
              }
              st.append("\n");
              st.append(chiSqstatisticVariable.getLB()+" "+chiSqstatisticVariable.getUB());
              st.append("\n");
              
              assertTrue(chiSqstatisticVariable.getLB() <= 0);
              assertTrue(chiSqstatisticVariable.getUB() >= 0);
           }else{
              st.append("No solution!");
           }
        //}while(solution = solver.nextSolution());
        System.out.println(st.toString());
      }
      
      @Override
      public void prettyOut() {
          
      }
   }
   
}
