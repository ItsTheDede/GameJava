package com.example;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Pause {
    public static void desenharMenuPausa(TextRenderer textRenderer, int shaderProgram, int vaoId, int uniModelMatrix, int uniColor, int uniAlpha, int opcaoSelecionada) {
        // Fundo
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        glUniform3f(uniColor, 0.0f, 0.0f, 0.0f); 
        glUniform1f(uniAlpha, 0.6f); 
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.0f, 2.0f, 2.0f);

        // Título "Pausa"
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        glUniform3f(uniColor, 0.2f, 0.6f, 1.0f); 
        glUniform1f(uniAlpha, 1.0f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.5f, 0.6f, 0.15f); 
        
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f);
        textRenderer.drawText("Pausa", -0.12f, 0.46f, 0.01f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Retornar
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        if (opcaoSelecionada == 0) glUniform3f(uniColor, 0.0f, 0.8f, 0.0f); else glUniform3f(uniColor, 0.2f, 0.2f, 0.2f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.15f, 0.6f, 0.12f); 
        
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f);
        textRenderer.drawText("Retornar", -0.2f, 0.12f, 0.008f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Recomeçar
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        if (opcaoSelecionada == 1) glUniform3f(uniColor, 0.0f, 0.8f, 0.0f); else glUniform3f(uniColor, 0.2f, 0.2f, 0.2f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, -0.1f, 0.6f, 0.12f); 
        
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f);
        textRenderer.drawText("Recomecar", -0.22f, -0.13f, 0.008f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Voltar ao Menu
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        if (opcaoSelecionada == 2) glUniform3f(uniColor, 0.0f, 0.8f, 0.0f); else glUniform3f(uniColor, 0.2f, 0.2f, 0.2f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, -0.35f, 0.6f, 0.12f); 
        
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f);
        textRenderer.drawText("Menu Inicial", -0.25f, -0.38f, 0.008f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);
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