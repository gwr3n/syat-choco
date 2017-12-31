![syat-choco Logo](img/syat-choco-small.png) 

syat-choco is a [Choco](http://www.choco-solver.org/) extension for [Declarative Statistics](http://arxiv.org/abs/1708.01829).

[Syādvāda](https://en.wikipedia.org/wiki/Anekantavada#Sy.C4.81dv.C4.81da), in [Jaina](https://en.wikipedia.org/wiki/Jainism) metaphysics, the doctrine that all judgments are conditional, holding good only in certain conditions, circumstances, or senses, expressed by the word syāt (Sanskrit: "may be"). The ways of looking at a thing (called naya) are infinite in number.

For more information, please consult our [Wiki](https://github.com/gwr3n/syat-choco/wiki).

Current snapshots:
* [syat-choco-1.0.0-SNAPSHOT with dependencies](jar/syat-choco-1.0.0-SNAPSHOT-shaded.jar)
* [syat-choco-1.0.0-SNAPSHOT without dependencies](jar/syat-choco-1.0.0-SNAPSHOT.jar)
* [syat-choco-1.0.0-SNAPSHOT sources](jar/syat-choco-1.0.0-SNAPSHOT-sources.jar)
* [syat-choco-1.0.0-SNAPSHOT javadoc](jar/syat-choco-1.0.0-SNAPSHOT-javadoc.jar)

### Quick start

Install Ibex as discussed [here](https://github.com/gwr3n/syat-choco/wiki/Ibex-quick-installation-notes).

Assuming Ibex libraries have been installed in 

    ~/ibex/ibex-2.3.4/lib/lib/

download [jsdp-1.0.0-SNAPSHOT with dependencies](jar/syat-choco-1.0.0-SNAPSHOT-shaded.jar) and run

    java -cp ./syat-choco-1.0.0-SNAPSHOT-shaded.jar -Djava.library.path=/Users/[username]/ibex/ibex-2.3.4/lib/lib/ org.chocosolver.samples.statistical.t.TTest
    
You should obtain the following output for the [Student t test](https://github.com/gwr3n/syat-choco/wiki/Student-t-test) example 

    TTest example - observations: [8, 14, 6, 12, 12, 9, 10, 9, 10, 5]
    Confidence interval for the mean: [8, 9, 10, 11]

You must provide an __absolute path__ as `-Djava.library.path=` argument.