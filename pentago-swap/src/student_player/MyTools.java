package student_player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import pentago_swap.PentagoBoardState;
import pentago_swap.PentagoBoardState.Piece;
import pentago_swap.PentagoBoardState.Quadrant;
import pentago_swap.PentagoMove;

public class MyTools {
	
	static Node root;
	static ArrayList<Alignment> data = new ArrayList<Alignment>();
	
	static Node descent(Node node) {
		if(node.visits == 0) {
			node.loadActions();
			return node;
		}else {
			Node bestToExpand = null;
			double max = Double.MIN_VALUE;
			
			for(Node child : node.children) {
				if(child.visits == 0) {
					return descent(child);
				}
				
				double ucb = calculateUCB(child);
				if(ucb > max) {
					bestToExpand = child;
					max = ucb;
				}
			}
			
			if(bestToExpand != null) {
				return descent(bestToExpand);
			}else {
				return node;
			}
		}
	}
	
	static int rollout(Node leaf, Random random) {
		PentagoBoardState board = (PentagoBoardState) leaf.state.clone();
		int me = board.getTurnPlayer();
		
		while(!board.gameOver()) {
			ArrayList<PentagoMove> moves = board.getAllLegalMoves();
			board.processMove(moves.get(random.nextInt(moves.size())));
		}
		
		if(board.getWinner() == me) {
			return 1;
		}else {
			return 0;
		}
	}
	
	static void update(Node node, int wins, int visits) {
		if(node != null) {
			node.wins += wins;
			node.visits += visits;
			update(node.parent, wins, visits);
		}
	}
	
	static double calculateUCB(Node node) {
		double q = node.wins/node.visits;
		double c = Math.sqrt(2);
		double rest = Math.sqrt(Math.log(node.parent.visits)/node.visits);
		return (q+c*rest);
	}
	
	static PentagoMove checkAboutToWin(PentagoBoardState state) {
		int me = state.getTurnPlayer();
		
		for(PentagoMove move : state.getAllLegalMoves()) {
			PentagoBoardState temp = (PentagoBoardState) state.clone();
			temp.processMove(move);
			
			if(temp.gameOver() && temp.getWinner() == me) {
				return move;
			}
		}
		
		return null;
	}
	
