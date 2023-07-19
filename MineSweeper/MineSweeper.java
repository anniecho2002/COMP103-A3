/* Code for COMP103 - 2021T2, Assignment 3
 * Name: Annie Cho
 * Username: choanni
 * ID: 300575457
 */

import ecs100.*;
import java.awt.Color;
import javax.swing.JButton;

/**
 *  Simple 'Minesweeper' program.
 *  There is a grid of squares, some of which contain a mine.
 *  
 *  The user can click on a square to either expose it or to
 *  mark/unmark it.
 *  
 *  If the user exposes a square with a mine, they lose.
 *  Otherwise, it is uncovered, and shows a number which represents the
 *  number of mines in the eight squares surrounding that one.
 *  If there are no mines adjacent to it, then all the unexposed squares
 *  immediately adjacent to it are exposed (and so on)
 *
 *  If the user marks a square, then they cannot expose the square,
 *  (unless they unmark it first)
 *  When all the squares without mines are exposed, the user has won.
 */
public class MineSweeper {

    public static final int ROWS = 15;
    public static final int COLS = 15;

    public static final double LEFT = 10; 
    public static final double TOP = 10;
    public static final double SQUARE_SIZE = 20;

    // Fields
    private boolean marking;

    private Square[][] squares;
    private int[][] board; // board that we can see
    private int[][] knowledge = new int[ROWS][COLS]; // board that we know

    private JButton mrkButton;
    private JButton expButton;
    Color defaultColor;

    /** 
     * Construct a new MineSweeper object
     * and set up the GUI
     */
    public static void main(String[] arguments){
        MineSweeper ms = new MineSweeper();
        ms.setupGUI();
        ms.setMarking(false);
        ms.makeGrid();
    }

