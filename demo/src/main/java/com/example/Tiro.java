package com.example;

public class Tiro {
    private float x, y;
    private float velocidade = 1.5f;
    private int dano;
    private int texturaId;

    public Tiro(float x, float y, int dano, int texturaId) {
        this.x = x; this.y = y;
        this.dano = dano;
        this.texturaId = texturaId;
    }

    public void atualizar(float delta) {
        y += velocidade * delta; 
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getDano() { return dano; }
    public int getTexturaId() { return texturaId; }
}