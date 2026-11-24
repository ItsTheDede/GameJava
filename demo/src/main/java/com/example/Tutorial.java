package com.example;

import java.util.prefs.Preferences;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Estado do tutorial: que página está aberta, navegação por teclas e
 * memória de "já viu o tutorial" (guardada nas preferências do utilizador).
 * O desenho é feito no Main (desenharTutorial), que tem acesso ao OpenGL.
 */
public class Tutorial {
    public static final int PAGINAS = 4;

    // Para voltar a ver o tutorial automaticamente (testes), muda este nome (ex.: "tutorialVisto2").
    private static final String CHAVE = "tutorialVisto";

    private final Preferences prefs = Preferences.userNodeForPackage(Tutorial.class);
    private boolean aberto = false;
    private int pagina = 0;

    public boolean isAberto() { return aberto; }
    public int getPagina() { return pagina; }

    public boolean jaViu() { return prefs.getBoolean(CHAVE, false); }

    public void abrir() { aberto = true; pagina = 0; }

    public void abrirSeForPrimeiraVez() {
        if (!jaViu()) abrir();
    }

    private void fechar() {
        aberto = false;
        prefs.putBoolean(CHAVE, true);
        try { prefs.flush(); } catch (Exception ignorado) { }
    }

    /** Trata as teclas enquanto o tutorial está aberto. */
    public void aoTecla(int key, int action) {
        if (action != GLFW_PRESS) return;

        if (key == GLFW_KEY_ENTER || key == GLFW_KEY_SPACE || key == GLFW_KEY_RIGHT) {
            if (pagina < PAGINAS - 1) pagina++;
            else fechar();
        } else if (key == GLFW_KEY_LEFT) {
            if (pagina > 0) pagina--;
        } else if (key == GLFW_KEY_ESCAPE) {
            fechar();
        }
    }

    /** Nome legível de uma tecla, para mostrar os controlos configurados pelo jogador. */
    public static String nomeTecla(int key) {
        switch (key) {
            case GLFW_KEY_SPACE:        return "ESPACO";
            case GLFW_KEY_LEFT:         return "SETA ESQUERDA";
            case GLFW_KEY_RIGHT:        return "SETA DIREITA";
            case GLFW_KEY_UP:           return "SETA CIMA";
            case GLFW_KEY_DOWN:         return "SETA BAIXO";
            case GLFW_KEY_ENTER:        return "ENTER";
            case GLFW_KEY_ESCAPE:       return "ESC";
            case GLFW_KEY_TAB:          return "TAB";
            case GLFW_KEY_LEFT_SHIFT:   return "SHIFT";
            case GLFW_KEY_RIGHT_SHIFT:  return "SHIFT";
            case GLFW_KEY_LEFT_CONTROL: return "CTRL";
            case GLFW_KEY_RIGHT_CONTROL:return "CTRL";
            default:
                String nome = glfwGetKeyName(key, 0);
                if (nome != null) return nome.toUpperCase();
                return "TECLA " + key;
        }
    }
}