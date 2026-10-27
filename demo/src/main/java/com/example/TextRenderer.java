package com.example;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.joml.Matrix4f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class TextRenderer {
    private int fontTexture;
    private STBTTBakedChar.Buffer cdata;
    private int bitmapW = 512;
    private int bitmapH = 512;

    public TextRenderer(String fontPath, int fontSize) {
        cdata = STBTTBakedChar.malloc(96);
        ByteBuffer ttf;
        try (InputStream is = getClass().getResourceAsStream(fontPath)) {
            if (is == null) throw new RuntimeException("Fonte não encontrada: " + fontPath);
            byte[] bytes = is.readAllBytes();
            ttf = BufferUtils.createByteBuffer(bytes.length);
            ttf.put(bytes).flip();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler a fonte", e);
        }

        ByteBuffer bitmap = BufferUtils.createByteBuffer(bitmapW * bitmapH);
        STBTruetype.stbtt_BakeFontBitmap(ttf, fontSize, bitmap, bitmapW, bitmapH, 32, cdata);

        fontTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, bitmapW, bitmapH, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    }

    public void drawText(String text, float x, float y, float scale, int shaderProgram, int vaoId, int uniModelMatrix, int uniColor, int uniAlpha) {
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        glUniform1i(glGetUniformLocation(shaderProgram, "uTexture"), 0);
        glUniform3f(uniColor, 1.0f, 1.0f, 1.0f); // Texto branco
        glUniform1f(uniAlpha, 1.0f);

        try (MemoryStack stack = stackPush()) {
            FloatBuffer xpos = stack.floats(0);
            FloatBuffer ypos = stack.floats(0);
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    xpos.put(0, x);
                    ypos.put(0, ypos.get(0) + 50 * scale);
                    continue;
                }
                STBTruetype.stbtt_GetBakedQuad(cdata, bitmapW, bitmapH, c - 32, xpos, ypos, q, true);
                
                float sx0 = xpos.get(0) + q.x0() * scale;
                float sy0 = ypos.get(0) + q.y0() * scale;
                float sx1 = xpos.get(0) + q.x1() * scale;
                float sy1 = ypos.get(0) + q.y1() * scale;

                Matrix4f model = new Matrix4f().translate((sx0 + sx1) / 2, -(sy0 + sy1) / 2, 0).scale((sx1 - sx0), (sy1 - sy0), 1.0f);
                FloatBuffer buffer = stack.mallocFloat(16);
                model.get(buffer);
                glUniformMatrix4fv(uniModelMatrix, false, buffer);
                
                glBindVertexArray(vaoId);
                glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
            }
        }
    }
}