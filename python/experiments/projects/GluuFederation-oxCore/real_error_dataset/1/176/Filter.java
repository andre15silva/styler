package org.gluu.search.filter;

import java.util.List;

/**
 * Simple filter without dependency to specific persistence filter mechanism
 *
 * @author Yuriy Movchan Date: 2017/12/12
 */
public class Filter {

    private FilterType type;

    private Filter[] filters;

    private String filterString;
    private String attributeName;
    private Object assertionValue;

    private String subInitial;
    private String[] subAny;
    private String subFinal;

    private boolean arrayAttribute = false;

    public Filter(FilterType type) {
        this.type = type;
    }

    public Filter(FilterType type, Filter... filters) {
        this(type);
        this.filters = filters;
    }

    public Filter(FilterType type, String filterString) {
        this(type);
        this.filterString = filterString;
    }

    public Filter(FilterType type, String attributeName, String assertionValue) {
        this(type);
        this.attributeName = attributeName;
        this.assertionValue = assertionValue;
    }

    public Filter(FilterType type, String attributeName, String subInitial, String[] subAny, String subFinal) {
        this(type);
        this.attributeName = attributeName;
        this.subInitial = subInitial;
        this.subAny = subAny;
        this.subFinal = subFinal;
    }

    /*
     * This method force filter to use specific syntax. It's not useful when we need
     * to support different persistent mechanisms
     */
    @Deprecated
    public static Filter create(final String filterString) {
        return new Filter(FilterType.RAW, filterString);
    }

    public static Filter createPresenceFilter(final String attributeName) {
        return new Filter(FilterType.PRESENCE, attributeName, null);
    }

    public static Filter createEqualityFilter(final String attributeName, final String assertionValue) {
        return new Filter(FilterType.EQUALITY, attributeName, assertionValue);
    }

    public static Filter createNOTFilter(final Filter filter) {
        return new Filter(FilterType.NOT, filter);
    }

    public static Filter createLessOrEqualFilter(final String attributeName, final String assertionValue) {
        return new Filter(FilterType.LESS_OR_EQUAL, attributeName, assertionValue);
    }

    public static Filter createGreaterOrEqualFilter(final String attributeName, final String assertionValue) {
        return new Filter(FilterType.GREATER_OR_EQUAL, attributeName, assertionValue);
    }

    public static Filter createApproximateMatchFilter(final String attributeName, final String assertionValue) {
        return new Filter(FilterType.APPROXIMATE_MATCH, attributeName, assertionValue);
    }

    public static Filter createSubstringFilter(final String attributeName, final String subInitial,
            final String[] subAny, final String subFinal) {
        return new Filter(FilterType.SUBSTRING, attributeName, subInitial, subAny, subFinal);
    }

    public static Filter createORFilter(final Filter... filters) {
        return new Filter(FilterType.OR, filters);
    }

    public static Filter createORFilter(final List<Filter> filters) {
        return new Filter(FilterType.OR, filters.toArray(new Filter[0]));
    }

    public static Filter createANDFilter(final Filter... filters) {
        return new Filter(FilterType.AND, filters);
    }

    public static Filter createANDFilter(final List<Filter> filters) {
        return new Filter(FilterType.AND, filters.toArray(new Filter[0]));
    }

    public final FilterType getType() {
        return type;
    }

    public final void setType(FilterType type) {
        this.type = type;
    }

    public final Filter[] getFilters() {
        return filters;
    }

    public final void setFilters(Filter[] filters) {
        this.filters = filters;
    }

    public final String getFilterString() {
        return filterString;
    }

    public final void setFilterString(String filterString) {
        this.filterString = filterString;
    }

    public final String getAttributeName() {
        return attributeName;
    }

    public final void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public final Object getAssertionValue() {
        return assertionValue;
    }

    public final void setAssertionValue(Object assertionValue) {
        this.assertionValue = assertionValue;
    }

    public final String getSubInitial() {
        return subInitial;
    }

    public final void setSubInitial(String subInitial) {
        this.subInitial = subInitial;
    }

    public final String[] getSubAny() {
        return subAny;
    }

    public final void setSubAny(String[] subAny) {
        this.subAny = subAny;
    }

    public final String getSubFinal() {
        return subFinal;
    }

    public final void setSubFinal(String subFinal) {
        this.subFinal = subFinal;
    }

    public final boolean isArrayAttribute() {
        return arrayAttribute;
    }

    public Filter arrayType() {
        this.arrayAttribute = true;
        return this;
    }

    @Override
    public String toString() {
        if (FilterType.RAW == this.type) {
            return this.filterString;
        }

        StringBuilder sb = new StringBuilder("(");

        if ((FilterType.NOT == this.type) || (FilterType.AND == this.type) || (FilterType.OR == this.type)) {
            if (this.filters != null) {
                sb.append(this.type.getSign());
                for (Filter filter : filters) {
                    sb.append(filter.toString());
                }
                sb.append(")");

                return sb.toString();
            }
        }
        if ((FilterType.EQUALITY == this.type) || (FilterType.LESS_OR_EQUAL == this.type)
                || (FilterType.GREATER_OR_EQUAL == this.type)) {
            return sb.append(this.attributeName).append(this.type.getSign()).append(this.assertionValue).append(')')
                    .toString();
        }

        if (FilterType.PRESENCE == this.type) {
            return sb.append(this.attributeName).append("=").append(this.type.getSign()).append(')').toString();
        }

        if (FilterType.APPROXIMATE_MATCH == this.type) {
            return sb.append(this.attributeName).append(this.type.getSign()).append("=").append(this.assertionValue)
                    .append(')').toString();
        }

        if (FilterType.SUBSTRING == this.type) {
            sb.append(this.attributeName).append(this.type.getSign());
            if (this.subInitial != null) {
                sb.append(this.subInitial);
                sb.append('*');
            }
            if (this.subAny != null) {
                sb.append('*');
                for (final String s : subAny) {
                    sb.append(s);
                    sb.append('*');
                }
            }
            if (this.subFinal != null) {
                sb.append('*');
                sb.append(this.subFinal);
            }
            sb.append(')');

            return sb.toString().replaceAll("\\*\\*","*");
        }

        return super.toString();
    }

}
