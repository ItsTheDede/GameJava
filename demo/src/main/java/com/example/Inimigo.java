package com.example;

import java.util.Random;

public class Inimigo {
    private float x, y;
    private int vida;
    private float velocidade;
    private boolean ehBoss;
    private int danoNoPlayer; 
    private float r, g, b; // Cores RGB do inimigo

    public Inimigo(float x, float y) {
        this.x = x; this.y = y;
        this.vida = 20; this.velocidade = 0.3f;
        this.ehBoss = false; this.danoNoPlayer = 20;
        
        // Sorteia uma cor para o inimigo comum
        Random rand = new Random();
        int cor = rand.nextInt(4);
        if (cor == 0) { r=1.0f; g=0.2f; b=0.2f; } // Vermelho
        else if (cor == 1) { r=1.0f; g=0.5f; b=0.0f; } // Laranja
        else if (cor == 2) { r=0.2f; g=1.0f; b=0.2f; } // Verde
        else { r=1.0f; g=1.0f; b=0.2f; } // Amarelo
    }

    public Inimigo(float x, float y, int vidaBoss, float velBoss) {
        this.x = x; this.y = y;
        this.vida = vidaBoss; this.velocidade = velBoss;
        this.ehBoss = true; this.danoNoPlayer = 40;
        
        // Boss tem cor baseada no nível dele
        this.r = 1.0f; this.g = 0.0f; this.b = 1.0f; // Roxo
    }

    public void atualizar(float delta) {
        y -= velocidade * delta;
    }

    public void tomarDano(int dano) {
        vida -= dano;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getVida() { return vida; }
    public boolean isBoss() { return ehBoss; }
    public int getDanoNoPlayer() { return danoNoPlayer; }
    
    // Novos getters de cor
    public float getR() { return r; }
    public float getG() { return g; }
    public float getB() { return b; }
}