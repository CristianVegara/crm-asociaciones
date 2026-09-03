package com.aitsolutions.crmclient.informe;

import com.aitsolutions.crmclient.dto.InformeResumenResponse;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class InformeHtmlBuilder {
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private InformeHtmlBuilder() {
    }

    static String construir(InformeResumenResponse informe) {
        var p = informe.getPacientes();
        var s = informe.getSesiones();
        var sa = informe.getSanciones();
        var pl = informe.getPlanesServicio();
        long base = s.getVerde() + s.getNaranja() + s.getRojo();
        String html = """
                <!DOCTYPE html><html lang="es"><head><meta charset="UTF-8" />
                <style>
                @page { size:A4; margin:20mm 18mm; background:#f3f6fa; }
                * { box-sizing:border-box; } body { font-family:Helvetica,Arial,sans-serif;
                color:#1e293b; margin:0; font-size:10pt; line-height:1.5; }
                .header-bar { background:#102a43; color:#fff; margin:-20mm -18mm 25px;
                padding:26px 20mm; border-bottom:4px solid #1677a8; }
                .header-table { width:100%%; border-collapse:collapse; }
                .metrics-table { width:100%%; border-collapse:separate; border-spacing:10px; margin-left:-10px; margin-right:-10px; }
                .data-table { width:100%%; border-collapse:collapse; }
                .header-table td { vertical-align:middle; padding:0; }
                .doc-title { font-size:18pt; font-weight:700; margin:0 0 4px; text-transform:uppercase; }
                .doc-subtitle { font-size:10pt; color:#94a3b8; } .meta-box { text-align:right;
                font-size:9pt; color:#cbd5e1; } .meta-box strong { color:#fff; }
                .section-title { font-size:11pt; font-weight:700; color:#102a43;
                text-transform:uppercase; margin:20px 0 10px; padding-left:8px;
                border-left:3px solid #1677a8; }
                .metrics-table { margin-bottom:15px; } .metric-card { background:#fff;
                border:1px solid #d9e2ec; padding:13px 15px; text-align:center; }
                .metric-card { border-radius:6px; } .metric-label { font-size:8.5pt; text-transform:uppercase; color:#64748b; font-weight:600; margin-bottom:4px; }
                .metric-value { font-size:18pt; font-weight:700; color:#102a43; }
                .highlight { color:#1677a8; } .success { color:#16805b; }
                .data-table { background:#fff; border:1px solid #d9e2ec; margin-bottom:20px; }
                .data-table th { background:#eaf2f8; color:#243b53; font-size:8.5pt;
                text-transform:uppercase; letter-spacing:.5px; padding:10px 14px; text-align:left; border-bottom:1px solid #cbd5e1; }
                .data-table td { padding:10px 14px; color:#334155; border-bottom:1px solid #f1f5f9; }
                .center { text-align:center; } .right { text-align:right; }
                .badge { padding:2px 8px; border-radius:12px; font-size:8pt; font-weight:600; }
                .green { background:#dcfce7; color:#15803d; } .orange { background:#ffedd5; color:#c2410c; }
                .red { background:#fee2e2; color:#b91c1c; } .yellow { background:#fef9c3; color:#a16207; }
                .callout { background:#eaf5fb; border:1px solid #b7d9ea; border-left:4px solid #1677a8;
                padding:12px 16px; margin-top:20px; } .callout-title { color:#0369a1; font-weight:700; }
                .callout-desc { color:#0c4a6e; } .footer { margin-top:28px; text-align:center;
                font-size:8pt; color:#94a3b8; border-top:1px solid #e2e8f0; padding-top:8px; }
                </style></head><body>
                <div class="header-bar"><table class="header-table"><tr><td>
                <div class="doc-title">Informe Ejecutivo</div><div class="doc-subtitle">
                CRM ASOCIACIONES — RESUMEN DE ACTIVIDAD</div></td><td class="meta-box">
                <div><strong>Periodo:</strong> %s – %s</div><div><strong>Tipo de Reporte:</strong> %s</div>
                <div><strong>Fecha Emisión:</strong> %s</div></td></tr></table></div>
                <table class="metrics-table"><tr>
                <td><div class="metric-card"><div class="metric-label">Pacientes Activos</div><div class="metric-value">%d</div></div></td>
                <td><div class="metric-card"><div class="metric-label">Total Sesiones</div><div class="metric-value highlight">%d</div></div></td>
                <td><div class="metric-card"><div class="metric-label">Asistencia</div><div class="metric-value success">%.1f%%</div></div></td>
                <td><div class="metric-card"><div class="metric-label">Sanciones Totales</div><div class="metric-value">%d</div></div></td>
                </tr></table>
                <div class="section-title">1. Pacientes y Planes de Servicio</div>
                <table class="data-table"><tr><th>Categoría</th><th class="center">Cantidad</th><th>Estado</th></tr>
                <tr><td><strong>Pacientes Activos</strong></td><td class="center">%d</td><td>%d nuevo(s) en el periodo</td></tr>
                <tr><td><strong>Planes de Servicio Creados</strong></td><td class="center">%d</td><td>Alta efectuada correctamente</td></tr>
                <tr><td><strong>Planes de Servicio Finalizados</strong></td><td class="center">%d</td><td>Finalizaciones registradas</td></tr></table>
                <div class="section-title">2. Desglose de Sesiones y Asistencia</div>
                <table class="data-table"><tr><th>Estado</th><th class="center">Código</th><th class="center">Cantidad</th><th class="right">Distribución</th></tr>
                <tr><td>Realizada con éxito</td><td class="center"><span class="badge green">Verde</span></td><td class="center">%d</td><td class="right">%s%%</td></tr>
                <tr><td>Pendiente / Alerta</td><td class="center"><span class="badge orange">Naranja</span></td><td class="center">%d</td><td class="right">%s%%</td></tr>
                <tr><td>Ausencia / Incidencia</td><td class="center"><span class="badge red">Rojo</span></td><td class="center">%d</td><td class="right">%s%%</td></tr>
                <tr><td>Baja Médica</td><td class="center"><span class="badge yellow">Amarillo</span></td><td class="center">%d</td><td class="right">%s%%</td></tr></table>
                <div class="section-title">3. Sanciones</div><table class="data-table"><tr><th>Tipo</th><th class="center">Cantidad</th></tr>
                <tr><td>Automáticas</td><td class="center">%d</td></tr><tr><td>Manuales</td><td class="center">%d</td></tr>
                <tr><td><strong>Total</strong></td><td class="center"><strong>%d</strong></td></tr></table>
                <div class="callout"><div class="callout-title">Resumen Operativo</div>
                <div class="callout-desc">Durante el periodo evaluado se registraron %d sesiones, con una efectividad del %.1f%%,
                %d pacientes activos, %d planes creados y %d sanciones.</div></div>
                <div class="footer">CRM Asociaciones — Documento generado automáticamente para control administrativo interno.</div>
                </body></html>
                """.formatted(
                fecha(informe.getDesde()), fecha(informe.getHasta()), texto(informe.getPeriodo()), FECHA.format(LocalDate.now()),
                p.getActivos(), s.getTotal(), s.getPorcentajeAsistencia(), sa.getTotal(),
                p.getActivos(), p.getNuevosEnPeriodo(), pl.getCreadosEnPeriodo(), pl.getFinalizadosEnPeriodo(),
                s.getVerde(), porcentaje(s.getVerde(), base), s.getNaranja(), porcentaje(s.getNaranja(), base),
                s.getRojo(), porcentaje(s.getRojo(), base), s.getAmarillo(), porcentaje(s.getAmarillo(), base),
                sa.getAutomaticas(), sa.getManuales(), sa.getTotal(), s.getTotal(), s.getPorcentajeAsistencia(),
                p.getActivos(), pl.getCreadosEnPeriodo(), sa.getTotal());
        var servicios = informe.getServicios();
        if (servicios == null) {
            return html;
        }
        String seccionServicios = """
                <div class="section-title">4. Servicios</div>
                <table class="data-table"><tr><th>Indicador</th><th class="right">Cantidad</th></tr>
                <tr><td>Número de servicios</td><td class="right">%d</td></tr>
                <tr><td>Número de cancelaciones</td><td class="right">%d</td></tr>
                <tr><td>Porcentaje de cancelaciones</td><td class="right">%.1f%%</td></tr></table>
                <div class="section-title">5. Servicios por tipo</div>%s
                <div class="section-title">6. Servicios por sexo</div>%s
                <div class="section-title">7. Servicios por asociación</div>%s
                """.formatted(servicios.getTotal(), servicios.getCancelaciones(),
                servicios.getPorcentajeCancelaciones(), tabla(servicios.getPorServicio()),
                tabla(servicios.getPorSexo()), tabla(servicios.getPorAsociacion()));
        return html.replace("<div class=\"footer\">", seccionServicios + "<div class=\"footer\">");
    }

    private static String fecha(String valor) {
        try { return FECHA.format(LocalDate.parse(valor)); }
        catch (RuntimeException e) { return texto(valor); }
    }
    private static String porcentaje(long valor, long base) {
        return String.format(Locale.ROOT, "%.1f", base == 0 ? 0.0 : valor * 100.0 / base);
    }
    private static String texto(String valor) { return valor == null || valor.isBlank() ? "No especificado" : valor; }

    private static String tabla(java.util.Map<String, Long> datos) {
        StringBuilder html = new StringBuilder("<table class=\"data-table\"><tr><th>Nombre</th><th class=\"right\">Cantidad</th></tr>");
        datos.forEach((nombre, cantidad) -> html.append("<tr><td>").append(texto(nombre))
                .append("</td><td class=\"right\">").append(cantidad).append("</td></tr>"));
        return html.append("</table>").toString();
    }
}
