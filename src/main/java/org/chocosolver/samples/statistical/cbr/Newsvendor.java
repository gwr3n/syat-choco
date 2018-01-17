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

package org.chocosolver.samples.statistical.cbr;

import java.util.stream.IntStream;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.mean.Mean;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.constraints.statistical.chisquare.ChiSquareFitPoisson;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.ESat;

import umontreal.iro.lecuyer.probdist.ChiSquareDist;

/**
 * Confidence-based reasoning for the Newsvendor problem as discussed in 
 * 
 * R. Rossi et al., European Journal of Operational Research, Elsevier, Vol. 239(3):674-684, 2014
 * 
 * We assume a Poisson distribution and we use a Normal approximation to estimate cost.
 * 
 * @author Roberto Rossi
 * @see <a href"https://doi.org/10.1016/j.ejor.2014.06.007">10.1016/j.ejor.2014.06.007</a>
 */
public class Newsvendor extends AbstractProblem {

    // Random variates
    public int[] demand; 

    // Decision Variables
    public IntVar[] randomVariates;
    public IntVar[] binVariables;
    public RealVar lambda;
    public IntVar intQ;
    public RealVar realQ;
    public RealVar chiSqstatisticVariable;
    public RealVar etc;
    
    // Ibex precision
    static public double precision = 0.01;
    
    // Bins used in the Chi Squared statistical constraint
    public int lb = 35;
    public int bins = 3;
    public int binSize = 10;
    int[] binBounds = IntStream.iterate(lb, i -> i + binSize).limit(bins+1).toArray();
    
    // Newsvendor problem parameters
    public double h = 1;   // holding cost
    public double p = 10;  // penalty cost
    
    // Buffer to store temporary results
    double outputQ = Double.NaN;
    double outputETC = Double.NaN;
    
    // Switch to compute uppoer or lower confidence interval bounds
    ModelType type;

    public Newsvendor(int[] demand, 
                      double h, 
                      double p, 
                      ModelType type, 
                      int lb, 
                      int bins, 
                      int binSize) {
       this.demand = demand;
       this.h = h;
       this.p = p;
       this.type = type;
       this.lb = lb;
       this.bins = bins;
       this.binSize = binSize;
    }
    
    public void setUp() {
        // read data
    }

    @Override
    public void createSolver() {
        solver = new Solver("Newsvendor");
    }

