package name.julatec.ekonomi.storage;

import name.julatec.ekonomi.tribunet.UuidFormatter;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Se quitaron de {@link UuidHexUserType} los dos métodos que recibían la sesión, porque
 * Hibernate 7 los marca para eliminación y el `default` de {@code UserType} ya delega en las
 * variantes con {@code WrapperOptions}. Esa delegación es lo que sostiene el cambio, y hasta
 * ahora no había ninguna prueba sobre este tipo: si una versión futura de Hibernate cambiara el
 * `default`, la clave primaria de dos entidades se leería o escribiría distinto sin que nada
 * avisara. Estas pruebas la fijan.
 */
class UuidHexUserTypeTest {

    private static final String HEX = "0123456789abcdef0123456789abcdef";

    private final UuidHexUserType tipo = new UuidHexUserType();

    /** Un ResultSet que solo sabe contestar getString(1). */
    private static ResultSet resultSetCon(String valor) {
        return (ResultSet) Proxy.newProxyInstance(
                UuidHexUserTypeTest.class.getClassLoader(), new Class<?>[]{ResultSet.class},
                (proxy, metodo, args) -> {
                    if (metodo.getName().equals("getString")) {
                        return valor;
                    }
                    throw new UnsupportedOperationException(metodo.getName());
                });
    }

    /** Un PreparedStatement que anota lo que se le pide, para comparar dos caminos. */
    private static PreparedStatement statementQueAnota(List<String> anotaciones) {
        return (PreparedStatement) Proxy.newProxyInstance(
                UuidHexUserTypeTest.class.getClassLoader(), new Class<?>[]{PreparedStatement.class},
                (proxy, metodo, args) -> {
                    anotaciones.add(metodo.getName() + "(" + java.util.Arrays.toString(args) + ")");
                    return null;
                });
    }

    private static SharedSessionContractImplementor sesion() {
        // Es un WrapperOptions: es lo que el default viejo le pasa al método nuevo.
        return (SharedSessionContractImplementor) Proxy.newProxyInstance(
                UuidHexUserTypeTest.class.getClassLoader(),
                new Class<?>[]{SharedSessionContractImplementor.class}, (p, m, a) -> null);
    }

    @Test
    @DisplayName("leer por el camino nuevo y por el viejo (vía sesión) da el mismo UUID")
    @SuppressWarnings({"deprecation", "removal"})
    void leerDaLoMismoPorLosDosCaminos() throws Exception {
        final UUID esperado = UuidFormatter.uuidFromString(HEX);

        final UUID nuevo = tipo.nullSafeGet(resultSetCon(HEX), 1, (WrapperOptions) sesion());
        final UUID viejo = tipo.nullSafeGet(resultSetCon(HEX), 1, sesion(), null);

        assertEquals(esperado, nuevo);
        assertEquals(esperado, viejo, "el default viejo tiene que delegar en el nuevo");
    }

    @Test
    @DisplayName("una columna nula o en blanco se lee como null, por los dos caminos")
    @SuppressWarnings({"deprecation", "removal"})
    void nuloYBlancoSonNull() throws Exception {
        assertNull(tipo.nullSafeGet(resultSetCon(null), 1, (WrapperOptions) sesion()));
        assertNull(tipo.nullSafeGet(resultSetCon("   "), 1, (WrapperOptions) sesion()));
        assertNull(tipo.nullSafeGet(resultSetCon(null), 1, sesion(), null));
    }

    @Test
    @DisplayName("escribir por el camino nuevo y por el viejo anota exactamente lo mismo: 32 dígitos, sin guiones")
    @SuppressWarnings({"deprecation", "removal"})
    void escribirDaLoMismoPorLosDosCaminos() throws Exception {
        final UUID uuid = UuidFormatter.uuidFromString(HEX);
        final List<String> nuevo = new ArrayList<>();
        final List<String> viejo = new ArrayList<>();

        tipo.nullSafeSet(statementQueAnota(nuevo), uuid, 3, (WrapperOptions) sesion());
        tipo.nullSafeSet(statementQueAnota(viejo), uuid, 3, sesion());

        assertEquals(List.of("setString([3, " + UuidFormatter.uuidToString(uuid) + "])"), nuevo);
        assertEquals(nuevo, viejo, "el default viejo tiene que delegar en el nuevo");
        assertEquals(32, UuidFormatter.uuidToString(uuid).length(), "formato de 32 dígitos, sin guiones");
    }

    @Test
    @DisplayName("escribir null llama a setNull con VARCHAR, por los dos caminos")
    @SuppressWarnings({"deprecation", "removal"})
    void escribirNullUsaSetNull() throws Exception {
        final List<String> nuevo = new ArrayList<>();
        final List<String> viejo = new ArrayList<>();

        tipo.nullSafeSet(statementQueAnota(nuevo), null, 1, (WrapperOptions) sesion());
        tipo.nullSafeSet(statementQueAnota(viejo), null, 1, sesion());

        assertEquals(List.of("setNull([1, " + Types.VARCHAR + "])"), nuevo);
        assertEquals(nuevo, viejo);
    }
}
