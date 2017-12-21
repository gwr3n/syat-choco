package org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions;

import org.chocosolver.solver.variables.IntVar;
import umontreal.iro.lecuyer.probdist.Distribution;

public interface DistributionVar extends Distribution {
	
	public int getNumberOfVarParameters();
	
	public IntVar[] getVarParatemers();
	
	public void setParameters(double[] parameters);
}
