package org.chocosolver.solver.constraints.nary.bincounts;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;

public class Bincounts extends Constraint {
   
   public Bincounts(IntVar[] valueVariables, IntVar[] binVariables, int[] binBounds){
      super("Bincounts", (Propagator<IntVar>) new PropBincountsEQFast(valueVariables, binVariables, binBounds));
   }

}
