package student_player;

import java.util.ArrayList;
import java.util.Collections;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoMove;

public class Node {
	PentagoBoardState state;
	PentagoMove action;
	Node parent;
	ArrayList<Node> children = new ArrayList<Node>();
	int wins = 0;
	int visits = 0;
	
	public Node(PentagoBoardState state, PentagoMove action, Node parent) {
		this.state = (PentagoBoardState) state.clone();
		this.action = action;
		this.parent = parent;
	}
	
	public void loadActions() {
		long start = System.currentTimeMillis();
		ArrayList<PentagoMove> legalMoves = state.getAllLegalMoves();
		Collections.shuffle(legalMoves);
		for(PentagoMove move : legalMoves) {
			if(System.currentTimeMillis() - start < 250) {
				PentagoBoardState temp = (PentagoBoardState) state.clone();
				temp.processMove(move);
				children.add(new Node(temp, move, this));
			}else {
				break;
			}
		}
	}
}
