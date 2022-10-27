package uet.oop.bomberman;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import uet.oop.bomberman.Input.InputHandler;
import uet.oop.bomberman.countdown.CountDown;
import uet.oop.bomberman.levels.LevelLoader;
import com.sun.javafx.perf.PerformanceTracker;
import uet.oop.bomberman.menu.Menu;
import uet.oop.bomberman.menu.StageMenu;
import uet.oop.bomberman.score.Score;
import uet.oop.bomberman.sound.Sound;

public class BombermanGame extends Application {
    private static int level = 1;
    private final LevelLoader lvLoad = new LevelLoader(level);
    static Group root = new Group();

    static Scene scene = new Scene(root);

    private final String TITLE = "BombermanGame";

    private final InputHandler _input = new InputHandler();

    private GraphicsContext gc;
    private Canvas canvas;

    private static PerformanceTracker tracker;

    private Menu menu = new Menu();

    private StageMenu stageMenu = new StageMenu();
    public static CountDown countdown = new CountDown(200);

    public static void main(String[] args) {
        Application.launch(BombermanGame.class);
    }

    @Override
    public void start(Stage stage) {
        Score.readHighScore();
        countdown.Restart();
        Sound.playBGM();
        stage.getIcons().add(new Image("/textures/icon.jfif"));
        // Tao Canvas
        canvas = new Canvas(LevelLoader.SCREEN_WIDTH, LevelLoader.SCREEN_HEIGHT);
        gc = canvas.getGraphicsContext2D();
        menu.setUpMainMenu(canvas.getWidth(), canvas.getHeight(), lvLoad);

        // Tao root container
        root.getChildren().addAll(canvas, menu);

        // Them scene vao stage
        stage.setScene(scene);
        stage.show();

        _input.prepareActionHandlers(scene);

        tracker = PerformanceTracker.getSceneTracker(scene);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!Board.BGMusic) {
                    Sound.mediaPlayer.pause();
                } else {
                    Sound.mediaPlayer.play();
                }
                if (lvLoad.board.levelOver) {
                    if (lvLoad.board.passLevel) {
                        menu.setStartGame(false);
                        level = menu.getCurrentStage();
                        level++;
                        lvLoad.introLevel.setShowIntro(true);
                        lvLoad.introLevel.resetTime();
                        menu = new Menu();
                        menu.unlockStage(level);
                        menu.setUpMainMenu(canvas.getWidth(), canvas.getHeight(), lvLoad);
                        stageMenu = new StageMenu();
                        stageMenu.setUpStageMenu(canvas.getWidth(), canvas.getHeight(), level - 1, root, menu, canvas);
                        root.getChildren().clear();
                        root.getChildren().addAll(canvas, stageMenu);
                        lvLoad.loadLevel(level);
                    } else {
                        lvLoad.loadLevel(level);
                        lvLoad.introLevel.setShowIntro(true);
                        lvLoad.introLevel.resetTime();
                    }
                }
                if (!lvLoad.board.bombers.isEmpty()) {
                    _input.handleInput(lvLoad.board.bombers.get(0), lvLoad.board, lvLoad);
                }
                if (menu.isStartGame()) {
                    root.getChildren().clear();
                    root.getChildren().add(canvas);
                    lvLoad.introLevel.show(gc, canvas.getWidth(), canvas.getHeight());
                    if (!lvLoad.introLevel.getShowIntro()) {
                        render();
                        update();
                    }
                }
                stage.setTitle(TITLE + "| " + (int) getFPS() + " rates"
                        + "| Timer" + countdown.timeLeftValue() + "s");
            }
        };
        timer.start();
    }

    public void stop() {
        Menu.writeStageStatus();
        Score.writeHighScore();
    }

    public void update() {
        lvLoad.board.updateAllEntity(lvLoad);
    }

    public void render() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        lvLoad.board.renderAllEntity(gc);
    }

    private float getFPS() {
        float fps = tracker.getAverageFPS();
        tracker.resetAverageFPS();
        return fps;
    }
}