package name.julatec.ekonomi.storage;

import name.julatec.ekonomi.tribunet.UuidFormatter;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.SqlTypes;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.UserType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;

/**
 * UUID guardado como 32 dígitos hexadecimales, sin guiones.
 * <p>
 * Existe porque Hibernate 7 dejó de aceptar un {@code AttributeConverter} sobre
 * un atributo anotado {@code @Id}: la aplicación no arrancaba con
 * {@code 'AttributeConverter' not allowed for attribute 'guid'}. Un
 * {@link UserType} sí está permitido en una clave primaria y hace lo mismo.
 * <p>
 * El formato no es negociable y por eso no se usa el mapeo de UUID que trae
 * Hibernate: aquel escribe los 36 caracteres con guiones, mientras que estas
 * columnas son {@code length = 32} y ya tienen años de datos escritos por
 * {@link UuidFormatter}, que quita los guiones. Cambiar el formato no sería una
 * migración: sería dejar de encontrar las filas que ya están.
 */
public class UuidHexUserType implements UserType<UUID> {

    @Override
    public int getSqlType() {
        return SqlTypes.VARCHAR;
    }

    @Override
    public Class<UUID> returnedClass() {
        return UUID.class;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public UUID deepCopy(UUID value) {
        return value;
    }

    @Override
    public UUID nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
        final String texto = rs.getString(position);
        return texto == null || texto.isBlank() ? null : UuidFormatter.uuidFromString(texto);
    }

    @Override
    public UUID nullSafeGet(ResultSet rs, int position,
                            SharedSessionContractImplementor session, Object owner) throws SQLException {
        return nullSafeGet(rs, position, (WrapperOptions) session);
    }

    @Override
    public void nullSafeSet(PreparedStatement st, UUID value, int index, WrapperOptions options)
            throws SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, UuidFormatter.uuidToString(value));
        }
    }

    @Override
    public void nullSafeSet(PreparedStatement st, UUID value, int index,
                            SharedSessionContractImplementor session) throws SQLException {
        nullSafeSet(st, value, index, (WrapperOptions) session);
    }
}
