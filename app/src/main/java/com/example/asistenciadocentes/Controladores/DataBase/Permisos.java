package com.example.asistenciadocentes.Controladores.DataBase;

import java.lang.ref.SoftReference;

public class Permisos {
  public String id_permiso;
  public String tipo;
  public String descripcion;
  public String fecha_creacion;
  public String fecha_incio;
  public String fecha_fin;
  public String imagen;
  public String estado;
  public String id_usuario;

    public Permisos(String id_permiso, String tipo, String descripcion, String fecha_creacion, String fecha_incio, String fecha_fin, String imagen, String estado, String id_usuario) {
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

