package org.chocosolver.solver.constraints.nary.bincounts;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

@SuppressWarnings("serial")
public class Bincounts extends Constraint {

   public Bincounts(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds, BincountsPropagatorType propagator){
      super("Bincounts", 
            propagator == BincountsPropagatorType.EQ ?
                  (Propagator<IntVar>) new PropBincountsEQ(valueVariables, binVariables, binBounds) :
            propagator == BincountsPropagatorType.EQFast ? new PropBincountsEQFast(valueVariables, binVariables, binBounds) :
                  new PropBincountsLE(valueVariables, binVariables, binBounds)
            );
   }
   
   public Bincounts(RealVar[] valueVariables, RealVar[] binVariables, double[] binBounds, BincountsPropagatorType propagator){
      super("Bincounts", new PropBincountsEQFastReal(valueVariables, binVariables, binBounds));
   }

}
