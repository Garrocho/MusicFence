package com.example.cti.musicfence;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Cti on 13/11/2017.
 */

public class Musica implements Parcelable {
    private int id;
    private String artista;
    private String titulo;
    private String path;
    private String nomeArq;
    private int duracao;

    public Musica(int id,String artista,String titulo,String path,String nomeArq,int duracao){
        super();
        this.id = id;
        this.artista = artista;
        this.titulo = titulo;
        this.path = path;
        this.nomeArq = nomeArq;
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
        return nomeArq;
    }

    public void setNomeArquivo(String nome) {
        this.nomeArq = nome;
    }

    public int getDuracao() {
        return duracao;
    }

    public void setDuracao(int duracao) {
        this.duracao = duracao;
    }

    public static final Creator<Musica> CREATOR = new Creator<Musica>() {
        @Override
        public Musica createFromParcel(Parcel in) {
            return new Musica(in.readInt(),in.readString(),in.readString(),in.readString(),
                    in.readString(),in.readInt());
        }

        @Override
        public Musica[] newArray(int size) {
            return new Musica[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(artista);
        dest.writeString(titulo);
        dest.writeString(path);
        dest.writeString(nomeArq);
        dest.writeInt(duracao);
    }
}
