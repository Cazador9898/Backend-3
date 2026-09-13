package cl.duoc.bancoxyz.processor;

import cl.duoc.bancoxyz.model.Transaccion;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Set;

public class TransaccionProcessor implements ItemProcessor<Transaccion, Transaccion> {
    private static final Set<String> TIPOS_VALIDOS = Set.of("debito", "credito");

    @Override
    public Transaccion process(Transaccion item) {
        if (item.getId() == null || item.getMonto() == null || item.getTipo() == null) {
            throw new IllegalArgumentException("Transacción con campos obligatorios incompletos");
        }

        LocalDate fecha = parseFecha(item.getFecha());
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha de transacción inválida: " + item.getFecha());
        }

        String tipo = item.getTipo().trim().toLowerCase();
        if (!TIPOS_VALIDOS.contains(tipo)) {
            throw new IllegalArgumentException("Tipo de transacción inválido: " + item.getTipo());
        }

        item.setFechaNormalizada(fecha);
        item.setTipo(tipo);

        boolean anomalia = item.getMonto().compareTo(BigDecimal.ZERO) <= 0;
        item.setAnomalia(anomalia);

        if (item.getMonto().compareTo(BigDecimal.ZERO) < 0) {
            item.setObservacion("Monto negativo detectado");
        } else if (item.getMonto().compareTo(BigDecimal.ZERO) == 0) {
            item.setObservacion("Monto igual a cero");
        } else {
            item.setObservacion("Transacción válida");
        }
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
