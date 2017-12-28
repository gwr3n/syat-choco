/*
 * syat-choco: a Choco extension for Declarative Statistics
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

import java.util.Arrays;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.nary.mean.Mean;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Decompositions of the {@code COVARIANCE} constraint
 * 
 * @author Roberto Rossi
 * @see <a href="https://en.wikipedia.org/wiki/Covariance">Covariance</a>
 */

public class Covariance {
   
   /**
    * {@code COVARIANCE} constraint decomposition for integer valued observations
    * 
    * @param name constraint name
    * @param observationsA population A observations
    * @param observationsB population B observations
    * @param covariance covariance
    * @param precision Ibex precision
    */
   public static void decompose(String name,
                                IntVar[] observationsA,
                                IntVar[] observationsB,
                                RealVar covariance,
                                double precision){
      Solver solver = covariance.getSolver();

      int minA = Arrays.stream(observationsA).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxA = Arrays.stream(observationsA).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar meanA = VariableFactory.real("Mean A", minA, maxA, precision, solver);
      Mean.decompose("MeanConstraint A", observationsA, meanA, precision);

      int minB = Arrays.stream(observationsB).mapToInt(o -> o.getLB()).min().getAsInt();
      int maxB = Arrays.stream(observationsB).mapToInt(o -> o.getUB()).max().getAsInt();

      RealVar meanB = VariableFactory.real("Mean B", minB, maxB, precision, solver);
      Mean.decompose("MeanConstraint B", observationsB, meanB, precision);

      String exp = "(";
      for(int i = 0; i < observationsA.length; i++){
         if(i < observationsA.length - 1)
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"})+";
         else
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"}))/"+(observationsA.length-1)+"={"+(observationsA.length + observationsB.length)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observationsA.length + observationsB.length + 3];
      RealVar[] realObservationsA = VF.real(observationsA, precision);
      RealVar[] realObservationsB = VF.real(observationsB, precision);
      System.arraycopy(realObservationsA, 0, allRealVariables, 0, observationsA.length);
      System.arraycopy(realObservationsB, 0, allRealVariables, observationsA.length, observationsB.length);
      allRealVariables[observationsA.length + observationsB.length] = covariance;
      allRealVariables[observationsA.length + observationsB.length + 1] = meanA;
      allRealVariables[observationsA.length + observationsB.length + 2] = meanB;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }

   /**
    * {@code COVARIANCE} constraint decomposition for integer valued observations
    * 
    * @param name constraint name
    * @param observationsA population A observations
    * @param observationsB population B observations
    * @param covariance covariance
    * @param precision Ibex precision
    */
   
   public static void decompose(String name,
                                RealVar[] observationsA,
                                RealVar[] observationsB,
                                RealVar covariance,
                                double precision){
      Solver solver = covariance.getSolver();

      double minA = Arrays.stream(observationsA).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxA = Arrays.stream(observationsA).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar meanA = VariableFactory.real("Mean A", minA, maxA, precision, solver);
      Mean.decompose("MeanConstraint A", observationsA, meanA, precision);

      double minB = Arrays.stream(observationsB).mapToDouble(o -> o.getLB()).min().getAsDouble();
      double maxB = Arrays.stream(observationsB).mapToDouble(o -> o.getUB()).max().getAsDouble();

      RealVar meanB = VariableFactory.real("Mean B", minB, maxB, precision, solver);
      Mean.decompose("MeanConstraint B", observationsB, meanB, precision);

      String exp = "(";
      for(int i = 0; i < observationsA.length; i++){
         if(i < observationsA.length - 1)
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"})+";
         else
            exp += "({"+i+"}-{"+(observationsA.length + observationsB.length + 1)+"})*({"+(observationsA.length + i)+"}-{"+(observationsA.length + observationsB.length + 2)+"}))/"+(observationsA.length-1)+"={"+(observationsA.length + observationsB.length)+"}";
      }

      RealVar[] allRealVariables = new RealVar[observationsA.length + observationsB.length + 3];
      System.arraycopy(observationsA, 0, allRealVariables, 0, observationsA.length);
      System.arraycopy(observationsB, 0, allRealVariables, observationsA.length, observationsB.length);
      allRealVariables[observationsA.length + observationsB.length] = covariance;
      allRealVariables[observationsA.length + observationsB.length + 1] = meanA;
      allRealVariables[observationsA.length + observationsB.length + 2] = meanB;

      solver.post(new RealConstraint(name, exp, Ibex.HC4_NEWTON, allRealVariables));
   }
}
