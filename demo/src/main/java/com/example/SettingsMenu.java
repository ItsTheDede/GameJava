package com.example;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL20.glUniform3f;

/** Menu de Configurações: ecrã principal (idioma, animações, tremor) e ecrã de controlos (remapear teclas). */
public class SettingsMenu {
    // Índices das linhas do ecrã principal. Para acrescentar uma opção: nova constante,
    // nova linha no array de desenhar() e novo ramo em aoTecla().
    private static final int OP_IDIOMA = 0, OP_ANIM = 1, OP_TREMOR = 2, OP_CONTROLOS = 3, OP_VOLTAR = 4;
    private static final int NUM_OPCOES_PRINCIPAL = 5;

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
        return tela == 0 ? NUM_OPCOES_PRINCIPAL : Config.Acao.values().length + 2;
    }

    /** Chamado pelo callback de teclado do Main enquanto o menu está aberto. */
    public void aoTecla(int key, int action) {
        // ESC (ao soltar, como no resto do jogo): cancela / volta / fecha
        if (key == GLFW_KEY_ESCAPE) {
            if (action == GLFW_RELEASE) {
                if (aEspera) aEspera = false;
                else if (tela == 1) { tela = 0; opcao = OP_CONTROLOS; }
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
            boolean alterna = key == GLFW_KEY_LEFT || key == GLFW_KEY_RIGHT || key == GLFW_KEY_ENTER;

            if (opcao == OP_IDIOMA && alterna) {
                Config.idioma = (Config.idioma == Config.PORTUGUES) ? Config.INGLES : Config.PORTUGUES;
                Config.guardar();
            } else if (opcao == OP_ANIM && alterna) {
                Config.animacoes = !Config.animacoes;
                Config.guardar();
            } else if (opcao == OP_TREMOR && alterna) {
                Config.tremor = !Config.tremor;
                Config.guardar();
            } else if (key == GLFW_KEY_ENTER) {
                if (opcao == OP_CONTROLOS) { tela = 1; opcao = 0; }
                else if (opcao == OP_VOLTAR) fechar();
            }
        } else {
            int na = Config.Acao.values().length;
            if (key == GLFW_KEY_ENTER) {
                if (opcao < na) aEspera = true;
                else if (opcao == na) { Config.resetarControlos(); Config.guardar(); }
                else { tela = 0; opcao = OP_CONTROLOS; }
            }
        }
    }

    public void desenhar(TextRenderer text, int shaderProgram, int vaoId,
                         int uniModelMatrix, int uniColor, int uniAlpha) {
        StartMenu.desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 0f, 0f, 0.05f, 0.92f);

        // título a "respirar"
        StartMenu.textoCentrado(text, Config.t("CONFIG"), 0f, 0.7f, 1.0f + StartMenu.pulso(2f, 0.04f),
                shaderProgram, uniModelMatrix, uniColor, uniAlpha, 0.3f, 0.8f, 1.0f);

        if (tela == 0) {
            // A ordem do array tem de ser a mesma dos índices OP_*
            String[] linhas = {
                    Config.t("IDIOMA") + ": " + Config.t("LANG_NOME"),
                    Config.t("ANIMACOES") + ": " + Config.t(Config.animacoes ? "LIGADO" : "DESLIGADO"),
                    Config.t("TREMOR") + ": " + Config.t(Config.tremor ? "LIGADO" : "DESLIGADO"),
                    Config.t("CONTROLOS"),
                    Config.t("VOLTAR")
            };
            for (int i = 0; i < linhas.length; i++) {
                StartMenu.opcaoAnimada(text, linhas[i], i == opcao, 0f, 0.3f - i * 0.18f, 0.7f,
                        shaderProgram, uniModelMatrix, uniColor, uniAlpha);
            }
        } else {
            Config.Acao[] acoes = Config.Acao.values();
            int na = acoes.length;
            for (int i = 0; i < na + 2; i++) {
                float y = 0.45f - i * 0.12f;
                boolean sel = (i == opcao);

                if (i < na) {
                    float r = sel ? 1f : 0.6f, g = sel ? 1f : 0.6f, b = sel ? 0f : 0.6f;
                    float escala = 0.4f + (sel ? StartMenu.pulso(6f, 0.02f) : 0f);

                    glUniform3f(uniColor, r, g, b);
                    String nome = (sel ? "> " : "  ") + Config.t("ACAO_" + acoes[i].name());
                    text.drawText(nome, -0.85f, y, escala, shaderProgram, uniModelMatrix, uniColor, uniAlpha);

                    // à espera de tecla, os "..." piscam (com animações off ficam sempre visíveis)
                    String tecla;
                    if (sel && aEspera) tecla = (StartMenu.pulso(8f, 1f) >= 0) ? "..." : "";
                    else tecla = Config.nomeTecla(Config.teclas[i]);
                    text.drawText(tecla, 0.15f, y, escala, shaderProgram, uniModelMatrix, uniColor, uniAlpha);
                } else {
                    String l = (i == na) ? Config.t("REPOR") : Config.t("VOLTAR");
                    StartMenu.opcaoAnimada(text, l, sel, 0f, y - 0.04f, 0.45f,
                            shaderProgram, uniModelMatrix, uniColor, uniAlpha);
                }
            }
            StartMenu.textoCentrado(text, aEspera ? Config.t("PRESSIONA_TECLA") : Config.t("DICA_CONFIG"),
                    0f, -0.8f, 0.35f, shaderProgram, uniModelMatrix, uniColor, uniAlpha, 0.7f, 0.7f, 0.7f);
        }
    }
}