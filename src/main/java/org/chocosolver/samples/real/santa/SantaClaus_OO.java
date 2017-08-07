/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.real.santa;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.real.Ibex;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.strategy.selectors.values.RealDomainMiddle;
import org.chocosolver.solver.search.strategy.selectors.variables.Cyclic;
import org.chocosolver.solver.search.strategy.strategy.RealStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.VF;
import org.slf4j.LoggerFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/07/12
 */
public class SantaClaus_OO extends AbstractProblem {

    int n_kids = 3;
    int n_gifts = 5;
    int min_price = 5;
    int max_price = 24;
    int[] gift_price = new int[]{11, 24, 5, 23, 17};

    IntVar[] kid_gift;
    IntVar[] kid_price;
    IntVar total_cost;
    
    RealVar average;
    RealVar average_deviation;
    RealVar[] allRV; 
    
    double precision = 1.e-4;

   @Override
    public void createSolver() {
        solver = new Solver("Santa Claude");
    }

    @Override
    public void buildModel() {
       kid_gift = VF.enumeratedArray("g2k", n_kids, 0, n_gifts, solver);
       kid_price = VF.boundedArray("p2k", n_kids, min_price, max_price, solver);
       total_cost = VF.bounded("total cost", min_price*n_kids, max_price * n_kids, solver);
        
        average = VF.real("average", min_price, max_price, precision, solver);
        average_deviation = VF.real("average_deviation", 0, max_price, precision, solver);
        
     // continuous views of FD variables
        RealVar[] realViews = VF.real(kid_price, precision);
     // kids must have different gifts
        //solver.post(IntConstraintFactory.alldifferent(kid_gift, "AC"));
        solver.post(ICF.alldifferent(kid_gift, "AC"));
     // compute cost
        for (int i = 0; i < n_kids; i++) {
           //solver.post(IntConstraintFactory.element(kid_price[i], gift_price, kid_gift[i], 0, "detect"));
           solver.post(ICF.element(kid_price[i], gift_price, kid_gift[i]));
        }
        solver.post(IntConstraintFactory.sum(kid_price, total_cost));
        
     // compute the average and average deviation costs
        allRV = new RealVar[realViews.length+2];
        System.arraycopy(realViews, 0, allRV, 0, realViews.length);
        allRV[realViews.length] = average;
        allRV[realViews.length + 1] = average_deviation;
        solver.post(new RealConstraint("average",
              "({0}+{1}+{2})/3={3}",
              Ibex.HC4_NEWTON, allRV
              ));
        solver.post(new RealConstraint("average_deviation",
              "(abs({0}-{3})+abs({1}-{3})+abs({2}-{3}))/3={4}",
              Ibex.HC4_NEWTON, allRV
              ));    
    }

    @Override
    public void configureSearch() {
        //solver.set(IntStrategyFactory.domOverWDeg(kid_gift, 29091981));
        solver.set(new RealStrategy(allRV, new Cyclic(), new RealDomainMiddle()));
        SearchMonitorFactory.limitTime(solver,10000);
    }

    @SuppressWarnings("serial")
   @Override
    public void solve() {
        solver.getSearchLoop().plugSearchMonitor(new IMonitorSolution() {
            public void onSolution() {
                if (LoggerFactory.getLogger("solver").isInfoEnabled()) {
                    LoggerFactory.getLogger("solver").info("*******************");
                    for (int i = 0; i < n_kids; i++) {
                        LoggerFactory.getLogger("solver").info("Kids #{} has received the gift #{} at a cost of {} euros",
                                new Object[]{i, kid_gift[i].getValue(), kid_price[i].getValue()});
                    }
                    LoggerFactory.getLogger("solver").info("Total cost: {} euros", total_cost.getValue());
                    LoggerFactory.getLogger("solver").info("Average: [{},{}] euros per kid", average.getLB(), average.getUB());
                    LoggerFactory.getLogger("solver").info("Average deviation: [{},{}] euros per kid", average_deviation.getLB(), average_deviation.getUB());
                }
            }
        });
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, average_deviation, precision);
    }

    @Override
    public void prettyOut() {
      solver.getIbex().release();
    }

    public static void main(String[] args) {
        new SantaClaus_OO().execute(args);
    }
}
