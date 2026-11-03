package com.example;

public class Pause {

    public static void desenharMenuPausa(TextRenderer text, int shaderProgram, int vaoId,
                                         int uniModelMatrix, int uniColor, int uniAlpha, int opcao) {
        // escurece o jogo por trás
        StartMenu.desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 0f, 0f, 0f, 0.65f);

        StartMenu.textoCentrado(text, Config.t("T_PAUSA"), 0f, 0.4f, 1.2f, shaderProgram, uniModelMatrix, uniColor, uniAlpha,
                1f, 1f, 1f);

        String[] opcoes = {Config.t("CONTINUAR"), Config.t("REINICIAR"), Config.t("CONFIG"), Config.t("MENU_PRINCIPAL")};
        for (int i = 0; i < opcoes.length; i++) {
            boolean sel = (i == opcao);
            String label = sel ? "> " + opcoes[i] + " <" : opcoes[i];
            StartMenu.textoCentrado(text, label, 0f, 0.05f - i * 0.18f, 0.7f, shaderProgram, uniModelMatrix, uniColor, uniAlpha,
                    sel ? 1f : 0.6f, sel ? 1f : 0.6f, sel ? 0f : 0.6f);
        }
    }
}