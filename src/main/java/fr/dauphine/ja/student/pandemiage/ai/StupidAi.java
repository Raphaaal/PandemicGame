package fr.dauphine.ja.student.pandemiage.ai;

import java.util.ArrayList;
import java.util.List;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;

public class StupidAi implements AiInterface {
	@Override
	public void playTurn(GameInterface g, PlayerInterface p) {
		for(int i=0;i<4;++i) {
			try {
				p.moveTo(g.neighbours(p.playerLocation()).get(0));
			} catch (UnauthorizedActionException e) {
				e.printStackTrace();
			}
		}
	} 

	@Override
	public List<PlayerCardInterface> discard(GameInterface g, PlayerInterface p, int maxHandSize, int nbEpidemic){
		List<PlayerCardInterface> discard = new ArrayList<>();
		int numdiscard = p.playerHand().size() - maxHandSize;  

		for(int i = 0; i < numdiscard; i++)
			discard.add(p.playerHand().get(i)); 

		return discard;
	}
}

