package org.chocosolver.solver.constraints.statistical;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.statistical.distributions.DistributionVar;

import umontreal.iro.lecuyer.probdist.Distribution;

import org.chocosolver.solver.constraints.statistical.binary.PropGreaterOrEqualX_DStDist;
import org.chocosolver.solver.constraints.statistical.binary.PropGreaterOrEqualX_YStDist;
import org.chocosolver.solver.constraints.statistical.binary.PropGreaterOrEqualX_YStMean;
import org.chocosolver.solver.constraints.statistical.binary.PropLessOrEqualX_DStDist;
import org.chocosolver.solver.constraints.statistical.binary.PropNotEqualX_DStDist;
import org.chocosolver.solver.constraints.statistical.binary.PropNotEqualX_YStDist;
import org.chocosolver.solver.constraints.statistical.binary.PropNotEqualX_YStMean;
import org.chocosolver.solver.constraints.statistical.unary.PropGreaterOrEqualXCStDist;
import org.chocosolver.solver.constraints.statistical.unary.PropGreaterOrEqualXCStMean;
import org.chocosolver.solver.constraints.statistical.unary.PropLessOrEqualXCStDist;
import org.chocosolver.solver.constraints.statistical.unary.PropLessOrEqualXCStMean;
import org.chocosolver.solver.constraints.statistical.unary.PropNotEqualXCStDist;
import org.chocosolver.solver.constraints.statistical.unary.PropNotEqualXCStMean;

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
public class ArithmeticSt extends Constraint {

    protected final Operator op1, op2; // operators.
    protected final int cste;
    protected final boolean isBinary; // to distinct unary and binary formula
    
    @SuppressWarnings("unused")
    private static boolean isOperation(Operator operator) {
        return operator.equals(Operator.PL) || operator.equals(Operator.MN);
    }

    @SuppressWarnings("unchecked")
   private static Propagator<IntVar>[] createProp(IntVar[] var, Operator op1, Operator op2, int cste, double confidence){
    	switch (op2) {
        case MEAN:
	        switch (op1) {
	            case EQ: // X = C
	            	//throw new UnsupportedOperationException();
	                return new Propagator[]{new PropGreaterOrEqualXCStMean(var, cste, 1-(1-confidence)/2.0),new PropLessOrEqualXCStMean(var, cste, 1-(1-confidence)/2.0)};
	            case NQ: // X =/= C
	            	return new Propagator[]{new PropNotEqualXCStMean(var, cste, 1-(1-confidence)/2.0)};
	            case GE: // X >= C
	            	return new Propagator[]{new PropGreaterOrEqualXCStMean(var, cste, confidence)};
	            case GT: // X > C -->  X >= C + 1
	            	return new Propagator[]{new PropGreaterOrEqualXCStMean(var, cste + 1, confidence)};
	            case LE: // X <= C
	            	return new Propagator[]{new PropLessOrEqualXCStMean(var, cste, confidence)};
	            case LT: // X < C --> X <= C - 1
	            	return new Propagator[]{new PropLessOrEqualXCStMean(var, cste - 1, confidence)};
	            default:
	                throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
	        }
	    default: 
	    	throw new SolverException("Incorrect formula; operator two should be one of those:{MEAN}");
        }
    }
    
    public ArithmeticSt(IntVar[] var, Operator op1, Operator op2, int cste, double confidence) {
        super("ArithmeticSt_Mean", createProp(var, op1, op2, cste, confidence));
        this.op1 = op1;
        this.op2 = op2;
        this.cste = cste;
        this.isBinary = false;
        
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
    
    public ArithmeticSt(IntVar[] var1, DistributionVar dist, Operator op1, double confidence) {
    	super("ArithmeticSt_DistVar", createProp(var1, dist, op1, confidence));
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
	            throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
	    }
    }
    
