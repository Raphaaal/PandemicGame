# PandemicGame
GUI and multiple symbolic AI to play the Pandemic game.

Project contributors:
TRAN Stanislas
LAMRANI ALAOUI Ayoub
AZORIN Raphaël

Package project:
Run ​ mvn package ​ at the root of the project.

Launch project (GUI):
After having packaged the project, run ​ mvn exec:java ​

Launch project (CLI):
Run ​ java -jar pandemiage-1.0-SNAPSHOT-jar-with-dependencies.jar ​ for default options.

Options:

-a JARFILE -d DIFFICULTY -g CITYGRAPH -t TURNDURATION -s HANDSIZE

JARFILE :
path to a .jar file containing at least one class that implements
fr.dauphine.ja.pandemiage.common.AiInterface and a MANIFEST files containing the AI-Class
property specifying the name of the class implementing AiInterface. Default : DynamicAi.jar.

DIFFICULTY :
level of game difficulty : 0 - Introduction game, 1 - Standard game, 2 - Heroic Game. Default : 0.

CITYGRAPH :
name of the file containing the graph of citie. Default : pandemic.graphml

TURNDURATION :
number of seconds representing the allocated time for each player to play his turn. Default : 1.

HANDSIZE :
maximum number of cards a player can hold. Default : 9.

Documentation (in French):
Refer to ​ user.pdf ​ , ​ dev.pdf ​ and experimentation.pdf ​ located in the ​ docs ​ directory.
