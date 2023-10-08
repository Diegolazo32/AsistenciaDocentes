package com.example.asistenciadocentes.Controladores.DataBase;

import java.lang.ref.SoftReference;

public class Permisos {
  String id_permiso;
  String tipo;
  String descripcion;
  String fecha_creacion;
  String fecha_incio;
  String fecha_fin;
  String imagen;
  Boolean estado;
  String id_usuario;
    public Permisos(String id_permiso, String tipo, String descripcion, String fecha_creacion, String fecha_incio, String fecha_fin, String imagen, Boolean estado, String id_usuario) {
        this.id_permiso = id_permiso;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.fecha_creacion = fecha_creacion;
        this.fecha_incio = fecha_incio;
        this.fecha_fin = fecha_fin;
        this.imagen = imagen;
        this.estado = estado;
        this.id_usuario = id_usuario;
    }
}

