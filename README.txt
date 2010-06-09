J/qtl is a Java graphical user interface (GUI) for the popular QTL data analysis
software R/qtl. See http://churchill.jax.org/software/jqtl.shtml for details.

LICENSE
========
Copyright (c) 2010 The Jackson Laboratory

This software was developed by Gary Churchill's Lab at The Jackson
Laboratory (see http://churchill.jax.org).

This is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This software is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this software.  If not, see <http://www.gnu.org/licenses/>.

BUILDING
========
In order successfully complete these build instructions you must have:
* git
* ant >= 1.7.1
* java >= 1.5

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
