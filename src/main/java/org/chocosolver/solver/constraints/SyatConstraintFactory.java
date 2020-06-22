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

package org.chocosolver.solver.constraints;

import org.chocosolver.solver.constraints.nary.contingency.ContingencyDecompositions;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType;
import org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositions;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.KolmogorovSmirnov;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions.DistributionVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import umontreal.iro.lecuyer.probdist.Distribution;

/**
 * A Factory to declare constraints in the syat library
 * 
 * @author Roberto Rossi
 *
 */

public class SyatConstraintFactory extends IntConstraintFactory {

   /**
    * Two-sample Kolmogorov-Smirnov statistical constraint.
    * 
    * @param observationsA first list of observations
    * @param observationsB second list of observations
    * @param op operator that defines the comparison to be performed between the two distributions {@link org.chocosolver.solver.constraints.Operator
}
    * @param confidence test confidence level
    * @return the Kolmogorov-Smirnov statistical constraint instance
    */
   public static KolmogorovSmirnov kolmogorov_smirnov(IntVar[] observationsA, IntVar[] observationsB, String op, double confidence) {
      Operator op1 = Operator.get(op);
      return new KolmogorovSmirnov(observationsA, observationsB, op1, confidence);
   }

   /**
    * One sample Kolmogorov-Smirnov statistical constraint.
    * 
    * @param observations list of observations
    * @param distribution target distribution
    * @param op operator that defines the comparison to be performed between the two distributions {@link org.chocosolver.solver.constraints.Operator
}
    * @param confidence test confidence level
    * @return the Kolmogorov-Smirnov statistical constraint instance
    */
   public static KolmogorovSmirnov kolmogorov_smirnov(IntVar[] observations, Distribution distribution, String op, double confidence) {
      Operator op1 = Operator.get(op);
      return new KolmogorovSmirnov(observations, distribution, op1, confidence);
   }

   /**
    * One sample Kolmogorov-Smirnov statistical constraint with parameterised target distribution.
    * 
    * @param observations list of observations
    * @param distribution parameterised target distribution (one parameter)
    * @param op operator that defines the comparison to be performed between the two distributions {@link org.chocosolver.solver.constraints.Operator
}
    * @param confidence test confidence level
    * @return the Kolmogorov-Smirnov statistical constraint instance
    */
   public static KolmogorovSmirnov kolmogorov_smirnov(IntVar[] observations, DistributionVar distribution, String op, double confidence) {
      Operator op1 = Operator.get(op);
      return new KolmogorovSmirnov(observations, distribution, op1, confidence);
   }
   
   /**
    * Decomposition of the {@code BINCOUNTS} global constraint with integer valued observations.
    * 
    * @param observations observations
    * @param binCounts bin counts
    * @param binBounds bin bounds expressed as a list of breakpoints
    * @param decompositionType decomposition type {@link org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType
}
    */
   public static void bincountsDecomposition(IntVar[] observations, IntVar[] binCounts, int[] binBounds, BincountsDecompositionType decompositionType){
      switch(decompositionType){
      case Rossi2016:
         BincountsDecompositions.bincountsDecomposition1(observations, binCounts, binBounds);
         break;
      case Agkun2016_1:
         BincountsDecompositions.bincountsDecomposition2(observations, binCounts, binBounds);
         break;
      case Agkun2016_2_EQ:
         BincountsDecompositions.bincountsDecomposition3(observations, binCounts, binBounds, true);
         break;
      case Agkun2016_2_LE:
         BincountsDecompositions.bincountsDecomposition3(observations, binCounts, binBounds, false);
         break;
      default:
         throw new NullPointerException();
      }
   }
   
   /**
    * Decomposition of the {@code BINCOUNTS} global constraint with real valued observations
    * 
    * @param observations observations
    * @param binCounts bin counts
    * @param binBounds bin bounds expressed as a list of breakpoints
    * @param precision Ibex precision 
    * @param decompositionType decomposition type {@link org.chocosolver.solver.constraints.nary.bincounts.BincountsDecompositionType
}
    */
   public static void bincountsDecomposition(RealVar[] observations, IntVar[] binCounts, double[] binBounds, double precision, BincountsDecompositionType decompositionType){
      switch(decompositionType){
      case Agkun2016_1:
         BincountsDecompositions.bincountsDecomposition2(observations, binCounts, binBounds, precision);
         break;
      case Agkun2016_2_EQ:
         BincountsDecompositions.bincountsDecomposition3(observations, binCounts, binBounds, precision, true);
         break;
      case Agkun2016_2_LE:
         BincountsDecompositions.bincountsDecomposition3(observations, binCounts, binBounds, precision, false);
         break;
      default:
         throw new NullPointerException();
      }
   }
   
   /**
    * {@code CONTINGENCY} constraint decomposition for integer valued observations.
    * 
    * @param observationsA population A observations
    * @param observationsB population B observations
    * @param binVariables contingency table cell counts
    * @param binBounds contingency table bin bounds; provide two arrays of 
    * breakpoints, for populations A and B, respectively
    * @param marginalsH contingency table row counts sums
    * @param marginalsV contingency table column counts sums
    */
   public static void contingencyDecomposition(IntVar[] observationsA, IntVar[] observationsB, IntVar[][] binVariables, int[][] binBounds, IntVar[] marginalsH, IntVar[] marginalsV){
      ContingencyDecompositions.decompose(observationsA, observationsB, binVariables, binBounds, marginalsH, marginalsV);
   }
   
   /**
    * {@code CONTINGENCY} constraint decomposition for real valued observations.
    * 
    * @param observationsA population A observations
    * @param observationsB population B observations
    * @param binVariables contingency table cell counts
    * @param binBounds contingency table bin bounds; provide two arrays of 
    * breakpoints, for populations A and B, respectively
    * @param marginalsH contingency table row counts sums
    * @param marginalsV contingency table column counts sums
    */
   public static void contingencyDecomposition(RealVar[] observationsA, RealVar[] observationsB, IntVar[][] binVariables, double[][] binBounds, IntVar[] marginalsH, IntVar[] marginalsV){
      ContingencyDecompositions.decompose(observationsA, observationsB, binVariables, binBounds, marginalsH, marginalsV);
   }
}
