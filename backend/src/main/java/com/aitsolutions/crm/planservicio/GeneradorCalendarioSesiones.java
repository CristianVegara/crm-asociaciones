package com.aitsolutions.crm.planservicio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Solo calcula fechas, no toca base de datos ni conoce PlanServicio: mas facil de testear
 * y de reutilizar (se usa tanto al crear un plan como al regenerar sus sesiones futuras
 * cuando se edita, apartado 11 del plan).
 */
public final class GeneradorCalendarioSesiones {

    private GeneradorCalendarioSesiones() {
        // Clase de utilidades, no instanciable
    }

    public static List<LocalDate> generarFechas(LocalDate desde, LocalDate hasta, Set<DayOfWeek> diasSemana) {
        List<LocalDate> fechas = new ArrayList<>();

        if (desde.isAfter(hasta)) {
            return fechas;
        }

        LocalDate actual = desde;
        while (!actual.isAfter(hasta)) {
            if (diasSemana.contains(actual.getDayOfWeek())) {
                fechas.add(actual);
            }
            actual = actual.plusDays(1);
        }
        return fechas;
    }
}
