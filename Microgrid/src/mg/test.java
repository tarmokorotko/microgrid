package mg;

import bin.PlaygroundPart;

public class test {

	public static void main(String[] args) {
		try {
			PlaygroundPart window = new PlaygroundPart();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}	

	}

}
