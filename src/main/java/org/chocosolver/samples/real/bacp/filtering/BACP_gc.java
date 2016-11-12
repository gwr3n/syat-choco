package org.chocosolver.samples.real.bacp.filtering;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;

public class BACP_gc extends Constraint {
   
   public BACP_gc(IntVar[] course_period, String instance){
      super("BACP_gc", (Propagator<IntVar>) new Prop_BACP_gc(course_period, instance));
   }

}
