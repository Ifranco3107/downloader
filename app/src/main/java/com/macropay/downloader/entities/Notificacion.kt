package com.macropay.downloader.entities

import java.io.Serializable

class Notificacion(var titulo: String, var mensaje: String, var accion: String) : Serializable {
    override fun toString(): String {
        return "Notificacion{" +
                "titulo='" + titulo + '\'' +
                ", Mensaje='" + mensaje + '\'' +
                ", Accion='" + accion + '\'' +
                '}'
    }
}