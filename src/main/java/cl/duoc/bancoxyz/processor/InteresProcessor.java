package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.model.InteresCuenta;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InteresProcessor implements ItemProcessor<InteresCuenta, InteresCuenta> {
    private final Set<String> registros = ConcurrentHashMap.newKeySet();

    @Override
    public InteresCuenta process(InteresCuenta item) {
        if (item.getCuentaId() == null || item.getNombre() == null || item.getSaldo() == null
                || item.getEdad() == null || item.getTipo() == null) {
            throw new IllegalArgumentException("Cuenta con campos obligatorios incompletos");
        }

        if (item.getEdad() < 18 || item.getEdad() > 100) {
            throw new IllegalArgumentException("Edad fuera de rango para cuenta " + item.getCuentaId());
        }
        if (item.getSaldo().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Saldo negativo para cuenta " + item.getCuentaId());
        }

        String nombre = item.getNombre().trim();
        String tipo = item.getTipo().trim().toLowerCase();
        if (!tipo.equals("ahorro") && !tipo.equals("prestamo")) {
            throw new IllegalArgumentException("Tipo de cuenta no soportado: " + item.getTipo());
        }

        // Detecta duplicados exactos del archivo legacy. Se filtran sin fallar el Job.
        String clave = nombre.toLowerCase() + "|" + item.getSaldo() + "|" + item.getEdad() + "|" + tipo;
        if (!registros.add(clave)) {
            return null;
        }

        BigDecimal tasa = tipo.equals("ahorro")
                ? new BigDecimal("0.0100")
                : new BigDecimal("0.0150");

        BigDecimal interes = item.getSaldo().multiply(tasa).setScale(2, RoundingMode.HALF_UP);
        BigDecimal saldoFinal = item.getSaldo().add(interes).setScale(2, RoundingMode.HALF_UP);

        item.setNombre(nombre);
        item.setTipo(tipo);
        item.setTasa(tasa);
        item.setInteresCalculado(interes);
        item.setSaldoFinal(saldoFinal);
        return item;
    }
}
