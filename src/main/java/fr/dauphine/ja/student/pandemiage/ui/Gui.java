package fr.dauphine.ja.student.pandemiage.ui;

import java.awt.TextArea;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javax.xml.stream.XMLStreamException;
import com.sun.glass.ui.Size;
import com.sun.javafx.scene.control.LabeledImpl;
import fr.dauphine.ja.pandemiage.common.AiInterface;
import fr.dauphine.ja.pandemiage.common.AiLoader;
import fr.dauphine.ja.pandemiage.common.Disease;
import fr.dauphine.ja.pandemiage.common.GameInterface;
import fr.dauphine.ja.pandemiage.common.GameStatus;
import fr.dauphine.ja.pandemiage.common.PlayerCardInterface;
import fr.dauphine.ja.pandemiage.common.PlayerInterface;
import fr.dauphine.ja.pandemiage.common.UnauthorizedActionException;
import fr.dauphine.ja.student.pandemiage.gameengine.Board;
import fr.dauphine.ja.student.pandemiage.gameengine.City;
import fr.dauphine.ja.student.pandemiage.gameengine.GameEngine;
import fr.dauphine.ja.student.pandemiage.gameengine.PandemicParser;
import fr.dauphine.ja.student.pandemiage.gameengine.Player;
import fr.dauphine.ja.student.pandemiage.gameengine.PlayerAction;
import fr.dauphine.ja.student.pandemiage.gameengine.PlayerCard;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.shape.*;

