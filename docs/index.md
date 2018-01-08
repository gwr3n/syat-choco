![syat-choco Logo](img/syat-choco-small.png) 

syat-choco is a [Choco](http://www.choco-solver.org/) extension for [Declarative Statistics](http://arxiv.org/abs/1708.01829).

[Syādvāda](https://en.wikipedia.org/wiki/Anekantavada#Sy.C4.81dv.C4.81da), in [Jaina](https://en.wikipedia.org/wiki/Jainism) metaphysics, the doctrine that all judgments are conditional, holding good only in certain conditions, circumstances, or senses, expressed by the word syāt (Sanskrit: "may be"). The ways of looking at a thing (called naya) are infinite in number.

For more information, please consult our [Wiki](https://github.com/gwr3n/syat-choco/wiki).

__Maven release:__

    <groupId>com.github.gwr3n</groupId>
	<artifactId>syat-choco</artifactId>
	<version>1.0.0</version>

__Maven latest snapshot:__

    <groupId>com.github.gwr3n</groupId>
	<artifactId>syat-choco</artifactId>
	<version>1.0.0-SNAPSHOT</version>
	
[How to download SNAPSHOT version from maven SNAPSHOT repository?](https://stackoverflow.com/questions/7715321/how-to-download-snapshot-version-from-maven-snapshot-repository)

__Current release:__
* [syat-choco-1.0.0 with dependencies](jar/syat-choco-1.0.0-shaded.jar)
* [syat-choco-1.0.0 without dependencies](jar/syat-choco-1.0.0.jar)
* [syat-choco-1.0.0 sources](jar/syat-choco-1.0.0-sources.jar)
* [syat-choco-1.0.0 javadoc](jar/syat-choco-1.0.0-javadoc.jar)

### Quick start

Install Ibex as discussed [here](https://github.com/gwr3n/syat-choco/wiki/Ibex-quick-installation-notes).

Assuming Ibex libraries have been installed in 

    ~/ibex/ibex-2.3.4/lib/lib/

download [syat-choco-1.0.0 with dependencies](jar/syat-choco-1.0.0-shaded.jar) and run

    java -cp ./syat-choco-1.0.0-shaded.jar -Djava.library.path=/Users/[username]/ibex/ibex-2.3.4/lib/lib/ org.chocosolver.samples.statistical.t.TTest
    
You should obtain the following output for the [Student t test](https://github.com/gwr3n/syat-choco/wiki/Student-t-test) example 

    TTest example - observations: [8, 14, 6, 12, 12, 9, 10, 9, 10, 5]
    Confidence interval for the mean: [8, 9, 10, 11]

You must provide an __absolute path__ as `-Djava.library.path=` argument.

### About the author

syat-choco is maintained by [Roberto Rossi](https://gwr3n.github.io), Reader at the University of Edinburgh.