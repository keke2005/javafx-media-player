
package org.example.playermedia;

import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.List;

public class HelloApplication extends Application {

    private MediaPlayer mediaPlayer;

    private final ObservableList<File> playlist =
            FXCollections.observableArrayList();

    private int currentIndex = -1;

    private final ListView<File> playlistView =
            new ListView<>(playlist);

    private final MediaView mediaView = new MediaView();

    private final Slider progressSlider = new Slider(0, 100, 0);

    private final Slider volumeSlider = new Slider(0, 1, 0.70);

    private final Label currentTimeLabel = new Label("00:00");
    private final Label totalTimeLabel = new Label("00:00");
    private final Label timeLabel = new Label("00:00 / 00:00");
    private final Label volumePercentLabel = new Label("70%");

    private final Button playPauseButton = new Button();
    private final Button stopButton = new Button();
    private final Button previousButton = new Button();
    private final Button nextButton = new Button();
    private final Button muteButton = new Button();
    private final Button fullscreenButton = new Button();
    private final Button centerPlayButton = new Button();
    private final Button togglePlaylistButton =
            new Button("Playlist");

    private final StackPane playerArea = new StackPane();
    private final BorderPane root = new BorderPane();

    private VBox sidePanel;
    private HBox topBar;
    private HBox bottomBar;

    private StackPane audioVisualizer;

    private RotateTransition rotateTransition;
    private PauseTransition volumeHideTimer;

    private boolean sliderChanging = false;
    private boolean muted = false;
    private boolean isPlaylistVisible = false;

    private double previousVolume = 0.70;
    private int retryCount = 0;


    @Override
    public void start(Stage stage) {

        stage.setTitle("Media Player");
        stage.setMinWidth(900);
        stage.setMinHeight(650);

        buildTopBar(stage);
        buildPlayerArea(stage);
        buildSidePanel(stage);
        buildBottomBar(stage);

        root.setTop(topBar);
        root.setCenter(playerArea);
        root.setBottom(bottomBar);
        root.setStyle("-fx-background-color: #2c2c2c;");
        Scene scene = new Scene(root, 1250, 760);
        var cssUrl = getClass().getResource("/style.css");

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        root.setFocusTraversable(true);
        installKeyboardControls(scene, stage);
        installWindowShortcuts(stage);
        stage.setScene(scene);
        stage.show();
        Platform.runLater(() -> root.requestFocus());
    }

    // Top bar
    private void buildTopBar(Stage stage) {

        topBar = new HBox(15);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(12, 20, 12, 20));

        topBar.setStyle("-fx-background-color: #1a1a1a;" + "-fx-border-color: #333;" + "-fx-border-width: 0 0 1 0;"
        );

        Button openFileButton = new Button("Open File");

        openFileButton.setStyle("-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
        );

        openFileButton.setOnAction(e -> {
            addFiles(stage);
            restoreKeyboardFocus();
        });

        openFileButton.setOnMouseEntered(e ->
                openFileButton.setStyle("-fx-background-color: #d6a84f;" + "-fx-text-fill: #151515;" + "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
                )
        );

        openFileButton.setOnMouseExited(e ->
                openFileButton.setStyle("-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
                )
        );

        Button shortcutsButton = new Button("Shortcuts");

        shortcutsButton.setStyle("-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
        );

        shortcutsButton.setOnAction(e -> showShortcutsDialog());

        shortcutsButton.setOnMouseEntered(e ->
                shortcutsButton.setStyle("-fx-background-color: #d6a84f;" + "-fx-text-fill: #151515;" + "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
                )
        );

        shortcutsButton.setOnMouseExited(e ->
                shortcutsButton.setStyle("-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
                )
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        togglePlaylistButton.setStyle("-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-font-weight: bold;" + "-fx-font-size: 14px;" +
                        "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
        );

        togglePlaylistButton.setOnMouseEntered(e ->
                togglePlaylistButton.setStyle("-fx-background-color: #d6a84f;" + "-fx-text-fill: #151515;" + "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
                )
        );

        togglePlaylistButton.setOnMouseExited(e ->
                togglePlaylistButton.setStyle("-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-font-weight: bold;" +
                                "-fx-font-size: 14px;" + "-fx-padding: 8 16;" + "-fx-background-radius: 4;" + "-fx-cursor: hand;"
                )
        );

        togglePlaylistButton.setOnAction(e -> {
            isPlaylistVisible = !isPlaylistVisible;

            if (isPlaylistVisible) {
                root.setRight(sidePanel);
            } else {
                root.setRight(null);
            }

            restoreKeyboardFocus();
        });

        topBar.getChildren().addAll(openFileButton, shortcutsButton, spacer, togglePlaylistButton
        );
    }

