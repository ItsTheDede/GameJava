package com.example;

/** Tiro disparado por um inimigo ou boss. Anda numa direção dada por um ângulo (0 = a direito para baixo). */
public class TiroInimigo {
    private float x, y;
    private final float velX, velY;
    private final float angulo;
    private final int dano;

    public TiroInimigo(float x, float y, float angulo, float velocidade, int dano) {
        this.x = x;
        this.y = y;
        this.angulo = angulo;
        this.dano = dano;
        this.velX = (float) Math.sin(angulo) * velocidade;
        this.velY = -(float) Math.cos(angulo) * velocidade; // negativo = para baixo
    }

    public void atualizar(float delta) {
        x += velX * delta;
        y += velY * delta;
    }

    public boolean foraDoEcra() {
        return y < -1.1f || y > 1.1f || x < -1.1f || x > 1.1f;
    }

    /** Rotação para o sprite do laser (que aponta para cima) ficar virado na direção do movimento. */
    public float getRotacao() { return (float) Math.PI + angulo; }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getDano() { return dano; }
}