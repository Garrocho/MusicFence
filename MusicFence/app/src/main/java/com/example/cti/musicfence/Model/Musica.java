package com.example.cti.musicfence.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Musica implements Parcelable{
    private int id;
    private String artista;
    private String titulo;
    private String path;
    private String nomeArquivo;
    private int duracao;

    public Musica() {

    }

    public Musica(int id, String artista, String title, String path, String nome, int duracao) {
        super();
        this.id = id;
        this.artista = artista;
        this.titulo = title;
        this.path = path;
        this.nomeArquivo = nome;
        this.duracao = duracao;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getArtista() {
        return artista;
    }

    public void setArtista(String artista) {
        this.artista = artista;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String title) {
        this.titulo = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nome) {
        this.nomeArquivo = nome;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    @Override
    public String toString() {
        //return String.format("%d - %s - %s - %s - %s - %d", id, artista, titulo, path, nomeArquivo, duracao);
        return String.format("%s", titulo);
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(artista);
        dest.writeString(titulo);
        dest.writeString(path);
        dest.writeString(nomeArquivo);
        dest.writeInt(duracao);
    }

    public static final Parcelable.Creator<Musica> CREATOR = new Parcelable.Creator<Musica>() {
        public Musica createFromParcel(Parcel in) {
            return new Musica(in.readInt(), in.readString(), in.readString(), in.readString(), in.readString(), in.readInt());
        }

        public Musica[] newArray(int size) {
            return new Musica[size];
        }
    };
}