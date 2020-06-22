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

package org.chocosolver.solver.constraints.nary.deviation;

import static org.junit.Assert.*;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PooledStandardDeviationTest {

   @Before
   public void setUp() throws Exception {
   }

   @After
   public void tearDown() throws Exception {
      System.gc();
      Thread.sleep(3000);
   }

   @Test
   public void testInteger() {
      String[] str={"-log","SOLUTION"};
      
      int[][] valuesA = {{1},{2},{3},{4},{5},{6},{7},{8},{9}};
      int[][] valuesB = {{5},{4},{6},{5},{4},{6},{5},{4},{6}};
      
      IntegerPooledStandardDeviation standardDeviation = new IntegerPooledStandardDeviation(valuesA, valuesB, new double[]{0,100});
      standardDeviation.execute(str);
      standardDeviation.getSolver().getIbex().release();
   }
   
   @Test
   public void testReal() {
      String[] str={"-log","SOLUTION"};
      
      double[][] valuesA = {{1},{2},{3},{4},{5},{6},{7},{8},{9}};
      double[][] valuesB = {{5},{4},{6},{5},{4},{6},{5},{4},{6}};
      
      RealPooledStandardDeviation standardDeviation = new RealPooledStandardDeviation(valuesA, valuesB, new double[]{0,100});
      standardDeviation.execute(str);
      standardDeviation.getSolver().getIbex().release();
   }

   class IntegerPooledStandardDeviation extends AbstractProblem {
      public IntVar[] valueVariablesA;
      public IntVar[] valueVariablesB;
      public RealVar standardDeviationVariable;
      
      public int[][] valuesA;
      public int[][] valuesB;
      public double[] standardDeviation;
      
      double precision = 1.e-4;
      
      public IntegerPooledStandardDeviation(int[][] valuesA, int[][] valuesB, double[] standardDeviation){
         this.valuesA = valuesA;
         this.valuesB = valuesB;
         this.standardDeviation = standardDeviation;
      }
      
      @Override
      public void createSolver() {
         solver = new Solver("IntegerPooledStandardDeviation");
      }
      
      @Override
      public void buildModel() {
         valueVariablesA = new IntVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            valueVariablesA[i] = VariableFactory.enumerated("ValueA"+(i+1), valuesA[i], solver);
         
         valueVariablesB = new IntVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            valueVariablesB[i] = VariableFactory.enumerated("ValueB"+(i+1), valuesB[i], solver);
         
         standardDeviationVariable = VariableFactory.real("PooledStandardDeviation", standardDeviation[0], standardDeviation[1], precision, solver);
         
         PooledStandardDeviation.decompose("PooledStandardDeviationConstraint", valueVariablesA, valueVariablesB, standardDeviationVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         AbstractStrategy<IntVar> stratA = IntStrategyFactory.activity(valueVariablesA,1234);
         AbstractStrategy<IntVar> stratB = IntStrategyFactory.activity(valueVariablesB,1234);
         solver.set(stratA, stratB);
      }
    
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariablesA.length; i++){
                 st.append(valueVariablesA[i].getValue()+", ");
              }
              st.append("\n");
              for(int i = 0; i < valueVariablesB.length; i++){
                 st.append(valueVariablesB[i].getValue()+", ");
              }
              st.append("\n");
              st.append(standardDeviationVariable.getLB()+" "+standardDeviationVariable.getUB());
              st.append("\n");
              
              assertTrue(standardDeviationVariable.getLB() <= Math.sqrt(4.125));
              assertTrue(standardDeviationVariable.getUB() >= Math.sqrt(4.125));
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
   
   class RealPooledStandardDeviation extends AbstractProblem {
      public RealVar[] valueVariablesA;
      public RealVar[] valueVariablesB;
      public RealVar standardDeviationVariable;
      
      public double[][] valuesA;
      public double[][] valuesB;
      public double[] standardDeviation;
      
      double precision = 1.e-4;
      
      public RealPooledStandardDeviation(double[][] valuesA, double[][] valuesB, double[] standardDeviation){
         this.valuesA = valuesA;
         this.valuesB = valuesB;
         this.standardDeviation = standardDeviation;
      }
      
      @Override
      public void createSolver() {
         solver = new Solver("RealPooledStandardDeviation");
      }
      
      @Override
      public void buildModel() {
         valueVariablesA = new RealVar[this.valuesA.length];
         for(int i = 0; i < this.valuesA.length; i++)
            valueVariablesA[i] = VariableFactory.real("ValueA"+(i+1), valuesA[i][0], valuesA[i][0], precision, solver);
         
         valueVariablesB = new RealVar[this.valuesB.length];
         for(int i = 0; i < this.valuesB.length; i++)
            valueVariablesB[i] = VariableFactory.real("ValueB"+(i+1), valuesB[i][0], valuesB[i][0], precision, solver);
         
         standardDeviationVariable = VariableFactory.real("PooledStandardDeviation", standardDeviation[0], standardDeviation[1], precision, solver);
         
         PooledStandardDeviation.decompose("PooledStandardDeviationConstraint", valueVariablesA, valueVariablesB, standardDeviationVariable, precision);
      }
      
      @Override
      public void configureSearch() {
         solver.set(
               new RealStrategy(valueVariablesA, new Cyclic(), new RealDomainMiddle()),
               new RealStrategy(valueVariablesB, new Cyclic(), new RealDomainMiddle()));
      }
    
      @Override
      public void solve() {
        StringBuilder st = new StringBuilder();
        boolean solution = solver.findSolution();
        //do{
           st.append("---\n");
           if(solution) {
              for(int i = 0; i < valueVariablesA.length; i++){
                 st.append("("+valueVariablesA[i].getLB()+", "+valueVariablesA[i].getUB() + ")\t");
              }
              st.append("\n");
              for(int i = 0; i < valueVariablesB.length; i++){
                 st.append("("+valueVariablesB[i].getLB()+", "+valueVariablesB[i].getUB() + ")\t");
              }
              st.append("\n");
              st.append(standardDeviationVariable.getLB()+" "+standardDeviationVariable.getUB());
              st.append("\n");
              
              assertTrue(standardDeviationVariable.getLB() <= Math.sqrt(4.125));
              assertTrue(standardDeviationVariable.getUB() >= Math.sqrt(4.125));
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

