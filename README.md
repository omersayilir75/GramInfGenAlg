# GramInfGenAlg

This repository contains a prototype implementation of a genetic algorithm for inferring context-free grammars from language samples.

This projects makes use of the <a href= http://moeaframework.org/>MOEA Framework</a> and offers both a single-objective approach (using a generic genetic algorithm) and a multiobjective approach (using the NSGA-II algorithm).

For the initialisation step, this project uses a modified version of the SEQUITUR implementation from <a href=https://github.com/jMotif/GI> jMotif/GI </a> [[1]](#1) that can be found at <a href= https://github.com/omersayilir75/GI> omersayilir75/GI</a>.


<a id="1">[1]</a>
Senin, P., Lin, J., Wang, X., Oates, T., Gandhi, S., Boedihardjo, A.P., Chen, C., Frankenstein, S., Lerner, M., GrammarViz 2.0: a tool for grammar-based pattern discovery in time series, ECML/PKDD Conference, 2014.