package name.julatec.ekonomi.tribunet.storage;

@SuppressWarnings("JpaDataSourceORMInspection")
@Entity(name = "factura")
@Table(indexes = {
        @Index(name = "emisor_numero_index", columnList = "emisor_numero"),
        @Index(name = "receptor_numero_index", columnList = "receptor_numero"),
        @Index(name = "numero_consecutivo_index", columnList = "numero_consecutivo"),
        @Index(name = "emisor_receptor_index", columnList = "emisor_numero,receptor_numero"),
        @Index(name = "fecha_emision_index", columnList = "fecha_emision"),
})

public class FacturaLineaDetalle implements ElectronicReceip {



}