public class Gui extends Application {
	private GameEngine ge;
	private int x = 1000;
	private int y = 600;
	private int xOrigin = x / 2;
	private int yOrigin = y;
	private int scale = 3;
	private PlayerInterface player;
	private Label userHand = null;
	private HBox layout = new HBox(6);
	private HBox printSide = new HBox(6);
	private HBox rightSide = new HBox(6);
	private VBox leftSide = new VBox(20);
	private Label labD = new Label();
	private Map<String, Label> labelByCity;
	private TreeSet<String> dset = new TreeSet<String>();
	private PandemicParser pp = new PandemicParser("pandemic.graphml");
	private HashMap<String, City> cities;
	private int nbAction;
	private GameStatus gameStatus;
	private Label infectionRateLabel;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws FileNotFoundException, XMLStreamException, ParseException,
			org.apache.commons.cli.ParseException, IllegalArgumentException, UnauthorizedActionException {

		Pane root = new Pane();
		userHand = new Label("Card logo ", initplayerMarker("card.png", true));
		labD = new Label("Outbreak logo ", initplayerMarker("disease.png", true));
		labD.setTextFill(Color.WHITE);
		userHand.setTextFill(Color.WHITE);
		layout.getChildren().add(labD);
		printSide.setAlignment(Pos.TOP_LEFT);
		layout.setAlignment(Pos.TOP_CENTER);
		rightSide.setAlignment(Pos.BOTTOM_LEFT);
		root.getChildren().add(rightSide);
		// create a input stream
		FileInputStream input = new FileInputStream("back.jpg");

		// create a image
		Image image = new Image(input);

		// set background
		Scene scene = new Scene(root, x, y * 2, Color.BURLYWOOD);

		Pane g = new Pane();

		userHand.setLayoutX(xOrigin + 500);
		userHand.setLayoutX(yOrigin - 500);
		cities = pp.ParseCities();
		this.labelByCity = new HashMap<>();

		for (City city : cities.values()) {

			if (city.getR() == 153 && city.getG() == 153 && city.getB() == 153) {
				Circle c = new Circle(city.getX() / scale + xOrigin, yOrigin - city.getY() / scale, 5);
				c.setFill(Color.BLACK);
				g.getChildren().add(c);
				Label cityLabel = new Label(city.getLabel());
				cityLabel.setTextFill(Color.BLACK);
				cityLabel.setTranslateX(c.getCenterX());
				cityLabel.setTranslateY(c.getCenterY());
				g.getChildren().add(cityLabel);
				this.labelByCity.put(city.getLabel(), cityLabel);

			}

			else if (city.getR() == 107 && city.getG() == 112 && city.getB() == 184) {
				Circle c = new Circle(city.getX() / scale + xOrigin, yOrigin - city.getY() / scale, 5);
				c.setFill(Color.BLUE);
				g.getChildren().add(c);
				Label cityLabel = new Label(city.getLabel());
				cityLabel.setTextFill(Color.BLUE);
				cityLabel.setTranslateX(c.getCenterX());
				cityLabel.setTranslateY(c.getCenterY());
				g.getChildren().add(cityLabel);
				this.labelByCity.put(city.getLabel(), cityLabel);

			} else if (city.getR() == 153 && city.getG() == 18 && city.getB() == 21) {
				Circle c = new Circle(city.getX() / scale + xOrigin, yOrigin - city.getY() / scale, 5);
				c.setFill(Color.RED);
				g.getChildren().add(c);
				
				Label cityLabel = new Label(city.getLabel());
				cityLabel.setTextFill(Color.RED);
				cityLabel.setTranslateX(c.getCenterX());
				cityLabel.setTranslateY(c.getCenterY());
				g.getChildren().add(cityLabel);
				this.labelByCity.put(city.getLabel(), cityLabel);

			} else if (city.getR() == 242 && city.getG() == 255 && city.getB() == 0) {
				Circle c = new Circle(city.getX() / scale + xOrigin, yOrigin - city.getY() / scale, 5);
				c.setFill(Color.YELLOW);
				g.getChildren().add(c);
				Label cityLabel = new Label(city.getLabel());
				cityLabel.setTextFill(Color.YELLOW);
				cityLabel.setTranslateX(c.getCenterX());
				cityLabel.setTranslateY(c.getCenterY());
				g.getChildren().add(cityLabel);
				this.labelByCity.put(city.getLabel(), cityLabel);

			}

			for (City neighbour : city.getNeighbours().values()) {
				Line line = new Line();
				line.setStartX(city.getX() / scale + xOrigin);
				line.setStartY(yOrigin - city.getY() / scale);
				line.setEndX(neighbour.getX() / scale + xOrigin);
				line.setEndY(yOrigin - neighbour.getY() / scale);
				line.setFill(Color.BLACK);
				g.getChildren().add(line);

			}

		}

		// initialize the gameEngine
		ge = new GameEngine("pandemic.graphml", "DynamicAI.jar", 2, 9, 15);
		// initialize the player
		player = ge.getPlayer();

		for (int i = 0; i < player.playerHand().size(); i++) {
			g.getChildren().add(setColor(player.playerHand()).get(i));
		}

		ImageView iv = initplayerMarker("bodyMarker.png", false);
		Label labCity = new Label(player.playerLocation(), initplayerMarker("city.png", true));
		labCity.setTextFill(Color.WHITE);

		g.getChildren().add(iv);

		layout.getChildren().add(labCity);
		layout.getChildren().add(userHand);
		Button button = new Button("Action");

		button.setPrefWidth(500);
		button.setStyle("-fx-font: 23 arial; -fx-base: #FF0000;");
		gameStatus = ge.gameStatus();
		layout.getChildren().add(button);
		printSide.getChildren().add(0, printCardLay());

		// initialize infectionRate display
		this.infectionRateLabel = new Label("Infection Rate : " + ge.infectionRate(),initplayerMarker("infected.png", true));

		this.infectionRateLabel.setTranslateX(10);
		this.infectionRateLabel.setTranslateY(650);
		this.infectionRateLabel.setTextFill(Color.YELLOW);
		g.getChildren().add(infectionRateLabel);

		// initialize 
		
		setPlayerLoaction(iv, cities.get(player.playerLocation()));
		button.setOnAction(e -> {

			if (dset != null) {
				boolean print = false;
				for (int i = 0; i < player.playerHand().size(); i++) {

					if (!dset.contains(player.playerHand().get(i).toString())
							| ge.getBoard().getLastCitiesOutbroken() != null) {
						print = true;
						break;
					}

				}
				if (print) {

					printSide.getChildren().set(0, printCardLay());

					int i = 1;
					for (Label lab : printOutBreak()) {
						if (printSide.getChildren().size() > i) {
							if (printSide.getChildren().get(i) != null) {
								printSide.getChildren().set(i, lab);
							} else {
								printSide.getChildren().add(i, lab);
							}
						} else {
							printSide.getChildren().add(i, lab);
						}
						i += 1;
					}

				}
			}

			nbAction++;
			if (nbAction > 4) {
				if (gameStatus == GameStatus.ONGOING) {
					for (int j = 0; j < ge.getBoard().getInfectionRate(); ++j) {
						ge.getBoard().drawInfectorCard();
						gameStatus = ge.getBoard().updateGameStatus();
					}
					updateCitiesDiseases();

					try {
						Alert alert = new Alert(AlertType.CONFIRMATION);
						alert.setTitle("Hand Information ! ");
						alert.setHeaderText("Cards have been drawn !");
						ButtonType hand = new ButtonType("Print my hand ");
						ButtonType outB = new ButtonType("Show OutBreak !");
						ButtonType buttonCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
						alert.getButtonTypes().setAll(hand, outB, buttonCancel);
						alert.showAndWait();
						Optional<ButtonType> result = alert.showAndWait();
						ge.draw(player);
						ge.draw(player);
						updateCitiesDiseases();
						updateInfectionRate();
						if (result.get() == hand) {
							checkHand();
							printCard();
						}
						if (result.get() == outB) {
							if (ge.getBoard() != null) {

								addOutBreak();

							}
						}

					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

				}
				if (gameStatus == GameStatus.VICTORIOUS) {
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("You WIN !");
					alert.setHeaderText("Congratulation !");
					alert.showAndWait();

					layout.setVisible(false);
				}
				if (gameStatus == GameStatus.DEFEATED) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("You LOSE !");
					alert.setHeaderText(" (: !");
					alert.showAndWait();
					layout.setVisible(false);

				}

				gameStatus = ge.getBoard().updateGameStatus();
				nbAction = 0;

			}

			ChoiceDialog<PlayerAction> dialog = new ChoiceDialog<>(PlayerAction.CHARTERFLY, PlayerAction.values());
			dialog.setTitle("Let's go !");
			dialog.setHeaderText("Choice one action ! number of action use : " + nbAction);
			dialog.setContentText("Action :");

			PlayerAction pc = null;
			// get the response value.
			Optional<PlayerAction> result = dialog.showAndWait();
			if (result.isPresent()) {
				pc = result.get();
			}

			Button bt = new Button("Ok !");
			try {
				switch (pc) {

				case DIRECTFLY:

					String cit = "";

					for (PlayerCardInterface city : player.playerHand())
						cit += " " + city.getCityName();
					dialogDisplay("DIRECTFLY", cit);
					labCity.setText(player.playerLocation());
					setPlayerLoaction(iv, cities.get(player.playerLocation()));
					break;
				case CHARTERFLY:
					dialogDisplay("CHARTERFLY", " Anywhere");
					labCity.setText(player.playerLocation());
					setPlayerLoaction(iv, cities.get(player.playerLocation()));
					break;

				case CREATEVACCINE:

					if (player.playerHand().size() < 5) {
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error Dialog");
						alert.setHeaderText("Look, an Error Dialog");
						alert.setContentText(" you don't have the card !");
						alert.showAndWait();
					} else {
						MenuButton choices = new MenuButton("Choose 5 cards");
						List<CheckMenuItem> items = new ArrayList<>();
						for (PlayerCardInterface cd : player.playerHand()) {
							items.add((new CheckMenuItem(cd.getCityName())));
						}
						Dialog dialog3 = new Dialog<>();
						choices.getItems().addAll(items);
						List<String> selectedItems = new ArrayList<>();
						for (CheckMenuItem item : items) {
							item.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
								if (newValue) {

									selectedItems.add(item.getText());
								} else {
									selectedItems.remove(item.getText());
								}
							});
						}
						dialog3.getDialogPane().setContent(choices);
						dialog3.getDialogPane().lookupButton(ButtonType.CLOSE);
						Window window = dialog3.getDialogPane().getScene().getWindow();
						window.setOnCloseRequest(event -> window.hide());
						dialog3.showAndWait();
						try {
							List<PlayerCardInterface> pcard = new ArrayList<>();
							for (PlayerCardInterface p : player.playerHand()) {
								for (String select : selectedItems)
									if (p.getCityName().equals(select))
										pcard.add(p);

							}

							player.discoverCure(pcard);
						} catch (Exception e3) {
							nbAction--;
							e3.printStackTrace();
						}

					}
					break;
				case DISPLAYHAND:

					for (PlayerCardInterface p : player.playerHand()) {
						userHand = new Label(p.toString(), initplayerMarker("card.png", true));
						layout.getChildren().add(userHand);

					}

					break;

				case DRIVE:
					String cit2 = "";
					for (String city : ge.neighbours(player.playerLocation()))
						cit2 += " " + city;
					dialogDisplay("DRIVE", cit2);
					labCity.setText(player.playerLocation());
					setPlayerLoaction(iv, cities.get(player.playerLocation()));

					break;

				case PASS:
					nbAction = 4;
					break;

				case REMOVEDISEASE:
					TextInputDialog dialog4 = new TextInputDialog();

					dialog4.setTitle("Disease");
					dialog4.setHeaderText("Enter disease color: BLACK YELLOW RED BLUE");
					dialog4.setContentText("color:");

					Optional<String> res = dialog4.showAndWait();

					res.ifPresent(name -> {
						try {
							player.treatDisease(Disease.valueOf(name));
						} catch (Exception e1) {
							Alert alert = new Alert(AlertType.ERROR);
							alert.setTitle("Error Dialog");
							alert.setHeaderText("Look, an Error Dialog");
							alert.setContentText(" you don't have the card !");
							
							alert.showAndWait();
							nbAction--;
						}
					});

					break;

				case DISPLAYCITIES:
					ListView<String> list2 = new ListView<String>();
					ObservableList<String> items2 = FXCollections.observableArrayList(cities.keySet());
					list2.setItems(items2);
					list2.setPrefWidth(100);
					list2.setPrefHeight(70);

					break;

				case DISPLAYLOCATION:
					labCity.setText(player.playerLocation());
					break;

				default:

					break;

				}
			} catch (IllegalArgumentException ex) {
				System.out.println("Unauthorized action");
				System.out.println(ex);
				nbAction--;
			} catch (FileNotFoundException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
			// }
			updateCitiesDiseases();

		});

