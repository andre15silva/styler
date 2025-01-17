/*
 * Copyright 2004-2018 H2 Group. Multiple-Licensed under the MPL 2.0, and the
 * EPL 1.0 (http://h2database.com/html/license.html). Initial Developer: H2
 * Group
 */
package org.h2.value;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import org.h2.api.ErrorCode;
import org.h2.api.TimestampWithTimeZone;
import org.h2.message.DbException;
import org.h2.util.DateTimeUtils;
import org.h2.util.StringUtils;

/**
 * Implementation of the TIMESTAMP WITH TIME ZONE data type.
 *
 * @see <a href="https://en.wikipedia.org/wiki/ISO_8601#Time_zone_designators">
 *      ISO 8601 Time zone designators</a>
 */
public class ValueTimestampTimeZone extends Value {

    /**
     * The precision in digits.
     */
    public static final int PRECISION = 30;

    /**
     * The display size of the textual representation of a timestamp. Example:
     * 2001-01-01 23:59:59.000 +10:00
     */
    public static final int DISPLAY_SIZE = 30;

    /**
     * The default scale for timestamps.
     */
    static final int DEFAULT_SCALE = 10;

    /**
     * A bit field with bits for the year, month, and day (see DateTimeUtils for
     * encoding)
     */
    private final long dateValue;
    /**
     * The nanoseconds since midnight.
     */
    private final long timeNanos;
    /**
     * Time zone offset from UTC in minutes, range of -18 hours to +18 hours. This
     * range is compatible with OffsetDateTime from JSR-310.
     */
    private final short timeZoneOffsetMins;

    private ValueTimestampTimeZone(long dateValue, long timeNanos,
            short timeZoneOffsetMins) {
        if (timeNanos < 0 || timeNanos >= 24L * 60 * 60 * 1000 * 1000 * 1000) {
            throw new IllegalArgumentException(
                    "timeNanos out of range " + timeNanos);
        }
        /*
         * Some current and historic time zones have offsets larger than 12 hours.
         * JSR-310 determines 18 hours as maximum possible offset in both directions, so
         * we use this limit too for compatibility.
         */
        if (timeZoneOffsetMins < (-18 * 60)
                || timeZoneOffsetMins > (18 * 60)) {
            throw new IllegalArgumentException(
                    "timeZoneOffsetMins out of range " + timeZoneOffsetMins);
        }
        this.dateValue = dateValue;
        this.timeNanos = timeNanos;
        this.timeZoneOffsetMins = timeZoneOffsetMins;
    }

    /**
     * Get or create a date value for the given date.
     *
     * @param dateValue the date value, a bit field with bits for the year,
     *            month, and day
     * @param timeNanos the nanoseconds since midnight
     * @param timeZoneOffsetMins the timezone offset in minutes
     * @return the value
     */
    public static ValueTimestampTimeZone fromDateValueAndNanos(long dateValue,
            long timeNanos, short timeZoneOffsetMins) {
        return (ValueTimestampTimeZone) Value.cache(new ValueTimestampTimeZone(
                dateValue, timeNanos, timeZoneOffsetMins));
    }

    /**
     * Get or create a timestamp value for the given timestamp.
     *
     * @param timestamp the timestamp
     * @return the value
     */
    public static ValueTimestampTimeZone get(TimestampWithTimeZone timestamp) {
        return fromDateValueAndNanos(timestamp.getYMD(),
                timestamp.getNanosSinceMidnight(),
                timestamp.getTimeZoneOffsetMins());
    }

    /**
     * Parse a string to a ValueTimestamp. This method supports the format
     * +/-year-month-day hour:minute:seconds.fractional and an optional timezone
     * part.
     *
     * @param s the string to parse
     * @return the date
     */
    public static ValueTimestampTimeZone parse(String s) {
        try {
            return (ValueTimestampTimeZone) DateTimeUtils.parseTimestamp(s, null, true);
        } catch (Exception e) {
            throw DbException.get(ErrorCode.INVALID_DATETIME_CONSTANT_2, e,
                    "TIMESTAMP WITH TIME ZONE", s);
        }
    }

    /**
     * A bit field with bits for the year, month, and day (see DateTimeUtils for
     * encoding).
     *
     * @return the data value
     */
    public long getDateValue() {
        return dateValue;
    }

    /**
     * The nanoseconds since midnight.
     *
     * @return the nanoseconds
     */
    public long getTimeNanos() {
        return timeNanos;
    }

    /**
     * The timezone offset in minutes.
     *
     * @return the offset
     */
    public short getTimeZoneOffsetMins() {
        return timeZoneOffsetMins;
    }

