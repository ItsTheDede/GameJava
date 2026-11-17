package com.example;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryStack.stackPush;

public class StartMenu {

    /** Valor que oscila entre -amplitude e +amplitude. Devolve 0 se as animações estiverem desligadas. */
    public static float pulso(float velocidade, float amplitude) {
        if (!Config.animacoes) return 0f;
        return (float) Math.sin(glfwGetTime() * velocidade) * amplitude;
    }

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

    /**
     * Desenha uma opção de menu centrada em cx. Se estiver selecionada fica amarela, pulsa
     * e ganha setas "> <" coladas ao texto que se aproximam e afastam. Usado por todos os menus.
     */
    public static void opcaoAnimada(TextRenderer text, String s, boolean sel, float cx, float y, float scale,
                                    int shaderProgram, int uniModelMatrix, int uniColor, int uniAlpha) {
        float escala = scale + (sel ? pulso(6f, scale * 0.05f) : 0f);
        float r = sel ? 1f : 0.6f, g = sel ? 1f : 0.6f, b = sel ? 0f : 0.6f;

        textoCentrado(text, s, cx, y, escala, shaderProgram, uniModelMatrix, uniColor, uniAlpha, r, g, b);

        if (sel) {
            float larg = text.getWidth(s, escala);
            float larguraSeta = text.getWidth(">", escala);
            float gap = 0.05f;
            float mexe = Math.abs(pulso(5f, 0.03f)); // vai e volta, sempre positivo

            glUniform3f(uniColor, r, g, b);
            text.drawText(">", cx - larg / 2f - gap - larguraSeta + mexe, y, escala, shaderProgram, uniModelMatrix, uniColor, uniAlpha);
            text.drawText("<", cx + larg / 2f + gap - mexe, y, escala, shaderProgram, uniModelMatrix, uniColor, uniAlpha);
        }
    }

    public static void desenharMenuInicial(TextRenderer text, int shaderProgram, int vaoId,
                                           int uniModelMatrix, int uniColor, int uniAlpha, int opcao) {
        // fundo escuro semi-transparente
        desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 0f, 0f, 0.05f, 0.75f);

        // título a "respirar"
        textoCentrado(text, "SPACEWAR", 0f, 0.35f, 1.2f + pulso(2f, 0.05f), shaderProgram, uniModelMatrix, uniColor, uniAlpha,
                0.3f, 0.8f, 1.0f);

        // as opções (esta linha tem de existir antes do for!)
        String[] opcoes = {Config.t("JOGAR"), Config.t("CONFIG"), Config.t("SAIR")};

        for (int i = 0; i < opcoes.length; i++) {
            opcaoAnimada(text, opcoes[i], i == opcao, 0f, -0.05f - i * 0.2f, 0.8f,
                    shaderProgram, uniModelMatrix, uniColor, uniAlpha);
        }
    }
}