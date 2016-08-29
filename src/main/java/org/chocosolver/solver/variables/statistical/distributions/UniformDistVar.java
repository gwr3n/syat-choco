package org.chocosolver.solver.variables.statistical.distributions;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.UniformDist;;

public class UniformDistVar extends ContinuousDistribution implements DistributionVar {

	IntVar M;
	double curM;
	
	public UniformDistVar (IntVar M) {
	      this.M = M;
	  }

	public void setParameters(double[] params){
		if(params.length > 1)
			throw new SolverException("Uniform distribution has a single parameter");
		this.curM = params[0];
	}
	
	public double density(double x){
		return (new UniformDist(0,this.curM)).density(x);
	}
	
	public double cdf(double x) {
		// TODO Auto-generated method stub
		return (new UniformDist(0,this.curM)).cdf(x);
	}
	
	public int getNumberOfVarParameters() {
		// TODO Auto-generated method stub
		return 1;
	}

	public IntVar[] getVarParatemers() {
		// TODO Auto-generated method stub
		return new IntVar[]{this.M};
	}

	public double[] getParams() {
		// TODO Auto-generated method stub
		return new double[]{this.curM};
	}
	
}
