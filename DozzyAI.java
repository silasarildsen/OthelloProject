import java.util.ArrayList;

public class DozzyAI implements IOthelloAI{
    private int player;
    private int boardLength;
    private static final int MAX_DEPTH = 7;

    /**
     * Given a gamestate this function decides which action to apply.
	 * @param s The current state of the game in which it should be the AI's turn.
	 * @return The position where the AI wants to put its token.
     */
    public Position decideMove(GameState s) {
        boardLength = s.getBoard().length;
        player = s.getPlayerInTurn();
        var res = this.maxValue(s, Float.MIN_VALUE, Float.MAX_VALUE, 0);
        Position move = res.e2;
        return move;
    }
    
    /**
     * Checks whether the given coordinate corresponds to an edge-tile on the board.
     * @param length The length of the board
     * @param i The row index
     * @param j The column index
     * @return True if the position is an edge, false otherwise
     */
    private boolean isEdge(int length, int i, int j){
        return i == 0 || i == length-1 || j == 0 || j == length-1;
    }

    /**
     * Checks whether the given coordinate corresponds to a near-edge-tile on the board.
     * @param length The length of the board
     * @param i The row index
     * @param j The column index
     * @return True if the position is a near-edge, false otherwise
     */
    private static boolean isNearEdge(int length, int i, int j) {
        return i == 1 || i == length-2 || j == 1 || j == length-2;
    }

    /**
     * Checks whether the given coordinate corresponds to a corner-tile on the board.
     * @param length The length of the board
     * @param i The row index
     * @param j The column index
     * @return True if the position is a corner, false otherwise
     */
    private static boolean isCorner(int length, int i, int j) {
        int lengthm1 = length - 1;
        return (i == 0 && j == 0 
            || i == lengthm1 && j == lengthm1 
            || i == 0 && j == lengthm1
            || i == lengthm1 && j == 0);
    }

