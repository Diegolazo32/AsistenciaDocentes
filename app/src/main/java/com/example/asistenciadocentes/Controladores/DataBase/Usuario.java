package com.example.asistenciadocentes.Controladores.DataBase;

public class Usuario {
    public String Nombre;

    //Comentario de prueba
    public String Titulo;
    public String Correo;
    public String Dias;
    public String Entrada;
    public String Salida;
    public boolean Estado;
    public String Rol;
    public String PP;
    public String Codigo;

    public Usuario(String Nombre, String Titulo, String Correo, String Dias, String Entrada, String Salida, boolean Estado, String Rol, String PP, String Codigo){
        this.Nombre = Nombre;
        this.Titulo = Titulo;
        this.Correo = Correo;
        this.Dias = Dias;
        this.Entrada = Entrada;
        this.Salida = Salida;
        this.Estado = Estado;
        this.Rol = Rol;
        this.PP = PP;
        this.Codigo = Codigo;
    }


    public Usuario() {

    }
}

