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
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {
    private long window;
    private int shaderProgram, vaoId, uniModelMatrix, uniTexture, uniColor, uniTint, uniAlpha;
    private int texPlayer, texPlayerDamage, texEnemy, texBoss, texLaser, texBackground;
    
    private TextRenderer textRenderer;

    private Player player;
    private List<Inimigo> inimigos = new ArrayList<>();
    private List<Tiro> tiros = new ArrayList<>();
    private List<Particula> particulas = new ArrayList<>();
    private Random random = new Random();

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
        init();
        setupGraphics();
        carregarTexturas();
        textRenderer = new TextRenderer("/font.ttf", 48); 
        player = new Player();
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
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) glfwSetWindowShouldClose(window, true);
            
            if (noMenuInicial && action == GLFW_PRESS) {
                if (key == GLFW_KEY_UP && menuOpcaoInicial > 0) menuOpcaoInicial--;
                if (key == GLFW_KEY_DOWN && menuOpcaoInicial < 1) menuOpcaoInicial++;
                if (key == GLFW_KEY_ENTER) {
                    if (menuOpcaoInicial == 0) { noMenuInicial = false; reiniciarJogo(); }
                    else if (menuOpcaoInicial == 1) glfwSetWindowShouldClose(window, true);
                }
            }

            if (pausado && action == GLFW_PRESS) {
                if (key == GLFW_KEY_UP && menuOpcaoPausa > 0) menuOpcaoPausa--;
                if (key == GLFW_KEY_DOWN && menuOpcaoPausa < 2) menuOpcaoPausa++;
                if (key == GLFW_KEY_ENTER) {
                    if (menuOpcaoPausa == 0) pausado = false;
                    else if (menuOpcaoPausa == 1) { reiniciarJogo(); pausado = false; }
                    else if (menuOpcaoPausa == 2) { reiniciarJogo(); pausado = false; noMenuInicial = true; }
                }
            }

            if (key == GLFW_KEY_L && action == GLFW_PRESS && !gameOver && !escolhendoMelhoria && !noMenuInicial) {
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
        player = new Player(); inimigos.clear(); tiros.clear(); particulas.clear();
        inimigosMortos = 0; bossAtual = 0; bossNaTela = false; gameOver = false;
        escolhendoMelhoria = false; spawnTimer = 0; screenShake = 0; tempoDanoPlayer = 0; tempoInvencivel = 0;
    }

    private void setupGraphics() {
        GL.createCapabilities();
        glEnable(GL_BLEND); glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, "#version 330 core\nlayout (location = 0) in vec3 aPos;\nlayout (location = 1) in vec2 aTexCoord;\nout vec2 TexCoord;\nuniform mat4 uModel;\nvoid main() { gl_Position = uModel * vec4(aPos, 1.0); TexCoord = aTexCoord; }\0");
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, "#version 330 core\nin vec2 TexCoord;\nout vec4 FragColor;\nuniform sampler2D uTexture;\nuniform vec3 uColor;\nuniform vec3 uTint;\nuniform float uAlpha;\nuniform int useTexture;\nvoid main() { \n if (useTexture == 1) {\n vec4 texColor = texture(uTexture, TexCoord);\n if(texColor.a < 0.1) discard;\n FragColor = vec4(texColor.rgb * uTint, texColor.a);\n } else {\n FragColor = vec4(uColor, uAlpha);\n }\n}\0");
        glCompileShader(fragmentShader);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        
        uniModelMatrix = glGetUniformLocation(shaderProgram, "uModel");
        uniTexture = glGetUniformLocation(shaderProgram, "uTexture");
        uniColor = glGetUniformLocation(shaderProgram, "uColor");
        uniTint = glGetUniformLocation(shaderProgram, "uTint");
        uniAlpha = glGetUniformLocation(shaderProgram, "uAlpha");
        glUseProgram(shaderProgram);

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
        texPlayer = carregarUmaTextura("demo/src/main/resources/player.png");
        texPlayerDamage = carregarUmaTextura("demo/src/main/resources/playerdamage.png");
        texEnemy = carregarUmaTextura("demo/src/main/resources/enemy.png");
        texBoss = carregarUmaTextura("demo/src/main/resources/boss.png");
        texLaser = carregarUmaTextura("demo/src/main/resources/laser.png");
    }

    private void criarExplosao(float x, float y) {
        for (int i = 0; i < 15; i++) particulas.add(new Particula(x, y, texLaser)); 
        screenShake = 0.1f; 
    }

    private void restaurarTextura() {
        glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        glUniform3f(uniTint, 1.0f, 1.0f, 1.0f);
    }

    private void loop() {
        int nivel = Math.min(bossAtual, 3);
        glClearColor(bgR[nivel], bgG[nivel], bgB[nivel], 1.0f);

        while (!glfwWindowShouldClose(window)) {
            float tempoAtual = (float) glfwGetTime();
            float delta = tempoAtual - tempoAnterior;
            tempoAnterior = tempoAtual;

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glUseProgram(shaderProgram);

            // FUNDO
            if (texBackground != 0) {
                restaurarTextura();
                desenhar(0.0f, 0.0f, 2.5f, 2.5f, 0, texBackground, 1.0f);
            }

            // LÓGICA
            if (!gameOver && !escolhendoMelhoria && !pausado && !noMenuInicial) {
                player.atualizar(window, delta, tempoAtual);
                if (tempoInvencivel > 0) tempoInvencivel -= delta;
                if (glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS && player.podeAtirar(tempoAtual)) {
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
                for (int i = 0; i < inimigos.size(); i++) {
                    Inimigo ini = inimigos.get(i);
                    ini.atualizar(delta);
                    if (ini.getY() < -1.0f) { inimigos.remove(i); i--; continue; }
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
                                    bossAtual++; bossNaTela = false; escolhendoMelhoria = true; tiros.clear();
                                } else { inimigosMortos++; }
                                break;
                            }
                        }
                    }
                }
            }

            // DESENHO DAS ENTIDADES
            float offsetX = 0, offsetY = 0;
            if (screenShake > 0) {
                offsetX = (random.nextFloat() - 0.5f) * screenShake;
                offsetY = (random.nextFloat() - 0.5f) * screenShake;
                screenShake -= delta * 0.5f; 
            }

            if (!noMenuInicial) {
                restaurarTextura(); 
                if (player.getVida() > 0) {
                    float rotacao = 0.0f;
                    if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS && !pausado) rotacao = 0.2f;
                    if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS && !pausado) rotacao = -0.2f;
                    if (tempoDanoPlayer > 0) {
                        tempoDanoPlayer -= delta;
                        int texturaDano = (int)(tempoDanoPlayer * 30) % 2 == 0 ? texPlayerDamage : texPlayer;
                        desenhar(player.getX() + offsetX, player.getY() + offsetY, 0.15f, 0.15f, rotacao, texturaDano, 1.0f);
                    } else if (tempoInvencivel > 0) {
                        if ((int)(tempoInvencivel * 15) % 2 == 0) desenhar(player.getX() + offsetX, player.getY() + offsetY, 0.15f, 0.15f, rotacao, texPlayer, 1.0f);
                    } else {
                        desenhar(player.getX() + offsetX, player.getY() + offsetY, 0.15f, 0.15f, rotacao, texPlayer, 1.0f);
                    }
                }
                for (Tiro t : tiros) desenhar(t.getX() + offsetX, t.getY() + offsetY, 0.05f, 0.1f, 0, t.getTexturaId(), 1.0f);
                for (Inimigo ini : inimigos) {
                    glUniform3f(uniTint, ini.getR(), ini.getG(), ini.getB());
                    if (ini.isBoss()) desenhar(ini.getX() + offsetX, ini.getY() + offsetY, 0.35f, 0.35f, 0, texBoss, 1.0f);
                    else desenhar(ini.getX() + offsetX, ini.getY() + offsetY, 0.15f, 0.15f, 0, texEnemy, 1.0f);
                }
                restaurarTextura();
                for (Particula p : particulas) desenhar(p.x + offsetX, p.y + offsetY, p.tamanho, p.tamanho, 0, p.texturaId, 1.0f);
            }

            // INTERFACES (UI)
            glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0); 

            if (noMenuInicial) {
                StartMenu.desenharMenuInicial(textRenderer, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha, menuOpcaoInicial);
            } else if (pausado) {
                Pause.desenharMenuPausa(textRenderer, shaderProgram, vaoId, uniModelMatrix, uniColor, uniAlpha, menuOpcaoPausa);
            } else if (escolhendoMelhoria) {
                glUniform1f(uniAlpha, 1.0f);
                glUniform3f(uniColor, 0.0f, 1.0f, 0.0f); desenhar(0.0f, 0.0f, 2.0f, 2.0f, 0, 0, 0.8f); 
                glUniform3f(uniColor, 0.0f, 0.5f, 0.0f); desenhar(-0.4f, 0.0f, 0.2f, 0.2f, 0, 0, 1.0f);
                glUniform3f(uniColor, 0.0f, 0.5f, 1.0f); desenhar( 0.0f, 0.0f, 0.2f, 0.2f, 0, 0, 1.0f);
                glUniform3f(uniColor, 1.0f, 0.5f, 0.0f); desenhar( 0.4f, 0.0f, 0.2f, 0.2f, 0, 0, 1.0f);
            }

            glfwSwapBuffers(window);
            glfwPollEvents();
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
            glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 1);
        } else {
            glUniform1i(glGetUniformLocation(shaderProgram, "useTexture"), 0);
        }
        glUniform1f(uniAlpha, alpha); 

        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
    }

    private void limpar() {
        glDeleteTextures(texPlayer); glDeleteTextures(texPlayerDamage); glDeleteTextures(texEnemy); glDeleteTextures(texBoss); glDeleteTextures(texLaser); glDeleteTextures(texBackground);
        glDeleteProgram(shaderProgram);
        if (window != NULL) {
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);
        }
        glfwTerminate();
        GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null) errorCallback.free();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}