package org.h2.value;

import org.h2.api.ErrorCode;
import org.h2.message.DbException;

import java.io.Serializable;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for new {@link DataType} {@link Password}.
 * It used serialization while reading and writing data, which makes dataType available on every platform.
 */
public class Password implements Serializable {
    private String password;

    /** */
    private static final long serialVersionUID = 1L;

    private static final String ERROR_MESSAGE = "PASSWORD is not valid. Valid password constraint:" +
            "\nWhite spaces are not allowed.\n" +
            "Minimum length should be 8 and Maximum Length should be 20\n" +
            "It Should contain atleast: \n" +
            "\tOne Special character\n" +
            "\tOne Upper Case character\n" +
            "\tOne Lower Case character.";

    /**
     * Password Constructor. It validates password. Converts the plain string into SHA256 HASH.
     * To protect identity.
     *
     * @param password - Password of the User.
     * @throws DbException if password is Invalid.
     */
    public Password(String password) {
        if (isValid(password)) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
                this.password = String.format("%64x", new BigInteger(1, hash));
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {
            throw DbException.get(
                    ErrorCode.UNKNOWN_DATA_TYPE_1, ERROR_MESSAGE, null);
        }
    }


    /**
     * Method validates password as per criteria mentioned in {@link Password.ERROR_MESSAGE}.
     *
     * @param password - User Password in String format.
     * @return True if password satisfies all the criteria mentioned in the {@link Password.ERROR_MESSAGE}.
     */
    private boolean isValid(String password) {
        String regex = "^(?=.*[^a-zA-Z])(?=.*[a-z])(?=.*[A-Z])\\S{8,20}$";
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(password);
        return matcher.matches();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Password password1 = (Password) o;

        return password != null ? password.equals(password1.password) : password1.password == null;
    }

    @Override
    public int hashCode() {
        return password != null ? password.hashCode() : 0;
    }

    /**
     * Compares two password object.
     * @param object
     * @returns -1 if object is less than current object.
     *          0 if objects are equal;
     *          1 if object is greater than current object.
     */
    public int compare(Password object) {
        return this.password.compareTo(object.password);
    }

    @Override
    public String toString() {
        return password;
    }
}
