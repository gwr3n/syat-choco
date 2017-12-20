package org.chocosolver.solver.constraints.statistical.kolmogorovsmirnov;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.syat.statistics.KolmogorovSmirnovTest;
import org.chocosolver.solver.variables.statistical.distributions.DistributionVar;

import umontreal.iro.lecuyer.probdist.ContinuousDistribution;
import umontreal.iro.lecuyer.probdist.EmpiricalDist;


/**
 * X <= C
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/11
 */
@SuppressWarnings("serial")
public class PropNotEqualX_DStDist extends Propagator<IntVar> {

    private final DistributionVar dist;
    private final double confidence;

    private static IntVar[] mergeArrays(IntVar[] var1, IntVar[] var2){
    	IntVar[] var3 = new IntVar[var1.length+var2.length];
    	System.arraycopy(var1, 0, var3, 0, var1.length);
    	System.arraycopy(var2, 0, var3, var1.length, var2.length);
    	return var3;
    }
    
    public PropNotEqualX_DStDist(IntVar[] var, DistributionVar dist, double confidence) {
        super(mergeArrays(var, dist.getVarParatemers()), PropagatorPriority.BINARY, true);
        if(!(dist instanceof ContinuousDistribution)) 
			throw new SolverException("Theoretical distribution should not be discrete");
        if(dist.getNumberOfVarParameters() > 1)
        	throw new SolverException("This propagator only supports distribution with a single parameter");
        this.dist = dist;
        this.confidence = confidence;
    }

    @Override
    public int getPropagationConditions(int vIdx) {
    	return IntEventType.INSTANTIATE.getMask() + IntEventType.BOUND.getMask();
    }

    /**
     * Double check validity if samples are not single observations
     * @param evtmask
     * @throws ContradictionException
     */
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // with views such as abs(...), the prop can be not entailed after initial propagation
        /*if (vars[0].updateUpperBound(constant, aCause) || vars[0].getUB() <= constant) {
            this.setPassive();
        }*/
        
        for(int i = 0; i < vars.length; i++){
        	double[] samplesLB = new double[vars.length];
        	int k = 1;
        	for(int j = 0; j < vars.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesLB[k++] = vars[j].getLB();
        	}
        	
        	double[] samplesUB = new double[vars.length];
        	k = 1;
        	for(int j = 0; j < vars.length; j++){
        		if(j==i) 
        			continue;
        		else
        			samplesUB[k++] = vars[j].getUB();
        	}
        	
        	IntVar pivotVar = vars[i];
        	DisposableValueIterator iterator = pivotVar.getValueIterator(true);
        	while(iterator.hasNext()){
        		int value = iterator.next();
        		samplesLB[0] = samplesUB[0] = value;
        		
        		DisposableValueIterator iteratorVar = this.dist.getVarParatemers()[0].getValueIterator(true);
        		boolean support = false;
        		while(iteratorVar.hasNext()){
        			int valueVar = iteratorVar.next();
        			this.dist.setParameters(new double[]{valueVar});
	        		EmpiricalDist empLB = new EmpiricalDist(samplesLB);
	        		KolmogorovSmirnovTest ksTestLBUB = new KolmogorovSmirnovTest(empLB, this.dist, this.confidence);
	    			EmpiricalDist empUB = new EmpiricalDist(samplesUB);
	    			KolmogorovSmirnovTest ksTestUBLB = new KolmogorovSmirnovTest(empUB, this.dist, this.confidence);
	    			
	    			if(!(ksTestLBUB.testD1GeqE1() && ksTestUBLB.testE1GeqD1())){
	    				support = true;
	    				break;
	    			}
        		}
        		if(!support) pivotVar.removeValue(value, this);
        	}
        }
        
        double[] samplesLB = new double[vars.length];
    	for(int j = 0; j < vars.length; j++){
    		samplesLB[j] = vars[j].getLB();
    	}
    	
    	double[] samplesUB = new double[vars.length];
    	for(int j = 0; j < vars.length; j++){
    		samplesUB[j] = vars[j].getUB();
    	}
    	
    	IntVar pivotVar = this.dist.getVarParatemers()[0];
    	DisposableValueIterator iterator = pivotVar.getValueIterator(true);
    	while(iterator.hasNext()){
    		int value = iterator.next();
    		this.dist.setParameters(new double[]{value});
    		
    		EmpiricalDist empLB = new EmpiricalDist(samplesLB);
    		KolmogorovSmirnovTest ksTestLBUB = new KolmogorovSmirnovTest(empLB, this.dist, this.confidence);
    		boolean lbub = ksTestLBUB.testD1GeqE1();
    		
    		EmpiricalDist empUB = new EmpiricalDist(samplesUB);
    		KolmogorovSmirnovTest ksTestUBLB = new KolmogorovSmirnovTest(empUB, this.dist, this.confidence);
    		boolean ublb = ksTestUBLB.testE1GeqD1();
    		
    		if(lbub && ublb){
				pivotVar.removeValue(value, this);
			}
    	}
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    @Override
    public ESat isEntailed() {
        /*if (vars[0].getUB() <= constant) {
            return ESat.TRUE;
        }
        if (vars[0].getLB() > constant) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;*/
    	return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        return vars[0].getName() + " <= " + dist.toString();
    }
}