    /** Set up the GUI: buttons and mouse to play the game */
    public void setupGUI(){
        UI.setMouseListener(this::doMouse);
        UI.addButton("New Game", this::makeGrid);
        this.expButton = UI.addButton("Expose", ()->setMarking(false));
        this.mrkButton = UI.addButton("Mark", ()->setMarking(true));

        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.0);
    }

    /** Respond to mouse events */
    public void doMouse(String action, double x, double y) {
        if (action.equals("released")){
            int row = (int)((y-TOP)/SQUARE_SIZE);
            int col = (int)((x-LEFT)/SQUARE_SIZE);
            if (row>=0 && row < ROWS && col >= 0 && col < COLS){
                if (marking) { mark(row, col);}
                else         { tryExpose(row, col); }
            }
        }
    }


    // Other Methods

    /** 
     * The player has clicked on a square to expose it
     * - if it is already exposed or marked, do nothing.
     * - if it's a mine: lose (call drawLose()) 
     * - otherwise expose it (call exposeSquareAt)
     * then check to see if the player has won and call drawWon() if they have.
     * (This method is not recursive)
     */
    public void tryExpose(int row, int col){
        int check = 0;
        if (squares[row][col].isExposed()){
            UI.println("Mine has already been exposed.");
        }
        else if(squares[row][col].hasMine()){
            drawLose();
            check = 1;
        }
        else{
            exposeSquareAt(row, col);
        }
        if (hasWon() && check == 0){
            drawWin();
        }
    }

    /** 
     *  Ensures that the square at row and col is exposed.
     *  If it is already exposed, do nothing.
     *  Otherwise,
     *    Expose it and redraw it.
     *    If the number of adjacent mines of this square is 0, then none of
     *      its neighbours have mines, so
     *      expose all its eight neighbours 
     *      (and if they have no adjacent mines, expose their neighbours, and ....)
     *      (be careful not to go over the edges of the map)
     *      Recursion is functions that call itself
     */
    public void exposeSquareAt(int row, int col){
        if (!squares[row][col].isExposed()){
            squares[row][col].setExposed(); // exposes the square
            squares[row][col].draw(row, col);
            if (squares[row][col].getAdjacentMines() == 0){
                if(row < ROWS - 1){exposeSquareAt(row+1, col);} // bottom
                if(row > 0){exposeSquareAt(row-1, col);}        // above
                if(col < COLS - 1){exposeSquareAt(row, col+1);} // right
                if(col > 0){exposeSquareAt(row, col-1);}        // left
                if(row != 0 && col !=0){exposeSquareAt(row-1, col-1);}                 // top left
                if(row != ROWS - 1 && col != 0){exposeSquareAt(row+1, col-1);}         // bottom left
                if(row != 0 && col != COLS - 1){exposeSquareAt(row-1, col+1);}         // top right
                if(row != ROWS - 1 && col != COLS - 1) {exposeSquareAt(row+1, col+1);} // bottom right
            }
            /*AIHelper();
            for (int i=0; i<knowledge.length; i++){
                UI.println(knowledge[i]);
            } */
        }
    }

    /** 
     * Returns true if the player has won:
     * If any square without a mine is not exposed, then the player has not won yet.
     * If all the squares without a mine have been exposed, then the player has won.
     * (It doesn't matter if the squares with a mine have been marked or not).
     */
    public boolean hasWon(){
        for (int i=0; i<ROWS; i++){
            for (int j=0; j<COLS; j++){
                if (!squares[i][j].hasMine() && !squares[i][j].isExposed()){
                    return false; // if there is any mine-less square that hasn't been exposed yet
                }
            }
        }
        return true;
    }
    
    /**
     * Check every square to see what's up
     * If it has been exposed then 
     * 
     * 
     * -1 for hidden squares
     * 0 for empty exposed square
     * 1-8 for exposed square that has adjacent mines
     * Work out which hidden squares are safe to expose and which have mines/should be marked
     */
    public void AIHelper(){
        UI.clearText();
        board = getVisibleState();
        for (int i=0; i<ROWS; i++){
            for (int j=0; j<COLS; j++){
                // goes through every square in the board
                checkEmpty(i, j);
            }
        }
    }
    
    /**
     * 0 is safe (0 probability)
     * 1 is a mine (1 probability)
     */
    public void checkEmpty(int row, int col){
        if(board[row][col] == 0){ // if we have an empty exposed square
            knowledge[row][col] = 0;
            if(row> 0 && row < ROWS -1 && col > 0 && col < COLS - 1){
                knowledge[row-1][col-1] = 0;
                knowledge[row-1][col] = 0;
                knowledge[row-1][col+1] = 0;
                knowledge[row][col-1] = 0;
                knowledge[row][col+1] = 0;
                knowledge[row+1][col-1] = 0;
                knowledge[row+1][col] = 0;
                knowledge[row+1][col+1] = 0;
            }
        }
        else if(board[row][col] == -1){   // if it is hidden and we don't know what it is
             
            

        }
        else{ // if it is an exposed square that has adjacent mines
            int numMines = board[row][col]; // number of mines adjacent to that exposed square
            int hiddenSquares = findHiddenSquares(row,col); // hidden squares adjacent to the exposed square
            // probability that it is a mine would be numMines/number of hidden squares around the square 
            float prob = (float)Math.round(numMines/hiddenSquares*100)/100;
            if(row> 0 && row < ROWS -1 && col > 0 && col < COLS - 1){
                knowledge[row-1][col-1] += prob;
                knowledge[row-1][col] += prob;
                knowledge[row-1][col+1] += prob;
                knowledge[row][col-1] += prob;
                knowledge[row][col+1] += prob;
                knowledge[row+1][col-1] += prob;
                knowledge[row+1][col] += prob;
                knowledge[row+1][col+1] += prob;
            }
        }
    }
    
    /**
     * Returns the number of hidden squares around the square
     */
    public int findHiddenSquares(int row, int col){
        int count = 1;
        // blah blah blah
        return count;
    }

    // completed methods
    
    /**
     * Mark/unmark the square.
     * If the square is exposed, don't do anything,
     * If it is marked, unmark it and redraw,
     * otherwise mark it and redraw.
     */
    public void mark(int row, int col){
        Square square = squares[row][col];
        if (square.isExposed())    { return; }
        else if (square.isMarked()){ square.unMark(); }
        else                       { square.mark(); }
        square.draw(row, col);
    }

    /**
     * Respond to the Mark and Expose buttons:
     * Remember whether the user is currently "Marking" or "Exposing"
     * Change the colour of the "Mark", "Expose" buttons
     */
    public void setMarking(boolean v){
        marking=v;
        if (marking) {
            mrkButton.setBackground(Color.red);
            expButton.setBackground(null);
        }
        else {
            expButton.setBackground(Color.red);
            mrkButton.setBackground(null);
        }
    }


    /**
     * Construct a grid with random mines.
     * Compute the number of adjacent mines in
     */
    public void makeGrid(){
        UI.clearGraphics();
        knowledge = new int[ROWS][COLS];
        this.squares = new Square[ROWS][COLS];
        for (int row=0; row < ROWS; row++){
            for (int col=0; col<COLS; col++){
                boolean isMine = Math.random()<0.1;     // approx 1 in 10 squares is a mine 
                this.squares[row][col] = new Square(isMine);
                this.squares[row][col].draw(row, col);
            }
        }
        // now compute the number of adjacent mines for each square
        for (int row=0; row<ROWS; row++){
            for (int col=0; col<COLS; col++){
                int count = 0;
                //look at each square in the neighbourhood.
                for (int r=Math.max(row-1,0); r<Math.min(row+2, ROWS); r++){
                    for (int c=Math.max(col-1,0); c<Math.min(col+2, COLS); c++){
                        if (squares[r][c].hasMine())
                            count++;
                    }
                }
                if (this.squares[row][col].hasMine())
                    count--;  // we weren't suppose to count this square, just the adjacent ones.

                this.squares[row][col].setAdjacentMines(count);
            }
        }
    }

    /** Draw a message telling the player they have won */
    public void drawWin(){
        UI.setFontSize(28);
        UI.drawString("You Win!", LEFT + COLS*SQUARE_SIZE + 20, TOP + ROWS*SQUARE_SIZE/2);
        UI.setFontSize(12);
    }

    /**
     * Draw a message telling the player they have lost
     * and expose all the squares and redraw them
     */
    public void drawLose(){
        for (int row=0; row<ROWS; row++){
            for (int col=0; col<COLS; col++){
                squares[row][col].setExposed();
                squares[row][col].draw(row, col);
            }
        }
        UI.setFontSize(28);
        UI.drawString("You Lose!", LEFT + COLS*SQUARE_SIZE+20, TOP + ROWS*SQUARE_SIZE/2);
        UI.setFontSize(12);
    }

    /**
     * Return a grid of integers, showing the visible state of the board:
     * -1 for any square that is not exposed
     * 0 - 8 for any exposed square, saying how many mines are adjacent to it.
     */
    public int[][] getVisibleState(){
        int[][] ans = new int[ROWS][COLS];
        for (int r=0; r<ROWS ; r++){
            for (int c=0; c<COLS; c++){
                ans[r][c] = squares[r][c].isExposed()?(squares[r][c].getAdjacentMines()):-1;
            }
        }
        return ans;
    }
}
