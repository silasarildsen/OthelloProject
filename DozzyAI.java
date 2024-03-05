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
        Tuple<Integer, Position> res = this.maxValue(s, Integer.MIN_VALUE, Integer.MAX_VALUE);
        //int value = res.e1;
        Position move = res.e2;
        return move;
    }
   
    private Tuple<Integer, Position> maxValue(GameState s, int alpha, int beta) {
        if (s.isFinished())
            return new Tuple<Integer, Position>(this.utility(s), null);
        
        int v = Integer.MIN_VALUE;

        Tuple<Integer, Position> move = new Tuple<Integer,Position>(v, new Position(-1,-1));
        for(Position a : this.actions(s))
        {
            var sn = CloneGame(s);
            sn.insertToken(a);
            var min = minValue(sn, alpha, beta);
            var v2 = min.e1;

            //undoBoard(s, a);
            if(v2 > v){
                v = v2;
                move = new Tuple<Integer,Position>(v2, a);
                alpha = Math.max(alpha, v);
            }
            /*if (v >= beta)
            {
                return move;
            } */
        }
        return move;
    }
    
    private Tuple<Integer, Position> minValue(GameState s, int alpha, int beta) {
        if (s.isFinished())
            return new Tuple<Integer, Position>(this.utility(s), null);
        
        int v = Integer.MAX_VALUE;

        var move = new Tuple<Integer,Position>(v, new Position(-2,-2));
        for(Position a : this.actions(s))
        {
            var sn = CloneGame(s);
            sn.insertToken(a);
            var max = maxValue(sn, alpha, beta);
            var v2 = max.e1;

            //undoBoard(s, a);
            if(v2 < v){
                v = v2;
                move = new Tuple<Integer,Position>(v2, a);
                beta = Math.min(beta, v);
            }
            /*if (v <= alpha)
            {
                return move;
            }*/
        }
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

    private static void undoBoard(GameState s, Position a){
        s.getBoard()[a.col][a.row] = 0;
        s.changePlayer();
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
     * @param S the GameState object to copy.
     */
    private static GameState CloneGame(GameState S){
        return new GameState(S.getBoard(), S.getPlayerInTurn());
    }
}