	static PentagoMove checkAboutToLose(PentagoBoardState state) {
		int them = state.getOpponent();
		
		PentagoMove move1 = null;
		PentagoMove move2 = null;
		int count = 0;
		
		while (count < 3 && move1 == move2) {
			move1 = (PentagoMove) state.getRandomMove();
			move2 = (PentagoMove) state.getRandomMove();
			count++;
		}
		
		for(int i = 0; i < 2; i++) {
			PentagoMove myMove = null;
			
			if(i == 0) {
				myMove = move1;
			}else {
				myMove = move2;
			}
			
			PentagoBoardState stateAfterMyMove = (PentagoBoardState) state.clone();
			stateAfterMyMove.processMove(myMove);
			
			for(PentagoMove theirMove : stateAfterMyMove.getAllLegalMoves()) {
				PentagoBoardState stateAfterTheirMove = (PentagoBoardState) stateAfterMyMove.clone();
				stateAfterTheirMove.processMove(theirMove);
				
				if(stateAfterTheirMove.gameOver() && stateAfterTheirMove.getWinner() == them) {
					int[] quadrantPositions = new int[] {1, 2, 3, 4};
					
					if(myMove.getASwap() == Quadrant.TL && myMove.getBSwap() == Quadrant.BL 
							|| myMove.getASwap() == Quadrant.BL && myMove.getBSwap() == Quadrant.TL) {
						int temp = quadrantPositions[0];
						quadrantPositions[0] = quadrantPositions[2];
						quadrantPositions[2] = temp;
					}else if(myMove.getASwap() == Quadrant.TR && myMove.getBSwap() == Quadrant.BR 
							|| myMove.getASwap() == Quadrant.BR && myMove.getBSwap() == Quadrant.TR) {
						int temp = quadrantPositions[1];
						quadrantPositions[1] = quadrantPositions[3];
						quadrantPositions[3] = temp;
					}else if(myMove.getASwap() == Quadrant.TL && myMove.getBSwap() == Quadrant.TR 
							|| myMove.getASwap() == Quadrant.TR && myMove.getBSwap() == Quadrant.TL) {
						int temp = quadrantPositions[0];
						quadrantPositions[0] = quadrantPositions[1];
						quadrantPositions[1] = temp;
					}else if(myMove.getASwap() == Quadrant.BL && myMove.getBSwap() == Quadrant.BR 
							|| myMove.getASwap() == Quadrant.BR && myMove.getBSwap() == Quadrant.BL) {
						int temp = quadrantPositions[2];
						quadrantPositions[2] = quadrantPositions[3];
						quadrantPositions[3] = temp;
					}else if(myMove.getASwap() == Quadrant.TL && myMove.getBSwap() == Quadrant.BR 
							|| myMove.getASwap() == Quadrant.BR && myMove.getBSwap() == Quadrant.TL) {
						int temp = quadrantPositions[0];
						quadrantPositions[0] = quadrantPositions[3];
						quadrantPositions[3] = temp;
					}else if(myMove.getASwap() == Quadrant.TR && myMove.getBSwap() == Quadrant.BL 
							|| myMove.getASwap() == Quadrant.BL && myMove.getBSwap() == Quadrant.TR) {
						int temp = quadrantPositions[1];
						quadrantPositions[1] = quadrantPositions[2];
						quadrantPositions[2] = temp;
					}
					
					int row = theirMove.getMoveCoord().getX();
					int col = theirMove.getMoveCoord().getY();
					int quadrant = -1;
					
					if(row >= 0 && row <= 2 && col >= 0 && col <= 2) {
						quadrant = quadrantPositions[0];
						if(quadrant == 1) {
							PentagoMove returnMove = new PentagoMove(row, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 2) {
							PentagoMove returnMove = new PentagoMove(row, col+3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 3) {
							PentagoMove returnMove = new PentagoMove(row+3, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 4) {
							PentagoMove returnMove = new PentagoMove(row+3, col+3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}
					}else if(row >= 0 && row <= 2 && col >= 3 && col <= 5) {
						quadrant = quadrantPositions[1];
						if(quadrant == 1) {
							PentagoMove returnMove = new PentagoMove(row, col-3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 2) {
							PentagoMove returnMove = new PentagoMove(row, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 3) {
							PentagoMove returnMove = new PentagoMove(row+3, col-3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 4) {
							PentagoMove returnMove = new PentagoMove(row+3, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}
					}else if(row >= 3 && row <= 5 && col >= 0 && col <= 2) {
						quadrant = quadrantPositions[2];
						if(quadrant == 1) {
							PentagoMove returnMove = new PentagoMove(row-3, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 2) {
							PentagoMove returnMove = new PentagoMove(row-3, col+3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 3) {
							PentagoMove returnMove = new PentagoMove(row, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 4) {
							PentagoMove returnMove = new PentagoMove(row, col+3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}
					}else if(row >= 3 && row <= 5 && col >= 3 && col <= 5) {
						quadrant = quadrantPositions[3];
						if(quadrant == 1) {
							PentagoMove returnMove = new PentagoMove(row-3, col-3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 2) {
							PentagoMove returnMove = new PentagoMove(row-3, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 3) {
							PentagoMove returnMove = new PentagoMove(row, col-3, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}else if(quadrant == 4) {
							PentagoMove returnMove = new PentagoMove(row, col, theirMove.getASwap(), theirMove.getBSwap(), state.getTurnPlayer());
							return state.isLegal(returnMove) ? returnMove : null;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	static void loadData() throws Exception {
		BufferedReader br = new BufferedReader(new FileReader("data/data.txt"));
		String line;
		
	    while ((line = br.readLine()) != null) {
	    	String first = line.replace("|", "").replace(" ", "");
		    String second = br.readLine().replace("|", "").replace(" ", "");
		       
		    String[] strCoordinate = br.readLine().split(",");
		    int row = Integer.parseInt(strCoordinate[0]);
		    int col = Integer.parseInt(strCoordinate[1]);
		    
		    data.add(new Alignment(first, second, row, col));
	    }
	    
	    br.close();
	}
	
	static PentagoMove checkData(PentagoBoardState state) {
		Piece agent = null;
		Piece opponent = null;
		
		if(state.getTurnPlayer() == 0) {
			agent = Piece.WHITE;
			opponent = Piece.BLACK;
		}else {
			agent = Piece.BLACK;
			opponent = Piece.WHITE;
		}
		
		for(int row = 0; row < 3; row++) {
			String topRow = "";
			String bottomRow = "";
			
			for(int col = 0; col < 6; col++) {
				if(state.getPieceAt(row, col) == agent) {
					topRow += "a";
				}else if(state.getPieceAt(row, col) == opponent) {
					topRow += "o";
				}else{
					topRow += "e";
				}
				
				if(state.getPieceAt(row+3, col) == agent) {
					bottomRow += "a";
				}else if(state.getPieceAt(row+3, col) == opponent) {
					bottomRow += "o";
				}else{
					bottomRow += "e";
				}
			}
			
			for(Alignment data : data) {
				if(data.compare(topRow, bottomRow)) {
					return new PentagoMove(data.row+row, data.col, Quadrant.TL, Quadrant.TR, state.getTurnPlayer());
				}
			}
		}
		
		for(int col = 0; col < 3; col++) {
			String leftColumn = "";
			String rightColumn = "";
			
			for(int row = 5; row > -1; row--) {
				if(state.getPieceAt(row, col) == agent) {
					leftColumn += "a";
				}else if(state.getPieceAt(row, col) == opponent) {
					leftColumn += "o";
				}else{
					leftColumn += "e";
				}
				
				if(state.getPieceAt(row, col+3) == agent) {
					rightColumn += "a";
				}else if(state.getPieceAt(row, col+3) == opponent) {
					rightColumn += "o";
				}else{
					rightColumn += "e";
				}
			}
			
			for(Alignment data : data) {
				if(data.compare(leftColumn, rightColumn)) {
					return new PentagoMove(5-data.col, data.row+col, Quadrant.TL, Quadrant.TR, state.getTurnPlayer());
				}
			}
		}
		
		return null;
	}
}
