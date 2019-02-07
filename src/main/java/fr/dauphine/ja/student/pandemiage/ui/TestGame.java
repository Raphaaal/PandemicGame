package fr.dauphine.ja.student.pandemiage.ui;

import java.io.FileNotFoundException;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.cli.ParseException;
import fr.dauphine.ja.pandemiage.common.DefeatReason;
import fr.dauphine.ja.pandemiage.common.GameStatus;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;

/**
 * This class is used to test the performance of an AI over certain criteria : 
 *  - # Win and # Defeats
 *  - Cured diseases
 *  - Defeat reasons  
 * The AI is tested over 100 iterations of the gameEngine with difficulty 0, hand size 9 and turn duration 15 seconds.
 */
public class TestGame {
	public static void main(String[] args) throws FileNotFoundException, IllegalArgumentException, XMLStreamException,
	ParseException, UnauthorizedActionException {

		int countCured = 0;
		int win = 0;
		int lose = 0;
		int outbreak=0;
		int lackOfCube=0;
		int lackOfCard=0;
		int error = 0;

		// Test of game performance out of 100 iterations of the AI
		for (int i = 0; i < 100; i++) {
			GameEngine g = new GameEngine("pandemic.graphml", "DynamicAI.jar",0, 9, 15);
			// Does not print game results in order not to exit system
			g.loopWithoutGameResults();

			countCured = countCured + g.getCuredPerf();

			if (g.gameStatus() == GameStatus.VICTORIOUS)
				win++;
			else {
				lose++;

				DefeatReason dr = g.getBoard().getDr();

				if(dr == DefeatReason.NO_MORE_PLAYER_CARDS) {
					lackOfCard++;
				} else if(dr == DefeatReason.NO_MORE_BLOCKS) {
					lackOfCube++;
				} else if(dr == DefeatReason.TOO_MANY_OUTBREAKS) {
					outbreak++;
				} else
					error++;
			}
		}

		System.out.println(countCured + " cured diseases");
		System.out.println("win :" + win + " lose:" + lose);
		System.out.println("outbreak : "+outbreak);
		System.out.println("no block : "+lackOfCube);
		System.out.println("no card : "+lackOfCard);
		System.out.println("no error : "+error);
	}
}
