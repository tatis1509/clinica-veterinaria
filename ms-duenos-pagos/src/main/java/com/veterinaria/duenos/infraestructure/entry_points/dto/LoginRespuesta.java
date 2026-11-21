package com.veterinaria.duenos.infraestructure.entry_points.dto;


public record LoginRespuesta(boolean autenticado, String mensaje, DuenoRespuesta dueno) {
}
