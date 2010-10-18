/*
 * Copyright (c) 2009 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jax.qtl;

import java.text.DecimalFormat;

/**
 * @author Lei Wu
 * @version 1.0
 */
// TODO Constants needs to be broken up into the packages that need the constants
// in some cases and turned into a properties bundle in other cases
@SuppressWarnings("all")
public interface Constants {
    // main jframe setup stuff & labels.
    static final String EMAIL_STRING = "jqtl@jax.org";
    static final String EOL = System.getProperty("line.separator");
    static final String TAB = "\t";

    // UI settings
    static final int CROSS_COMBOBOX_WIDTH = 250;
    static final int STANDARD_TABLE_HEIGHT = 150;
    static final int STATNDARD_TABLE_WIDTH = 250;
    static final int CHECK_BOX_TABLE_CELL_WIDTH = 50;
    static final int CHECK_BOX_VERTICAL_TABLE_CELL_WIDTH = 20;
    static final String ERROR_TITLE = "Error";
    static final String WARNING_TITLE = "Warning";
    static final int TEXT_FIELD_WIDTH = 150;
    static final String CHOOSE_PARMETER_TITLE = " Choose Parameters ";

    // cross type
    static final int BACKCROSS = 0, INTERCROSS = 1, FOURWAYCROSS = 2;
    static final String[] CROSS_TYPE = new String[] {"Backcross","f2 intercross","4 way cross"};

    // function option lists
    static final String[] MAP_FUNCTION = {" Haldane", " Kosambi", " Carter-Falconer", " Morgan"};
    static final String[] STEP_WIDTH = {" fixed", " variable"};
    static final String[] RIPPLE_METHOD = {" countxo", " likelihood"};
    static final String[] TRUE_FALSE = {"TRUE", "FALSE"};
    // calc.genoprob, sim.geno and est.map label
    static final String MAP_FUNCTION_LABEL = "Map Function";
    static final String STEP_SIZE_LABEL = "Step Size(cM)";
    static final String NUM_DRAWS_LABEL = "Number of Imputations";
    static final String GENO_ERROR_RATE_LABEL = "Genotyping error rate";
    static final String DIST_PAST_TERMINAL_LABEL = "Distance past terminal";
    static final String STEP_WIDTH_TYPE_LABEL = "Step width type";
    static final String M_LABEL = "Interference parameter for the chi-square model";
    static final String P_LABEL = "Proportion of chiasmata from the NI mechanism, in the Stahl model";
    static final String SEX_SP_LABEL = "Estimate sex specific map";
    static final String ERROR_PROB_DEFAULT = "0.0001";
    static final String OFF_END_DEFAULT = "0.0";
    static final String ZERO = "0";

    // output format
    static final DecimalFormat ONE_DIGIT_FORMATTER = new DecimalFormat("0.0");
    static final DecimalFormat THREE_DIGIT_FORMATTER = new DecimalFormat("0.000");
    static final DecimalFormat FOUR_DIGIT_FORMATTER = new DecimalFormat("0.0000");
    static final DecimalFormat FIVE_DIGIT_FORMATTER = new DecimalFormat("0.00000");
    static final DecimalFormat ZERO_DIGIT_FORMATTER = new DecimalFormat("0");

    // scan, ripple parameter
    static final String[] USE_ALL = {" all observations"," complete observations"};
    static final int MAXIT_DEFAULT = 4000;
    static final String TOL_DEFAULT = "1e-4";
    static final double TOL_DEFAULT_NUM = 0.0001;
    static final String SELECT_PHENO_TITLE = " Select phenos ";
    static final String SELECT_CHROMOSOME_TITLE = " Select Chromosomes ";

    // scan summary label
    static final String SCAN_MODEL_LABEL = "Phenotypic Model";
    static final String SCAN_METHOD_LABEL = "Scan Method";
    static final String USE_LABEL = "Use";
    static final String MAXIT_LABEL = "Maximum number of iterations";
    static final String TOL_LABEL = "Tolerance value for determining convergence";
    static final String NUM_PERM_LABEL = "Number of Permutations";
    static final String SEP_PERM_LABEL = "Separate permutations for autosomes and the X chromosome";
    static final String UPPER_LABEL = "Upper";
    static final String TIES_RANDOM_LABEL = "Ties.random";
    static final String INCL_MARKERS_LABEL = "incl.markers";
    static final String CLEAN_OUTPUT_LABEL = "clean.output";

    // scan cov table header
    static final String[] covTableColumnName = new String[] {"Additive","Interactive","Covariates"};

    // ci plot
    static final double DEFAULT_LOD_INT = 1.5;
    static final double DEFAULT_BAYES_INT = 0.95;

    // phenoData
    static final int NOT_SEX = 0;
    static final int MIX_SEX = 1;
    static final int MALE_SEX = 2;
    static final int FEMALE_SEX = 3;

    // scantwo plot
    static final int NUM_COLORS = 256;
    static final String[] LOD_TYPE = new String[] {"full","add","cond-int","cond-add","int"};
    static final int LOD_FULL = 0;
    static final int LOD_ADD = 1;
    static final int LOD_COND_INT = 2;
    static final int LOD_COND_ADD = 3;
    static final int LOD_INT = 4;
    static final String UPPER_LOD_LABEL = "LOD scores in the upper triangle";
    static final String LOWER_LOD_LABEL = "LOD scores in the lower triangle";
    static final String[] COLOR_MAP_LIST = new String[] {"redblue", "cm", "gray", "heat", "terrain", "topo"};
    static final String COLOR_MAP_LABEL = "Color map";
    static final String ADD_COLOR_MAP_SCALE = "Add color map scale";

    // RF plot
    static final int MAX_RF_LOD = 12;

    // organization
    static final String NO_SCANONE_MESSAGE = "this cross doesn't have any scanone result";
    static final String NO_SCANTWO_MESSAGE = "this cross doesn't have any scantwo result";
    static final String NO_QTL_BASKET_MESSAGE = "this cross doesn't have any QTL basket";
}

