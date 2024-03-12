import java.util.ArrayList;

public class DozzyAI implements IOthelloAI{
    private int player;
    private static final int MAX_DEPTH = 7;

    /**
     * Given a gamestate this function decides which action to apply
     * Calculates the move to make for the given game state.
	 * @param s The current state of the game in which it should be the AI's turn.
	 * @return the position where the AI wants to put its token. 
	 * Is only called when a move is possible, but feel free to return 
	 * e.g. (-1, -1) if no moves are possible.
     */
    public Position decideMove(GameState s) {
        player = s.getPlayerInTurn();
        var res = this.maxValue(s, Float.MIN_VALUE, Float.MAX_VALUE, 0);
        //int value = res.e1;
        Position move = res.e2;
        return move;
    }
    
    private boolean isEdge(int length, int i, int j){
        return i == 0 || i == length-1 || j == 0 || j == length-1;
    }

    private static boolean isNearEdge(int length, int i, int j) {
        return i == 1 || i == length-2 || j == 1 || j == length-2;
    }

    private static boolean isCorner(int length, int i, int j) {
        int lengthm1 = length - 1;
        return (i == 0 && j == 0 
            || i == lengthm1 && j == lengthm1 
            || i == 0 && j == lengthm1
            || i == lengthm1 && j == 0);
    }

    
    /**
     * Approximates the expected utility for a given game-state.
     */
    private float eval(GameState s){
        float util = utility(s);
        if (s.isFinished()) return util;
        var board = s.getBoard();
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j] != player) continue;
                if (isCorner(board.length, i, j)){
                    util *= 0.8f;
                }
                else if(isEdge(board.length, i, j)){
                    util *= 0.5f;
                }
                // near edge position, good for min
                else if(isNearEdge(board.length, i, j)){
                    util *= 0.1f;
                }
                // wack position - Everything in the middle-ish
                else {
                    util *= 0.25f;
                }
            }
        }
        return util;
    }

    /**
     * Determines if the search should be cutoff, either by the game reaching a 
     * finished state
     * or by reaching some MAX reccursion depth
     */
    private boolean isCutoff(GameState s, int D){
        if(D == MAX_DEPTH && isQuiescent(s)) return false;
        else if(s.isFinished() || D > MAX_DEPTH) return true;
        else return false;
    }

    private boolean isQuiescent(GameState s){
        var actions = actions(s);
        var length = actions.size();
        var boardLength = s.getBoard().length;
        for(int i = 0; i < length; i++){
            var a = actions.get(i);
            if(isCorner(boardLength,a.col, a.row)) return true;
            // var clone = CloneGame(s); //
            // var tokens = clone.countTokens()[player - 1];
            // clone.insertToken(actions.get(i));
            // var tokensAfter = clone.countTokens()[player-1];
            // if(((float)tokensAfter - tokens) > s.getBoard().length * 0.6) return true;
        }
        return false;
    }
   
    private Tuple<Float, Position> maxValue(GameState s, float alpha, float beta, int D) {
        if (isCutoff(s, D))
            return new Tuple<Float, Position>(eval(s), null);
        
        float v = Float.MIN_VALUE;
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

    private ArrayList<Position> actions(GameState s)
    {
        return s.legalMoves();
    }

    /**
     * Calculates the utility of a given game state
     * We decided that the utility would be the number of 
     * black pieces
     * @TODO: What about corners?
     * @param s the gamestate to calc utility of
     */
    private int utility(GameState s) {
        int[] tokens = s.countTokens();
        return tokens[0];
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
     * Helper function to copy gamestate, less key stroke
     */
    private static GameState CloneGame(GameState S){
        return new GameState(S.getBoard(), S.getPlayerInTurn());
    }
}