    @Override
    public void buildModel() {
        setUp();
        int n = demand.length;
        randomVariates = new IntVar[n];
        for(int i = 0; i < n; i++){
           randomVariates[i] = VariableFactory.bounded("sample "+i, demand[i], demand[i], solver);
        }

        lambda = VariableFactory.real("lambda", 0, Integer.MAX_VALUE, precision, solver);
        
        RealVar mean;
        
        if(type == ModelType.CI_UPPER_BOUND_A) {
           mean = VariableFactory.real("sampleMeanVar", Integer.MIN_VALUE, Integer.MAX_VALUE, precision, solver);
           Mean.decompose("sampleMeanCon", randomVariates, mean, precision);
           solver.post(new RealConstraint("mean", "{0}>={1}", Ibex.HC4_NEWTON, new RealVar[] {lambda,mean}));
        }
        else if(type == ModelType.CI_UPPER_BOUND_B) {
           mean = VariableFactory.real("sampleMeanVar", Integer.MIN_VALUE, Integer.MAX_VALUE, precision, solver);
           Mean.decompose("sampleMeanCon", randomVariates, mean, precision);
           solver.post(new RealConstraint("mean", "{0}<={1}", Ibex.HC4_NEWTON, new RealVar[] {lambda,mean}));
        }
        
        binVariables = new IntVar[this.binBounds.length-1];
        for(int i = 0; i < this.binVariables.length; i++)
           binVariables[i] = VariableFactory.bounded("Bin "+(i+1), 0, demand.length, solver);
        
        double chiSq = ChiSquareDist.inverseF(bins-1, 0.95);
        chiSqstatisticVariable = VariableFactory.real("chiSqStatistics", 
                                                      this.type != ModelType.CI_LOWER_BOUND ? chiSq : 0, chiSq, 
                                                      precision, 
                                                      solver);
        
        ChiSquareFitPoisson.decomposition("chiSqConstraint", 
                                          randomVariates, 
                                          binVariables, 
                                          binBounds, 
                                          lambda, 
                                          chiSqstatisticVariable, 
                                          precision, 
                                          false);
        
        etc = VariableFactory.real("etc", 0, Integer.MAX_VALUE, precision, solver);
        
        intQ = VariableFactory.bounded("intQ", 0, 1000, solver);
        realQ = VariableFactory.real(intQ, precision);
        
        /**
         * Normal approximation by <a href="https://www.hindawi.com/journals/mpe/2012/124029/">Vazquez-Leal, et al.</a>; Eq. 4.5.
         */
        String normalCDF = "(2.71828^(-358*(({1}-{0})/sqrt({0}))/23+111*atan(37*(({1}-{0})/sqrt({0}))/294))+1)^(-1)";
        String normalPDF = "(1/sqrt(2*3.14159265359)*2.71828^(-0.5*(({1}-{0})/sqrt({0}))^2))";
        
        String etcStr = "{2}=("+h+"+"+p+")*sqrt({0})*((({1}-{0})/sqrt({0}))*"+normalCDF+"+"+normalPDF+")-"+p+"*({1}-{0})";
        
        solver.post(new RealConstraint("ETC", etcStr, Ibex.HC4_NEWTON, new RealVar[] {lambda, realQ, etc}));
    }
    
    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
       IntVar[] var3 = new IntVar[var1.length+var2.length];
       System.arraycopy(var1, 0, var3, 0, var1.length);
       System.arraycopy(var2, 0, var3, var1.length, var2.length);
       return var3;
     }

    @Override
    public void configureSearch() {
        solver.set(
              IntStrategyFactory.domOverWDeg(mergeArrays(binVariables, new IntVar[] {intQ}), 2211),
              new RealStrategy(new RealVar[]{chiSqstatisticVariable}, new Cyclic(), new RealDomainMiddle()),
              new RealStrategy(new RealVar[]{lambda}, new Cyclic(), new RealDomainMiddle()),
              new RealStrategy(new RealVar[]{etc}, new Cyclic(), new RealDomainMiddle())
              );
    }

    @Override
    public void solve() {
       solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, etc, precision);
       if(solver.isFeasible() == ESat.TRUE) {
          this.outputQ = intQ.getValue();
          this.outputETC = etc.getLB();
       }else {
          System.out.println("No solution");
       }
    }

    @Override
    public void prettyOut() {
        
    }
    
    public static void main(String[] args) {      
      String[] str={"-log","SOLUTION"};
      
      // Random variates generated according to a Poisson with lambda = 50
      int[] demand = {51, 54, 50, 45, 52, 39, 52, 54, 50, 40}; 
      
      double h = 1; // Newsvendor holding cost
      double p = 10; // Newsvendor penalty cost
      
      /**
       * Create 3 bins of size 10 starting from value 35
       */
      int lb = 35;
      int bins = 3;
      int binSize = 10;
      
      // Decrease Ibex precision, otherwise the first problem takes very long
      precision = 0.1;
      
      /**
       * Compute lower bound of the optimal expected total cost and order quantity
       */
      Newsvendor pb = new Newsvendor(demand, h, p, ModelType.CI_LOWER_BOUND, lb, bins, binSize);
      pb.execute(str);
      pb.getSolver().getIbex().release();
      double Q_lb = pb.outputQ;
      double ETC_lb = pb.outputETC;
      
      System.gc();
      
      // Now increase Ibex precision again
      precision = 0.01;
      
      /**
       *  Compute upper bound of the optimal expected total cost and order quantity.
       *  
       *  This requires solving two problems, see <a href"https://doi.org/10.1016/j.ejor.2014.06.007">10.1016/j.ejor.2014.06.007</a>
       *  
       */
      pb = new Newsvendor(demand, h, p, ModelType.CI_UPPER_BOUND_A, lb, bins, binSize);
      pb.execute(str);
      pb.getSolver().getIbex().release();
      double Q_ub_A = pb.outputQ;
      double ETC_ub_A = pb.outputETC;
      
      System.gc();
      
      pb = new Newsvendor(demand, h, p, ModelType.CI_UPPER_BOUND_B, lb, bins, binSize);
      pb.execute(str);
      pb.getSolver().getIbex().release();
      double Q_ub_B = pb.outputQ;
      double ETC_ub_B = pb.outputETC;
      
      System.out.println("ETC: ["+ETC_lb+","+Math.max(ETC_ub_A, ETC_ub_B)+"]");
      System.out.println("Q: ["+Q_lb+","+(ETC_ub_A > ETC_ub_B ? Q_ub_A : Q_ub_B)+"]");
    }
}

enum ModelType {
   CI_UPPER_BOUND_A,
   CI_UPPER_BOUND_B,
   CI_LOWER_BOUND
}

