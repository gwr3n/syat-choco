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
 * A constraint dedicated to arithmetic operations.
 * <br/>
 * There are three available definitions:
 * <li>
 * <ul>VAR op CSTE,</ul>
 * <ul>VAR op VAR,</ul>
 * <ul>VAR op VAR op CSTE</ul>
 * </li>
 * where VAR is a variable, CSTE a constant and op is an operator among {"=", "!=","<", ">", "<=, ">="} or{"+", "-"}.
 *
 * @author Charles Prud'homme
 * @since 21/06/12
 */
@SuppressWarnings("serial")
public class KolmogorovSmirnov extends Constraint {

    protected final Operator op1, op2; // operators.
    protected final int cste;
    protected final boolean isBinary; // to distinct unary and binary formula
    
    @SuppressWarnings("unused")
    private static boolean isOperation(Operator operator) {
        return operator.equals(Operator.PL) || operator.equals(Operator.MN);
    }
    
    @SuppressWarnings("unchecked")
   private static Propagator<IntVar>[] createProp(IntVar[] var1, DistributionVar dist, Operator op1, double confidence) {
    	switch (op1) {
			case EQ: // X = Y
				return new Propagator[]{new PropGreaterOrEqualX_DStDist(var1, dist, 1-(1-confidence)/2.0), new PropLessOrEqualX_DStDist(var1, dist, 1-(1-confidence)/2.0)};
			case NQ: // X =/= Y
				return new Propagator[]{new PropNotEqualX_DStDist(var1, dist, 1-(1-confidence)/2.0)};
	    	case GE: //  X >= Y
	    		return new Propagator[]{new PropGreaterOrEqualX_DStDist(var1, dist, confidence)};
	    	case LE: //  X <= Y --> Y >= X
	    		return new Propagator[]{new PropLessOrEqualX_DStDist(var1, dist, confidence)};
	    	default:
	            throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");    
    	}
    }
    
    public KolmogorovSmirnov(IntVar[] var1, DistributionVar dist, Operator op1, double confidence) {
    	super("KolmogorovSmirnov_DistVar", createProp(var1, dist, op1, confidence));
    	this.op1 = op1;
    	this.op2 = Operator.DISTRIBUTION;
    	this.isBinary = false;
    	this.cste = 0;
    	
    }
    
    @SuppressWarnings("unchecked")
   private static Propagator<IntVar>[] createProp(IntVar[] var1, Distribution dist, Operator op1, double confidence) {
    	switch (op1) {
	        case EQ: // X = Y
	        	return new Propagator[]{new PropGreaterOrEqualXCStDist(var1, dist, 1-(1-confidence)/2.0), new PropLessOrEqualXCStDist(var1, dist, 1-(1-confidence)/2.0)};
	        case NQ: // X =/= Y
	        	return new Propagator[]{new PropNotEqualXCStDist(var1, dist, 1-(1-confidence)/2.0)};
	        case GE: //  X >= Y
	        	return new Propagator[]{new PropGreaterOrEqualXCStDist(var1, dist, confidence)};
	        case GT: //  X > Y --> X >= Y + 1
	        	throw new NullPointerException("Not implemented");
	            //return new Propagator[]{new PropGreaterOrEqualXCStDist(var1, dist, confidence)};
	        case LE: //  X <= Y --> Y >= X
	        	return new Propagator[]{new PropLessOrEqualXCStDist(var1, dist, confidence)};
	        case LT: //  X < Y --> Y >= X + 1
	        	throw new NullPointerException("Not implemented");
	            //return new Propagator[]{new PropGreaterOrEqualXCStDist(new IntVar[]{var2, var1}, 1)};
	        default:
	            throw new SolverException("Undefined operator: {=, !=, >=, >, <=, <}");
	    }
    }
    
    public KolmogorovSmirnov(IntVar[] var1, Distribution dist, Operator op1, double confidence) {
    	super("KolmogorovSmirnov_Dist", createProp(var1, dist, op1, confidence));
    	this.op1 = op1;
    	this.op2 = Operator.DISTRIBUTION;
    	this.isBinary = false;
    	this.cste = 0;
    }
    
    @SuppressWarnings("unused")
    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }
    
    @SuppressWarnings("unchecked")
   private static Propagator<IntVar>[] createProp(IntVar[] var1, IntVar[] var2, Operator op1, Operator op2, double confidence) {
    	switch (op2) {
        case DISTRIBUTION:
	        switch (op1) {
	            case EQ: // X = Y
	            	return new Propagator[]{new PropGreaterOrEqualX_YStDist(var1, var2, 1-(1-confidence)/2.0),new PropGreaterOrEqualX_YStDist(var2, var1, 1-(1-confidence)/2.0)};
	            case NQ: // X =/= Y
	            	return new Propagator[]{new PropNotEqualX_YStDist(var1, var2, 1-(1-confidence)/2.0)};
	            case GE: //  X >= Y
	            	return new Propagator[]{new PropGreaterOrEqualX_YStDist(var1, var2, confidence)};
	            case GT: //  X > Y --> X >= Y + 1
	            	throw new NullPointerException("Not implemented");
	                //setPropagators(new PropGreaterOrEqualX_YC(vars, 1));
	                //break;
	            case LE: //  X <= Y --> Y >= X
	            	return new Propagator[]{new PropGreaterOrEqualX_YStDist(var2, var1, confidence)};
	            case LT: //  X < Y --> Y >= X + 1
	            	throw new NullPointerException("Not implemented");
	                //setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, 1));
	                //break;
	            default:
	                throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
	        }
	    default: 
	    	throw new SolverException("Undefined operator");
        }
    }
    
    public KolmogorovSmirnov(IntVar[] var1, IntVar[] var2, Operator op1, Operator op2, double confidence) {
        super("KolmogorovSmirnov_TwoSample", createProp(var1, var2, op1, op2, confidence));
        this.op1 = op1;
        this.op2 = op2;
        this.cste = 0;
        this.isBinary = true;
        
    }

	@Override
	public Constraint makeOpposite(){
		throw new UnsupportedOperationException();
	}
}