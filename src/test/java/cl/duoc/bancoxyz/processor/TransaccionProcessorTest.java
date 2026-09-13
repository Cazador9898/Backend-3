package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.model.Transaccion;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class TransaccionProcessorTest {
    @Test
    void marcaMontoNegativoComoAnomalia() throws Exception {
        Transaccion t = new Transaccion();
        t.setId(1L);
        t.setFecha("2024-01-01");
        t.setMonto(new BigDecimal("-100"));
        t.setTipo("debito");

        Transaccion r = new TransaccionProcessor().process(t);

        assertNotNull(r);
        assertTrue(r.isAnomalia());
        assertEquals("Monto negativo detectado", r.getObservacion());
    }
}
