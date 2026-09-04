package com.aitsolutions.crm.auditoria;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_evento", indexes = {
        @Index(name = "idx_auditoria_fecha", columnList = "fecha"),
        @Index(name = "idx_auditoria_usuario", columnList = "usuario")
})
public class AuditoriaEvento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false, length = 10)
    private String metodo;

    @Column(nullable = false, length = 255)
    private String ruta;

    @Column(nullable = false)
    private int estadoHttp;

    @Column(length = 45)
    private String direccionIp;

    protected AuditoriaEvento() {
    }

    public AuditoriaEvento(String usuario, String metodo, String ruta, int estadoHttp, String direccionIp) {
        this.fecha = LocalDateTime.now();
        this.usuario = usuario;
        this.metodo = metodo;
        this.ruta = ruta;
        this.estadoHttp = estadoHttp;
        this.direccionIp = direccionIp;
    }

    public Long getId() { return id; }
    public LocalDateTime getFecha() { return fecha; }
    public String getUsuario() { return usuario; }
    public String getMetodo() { return metodo; }
    public String getRuta() { return ruta; }
    public int getEstadoHttp() { return estadoHttp; }
    public String getDireccionIp() { return direccionIp; }
}
