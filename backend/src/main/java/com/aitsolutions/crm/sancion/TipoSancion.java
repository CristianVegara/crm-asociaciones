package com.aitsolutions.crm.sancion;

/**
 * SUSPENSION_TEMPORAL cubre tanto las automaticas por umbral de faltas (3 ROJAS / 6 NARANJAS,
 * apartado 4 del plan) como las que un trabajador decida aplicar manualmente.
 * TARJETA_AMARILLA_AGRESION es un concepto totalmente distinto del AMARILLO del semaforo de
 * asistencia (que es una falta justificada y no cuenta para nada): aqui es una sancion
 * disciplinaria manual por agresion, que prescribe al año (decision tomada con Cristian,
 * el propio plan tenia esto ambiguo en el apartado 4).
 */
public enum TipoSancion {
    SUSPENSION_TEMPORAL,
    TARJETA_AMARILLA_AGRESION,
    OTRA
}
