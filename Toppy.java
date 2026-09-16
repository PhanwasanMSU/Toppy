import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

/**
 * ============================================================
 *           TOPPY BIRD - KAIJU BATTLE EDITION
 *         Java Swing Edition - JDK 17 (1280x720 HD)
 * ============================================================
 */
public class ToppyBird extends JPanel
        implements ActionListener, KeyListener {

    // =========================================================
    // WINDOW (HD 16:9)
    // =========================================================

    static final int SCREEN_WIDTH = 1280;
    static final int SCREEN_HEIGHT = 720;

    // =========================================================
    // PATH
    // =========================================================

    static final String BASE_PATH =
            "D:\\Toppy\\BGM\\";

    static final String BOSS_PATH =
            BASE_PATH + "boss_pixel_art_sprites_left_transparent\\";

    // =========================================================
    // GAME STAGES
    // =========================================================

    static final int STAGE_MENU = 0;
    static final int STAGE_FLAPPY = 1;
    static final int STAGE_SHOOTER = 2;

    int currentStage = STAGE_MENU;

    boolean gameRunning = true;

    // =========================================================
    // TIMER
    // =========================================================

    javax.swing.Timer gameTimer;

    long stage2StartTime = 0;

    // =========================================================
    // RANDOM
    // =========================================================

    Random random = new Random();

    // =========================================================
    // INPUT
    // =========================================================

    boolean keyUp = false;
    boolean keyDown = false;
    boolean keyLeft = false;
    boolean keyRight = false;

    // =========================================================
    // STAGE 1
    // =========================================================

    static final int GROUND_HEIGHT = 90;

    static final int BIRD_X = 300;

    static final int BIRD_SIZE = 80;

    static final int PIPE_WIDTH = 95;
    static final int PIPE_GAP = 225;
    static final int PIPE_SPEED = 5;

    static final double GRAVITY = 0.7;
    static final double JUMP_FORCE = -11.5;

    double birdY;
    double birdVelocity;

    Pipe[] pipes = new Pipe[4];

    int stage1Score = 0;
    boolean stage1GameOver = false;

    // =========================================================
    // STAGE 2 PLAYER & POWER-UPS
    // =========================================================

    static final int PLAYER_WIDTH = 80;
    static final int PLAYER_HEIGHT = 80;

    double playerX;
    double playerY;

    static final double NORMAL_PLAYER_SPEED = 6.5;
    static final double ULTIMATE_PLAYER_SPEED = 9.0;

    static final int MAX_PLAYER_HP = 20;
    int playerHP = MAX_PLAYER_HP;

    int stage2Score = 0;

    // TopGold Power-Up (ทุกๆ 20 แต้ม)
    boolean isGoldMode = false;
    long goldModeStartTime = 0;
    static final long GOLD_MODE_DURATION = 7000;
    int lastGoldScoreTrigger = 0;

    // TopUI Ultimate Form (ครบ 100 แต้ม)
    boolean isTopUIForm = false;
    long lastTopUIBeamTime = 0; // สำหรับยิงบีมในเฟสไคจู

    // =========================================================
    // METEOR
    // =========================================================

    List<Meteor> meteors =
            new ArrayList<>();

    long lastMeteorSpawn = 0;

    int meteorSpawnDelay = 600;

    // =========================================================
    // BULLETS
    // =========================================================

    List<Bullet> bullets =
            new ArrayList<>();

    long lastShotTime = 0;

    static final long SHOT_DELAY = 180;

    static final double BULLET_SPEED = 12;

    static final double NORMAL_BULLET_DAMAGE = 1.5;
    static final double GOLD_BULLET_DAMAGE = 8.0;
    static final double ULTIMATE_BULLET_DAMAGE = 15.0;

    // =========================================================
    // BOSS & MULTI-PHASE VARIABLES
    // =========================================================

    boolean bossWarning = false;
    boolean bossActive = false;
    boolean bossDefeated = false;

    int bossPhase = 1;
    boolean bossTransforming = false;
    long bossTransformStartTime = 0;
    static final long BOSS_TRANSFORM_DURATION = 2500;

    long bossivAttackTimer = 0;

    long bossWarningStart = 0;

    static final long BOSS_WARNING_TIME = 3000;

    static final long BOSS_START_TIME = 30000;

    double bossX;
    double bossY;

    static final double BOSS_PHASE1_MAX_HP = 500;
    static final double BOSS_PHASE2_MAX_HP = 1250;
    static final double BOSS_PHASE3_MAX_HP = 5000; // เลือดไคจูยักษ์ 5000 HP
    double bossHP = BOSS_PHASE1_MAX_HP;
    double currentMaxBossHP = BOSS_PHASE1_MAX_HP;

    int bossWidth = 200;
    int bossHeight = 200;

    int bossSpriteIndex = 0;

    long lastBossSpriteChange = 0;

    static final long BOSS_SPRITE_DELAY = 1500;

    long lastBossShot = 0;

    static final long BOSS_SHOT_DELAY = 1200;

    List<BossBullet> bossBullets =
            new ArrayList<>();

    // =========================================================
    // BOSS SPRITES & IMAGES
    // =========================================================

    BufferedImage[] bossSprites =
            new BufferedImage[15];

    BufferedImage darkBossImage;
    BufferedImage chargeSprite;

    BufferedImage topImage;
    BufferedImage topGoldImage;
    BufferedImage topUIImage;

    // =========================================================
    // SOUND
    // =========================================================

    Clip introSound;
    Clip bgmStage1;
    Clip bgmStage2;
    Clip grandMusic;
    Clip paradigmMusic;

    Clip jumpSound;
    Clip coinSound;
    Clip extraSound;
    Clip hitSound;

    boolean soundOn = true;

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            JFrame frame =
                    new JFrame("TOPPY BIRD - KAIJU BATTLE EDITION");

            ToppyBird game =
                    new ToppyBird();

            frame.setDefaultCloseOperation(
                    JFrame.EXIT_ON_CLOSE
            );

            frame.setResizable(false);

            frame.add(game);

            frame.pack();

            frame.setLocationRelativeTo(null);

            frame.setVisible(true);

            game.requestFocusInWindow();
        });
    }

    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public ToppyBird() {
        System.setProperty("sun.java2d.opengl", "true");

        setPreferredSize(
                new Dimension(
                        SCREEN_WIDTH,
                        SCREEN_HEIGHT
                )
        );

        setFocusable(true);

        addKeyListener(this);

        loadImages();

        loadSounds();

        resetFlappy();

        gameTimer =
                new javax.swing.Timer(
                        16,
                        this
                );

        gameTimer.start();

        playIntro();
    }

    // =========================================================
    // LOAD IMAGES
    // =========================================================

    void loadImages() {

        topImage = loadImage(BASE_PATH + "Top.png");

        topGoldImage = loadImage(BASE_PATH + "TopGold.png");
        if (topGoldImage == null) {
            topGoldImage = loadImage(BOSS_PATH + "TopGold.png");
        }

        topUIImage = loadImage(BASE_PATH + "TopUI.png");
        if (topUIImage == null) {
            topUIImage = loadImage(BOSS_PATH + "TopUI.png");
        }

        String[] names = {
                "01_boss_idle_breath_left.png",
                "02_boss_idle_crossed_arms_1_left.png",
                "03_boss_idle_crossed_arms_2_left.png",
                "04_boss_intro_look_up_left.png",
                "05_boss_intro_aura_powerup_left.png",
                "06_boss_claw_prep_left.png",
                "07_boss_claw_slash_arc_left.png",
                "08_boss_claw_slash_impact_left.png",
                "09_boss_claw_hit_effect_left.png",
                "10_boss_battle_stance_left.png",
                "11_boss_ranged_charge_left.png",
                "12_boss_ranged_gather_orb_left.png",
                "13_boss_ranged_beam_launch_left.png",
                "14_boss_ultimate_full_beam_left.png",
                "15_boss_projectile_fx_left.png"
        };

        for (int i = 0; i < names.length; i++) {
            bossSprites[i] = loadImage(BOSS_PATH + names[i]);
        }

        chargeSprite = bossSprites[10];

        darkBossImage = loadImage(BOSS_PATH + "dark_boss.png");
        if (darkBossImage == null) {
            darkBossImage = loadImage(BASE_PATH + "dark_boss.png");
        }
    }

    BufferedImage loadImage(String path) {

        try {

            File file =
                    new File(path);

            if (!file.exists()) {

                System.out.println(
                        "IMAGE NOT FOUND: "
                                + path
                );

                return null;
            }

            return ImageIO.read(file);

        } catch (IOException e) {

            System.out.println(
                    "IMAGE ERROR: "
                            + path
            );

            return null;
        }
    }

    // =========================================================
    // LOAD SOUNDS
    // =========================================================

    void loadSounds() {

        introSound    = loadClip(BASE_PATH + "Intro.wav");
        bgmStage1     = loadClip(BASE_PATH + "BGM.wav");
        bgmStage2     = loadClip(BASE_PATH + "BGM2.wav");
        grandMusic    = loadClip(BASE_PATH + "GRAND.wav");
        paradigmMusic = loadClip(BASE_PATH + "Paradigm.wav");

        jumpSound  = loadClip(BASE_PATH + "JUMP.wav");
        coinSound  = loadClip(BASE_PATH + "Coins.wav");
        extraSound = loadClip(BASE_PATH + "EXTRA.wav");
        hitSound   = loadClip(BASE_PATH + "Hit.wav");

        setVolume(bgmStage2, 0.4f);
        setVolume(grandMusic, 0.4f);
        setVolume(paradigmMusic, 0.4f);
    }

    void setVolume(Clip clip, float volume) {
        if (clip == null) return;

        try {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = 
                    (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

                if (volume <= 0.0f) {
                    gainControl.setValue(gainControl.getMinimum());
                } else {
                    float dB = (float) (Math.log10(volume) * 20.0);
                    dB = Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB));
                    gainControl.setValue(dB);
                }
            }
        } catch (Exception ignored) {
        }
    }

    Clip loadClip(String path) {

        try {

            File file =
                    new File(path);

            if (!file.exists()) {

                System.out.println(
                        "SOUND NOT FOUND: "
                                + path
                );

                return null;
            }

            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(
                            file
                    );

            Clip clip =
                    AudioSystem.getClip();

            clip.open(audio);

            audio.close();

            return clip;

        } catch (Exception e) {

            System.out.println(
                    "SOUND ERROR: "
                            + path
            );

            return null;
        }
    }

    void playClip(Clip clip) {

        if (!soundOn ||
                clip == null) {

            return;
        }

        try {

            if (clip.isRunning()) {

                clip.stop();
            }

            clip.setFramePosition(0);

            clip.start();

        } catch (Exception ignored) {
        }
    }

    void playMusic(Clip clip) {

        if (!soundOn ||
                clip == null) {

            return;
        }

        try {

            clip.stop();

            clip.setFramePosition(0);

            clip.loop(
                    Clip.LOOP_CONTINUOUSLY
            );

        } catch (Exception ignored) {
        }
    }

    void stopMusic(Clip clip) {

        if (clip == null) {

            return;
        }

        try {

            clip.stop();

            clip.setFramePosition(0);

        } catch (Exception ignored) {
        }
    }

    void playIntro() {

        playClip(introSound);
    }

    void stopAllMusic() {

        stopMusic(bgmStage1);

        stopMusic(bgmStage2);

        stopMusic(grandMusic);

        stopMusic(paradigmMusic);
    }

    // =========================================================
    // MENU
    // =========================================================

    void drawMenu(Graphics2D g) {
        g.setColor(new Color(10, 8, 25));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(255, 255, 255, 220));
        for (int i = 0; i < 90; i++) {
            int x = (i * 127) % SCREEN_WIDTH;
            int y = (i * 97) % SCREEN_HEIGHT;
            int size = (i % 2 == 0) ? 4 : 2;
            g.fillRect(x, y, size, size);
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 85));
        g.setColor(new Color(0, 0, 0));
        g.drawString("TOPPY BIRD", (SCREEN_WIDTH - g.getFontMetrics().stringWidth("TOPPY BIRD")) / 2 + 6, 166);
        
        g.setColor(new Color(255, 230, 0));
        String title = "TOPPY BIRD";
        int tw = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (SCREEN_WIDTH - tw) / 2, 160);

        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.setColor(new Color(150, 200, 255));

        String select = "< SELECT STAGE >";
        int sw = g.getFontMetrics().stringWidth(select);
        g.drawString(select, (SCREEN_WIDTH - sw) / 2, 230);

        drawPixelButton(g, 390, 290, 500, 80, "[ 1 ] ORIGINAL FLAPPY BIRD");
        drawPixelButton(g, 390, 410, 500, 80, "[ 2 ] METEOR SHOOTING BOSS");

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.setColor(new Color(220, 220, 220));

        String controls = "1 / 2 = Select Stage    |    M = Sound (On/Off)    |    ESC = Exit";
        int cw = g.getFontMetrics().stringWidth(controls);
        g.drawString(controls, (SCREEN_WIDTH - cw) / 2, 630);
    }

    void drawPixelButton(
            Graphics2D g,
            int x,
            int y,
            int w,
            int h,
            String text
    ) {
        g.setColor(new Color(0, 0, 0));
        g.fillRect(x + 6, y + 6, w, h);

        g.setColor(new Color(30, 40, 95));
        g.fillRect(x, y, w, h);

        g.setColor(new Color(255, 215, 0));
        g.drawRect(x, y, w, h);
        g.drawRect(x + 1, y + 1, w - 2, h - 2);

        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.setColor(Color.WHITE);

        int tw = g.getFontMetrics().stringWidth(text);
        int th = g.getFontMetrics().getAscent();

        g.drawString(text, x + (w - tw) / 2, y + h / 2 + th / 3);
    }

    // =========================================================
    // START STAGES
    // =========================================================

    void startStage1() {

        currentStage = STAGE_FLAPPY;

        resetFlappy();

        stopAllMusic();

        playMusic(bgmStage1);

        requestFocusInWindow();
    }

    void startStage2() {

        currentStage = STAGE_SHOOTER;

        resetShooter();

        stopAllMusic();

        playMusic(bgmStage2);

        requestFocusInWindow();
    }

    void resetFlappy() {

        birdY = SCREEN_HEIGHT / 2.0;

        birdVelocity = 0;

        stage1Score = 0;

        stage1GameOver = false;

        pipes[0] = new Pipe(800);
        pipes[1] = new Pipe(1200);
        pipes[2] = new Pipe(1600);
        pipes[3] = new Pipe(2000);
    }

    void resetShooter() {

        playerX = 120;

        playerY = SCREEN_HEIGHT / 2.0;

        playerHP = MAX_PLAYER_HP;

        stage2Score = 0;

        isGoldMode = false;
        goldModeStartTime = 0;
        lastGoldScoreTrigger = 0;

        isTopUIForm = false;
        lastTopUIBeamTime = 0;

        meteors.clear();

        bullets.clear();

        bossBullets.clear();

        bossWarning = false;

        bossActive = false;

        bossDefeated = false;

        bossPhase = 1;
        bossTransforming = false;
        bossivAttackTimer = 0;

        bossWidth = 200;
        bossHeight = 200;
        currentMaxBossHP = BOSS_PHASE1_MAX_HP;
        bossHP = currentMaxBossHP;

        bossSpriteIndex = 0;

        stage2StartTime = System.currentTimeMillis();

        lastMeteorSpawn = 0;

        lastShotTime = 0;

        lastBossShot = 0;
    }

    // =========================================================
    // GAME LOOP
    // =========================================================

    @Override
    public void actionPerformed(ActionEvent e) {

        if (!gameRunning) {
            return;
        }

        if (currentStage == STAGE_FLAPPY) {

            if (!stage1GameOver) {
                updateFlappy();
            }

        } else if (currentStage == STAGE_SHOOTER) {

            if (!bossDefeated) {
                updateShooter();
            }
        }

        repaint();
    }

    // =========================================================
    // STAGE 1 UPDATE
    // =========================================================

    void updateFlappy() {

        birdVelocity += GRAVITY;

        birdY += birdVelocity;

        for (Pipe pipe : pipes) {
            pipe.x -= PIPE_SPEED;
        }

        for (int i = 0; i < pipes.length; i++) {

            if (pipes[i].x + PIPE_WIDTH < 0) {

                int maxX = 0;

                for (Pipe p : pipes) {

                    if (p.x > maxX) {
                        maxX = p.x;
                    }
                }

                pipes[i] = new Pipe(maxX + 400);
            }
        }

        for (Pipe pipe : pipes) {

            if (!pipe.passed && pipe.x + PIPE_WIDTH < BIRD_X) {

                pipe.passed = true;

                stage1Score++;

                playClip(coinSound);

                if (stage1Score % 10 == 0) {
                    playClip(extraSound);
                }
            }
        }

        if (birdY - BIRD_SIZE / 2.0 <= 0) {
            triggerFlappyGameOver();
            return;
        }

        if (birdY + BIRD_SIZE / 2.0 >= SCREEN_HEIGHT - GROUND_HEIGHT) {
            triggerFlappyGameOver();
            return;
        }

        for (Pipe pipe : pipes) {

            if (checkPipeCollision(pipe)) {
                triggerFlappyGameOver();
                return;
            }
        }
    }

    void triggerFlappyGameOver() {
        stage1GameOver = true;
        playClip(hitSound);
        stopMusic(bgmStage1);
    }

    boolean checkPipeCollision(Pipe pipe) {

        int birdLeft = BIRD_X - BIRD_SIZE / 2;
        int birdRight = BIRD_X + BIRD_SIZE / 2;

        int birdTop = (int) birdY - BIRD_SIZE / 2;
        int birdBottom = (int) birdY + BIRD_SIZE / 2;

        if (birdRight < pipe.x || birdLeft > pipe.x + PIPE_WIDTH) {
            return false;
        }

        int topBottom = pipe.gapY - PIPE_GAP / 2;
        int bottomTop = pipe.gapY + PIPE_GAP / 2;

        if (birdTop < topBottom || birdBottom > bottomTop) {
            return true;
        }

        return false;
    }

    // =========================================================
    // STAGE 2 UPDATE
    // =========================================================

    void updateShooter() {

        updatePlayer();

        updateGoldModeTimer();
        updateTopUIKaijuBeam(); // ยิงบีมอัตโนมัติเฉพาะตอนสู้ไคจู (Phase 3)

        spawnMeteor();

        updateMeteors();

        updateBullets();

        updateBossTimer();

        if (bossTransforming) {
            updateBossTransformation();
        } else if (bossActive) {
            updateBoss();
            updateBossBullets();
        }

        checkPlayerDead();
    }

    void updatePlayer() {
        double currentSpeed = isTopUIForm ? ULTIMATE_PLAYER_SPEED : NORMAL_PLAYER_SPEED;

        if (keyUp) {
            playerY -= currentSpeed;
        }

        if (keyDown) {
            playerY += currentSpeed;
        }

        if (keyLeft) {
            playerX -= currentSpeed;
        }

        if (keyRight) {
            playerX += currentSpeed;
        }

        if (playerX < 0) {
            playerX = 0;
        }

        if (playerY < 0) {
            playerY = 0;
        }

        if (playerX + PLAYER_WIDTH > SCREEN_WIDTH) {
            playerX = SCREEN_WIDTH - PLAYER_WIDTH;
        }

        if (playerY + PLAYER_HEIGHT > SCREEN_HEIGHT - GROUND_HEIGHT) {
            playerY = SCREEN_HEIGHT - GROUND_HEIGHT - PLAYER_HEIGHT;
        }
    }

    void updateGoldModeTimer() {
        if (isGoldMode) {
            long elapsed = System.currentTimeMillis() - goldModeStartTime;
            if (elapsed >= GOLD_MODE_DURATION) {
                isGoldMode = false;
            }
        }
    }

    // TopUI ยิงลำแสงบีมได้เฉพาะใน Phase 3 (Kaiju Mode)
    void updateTopUIKaijuBeam() {
        if (isTopUIForm && bossPhase == 3 && bossActive && !bossTransforming) {
            long now = System.currentTimeMillis();
            if (now - lastTopUIBeamTime >= 5000) { // ทุกๆ 5 วินาที
                lastTopUIBeamTime = now;
                playClip(extraSound);

                double startX = playerX + PLAYER_WIDTH;
                double startY = playerY + PLAYER_HEIGHT / 2.0 - 25;
                bullets.add(new Bullet(startX, startY, true, 35.0, true));
            }
        }
    }

    void spawnMeteor() {

        long now = System.currentTimeMillis();

        if (now - lastMeteorSpawn < meteorSpawnDelay) {
            return;
        }

        lastMeteorSpawn = now;

        int size = 35 + random.nextInt(50);

        int y = random.nextInt(SCREEN_HEIGHT - GROUND_HEIGHT - size);

        double speed = 2.5 + random.nextDouble() * 3.5;

        meteors.add(new Meteor(SCREEN_WIDTH, y, size, speed));
    }

    void updateMeteors() {

        Iterator<Meteor> iterator = meteors.iterator();

        while (iterator.hasNext()) {

            Meteor meteor = iterator.next();

            meteor.x -= meteor.speed;

            if (meteor.x < -meteor.size) {
                iterator.remove();
                continue;
            }

            Rectangle meteorRect = new Rectangle((int) meteor.x, (int) meteor.y, meteor.size, meteor.size);
            Rectangle playerRect = new Rectangle((int) playerX, (int) playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

            if (meteorRect.intersects(playerRect)) {

                playerHP--;

                playClip(hitSound);

                iterator.remove();
            }
        }
    }

    void shoot() {

        long now = System.currentTimeMillis();

        if (now - lastShotTime < SHOT_DELAY) {
            return;
        }

        lastShotTime = now;

        double damage = NORMAL_BULLET_DAMAGE;
        if (isTopUIForm) {
            damage = ULTIMATE_BULLET_DAMAGE;
        } else if (isGoldMode) {
            damage = GOLD_BULLET_DAMAGE;
        }

        bullets.add(new Bullet(playerX + PLAYER_WIDTH, playerY + PLAYER_HEIGHT / 2.0 - 5, isTopUIForm, damage, false));
    }

    void updateBullets() {

        Iterator<Bullet> iterator = bullets.iterator();

        while (iterator.hasNext()) {

            Bullet bullet = iterator.next();

            bullet.x += BULLET_SPEED;

            if (bullet.x > SCREEN_WIDTH) {
                iterator.remove();
                continue;
            }

            boolean removed = false;

            Iterator<Meteor> meteorIterator = meteors.iterator();

            while (meteorIterator.hasNext()) {

                Meteor meteor = meteorIterator.next();

                Rectangle bulletRect = new Rectangle((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
                Rectangle meteorRect = new Rectangle((int) meteor.x, (int) meteor.y, meteor.size, meteor.size);

                if (bulletRect.intersects(meteorRect)) {

                    stage2Score++;

                    if (stage2Score > 0 && stage2Score % 20 == 0 && stage2Score != lastGoldScoreTrigger && !isTopUIForm) {
                        lastGoldScoreTrigger = stage2Score;
                        isGoldMode = true;
                        goldModeStartTime = System.currentTimeMillis();
                        playClip(extraSound);
                    }

                    if (stage2Score >= 100 && !isTopUIForm) {
                        isTopUIForm = true;
                        isGoldMode = false;
                        playClip(extraSound);
                    }

                    playClip(hitSound);

                    meteorIterator.remove();

                    iterator.remove();

                    removed = true;

                    break;
                }
            }

            if (removed) {
                continue;
            }

            if (bossActive && !bossTransforming) {

                Rectangle bulletRect = new Rectangle((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
                Rectangle bossRect = new Rectangle((int) bossX, (int) bossY, bossWidth, bossHeight);

                if (bulletRect.intersects(bossRect)) {

                    bossHP -= bullet.damage;

                    playClip(hitSound);

                    if (!bullet.isTopUIBeam) {
                        iterator.remove();
                    }

                    if (bossHP <= 0) {

                        if (bossPhase == 1) {
                            startBossTransformation(2);
                        } else if (bossPhase == 2) {
                            startBossTransformation(3); // เข้าสู่ Kaiju Phase (Phase 3)
                        } else {
                            bossHP = 0;
                            bossDefeated = true;
                            bossActive = false;

                            stopMusic(paradigmMusic);
                            playMusic(bgmStage2);
                        }
                    }
                }
            }
        }
    }

    // =========================================================
    // BOSS TRANSFORMATION LOGIC
    // =========================================================

    void startBossTransformation(int nextPhase) {
        bossTransforming = true;
        bossTransformStartTime = System.currentTimeMillis();
        bossPhase = nextPhase;

        bossBullets.clear();

        int healAmount = (int) Math.round(MAX_PLAYER_HP * 0.35);
        playerHP = Math.min(MAX_PLAYER_HP, playerHP + healAmount);

        playClip(extraSound);

        if (bossPhase == 2 || bossPhase == 3) {
            stopMusic(grandMusic);
            playMusic(paradigmMusic);
        }
    }

    void updateBossTransformation() {
        long elapsed = System.currentTimeMillis() - bossTransformStartTime;

        if (elapsed >= BOSS_TRANSFORM_DURATION) {
            bossTransforming = false;

            if (bossPhase == 2) {
                currentMaxBossHP = BOSS_PHASE2_MAX_HP;
                bossWidth = 200;
                bossHeight = 200;
                bossivAttackTimer = System.currentTimeMillis();
            } else if (bossPhase == 3) {
                currentMaxBossHP = BOSS_PHASE3_MAX_HP; // 5000 HP
                bossWidth = 450;  // ขยายใหญ่เป็นไคจู
                bossHeight = 450;
                bossivAttackTimer = System.currentTimeMillis();
            }

            bossHP = currentMaxBossHP;
            lastBossShot = System.currentTimeMillis();
            lastBossSpriteChange = System.currentTimeMillis();
        }
    }

    void updateBossTimer() {

        if (bossActive || bossDefeated || bossTransforming) {
            return;
        }

        long elapsed = System.currentTimeMillis() - stage2StartTime;

        if (elapsed >= BOSS_START_TIME && !bossWarning) {

            bossWarning = true;

            bossWarningStart = System.currentTimeMillis();

            stopMusic(bgmStage2);

            playClip(grandMusic);
        }

        if (bossWarning && System.currentTimeMillis() - bossWarningStart >= BOSS_WARNING_TIME) {

            bossWarning = false;

            bossActive = true;

            bossX = SCREEN_WIDTH - 280;

            bossY = 220;

            bossPhase = 1;
            bossWidth = 200;
            bossHeight = 200;
            currentMaxBossHP = BOSS_PHASE1_MAX_HP;
            bossHP = currentMaxBossHP;

            bossSpriteIndex = 0;

            lastBossSpriteChange = System.currentTimeMillis();
        }
    }

    void updateBoss() {

        double targetY = playerY - bossHeight / 2.0;

        double moveSpeed = (bossPhase == 3) ? 1.0 : ((bossPhase == 2) ? 2.2 : 1.4); // ไคจูตัวใหญ่จะเคลื่อนที่ช้าลงแต่ทรงพลัง

        if (bossY < targetY) {
            bossY += moveSpeed;
        } else if (bossY > targetY) {
            bossY -= moveSpeed;
        }

        if (bossY < 20) {
            bossY = 20;
        }

        if (bossY + bossHeight > SCREEN_HEIGHT - GROUND_HEIGHT) {
            bossY = SCREEN_HEIGHT - GROUND_HEIGHT - bossHeight;
        }

        long now = System.currentTimeMillis();

        if (bossPhase == 1) {
            if (now - lastBossSpriteChange > BOSS_SPRITE_DELAY) {
                lastBossSpriteChange = now;
                bossSpriteIndex++;
                if (bossSpriteIndex >= bossSprites.length) {
                    bossSpriteIndex = 0;
                }
            }
        }

        if (bossPhase == 3) {
            if (now - bossivAttackTimer >= 12000) {
                bossivAttackTimer = now;
                firePhase3KaijuBurst();
            } else {
                if (now - lastBossShot > 600) {
                    lastBossShot = now;
                    fireBossBullet(true);
                }
            }
        } else if (bossPhase == 2) {
            if (now - bossivAttackTimer >= 15000) {
                bossivAttackTimer = now;
                firePhase2RandomSpreadBurst();
            } else {
                if (now - lastBossShot > 850) {
                    lastBossShot = now;
                    fireBossBullet(true);
                }
            }
        } else {
            if (now - lastBossShot > BOSS_SHOT_DELAY) {
                lastBossShot = now;
                fireBossBullet(false);
            }
        }
    }

    void firePhase3KaijuBurst() {
        // การโจมตีสุดโหดของไคจู ยิงกระสุนและลูกบอลยักษ์รัวๆ
        double startX = bossX;
        double startY = bossY + bossHeight / 2.0;

        double[] angles = {-0.4, -0.2, 0.0, 0.2, 0.4};
        for (double angle : angles) {
            double speed = 18.0;
            double vx = -speed * Math.cos(angle);
            double vy = speed * Math.sin(angle);
            bossBullets.add(new BossBullet(startX, startY, vx, vy, 160, 160, true, true));
        }
    }

    void firePhase2RandomSpreadBurst() {
        int choice = random.nextInt(2);

        double startX = bossX;
        double startY = bossY + bossHeight / 2.0;

        if (choice == 0) {
            double[] angles = {-0.35, -0.18, 0.0, 0.18, 0.35};
            for (double angle : angles) {
                double speed = 24.0;
                double vx = -speed * Math.cos(angle);
                double vy = speed * Math.sin(angle);
                bossBullets.add(new BossBullet(startX, startY - 20, vx, vy, 240, 60, true, false));
            }
        } else {
            double targetX = playerX;
            double targetY = playerY + PLAYER_HEIGHT / 2.0;
            double dx = targetX - startX;
            double dy = targetY - startY;
            double baseAngle = Math.atan2(dy, dx);

            double[] angleOffsets = {-0.4, -0.2, 0.0, 0.2, 0.4};
            for (double offset : angleOffsets) {
                double angle = baseAngle + offset;
                double speed = 14.0;
                double vx = speed * Math.cos(angle);
                double vy = speed * Math.sin(angle);
                bossBullets.add(new BossBullet(startX - 30, startY - 30, vx, vy, 130, 130, false, true));
            }
        }
    }

    void fireBossBullet(boolean isDark) {

        double startX = bossX;
        double startY = bossY + bossHeight / 2.0;

        double targetX = playerX;
        double targetY = playerY + PLAYER_HEIGHT / 2.0;

        double dx = targetX - startX;
        double dy = targetY - startY;

        double length = Math.sqrt(dx * dx + dy * dy);

        if (length == 0) {
            length = 1;
        }

        double speed = (bossPhase == 3) ? 9.0 : ((bossPhase == 2) ? 7.5 : 5.5);

        double vx = dx / length * speed;
        double vy = dy / length * speed;

        bossBullets.add(new BossBullet(startX, startY, vx, vy, 22, 22, isDark, false));
    }

    void updateBossBullets() {

        Iterator<BossBullet> iterator = bossBullets.iterator();

        Rectangle playerRect = new Rectangle((int) playerX, (int) playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        while (iterator.hasNext()) {

            BossBullet bullet = iterator.next();

            bullet.x += bullet.vx;
            bullet.y += bullet.vy;

            if (bullet.x < -300 || bullet.x > SCREEN_WIDTH + 300 ||
                bullet.y < -300 || bullet.y > SCREEN_HEIGHT + 300) {

                iterator.remove();
                continue;
            }

            Rectangle bulletRect = new Rectangle((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);

            if (bulletRect.intersects(playerRect)) {

                playerHP--;

                playClip(hitSound);

                iterator.remove();
            }
        }
    }

    void checkPlayerDead() {

        if (playerHP <= 0) {

            playerHP = 0;

            resetShooter();

            stopMusic(grandMusic);
            stopMusic(paradigmMusic);

            playMusic(bgmStage2);
        }
    }

    // =========================================================
    // PAINT & PIXEL RENDERING (NEAREST NEIGHBOR RETRO STYLE)
    // =========================================================

    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        // ปิด Antialiasing และเปิด Nearest Neighbor เพื่อให้กราฟิกพิกเซลมีความคมชัดแบบสไตล์ Retro แท้ๆ
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        if (currentStage == STAGE_MENU) {

            drawMenu(g2);

        } else if (currentStage == STAGE_FLAPPY) {

            drawFlappy(g2);

        } else if (currentStage == STAGE_SHOOTER) {

            drawShooter(g2);
        }
    }

    // =========================================================
    // FLAPPY DRAW (PIXEL ART STYLE)
    // =========================================================

    void drawFlappy(Graphics2D g) {

        drawFlappyBackground(g);

        for (Pipe pipe : pipes) {
            drawPixelPipe(g, pipe);
        }

        drawTopCharacter(g, BIRD_X, (int) birdY, BIRD_SIZE, BIRD_SIZE, false, false);

        drawFlappyHUD(g);

        if (stage1GameOver) {
            drawFlappyGameOver(g);
        }
    }

    void drawFlappyBackground(Graphics2D g) {
        g.setColor(new Color(92, 148, 252));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setColor(new Color(255, 220, 50));
        g.fillRect(1040, 50, 100, 100);
        g.setColor(new Color(255, 255, 120));
        g.fillRect(1060, 70, 60, 60);

        drawPixelCloud(g, 150, 80);
        drawPixelCloud(g, 700, 120);

        g.setColor(new Color(222, 158, 65));
        g.fillRect(0, SCREEN_HEIGHT - GROUND_HEIGHT, SCREEN_WIDTH, GROUND_HEIGHT);

        g.setColor(new Color(88, 216, 88));
        g.fillRect(0, SCREEN_HEIGHT - GROUND_HEIGHT, SCREEN_WIDTH, 24);

        g.setColor(new Color(32, 152, 32));
        g.fillRect(0, SCREEN_HEIGHT - GROUND_HEIGHT + 24, SCREEN_WIDTH, 8);
    }

    void drawPixelCloud(Graphics2D g, int x, int y) {
        g.setColor(Color.WHITE);
        g.fillRect(x, y, 96, 32);
        g.fillRect(x + 16, y - 16, 64, 16);
    }

    void drawPixelPipe(Graphics2D g, Pipe pipe) {

        int topHeight = pipe.gapY - PIPE_GAP / 2;
        int bottomY = pipe.gapY + PIPE_GAP / 2;

        g.setColor(new Color(116, 187, 43));
        g.fillRect(pipe.x, 0, PIPE_WIDTH, topHeight);
        
        g.setColor(new Color(222, 239, 156));
        g.fillRect(pipe.x, 0, 8, topHeight);
        g.setColor(Color.BLACK);
        g.drawRect(pipe.x, 0, PIPE_WIDTH, topHeight);

        g.setColor(new Color(116, 187, 43));
        g.fillRect(pipe.x - 6, topHeight - 24, PIPE_WIDTH + 12, 24);
        g.setColor(Color.BLACK);
        g.drawRect(pipe.x - 6, topHeight - 24, PIPE_WIDTH + 12, 24);
        g.setColor(new Color(222, 239, 156));
        g.fillRect(pipe.x - 4, topHeight - 22, 8, 20);

        g.setColor(new Color(116, 187, 43));
        g.fillRect(pipe.x, bottomY, PIPE_WIDTH, SCREEN_HEIGHT - GROUND_HEIGHT - bottomY);
        g.setColor(new Color(222, 239, 156));
        g.fillRect(pipe.x, bottomY, 8, SCREEN_HEIGHT - GROUND_HEIGHT - bottomY);
        g.setColor(Color.BLACK);
        g.drawRect(pipe.x, bottomY, PIPE_WIDTH, SCREEN_HEIGHT - GROUND_HEIGHT - bottomY);

        g.setColor(new Color(116, 187, 43));
        g.fillRect(pipe.x - 6, bottomY, PIPE_WIDTH + 12, 24);
        g.setColor(Color.BLACK);
        g.drawRect(pipe.x - 6, bottomY, PIPE_WIDTH + 12, 24);
        g.setColor(new Color(222, 239, 156));
        g.fillRect(pipe.x - 4, bottomY + 2, 8, 20);
    }

    void drawTopCharacter(Graphics2D g, int centerX, int centerY, int targetWidth, int targetHeight, boolean isUIForm, boolean isGold) {

        BufferedImage imgToDraw = topImage;
        if (isUIForm && topUIImage != null) {
            imgToDraw = topUIImage;
        } else if (isGold && topGoldImage != null) {
            imgToDraw = topGoldImage;
        }

        if (isUIForm) {
            long now = System.currentTimeMillis();
            if ((now / 90) % 2 == 0) {
                g.setColor(new Color(0, 255, 255, 140));
                g.fillRect(centerX - targetWidth / 2 - 12, centerY - targetHeight / 2 - 12, targetWidth + 24, targetHeight + 24);
            }
        } else if (isGold) {
            long now = System.currentTimeMillis();
            if ((now / 110) % 2 == 0) {
                g.setColor(new Color(255, 215, 0, 150));
                g.fillRect(centerX - targetWidth / 2 - 10, centerY - targetHeight / 2 - 10, targetWidth + 20, targetHeight + 20);
            }
        }

        if (imgToDraw != null) {

            int imgW = imgToDraw.getWidth();
            int imgH = imgToDraw.getHeight();

            double scale = Math.min((double) targetWidth / imgW, (double) targetHeight / imgH) * 1.6;
            int drawW = (int) (imgW * scale);
            int drawH = (int) (imgH * scale);

            g.drawImage(
                    imgToDraw,
                    centerX - drawW / 2,
                    centerY - drawH / 2,
                    drawW,
                    drawH,
                    null
            );

        } else {

            g.setColor(isUIForm ? Color.WHITE : (isGold ? Color.YELLOW : Color.ORANGE));
            g.fillRect(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight);
            g.setColor(Color.BLACK);
            g.drawRect(centerX - targetWidth / 2, centerY - targetHeight / 2, targetWidth, targetHeight);
        }
    }

    void drawFlappyHUD(Graphics2D g) {
        g.setFont(new Font("Monospaced", Font.BOLD, 65));
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(stage1Score), (SCREEN_WIDTH - g.getFontMetrics().stringWidth(String.valueOf(stage1Score))) / 2 + 4, 84);
        
        g.setColor(Color.WHITE);
        String score = String.valueOf(stage1Score);
        int sw = g.getFontMetrics().stringWidth(score);
        g.drawString(score, (SCREEN_WIDTH - sw) / 2, 80);

        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.setColor(Color.YELLOW);
        g.drawString("STAGE 1 - ORIGINAL FLAPPY", 30, 40);

        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(Color.WHITE);
        g.drawString("SPACE = JUMP", 30, 68);
        g.drawString("M = SOUND", SCREEN_WIDTH - 160, 40);
        g.drawString("ESC = MENU", SCREEN_WIDTH - 160, 68);
    }

    void drawFlappyGameOver(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setFont(new Font("Monospaced", Font.BOLD, 75));
        g.setColor(Color.RED);

        String text = "GAME OVER";
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (SCREEN_WIDTH - tw) / 2, 280);

        g.setFont(new Font("Monospaced", Font.BOLD, 35));
        g.setColor(Color.YELLOW);

        String scoreText = "FINAL SCORE: " + stage1Score;
        int stw = g.getFontMetrics().stringWidth(scoreText);
        g.drawString(scoreText, (SCREEN_WIDTH - stw) / 2, 355);

        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.setColor(Color.WHITE);

        String restartText = "PRESS SPACE / ENTER TO RESTART";
        int rtw = g.getFontMetrics().stringWidth(restartText);
        g.drawString(restartText, (SCREEN_WIDTH - rtw) / 2, 435);

        String menuText = "PRESS ESC FOR MENU";
        int mtw = g.getFontMetrics().stringWidth(menuText);
        g.drawString(menuText, (SCREEN_WIDTH - mtw) / 2, 480);
    }

    // =========================================================
    // SHOOTER DRAW (PIXEL ART STYLE)
    // =========================================================

    void drawShooterBackground(Graphics2D g) {

        if (bossPhase == 3) {
            g.setColor(new Color(50, 5, 10)); // โทนสีแดงเข้มดุเดือดสำหรับไคจู
        } else if (bossPhase == 2) {
            g.setColor(new Color(30, 5, 25));
        } else {
            g.setColor(new Color(12, 10, 32));
        }
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        for (int i = 0; i < 150; i++) {
            int x = (i * 71) % SCREEN_WIDTH;
            int y = (i * 137) % (SCREEN_HEIGHT - GROUND_HEIGHT);
            int size = (i % 2 == 0) ? 3 : 2;

            if (bossPhase == 3) {
                g.setColor(new Color(255, 80, 80));
            } else if (bossPhase == 2) {
                g.setColor(new Color(255, 120 + (i * 10) % 100, 160));
            } else {
                g.setColor(new Color(160, 180 + (i * 15) % 70, 255));
            }
            g.fillRect(x, y, size, size);
        }

        if (bossPhase == 2) {
            g.setColor(new Color(140, 30, 60));
            g.fillRect(1000, 90, 130, 130);
            g.setColor(new Color(190, 60, 90));
            g.fillRect(1030, 120, 70, 70);
        } else if (bossPhase < 2) {
            g.setColor(new Color(60, 60, 130));
            g.fillRect(1000, 90, 130, 130);
            g.setColor(new Color(90, 90, 170));
            g.fillRect(1030, 120, 70, 70);
        }
    }

    void drawShooter(Graphics2D g) {

        drawShooterBackground(g);

        for (Meteor meteor : meteors) {
            drawPixelMeteor(g, meteor);
        }

        for (Bullet bullet : bullets) {
            drawPixelBullet(g, bullet);
        }

        drawTopCharacter(
                g,
                (int) playerX + PLAYER_WIDTH / 2,
                (int) playerY + PLAYER_HEIGHT / 2,
                PLAYER_WIDTH,
                PLAYER_HEIGHT,
                isTopUIForm,
                isGoldMode
        );

        if (bossActive || bossTransforming) {
            drawBoss(g);
        }

        for (BossBullet bullet : bossBullets) {
            drawPixelBossBullet(g, bullet);
        }

        drawShooterHUD(g);

        if (bossWarning) {
            drawBossWarning(g);
        }

        if (bossDefeated) {
            drawBossDefeated(g);
        }
    }

    void drawPixelMeteor(Graphics2D g, Meteor meteor) {

        int x = (int) meteor.x;
        int y = (int) meteor.y;

        g.setColor(new Color(110, 110, 120));
        g.fillRect(x, y, meteor.size, meteor.size);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, meteor.size, meteor.size);

        int hole = meteor.size / 4;
        g.setColor(new Color(60, 60, 70));
        g.fillRect(x + meteor.size / 4, y + meteor.size / 3, hole, hole);
    }

    void drawPixelBullet(Graphics2D g, Bullet bullet) {

        if (bullet.isTopUIBeam) {
            g.setColor(Color.WHITE);
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.CYAN);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        } else if (bullet.isTopUI) {
            g.setColor(Color.WHITE);
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.CYAN);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        } else if (bullet.isGold) {
            g.setColor(new Color(255, 215, 0));
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.BLACK);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.ORANGE);
            g.fillRect((int) bullet.x - 6, (int) bullet.y, 8, bullet.height);
            g.setColor(Color.BLACK);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        }
    }

    void drawBoss(Graphics2D g) {

        BufferedImage image = null;

        if (bossTransforming) {
            image = chargeSprite != null ? chargeSprite : bossSprites[10];

            long elapsed = System.currentTimeMillis() - bossTransformStartTime;
            if ((elapsed / 90) % 2 == 0) {
                g.setColor(new Color(180, 0, 255, 160));
                g.fillRect((int) bossX - 25, (int) bossY - 25, bossWidth + 50, bossHeight + 50);
            }
        } 
        else if (bossPhase >= 2) {
            image = darkBossImage != null ? darkBossImage : bossSprites[10];
        } 
        else {
            image = bossSprites[bossSpriteIndex];
        }

        if (image != null) {
            g.drawImage(image, (int) bossX, (int) bossY, bossWidth, bossHeight, null);
        } else {
            g.setColor(bossPhase == 3 ? new Color(150, 0, 0) : (bossPhase == 2 ? new Color(60, 0, 90) : new Color(130, 30, 180)));
            g.fillRect((int) bossX, (int) bossY, bossWidth, bossHeight);
            g.setColor(Color.BLACK);
            g.drawRect((int) bossX, (int) bossY, bossWidth, bossHeight);
        }

        int barW = 500;
        int barX = (SCREEN_WIDTH - barW) / 2;
        int barY = 30;
        int barH = 26;

        g.setColor(Color.BLACK);
        g.fillRect(barX - 4, barY - 4, barW + 8, barH + 8);
        
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);

        double hpPercent = bossHP / currentMaxBossHP;
        if (hpPercent < 0) hpPercent = 0;

        if (bossPhase == 3) {
            g.setColor(new Color(255, 140, 0));
        } else if (bossPhase == 2) {
            g.setColor(new Color(230, 30, 40));
        } else {
            g.setColor(new Color(180, 40, 220));
        }

        g.fillRect(barX, barY, (int) (barW * hpPercent), barH);

        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);

        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        String bossName = (bossPhase == 3) ? "KAIJU DARK BOSS  " : ((bossPhase == 2) ? "DARK BOSS  " : "BOSS  ");
        if (bossTransforming) bossName = "AWAKENING... ";

        String text = bossName + String.format("%.1f", bossHP) + " / " + (int) currentMaxBossHP;
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (SCREEN_WIDTH - tw) / 2, 24);
    }

    void drawPixelBossBullet(Graphics2D g, BossBullet bullet) {

        if (bullet.isBeam) {
            g.setColor(new Color(220, 80, 255));
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.BLACK);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        } else if (bullet.isGiantBall) {
            g.setColor(new Color(255, 50, 80));
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.YELLOW);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        } else if (bullet.isDark) {
            g.setColor(new Color(255, 30, 50));
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.BLACK);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        } else {
            g.setColor(new Color(210, 50, 255));
            g.fillRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
            g.setColor(Color.BLACK);
            g.drawRect((int) bullet.x, (int) bullet.y, bullet.width, bullet.height);
        }
    }

    void drawShooterHUD(Graphics2D g) {

        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.setColor(Color.WHITE);
        g.drawString("HP", 30, 40);

        int hpBarX = 75;
        int hpBarY = 20;
        int hpBarW = 240;
        int hpBarH = 25;

        g.setColor(Color.BLACK);
        g.fillRect(hpBarX - 2, hpBarY - 2, hpBarW + 4, hpBarH + 4);
        
        g.setColor(Color.DARK_GRAY);
        g.fillRect(hpBarX, hpBarY, hpBarW, hpBarH);

        g.setColor(new Color(50, 220, 80));
        g.fillRect(hpBarX, hpBarY, hpBarW * playerHP / MAX_PLAYER_HP, hpBarH);

        g.setColor(Color.WHITE);
        g.drawRect(hpBarX, hpBarY, hpBarW, hpBarH);

        g.drawString(playerHP + " / " + MAX_PLAYER_HP, 325, 41);

        g.setFont(new Font("Monospaced", Font.BOLD, 26));
        g.drawString("SCORE: " + stage2Score, 30, 85);

        if (bossPhase == 3) {
            g.setFont(new Font("Monospaced", Font.BOLD, 20));
            g.setColor(Color.ORANGE);
            g.drawString("⚠ KAIJU PHASE ACTIVE! TOP-UI BEAM UNLOCKED!", 30, 125);
        } else if (isTopUIForm) {
            g.setFont(new Font("Monospaced", Font.BOLD, 20));
            g.setColor(Color.CYAN);
            g.drawString("★ TOP-UI FORM ACTIVE (DMG 15.0)", 30, 125);
        } else if (isGoldMode) {
            long remaining = GOLD_MODE_DURATION - (System.currentTimeMillis() - goldModeStartTime);
            double sec = Math.max(0, remaining / 1000.0);

            g.setFont(new Font("Monospaced", Font.BOLD, 20));
            g.setColor(new Color(255, 215, 0));
            g.drawString(String.format("★ GOLD POWER: %.1fs (DMG 8.0)", sec), 30, 125);
        }

        String stageLabel = (bossPhase == 3) ? "STAGE 2 [KAIJU]" : ((bossPhase == 2) ? "STAGE 2 [PHASE 2]" : "STAGE 2");
        g.drawString(stageLabel, SCREEN_WIDTH - 280, 40);

        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g.drawString("ARROWS / WASD = MOVE", SCREEN_WIDTH - 280, 68);
        g.drawString("SPACE = SHOOT", SCREEN_WIDTH - 280, 95);
    }

    void drawBossWarning(Graphics2D g) {

        long elapsed = System.currentTimeMillis() - bossWarningStart;
        boolean flash = (elapsed / 200) % 2 == 0;

        if (flash) {
            g.setColor(new Color(255, 0, 0, 150));
            g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 75));
        g.setColor(Color.RED);

        String text = "WARNING";
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (SCREEN_WIDTH - tw) / 2, 340);

        g.setFont(new Font("Monospaced", Font.BOLD, 30));
        g.setColor(Color.WHITE);

        String sub = "BOSS APPROACHING";
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, (SCREEN_WIDTH - sw) / 2, 395);
    }

    void drawBossDefeated(Graphics2D g) {

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        g.setFont(new Font("Monospaced", Font.BOLD, 65));
        g.setColor(Color.YELLOW);

        String text = "KAIJU DEFEATED!";
        int tw = g.getFontMetrics().stringWidth(text);
        g.drawString(text, (SCREEN_WIDTH - tw) / 2, 290);

        g.setFont(new Font("Monospaced", Font.BOLD, 35));
        g.setColor(Color.WHITE);

        String sub = "FINAL SCORE: " + stage2Score;
        int sw = g.getFontMetrics().stringWidth(sub);
        g.drawString(sub, (SCREEN_WIDTH - sw) / 2, 360);

        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        g.setColor(Color.YELLOW);

        String restartText = "PRESS SPACE / ENTER TO PLAY AGAIN";
        int rtw = g.getFontMetrics().stringWidth(restartText);
        g.drawString(restartText, (SCREEN_WIDTH - rtw) / 2, 440);

        g.setColor(Color.WHITE);
        String menuText = "PRESS ESC FOR MENU";
        int mtw = g.getFontMetrics().stringWidth(menuText);
        g.drawString(menuText, (SCREEN_WIDTH - mtw) / 2, 480);
    }

    // =========================================================
    // KEY PRESSED
    // =========================================================

    @Override
    public void keyPressed(KeyEvent e) {

        int key = e.getKeyCode();

        if (currentStage == STAGE_MENU) {

            if (key == KeyEvent.VK_1) {
                startStage1();
                return;
            }

            if (key == KeyEvent.VK_2) {
                startStage2();
                return;
            }
        }

        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_ENTER) {

            if (currentStage == STAGE_FLAPPY) {

                if (stage1GameOver) {
                    resetFlappy();
                    playMusic(bgmStage1);
                } else {
                    if (key == KeyEvent.VK_SPACE) {
                        birdVelocity = JUMP_FORCE;
                        playClip(jumpSound);
                    }
                }

            } else if (currentStage == STAGE_SHOOTER) {

                if (bossDefeated) {
                    resetShooter();
                    playMusic(bgmStage2);
                } else {
                    if (key == KeyEvent.VK_SPACE) {
                        shoot();
                    }
                }
            }
        }

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            keyUp = true;
        }

        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            keyDown = true;
        }

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            keyLeft = true;
        }

        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            keyRight = true;
        }

        if (key == KeyEvent.VK_M) {
            toggleSound();
        }

        if (key == KeyEvent.VK_ESCAPE) {

            if (currentStage != STAGE_MENU) {

                currentStage = STAGE_MENU;
                stage1GameOver = false;

                stopAllMusic();
                requestFocusInWindow();

            } else {

                closeGame();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) {
            keyUp = false;
        }

        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            keyDown = false;
        }

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            keyLeft = false;
        }

        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            keyRight = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    // =========================================================
    // TOGGLE SOUND
    // =========================================================

    void toggleSound() {

        soundOn = !soundOn;

        if (!soundOn) {

            stopAllMusic();

            stopClip(introSound);
            stopClip(jumpSound);
            stopClip(coinSound);
            stopClip(extraSound);
            stopClip(hitSound);

            System.out.println("SOUND OFF");

        } else {

            if (currentStage == STAGE_FLAPPY) {

                if (!stage1GameOver) {
                    playMusic(bgmStage1);
                }

            } else if (currentStage == STAGE_SHOOTER) {

                if (bossPhase == 2) {
                    playMusic(paradigmMusic);
                } else if (bossActive || bossWarning) {
                    playMusic(grandMusic);
                } else {
                    playMusic(bgmStage2);
                }

            } else {

                playIntro();
            }

            System.out.println("SOUND ON");
        }
    }

    void stopClip(Clip clip) {

        if (clip != null) {

            try {
                clip.stop();
            } catch (Exception ignored) {
            }
        }
    }

    void closeGame() {

        gameRunning = false;

        if (gameTimer != null) {
            gameTimer.stop();
        }

        stopAllMusic();

        stopClip(introSound);
        stopClip(jumpSound);
        stopClip(coinSound);
        stopClip(extraSound);
        stopClip(hitSound);

        closeClip(introSound);
        closeClip(bgmStage1);
        closeClip(bgmStage2);
        closeClip(grandMusic);
        closeClip(paradigmMusic);
        closeClip(jumpSound);
        closeClip(coinSound);
        closeClip(extraSound);
        closeClip(hitSound);

        System.exit(0);
    }

    void closeClip(Clip clip) {

        if (clip != null) {

            try {
                clip.close();
            } catch (Exception ignored) {
            }
        }
    }

    // =========================================================
    // INNER CLASSES
    // =========================================================

    static class Pipe {

        int x;
        int gapY;
        boolean passed;

        Pipe(int x) {

            this.x = x;
            Random random = new Random();

            this.gapY = 200 + random.nextInt(SCREEN_HEIGHT - GROUND_HEIGHT - 400);
            this.passed = false;
        }
    }

    static class Meteor {

        double x;
        double y;
        int size;
        double speed;

        Meteor(double x, double y, int size, double speed) {

            this.x = x;
            this.y = y;
            this.size = size;
            this.speed = speed;
        }
    }

    static class Bullet {

        double x;
        double y;
        int width;
        int height;
        boolean isTopUI;
        boolean isGold;
        double damage;
        boolean isTopUIBeam;

        Bullet(double x, double y, boolean isTopUI, double damage, boolean isTopUIBeam) {

            this.x = x;
            this.y = y;
            this.isTopUI = isTopUI;
            this.isGold = !isTopUI && (damage == GOLD_BULLET_DAMAGE);
            this.damage = damage;
            this.isTopUIBeam = isTopUIBeam;

            if (isTopUIBeam) {
                this.width = 300;
                this.height = 40;
            } else if (isTopUI) {
                this.width = 32;
                this.height = 14;
            } else if (isGold) {
                this.width = 32;
                this.height = 14;
            } else {
                this.width = 25;
                this.height = 10;
            }
        }
    }

    static class BossBullet {

        double x;
        double y;
        double vx;
        double vy;
        int width;
        int height;
        boolean isDark = false;
        boolean isBeam = false;
        boolean isGiantBall = false;

        BossBullet(double x, double y, double vx, double vy, int width, int height, boolean isDark, boolean isGiantBall) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.width = width;
            this.height = height;
            this.isDark = isDark;
            this.isGiantBall = isGiantBall;
            this.isBeam = (width > height * 2);
        }
    }
}