    // Shortcut board
    private void showShortcutsDialog() {

        Dialog<Void> dialog = new Dialog<>();

        dialog.setTitle("Keyboard Shortcuts");
        dialog.setHeaderText(null);
        DialogPane pane = dialog.getDialogPane();

        pane.setStyle("-fx-background-color: #1e1e1e;" + "-fx-border-color: #d6a84f;" + "-fx-border-width: 1;"
        );

        VBox main = new VBox(18);
        main.setPadding(new Insets(20));
        main.setPrefWidth(560);
        main.setStyle("-fx-background-color: #1e1e1e;");

        Label title = new Label("KEYBOARD SHORTCUTS");

        title.setStyle("-fx-text-fill: #d6a84f;" + "-fx-font-size: 22px;" + "-fx-font-weight: bold;"
        );

        Label subtitle = new Label(
                "Control your media player using your keyboard"
        );

        subtitle.setStyle(
                "-fx-text-fill: #aaaaaa;" +
                        "-fx-font-size: 12px;"
        );

        VBox heading = new VBox(5, title, subtitle);

        VBox playback = createShortcutSection(
                "PLAYBACK",
                new String[][]{
                        {"SPACE", "Play / Pause"},
                        {"S", "Stop playback"},
                        {"N", "Play next media"},
                        {"P", "Play previous media"}
                }
        );

        VBox volume = createShortcutSection(
                "VOLUME",
                new String[][]{
                        {"UP", "Increase volume by 10%"},
                        {"DOWN", "Decrease volume by 10%"},
                        {"M", "Mute / Unmute"}
                }
        );

        VBox files = createShortcutSection(
                "FILES & WINDOW",
                new String[][]{
                        {"O", "Open media files"},
                        {"F11", "Toggle fullscreen"}
                }
        );

        main.getChildren().addAll(heading, playback, volume, files
        );

        ScrollPane scrollPane = new ScrollPane(main);

        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(440);
        scrollPane.setStyle("-fx-background: #1e1e1e;" + "-fx-background-color: #1e1e1e;" + "-fx-border-color: transparent;"
        );

        pane.setContent(scrollPane);
        pane.getButtonTypes().add(ButtonType.CLOSE);

        Button closeButton = (Button)
                pane.lookupButton(ButtonType.CLOSE);

        closeButton.setStyle("-fx-background-color: #d6a84f;" + "-fx-text-fill: #151515;" + "-fx-font-weight: bold;" + "-fx-padding: 8 25;" + "-fx-cursor: hand;"
        );

        dialog.setOnHidden(e -> restoreKeyboardFocus());
        dialog.showAndWait();
    }

    // shortcut sections
    private VBox createShortcutSection(
            String sectionTitle, String[][] shortcuts) {

        VBox section = new VBox(8);

        section.setPadding(new Insets(12));

        section.setStyle("-fx-background-color: #292929;" + "-fx-background-radius: 8;" + "-fx-border-color: #3a3a3a;" + "-fx-border-radius: 8;"
        );

        Label heading = new Label(sectionTitle);
        heading.setStyle("-fx-text-fill: #d6a84f;" + "-fx-font-size: 12px;" + "-fx-font-weight: bold;"
        );

        VBox rows = new VBox(8);

        for (String[] shortcut : shortcuts) {
            Label key = new Label(shortcut[0]);
            key.setAlignment(Pos.CENTER);
            key.setMinWidth(75);
            key.setPadding(new Insets(6, 8, 6, 8));

            key.setStyle("-fx-background-color: #3a3a3a;" + "-fx-text-fill: #d6a84f;" + "-fx-font-weight: bold;" + "-fx-background-radius: 5;" + "-fx-border-color: #555555;" + "-fx-border-radius: 5;"
            );

            Label description = new Label(shortcut[1]);

            description.setStyle("-fx-text-fill: #eeeeee;" + "-fx-font-size: 13px;"
            );

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox row = new HBox(12, key, description, spacer
            );

            row.setAlignment(Pos.CENTER_LEFT);
            rows.getChildren().add(row);
        }

        section.getChildren().addAll(heading, rows);
        return section;
    }

