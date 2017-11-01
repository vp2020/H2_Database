package org.h2.value;

import org.h2.api.ErrorCode;
import org.h2.api.PasswordDataTypeHandler;
import org.h2.engine.SysProperties;
import org.h2.message.DbException;
import org.h2.util.JdbcUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/**
 * {@link Value} class for {@link Password} DataType.
 */
public class ValuePassword extends Value {
    private static final ValueString EMPTY = new ValueString("");

    /**
     * {@link Password} Object.
     */
    protected Password value;

    /**
     * Initialize Constructor.
     *
     * @param value - Password object.
     */
    protected ValuePassword(Password value) {
        assert value != null;
        this.value = value;
    }

    public static Value get(Password password) {
        return new ValuePassword(password);
    }

    @Override
    public String getSQL() {
        return value.toString();
    }

    @Override
    public int getType() {
        //TODO: May  need to replace it with String.
        return PasswordDataTypeHandler.PASSWORD_DATA_TYPE_ID;
    }

    @Override
    public long getPrecision() {
        return 0;
    }

    @Override
    public int getDisplaySize() {
        return 0;
    }

    @Override
    public String getString() {
        return value.toString();
    }

    @Override
    public Object getObject() {
        return value;
    }

    @Override
    public void set(PreparedStatement prep, int parameterIndex) throws SQLException {
        Object obj = JdbcUtils.deserialize(getBytesNoCopy(), getDataHandler());
        prep.setObject(parameterIndex, obj, Types.JAVA_OBJECT);
    }

    @Override
    protected int compareSecure(Value v, CompareMode mode) {
        return value.compare((Password) v.getObject());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ValuePassword
                && value.equals(((ValuePassword) other).value);
    }

    @Override
    public Value convertTo(int targetType) {
        if (getType() == targetType) {
            return this;
        }
        switch (targetType) {
            case Value.BYTES: {
                return ValueBytes.getNoCopy(JdbcUtils.serialize(value, null));
            }
            case Value.STRING: {
                return ValueString.get(value.toString());
            }
            case Value.JAVA_OBJECT: {
                return ValueJavaObject.getNoCopy(JdbcUtils.serialize(value, null));
            }
        }
        throw DbException.get(
                ErrorCode.DATA_CONVERSION_ERROR_1, getString());
    }

    /**
     * Gets object representation of {@link Password}.
     *
     * @param s                       - Password String.
     * @param treatEmptyStringsAsNull - flag to process empty string or not.
     * @return {@link Value} Object. {@link ValuePassword} in this case.
     */
    public static Value get(String s, boolean treatEmptyStringsAsNull) {
        if (s.isEmpty()) {
            return treatEmptyStringsAsNull ? ValueNull.INSTANCE : EMPTY;
        }
        ValuePassword obj = new ValuePassword(new Password(s));
        if (s.length() > SysProperties.OBJECT_CACHE_MAX_PER_ELEMENT_SIZE) {
            return obj;
        }
        return Value.cache(obj);
    }
}