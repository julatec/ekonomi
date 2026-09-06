package name.julatec.ekonomi.tribunet.storage;

/**
 * El SQL del catálogo de contrapartes, en constantes para poder reusar el mismo {@code from}
 * en la consulta y en la de conteo — Spring Data exige un {@code countQuery} propio para las
 * consultas nativas paginadas, y tenerlo escrito dos veces es una divergencia esperando pasar.
 * <p>
 * Tres decisiones que no son obvias:
 * <p>
 * <b>{@code union all}, no {@code union}.</b> El {@code union} de la consulta vieja funde
 * filas {@code (numero, nombre, conteo)} idénticas. Cuando una cédula aparece la misma
 * cantidad de veces como emisor y como receptor con la misma grafía, las dos ramas producen
 * la misma terna y se colapsan: el total sale exactamente a la mitad. Con conteos desiguales
 * el número viejo sí era correcto, que es por lo que el defecto no salta a la vista.
 * <p>
 * <b>Se agrupa solo por {@code numero}.</b> La consulta vieja agrupaba por
 * {@code (numero, nombre)} y el de-duplicado por grafía se hacía en memoria, lo que obligaba
 * a traer la tabla entera antes de poder paginar. El nombre representativo se elige acá con
 * {@code group_concat} ordenado por conteo: gana la grafía que aparece en más comprobantes,
 * igual que hacía el código Java, pero sumando todas las variantes en vez de quedarse con una.
 * <p>
 * <b>La rama del receptor excluye los documentos donde el receptor <i>es</i> el emisor.</b>
 * Sin esa condición se cuentan <b>apariciones</b> y no comprobantes: un documento con la misma
 * parte de los dos lados entra por las dos ramas. Medido contra la contabilidad real el 5 sep
 * 2026 — la columna decía 1.437 para una cédula que tiene 1.370 comprobantes, porque 67 la
 * llevan en los dos nodos. El número prometía uno y el clic mostraba otro, que es justo lo que
 * hace desconfiar de una pantalla.
 * <p>
 * <b>Las cinco tablas, no solo {@code factura}.</b> La consulta vieja solo miraba facturas,
 * así que el selector ofrecía menos cédulas de las que aceptan los reportes, que sí consultan
 * facturas de compra y notas.
 */
final class ClientesSql {

    private ClientesSql() {
    }

    private static final String RAMA_EMISOR = """
            select emisor_numero as numero, emisor_nombre as nombre, count(*) as c
              from %s where emisor_numero is not null and emisor_nombre is not null
             group by emisor_numero, emisor_nombre
            """;

    private static final String RAMA_RECEPTOR = """
            select receptor_numero as numero, receptor_nombre as nombre, count(*) as c
              from %s where receptor_numero is not null and receptor_nombre is not null
             group by receptor_numero, receptor_nombre
            """;

    /**
     * El filtro de fecha, repetido en las diez ramas — igual que {@code :patron}, no se puede
     * factorizar a un {@code where} de afuera porque el rango entra {@code UNION ALL} adentro.
     * Sin esto la columna "Comprobantes" era de TODO el histórico, sin importar qué rango
     * tuviera puesto la barra superior: prometía un número que "ver comprobantes" —que sí
     * respeta ese rango— nunca podía mostrar completo.
     * <p>
     * Concatenada con {@code +} y no con {@code String.formatted}: {@code @Query} exige una
     * constante de compilación, y una llamada a método —aunque el resultado sea siempre el
     * mismo— deja de serlo.
     */
    private static final String EN_RANGO = "and fecha_emision between :desde and :hasta\n";

    /**
     * El {@code from} completo. Es una constante de compilación —concatenación de literales—
     * para poder pegarla dentro de {@code @Query}, que no admite interpolación.
     */
    static final String FROM = """
            from (
                select emisor_numero as numero, emisor_nombre as nombre, count(*) as c
                  from factura where emisor_numero is not null and emisor_nombre is not null
                   """ + EN_RANGO + """
                 group by emisor_numero, emisor_nombre
                union all
                select receptor_numero, receptor_nombre, count(*)
                  from factura where receptor_numero is not null and receptor_nombre is not null
                   and (emisor_numero is null or emisor_numero <> receptor_numero)
                   """ + EN_RANGO + """
                 group by receptor_numero, receptor_nombre
                union all
                select emisor_numero, emisor_nombre, count(*)
                  from factura_compra where emisor_numero is not null and emisor_nombre is not null
                   """ + EN_RANGO + """
                 group by emisor_numero, emisor_nombre
                union all
                select receptor_numero, receptor_nombre, count(*)
                  from factura_compra where receptor_numero is not null and receptor_nombre is not null
                   and (emisor_numero is null or emisor_numero <> receptor_numero)
                   """ + EN_RANGO + """
                 group by receptor_numero, receptor_nombre
                union all
                select emisor_numero, emisor_nombre, count(*)
                  from factura_exportacion where emisor_numero is not null and emisor_nombre is not null
                   """ + EN_RANGO + """
                 group by emisor_numero, emisor_nombre
                union all
                select receptor_numero, receptor_nombre, count(*)
                  from factura_exportacion where receptor_numero is not null and receptor_nombre is not null
                   and (emisor_numero is null or emisor_numero <> receptor_numero)
                   """ + EN_RANGO + """
                 group by receptor_numero, receptor_nombre
                union all
                select emisor_numero, emisor_nombre, count(*)
                  from nota_credito where emisor_numero is not null and emisor_nombre is not null
                   """ + EN_RANGO + """
                 group by emisor_numero, emisor_nombre
                union all
                select receptor_numero, receptor_nombre, count(*)
                  from nota_credito where receptor_numero is not null and receptor_nombre is not null
                   and (emisor_numero is null or emisor_numero <> receptor_numero)
                   """ + EN_RANGO + """
                 group by receptor_numero, receptor_nombre
                union all
                select emisor_numero, emisor_nombre, count(*)
                  from nota_debito where emisor_numero is not null and emisor_nombre is not null
                   """ + EN_RANGO + """
                 group by emisor_numero, emisor_nombre
                union all
                select receptor_numero, receptor_nombre, count(*)
                  from nota_debito where receptor_numero is not null and receptor_nombre is not null
                   and (emisor_numero is null or emisor_numero <> receptor_numero)
                   """ + EN_RANGO + """
                 group by receptor_numero, receptor_nombre
            ) v
            group by v.numero
            having v.numero like :patron or max(v.nombre like :patron) = 1
            """;

    /**
     * Sin {@code order by}: lo pone {@code Pageable}. Si el SQL lo trajera, el orden que pida
     * la interfaz quedaría de tercer desempate y no se vería el efecto — una falla silenciosa,
     * peor que un error.
     */
    static final String BUSCAR = """
            select v.numero as numero,
                   sum(v.c)  as comprobantes,
                   substring_index(
                       group_concat(v.nombre order by v.c desc, v.nombre separator '\\n'),
                       '\\n', 1) as nombre
            """ + FROM;

    static final String CONTAR = "select count(*) from ( select v.numero " + FROM + " ) t";
}
