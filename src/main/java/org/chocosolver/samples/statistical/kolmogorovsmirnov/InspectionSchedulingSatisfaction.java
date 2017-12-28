package org.chocosolver.samples.statistical.kolmogorovsmirnov;

/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.SyatConstraintFactory;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.VariableFactory;
import umontreal.iro.lecuyer.probdist.Distribution;
import umontreal.iro.lecuyer.probdist.ExponentialDist;
import org.chocosolver.util.ESat;

/**
 * @author Gregy4
 */
public class InspectionSchedulingSatisfaction extends AbstractProblem {

	public static int NUM_OF_INSPECTIONS = 250;
	public static int HORIZON = 365;
	public static int INSPECTORS = 5;
	public static int UNITS_TO_INSPECT = 10;
	public static int INSPECTION_LENGTH = 1;
	public static int EXP_INTERVAL_BETWEEN_INSPECTION = 5;
	
	IntVar[] starts, ends;
	IntVar[][] intervals;
	IntVar maxEnd;

	@Override
	public void createSolver(){
		solver = new Solver("schedule");
	}

	@Override
	public void buildModel(){
		if(NUM_OF_INSPECTIONS*1.0/UNITS_TO_INSPECT != Math.round(NUM_OF_INSPECTIONS*1.0/UNITS_TO_INSPECT))
			throw new SolverException("NUM_OF_INSPECTIONS/UNIT_TO_INSPECT not an integer");
		
		// build variables
		starts = new IntVar[NUM_OF_INSPECTIONS];
		ends = new IntVar[NUM_OF_INSPECTIONS];
		IntVar duration = VariableFactory.fixed(INSPECTION_LENGTH, solver);
		maxEnd = VariableFactory.bounded("maxEnd", 1, HORIZON, solver);
		IntVar[] res = new IntVar[NUM_OF_INSPECTIONS];
		Task[] tasks = new Task[NUM_OF_INSPECTIONS];
		for (int iTask=0; iTask < NUM_OF_INSPECTIONS; ++iTask) {
			starts[iTask] = VariableFactory.bounded("start" + iTask, 1, HORIZON, solver);
			ends[iTask] = VariableFactory.bounded("ends" + iTask, 1, HORIZON, solver);
			tasks[iTask] = VariableFactory.task(starts[iTask], duration, ends[iTask]);
			res[iTask] = VariableFactory.fixed(1 , solver);
		}

		//for(int i = 0; i < NUM_OF_INSPECTIONS; i++){
			//int[] assignment = {0,1,2,3,4,5,6,7,9,12,16,20,25,31,38,46,54,63,73,84,96,109,123,138,155,173,192,212,234,257,281,307,335,364,395,428,463,501,541,584,630,679,731,788,849,16,20,25,31,38,46,54,63,73,84,96,109,123,138,155,173,192,212,234,257,281,307,335,364,395,428,463,501,541,584,630,679,731,788,849,850,851,852,853,854,855,856,858,861,865};
			//solver.post(IntConstraintFactory.arithm(starts[i], "=", assignment[i]));
		//}
		
		// post a cumulative constraint
		solver.post(IntConstraintFactory.cumulative(tasks, res, VariableFactory.fixed(INSPECTORS, solver), false));

		// maintain makespan
		solver.post(IntConstraintFactory.maximum(maxEnd, ends));

		// add precedences
		for(int i = 0; i < UNITS_TO_INSPECT; i++){
			for(int j = 1; j < NUM_OF_INSPECTIONS/UNITS_TO_INSPECT; j++){
				solver.post(IntConstraintFactory.arithm(starts[i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j], ">=", ends[i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j-1]));
			}
			// Last inspection should be executed in the last month of the year
			solver.post(IntConstraintFactory.arithm(ends[(i+1)*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)-1], ">=", 330));
		}
		
		intervals = new IntVar[UNITS_TO_INSPECT][NUM_OF_INSPECTIONS/UNITS_TO_INSPECT-1];
		for(int i = 0; i < UNITS_TO_INSPECT; i++){
			for(int j = 1; j < NUM_OF_INSPECTIONS/UNITS_TO_INSPECT; j++){
				//Interval between inspections should be less or equal to 36.5 days (HORIZON/10)
				intervals[i][j-1] = VariableFactory.bounded("interval " + (i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j) + " " + (i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j-1) , 0, HORIZON/10, solver);
				solver.post(IntConstraintFactory.scalar(new IntVar[]{starts[i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j-1],starts[i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j],VariableFactory.fixed(-1, solver)}, new int[]{-1,1,1}, intervals[i][j-1]));
			}
			Distribution dist = new ExponentialDist(1.0/EXP_INTERVAL_BETWEEN_INSPECTION);
			solver.post(SyatConstraintFactory.kolmogorov_smirnov(intervals[i], dist, "=", 0.9));
		}
	}

	@Override
	public void configureSearch(){
		/*IntVar[] intervalsArray = new IntVar[UNITS_TO_INSPECT*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT-1)];
		for(int i = 0; i < UNITS_TO_INSPECT; i++){
			System.arraycopy(intervals[i], 0, intervalsArray,i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT-1), intervals[i].length);
		}*/
		
		solver.set(IntStrategyFactory.impact(starts,2211));
	}

	@Override
	public void solve(){
		solver.findSolution();
		//solver.findOptimalSolution(ResolutionPolicy.MINIMIZE,maxEnd);
	}

	@Override
	public void prettyOut(){
		if (solver.isFeasible() == ESat.TRUE) {
			StringBuilder schedule = new StringBuilder();
			
			StringBuilder st = new StringBuilder();
			st.append("\n");
			for(int i = 0; i < UNITS_TO_INSPECT; i++){
				int counter = 0;
				st.append("Unit "+(i+1)+"\t Inspection start times: ");
				for(int j = 0; j < NUM_OF_INSPECTIONS/UNITS_TO_INSPECT; j++){
					st.append(starts[i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j].getValue()+"\t");
					 while(++counter<starts[i*(NUM_OF_INSPECTIONS/UNITS_TO_INSPECT)+j].getValue()) 
						 schedule.append("0\t");
					 schedule.append("1\t");
				}
				st.append("\n");
				schedule.append("\n");
			}
			st.append("\n");
			for(int i = 0; i < UNITS_TO_INSPECT; i++){
				st.append("Unit "+(i+1)+"\t intervals: ");
				for(int j = 0; j < NUM_OF_INSPECTIONS/UNITS_TO_INSPECT - 1; j++){
					st.append(intervals[i][j].getValue()+"\t");
				}
				st.append("\n");
			}
			System.out.println(st.toString());
			System.out.println("\n");
			System.out.println(schedule.toString());
		}
	}

	public static void main(String[] args){
	   @SuppressWarnings("unused")
		String[] str={"-log","SEARCH"};
		new InspectionSchedulingSatisfaction().execute();
	}
}