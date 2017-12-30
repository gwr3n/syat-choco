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

package org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;

import umontreal.iro.lecuyer.probdist.Distribution;

import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.distributions.DistributionVar;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropGreaterOrEqualXCStDist;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropGreaterOrEqualX_DStDist;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropGreaterOrEqualX_YStDist;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropLessOrEqualXCStDist;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropLessOrEqualX_DStDist;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropNotEqualXCStDist;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropNotEqualX_DStDist;
import org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov.propagators.PropNotEqualX_YStDist;

/**
 * Kolmogorov-Smirnov statistical constraint
 * 
 * @author Roberto Rossi
 *
 */

@SuppressWarnings("serial")
public class KolmogorovSmirnov extends Constraint {

    protected final Operator op; // operators.
    protected final int cste;
    protected final boolean isBinary; // to distinguish unary and binary formula
    
    @SuppressWarnings("unused")
    private static boolean isOperation(Operator operator) {
        return operator.equals(Operator.PL) || operator.equals(Operator.MN);
    }
    
    @SuppressWarnings("unchecked")
   private static Propagator<IntVar>[] createProp(IntVar[] observations, DistributionVar dist, Operator op, double confidence) {
    	switch (op) {
			case EQ: // X = Y
				return new Propagator[]{new PropGreaterOrEqualX_DStDist(observations, dist, 1-(1-confidence)/2.0), new PropLessOrEqualX_DStDist(observations, dist, 1-(1-confidence)/2.0)};
			case NQ: // X =/= Y
				return new Propagator[]{new PropNotEqualX_DStDist(observations, dist, 1-(1-confidence)/2.0)};
	    	case GE: //  X >= Y
	    		return new Propagator[]{new PropGreaterOrEqualX_DStDist(observations, dist, confidence)};
	    	case LE: //  X <= Y --> Y >= X
	    		return new Propagator[]{new PropLessOrEqualX_DStDist(observations, dist, confidence)};
	    	default:
	            throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");    
    	}
    }
    
    /**
     * One sample Kolmogorov-Smirnov statistical constraint with parameterised target distribution.
     * 
     * @param observations list of observations
     * @param distribution parameterised target distribution (one parameter)
     * @param op operator that defines the comparison to be performed between the two distributions {@link org.chocosolver.solver.constraints.Operator
 }
     * @param confidence test confidence level
     */
    public KolmogorovSmirnov(IntVar[] observations, DistributionVar distribution, Operator op, double confidence) {
    	super("KolmogorovSmirnov_DistVar", createProp(observations, distribution, op, confidence));
    	this.op = op;
    	this.isBinary = false;
    	this.cste = 0;
    	
    }
    
    @SuppressWarnings("unchecked")
   private static Propagator<IntVar>[] createProp(IntVar[] observations, Distribution distribution, Operator op, double confidence) {
    	switch (op) {
	        case EQ: // X = Y
	        	return new Propagator[]{new PropGreaterOrEqualXCStDist(observations, distribution, 1-(1-confidence)/2.0), new PropLessOrEqualXCStDist(observations, distribution, 1-(1-confidence)/2.0)};
	        case NQ: // X =/= Y
	        	return new Propagator[]{new PropNotEqualXCStDist(observations, distribution, 1-(1-confidence)/2.0)};
	        case GE: //  X >= Y
	        	return new Propagator[]{new PropGreaterOrEqualXCStDist(observations, distribution, confidence)};
	        case GT: //  X > Y --> X >= Y + 1
	        	throw new NullPointerException("Not implemented");
	            //return new Propagator[]{new PropGreaterOrEqualXCStDist(var1, dist, confidence)};
	        case LE: //  X <= Y --> Y >= X
	        	return new Propagator[]{new PropLessOrEqualXCStDist(observations, distribution, confidence)};
	        case LT: //  X < Y --> Y >= X + 1
	        	throw new NullPointerException("Not implemented");
	            //return new Propagator[]{new PropGreaterOrEqualXCStDist(new IntVar[]{var2, var1}, 1)};
	        default:
	            throw new SolverException("Undefined operator: {=, !=, >=, >, <=, <}");
	    }
    }
    
    /**
     * One sample Kolmogorov-Smirnov statistical constraint.
     * 
     * @param observations list of observations
     * @param distribution target distribution
     * @param op operator that defines the comparison to be performed between the two distributions {@link org.chocosolver.solver.constraints.Operator
 }
     * @param confidence test confidence level
     */
    public KolmogorovSmirnov(IntVar[] observations, Distribution distribution, Operator op, double confidence) {
    	super("KolmogorovSmirnov_Dist", createProp(observations, distribution, op, confidence));
    	this.op = op;
    	this.isBinary = false;
    	this.cste = 0;
    }
    
    @SuppressWarnings("unused")
    private static IntVar[] mergeArrays(IntVar[] array1, IntVar[] array2){
    	IntVar[] var3 = new IntVar[array1.length+array2.length];
    	System.arraycopy(array1, 0, var3, 0, array1.length);
    	System.arraycopy(array2, 0, var3, array1.length, array2.length);
    	return var3;
    }
    
    @SuppressWarnings("unchecked")
    private static Propagator<IntVar>[] createProp(IntVar[] observations1, IntVar[] observations2, Operator op, double confidence) {
       switch (op) {
       case EQ: // X = Y
          return new Propagator[]{new PropGreaterOrEqualX_YStDist(observations1, observations2, 1-(1-confidence)/2.0),new PropGreaterOrEqualX_YStDist(observations2, observations1, 1-(1-confidence)/2.0)};
       case NQ: // X =/= Y
          return new Propagator[]{new PropNotEqualX_YStDist(observations1, observations2, 1-(1-confidence)/2.0)};
       case GE: //  X >= Y
          return new Propagator[]{new PropGreaterOrEqualX_YStDist(observations1, observations2, confidence)};
       case GT: //  X > Y --> X >= Y + 1
          throw new NullPointerException("Not implemented");
          //setPropagators(new PropGreaterOrEqualX_YC(vars, 1));
          //break;
       case LE: //  X <= Y --> Y >= X
          return new Propagator[]{new PropGreaterOrEqualX_YStDist(observations2, observations1, confidence)};
       case LT: //  X < Y --> Y >= X + 1
          throw new NullPointerException("Not implemented");
          //setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, 1));
          //break;
       default:
          throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
       }
    }
    
    /**
     * Two-sample Kolmogorov-Smirnov statistical constraint.
     * 
     * @param observationsA first list of observations
     * @param observationsB second list of observations
     * @param op operator that defines the comparison to be performed between the two distributions {@link org.chocosolver.solver.constraints.Operator
 }
     * @param confidence test confidence level
     */
    public KolmogorovSmirnov(IntVar[] observations1, IntVar[] observations2, Operator op, double confidence) {
        super("KolmogorovSmirnov_TwoSample", createProp(observations1, observations2, op, confidence));
        this.op = op;
        this.cste = 0;
        this.isBinary = true;
        
    }

    /**
     * Operation undefined for this constraint.
     */
	@Override
	public Constraint makeOpposite(){
		throw new UnsupportedOperationException();
	}
}