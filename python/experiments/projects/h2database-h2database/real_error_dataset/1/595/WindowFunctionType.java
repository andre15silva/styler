/*
 * Copyright 2004-2018 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.expression.aggregate;

/**
 * A type of a window function.
 */
public enum WindowFunctionType {

    /**
     * The type for ROW_NUMBER() window function.
     */
    ROW_NUMBER,

    /**
     * The type for RANK() window function.
     */
    RANK,

    /**
     * The type for DENSE_RANK() window function.
     */
    DENSE_RANK,

    /**
     * The type for PERCENT_RANK() window function.
     */
    PERCENT_RANK,

    /**
     * The type for CUME_DIST() window function.
     */
    CUME_DIST,

    /**
     * The type for LEAD() window function.
     */
    LEAD,

    /**
     * The type for LAG() window function.
     */
    LAG,

    /**
     * The type for FIRST_VALUE() window function.
     */
    FIRST_VALUE,

    /**
     * The type for LAST_VALUE() window function.
     */
    LAST_VALUE,

    /**
     * The type for NTH_VALUE() window function.
     */
    NTH_VALUE,

    ;

    /**
     * Returns the type of window function with the specified name, or null.
     *
     * @param name
     *            name of a window function
     * @return the type of window function, or null.
     */
    public static WindowFunctionType get(String name) {
        switch (name) {
        case "ROW_NUMBER":
            return ROW_NUMBER;
        case "RANK":
            return RANK;
        case "DENSE_RANK":
            return DENSE_RANK;
        case "PERCENT_RANK":
            return PERCENT_RANK;
        case "CUME_DIST":
            return CUME_DIST;
        case "LEAD":
            return LEAD;
        case "LAG":
            return LAG;
        case "FIRST_VALUE":
            return FIRST_VALUE;
        case "LAST_VALUE":
            return LAST_VALUE;
        case "NTH_VALUE":
            return NTH_VALUE;
        default:
            return null;
        }
    }

}