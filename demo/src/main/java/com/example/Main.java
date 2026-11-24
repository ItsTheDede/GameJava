package com.example;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.*;
import org.joml.Matrix4f;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private int shaderProgram, vaoId, uniModelMatrix, uniTexture, uniColor, uniTint, uniAlpha, uniUseTexture;
    private int texPlayer, texPlayerDamage, texEnemy, texEnemy2, texBoss, texBoss2, texLaser, texBackground, texBackground2, texBackground3, texBackground4;

    private TextRenderer textRenderer;
    private SettingsMenu config = new SettingsMenu();

    private Player player;
    private List<Inimigo> inimigos = new ArrayList<>();
    private List<Tiro> tiros = new ArrayList<>();
    private List<TiroInimigo> tirosInimigos = new ArrayList<>();
    private List<Particula> particulas = new ArrayList<>();
    private Random random = new Random();

    private List<Estrela> estrelas = new ArrayList<>();
    // Animação de mudança de cenário (warp)
    private boolean transicaoAtiva = false;
    private float transicaoTempo = 0.0f, warp = 0.0f, flash = 0.0f;
    private int nivelDestino = 0;
    private static final float TRANS_DURACAO = 3.0f; // duração total (s)
    private static final float TRANS_PICO = 1.2f;    // momento da troca de fundo (s)
    private int fundoAtualNivel = 0;

    // CORREÇÃO AQUI: O índice 3 agora aponta para o fundo 3 (background4.png).
    // Isto garante que ao matar o boss 4 (bossAtual = 4), o alvo seja 3,
    // diferente do fundo anterior (2), acionando a transição.
    private static final int[] FUNDO_POR_BOSS = {0, 1, 2, 3, 3};

    private static class Estrela {
        float x, y, vel, tamanho, brilho;
        Estrela(float x, float y, float vel, float tamanho, float brilho) {
            this.x = x; this.y = y; this.vel = vel;
            this.tamanho = tamanho; this.brilho = brilho;
        }
    }

    private float tempoAnterior, spawnTimer = 0.0f;
    private boolean noMenuInicial = true, pausado = false, gameOver = false, escolhendoMelhoria = false;
    private int menuOpcaoInicial = 0, menuOpcaoPausa = 0;

    private int inimigosMortos = 0, bossAtual = 0;
    private boolean bossNaTela = false;
    private int[] vidaDosBoss = {30, 50, 70, 90};
    private float screenShake = 0.0f, tempoDanoPlayer = 0.0f, tempoInvencivel = 0.0f;

    private float[] bgR = {0.05f, 0.1f, 0.2f, 0.1f};
    private float[] bgG = {0.05f, 0.0f, 0.05f, 0.1f};
    private float[] bgB = {0.1f, 0.1f, 0.1f, 0.2f};

    public void run() {
        Config.carregar();
        init();
        setupGraphics();
        carregarTexturas();
        textRenderer = new TextRenderer("/font.ttf", 48, vaoId);
        player = new Player();
        inicializarFundo();
        tempoAnterior = (float) glfwGetTime();
        loop();
        limpar();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        window = glfwCreateWindow(800, 600, "SpaceWar", NULL, NULL);

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            // Se o menu de configurações está aberto, ele trata de todas as teclas
            if (config.isAberto()) { config.aoTecla(key, action); return; }

            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true);

            if (noMenuInicial && action == GLFW_PRESS) {
                if (key == GLFW_KEY_UP && menuOpcaoInicial > 0) menuOpcaoInicial--;
                if (key == GLFW_KEY_DOWN && menuOpcaoInicial < 2) menuOpcaoInicial++;
                if (key == GLFW_KEY_ENTER) {
                    if (menuOpcaoInicial == 0) { noMenuInicial = false; reiniciarJogo(); }
                    else if (menuOpcaoInicial == 1) config.abrir();
                    else if (menuOpcaoInicial == 2) glfwSetWindowShouldClose(window, true);
                }
            }

            if (pausado && action == GLFW_PRESS) {
                if (key == GLFW_KEY_UP && menuOpcaoPausa > 0) menuOpcaoPausa--;
                if (key == GLFW_KEY_DOWN && menuOpcaoPausa < 3) menuOpcaoPausa++;
                if (key == GLFW_KEY_ENTER) {
                    if (menuOpcaoPausa == 0) pausado = false;
                    else if (menuOpcaoPausa == 1) { reiniciarJogo(); pausado = false; }
                    else if (menuOpcaoPausa == 2) config.abrir();
                    else if (menuOpcaoPausa == 3) { reiniciarJogo(); pausado = false; noMenuInicial = true; }
                }
            }

            // Game over: ENTER volta ao menu inicial
            if (gameOver && !noMenuInicial && action == GLFW_PRESS && key == GLFW_KEY_ENTER) {
                reiniciarJogo();
                noMenuInicial = true;
            }

            if (key == Config.tecla(Config.Acao.PAUSAR) && action == GLFW_PRESS && !gameOver && !escolhendoMelhoria && !noMenuInicial) {
                pausado = !pausado; menuOpcaoPausa = 0;
            }

            if (escolhendoMelhoria && action == GLFW_PRESS) {
                if (key == GLFW_KEY_1) { player.melhorarVida(); escolhendoMelhoria = false; }
                if (key == GLFW_KEY_2) { player.melhorarResistencia(); escolhendoMelhoria = false; }
                if (key == GLFW_KEY_3) { player.melhorarTiro(); escolhendoMelhoria = false; }
            }
        });

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);
    }

    private void reiniciarJogo() {
        player = new Player(); inimigos.clear(); tiros.clear(); tirosInimigos.clear(); particulas.clear();
        inimigosMortos = 0; bossAtual = 0; bossNaTela = false; gameOver = false;
        escolhendoMelhoria = false; spawnTimer = 0; screenShake = 0; tempoDanoPlayer = 0; tempoInvencivel = 0;
        transicaoAtiva = false; transicaoTempo = 0.0f; warp = 0.0f; flash = 0.0f; fundoAtualNivel = 0;
    }

    private void inicializarFundo() {
        estrelas.clear();
        for (int i = 0; i < 120; i++) {
            float vel = random.nextFloat() * 0.4f + 0.1f;
            estrelas.add(new Estrela(
                    (random.nextFloat() * 2.0f) - 1.0f,
                    (random.nextFloat() * 2.0f) - 1.0f,
                    vel, 0.005f + vel * 0.01f, 0.3f + random.nextFloat() * 0.7f
            ));
        }
    }

    private void atualizarFundo(float delta) {
        for (Estrela e : estrelas) {
            e.y -= e.vel * delta * 3.0f * (1.0f + warp * 12.0f);
            if (e.y < -1.0f) {
                e.y = 1.0f;
                e.x = (random.nextFloat() * 2.0f) - 1.0f;
            }
        }
    }

    private void desenharFundoAnimado(int nivel) {
        glUniform1i(uniUseTexture, 0);
        float tintR = Math.min(1.0f, 0.6f + bgR[nivel]);
        float tintG = Math.min(1.0f, 0.6f + bgG[nivel]);
        float tintB = Math.min(1.0f, 0.9f + bgB[nivel]);

        for (Estrela e : estrelas) {
            float a = 0.6f + e.brilho * 0.4f;
            glUniform3f(uniColor, tintR * e.brilho + (1 - e.brilho) * 0.8f, tintG * e.brilho + (1 - e.brilho) * 0.8f, tintB);
            desenhar(e.x, e.y, e.tamanho, e.tamanho * (1.0f + warp * 25.0f), 0, 0, Math.min(1.0f, a + warp * 0.4f));
        }
        restaurarTextura();
    }

    private void setupGraphics() {
        GL.createCapabilities();
        glEnable(GL_BLEND); glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Vertex shader: uUV permite escolher uma sub-região da textura (usado pelo texto)
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, "#version 330 core\nlayout (location = 0) in vec3 aPos;\nlayout (location = 1) in vec2 aTexCoord;\nout vec2 TexCoord;\nuniform mat4 uModel;\nuniform vec4 uUV;\nvoid main() { gl_Position = uModel * vec4(aPos, 1.0); TexCoord = uUV.xy + aTexCoord * (uUV.zw - uUV.xy); }\0");
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == 0)
            System.err.println("Erro no vertex shader: " + glGetShaderInfoLog(vertexShader));

        // Fragment shader: useFont == 1 usa o canal vermelho da textura como alpha
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, "#version 330 core\nin vec2 TexCoord;\nout vec4 FragColor;\nuniform sampler2D uTexture;\nuniform vec3 uColor;\nuniform vec3 uTint;\nuniform float uAlpha;\nuniform int useTexture;\nuniform int useFont;\nvoid main() {\n if (useFont == 1) {\n float a = texture(uTexture, TexCoord).r;\n FragColor = vec4(uColor, a * uAlpha);\n } else if (useTexture == 1) {\n vec4 texColor = texture(uTexture, TexCoord);\n if(texColor.a < 0.1) discard;\n FragColor = vec4(texColor.rgb * uTint, texColor.a);\n } else {\n FragColor = vec4(uColor, uAlpha);\n }\n}\0");
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == 0)
            System.err.println("Erro no fragment shader: " + glGetShaderInfoLog(fragmentShader));

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        uniModelMatrix = glGetUniformLocation(shaderProgram, "uModel");
        uniTexture = glGetUniformLocation(shaderProgram, "uTexture");
        uniColor = glGetUniformLocation(shaderProgram, "uColor");
        uniTint = glGetUniformLocation(shaderProgram, "uTint");
        uniAlpha = glGetUniformLocation(shaderProgram, "uAlpha");
        uniUseTexture = glGetUniformLocation(shaderProgram, "useTexture");
        glUseProgram(shaderProgram);

        // Valor por defeito do uUV: textura inteira
        glUniform4f(glGetUniformLocation(shaderProgram, "uUV"), 0f, 0f, 1f, 1f);

        float[] vertices = { -0.5f, -0.5f, 0.0f, 0.0f, 0.0f, 0.5f, -0.5f, 0.0f, 1.0f, 0.0f, 0.5f, 0.5f, 0.0f, 1.0f, 1.0f, -0.5f, 0.5f, 0.0f, 0.0f, 1.0f };
        int[] indices = { 0, 1, 2, 2, 3, 0 };

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        int vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        int eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
    }

    private int carregarUmaTextura(String caminho) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1), h = stack.mallocInt(1), channels = stack.mallocInt(1);
            STBImage.stbi_set_flip_vertically_on_load(true);
            ByteBuffer image = STBImage.stbi_load(caminho, w, h, channels, 4);
            if (image == null) { System.err.println("ERRO: Falha ao carregar: " + caminho); return 0; }
            int texId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, texId);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w.get(0), h.get(0), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            STBImage.stbi_image_free(image);
            return texId;
        }
    }

    private void carregarTexturas() {
        texBackground = carregarUmaTextura("demo/src/main/resources/background.png");
        texBackground2 = carregarUmaTextura("demo/src/main/resources/background2.png");
        texBackground3 = carregarUmaTextura("demo/src/main/resources/background3.png");
        texBackground4 = carregarUmaTextura("demo/src/main/resources/background4.png");
        texPlayer = carregarUmaTextura("demo/src/main/resources/player.png");
        texPlayerDamage = carregarUmaTextura("demo/src/main/resources/playerdamage.png");
        texEnemy = carregarUmaTextura("demo/src/main/resources/enemy.png");
        texEnemy2 = carregarUmaTextura("demo/src/main/resources/enemy2.png");
        texBoss = carregarUmaTextura("demo/src/main/resources/boss.png");
        texBoss2 = carregarUmaTextura("demo/src/main/resources/boss2.png");
        texLaser = carregarUmaTextura("demo/src/main/resources/laser.png");
    }

    private void criarExplosao(float x, float y) {
        for (int i = 0; i < 15; i++) particulas.add(new Particula(x, y, texLaser));
        screenShake = 0.1f;
    }

    private void restaurarTextura() {
        glUniform1i(uniUseTexture, 1);
        glUniform3f(uniTint, 1.0f, 1.0f, 1.0f);
    }

    /** Atalho para desenhar texto centrado em cx. */
    private void texto(String s, float cx, float y, float scale, float r, float g, float b) {
        StartMenu.textoCentrado(textRenderer, s, cx, y, scale, shaderProgram, uniModelMatrix, uniColor, uniAlpha, r, g, b);
    }

    private void loop() {
        while (!glfwWindowShouldClose(window)) {
            float tempoAtual = (float) glfwGetTime();
            float delta = tempoAtual - tempoAnterior;
            tempoAnterior = tempoAtual;

            // Se as animações foram desligadas (ex.: a partir do menu de pausa) a meio de uma
            // transição, termina-a já para o fundo não ficar com zoom/warp.
            if (!Config.animacoes) atualizarTransicao(0f);

            int nivel = Math.min(bossAtual, 3);
            glClearColor(bgR[nivel], bgG[nivel], bgB[nivel], 1.0f);

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(shaderProgram);

            int texturaDoFundo = texBackground;
            if (fundoAtualNivel == 1 && texBackground2 != 0) texturaDoFundo = texBackground2;
            if (fundoAtualNivel == 2 && texBackground3 != 0) texturaDoFundo = texBackground3;
            if (fundoAtualNivel == 3 && texBackground4 != 0) texturaDoFundo = texBackground4;

            if (texturaDoFundo != 0) {
                restaurarTextura();
                float zoom = 2.5f * (1.0f + warp * 0.25f); // "soco" de zoom durante o warp
                desenhar(0.0f, 0.0f, zoom, zoom, 0, texturaDoFundo, 1.0f);
            }

            desenharFundoAnimado(nivel);

            // estrelas a mexer por trás do menu inicial
            if (noMenuInicial && Config.animacoes) atualizarFundo(delta);

            if (!gameOver && !escolhendoMelhoria && !pausado && !noMenuInicial) {
                atualizarTransicao(delta);
                atualizarFundo(delta);

                player.atualizar(window, delta, tempoAtual);
                if (tempoInvencivel > 0) tempoInvencivel -= delta;
                if (Config.pressionada(window, Config.Acao.ATIRAR) && player.podeAtirar(tempoAtual)) {
                    tiros.add(new Tiro(player.getX(), player.getY() + 0.1f, player.getDanoDoTiro(), texLaser));
                }
                if (!bossNaTela) {
                    spawnTimer += delta;
                    if (spawnTimer > 1.0f) {
                        float xAleatorio = (random.nextFloat() * 1.6f) - 0.8f;
                        inimigos.add(new Inimigo(xAleatorio, 0.9f));
                        spawnTimer = 0.0f;
                    }
                    if (inimigosMortos >= (bossAtual + 1) * 10 && bossAtual < vidaDosBoss.length) {
                        inimigos.clear();
                        inimigos.add(new Inimigo(0.0f, 0.8f, vidaDosBoss[bossAtual], 0.15f + (bossAtual * 0.05f)));
                        bossNaTela = true;
                    }
                }
                for (int i = 0; i < tiros.size(); i++) {
                    Tiro t = tiros.get(i); t.atualizar(delta);
                    if (t.getY() > 1.0f) { tiros.remove(i); i--; }
                }
                for (int i = 0; i < particulas.size(); i++) {
                    Particula p = particulas.get(i); p.atualizar(delta);
                    if (p.vida <= 0) { particulas.remove(i); i--; }
                }
                // Tiros dos inimigos: mover, remover os que saem do ecrã e testar acerto no jogador
                for (int i = 0; i < tirosInimigos.size(); i++) {
                    TiroInimigo ti = tirosInimigos.get(i);
                    ti.atualizar(delta);
                    if (ti.foraDoEcra()) { tirosInimigos.remove(i); i--; continue; }
                    if (tempoInvencivel <= 0 && Math.abs(ti.getX() - player.getX()) < 0.08f && Math.abs(ti.getY() - player.getY()) < 0.08f) {
                        player.tomarDano(ti.getDano());
                        tempoDanoPlayer = 0.2f; tempoInvencivel = 1.0f;
                        criarExplosao(player.getX(), player.getY());
                        screenShake = 0.15f;
                        tirosInimigos.remove(i); i--;
                        if (player.getVida() <= 0) gameOver = true;
                    }
                }
                for (int i = 0; i < inimigos.size(); i++) {
                    Inimigo ini = inimigos.get(i);
                    ini.atualizar(delta);
                    if (ini.getY() < -1.0f) { inimigos.remove(i); i--; continue; }
                    // só atira se já está no ecrã e ainda está acima do jogador
                    if (ini.getY() < 0.95f && ini.getY() > player.getY() && ini.deveAtirar()) dispararInimigo(ini);
                    if (tempoInvencivel <= 0 && Math.abs(ini.getX() - player.getX()) < 0.15f && Math.abs(ini.getY() - player.getY()) < 0.15f) {
                        player.tomarDano(ini.getDanoNoPlayer());
                        tempoDanoPlayer = 0.2f; tempoInvencivel = 1.0f; screenShake = 0.15f;
                        criarExplosao(ini.getX(), ini.getY());
                        inimigos.remove(i); i--;
                        if (ini.isBoss()) bossNaTela = false;
                        if (player.getVida() <= 0) gameOver = true;
                        continue;
                    }
                    for (int j = 0; j < tiros.size(); j++) {
                        Tiro t = tiros.get(j);
                        if (Math.abs(t.getX() - ini.getX()) < 0.05f && Math.abs(t.getY() - ini.getY()) < 0.05f) {
                            ini.tomarDano(t.getDano());
                            tiros.remove(j); j--;
                            particulas.add(new Particula(t.getX(), t.getY(), texLaser));
                            if (ini.getVida() <= 0) {
                                criarExplosao(ini.getX(), ini.getY());
                                inimigos.remove(i); i--;
                                if (ini.isBoss()) {
                                    bossAtual++; bossNaTela = false; escolhendoMelhoria = true; tiros.clear(); tirosInimigos.clear();
                                } else { inimigosMortos++; }
                                break;
                            }
                        }
                    }
                }
            }

            // Tremor de ecrã: só desloca a imagem se as animações estiverem ligadas,
            // mas o valor continua a decrescer na mesma.
            float offsetX = 0, offsetY = 0;
            if (screenShake > 0) {
                if (Config.animacoes && Config.tremor) {
                    offsetX = (random.nextFloat() - 0.5f) * screenShake;
                    offsetY = (random.nextFloat() - 0.5f) * screenShake;
                }
                screenShake -= delta * 0.5f;
            }

            if (!noMenuInicial) {
                restaurarTextura();
                if (player.getVida() > 0) {
                    float rotacao = 0.0f;
                    if (Config.pressionada(window, Config.Acao.ESQUERDA) && !pausado) rotacao = 0.2f;
                    if (Config.pressionada(window, Config.Acao.DIREITA) && !pausado) rotacao = -0.2f;
                    if (tempoDanoPlayer > 0) {
                        tempoDanoPlayer -= delta;
                        int texturaDano = (!Config.animacoes || (int)(tempoDanoPlayer * 30) % 2 == 0) ? texPlayerDamage : texPlayer;
                        desenhar(player.getX() + offsetX, player.getY() + offsetY, 0.15f, 0.15f, rotacao, texturaDano, 1.0f);
                    } else if (tempoInvencivel > 0) {
                        if (!Config.animacoes || (int)(tempoInvencivel * 15) % 2 == 0)
                            desenhar(player.getX() + offsetX, player.getY() + offsetY, 0.15f, 0.15f, rotacao, texPlayer, 1.0f);
                    } else {
                        desenhar(player.getX() + offsetX, player.getY() + offsetY, 0.15f, 0.15f, rotacao, texPlayer, 1.0f);
                    }
                }
                for (Tiro t : tiros) desenhar(t.getX() + offsetX, t.getY() + offsetY, 0.05f, 0.1f, 0, t.getTexturaId(), 1.0f);
                glUniform3f(uniTint, 1.0f, 0.3f, 0.3f); // laser inimigo avermelhado
                for (TiroInimigo ti : tirosInimigos) desenhar(ti.getX() + offsetX, ti.getY() + offsetY, 0.05f, 0.1f, ti.getRotacao(), texLaser, 1.0f);
                restaurarTextura();

                // Texturas de inimigo/boss mudam no mundo 2 (fundo background2.png)
                boolean mundo2 = (fundoAtualNivel == 1);
                int texInimigo = (mundo2 && texEnemy2 != 0) ? texEnemy2 : texEnemy;
                int texChefe   = (mundo2 && texBoss2 != 0)  ? texBoss2  : texBoss;

                for (Inimigo ini : inimigos) {
                    glUniform3f(uniTint, ini.getR(), ini.getG(), ini.getB());
                    if (ini.isBoss()) desenhar(ini.getX() + offsetX, ini.getY() + offsetY, 0.35f, 0.35f, 0, texChefe, 1.0f);
                    else desenhar(ini.getX() + offsetX, ini.getY() + offsetY, 0.15f, 0.15f, 0, texInimigo, 1.0f);
                }
                restaurarTextura();
                if (Config.animacoes)
                    for (Particula p : particulas) desenhar(p.x + offsetX, p.y + offsetY, p.tamanho, p.tamanho, 0, p.texturaId, 1.0f);
            }

            glUniform1i(uniUseTexture, 0);
            desenharTransicao();

            if (config.isAberto()) {
                config.desenhar(textRenderer, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha);
            } else if (noMenuInicial) {
                StartMenu.desenharMenuInicial(textRenderer, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha, menuOpcaoInicial);
            } else if (pausado) {
                Pause.desenharMenuPausa(textRenderer, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha, menuOpcaoPausa);
            } else if (escolhendoMelhoria) {
                glUniform1f(uniAlpha, 1.0f);
                glUniform3f(uniColor, 0.0f, 1.0f, 0.0f); desenhar(0.0f, 0.0f, 2.0f, 2.0f, 0, 0, 0.8f);
                glUniform3f(uniColor, 0.0f, 0.5f, 0.0f); desenhar(-0.5f, 0.0f, 0.2f, 0.2f, 0, 0, 1.0f);
                glUniform3f(uniColor, 0.0f, 0.5f, 1.0f); desenhar( 0.0f, 0.0f, 0.2f, 0.2f, 0, 0, 1.0f);
                glUniform3f(uniColor, 1.0f, 0.5f, 0.0f); desenhar( 0.5f, 0.0f, 0.2f, 0.2f, 0, 0, 1.0f);

                texto(Config.t("M_TITULO"), 0.0f, 0.45f, 0.8f, 1f, 1f, 1f);
                texto(Config.t("M_VIDA"), -0.5f, -0.2f, 0.35f, 1f, 1f, 1f);
                texto(Config.t("M_RESIST"), 0.0f, -0.2f, 0.35f, 1f, 1f, 1f);
                texto(Config.t("M_TIRO"), 0.5f, -0.2f, 0.35f, 1f, 1f, 1f);
            } else if (gameOver) {
                StartMenu.desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 0.3f, 0f, 0f, 0.6f);
                texto(Config.t("GAME_OVER"), 0.0f, 0.1f, 1.4f, 1f, 0.2f, 0.2f);
                texto(Config.t("PRESSIONA_ENTER"), 0.0f, -0.2f, 0.6f, 1f, 1f, 1f);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }

    /** Cria os tiros de um inimigo. O boss dispara um leque; os inimigos comuns um tiro a direito. */
    private void dispararInimigo(Inimigo ini) {
        if (ini.isBoss()) {
            int balas = 3 + (bossAtual / 2) * 2;   // boss 1 e 2: 3 balas; boss 3 e 4: 5 balas
            float abertura = 0.25f;                 // ângulo entre balas (radianos)
            for (int k = 0; k < balas; k++) {
                float angulo = (k - (balas - 1) / 2.0f) * abertura;
                tirosInimigos.add(new TiroInimigo(ini.getX(), ini.getY() - 0.2f, angulo, 0.7f, 15));
            }
        } else {
            tirosInimigos.add(new TiroInimigo(ini.getX(), ini.getY() - 0.1f, 0f, 0.7f, 10));
        }
    }

    /** Decide quando começa a transição e calcula a intensidade do warp e do flash. */
    private void atualizarTransicao(float delta) {
        int alvo = FUNDO_POR_BOSS[Math.min(bossAtual, FUNDO_POR_BOSS.length - 1)]; // 4 fundos (0 a 3)

        // Animações desligadas: troca o fundo logo, sem warp, flash nem banner
        if (!Config.animacoes) {
            fundoAtualNivel = alvo;
            transicaoAtiva = false;
            warp = 0.0f; flash = 0.0f;
            return;
        }

        if (!transicaoAtiva && alvo != fundoAtualNivel) {
            transicaoAtiva = true;
            transicaoTempo = 0.0f;
            nivelDestino = alvo;
        }
        if (!transicaoAtiva) { warp = 0.0f; flash = 0.0f; return; }

        transicaoTempo += delta;
        float t = transicaoTempo;

        // warp: sobe até ao pico e depois desce
        float i = (t < TRANS_PICO) ? t / TRANS_PICO : 1.0f - (t - TRANS_PICO) / (TRANS_DURACAO - TRANS_PICO);
        i = Math.max(0.0f, Math.min(1.0f, i));
        warp = i * i * (3.0f - 2.0f * i);

        // flash branco no momento da troca (esconde a mudança de fundo)
        flash = Math.max(0.0f, 1.0f - Math.abs(t - TRANS_PICO) / 0.35f);
        if (t >= TRANS_PICO) fundoAtualNivel = nivelDestino;

        screenShake = Math.max(screenShake, 0.03f * warp);

        if (t >= TRANS_DURACAO) { transicaoAtiva = false; warp = 0.0f; flash = 0.0f; }
    }

    /** Escurecimento azulado, flash branco e banner "SETOR N". */
    private void desenharTransicao() {
        if (!transicaoAtiva) return;

        if (warp > 0.0f)
            StartMenu.desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 0.05f, 0.1f, 0.3f, 0.35f * warp);
        if (flash > 0.0f)
            StartMenu.desenharRect(vaoId, uniModelMatrix, uniColor, uniAlpha, 0f, 0f, 2.5f, 2.5f, 1f, 1f, 1f, flash);

        float t = transicaoTempo;
        float inicio = TRANS_PICO + 0.15f;
        if (t > inicio) {
            float p = Math.min(1.0f, (t - inicio) / 0.5f);                                    // entrada
            float saida = Math.max(0.0f, Math.min(1.0f, (t - (TRANS_DURACAO - 0.6f)) / 0.6f)); // saída
            float escala = 1.6f - 0.6f * (1.0f - (1.0f - p) * (1.0f - p));                    // encolhe com easing
            textRenderer.setAlpha(p * (1.0f - saida));
            texto(Config.t("SETOR") + " " + (nivelDestino + 1), 0.0f, 0.5f, escala, 1f, 1f, 0.4f);
            textRenderer.setAlpha(1.0f);
        }
    }

    private void desenhar(float x, float y, float sX, float sY, float rotacao, int textura, float alpha) {
        Matrix4f model = new Matrix4f().translate(x, y, 0).rotateZ(rotacao).scale(sX, sY, 1.0f);
        try (MemoryStack stack = stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            model.get(buffer);
            glUniformMatrix4fv(uniModelMatrix, false, buffer);
        }

        if (textura != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, textura);
            glUniform1i(uniTexture, 0);
            glUniform1i(uniUseTexture, 1);
        } else {
            glUniform1i(uniUseTexture, 0);
        }
        glUniform1f(uniAlpha, alpha);

        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

    private void limpar() {
        glDeleteTextures(texPlayer); glDeleteTextures(texPlayerDamage);
        glDeleteTextures(texEnemy); glDeleteTextures(texEnemy2);
        glDeleteTextures(texBoss); glDeleteTextures(texBoss2);
        glDeleteTextures(texLaser);
        glDeleteTextures(texBackground); glDeleteTextures(texBackground2); glDeleteTextures(texBackground3); glDeleteTextures(texBackground4);
        if (textRenderer != null) textRenderer.cleanup();
        glDeleteProgram(shaderProgram);
        if (window != NULL) { glfwFreeCallbacks(window); glfwDestroyWindow(window); }
        glfwTerminate();
        GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null) errorCallback.free();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}