package student_player;

public class Alignment {
	String first;
	String second;
	int row;
	int col;
	
	public Alignment(String first, String second, int row, int col) {
		this.first = first;
		this.second = second;
		this.row = row;
		this.col = col;
	}
	
	boolean compare(String first, String second) {
		for(int i = 0; i < 6; i++) {
			if(this.first.charAt(i) != '~') {
				if(this.first.charAt(i) != first.charAt(i)) {
					return false;
				}
			}
			
			if(this.second.charAt(i) != '~') {
				if(this.second.charAt(i) != second.charAt(i)) {
					return false;
				}
			}
		}
		
		return true;
	}
}