		BackgroundImage backgroundimage = new BackgroundImage(image, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(stage.getWidth(), stage.getHeight(), false, false, false, false));
		Background background = new Background(backgroundimage);
		leftSide.getChildren().add(layout);

		// List of countries
		ListView<String> list2 = new ListView<String>();
		ObservableList<String> items2 = FXCollections.observableArrayList(cities.keySet());
		list2.setItems(items2);
		list2.setPrefWidth(600);
		list2.setPrefHeight(stage.getHeight());
		layout.getChildren().add(list2);

		leftSide.setSpacing(10);
		root.setBackground(background);
		leftSide.getChildren().add(printSide);
		root.getChildren().add(g);
		root.getChildren().add(leftSide);

		stage.setScene(scene);
		stage.setTitle("ToxicTeam - Pandemiage");

		stage.show();

	}

	/**
	 * Create list of outBreak label
	 * 
	 * @return List of Labels
	 */
	private List<Label> printOutBreak() {
		VBox go = new VBox();
		List<Label> lab = new ArrayList<>();
		for (City c : ge.getBoard().getLastCitiesOutbroken()) {
			for (Disease d : c.getDiseases().keySet()) {
				if (c.getDiseases().get(d) > 0) {
					try {
						Label labD = new Label(d.toString() + " |  " + c.getDiseases().get(d) + " | : " + c.getLabel(),
								initplayerMarker("disease.png", true));
						labD.setTextFill(Color.YELLOWGREEN);
						lab.add(labD);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
		return lab;

	}

	/**
	 * Display dialog interface with move actions
	 * 
	 * @param s
	 *            string action name
	 * @param cit
	 *            information about the city
	 */
	private void dialogDisplay(String s, String cit) {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle(s);
		dialog.setHeaderText("Choose : " + cit);
		dialog.setContentText("City:");
		String res = null;
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			res = result.get();
		}
		try {
			if (s.equals("DRIVE")) {
				player.moveTo(res);
			}
			if (s.equals("CHARTERFLY")) {
				player.flyToCharter(res);
			}
			if (s.equals("DIRECTFLY")) {
				player.flyTo(res);
			}
		} catch (Exception e1) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error Dialog");
			alert.setHeaderText("Please try again");
			alert.showAndWait();
			nbAction--;

		}

	}

	/**
	 * Create Box with all hand's card
	 * 
	 * @return VBOX
	 */
	public VBox printCardLay() {

		VBox g = new VBox();
		for (PlayerCardInterface pc : player.playerHand()) {
			dset.add(pc.toString());
		}

		ObservableList<String> items = FXCollections.observableArrayList(dset.descendingSet());
		for (PlayerCardInterface p : player.playerHand()) {
			try {
				userHand = new Label(p.toString(), initplayerMarker("card.png", true));
				userHand.setTextFill(Color.WHITE);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			g.getChildren().add(userHand);
		}
		return g;

	}

	/**
	 * Create ListView of cards and show it on dialog interface
	 */
	private void printCard() {
		ListView<String> listView = new ListView<String>();
		TreeSet<String> dset2 = new TreeSet<String>();
		for (PlayerCardInterface pc : player.playerHand()) {
			dset2.add(pc.toString());
		}

		ObservableList<String> items = FXCollections.observableArrayList(dset2.descendingSet());
		listView.setItems(items);
		listView.setCellFactory(param -> new ListCell<String>() {
			@Override
			public void updateItem(String name, boolean empty) {
				super.updateItem(name, empty);
				setText(name);
				try {
					setGraphic(initplayerMarker("card.png", true));
				} catch (FileNotFoundException e) {
 					e.printStackTrace();
				}
			}
		});
		Alert alert = new Alert(AlertType.WARNING);
		alert.setTitle("Your hand !");
		alert.setHeaderText("Information ");
		alert.setGraphic(listView);

		alert.showAndWait();

	}

	/**
	 * Create ListView and show it in Dialog Interface
	 */
	private void addOutBreak() {
		ListView<String> listView = new ListView<String>();
		TreeSet<String> dset = new TreeSet<String>();
		for (City c : ge.getBoard().getLastCitiesOutbroken()) {
			for (Disease d : c.getDiseases().keySet()) {
				if (c.getDiseases().get(d) > 0) {
					dset.add(d.toString() + " |  " + c.getDiseases().get(d) + " | : " + c.getLabel());

				}
			}
		}

		ObservableList<String> items = FXCollections.observableArrayList(dset.descendingSet());
		listView.setItems(items);
		listView.setCellFactory(param -> new ListCell<String>() {
			@Override
			public void updateItem(String name, boolean empty) {
				super.updateItem(name, empty);
				setText(name);
				try {
					setGraphic(initplayerMarker("disease.png", true));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});

		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("Outbreak !");
		alert.setHeaderText("Information Alert");
		alert.setGraphic(listView);

		alert.showAndWait();

	}

	/**
	 * Check if the size of the hand repect the rule
	 */
	private void checkHand() {

		while (player.playerHand().size() > 9) {
			ChoiceDialog<PlayerCardInterface> cdialog = new ChoiceDialog<>(player.playerHand().get(0),
					player.playerHand());
			cdialog.setTitle("Choose card that you want to remove");
			cdialog.setHeaderText("you need to remove : " + (player.playerHand().size() - 9) + " card ");
			cdialog.setContentText("Card name :");

			// Traditional way to get the response value.
			Optional<PlayerCardInterface> result = cdialog.showAndWait();
			if (result.isPresent()) {
				player.playerHand().remove(result.get());
				System.out.println("test " + player.playerHand());
			}
		}
	}

	/**
	 * Initialize an given image
	 *
	 *
	 * @param path
	 * @param val
	 * @return
	 * @throws FileNotFoundException
	 */
	public ImageView initplayerMarker(String path, boolean val) throws FileNotFoundException {
		Image image = new Image(new FileInputStream(path));
		ImageView imageView = new ImageView(image);
		if (val) {
			imageView.setFitHeight(45);
			imageView.setFitWidth(45);
		} else {
			imageView.setFitHeight(25);
			imageView.setFitWidth(25);
		}

		return imageView;
	}

	public ArrayList<Circle> setColor(List<PlayerCardInterface> ph) {
		ArrayList<Circle> list = new ArrayList<>();

		for (PlayerCardInterface card : ph) {
			Circle c = new Circle(cities.get(card.getCityName()).getX() / scale + xOrigin,
					yOrigin - cities.get(card.getCityName()).getY() / scale, 5);
			switch (card.getDisease()) {
			case BLACK:
				c.setFill(Color.BLACK);
				break;

			case RED:
				c.setFill(Color.RED);
				break;
			case BLUE:
				c.setFill(Color.BLUE);
				break;

			case YELLOW:
				c.setFill(Color.YELLOW);
				break;
			default:
				break;
			}
			list.add(c);
		}

		return list;
	}

	public ImageView setPlayerLoaction(ImageView iv, City city) throws FileNotFoundException {

		if (city == null) {
			iv.setX((-789.55255 / scale) + xOrigin);
			iv.setY(yOrigin - (824.45905 / scale));

		} else {
			iv.setX((city.getX() / scale) + xOrigin);
			iv.setY(yOrigin - (city.getY() / scale));

		}

		return iv;

	}

	/**
	 * update the number of Diseases by city displayed
	 */
	public void updateCitiesDiseases() {
		for (String city : ge.allCityNames()) {
			String diseasesValue = "\n";
			for (Disease d : Disease.values()) {
				if (ge.infectionLevel(city, d) > 0) {
					diseasesValue += d.toString().substring(0, 1) + ge.infectionLevel(city, d) + " ";
				}
			}
			this.labelByCity.get(city).setText(city + diseasesValue);

		}
	}

	/**
	 * update the infectionRate Displayed
	 */
	public void updateInfectionRate() {
		this.infectionRateLabel.setText("Infection Rate : " + ge.infectionRate());
		this.infectionRateLabel.setTextFill(Color.ROSYBROWN);
	}

}