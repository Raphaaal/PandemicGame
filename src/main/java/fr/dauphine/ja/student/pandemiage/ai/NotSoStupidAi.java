package fr.dauphine.ja.student.pandemiage.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;

public class NotSoStupidAi implements AiInterface {

	private List<Disease> getAllDiseases(){
		List<Disease >diseases = new LinkedList<>();
		diseases.add(Disease.BLACK);
		diseases.add(Disease.BLUE);
		diseases.add(Disease.RED);
		diseases.add(Disease.YELLOW);
		return diseases;
	}

	/**
	 * This basic AI will try to use its 4 actions by trying to :
	 * 
	 * 1. Discover the cure of an uncured disease
	 * 2. Treat as many diseases as possible in its location
	 * 3. Move to a random neighbour
	 * 
	 * Print Exception stack in last resort. 
	 * The bad size of this AI is that it doessn't handle well UnauthorizedActions exceptions to avoid losing actions points.
	 * 
	 * @param g the gameEmgine for the AI to play on
	 * @param p the player for the AI to play
	 */
	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {

		int nbActions = 4;

		while(nbActions > 0) {
			try {
				// Attempt to cure disease
				for (int j = 0; j < p.playerHand().size(); ++j) {
					// Need to count 5 same disease cards
					List<PlayerCardInterface> cardNames = new ArrayList<>();
					int sameDiseaseCardsCount = 0;
					Disease disease = p.playerHand().get(j).getDisease();
					for(int k = j+1; k < p.playerHand().size(); ++k) {
						if (p.playerHand().get(k).getDisease() == disease) {
							++sameDiseaseCardsCount;
							cardNames.add(p.playerHand().get(k));
						}
					}
					if(sameDiseaseCardsCount >= 5 && !g.isCured(disease)) {
						p.discoverCure(cardNames);
						System.out.println("Cured disease " + disease);
						nbActions = nbActions -1;
					}
				}

				// Attempt to treat
				for (Disease d : getAllDiseases()) {
					int nbDiseasesToCure = g.infectionLevel(p.playerLocation(), d);
					while (nbDiseasesToCure > 0) {
						p.treatDisease(d);
						System.out.println("Player treated disease " + d + " in " + p.playerLocation());
						nbActions = nbActions -1;
						nbDiseasesToCure = nbDiseasesToCure - 1;
					}
				}

				// Attempt to move to random neighbor
				Random r = new Random();
				int nextMove = r.nextInt(g.neighbours(p.playerLocation()).size());
				p.moveTo(g.neighbours(p.playerLocation()).get(nextMove));
				System.out.println("Player moved to " + p.playerLocation());
				nbActions = nbActions -1;

				// Redo attempts from Treating a disease
			} catch (UnauthorizedActionException e) {
				try {
					for (Disease d : getAllDiseases()) {
						int nbDiseasesToCure = g.infectionLevel(p.playerLocation(), d);
						while (nbDiseasesToCure > 0) {
							p.treatDisease(d);
							System.out.println("Player treated disease " + d + " in " + p.playerLocation());
							nbActions = nbActions -1;
							nbDiseasesToCure = nbDiseasesToCure - 1;
						}

					}
					Random r = new Random();
					int nextMove = r.nextInt(g.neighbours(p.playerLocation()).size());
					p.moveTo(g.neighbours(p.playerLocation()).get(nextMove));
					System.out.println("Player moved to " + p.playerLocation());
					nbActions = nbActions -1;
				}
				
				// Redo attempts from Moving
				catch (UnauthorizedActionException e1) {
					Random r = new Random();
					int nextMove = r.nextInt(g.neighbours(p.playerLocation()).size());
					try {
						p.moveTo(g.neighbours(p.playerLocation()).get(nextMove));
						System.out.println("Player moved to " + p.playerLocation());
						nbActions = nbActions -1;

						// Print stack in worst Exception handling case
					} catch (UnauthorizedActionException e2) {
						nbActions = nbActions -1;
						e2.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public List<PlayerCardInterface> discard(GameInterface g, PlayerInterface p, int maxHandSize, int nbEpidemic){
		List<PlayerCardInterface> discard = new ArrayList<>();
		int numdiscard = p.playerHand().size() - maxHandSize;  

		//Discard the less frequent disease in hand
		Map<PlayerCardInterface, Integer> handCardsDiseaseFreq = new HashMap<>(); 

		for (PlayerCardInterface pc : p.playerHand()) {
			handCardsDiseaseFreq.put(pc, 0);
			for (int i = 0; i < p.playerHand().size(); i++) {
				if(pc.getDisease() == p.playerHand().get(i).getDisease())
					handCardsDiseaseFreq.put(pc, handCardsDiseaseFreq.get(pc) + 1);
			}
		}

		for(int i = 0; i < numdiscard; i++) {
			PlayerCardInterface toDiscard = getMinFreqCard(handCardsDiseaseFreq, handCardsDiseaseFreq.keySet());
			discard.add(p.playerHand().get(i)); 
			handCardsDiseaseFreq.remove(toDiscard);
		}

		return discard;
	}

	private PlayerCardInterface getMinFreqCard(Map<PlayerCardInterface, Integer> map, Set<PlayerCardInterface> set) {
		PlayerCardInterface minKey = null;
		int minValue = Integer.MAX_VALUE;
		for(PlayerCardInterface key : set) {
			int value = map.get(key);
			if(value < minValue) {
				minValue = value;
				minKey = key;
			}
		}
		return minKey;
	}
}