    /**
     * Approximates the expected utility for a given game-state.
     * @param s the game-state to evaluate
     * @return The expected utility of the game-state
     */
    private float eval(GameState s){
        if (s.isFinished()) return utility(s);

        // Set up lists for the four features we are interested in.
        // The features are represented in the lists as: [corners, edges, nearEdges, normals]
        float[] placedTiles = {0,0,0,0};
        float[] weights = {0.7f, 0.2f, -0.1f, 0.1f};
        float[] maxValues = {4, (boardLength * 4) - 4, ((boardLength - 2) * 4) - 4, boardLength*boardLength};
        int cornerIndex = 0, edgeIndex = 1, nearEdgeIndex = 2, normalIndex = 3;

        // Find out how many tiles there are for each of the four features
        var board = s.getBoard();
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j] != player) continue;
                if (isCorner(board.length, i, j)){
                    placedTiles[cornerIndex]++;
                }
                else if(isEdge(board.length, i, j)){
                    placedTiles[edgeIndex]++;
                }
                else if(isNearEdge(board.length, i, j)){
                    placedTiles[nearEdgeIndex]++;
                }
                // any other position - somewhere in the middle of the board.
                else {
                    placedTiles[normalIndex]++;
                }
            }
        }

        // Find the weighted sum of the placed tiles for the given features
        float score = 0;
        for (int i = 0; i < placedTiles.length; i++) {
            score += ((placedTiles[i] / maxValues[i]) * weights[i]);
        }

        return score;
    }

    /**
     * Determines if the search should be cutoff, either by the game reaching a finished state or by the depth reaching the maximum depth.
     * @param s the gamestate to check
     * @param D the current depth of the search
     */
    private boolean isCutoff(GameState s, int D){
        if(D == MAX_DEPTH && isQuiescent(s)) return false;
        else if(D > MAX_DEPTH || s.isFinished()) return true;
        else return false;
    }

    /**
     * Determines if the gamestate is quiescent, i.e. if the gamestate is in a state where we have a chance to
     * make a move that would wildly swing the evaluation of the gamestate.
     * @param s the gamestate to check
     * @return true if the gamestate is quiescent, false otherwise
     */
    private boolean isQuiescent(GameState s){
        var actions = actions(s);
        var length = actions.size();
        var boardLength = s.getBoard().length;
        for(int i = 0; i < length; i++){
            var a = actions.get(i);
            if(isCorner(boardLength,a.col, a.row)) return true;
        }
        return false;
    }
   
    /**
     * Function for finding the best move for Max to make.
     * If Max has no legal moves, the search will continue, until either a leaf-node is reached, or the maximum recursion depth is reached.
     * @param s The current game state.
     * @param alpha The current value for alpha.
     * @param beta The current value for beta.
     * @param D The current depth of the search.
     * @return A tuple containing the value of the game state and the position to make the move.
     */
    private Tuple<Float, Position> maxValue(GameState s, float alpha, float beta, int D) {
        if (isCutoff(s, D))
            return new Tuple<Float, Position>(eval(s), null);
        
        float v = -Float.MAX_VALUE;
        Tuple<Float, Position> move = new Tuple<Float,Position>(v, null);
        
        var actions = actions(s);
        
        int i = 0;
        //Do while to catch the cases where Max is unable to do any plays.
        do {
            var a = !actions.isEmpty() ? actions.get(i) : null;

            var clonedState = CloneGame(s);
            if(actions.isEmpty())
                clonedState.changePlayer();
            else 
                clonedState.insertToken(a);
            
            var min = minValue(clonedState, alpha, beta, D+1);

            if(min.e1 > v){
                v = min.e1;
                move = new Tuple<Float,Position>(min.e1, a);
                alpha = Math.max(alpha, v);
            }
            if (v >= beta)
            {
                return move;
            }
            i++;
        } while(i < actions.size());
    
        return move;
    }
    
    /**
     * Function for finding the best move for Min to make.
     * If Min has no legal moves, the search will continue, until either a leaf-node is reached, or the maximum recursion depth is reached.
     * @param s The current game state.
     * @param alpha The current value for alpha.
     * @param beta The current value for beta.
     * @param D The current depth of the search.
     * @return A tuple containing the value of the game state and the position to make the move.
     */
    private Tuple<Float, Position> minValue(GameState s, float alpha, float beta, int D) {
        if (isCutoff(s, D))
            return new Tuple<Float, Position>(eval(s), null);
        
        float v = Float.MAX_VALUE;

        var move = new Tuple<Float,Position>(v, null);
        var actions = actions(s);

        int i = 0;
        do {
            var a = !actions.isEmpty() ? actions.get(i) : null;

            var clonedState = CloneGame(s);
            if(actions.isEmpty())
                clonedState.changePlayer();
            else 
                clonedState.insertToken(a);
            
            var max = maxValue(clonedState, alpha, beta, D+1);

            if(max.e1 < v){
                v = max.e1;
                move = new Tuple<Float,Position>(max.e1, a);
                beta = Math.min(beta, v);
            }
            if (v <= alpha)
            {
                return move;
            }
            i++;
        } while(i < actions.size());
        return move;
    }

    /**
     * Simple wrapper function for the legalMoves function in GameState.
     * @param s the gamestate to get actions for
     * @return a list of all possible actions for the given game state
     */
    private ArrayList<Position> actions(GameState s)
    {
        return s.legalMoves();
    }

    /**
     * Calculates the utility of a given game state.
     * The utility of a leaf-node is 1 if Max wins, -1 if Min wins, and 0 if it is a draw.
     * @param s the gamestate to calc utility of
     */
    private int utility(GameState s) {
        if(!s.isFinished()) throw new RuntimeException("Game is not finished");
        var tokens = s.countTokens();
        return tokens[0] > tokens[1] ? 1 : tokens[0] < tokens[1] ? -1 : 0;
    }

    /**
     * Homemade Tuple to match that of the pseudocode
     * from RN21 chapter 6.
     */
    private class Tuple<T1, T2> {
        public final T1 e1;
        public final T2 e2;

        public Tuple(T1 e1, T2 e2) {
            this.e1 = e1;
            this.e2 = e2;
        }
    }

    /**
     * Helper function to copy gamestate, less key-strokes
     */
    private static GameState CloneGame(GameState S){
        return new GameState(S.getBoard(), S.getPlayerInTurn());
    }
}