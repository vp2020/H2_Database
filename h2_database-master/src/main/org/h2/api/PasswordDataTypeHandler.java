package org.h2.api;

import org.h2.api.CustomDataTypesHandler;
import org.h2.api.ErrorCode;
import org.h2.message.DbException;
import org.h2.store.DataHandler;
import org.h2.util.JdbcUtils;
import org.h2.value.*;

import java.sql.Types;
import java.util.Locale;

/**
 * This provides feature to plug-in new DataType Password.
 */
public class PasswordDataTypeHandler implements CustomDataTypesHandler {
    /**
     * String representation for {@link Password} {@link DataType}
     */
    public final static String PASSWORD_DATA_TYPE_NAME = "password";

    public final DataType passwordDataType;
    /**
     * Type ID for {@link Password} {@link DataType};
     */
    public final static int PASSWORD_DATA_TYPE_ID = Value.PASSWORD;

    /**
     * Order for complex number data type
     */
    public final static int PASSWORD_DATA_TYPE_ORDER = 53_000;

    /**
     * Initializes Password DataType object.
     */
    public PasswordDataTypeHandler() {
        passwordDataType = createPassword();
    }

    /**
     * Creates Password DataType Object.
     * @return Password DataType Object.
     */
    private DataType createPassword() {
        DataType result = new DataType();
        result.type = Value.PASSWORD;
        result.name = PASSWORD_DATA_TYPE_NAME;
        result.sqlType = Types.JAVA_OBJECT;
        result.autoIncrement = true;
        result.decimal = true;
        result.maxPrecision = Integer.MAX_VALUE;
        result.maxScale = 0;
        result.minScale = 0;
        result.params = "LENGTH";
        result.prefix = "'";
        result.suffix = "'";
        result.supportsPrecision = true;
        result.supportsScale = false;
        result.defaultPrecision = Integer.MAX_VALUE;
        result.defaultScale = 0;
        result.defaultDisplaySize = Integer.MAX_VALUE;
        result.caseSensitive = true;
        result.hidden = false;
        result.memory = 24;
        return result;
    }

    @Override
    public DataType getDataTypeByName(String name) {
        if (name.toLowerCase(Locale.ENGLISH).equals(PASSWORD_DATA_TYPE_NAME)) {
            return passwordDataType;
        }
        return null;
    }

    @Override
    public DataType getDataTypeById(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return passwordDataType;
        }
        return null;
    }

    @Override
    public String getDataTypeClassName(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return Password.class.getName();
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + type);
    }

    @Override
    public int getTypeIdFromClass(Class<?> cls) {
        if (cls == Password.class) {
            return PASSWORD_DATA_TYPE_ID;
        }
        return Value.JAVA_OBJECT;
    }

    @Override
    public Value convert(Value source, int targetType) {
        if (source.getType() == targetType) {
            return source;
        }
        if (targetType == PASSWORD_DATA_TYPE_ID) {
            switch (source.getType()) {
                case Value.JAVA_OBJECT: {
                    assert source instanceof ValueJavaObject;
                    return ValuePassword.get((Password)
                            JdbcUtils.deserialize(source.getBytesNoCopy(), null));
                }
                case Value.STRING: {
                    assert source instanceof ValueString;
                    return ValuePassword.get(new Password(source.getString()));
                }
                case Value.BYTES: {
                    assert source instanceof ValueBytes;
                    return ValuePassword.get((Password) JdbcUtils.deserialize(source.getBytesNoCopy(), null));
                }
            }

            throw DbException.get(
                    ErrorCode.DATA_CONVERSION_ERROR_1, source.getString());
        } else {
            return source.convertTo(targetType);
        }
    }


    @Override
    public int getDataTypeOrder(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return PASSWORD_DATA_TYPE_ORDER;
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + type);
    }

    @Override
    public Value getValue(int type, Object data, DataHandler dataHandler) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            assert data instanceof Password;
            return ValuePassword.get((Password) data);
        }
        return ValueJavaObject.getNoCopy(data, null, dataHandler);
    }

    @Override
    public Object getObject(Value value, Class<?> cls) {
        if (cls.equals(Password.class)) {
            if (value.getType() == PASSWORD_DATA_TYPE_ID) {
                return value.getObject();
            }
            return convert(value, PASSWORD_DATA_TYPE_ID).getObject();
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + value.getType());
    }

    @Override
    public boolean supportsAdd(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return true;
        }
        return false;
    }

    @Override
    public int getAddProofType(int type) {
        if (type == PASSWORD_DATA_TYPE_ID) {
            return type;
        }
        throw DbException.get(
                ErrorCode.UNKNOWN_DATA_TYPE_1, "type:" + type);
    }
}