    public ArithmeticSt(IntVar[] var1, Distribution dist, Operator op1, double confidence) {
    	super("ArithmeticSt_Dist", createProp(var1, dist, op1, confidence));
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
        case MEAN:
	        switch (op1) {
	            case EQ: // X = Y
	            	return new Propagator[]{new PropGreaterOrEqualX_YStMean(var1, var2, 1-(1-confidence)/2.0),new PropGreaterOrEqualX_YStMean(var2, var1, 1-(1-confidence)/2.0)};
	            case NQ: // X =/= Y
	            	return new Propagator[]{new PropNotEqualX_YStMean(var1, var2, 1-(1-confidence)/2.0)};
	            case GE: //  X >= Y
	            	return new Propagator[]{new PropGreaterOrEqualX_YStMean(var1, var2, confidence)};
	            case GT: //  X > Y --> X >= Y + 1
	            	throw new NullPointerException("Not implemented");
	                //setPropagators(new PropGreaterOrEqualX_YC(vars, 1));
	                //break;
	            case LE: //  X <= Y --> Y >= X
	            	return new Propagator[]{new PropGreaterOrEqualX_YStMean(var2, var1, confidence)};
	            case LT: //  X < Y --> Y >= X + 1
	            	throw new NullPointerException("Not implemented");
	                //setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, 1));
	                //break;
	            default:
	                throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
	        }
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
	    	throw new SolverException("Incorrect formula; operator two should be one of those:{MEAN}");
        }
    }
    
    public ArithmeticSt(IntVar[] var1, IntVar[] var2, Operator op1, Operator op2, double confidence) {
        super("ArithmeticSt_TwoSample", createProp(var1, var2, op1, op2, confidence));
        this.op1 = op1;
        this.op2 = op2;
        this.cste = 0;
        this.isBinary = true;
        
    }

    /*public ArithmeticSt(IntVar var1, Operator op1, IntVar var2, Operator op2, int cste, Solver solver) {
        super(new IntVar[]{var1, var2}, solver);
        this.op1 = op1;
        this.op2 = op2;
        if (isOperation(op1) == isOperation(op2)) {
            throw new SolverException("Incorrect formula; operators must be different!");
        }
        this.cste = cste;
        this.isBinary = true;
        if (op1 == Operator.PL) {
            switch (op2) {
                case EQ: // X+Y = C
                    setPropagators(new PropEqualXY_C(vars, cste));
                    break;
                case NQ: // X+Y != C
                    setPropagators(new PropNotEqualXY_C(vars, cste));
                    break;
                case GE: // X+Y >= C
                    setPropagators(new PropGreaterOrEqualXY_C(vars, cste));
                    break;
                case GT: // X+Y > C --> X+Y >= C+1
                    setPropagators(new PropGreaterOrEqualXY_C(vars, cste + 1));
                    break;
                case LE: // X+Y <= C
                    setPropagators(new PropLessOrEqualXY_C(vars, cste));
                    break;
                case LT: // X+Y < C --> X+Y <= C-1
                    setPropagators(new PropLessOrEqualXY_C(vars, cste - 1));
                    break;
                default:
                    throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
            }
        } else if (op1 == Operator.MN) {
            switch (op2) {
                case EQ: // X-Y = C --> X = Y+C
                    setPropagators(new PropEqualX_YC(vars, cste));
                    break;
                case NQ: // X-Y != C --> X != Y+C
                    setPropagators(new PropNotEqualX_YC(vars, cste));
                    break;
                case GE: // X-Y >= C --> X >= Y+C
                    setPropagators(new PropGreaterOrEqualX_YC(vars, cste));
                    break;
                case GT: // X-Y > C --> X >= Y+C+1
                    setPropagators(new PropGreaterOrEqualX_YC(vars, cste + 1));
                    break;
                case LE:// X-Y <= C --> Y >= X-C
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste));
                    break;
                case LT:// X-Y < C --> Y >= X-C+1
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -cste + 1));
                    break;
                default:
                    throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
            }
        } else {
            int _cste = cste * (op2 == Operator.PL ? 1 : -1);
            switch (op1) {
                case EQ:// X = Y + C
                    setPropagators(new PropEqualX_YC(vars, _cste));
                    break;
                case NQ:// X =/= Y + C
                    setPropagators(new PropNotEqualX_YC(vars, _cste));
                    break;
                case GE:// X >= Y + C
                    setPropagators(new PropGreaterOrEqualX_YC(vars, _cste));
                    break;
                case GT:// X > Y + C --> X >= Y + C + 1
                    setPropagators(new PropGreaterOrEqualX_YC(vars, _cste + 1));
                    break;
                case LE:// X <= Y + C --> Y >= X - C
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste));
                    break;
                case LT:// X < Y + C --> Y > X - C + 1
                    setPropagators(new PropGreaterOrEqualX_YC(new IntVar[]{var2, var1}, -_cste + 1));
                    break;
                default:
                    throw new SolverException("Incorrect formula; operator should be one of those:{=, !=, >=, >, <=, <}");
            }
        }
    }*/

	@Override
	public Constraint makeOpposite(){
		throw new UnsupportedOperationException();
	}
}