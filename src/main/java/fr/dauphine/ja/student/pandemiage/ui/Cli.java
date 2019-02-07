package fr.dauphine.ja.student.pandemiage.ui;

import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.xml.stream.XMLStreamException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import javafx.application.Application;

/**
 * Command line interface to launch various modes of this Pandemic project :
 * - Launch an AI jar
 * - Play yourself in command line
 * - Multiplayer mode in command line
 * 
 * AI jar, city graph, difficulty, turn duration and hand size can be set using parameters.
 *
 */
public class Cli {

	public static final String DEFAULT_AIJAR = "DynamicAI.jar";
	public static final String DEFAULT_CITYGRAPH_FILE = "pandemic.graphml";
	public static final int DEFAULT_TURN_DURATION = 1; // in seconds
	public static final int DEFAULT_DIFFICULTY = 0; // Normal
	public static final int DEFAULT_HAND_SIZE = 9;

	public static void main(String[] args) throws FileNotFoundException, XMLStreamException, ParseException, IllegalArgumentException, UnauthorizedActionException {

		String aijar = DEFAULT_AIJAR;
		String cityGraphFile = DEFAULT_CITYGRAPH_FILE;
		int difficulty = DEFAULT_DIFFICULTY;
		int turnDuration = DEFAULT_TURN_DURATION;
		int handSize = DEFAULT_HAND_SIZE;

		Options options = new Options();
		CommandLineParser parser = new DefaultParser();

		options.addOption("a", "aijar", true, "use <FILE> as player Ai.");
		options.addOption("d", "difficulty", true, "Difficulty level. 0 (Introduction), 1 (Normal) or 3 (Heroic).");
		options.addOption("c", "citygraph", true, "City graph filename.");
		options.addOption("t", "turnduration", true, "Number of seconds allowed to play a turn.");
		options.addOption("s", "handsize", true, "Maximum size of a player hand.");
		options.addOption("h", "help", false, "Display this help");

		try {
			CommandLine cmd = parser.parse(options, args);

			if (cmd.hasOption("a")) {
				aijar = cmd.getOptionValue("a");
			}

			if (cmd.hasOption("c")) {
				cityGraphFile = cmd.getOptionValue("c");
			}

			if (cmd.hasOption("d")) {
				difficulty = Integer.parseInt(cmd.getOptionValue("d"));
			}

			if (cmd.hasOption("t")) {
				turnDuration = Integer.parseInt(cmd.getOptionValue("t"));
			}
			if (cmd.hasOption("s")) {
				handSize = Integer.parseInt(cmd.getOptionValue("s"));
			}

			if (cmd.hasOption("h")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("pandemiage", options);
				System.exit(0);
			}

		} catch (ParseException e) {
			System.err.println("Error: invalid command line format.");
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("pandemiage", options);
			System.exit(1);
		}

		
		try (Scanner sc = new Scanner(System.in)) {
			int choice = -1;
			while (choice != 1 && choice != 2 && choice != 3) {
				System.out.println("###########################################");
				System.out.println("###########################################");
				System.out.println("#######     WELCOME TO PANDEMIC   #########");
				System.out.println("###########################################");
				System.out.println("###########################################");
				System.out.println("");
				System.out.println("- City graph : " + cityGraphFile);
				System.out.println("- AI jar : " + aijar);
				System.out.println("- Difficulty : " + difficulty);
				System.out.println("- Hand size : " + handSize);
				System.out.println("- Turn duration : " + turnDuration);
				System.out.println("");
				System.out.println("###########################################");
				System.out.println("###########################################");
				System.out.println("###########################################");
				System.out.println("###########################################");
				System.out.println("###########################################");
				System.out.println("");
				System.out.println("Choose :");
				System.out.println(" 1 : Launch the AI");
				System.out.println(" 2 : Play yourself");
				System.out.println(" 3 : Multiplayer");
				choice = sc.nextInt();
			}
			if (choice == 1) {
				GameEngine g = new GameEngine(cityGraphFile, aijar, difficulty, handSize, turnDuration);
				g.loopAI();
			}
			if (choice == 2) {
				GameEngine g = new GameEngine(cityGraphFile, aijar, difficulty, handSize, turnDuration);
				g.loopPlayer();

			}
			if (choice == 3) {
				choice=-1;
				System.out.println("Enter the number of players between 1 and 4 : ");
				while (choice > 4 || choice < 1) {
					choice = sc.nextInt();
				}
				GameEngine g = new GameEngine(cityGraphFile, aijar, difficulty, handSize, turnDuration, choice);
				g.multiPlayerLoop();
			}

		}
	}
}
