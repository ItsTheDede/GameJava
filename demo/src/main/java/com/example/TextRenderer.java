package com.example;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackRange;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.system.MemoryStack.stackPush;

public class TextRenderer {
    private static final int BITMAP_W = 2048, BITMAP_H = 2048;
    private static final int FIRST_CHAR = 32, NUM_CHARS = 96;
    private static final float WINDOW_W = 800f, WINDOW_H = 600f;

    private final int quadVao;
    private final STBTTPackedchar.Buffer charData;
    private final int fontTexture;

    private int locUseFont = -1, locUV, locTexture;
    private float alpha = 1.0f;

    /** Transparência do texto (0 a 1). Lembra-te de repor a 1 depois de usar. */
    public void setAlpha(float a) { alpha = a; }

    public TextRenderer(String fontPath, int fontSize, int quadVao) {
        this.quadVao = quadVao;

        ByteBuffer ttf;
        try (InputStream is = TextRenderer.class.getResourceAsStream(fontPath)) {
            if (is == null) throw new RuntimeException("Fonte não encontrada: " + fontPath);
            byte[] bytes = is.readAllBytes();
            ttf = BufferUtils.createByteBuffer(bytes.length);
            ttf.put(bytes).flip();
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erro ao ler a fonte", e);
        }

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        charData = STBTTPackedchar.malloc(NUM_CHARS);

        try (MemoryStack stack = stackPush()) {
            STBTTPackContext ctx = STBTTPackContext.malloc(stack);
            STBTTPackRange.Buffer ranges = STBTTPackRange.calloc(1, stack);
            ranges.font_size(fontSize)
                  .first_unicode_codepoint_in_range(FIRST_CHAR)
                  .num_chars(NUM_CHARS)
                  .chardata_for_range(charData);

            if (!STBTruetype.stbtt_PackBegin(ctx, bitmap, BITMAP_W, BITMAP_H, 0, 1))
                throw new IllegalStateException("Falha em stbtt_PackBegin");
            STBTruetype.stbtt_PackSetOversampling(ctx, 2, 2);
            if (!STBTruetype.stbtt_PackFontRanges(ctx, ttf, 0, ranges))
                throw new IllegalStateException("Falha em stbtt_PackFontRanges");
            STBTruetype.stbtt_PackEnd(ctx);
        }

        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        fontTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_W, BITMAP_H, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    /** Largura do texto em coordenadas NDC (útil para centrar). */
    public float getWidth(String text, float scale) {
        float w = 0;
        for (char c : text.toCharArray()) {
            if (c < FIRST_CHAR || c >= FIRST_CHAR + NUM_CHARS) continue;
            w += charData.get(c - FIRST_CHAR).xadvance();
        }
        return w * (2f / WINDOW_W) * scale;
    }

    /**
     * Desenha texto. (x, y) = início da linha de base, em NDC.
     * scale = 1.0 desenha ao tamanho da fonte (ex.: 48 px).
     * A cor define-se antes com glUniform3f(uniColor, ...).
     * Só suporta ASCII 32-127 (sem acentos).
     */
    public void drawText(String text, float x, float y, float scale,
                         int shaderProgram, int uniModelMatrix, int uniColor, int uniAlpha) {
        if (locUseFont == -1) {
            locUseFont = glGetUniformLocation(shaderProgram, "useFont");
            locUV = glGetUniformLocation(shaderProgram, "uUV");
            locTexture = glGetUniformLocation(shaderProgram, "uTexture");
        }

        float sx = (2f / WINDOW_W) * scale;
        float sy = (2f / WINDOW_H) * scale;

        glUniform1i(locUseFont, 1);
        glUniform1f(uniAlpha, alpha);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glUniform1i(locTexture, 0);
        glBindVertexArray(quadVao);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer px = stack.floats(0f);
            FloatBuffer py = stack.floats(0f);
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
            FloatBuffer m = stack.mallocFloat(16);

            for (char c : text.toCharArray()) {
                if (c < FIRST_CHAR || c >= FIRST_CHAR + NUM_CHARS) continue;
                STBTruetype.stbtt_GetPackedQuad(charData, BITMAP_W, BITMAP_H, c - FIRST_CHAR, px, py, q, false);

                float w = (q.x1() - q.x0()) * sx;
                float h = (q.y1() - q.y0()) * sy;
                float cx = x + (q.x0() + q.x1()) * 0.5f * sx;
                float cy = y - (q.y0() + q.y1()) * 0.5f * sy; // o y do stb cresce para baixo

                // v=0 do quad = fundo da letra (t1), v=1 = topo (t0)
                glUniform4f(locUV, q.s0(), q.t1(), q.s1(), q.t0());
                new Matrix4f().translate(cx, cy, 0).scale(w, h, 1f).get(m);
                glUniformMatrix4fv(uniModelMatrix, false, m);
                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            }
        }

        // repor o estado normal
        glUniform4f(locUV, 0f, 0f, 1f, 1f);
        glUniform1i(locUseFont, 0);
    }

    public void cleanup() {
        glDeleteTextures(fontTexture);
        charData.free();
    }
}