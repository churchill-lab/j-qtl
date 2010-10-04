J/qtl is a Java graphical user interface (GUI) for the popular QTL data analysis
software R/qtl. See http://churchill.jax.org/software/jqtl.shtml for details.

WARRANTY DISCLAIMER AND COPYRIGHT NOTICE
========================================

The Jackson Laboratory makes no representation about the suitability or accuracy
of this software for any purpose, and makes no warranties, either express or
implied, including merchantability and fitness for a particular purpose or that
the use of this software will not infringe any third party patents, copyrights,
trademarks, or other rights. The software are provided "as is".

This software is provided to enhance knowledge and encourage progress in the
scientific community. This is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version."

BUILDING
========
In order successfully complete these build instructions you must have:
* git
* ant >= 1.7.1
* java development kit (JDK) >= 1.5

To obtain and build the source code enter the following commands:
    git clone git://github.com/keithshep/j-qtl.git
    cd j-qtl
    git submodule init
    git submodule update
    ant

After the build completes you will have a full JNLP distribution built under:
    ./modules/main/web-dist

Assuming you have a bash shell you should also be able to run from the
command-line (without bothering with a JNLP install) by doing:
    cd modules/main
    ./run-jqtl.bash

If you're running on Windows you should be able to adapt run-jqtl.bash to a
bat script with few modifications.

CONTRIBUTING
============
J/qtl is open sourced and we welcome contributions. It is probably a good idea
to get in contact with us if you intend to make a contribution so that we can
coordinate and advise you on whether or not we think we will be able to accept
the proposed changes.
