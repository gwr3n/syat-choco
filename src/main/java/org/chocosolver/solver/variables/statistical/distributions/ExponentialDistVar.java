package org.chocosolver.solver.variables.statistical.distributions;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import umontreal.iro.lecuyer.probdist.ExponentialDist;
import umontreal.iro.lecuyer.probdist.ContinuousDistribution;

public class ExponentialDistVar extends ContinuousDistribution implements DistributionVar {

	IntVar mean;
	double curLambbda;
	
	public ExponentialDistVar (IntVar mean) {
	      this.mean = mean;
	  }

	public void setParameters(double[] params){
		if(params.length > 1)
			throw new SolverException("Exponential distribution has a single parameter");
		this.curLambbda = 1.0/params[0];
	}
	
	public double density(double x){
		return (new ExponentialDist(this.curLambbda)).density(x);
	}
	
	public double cdf(double x) {
		// TODO Auto-generated method stub
		return (new ExponentialDist(this.curLambbda)).cdf(x);
	}
	
	public int getNumberOfVarParameters() {
		// TODO Auto-generated method stub
		return 1;
	}

	public IntVar[] getVarParatemers() {
		// TODO Auto-generated method stub
		return new IntVar[]{this.mean};
	}

	public double[] getParams() {
		// TODO Auto-generated method stub
		return new double[]{1.0/this.curLambbda};
	}
}
