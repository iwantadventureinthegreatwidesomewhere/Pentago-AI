package student_player;

import boardgame.Move;
import java.util.Random;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoMove;
import pentago_swap.PentagoPlayer;

/** A player file submitted by a student. */
public class StudentPlayer extends PentagoPlayer {

    /**
     * You must modify this constructor to return your student number. This is
     * important, because this is what the code that runs the competition uses to
     * associate you with your agent. The constructor should do nothing else.
     */
    public StudentPlayer() {
        super("260803123");
    }

    /**
     * This is the primary method that you need to implement. The ``boardState``
     * object contains the current state of the game, which your agent must use to
     * make decisions.
     */
    public Move chooseMove(PentagoBoardState boardState) {
    	
    	long start = System.currentTimeMillis();
    	
    	try{
    		if(boardState.getTurnNumber() == 0) {
    			MyTools.loadData();
        	}
    	
	        if(boardState.getTurnNumber() < 2) {
	        	if(boardState.isLegal(new PentagoMove(1, 1, Quadrant.TL, Quadrant.TR, this.player_id))) {
	    			return new PentagoMove(1, 1, Quadrant.TL, Quadrant.TR, this.player_id);
	    		}else if(boardState.isLegal(new PentagoMove(1, 4, Quadrant.TL, Quadrant.TR, this.player_id))) {
	    			return new PentagoMove(1, 4, Quadrant.TL, Quadrant.TR, this.player_id);
	    		}else if(boardState.isLegal(new PentagoMove(4, 1, Quadrant.TL, Quadrant.TR, this.player_id))) {
	    			return new PentagoMove(4, 1, Quadrant.TL, Quadrant.TR, this.player_id);
	    		}else if(boardState.isLegal(new PentagoMove(4, 4, Quadrant.TL, Quadrant.TR, this.player_id))) {
	    			return new PentagoMove(4, 4, Quadrant.TL, Quadrant.TR, this.player_id);
	    		}
	        }
	        
	        PentagoMove winningMove = MyTools.checkAboutToWin(boardState);
	        if(winningMove != null) {
	        	return winningMove;
	        }
	        
	        PentagoMove defenseMove = MyTools.checkAboutToLose(boardState);
	        if(defenseMove != null) {
	        	return defenseMove;
	        }
	        
	        PentagoMove dataMove = MyTools.checkData(boardState);
	        if(dataMove != null) {
	        	return dataMove;
	        }
	        
	        Node root = new Node(boardState, null, null);
			MyTools.root = root;
	        
	        while(System.currentTimeMillis() - start < 1000) {
	        	Random random = new Random();
				Node leaf = MyTools.descent(root);
				int wins = MyTools.rollout(leaf, random);
				MyTools.update(leaf, wins, 1);
	        }
	        
	        Node best = null;
			double max = Double.MIN_VALUE;
			
			for(Node child : root.children) {
				double wins = child.wins;
				double visits = child.visits;
				if(wins/visits > max) {
					best = child;
					max = wins/visits;
				}else if(wins/visits == max) {
					if(visits > best.visits) {
						best = child;
						max = wins/visits;
					}
				}
			}
			
			return best.action;
			
    	}catch(Exception e) {
    		return boardState.getRandomMove();
    	}
    }
}
