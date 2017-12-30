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

package org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions;

import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.UniformDist;;

/**
 * An Exponential distribution on {@code [0..M]}, parameterised by {@code M}
 * 
 * @author Roberto Rossi
 *
 */
public class UniformDistVar extends ContinuousDistribution implements DistributionVar {

	IntVar M;
	double curM;
	
	/**
	 * Constructor for an Exponential distribution on {@code [0..M]}, parameterised by {@code M}
	 * 
	 * @param M the upper bound of the distribution support
	 */
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