    /**
     * Returns compatible offset-based time zone with no DST schedule.
     *
     * @return compatible offset-based time zone
     */
    public TimeZone getTimeZone() {
        int offset = timeZoneOffsetMins;
        if (offset == 0) {
            return DateTimeUtils.UTC;
        }
        StringBuilder b = new StringBuilder(9);
        b.append("GMT");
        if (offset < 0) {
            b.append('-');
            offset = - offset;
        } else {
            b.append('+');
        }
        StringUtils.appendZeroPadded(b, 2, offset / 60);
        b.append(':');
        StringUtils.appendZeroPadded(b, 2, offset % 60);
        return new SimpleTimeZone(offset * 60000, b.toString());
    }

    @Override
    public Timestamp getTimestamp() {
        throw new UnsupportedOperationException("unimplemented");
    }

    @Override
    public int getType() {
        return Value.TIMESTAMP_TZ;
    }

    @Override
    public String getString() {
        return DateTimeUtils.timestampTimeZoneToString(dateValue, timeNanos, timeZoneOffsetMins);
    }

    @Override
    public String getSQL() {
        return "TIMESTAMP WITH TIME ZONE '" + getString() + "'";
    }

    @Override
    public long getPrecision() {
        return PRECISION;
    }

    @Override
    public int getScale() {
        return DEFAULT_SCALE;
    }

    @Override
    public int getDisplaySize() {
        return DISPLAY_SIZE;
    }

    @Override
    public Value convertScale(boolean onlyToSmallerScale, int targetScale) {
        if (targetScale >= DEFAULT_SCALE) {
            return this;
        }
        if (targetScale < 0) {
            throw DbException.getInvalidValueException("scale", targetScale);
        }
        long n = timeNanos;
        BigDecimal bd = BigDecimal.valueOf(n);
        bd = bd.movePointLeft(9);
        bd = ValueDecimal.setScale(bd, targetScale);
        bd = bd.movePointRight(9);
        long n2 = bd.longValue();
        if (n2 == n) {
            return this;
        }
        return fromDateValueAndNanos(dateValue, n2, timeZoneOffsetMins);
    }

    @Override
    protected int compareSecure(Value o, CompareMode mode) {
        ValueTimestampTimeZone t = (ValueTimestampTimeZone) o;
        // Maximum time zone offset is +/-18 hours so difference in days between local
        // and UTC cannot be more than one day
        long daysA = DateTimeUtils.absoluteDayFromDateValue(dateValue);
        long timeA = timeNanos - timeZoneOffsetMins * 60_000_000_000L;
        if (timeA < 0) {
            timeA += DateTimeUtils.NANOS_PER_DAY;
            daysA--;
        } else if (timeA >= DateTimeUtils.NANOS_PER_DAY) {
            timeA -= DateTimeUtils.NANOS_PER_DAY;
            daysA++;
        }
        long daysB = DateTimeUtils.absoluteDayFromDateValue(t.dateValue);
        long timeB = t.timeNanos - t.timeZoneOffsetMins * 60_000_000_000L;
        if (timeB < 0) {
            timeB += DateTimeUtils.NANOS_PER_DAY;
            daysB--;
        } else if (timeB >= DateTimeUtils.NANOS_PER_DAY) {
            timeB -= DateTimeUtils.NANOS_PER_DAY;
            daysB++;
        }
        int cmp = Long.compare(daysA, daysB);
        if (cmp != 0) {
            return cmp;
        }
        return Long.compare(timeA, timeB);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof ValueTimestampTimeZone)) {
            return false;
        }
        ValueTimestampTimeZone x = (ValueTimestampTimeZone) other;
        return dateValue == x.dateValue && timeNanos == x.timeNanos
                && timeZoneOffsetMins == x.timeZoneOffsetMins;
    }

    @Override
    public int hashCode() {
        return (int) (dateValue ^ (dateValue >>> 32) ^ timeNanos
                ^ (timeNanos >>> 32) ^ timeZoneOffsetMins);
    }

    @Override
    public Object getObject() {
        return new TimestampWithTimeZone(dateValue, timeNanos,
                timeZoneOffsetMins);
    }

    @Override
    public void set(PreparedStatement prep, int parameterIndex)
            throws SQLException {
        prep.setString(parameterIndex, getString());
    }

    @Override
    public Value add(Value v) {
        throw DbException.getUnsupportedException(
                "manipulating TIMESTAMP WITH TIME ZONE values is unsupported");
    }

    @Override
    public Value subtract(Value v) {
        throw DbException.getUnsupportedException(
                "manipulating TIMESTAMP WITH TIME ZONE values is unsupported");
    }

}
