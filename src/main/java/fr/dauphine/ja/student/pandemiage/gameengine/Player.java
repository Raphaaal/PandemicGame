package fr.dauphine.ja.student.pandemiage.gameengine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;

/**
 * A ready-to-play Player for a Board.
 *
 */
public class Player implements PlayerInterface {
	
	private static int countPlayer = 1;
	private City location;
	private final Board board;
	private List<PlayerCardInterface> hand;
	private int actionPoint;
	private final int id;

	/**
	 * Creates a Player with an auto-incremented id.
	 * 
	 * @param city not null
	 * @param board the Board
	 * @throws IllegalArgumentException
	 */
	public Player(City city, Board board) throws IllegalArgumentException {
		this.hand = new ArrayList<>();
		this.location = Objects.requireNonNull(city);
		this.actionPoint = 0;
		this.board = board;
		this.id = countPlayer++;
	}

	@Override
	public void moveTo(String cityName) throws UnauthorizedActionException {
		if (location.getNeighbours().containsKey(cityName)) {
			location = location.getNeighbours().get(cityName);
			this.actionPoint--;
		} else
			throw new UnauthorizedActionException("this city isn't a neighbour");
	}

	@Override
	public void flyTo(String cityName) throws UnauthorizedActionException {
		Objects.requireNonNull(cityName);
		hand.remove(getIndexOfCard(cityName));
		location = board.getCity(cityName);
		this.actionPoint--;

	}

	@Override
	public void flyToCharter(String cityName) throws UnauthorizedActionException {
		Objects.requireNonNull(cityName);
		hand.remove(getIndexOfCard(this.location.getLabel()));
		location = board.getCity(cityName);
		this.actionPoint--;

	}

	@Override
	public void skipTurn() {
		this.actionPoint = 0;

	}

	@Override
	public void treatDisease(Disease d) throws UnauthorizedActionException {
		if (location.getDiseases().get(d) > 0) {
			if (board.getVaccine(d)) {
				while (location.getDiseases().get(d) > 0)
					board.removeDisease(location.getLabel(), d);
			} else
				board.removeDisease(location.getLabel(), d);
			this.actionPoint--;
		} else {
			throw new UnauthorizedActionException("this disease isn't here");
		}

	}

	@Override
	public void discoverCure(List<PlayerCardInterface> cardNames) throws UnauthorizedActionException {
		if (Objects.requireNonNull(cardNames).size() < 0)
			throw new IllegalArgumentException();
		Disease disease = cardNames.get(0).getDisease();
		for (PlayerCardInterface card : cardNames) {
			if (card.getDisease() != disease || !hand.contains(card))
				throw new UnauthorizedActionException(
						"Every card have to have the same disease and have to be in your hand. ");
		}
		this.board.setVaccine(disease);
		for (PlayerCardInterface card : cardNames) {
			hand.remove(card);
		}
		this.actionPoint--;
	}

	@Override
	public String playerLocation() {
		return this.location.getLabel();
	}

	@Override
	public List<PlayerCardInterface> playerHand() {
		return this.hand;
	}

	/**
	 * Execute a given Action for a real player.
	 * 
	 * @param action  the action to execute
	 * @throws UnauthorizedActionException
	 */
	 public void playTurn(){
		this.actionPoint = 4;
		Scanner sc = new Scanner(System.in);

		while (this.actionPoint > 0) {
			boolean retry = true;
			do {

				System.out.println("Player " + this.id + " : Choose an action please.");
				displayActions();
				System.out.println("");

				try {
					switch (PlayerAction.valueOf(sc.nextLine())) {
					case CHARTERFLY:
						System.out.println("Choose a city : " + board.getCities().keySet());
						flyToCharter(sc.nextLine());
						break;

					case CREATEVACCINE:
						discoverCure(chooseCardForVaccin(sc));
						break;

					case DIRECTFLY:
						System.out.println("Choose a city : " + board.getCities().keySet());
						flyTo(sc.nextLine());
						break;

					case DISPLAYHAND:
						System.out.println(hand);
						break;

					case DRIVE:
						System.out.println("Choose a city ");
						moveTo(sc.nextLine());
						break;

					case PASS:
						retry = false;
						skipTurn();
						break;

					case REMOVEDISEASE:
						System.out.println("Choose a disease " );
						treatDisease(Disease.valueOf(sc.nextLine()));
						break;

					case DISPLAYCITIES:
						System.out.println(board.getCities());
						break;

					case DISPLAYLOCATION:
						System.out.println(this.location);
						break;

					default:
						break;
					}
					retry = false;

				} catch (UnauthorizedActionException | IllegalArgumentException e) {
					System.out.println("Unauthorized action");
					System.out.println(e);
				}

			}

			while (retry);

		}
	}

	public void displayActions() {
		for (PlayerAction action : PlayerAction.values())
			System.out.print(action + " ");
	}

	public List<PlayerCardInterface> chooseCardForVaccin(Scanner sc) throws UnauthorizedActionException {
		if (hand.size() < 5)
			throw new UnauthorizedActionException("You don't have enough cards.");

		System.out.println("Choose 5 cards (enter their number) : ");
		for (int i = 0; i < hand.size(); i++) {
			System.out.print(i + "-" + hand.get(i) + " ");
		}
		System.out.println();
		List<PlayerCardInterface> cards = new ArrayList<>();
		while (cards.size() < 5) {
			System.out.println(5 - cards.size() + " left.");
			int choice = sc.nextInt();
			if (!cards.contains(hand.get(choice)))
				cards.add(hand.get(choice));
			System.out.println("This card is already selected.");
		}
		return cards;
	}

	public int getIndexOfCard(String cityName) throws UnauthorizedActionException {
		for (int i = 0; i < hand.size(); i++)
			if (hand.get(i).getCityName().equals(cityName)) {
				return i;
			}
		throw new UnauthorizedActionException("You don't have the card : " + cityName + " in your hand");
	}

	public void discard(int nbDiscard) {
		int discardLeft = nbDiscard;
		try (Scanner sc = new Scanner(System.in);) {
			while (discardLeft > 0) {
				System.out.println("Choose " + nbDiscard + " cards to discard : ");
				System.out.println(this.hand);

				try {
					this.hand.remove(getIndexOfCard(sc.nextLine()));
					discardLeft--;
				} catch (UnauthorizedActionException e) {
					throw new IllegalStateException(e);
				}
			}

		}
	}

}
