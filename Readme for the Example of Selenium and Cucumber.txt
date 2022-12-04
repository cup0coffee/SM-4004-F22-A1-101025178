 
	
HOW TO RUN:

	1. See the readme inside the folder for instructions on importing in eclipse, running tests,
	etc. This project uses maven, I have tried to be as clear as possible with how to run it with no problems. I tested on Eclipse and on Intellij, so no Eclipse issues should happen.
	2. TEST OUTPUT IS NOT HARDCODED TO OUTPUT TO A FILE. This is silly - there are multiple ways to
	output to a file. Use redirection if needed, or use maven's log ability. Again, see the other 
	readme for information. Thanks.
	
ASSIGNMENT 3 SPECIFICS:

	1. The cucumber step defs are located in the src/test/java/cucumber directory.
	2. To add more tests... well, you should know.
	
REQUIREMENTS (split between this and selenium):

	NON-FUNCTIONAL REQUIREMENT: The ability to SPLIT your hand - currently does nothing except waste your turn.
	
	REQUIREMENTS:
	
	REQ-#1 - Support seven card charlie
	REQ-#2 - Support no winners if everyone busts
	REQ-#3 - Support a winner
	REQ-#4 - Support winner that has lowest amount of cards when tied.
	
	REQ-#5 - Support various AI logic scenarios. 
	REQ-#6 - Support various dealer scenarios.
	
	TRACEABILITY:
	
	ai.feature --> REQ-#5
	dealer.feature --> REQ-#6
	wincondition.feature --> REQ-#1, REQ-#2, REQ-#3, REQ-#4
	
	DETAILED:
	
	AI.FEATURE
	
	REQ-#5.1 - AI should split given two initial cards of the same rank
	REQ-#5.2 - AI should stay with a card value of 21
	REQ-#5.3 - AI should hit if another play has stayed with cards visible being value 10
	REQ-#5.4 - AI should hit if their value is between 18 and 20 and another player card value - 10
	REQ-#5.5 - AI should stay if value is between 18-20
	REQ-#5.6 - AI should hit in all other cases
	
	REQ-#5.1 - Scenario: AI should split given two initial cards of the same rank
	REQ-#5.2 - Scenario: AI should stay with a card value of 21
	REQ-#5.3 - Scenario: AI should hit if another player has stayed with two cards with the visible one being a 10
	REQ-#5.4 - Scenario: AI should hit if their value is between 18 and 20 and another player has visible card value greater than AI's minus 10
	REQ-#5.5 - Scenario: AI should stay if their value is between 18 and 20 and another player's card value is not greater than AI's minus 10
	REQ-#5.6 - Scenario: AI should hit in other cases
 
	DEALER.FEATURE
	
	REQ-#6.1 - Dealer should always hit if his hand value is less than 17
	REQ-#6.2 - Dealer should stay if their hand value totals 17 and they do not have an ace
	REQ-#6.3 - Dealer should hit if their hand value totals 17 and they have an ace
	REQ-#6.4 - Dealer should hit in all other scenarios
	REQ-#6.5 - Dealer should hit in all other scenarios
	
	REQ-#6.1 - Scenario: Dealer should always hit if his hand value is less than 17
	REQ-#6.2 - Scenario: Dealer should stay if their hand value totals 17 and they do not have an ace
	REQ-#6.3 - Scenario: Dealer should hit if their hand value totals 17 and they have an ace
	REQ-#6.4 - Scenario: Dealer should hit in all other scenarios
	REQ-#6.5 - Scenario: Dealer should hit in all other scenarios
	
	WINCONDITION.FEATURE
	
	REQ-#1 - Scenario: Player immediately wins if they have 7 cards without busting
	REQ-#4 - Scenario: When players tie with scores, the player lowest amount of cards is the winner
	REQ-#3 - Scenario: Player with highest score that didn't bust wins
	REQ-#2 - Scenario: No winners are possible when everyone busts