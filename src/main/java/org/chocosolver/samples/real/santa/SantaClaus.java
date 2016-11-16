package org.chocosolver.samples.real.santa;

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;

/**
 * Run with -Djava.library.path=/Users/gwren/EclipseWorkspace/Ibex/ibex-2.3.1/lib/lib/
 * @author gwren
 *
 */
public class SantaClaus {

   public static void main(String args[]){
      int n_kids = 3;
      int n_gifts = 5;
      int[] gift_price = new int[]{11, 24, 5, 23, 17};
      int min_price = 5;
      int max_price = 24;
      // solver
      Solver solver = new Solver("Santa Claus");

      // FD variables
      // VF is the factory for variables' declaration
      IntVar[] kid_gift = VF.enumeratedArray("g2k", n_kids, 0, n_gifts, solver);
      IntVar[] kid_price = VF.boundedArray("p2k", n_kids, min_price, max_price, solver);
      IntVar total_cost = VF.bounded("total cost", min_price*n_kids, max_price * n_kids, solver);
      // CD variable
      double precision = 1.e-4;
      RealVar average = VF.real("average", min_price, max_price, precision, solver);
      RealVar average_deviation = VF.real("average_deviation", 0, max_price, precision, solver);
      // continuous views of FD variables
      RealVar[] realViews = VF.real(kid_price, precision);
      // kids must have different gifts
      // ICF is the factory for integer constraints' declaration
      solver.post(ICF.alldifferent(kid_gift, "AC"));
      // compute cost
      for (int i = 0; i < n_kids; i++) {
         solver.post(ICF.element(kid_price[i], gift_price, kid_gift[i]));
      }
      solver.post(ICF.sum(kid_price, total_cost));
      // compute the average and average deviation costs
      
      
      RealVar[] allRV = new RealVar[realViews.length+2];
      System.arraycopy(realViews, 0, allRV, 0, realViews.length);
      allRV[realViews.length] = average;
      allRV[realViews.length + 1] = average_deviation;
      solver.post(new RealConstraint("average",
            "({0}+{1}+{2})/3={3}",
            Ibex.HC4_NEWTON, allRV
            ));
      solver.post(new RealConstraint("average_deviation",
            "(abs({0}-{3})+abs({1}-{3})+abs({2}-{3}))/3={4}",
            Ibex.HC4_NEWTON, allRV
            ));
      
      // set search strategy (selects smallest domains first)
      //solver.set(IntStrategyFactory.firstFail_InDomainMin(kid_gift));
      solver.set(new RealStrategy(allRV, new Cyclic(), new RealDomainMiddle()));
      
      Chatterbox.showSolutions(solver);
      Chatterbox.showStatistics(solver);
      
      // find optimal solution (the gift distribution should be fair)
      solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, average_deviation, precision);
      
      solver.getIbex().release();
   }
}
