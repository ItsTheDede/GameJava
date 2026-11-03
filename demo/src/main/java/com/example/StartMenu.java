package com.example;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryStack.stackPush;

public class StartMenu {

    /** Desenha um retângulo de cor sólida (coordenadas NDC, centro x,y). */
    public static void desenharRect(int vaoId, int uniModelMatrix, int uniColor, int uniAlpha,
                                    float x, float y, float w, float h,
                                    float r, float g, float b, float a) {
        try (MemoryStack stack = stackPush()) {
            FloatBuffer m = stack.mallocFloat(16);
            new Matrix4f().translate(x, y, 0).scale(w, h, 1f).get(m);
            glUniformMatrix4fv(uniModelMatrix, false, m);
        }
        glUniform3f(uniColor, r, g, b);
        glUniform1f(uniAlpha, a);
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

    /** Desenha texto centrado horizontalmente em cx. */
    public static void textoCentrado(TextRenderer text, String s, float cx, float y, float scale,
                                     int shaderProgram, int uniModelMatrix, int uniColor, int uniAlpha,
                                     float r, float g, float b) {
        glUniform3f(uniColor, r, g, b);
        float x = cx - text.getWidth(s, scale) / 2f;
        text.drawText(s, x, y, scale, shaderProgram, uniModelMatrix, uniColor, uniAlpha);
    }

    public static void desenharMenuInicial(TextRenderer text, int shaderProgram, int vaoId,
                                           int uniModelMatrix, int uniColor, int uniAlpha, int opcao) {
        // fundo escuro semi-transparente
        desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 0f, 0f, 0.05f, 0.75f);

        // título
        textoCentrado(text, "SPACEWAR", 0f, 0.35f, 1.4f, shaderProgram, uniModelMatrix, uniColor, uniAlpha,
                0.3f, 0.8f, 1.0f);

        String[] opcoes = {Config.t("JOGAR"), Config.t("CONFIG"), Config.t("SAIR")};
        for (int i = 0; i < opcoes.length; i++) {
            boolean sel = (i == opcao);
            String label = sel ? "> " + opcoes[i] + " <" : opcoes[i];
            textoCentrado(text, label, 0f, -0.05f - i * 0.2f, 0.8f, shaderProgram, uniModelMatrix, uniColor, uniAlpha,
                    sel ? 1f : 0.6f, sel ? 1f : 0.6f, sel ? 0f : 0.6f);
        }

        textoCentrado(text, Config.t("DICA_MENU"), 0f, -0.8f, 0.4f, shaderProgram, uniModelMatrix, uniColor, uniAlpha,
                0.7f, 0.7f, 0.7f);
    }
}