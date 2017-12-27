package org.chocosolver.solver.constraints.nary.deviation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CovarianceTest.class, PooledStandardDeviationTest.class, StandardDeviationTest.class,
      StandardErrorTest.class, VarianceTest.class })
public class AllTests {

}
