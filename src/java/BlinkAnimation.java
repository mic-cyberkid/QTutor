
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class BlinkAnimation {
    private static BlinkAnimation instance;
    private Timeline blinkTimeline;

    public static BlinkAnimation getInstance() {
        if (instance == null) {
            instance = new BlinkAnimation();
        }
        return instance;
    }

    public void startBlink(Node node) {
        if (blinkTimeline != null) {
            blinkTimeline.play();
        } else if (blinkTimeline == null) {
            createTimeline(node);
            blinkTimeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
                node.setVisible(!node.isVisible());
            }));
            blinkTimeline.setCycleCount(Timeline.INDEFINITE);
            blinkTimeline.play();
        }
    }

    public void stopBlink() {
        // Change send button icon to stop icon and add blinking effect
        if (blinkTimeline != null) {
            blinkTimeline.stop();
            blinkTimeline = null;
        }
    }

    private void createTimeline(Node node) {
        blinkTimeline = new Timeline(new KeyFrame(Duration.millis(1000), e -> {
            node.setVisible(!node.isVisible());
        }));
        blinkTimeline.setCycleCount(Timeline.INDEFINITE);
        
    }
    
    private BlinkAnimation() {}
}