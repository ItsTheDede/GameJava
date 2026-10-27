package com.example;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class StartMenu {
    public static void desenharMenuInicial(TextRenderer textRenderer, int shaderProgram, int vaoId, int uniModelMatrix, int uniColor, int uniAlpha, int opcaoSelecionada) {
        // Fundo Escuro
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        glUniform3f(uniColor, 0.0f, 0.0f, 0.05f); 
        glUniform1f(uniAlpha, 0.9f); 
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.0f, 2.0f, 2.0f);

        // Título "SpaceWar" (Fundo Roxo)
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        glUniform3f(uniColor, 0.5f, 0.0f, 0.5f); 
        glUniform1f(uniAlpha, 1.0f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.5f, 1.2f, 0.2f); 
        
        // TEXTO DO TÍTULO (Amarelo, Escala 0.01f)
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1); // Ativa textura
        textRenderer.drawText("SpaceWar", -0.35f, 0.45f, 0.01f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Botão Começar
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0); // Desativa textura para desenhar o fundo do botão
        if (opcaoSelecionada == 0) glUniform3f(uniColor, 0.0f, 0.8f, 0.0f); else glUniform3f(uniColor, 0.2f, 0.2f, 0.2f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.0f, 0.6f, 0.15f);
        
        // TEXTO DO BOTÃO 1 (Branco)
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1); 
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f); // Texto branco
        textRenderer.drawText("Comecar", -0.18f, -0.02f, 0.008f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Botão Sair
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0); 
        if (opcaoSelecionada == 1) glUniform3f(uniColor, 0.8f, 0.0f, 0.0f); else glUniform3f(uniColor, 0.2f, 0.2f, 0.2f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, -0.3f, 0.6f, 0.15f);
        
        // TEXTO DO BOTÃO 2 (Branco)
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1); 
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f); 
        textRenderer.drawText("Sair", -0.05f, -0.32f, 0.008f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);
    }

    private static void desenharQuadrado(int vaoId, int uniModelMatrix, float x, float y, float sX, float sY) {
        Matrix4f model = new Matrix4f().translate(x, y, 0).scale(sX, sY, 1.0f);
        try (MemoryStack stack = stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            model.get(buffer);
            glUniformMatrix4fv(uniModelMatrix, false, buffer);
        }
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }
}