    // Player area
    private void buildPlayerArea(Stage stage) {

        playerArea.setStyle("-fx-background-color: #2c2c2c;");
        playerArea.setMinSize(400, 300);

        StackPane videoHolder = new StackPane();
        videoHolder.setStyle("-fx-background-color: #1e1e1e;" + "-fx-background-radius: 8;"
        );

        mediaView.setPreserveRatio(true);

        mediaView.fitWidthProperty().bind(
                videoHolder.widthProperty()
        );

        mediaView.fitHeightProperty().bind(videoHolder.heightProperty()
        );

        audioVisualizer = new StackPane();
        audioVisualizer.setMaxSize(220, 220);
        audioVisualizer.setVisible(false);

        Circle outerCircle = new Circle(100);
        outerCircle.setFill(Color.web("#1e1e1e"));
        outerCircle.setStroke(Color.web("#d6a84f"));
        outerCircle.setStrokeWidth(4);

        Circle middleRing = new Circle(70);
        middleRing.setFill(Color.web("#111111"));
        middleRing.setStroke(Color.web("#333333"));
        middleRing.setStrokeWidth(1);

        Circle innerCircle = new Circle(30);
        innerCircle.setFill(Color.web("#d6a84f"));

        Label noteLabel = new Label("♫");

        noteLabel.setStyle("-fx-text-fill: #151515;" + "-fx-font-size: 28px;" + "-fx-font-weight: bold;"
        );

        audioVisualizer.getChildren().addAll(outerCircle, middleRing, innerCircle, noteLabel
        );

        rotateTransition = new RotateTransition(Duration.seconds(4), audioVisualizer
        );

        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(RotateTransition.INDEFINITE
        );

        rotateTransition.setInterpolator(
                javafx.animation.Interpolator.LINEAR
        );

        centerPlayButton.setStyle("-fx-background-color: rgba(0,0,0,0.6);" + "-fx-background-radius: 50;" + "-fx-min-width: 80;" +
                        "-fx-min-height: 80;" + "-fx-max-width: 80;" + "-fx-max-height: 80;" +
                        "-fx-border-color: #d6a84f;" + "-fx-border-width: 3;" + "-fx-border-radius: 50;"
        );

        try {

            ImageView playIcon = new ImageView(
                    new Image(getClass().getResourceAsStream("/icons/play.png")
                    )
            );

            playIcon.setFitWidth(40);
            playIcon.setFitHeight(40);
            playIcon.setPreserveRatio(true);

            centerPlayButton.setGraphic(playIcon);

        } catch (Exception e) {

            centerPlayButton.setText("▶");

            centerPlayButton.setStyle("-fx-font-size: 30px;" + "-fx-text-fill: #d6a84f;" + "-fx-background-color: rgba(0,0,0,0.6);" + "-fx-background-radius: 50;" +
                            "-fx-min-width: 80;" + "-fx-min-height: 80;" + "-fx-border-color: #d6a84f;" +
                            "-fx-border-width: 3;" + "-fx-border-radius: 50;"
            );
        }

        centerPlayButton.setOnAction(e -> {

            if (mediaPlayer == null) {
                addFiles(stage);
            } else {
                togglePlayPause();
            }
        });

        videoHolder.getChildren().addAll(mediaView, audioVisualizer, centerPlayButton
        );

        playerArea.getChildren().add(videoHolder);

        StackPane.setMargin(videoHolder,
                new Insets(15)
        );
    }

