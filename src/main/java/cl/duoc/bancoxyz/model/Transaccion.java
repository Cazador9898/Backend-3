package cl.duoc.bancoxyz.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Transaccion {
    private Long id;
    private String fecha;
    private BigDecimal monto;
    private String tipo;
    private LocalDate fechaNormalizada;
    private boolean anomalia;
    private String observacion;

    public Transaccion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public LocalDate getFechaNormalizada() { return fechaNormalizada; }
    public void setFechaNormalizada(LocalDate fechaNormalizada) { this.fechaNormalizada = fechaNormalizada; }
    public boolean isAnomalia() { return anomalia; }
    public void setAnomalia(boolean anomalia) { this.anomalia = anomalia; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
