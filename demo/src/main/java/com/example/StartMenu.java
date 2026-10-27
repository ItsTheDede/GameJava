package com.example;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class StartMenu {
    public static void desenharMenuInicial(TextRenderer textRenderer, int shaderProgram, int vaoId, int uniModelMatrix, int uniColor, int uniAlpha, int opcaoSelecionada) {
        // Guarda o estado do depth test e desliga para o menu (senão o texto no mesmo z some)
        boolean depthLigado = glIsEnabled(GL_DEPTH_TEST);
        glDisable(GL_DEPTH_TEST);

        // Blending para transparência e texto
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int locUseTexture = glGetUniformLocation(shaderProgram, "useTexture");

        // Fundo escuro
        glUniform1i(locUseTexture, 0);
        glUniform3f(uniColor, 0.0f, 0.0f, 0.05f);
        glUniform1f(uniAlpha, 0.9f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.0f, 2.0f, 2.0f);

        // Título "SpaceWar" (fundo roxo)
        glUniform1i(locUseTexture, 0);
        glUniform3f(uniColor, 0.5f, 0.0f, 0.5f);
        glUniform1f(uniAlpha, 1.0f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.5f, 1.2f, 0.2f);

        // Texto do título (amarelo)
        glUniform1i(locUseTexture, 1);
        glUniform3f(uniColor, 1.0f, 1.0f, 0.0f); // <-- faltava isso
        glUniform1f(uniAlpha, 1.0f);
        textRenderer.drawText("SpaceWar", -0.35f, 0.45f, 0.01f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Botão Começar
        glUniform1i(locUseTexture, 0);
        if (opcaoSelecionada == 0) glUniform3f(uniColor, 0.0f, 0.8f, 0.0f); else glUniform3f(uniColor, 0.2f, 0.2f, 0.2f);
        glUniform1f(uniAlpha, 1.0f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, 0.0f, 0.6f, 0.15f);

        // Texto do botão 1 (branco)
        glUniform1i(locUseTexture, 1);
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f);
        glUniform1f(uniAlpha, 1.0f);
        textRenderer.drawText("Comecar", -0.18f, -0.02f, 0.008f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Botão Sair
        glUniform1i(locUseTexture, 0);
        if (opcaoSelecionada == 1) glUniform3f(uniColor, 0.8f, 0.0f, 0.0f); else glUniform3f(uniColor, 0.2f, 0.2f, 0.2f);
        glUniform1f(uniAlpha, 1.0f);
        desenharQuadrado(vaoId, uniModelMatrix, 0.0f, -0.3f, 0.6f, 0.15f);

        // Texto do botão 2 (branco)
        glUniform1i(locUseTexture, 1);
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f);
        glUniform1f(uniAlpha, 1.0f);
        textRenderer.drawText("Sair", -0.05f, -0.32f, 0.008f, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);

        // Volta useTexture ao normal e restaura o depth test
        glUniform1i(locUseTexture, 0);
        if (depthLigado) glEnable(GL_DEPTH_TEST);
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