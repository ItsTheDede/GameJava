package com.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.lwjgl.glfw.GLFW.*;

/** Configurações do jogo: idioma, controlos e textos traduzidos. */
public class Config {

    public enum Acao { CIMA, BAIXO, ESQUERDA, DIREITA, ATIRAR, PAUSAR }

    public static final int PORTUGUES = 0, INGLES = 1;
    private static final String FICHEIRO = "config.properties";

    public static int idioma = PORTUGUES;
    public static final int[] teclas = new int[Acao.values().length];

    private static final Map<String, String[]> TEXTOS = new HashMap<>();

    static {
        resetarControlos();

        // chave, português, inglês  (a fonte só suporta ASCII: sem acentos)
        t("JOGAR", "JOGAR", "PLAY");
        t("CONFIG", "CONFIGURACOES", "SETTINGS");
        t("SAIR", "SAIR", "QUIT");
        t("DICA_MENU", "SETAS + ENTER", "ARROWS + ENTER");

        t("T_PAUSA", "PAUSA", "PAUSED");
        t("CONTINUAR", "CONTINUAR", "RESUME");
        t("REINICIAR", "REINICIAR", "RESTART");
        t("MENU_PRINCIPAL", "MENU PRINCIPAL", "MAIN MENU");

        t("IDIOMA", "IDIOMA", "LANGUAGE");
        t("LANG_NOME", "PORTUGUES", "ENGLISH");
        t("CONTROLOS", "CONTROLOS", "CONTROLS");
        t("VOLTAR", "VOLTAR", "BACK");
        t("REPOR", "REPOR PADRAO", "RESET DEFAULTS");
        t("DICA_CONFIG", "ENTER ALTERA  -  ESC VOLTA", "ENTER CHANGES  -  ESC BACK");
        t("PRESSIONA_TECLA", "PRESSIONA UMA TECLA (ESC CANCELA)", "PRESS A KEY (ESC CANCELS)");

        t("ACAO_CIMA", "MOVER PARA CIMA", "MOVE UP");
        t("ACAO_BAIXO", "MOVER PARA BAIXO", "MOVE DOWN");
        t("ACAO_ESQUERDA", "MOVER ESQUERDA", "MOVE LEFT");
        t("ACAO_DIREITA", "MOVER DIREITA", "MOVE RIGHT");
        t("ACAO_ATIRAR", "ATIRAR", "SHOOT");
        t("ACAO_PAUSAR", "PAUSAR", "PAUSE");

        t("K_CIMA", "SETA CIMA", "UP ARROW");
        t("K_BAIXO", "SETA BAIXO", "DOWN ARROW");
        t("K_ESQUERDA", "SETA ESQUERDA", "LEFT ARROW");
        t("K_DIREITA", "SETA DIREITA", "RIGHT ARROW");
        t("K_ESPACO", "ESPACO", "SPACE");
        t("K_TECLA", "TECLA", "KEY");

        t("M_TITULO", "ESCOLHE UMA MELHORIA", "CHOOSE AN UPGRADE");
        t("M_VIDA", "1 VIDA", "1 LIFE");
        t("M_RESIST", "2 RESIST.", "2 ARMOR");
        t("M_TIRO", "3 TIRO", "3 SHOT");

        t("SETOR", "SETOR", "SECTOR");
        t("GAME_OVER", "GAME OVER", "GAME OVER");
        t("PRESSIONA_ENTER", "PRESSIONA ENTER", "PRESS ENTER");
    }

    private static void t(String chave, String pt, String en) {
        TEXTOS.put(chave, new String[]{pt, en});
    }

    /** Devolve o texto na língua atual. */
    public static String t(String chave) {
        String[] v = TEXTOS.get(chave);
        return v == null ? chave : v[idioma];
    }

    // ---------------- Controlos ----------------

    public static void resetarControlos() {
        teclas[Acao.CIMA.ordinal()] = GLFW_KEY_UP;
        teclas[Acao.BAIXO.ordinal()] = GLFW_KEY_DOWN;
        teclas[Acao.ESQUERDA.ordinal()] = GLFW_KEY_LEFT;
        teclas[Acao.DIREITA.ordinal()] = GLFW_KEY_RIGHT;
        teclas[Acao.ATIRAR.ordinal()] = GLFW_KEY_SPACE;
        teclas[Acao.PAUSAR.ordinal()] = GLFW_KEY_L;
    }

    /** Atribui uma tecla a uma ação. Se outra ação já a usa, trocam de teclas. */
    public static void atribuir(Acao acao, int tecla) {
        int i = acao.ordinal();
        int antiga = teclas[i];
        for (int j = 0; j < teclas.length; j++) {
            if (j != i && teclas[j] == tecla) teclas[j] = antiga;
        }
        teclas[i] = tecla;
    }

    public static boolean pressionada(long window, Acao acao) {
        return glfwGetKey(window, teclas[acao.ordinal()]) == GLFW_PRESS;
    }

    public static int tecla(Acao acao) {
        return teclas[acao.ordinal()];
    }

    public static String nomeTecla(int k) {
        if (k == GLFW_KEY_UP) return t("K_CIMA");
        if (k == GLFW_KEY_DOWN) return t("K_BAIXO");
        if (k == GLFW_KEY_LEFT) return t("K_ESQUERDA");
        if (k == GLFW_KEY_RIGHT) return t("K_DIREITA");
        if (k == GLFW_KEY_SPACE) return t("K_ESPACO");
        if (k == GLFW_KEY_ENTER) return "ENTER";
        if (k == GLFW_KEY_TAB) return "TAB";
        if (k == GLFW_KEY_BACKSPACE) return "BACKSPACE";
        if (k == GLFW_KEY_LEFT_SHIFT || k == GLFW_KEY_RIGHT_SHIFT) return "SHIFT";
        if (k == GLFW_KEY_LEFT_CONTROL || k == GLFW_KEY_RIGHT_CONTROL) return "CTRL";
        if (k == GLFW_KEY_LEFT_ALT || k == GLFW_KEY_RIGHT_ALT) return "ALT";
        if (k >= GLFW_KEY_A && k <= GLFW_KEY_Z) return String.valueOf((char) k);
        if (k >= GLFW_KEY_0 && k <= GLFW_KEY_9) return String.valueOf((char) k);
        if (k >= GLFW_KEY_F1 && k <= GLFW_KEY_F12) return "F" + (k - GLFW_KEY_F1 + 1);
        return t("K_TECLA") + " " + k;
    }

    // ---------------- Guardar / carregar ----------------

    public static void guardar() {
        Properties p = new Properties();
        p.setProperty("idioma", String.valueOf(idioma));
        for (Acao a : Acao.values()) p.setProperty("tecla." + a.name(), String.valueOf(teclas[a.ordinal()]));
        try (OutputStream os = new FileOutputStream(FICHEIRO)) {
            p.store(os, "SpaceWar");
        } catch (IOException e) {
            System.err.println("Nao foi possivel guardar a configuracao: " + e.getMessage());
        }
    }

    public static void carregar() {
        File f = new File(FICHEIRO);
        if (!f.exists()) return;
        Properties p = new Properties();
        try (InputStream is = new FileInputStream(f)) {
            p.load(is);
            idioma = Integer.parseInt(p.getProperty("idioma", "0")) == INGLES ? INGLES : PORTUGUES;
            for (Acao a : Acao.values()) {
                String v = p.getProperty("tecla." + a.name());
                if (v != null) teclas[a.ordinal()] = Integer.parseInt(v);
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Configuracao invalida, a usar valores por defeito.");
            idioma = PORTUGUES;
            resetarControlos();
        }
    }
}