/*
 * File: Yahtzee.java
 * ------------------
 * Implementation of Yahtzee
 * 
 * Created by Zalan Khan, Finished July 14, 2014
 * 
 * Assignment # 5 of Stanford CS 106A.
 * 
 * Starter file which includes the graphical canvas  and some logic used
 * from hand-out.
 */
 

import acm.io.*;
import acm.program.*;
import acm.util.*;

import java.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	public static void main(String[] args) {
		new Yahtzee().start(args);
	}
	
	public void init() {
		setSize(600, 350);
	}
	
	public void run() {
		setup();
		play();
	}
	
	/* Sets the graphical display.
	 * Asks for number of players, and their names.
	 */
	private void setup() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
	}

	private void play() {
		int numOfRounds = N_SCORING_CATEGORIES;
		
		for(int i = 0; i < numOfRounds; i++) {
			playerTurn = 1; //The round starts off with player 1 going first.
			playRound();
		}
		
		playerTurn = 1;
		
		//Displays all the scores for each player
		for(int i = 0; i < nPlayers; i++){ 
			displayScores();
			if(playerTurn < nPlayers) {
				playerTurn = playerTurn + 1; 
			}
		}
		displayWinner();
	}
	
	/* The program must do the initial roll and re-roll twice even if no dice
	 * are selected. After this, the user clicks one category and gets an
	 * appropriate score.
	 */
	private void playRound() {
		
		for (int i = 0; i < nPlayers; i++) {
			doFirstRoll();
			doSecondRoll();
			doThirdRoll();
			checkCategory();
			
			if(playerTurn < nPlayers) {
				playerTurn = playerTurn + 1; //Next player's turn
			}
		}
	}
	
	/* The program waits for the player to click on the "Roll Dice" button, which
	 * rolls all the dice, and assigns them a random number from 1 to 6.
	 */
	private void doFirstRoll() {
	 	display.printMessage(" " + playerNames[playerTurn - 1] + "'s turn! Click Roll Dice to start.");
	 	
		for(int i = 0; i < N_DICE; i++) {
			dice[i] = rgen.nextInt(1, 6); //Gives each die a value between 1 and 6 inclusively
		}
		display.waitForPlayerToClickRoll(playerTurn);
		display.displayDice(dice);
	}
	
	private void doSecondRoll() {
		displayMsg();
		rerollDice();
	}
	
	private void doThirdRoll() {
		displayMsg();
		rerollDice();
	}
	
	private void displayMsg() {
		display.printMessage("Select the dice you wish to re-roll and click Roll Again.");
	}
	
	//The selected dice are re-rolled and given new values.
	private void rerollDice() {
	 	display.waitForPlayerToSelectDice();
	 
	 	for(int i = 0; i < N_DICE; i++) {
	 		if (display.isDieSelected(i)){
	 			dice[i] = rgen.nextInt(1, 6);
	 		}
	 	}
	 	display.displayDice(dice);
	}
	
	/* The user waits for the player to click a category.
	 * Depending on the category and the dice values, they are assigned a score
	 * in that category.
	 */
	private void checkCategory() {
		
		display.printMessage("Select the category you wish for an appropriate score.");
		
		//Tells us which category the user selected.
		category = display.waitForPlayerToSelectCategory(); 
		
		/* This essentially checks if you've selected a category which you've selected
		 * in a previous round. It makes sure that the user selects a new
		 * category.
		 */
		while(true) {
			if(categoryChker[playerTurn][category] == 0) {
				break;
			}
			else {
				display.printMessage("You've already selected this category. Select another category.");
				category = display.waitForPlayerToSelectCategory(); 
			}
		}
		
		//The score is equal to the sum of the 1's, 2’s, 3’s and so on, showing on the dice.
		checkOneToSix();
		
		//At least three of the dice must show the same value.
		checkThreeOfAKind();
		
		// At least four of the dice must show the same value.
		checkFourOfAKind();
		
		// The dice must show three of one value and two of another value.
		checkFullHouse();
		
		// The dice must contain at least four consecutive values
		checkSmallStraight();
		
		// The dice must contain five consecutive values
		checkLargeStraight();
		
		// All of the dice must show the same value
		checkYahtzee();
		
		// The score is equal to the sum of all of the values showing on the dice. 
		checkChance(); 
		
	}
	
	private void checkOneToSix() {
		
		/* Remember that each category from one to six, has a value of 1 to 6
		 * respectively.
		 * If the category clicked is from one to six, go through all the dice
		 * values, and if those dice values are equal to the category value,
		 * add them to the score.
		 */
		if(category >= ONES && category <= SIXES) {
			
			for(int i = 0; i < N_DICE; i++){
				if(dice[i] == category) {
					score = score + category;
				}
			}
			display.updateScorecard(category, playerTurn, score);
			
			/* Remember the upper score is actually from index 0 to 3 and not
			 * index 1 to 4. 
			 * */
		 	upperScore[playerTurn - 1] = upperScore[playerTurn - 1] + score;
		 	score = 0; //Score should be reset, so it can be used for other categories
		 	
		 	/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
		 	categoryChker[playerTurn][category] = -1; 
		}
	}
	
	/* For every die, compare with the other four dice values.
	 * Example: die 1 will compare with dice 2, 3, 4, 5.
	 * Die 2 will compare with dice 1, 3, 4, 5.
	 * This problem is solved using two for loops.
	 * 
	 * The counter tells us how many dice have the same value. When the counter
	 * reaches an appropriate value, it'll break out of the loop and put
	 * the score to three times the value used of the appropriate dice value.
	 * 
	 * If we didn't break out of the loop, the loop would show repetitive results.
	 */
	private void checkThreeOfAKind() {
		int counter = 0; //Counts to make sure there are three dice with the same value.
		
		if(category == THREE_OF_A_KIND ) {
			
			for(int i = 0; i < N_DICE; i++) {
				counter = 0; //Need to reset counter to zero for each loop
				for(int j = 0; j < N_DICE; j++) {
					
					/* The counter should not count for the same dice value. (i != j) */
					if((dice[i] == dice[j]) && (i != j)) { 
						counter = counter + 1;
						if(counter == 2) { 
							score = dice[i] * 3;
							break;
						}
					}
				}
		
				/* When the counter reaches two, there are three dice that have the
				 * same value.
				 */
				if(counter == 2) {
					break;
				}
			}
			display.updateScorecard(category, playerTurn, score);
			lowerScore[playerTurn - 1] = lowerScore[playerTurn - 1] + score;
			score = 0; //Score should be reset, so it can be used for other categories
			
			/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
			categoryChker[playerTurn][category] = -1;
		}
	}
	
	//Similar process to checkThreeOfAKind
	private void checkFourOfAKind() {
		int counter = 0; //Counts to make sure there are four dice with the same value.
		
		if(category == FOUR_OF_A_KIND ) {
			
			for(int i = 0; i < N_DICE; i++) {
				counter = 0; //Need to reset counter to zero for each loop
				for(int j = 0; j < N_DICE; j++) {
					
					/* The counter should not count for the same dice value. (i != j) */
					if((dice[i] == dice[j]) && (i != j)) {
						counter = counter + 1;
						if(counter == 3) {
							score = dice[i] * 4;
							break;//Problem was solved, break out of loop.
						}
					}
				}
				if(counter == 3) { 
					break; //Stops checking since four of a kind was found. Don't need to check repetitive answers.
				}
			}
			display.updateScorecard(category, playerTurn, score);
			lowerScore[playerTurn - 1] = lowerScore[playerTurn - 1] + score;
			score = 0; //Score should be reset, so it can be used for other categories
			
			/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
			categoryChker[playerTurn][category] = -1;
		}
	}
	
	/* Essentially, we create two sorting loops to help show three of 
	 * one value and two of another value.
	 * When the counters reach an appropriate value, a score of 25 is put in place.
	 */
	private void checkFullHouse() {
		int doubleCounter = 0; //Counts to make sure there are two dice with the same value.
		int tripleCounter = 0; //Counts to make sure there are three dice with the same value.
		int temp = 0; //Holds the three of a kind value.
		
		if(category == FULL_HOUSE) {
			
			for(int i = 0; i < N_DICE; i++) {
				
				for(int j = 0; j < N_DICE; j++) {
					
					if((dice[i] == dice[j]) && (i != j)) {
						tripleCounter = tripleCounter + 1;
						if(tripleCounter == 2) {
							temp = dice[i];
							break;
						}
					}
				}
				if(tripleCounter == 2 ) {  //The problem was solved, no need to do repetitive loops
					break;
				}
			}
			
			for(int i = 0; i < N_DICE; i++) {
				
				for(int j = 0; j < N_DICE; j++) {
					
					if((temp != dice[i]) && (dice[i] == dice[j]) && (i != j)) {
						doubleCounter = doubleCounter + 1;
						
						if(doubleCounter == 1 ) { 
							break;
						}
					}
				}
				if(doubleCounter == 1 ) { 
					break;
				}
			}
			if(tripleCounter == 2 && doubleCounter == 1) {
				score = 25;	
			}
			display.updateScorecard(category, playerTurn, score);
			lowerScore[playerTurn - 1] = lowerScore[playerTurn - 1] + score;
			score = 0; //Score should be reset, so it can be used for other categories
			
			/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
			categoryChker[playerTurn][category] = -1;
		}
	}
	
	/* There are three possible combinations to create a small straight with
	 * five dice which have values from 1 to 6.
	 * Case 1: 1, 2, 3, 4
	 * Case 2: 2, 3, 4, 5
	 * Case 3: 3, 4, 5, 6
	 * The five dice must contain all the values for any case to be a small
	 * straight.
	 */
	private void checkSmallStraight() {
		
		 //Creates an array list for each dice value.
        ArrayList <Integer> numOfOnes = new ArrayList <Integer> ();
        ArrayList <Integer> numOfTwos = new ArrayList <Integer> ();
        ArrayList <Integer> numOfThrees = new ArrayList <Integer> ();
        ArrayList <Integer> numOfFours = new ArrayList <Integer> ();
        ArrayList <Integer> numOfFives = new ArrayList <Integer> ();
        ArrayList <Integer> numOfSixes = new ArrayList <Integer> ();
        
        //Adds the values rolled to the array list.
        for(int i = 0; i < N_DICE; i++) {
            if(dice[i] == 1) {
                numOfOnes.add(1);
            }
            else if(dice[i] == 2) {
                numOfTwos.add(1);
            }
            else if(dice[i] == 3) {
                numOfThrees.add(1);
            }
            else if(dice[i] == 4) {
                numOfFours.add(1);
            }
            else if(dice[i] == 5) {
                numOfFives.add(1);
            }
            else if(dice[i] == 6) {
                numOfSixes.add(1);
            }
        }

		if(category == SMALL_STRAIGHT) {
			
			//checks case 1
			if(numOfOnes.size() >= 1 && numOfTwos.size() >= 1 
					&& numOfThrees.size() >= 1 && numOfFours.size() >= 1) {
				score = 30;	
			}
			
			//checks case 2
			if(numOfTwos.size() >= 1 && numOfThrees.size() >= 1
					&& numOfFours.size() >= 1 && numOfFives.size() >= 1) {
				score = 30;
			}
			
			//checks case 3:
			if(numOfThrees.size() >= 1 && numOfFours.size() >= 1
					&& numOfFives.size() >= 1 && numOfSixes.size() >= 1) {
				score = 30;
			}	
				
			display.updateScorecard(category, playerTurn, score);
			lowerScore[playerTurn - 1] = lowerScore[playerTurn - 1] + score;
			score = 0; //Score should be reset, so it can be used for other categories
			
			/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
			categoryChker[playerTurn][category] = -1;
		}
	}
	 
	/* There are two possible combinations to create a large straight with
	 * five dice which have values from 1 to 6.
	 * Case 1: 1, 2, 3, 4, 5
	 * Case 2: 2, 3, 4, 5, 6
	 * The five dice must contain all the values for any case to be a large
	 * straight.
	 */
	private void checkLargeStraight() {
		
		 //Creates an array list for each dice value.
        ArrayList <Integer> numOfOnes = new ArrayList <Integer> ();
        ArrayList <Integer> numOfTwos = new ArrayList <Integer> ();
        ArrayList <Integer> numOfThrees = new ArrayList <Integer> ();
        ArrayList <Integer> numOfFours = new ArrayList <Integer> ();
        ArrayList <Integer> numOfFives = new ArrayList <Integer> ();
        ArrayList <Integer> numOfSixes = new ArrayList <Integer> ();
        
        //Adds the values rolled to the array list.
        for(int i = 0; i < N_DICE; i++) {
            if(dice[i] == 1) {
                numOfOnes.add(1);
            }
            else if(dice[i] == 2) {
                numOfTwos.add(1);
            }
            else if(dice[i] == 3) {
                numOfThrees.add(1);
            }
            else if(dice[i] == 4) {
                numOfFours.add(1);
            }
            else if(dice[i] == 5) {
                numOfFives.add(1);
            }
            else if(dice[i] == 6) {
                numOfSixes.add(1);
            }
        }
		
		if(category == LARGE_STRAIGHT) {
			
			//checks case 1
			if(numOfOnes.size() >= 1 && numOfTwos.size() >= 1 && numOfThrees.size() >= 1 
					&& numOfFours.size() >= 1 && numOfFives.size() >= 1) {
				score = 40;	
			}
			
			//checks case 2
			if(numOfTwos.size() >= 1 && numOfThrees.size() >= 1 && numOfFours.size() >= 1
					&& numOfFives.size() >= 1 && numOfSixes.size() >= 1) {
				score = 40;
			}
		
			display.updateScorecard(category, playerTurn, score);
			lowerScore[playerTurn - 1] = lowerScore[playerTurn - 1] + score;
			score = 0; //Score should be reset, so it can be used for other categories
			
			/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
			categoryChker[playerTurn][category] = -1;
		}
	}
	
	
	//Similar process to checkThreeOfAKind
	private void checkYahtzee() {
		int counter = 0; //Counts to make sure there are five dice with the same value.
		
		if(category == YAHTZEE ) {
		
			
			for(int i = 0; i < N_DICE; i++) {
				counter = 0; //Need to reset counter to zero for each loop
				
				for(int j = 0; j < N_DICE; j++) {
					if((dice[i] == dice[j]) && (i != j)) {
						counter = counter + 1;
						if(counter == 4) {
							score = 50;
							break;
						}
					}
				}
				if(counter == 4) {
					break;
				}
			}
			display.updateScorecard(category, playerTurn, score);
			lowerScore[playerTurn - 1] = lowerScore[playerTurn - 1] + score;
			score = 0; //Score should be reset, so it can be used for other categories
			
			/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
			categoryChker[playerTurn][category] = -1;
		}
	}
	
	/* The loop goes through the five dice values and adds them up.  */
	private void checkChance() {
	
		if(category == CHANCE) {
			
			for(int i = 0; i < N_DICE; i++){
				score = score + dice[i];
			}
			
			display.updateScorecard(category, playerTurn, score);
			lowerScore[playerTurn - 1] = lowerScore[playerTurn - 1] + score;
			score = 0; //Score should be reset, so it can be used for other categories
			
			/* Score for this category has been entered already, it should
		 	 * not be accessed again.
		 	 */
			categoryChker[playerTurn][category] = -1;
		}
	}
	
	private void displayScores() {
		
		//Displays upper score.
		display.updateScorecard(UPPER_SCORE, playerTurn, upperScore[playerTurn - 1]);
		
		//Displays upper bonus score.
		if(upperScore[playerTurn - 1] > 63) {
			upperBonus[playerTurn - 1] = 35;
		}
		
		display.updateScorecard(UPPER_BONUS, playerTurn, upperBonus[playerTurn - 1]);
		
		//Displays lower score.
		display.updateScorecard(LOWER_SCORE, playerTurn, lowerScore[playerTurn - 1]);
		
		//Displays total score.
		totalScore[playerTurn - 1] = upperScore[playerTurn - 1] + upperBonus[playerTurn - 1] + lowerScore[playerTurn - 1];
		display.updateScorecard(TOTAL, playerTurn, totalScore[playerTurn - 1]);
	}
	
	private void displayWinner() {
		int indexOfWinner = 0; //Index starts off at 0
		int highestScore = 0;
		
		for(int i = 0; i < nPlayers; i++) {
			if(totalScore[i] > highestScore) {
				indexOfWinner = i;
				highestScore =  totalScore[i];
			}
		}
		display.printMessage(" " + playerNames[indexOfWinner] + " has won the game!");
	}
	
/* Private instance variables */
	
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	
	private int nPlayers;
	private int playerTurn = 1; //Starts the program off at player 1.
	private int category;
	private int score; 
	
	private String[] playerNames;
	private int[] dice = new int[5];
	private int[] lowerScore = new int[4]; //Stores each player's lower score.
	private int[] upperScore = new int[4]; //Stores each player's upper score.
	private int[] upperBonus = new int[4]; //Stores each player's upper bonus.
	private int[] totalScore = new int[4]; //Stores each player's total score.
	private int[][] categoryChker = new int[5][17];
}