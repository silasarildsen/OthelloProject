import java.util.ArrayList;

public class DozzyAI implements IOthelloAI{
    /**
     * Given a gamestate this function decides which action to apply
     * Calculates the move to make for the given game state.
	 * @param s The current state of the game in which it should be the AI's turn.
	 * @return the position where the AI wants to put its token. 
	 * Is only called when a move is possible, but feel free to return 
	 * e.g. (-1, -1) if no moves are possible.
     */
    public Position decideMove(GameState s) {
        Tuple<Integer, Position> res = this.maxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        //int value = res.e1;
        Position move = res.e2;
        return move;
    }
    
    /**
     * Approximates the expected utility for a given game-state.
     */
    private int eval(GameState s){
        var board = s.getBoard();
        var player = s.getPlayerInTurn();
        int val = 0;
        // Is current player's token in the corner or on the edge?
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j] == player) continue;
                
                val += board[i][j] == player ? 1 : 0;
                if (i == 0 && j == 0 
                    || i == board.length-1 && j == board.length-1 
                    || i == 0 && j == board.length-1
                    || i == board.length-1 && j == 0){
                    val += 2;
                }
                else if(i == 0 || i == board.length-1 || j == 0 || j == board.length-1){
                    val += 1;
                }
            }
        }
        return val;
    }

    /**
     * Determines if the search should be cutoff, either by the game reaching a 
     * finished state
     * or by reaching some MAX reccursion depth
     */
    private boolean isCutoff(GameState s, int D){
        if(s.isFinished() || D > 5) return true;
        else return false;
    }
   
    private Tuple<Integer, Position> maxValue(GameState s, int alpha, int beta, int D) {
        if (isCutoff(s, D))
            return new Tuple<Integer, Position>(eval(s), null);
        
        int v = Integer.MIN_VALUE;
        Tuple<Integer, Position> move = new Tuple<Integer,Position>(v, null);
        
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
                move = new Tuple<Integer,Position>(min.e1, a);
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
    
    private Tuple<Integer, Position> minValue(GameState s, int alpha, int beta, int D) {
        if (s.isFinished())
            return new Tuple<Integer, Position>(this.utility(s), null);
        
        int v = Integer.MAX_VALUE;

        var move = new Tuple<Integer,Position>(v, null);
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
                move = new Tuple<Integer,Position>(max.e1, a);
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