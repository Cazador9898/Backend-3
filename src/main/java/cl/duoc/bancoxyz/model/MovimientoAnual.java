package cl.duoc.bancoxyz.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MovimientoAnual {
    private Long cuentaId;
    private String fecha;
    private String transaccion;
    private BigDecimal monto;
    private String descripcion;
    private LocalDate fechaNormalizada;

    public MovimientoAnual() {}

    public Long getCuentaId() { return cuentaId; }
    public void setCuentaId(Long cuentaId) { this.cuentaId = cuentaId; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getTransaccion() { return transaccion; }
    public void setTransaccion(String transaccion) { this.transaccion = transaccion; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public LocalDate getFechaNormalizada() { return fechaNormalizada; }
    public void setFechaNormalizada(LocalDate fechaNormalizada) { this.fechaNormalizada = fechaNormalizada; }
}
