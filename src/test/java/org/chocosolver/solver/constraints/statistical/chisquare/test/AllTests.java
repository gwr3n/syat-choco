package org.chocosolver.solver.constraints.statistical.chisquare.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ ChiSquareFitEmpiricalTest.class, ChiSquareFitNormalTest.class, ChiSquareFitPoissonTest.class,
      ChiSquareIndependenceTest.class })
public class AllTests {

}
