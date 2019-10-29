public final class Viewport
{
    public int row;
    public int col;
    public int numRows;
    public int numCols;

    public Viewport(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
    }

    public  void shift( int col, int row) {
        col = col;
        row = row;
    }

    public  boolean contains( Point p) {
        return p.y >= row && p.y < row + numRows
                && p.x >= col && p.x < col + numCols;
    }

    public Point viewportToWorld( int col, int row) {
        return new Point(col + this.col, row + this.row);
    }

    public Point worldToViewport( int col, int row) {
        return new Point(col - col, row - row);
    }
}
