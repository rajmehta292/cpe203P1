import processing.core.PApplet;
import processing.core.PImage;

import java.util.Optional;

public final class WorldView
{
    public PApplet screen;
    public WorldModel world;
    public int tileWidth;
    public int tileHeight;
    public Viewport viewport;

    public WorldView(
            int numRows,
            int numCols,
            PApplet screen,
            WorldModel world,
            int tileWidth,
            int tileHeight)
    {
        this.screen = screen;
        this.world = world;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.viewport = new Viewport(numRows, numCols);
    }

    public static int clamp(int value, int low, int high) {
        return Math.min(high, Math.max(value, low));
    }

    public void shiftView( int colDelta, int rowDelta) {
        int newCol = clamp(viewport.col + colDelta, 0,
                           world.numCols - viewport.numCols);
        int newRow = clamp(viewport.row + rowDelta, 0,
                           world.numRows - viewport.numRows);

        viewport.shift( newCol, newRow);
    }

    public  void drawBackground() {
        for (int row = 0; row < viewport.numRows; row++) {
            for (int col = 0; col < viewport.numCols; col++) {
                Point worldPoint = viewport.viewportToWorld( col, row);
                Optional<PImage> image =
                        Functions.getBackgroundImage(world, worldPoint);
                if (image.isPresent()) {
                    screen.image(image.get(), col * tileWidth,
                                      row * tileHeight);
                }
            }
        }
    }

    public  void drawViewport() {
        drawBackground();
        drawEntities();
    }

    public  void drawEntities() {
        for (Entity entity : world.entities) {
            Point pos = entity.position;

            if (viewport.contains( pos)) {
                Point viewPoint = viewport.worldToViewport( pos.x, pos.y);
                screen.image(Functions.getCurrentImage(entity),
                                  viewPoint.x * tileWidth,
                                  viewPoint.y * tileHeight);
            }
        }
    }
}
