package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.model.MovimientoAnual;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class MovimientoAnualProcessor implements ItemProcessor<MovimientoAnual, MovimientoAnual> {
    private static final Set<String> TIPOS_VALIDOS = Set.of("deposito", "retiro", "compra");

    @Override
    public MovimientoAnual process(MovimientoAnual item) {
        if (item.getCuentaId() == null || item.getMonto() == null || item.getTransaccion() == null) {
            throw new IllegalArgumentException("Movimiento anual con campos obligatorios incompletos");
        }

        LocalDate fecha = parseFecha(item.getFecha());
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha anual inválida: " + item.getFecha());
        }

        String tipo = item.getTransaccion().trim().toLowerCase();
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de movimiento inválido: " + item.getTransaccion());
        }

        // Corrige signos para dejar consistente la data legacy.
        BigDecimal monto = item.getMonto().abs();
        if (tipo.equals("retiro") || tipo.equals("compra")) {
            monto = monto.negate();
        }

        if (item.getDescripcion() == null || item.getDescripcion().isBlank()) {
            item.setDescripcion("Sin descripción");
        } else {
            item.setDescripcion(item.getDescripcion().trim());
        }

        item.setFechaNormalizada(fecha);
        item.setTransaccion(tipo);
        item.setMonto(monto);
        return item;
    }

    private LocalDate parseFecha(String value) {
        if (value == null || value.isBlank()) return null;
        DateTimeFormatter[] formatos = {
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        };
        for (DateTimeFormatter formato : formatos) {
            try {
                return LocalDate.parse(value.trim(), formato);
            } catch (DateTimeParseException ignored) {
                // Se intenta con el siguiente formato.
            }
        }
        return null;
    }
}
