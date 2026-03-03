package svm;

import java.awt.*;

public class ScreenUtils {
  private static final double BASELINE_WIDTH = 1920.0;

  public static double getScaleFactor() {
    final int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
    return screenWidth / BASELINE_WIDTH;
  }

  public static int getFontSize(int baseSize) {
    final double scaleFactor = getScaleFactor();
    final int fontSize = (int) Math.round(baseSize * scaleFactor);
    return Math.max(fontSize, baseSize);
  }

  public static int getScaledComponentSize(int baseValue) {
    return (int) Math.round(baseValue * getScaleFactor());
  }

  public static Dimension getScaledComponentSize(int baseWidth, int baseHeight) {
    final double scaleFactor = getScaleFactor();
    return new Dimension(
        (int) Math.round(baseWidth * scaleFactor),
        (int) Math.round(baseHeight * scaleFactor)
    );
  }

  public static Dimension getMinimumSize(int baseWidth, int baseHeight) {
    final double scaleFactor = getScaleFactor();
    final int width = (int) Math.round(baseWidth * scaleFactor);
    final int height = (int) Math.round(baseHeight * scaleFactor);

    return new Dimension(width, height);
  }

  public static Point getScreenCenter(Component component) {
    final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension comp = component.getSize();
    final int x = (screen.width - comp.width) / 2;
    final int y = (screen.height - comp.height) / 2;

    return new Point(Math.max(0, x), Math.max(0, y));
  }
}