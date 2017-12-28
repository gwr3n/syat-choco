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

package org.chocosolver.solver.constraints.statistical.fisherratio;

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.deviation.Variance;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * http://www.itl.nist.gov/div898/handbook/eda/section3/eda359.htm
 * 
 * @author Roberto Rossi
 *
 */

public class FisherRatioStatistic {
   
   public static void decomposition(String name,
                                    IntVar[] seriesA,
                                    IntVar[] seriesB,
                                    RealVar statistic,
                                    double precision){
      Solver solver = statistic.getSolver();
      
      int minA = Arrays.stream(seriesA).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxA = Arrays.stream(seriesA).mapToInt(o -> o.getUB()).max().getAsInt();
      
      RealVar varianceA = VariableFactory.real(name+"varianceA", 0, Math.pow(maxA-minA,2), precision, solver);
      Variance.decompose(name+"_varianceA_cons", seriesA, varianceA, precision);
      
      int minB = Arrays.stream(seriesB).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxB = Arrays.stream(seriesB).mapToInt(o -> o.getUB()).max().getAsInt();
      
      RealVar varianceB = VariableFactory.real(name+"varianceB", 0, Math.pow(maxB-minB,2), precision, solver);
      Variance.decompose(name+"_varianceB_cons", seriesB, varianceB, precision);
      
      String exp = "{0}/{1}={2}";
      
      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, new RealVar[]{varianceA,varianceB,statistic}));
   }
   
   public static void decomposition(String name,
                                    RealVar[] seriesA,
                                    RealVar[] seriesB,
                                    RealVar statistic,
                                    double precision){
      Solver solver = statistic.getSolver();
      
      double minA = Arrays.stream(seriesA).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxA = Arrays.stream(seriesA).mapToDouble(o -> o.getUB()).max().getAsDouble();
      
      RealVar varianceA = VariableFactory.real(name+"_varianceA", 0, Math.pow(maxA-minA,2), precision, solver);
      Variance.decompose(name+"_varianceA_cons", seriesA, varianceA, precision);
      
      double minB = Arrays.stream(seriesB).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxB = Arrays.stream(seriesB).mapToDouble(o -> o.getUB()).max().getAsDouble();
      
      RealVar varianceB = VariableFactory.real(name+"_varianceB", 0, Math.pow(maxB-minB,2), precision, solver);
      Variance.decompose(name+"_varianceB_cons", seriesB, varianceB, precision);
      
      String exp = "{0}/{1}={2}";
      
      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, new RealVar[]{varianceA,varianceB,statistic}));
   }
}
