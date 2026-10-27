package com.example;

import static org.lwjgl.glfw.GLFW.*;

public class Player {
    private float x = 0.0f;
    private float y = -0.8f;
    private float velocidade = 1.0f;
    
    private int vida = 100;
    private int vidaMaxima = 100;
    private int danoDoTiro = 10;
    private float resistencia = 0.0f; 

    private float tempoUltimoTiro = 0.0f;
    private float cooldownTiro = 0.3f; 

    public void atualizar(long window, float delta, float tempoAtual) {
        if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS)  x -= velocidade * delta;
        if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) x += velocidade * delta;
        if (glfwGetKey(window, GLFW_KEY_UP) == GLFW_PRESS)    y += velocidade * delta;
        if (glfwGetKey(window, GLFW_KEY_DOWN) == GLFW_PRESS)  y -= velocidade * delta;

        if (x < -0.9f) x = -0.9f;
        if (x > 0.9f) x = 0.9f;
        if (y < -0.9f) y = -0.9f;
        if (y > 0.9f) y = 0.9f;
    }

    public void tomarDano(int dano) {
        int danoReal = (int)(dano * (1.0f - resistencia));
        this.vida -= danoReal;
        if (this.vida < 0) this.vida = 0;
    }

    public void melhorarVida() {
        this.vidaMaxima += 50;
        this.vida = this.vidaMaxima;
    }

    public void melhorarResistencia() {
        this.resistencia += 0.2f;
        if (this.resistencia > 0.8f) this.resistencia = 0.8f;
    }

    public void melhorarTiro() {
        this.danoDoTiro += 10;
        this.cooldownTiro -= 0.05f;
        if (this.cooldownTiro < 0.1f) this.cooldownTiro = 0.1f;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getVida() { return vida; }
    public int getVidaMaxima() { return vidaMaxima; }
    public int getDanoDoTiro() { return danoDoTiro; }
    
    public boolean podeAtirar(float tempoAtual) {
        if (tempoAtual - tempoUltimoTiro >= cooldownTiro) {
            tempoUltimoTiro = tempoAtual;
            return true;
        }
        return false;
    }
}