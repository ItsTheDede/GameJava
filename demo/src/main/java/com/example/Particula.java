package com.example;

import java.util.Random;

public class Particula {
    public float x, y;
    public float velX, velY;
    public float vida = 1.0f; // 1.0 = 100% de vida, vai diminuindo
    public int texturaId;
    public float tamanho;

    public Particula(float x, float y, int texturaId) {
        this.x = x;
        this.y = y;
        this.texturaId = texturaId;
        this.tamanho = 0.05f;
        
        // Cria uma direção aleatória para a partícula voar
        Random rand = new Random();
        float angulo = rand.nextFloat() * (float)Math.PI * 2;
        float velocidade = rand.nextFloat() * 0.5f + 0.2f;
        this.velX = (float)Math.cos(angulo) * velocidade;
        this.velY = (float)Math.sin(angulo) * velocidade;
    }

    public void atualizar(float delta) {
        x += velX * delta;
        y += velY * delta;
        vida -= delta * 2.0f; // A partícula some em meio segundo
        tamanho -= delta * 0.1f; // Vai encolhendo
    }
}