    // Playlist
    private void buildSidePanel(Stage stage) {

        sidePanel = new VBox(12);

        sidePanel.setPrefWidth(280);
        sidePanel.setMinWidth(220);
        sidePanel.setPadding(new Insets(14));

        sidePanel.setStyle("-fx-background-color: #1a1a1a;" + "-fx-border-color: #333;" + "-fx-border-width: 0 0 0 1;"
        );

        Label playlistTitle = new Label("PLAYLIST");

        playlistTitle.setStyle("-fx-text-fill: #d6a84f;" + "-fx-font-size: 14px;" + "-fx-font-weight: bold;"
        );

        Label playlistCount = new Label();

        playlistCount.setStyle("-fx-text-fill: #888;");

        playlistCount.textProperty().bind(
                Bindings.size(playlist).asString("(%d)")
        );

        Region headingSpacer = new Region();

        HBox heading = new HBox(8, playlistTitle, headingSpacer, playlistCount
        );

        HBox.setHgrow(headingSpacer, Priority.ALWAYS);

        playlistView.setPlaceholder(
                new Label("No media files added")
        );

        playlistView.setStyle("-fx-background-color: #1a1a1a;" + "-fx-control-inner-background: #1a1a1a;"
        );

        playlistView.setCellFactory(list ->
                new ListCell<>() {

                    @Override
                    protected void updateItem(
                            File file, boolean empty) {

                        super.updateItem(file, empty);

                        if (empty || file == null) {
                            setText(null);
                            setGraphic(null);

                            setStyle("-fx-background-color: #1a1a1a;"
                            );

                        } else {

                            setText(file.getName());

                            setStyle("-fx-background-color: #2a2a2a;" + "-fx-text-fill: #d6a84f;" + "-fx-font-size: 13px;" + "-fx-padding: 10;" + "-fx-border-color: #333;" + "-fx-border-width: 0 0 1 0;"
                            );
                        }
                    }
                }
        );

        playlistView.setOnMouseClicked(e -> {

            if (e.getClickCount() == 2) {
                playSelected();
            }

            restoreKeyboardFocus();
        });

        playlistView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldFile, newFile) -> {
                    if (newFile != null) {
                        int index = playlist.indexOf(newFile);

                        if (index >= 0) {
                            loadMedia(index, true);
                        }
                    }
                });

        Button add = new Button("+ Add");

        add.setStyle("-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-cursor: hand;"
        );

        add.setOnAction(e -> {
            addFiles(stage);
            restoreKeyboardFocus();
        });

        Button remove = new Button("Remove");

        remove.setStyle("-fx-background-color: #582c2c;" + "-fx-text-fill: white;" + "-fx-cursor: hand;"
        );

        remove.setOnAction(e -> {
            removeSelected();
            restoreKeyboardFocus();
        });

        HBox playlistButtons = new HBox(8, add, remove);
        VBox.setVgrow(playlistView, Priority.ALWAYS);
        sidePanel.getChildren().addAll(heading, playlistView, playlistButtons);
    }

    // Bottom bar
    private void buildBottomBar(Stage stage) {
        bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10, 20, 10, 20));
        bottomBar.setStyle("-fx-background-color: #1a1a1a;" + "-fx-border-color: #333;" + "-fx-border-width: 1 0 0 0;"
        );

        bottomBar.setMinHeight(60);
        bottomBar.setPrefHeight(60);

        progressSlider.setPrefWidth(200);

        HBox.setHgrow(progressSlider, Priority.ALWAYS);

        styleControl(previousButton);
        styleControl(playPauseButton);
        styleControl(stopButton);
        styleControl(nextButton);
        styleControl(muteButton);
        styleControl(fullscreenButton);

        setIcon(previousButton, "previous", 18);
        setIcon(playPauseButton, "play", 20);
        setIcon(stopButton, "stop", 16);
        setIcon(nextButton, "next", 18);
        setIcon(muteButton, "volume", 18);
        setIcon(fullscreenButton, "fullscreen", 18);

        previousButton.setOnAction(e -> {
            previous();
            restoreKeyboardFocus();
        });

        playPauseButton.setOnAction(e -> {
            togglePlayPause();
            restoreKeyboardFocus();
        });

        stopButton.setOnAction(e -> {
            stopMedia();
            restoreKeyboardFocus();
        });

        nextButton.setOnAction(e -> {
            next();
            restoreKeyboardFocus();
        });

        muteButton.setOnAction(e -> {
            toggleMute();
            restoreKeyboardFocus();
        });

        fullscreenButton.setOnAction(e ->
                stage.setFullScreen(!stage.isFullScreen())
        );

        timeLabel.setStyle("-fx-text-fill: #d6a84f;" + "-fx-font-size: 13px;" + "-fx-font-weight: bold;"
        );

        timeLabel.textProperty().bind(
                Bindings.concat(
                        currentTimeLabel.textProperty(),
                        " / ",
                        totalTimeLabel.textProperty()
                )
        );

        volumeSlider.setPrefWidth(80);

        volumePercentLabel.setStyle("-fx-text-fill: #d6a84f;" + "-fx-font-size: 12px;" + "-fx-font-weight: bold;" + "-fx-min-width: 35;"
        );

        volumePercentLabel.setVisible(false);

        volumeHideTimer = new PauseTransition(
                Duration.seconds(1.5)
        );

        volumeHideTimer.setOnFinished(e ->
                volumePercentLabel.setVisible(false)
        );

        volumeSlider.valueProperty().addListener(
                (obs, oldValue, newValue) -> {

                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(
                                newValue.doubleValue()
                        );
                    }

                    if (newValue.doubleValue() > 0) {
                        previousVolume = newValue.doubleValue();
                        muted = false;
                        setIcon(muteButton, "volume", 18);
                    }

                    volumePercentLabel.setText(
                            String.format(
                                    "%d%%",
                                    (int) (
                                            newValue.doubleValue() * 100
                                    )
                            )
                    );

                    volumePercentLabel.setVisible(true);
                    volumeHideTimer.playFromStart();
                }
        );

        HBox mainControls = new HBox(5, previousButton, playPauseButton, stopButton, nextButton
        );

        mainControls.setAlignment(Pos.CENTER);

        HBox volumeBox = new HBox(5, muteButton, volumeSlider, volumePercentLabel
        );

        volumeBox.setAlignment(Pos.CENTER);

        bottomBar.getChildren().addAll(mainControls, progressSlider, timeLabel, volumeBox,
                fullscreenButton
        );

        HBox.setHgrow(progressSlider, Priority.ALWAYS);

        progressSlider.setOnMousePressed(e ->
                sliderChanging = true
        );

        progressSlider.setOnMouseReleased(e -> {
            seekFromSlider();
            sliderChanging = false;
        });

        progressSlider.valueChangingProperty().addListener(
                (obs, wasChanging, isChanging) -> {
                    sliderChanging = isChanging;
                    if (!isChanging) {
                        seekFromSlider();
                    }
                }
        );
    }

    // Add files
    private void addFiles(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Audio / Video Files");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Media Files", "*.mp3", "*.wav", "*.m4a", "*.aac", "*.aiff", "*.mp4", "*.m4v"
                )
        );

        List<File> files = chooser.showOpenMultipleDialog(
                stage != null
                        ? stage
                        : playerArea.getScene().getWindow()
        );

        if (files != null) {
            for (File file : files) {
                if (!playlist.contains(file)) {
                    playlist.add(file);
                }
            }

            if (currentIndex == -1 && !playlist.isEmpty()) {
                playlistView.getSelectionModel().select(0);
            }
        }
    }

    // Remove file
    private void removeSelected() {
        int selected =
                playlistView.getSelectionModel().getSelectedIndex();
        if (selected < 0) {
            return;
        }

        boolean removingCurrent = selected == currentIndex;
        playlist.remove(selected);

        if (playlist.isEmpty()) {
            currentIndex = -1;
            releasePlayer();
            currentTimeLabel.setText("00:00");
            totalTimeLabel.setText("00:00");
            progressSlider.setValue(0);
            return;
        }
        if (selected < currentIndex) {
            currentIndex--;
        }
        if (removingCurrent) {
            loadMedia(
                    Math.min(selected, playlist.size() - 1),
                    false
            );
        } else {
            playlistView.getSelectionModel().select(
                    Math.min(selected, playlist.size() - 1)
            );
        }
    }

    // Play selected file
    private void playSelected() {
        int selected =
                playlistView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            if (selected != currentIndex) {
                loadMedia(selected, true);
            } else {
                togglePlayPause();
            }
        }
    }

    // Load media
    private void loadMedia(int index, boolean autoPlay) {
        if (index < 0 || index >= playlist.size()) {
            return;
        }

        currentIndex = index;
        File file = playlist.get(index);

        releasePlayer();

        try {
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaView.setMediaPlayer(mediaPlayer);
            boolean isVideo = isVideoFile(file);
            mediaView.setVisible(isVideo);
            audioVisualizer.setVisible(!isVideo);
            mediaPlayer.setVolume(
                    muted ? 0 : volumeSlider.getValue()
            );

            mediaPlayer.setOnReady(() -> {
                Duration total = media.getDuration();
                progressSlider.setDisable(false);
                totalTimeLabel.setText(formatTime(total));
                retryCount = 0;

                if (autoPlay) {
                    mediaPlayer.play();
                    setIcon(playPauseButton, "pause", 20);
                }
            });

            mediaPlayer.currentTimeProperty().addListener(
                    (obs, oldTime, newTime) -> {
                        if (!sliderChanging && mediaPlayer != null) {
                            Duration total =
                                    mediaPlayer.getTotalDuration();
                            if (total != null &&
                                    total.greaterThan(Duration.ZERO)) {
                                progressSlider.setValue(
                                        newTime.toMillis()
                                                / total.toMillis()
                                                * 100.0
                                );
                            }
                        }

                        currentTimeLabel.setText(
                                formatTime(newTime)
                        );
                    }
            );

            mediaPlayer.setOnPlaying(() -> {
                setIcon(playPauseButton, "pause", 20);
                centerPlayButton.setVisible(false);
                if (audioVisualizer.isVisible()) {
                    rotateTransition.play();
                }
            });

            mediaPlayer.setOnPaused(() -> {
                setIcon(playPauseButton, "play", 20);
                centerPlayButton.setVisible(true);
                rotateTransition.pause();
            });

            mediaPlayer.setOnStopped(() -> {
                setIcon(playPauseButton, "play", 20);
                centerPlayButton.setVisible(true);
                rotateTransition.stop();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                setIcon(playPauseButton, "play", 20);
                centerPlayButton.setVisible(true);
                rotateTransition.stop();
                if (currentIndex < playlist.size() - 1) {
                    next();
                }
            });

            mediaPlayer.setOnError(() -> {
                Throwable err = mediaPlayer.getError();
                if (err != null) {
                    err.printStackTrace();
                }

                if (retryCount < 3 && currentIndex != -1) {
                    retryCount++;
                    int retryIndex = currentIndex;
                    boolean retryPlay = autoPlay;
                    Platform.runLater(() -> {
                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException ignored) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        Platform.runLater(() ->
                                loadMedia(retryIndex, retryPlay)
                        );
                    });
                }
            });

            playlistView.getSelectionModel().select(index);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isVideoFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp4")
                || name.endsWith(".m4v")
                || name.endsWith(".fxm")
                || name.endsWith(".flv")
                || name.endsWith(".webm");
    }

    // Play and pause
    private void togglePlayPause() {
        if (mediaPlayer == null) {
            if (!playlist.isEmpty()) {
                loadMedia(0, true);
            }
            return;
        }

        switch (mediaPlayer.getStatus()) {
            case PLAYING:
                mediaPlayer.pause();
                break;
            case PAUSED:
            case READY:
            case STOPPED:
                mediaPlayer.play();
                break;
            default:
                break;
        }
    }

    // Stop
    private void stopMedia() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            progressSlider.setValue(0);
            currentTimeLabel.setText("00:00");
        }
    }

    // Next
    private void next() {
        if (playlist.isEmpty()) {
            return;
        }
        int index = currentIndex + 1;
        if (index >= playlist.size()) {
            index = 0;
        }

        loadMedia(index, true);
    }

    // Previous
    private void previous() {
        if (playlist.isEmpty()) {
            return;
        }
        int index = currentIndex - 1;
        if (index < 0) {
            index = playlist.size() - 1;
        }

        loadMedia(index, true);
    }

    // mute and unmute
    private void toggleMute() {
        if (mediaPlayer == null) {
            return;
        }

        if (!muted) {
            previousVolume = volumeSlider.getValue();
            volumeSlider.setValue(0);
            mediaPlayer.setVolume(0);
            setIcon(muteButton, "mute", 18);
            muted = true;

        } else {
            double restore = previousVolume <= 0 ? 0.70 : previousVolume;
            volumeSlider.setValue(restore);
            mediaPlayer.setVolume(restore);
            setIcon(muteButton, "volume", 18);
            muted = false;
        }
    }


    private void seekFromSlider() {
        if (mediaPlayer == null) {
            return;
        }
        Duration total = mediaPlayer.getTotalDuration();
        if (total != null && total.greaterThan(Duration.ZERO)) {
            mediaPlayer.seek(
                    total.multiply(progressSlider.getValue() / 100.0)
            );
        }
    }

    // change volume
    private void changeVolume(double amount) {
        double volume = Math.max(0, Math.min(1, volumeSlider.getValue() + amount));

        volumeSlider.setValue(volume);

        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }

        if (volume > 0) {
            muted = false;
            setIcon(muteButton, "volume", 18);

        } else {
            muted = true;
            setIcon(muteButton, "mute", 18);
        }
    }


    private String formatTime(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return "00:00";
        }
        int total = (int) Math.floor(duration.toSeconds());
        int hours = total / 3600;
        int minutes = (total % 3600) / 60;
        int seconds = total % 60;

        if (hours > 0) {

            return String.format("%02d:%02d:%02d", hours, minutes, seconds
            );
        }

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            mediaView.setMediaPlayer(null);
            centerPlayButton.setVisible(true);
            rotateTransition.stop();
        }
    }

    // Keyboard controls
    private void installKeyboardControls(
            Scene scene, Stage stage) {

        scene.addEventFilter(
                KeyEvent.KEY_PRESSED,
                event -> {
                    switch (event.getCode()) {
                        case SPACE:
                            togglePlayPause();
                            event.consume();
                            break;
                        case S:
                            stopMedia();
                            event.consume();
                            break;
                        case N:
                            next();
                            event.consume();
                            break;
                        case P:
                            previous();
                            event.consume();
                            break;
                        case UP:
                            changeVolume(0.10);
                            event.consume();
                            break;
                        case DOWN:
                            changeVolume(-0.10);
                            event.consume();
                            break;
                        case M:
                            toggleMute();
                            event.consume();
                            break;
                        case O:
                            addFiles(stage);
                            event.consume();
                            break;
                        case F11:
                            stage.setFullScreen(
                                    !stage.isFullScreen()
                            );
                            event.consume();
                            break;
                        default:
                            break;
                    }
                }
        );
    }

    private void restoreKeyboardFocus() {

        Platform.runLater(() -> {

            if (root != null) {
                root.requestFocus();
            }
        });
    }

    // Window shortcut
    private void installWindowShortcuts(Stage stage) {
        stage.setOnCloseRequest(e -> releasePlayer());
    }

    private void styleControl(Button button) {

        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-cursor: hand;"
        );

        button.setOnMouseEntered(e ->
                button.setStyle(
                        "-fx-background-color: #333;" + "-fx-text-fill: #d6a84f;" + "-fx-font-size: 16px;" +
                                "-fx-padding: 5 10;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 4;"
                )
        );

        button.setOnMouseExited(e ->
                button.setStyle("-fx-background-color: transparent;" + "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-padding: 5 10;" +
                                "-fx-cursor: hand;"
                )
        );
    }

    // Load icons
    private ImageView icon(String name, int size) {

        try {
            Image image = new Image(
                    getClass().getResourceAsStream("/icons/" + name + ".png")
            );

            if (image.isError() || image.getWidth() == 0) {
                return null;
            }

            ImageView view = new ImageView(image);

            view.setFitWidth(size);
            view.setFitHeight(size);
            view.setPreserveRatio(true);

            return view;

        } catch (Exception e) {
            return null;
        }
    }


    private void setIcon(Button button, String name, int size) {
        ImageView imageView = icon(name, size);

        if (imageView == null) {

            String symbol;

            switch (name) {

                case "previous":
                    symbol = "⏮";
                    break;

                case "play":
                    symbol = "▶";
                    break;

                case "pause":
                    symbol = "⏸";
                    break;

                case "stop":
                    symbol = "⏹";
                    break;

                case "next":
                    symbol = "⏭";
                    break;

                case "volume":
                    symbol = "🔊";
                    break;

                case "mute":
                    symbol = "🔇";
                    break;

                case "fullscreen":
                    symbol = "⛶";
                    break;

                default:
                    symbol = "?";
                    break;
            }

            button.setGraphic(null);
            button.setText(symbol);

        } else {

            button.setText("");
            button.setGraphic(imageView);
        }
        button.setAccessibleText(name);
    }

    @Override
    public void stop() {

        if (volumeHideTimer != null) {
            volumeHideTimer.stop();
        }
        releasePlayer();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
