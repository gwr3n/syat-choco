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

package org.chocosolver.samples.statistical.t;

import umontreal.iro.lecuyer.probdist.PoissonDist;
import umontreal.iro.lecuyer.probdist.StudentDist;

import java.util.Random;
import java.util.stream.IntStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.statistical.t.tStatistic;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * This class implements a simple example illustrating the use of 
 * Student's t statistical constraints for the computation of confidence interval 
 * of the mean. Further details on the underpinning theory can be found at
 * <a href="https://en.wikipedia.org/wiki/Confidence_interval#Theoretical_example">
 * Confidence interval</a>.
 * 
 * Our aim is to determine nominal coverage probability for confidence intervals 
 * represented by the declarative statistics model.
 * 
 * @author Roberto Rossi
 *
 */

public class TTestCIBatch extends AbstractProblem {

   /**
    *  Input data.
    */
   int[] data;

    /**
     * Decision variables
     */
    public IntVar[] population;
    public RealVar mean;
    
    /**
     * Ibex real constraints numerical precision
     */
    double precision = 1.e-4;
    
    /**
     * Significance level
     */
    double alpha = 0.1;
    
    public TTestCIBatch(int[] data) {
       this.data = data;
    }

    @Override
    public void createSolver() {
        solver = new Solver("TTest example");
    }

    @Override
    public void buildModel() {
       
        /**
         * Define decision variables representing observed realisations
         */
        population = IntStream.iterate(0, i -> i + 1)
                              .limit(data.length)
                              .mapToObj(i -> VariableFactory.bounded("sample "+i, data[i], data[i], solver))
                              .toArray(IntVar[]::new);
        
        /**
         * Define decision variable representing mean values that are compatible (at prescribed confidence level) with realisations
         */
        mean = VariableFactory.real("mean ", 10, 10, precision, solver);
        
        /**
         * RealVar representing feasible values of Student's t statistics at prescribed confidence level
         */ 
        StudentDist tDist = new StudentDist(data.length - 1);
        RealVar t = VariableFactory.real("tStatistic", tDist.inverseF(alpha/2), tDist.inverseF(1-alpha/2), precision, solver);
        
        /**
         * Student's t statistical constraint
         */
        tStatistic.decompose("tStatistic_cons", population, mean, t, precision);
    }
    
    @Override
    public void configureSearch() {
      AbstractStrategy<IntVar> stratPop = IntStrategyFactory.domOverWDeg(population,2211);
      RealStrategy stratMean = new RealStrategy(new RealVar[]{mean}, new Cyclic(), new RealDomainMiddle());
      solver.set(stratPop,stratMean);
    }

    @Override
    public void solve() {
      StringBuilder st = new StringBuilder();
      st.append("Confidence interval for the mean: ");
      boolean solution = solver.findSolution();
      if(solution) {
         feasible++;
         st.append("Solution!");
      }else{
         st.append("No solution!");
      }
    }

    @Override
    public void prettyOut() {
       
    }
    
    public static double feasible = 0;

    public static void main(String[] args) {

       int replications = 500;
       Random rnd = new Random(1234);
       PoissonDist dist = new PoissonDist(10);
       
       for(int k = 0; k < replications; k++) {
          String[] str={"-log","SILENT"};
          int[] data = IntStream.iterate(0, i -> i + 1).map(i -> (int) dist.inverseF(rnd.nextDouble())).limit(10).toArray();
          TTestCIBatch t = new TTestCIBatch(data);
          t.execute(str);
          t.getSolver().getIbex().release();
          t = null;
          System.gc();
          try {
             Thread.sleep(50);
          } catch (InterruptedException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
          }
       }
       
       System.out.println("Coverage probability: "+feasible/replications);
    }
}
