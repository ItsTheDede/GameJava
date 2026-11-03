package com.example;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.glUniform3f;

/** Menu de Configurações: ecrã principal (idioma) e ecrã de controlos (remapear teclas). */
public class SettingsMenu {
    private boolean aberto = false;
    private int tela = 0;          // 0 = principal, 1 = controlos
    private int opcao = 0;
    private boolean aEspera = false; // à espera de uma tecla para remapear

    public boolean isAberto() { return aberto; }

    public void abrir() {
        aberto = true;
        tela = 0;
        opcao = 0;
        aEspera = false;
    }

    private void fechar() {
        aberto = false;
        Config.guardar();
    }

    private int numOpcoes() {
        return tela == 0 ? 3 : Config.Acao.values().length + 2;
    }

    /** Chamado pelo callback de teclado do Main enquanto o menu está aberto. */
    public void aoTecla(int key, int action) {
        // ESC (ao soltar, como no resto do jogo): cancela / volta / fecha
        if (key == GLFW_KEY_ESCAPE) {
            if (action == GLFW_RELEASE) {
                if (aEspera) aEspera = false;
                else if (tela == 1) { tela = 0; opcao = 1; }
                else fechar();
            }
            return;
        }
        if (action != GLFW_PRESS) return;

        // à espera de uma tecla nova
        if (aEspera) {
            if (key == GLFW_KEY_ENTER || key == GLFW_KEY_UNKNOWN) return; // ENTER é reservado aos menus
            Config.atribuir(Config.Acao.values()[opcao], key);
            aEspera = false;
            Config.guardar();
            return;
        }

        if (key == GLFW_KEY_UP && opcao > 0) opcao--;
        if (key == GLFW_KEY_DOWN && opcao < numOpcoes() - 1) opcao++;

        if (tela == 0) {
            if (opcao == 0 && (key == GLFW_KEY_LEFT || key == GLFW_KEY_RIGHT || key == GLFW_KEY_ENTER)) {
                Config.idioma = (Config.idioma == Config.PORTUGUES) ? Config.INGLES : Config.PORTUGUES;
                Config.guardar();
            } else if (key == GLFW_KEY_ENTER) {
                if (opcao == 1) { tela = 1; opcao = 0; }
                else if (opcao == 2) fechar();
            }
        } else {
            int na = Config.Acao.values().length;
            if (key == GLFW_KEY_ENTER) {
                if (opcao < na) aEspera = true;
                else if (opcao == na) { Config.resetarControlos(); Config.guardar(); }
                else { tela = 0; opcao = 1; }
            }
        }
    }

    public void desenhar(TextRenderer text, int shaderProgram, int vaoId,
                         int uniModelMatrix, int uniColor, int uniAlpha) {
        StartMenu.desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 0f, 0f, 0.05f, 0.92f);
        StartMenu.textoCentrado(text, Config.t("CONFIG"), 0f, 0.7f, 1.0f,
                shaderProgram, uniModelMatrix, uniColor, uniAlpha, 0.3f, 0.8f, 1.0f);

        if (tela == 0) {
            String[] linhas = {
                    Config.t("IDIOMA") + ": " + Config.t("LANG_NOME"),
                    Config.t("CONTROLOS"),
                    Config.t("VOLTAR")
            };
            for (int i = 0; i < linhas.length; i++) {
                boolean sel = (i == opcao);
                String label = sel ? "> " + linhas[i] + " <" : linhas[i];
                StartMenu.textoCentrado(text, label, 0f, 0.2f - i * 0.2f, 0.7f,
                        shaderProgram, uniModelMatrix, uniColor, uniAlpha,
                        sel ? 1f : 0.6f, sel ? 1f : 0.6f, sel ? 0f : 0.6f);
            }
        } else {
            Config.Acao[] acoes = Config.Acao.values();
            int na = acoes.length;
            for (int i = 0; i < na + 2; i++) {
                float y = 0.45f - i * 0.12f;
                boolean sel = (i == opcao);
                float r = sel ? 1f : 0.6f, g = sel ? 1f : 0.6f, b = sel ? 0f : 0.6f;

                if (i < na) {
                    glUniform3f(uniColor, r, g, b);
                    String nome = (sel ? "> " : "  ") + Config.t("ACAO_" + acoes[i].name());
                    text.drawText(nome, -0.85f, y, 0.4f, shaderProgram, uniModelMatrix, uniColor, uniAlpha);

                    String tecla = (sel && aEspera) ? "..." : Config.nomeTecla(Config.teclas[i]);
                    text.drawText(tecla, 0.15f, y, 0.4f, shaderProgram, uniModelMatrix, uniColor, uniAlpha);
                } else {
                    String l = (i == na) ? Config.t("REPOR") : Config.t("VOLTAR");
                    if (sel) l = "> " + l + " <";
                    StartMenu.textoCentrado(text, l, 0f, y - 0.04f, 0.45f,
                            shaderProgram, uniModelMatrix, uniColor, uniAlpha, r, g, b);
                }
            }
            StartMenu.textoCentrado(text, aEspera ? Config.t("PRESSIONA_TECLA") : Config.t("DICA_CONFIG"),
                    0f, -0.8f, 0.35f, shaderProgram, uniModelMatrix, uniColor, uniAlpha, 0.7f, 0.7f, 0.7f);
        }
    }
}