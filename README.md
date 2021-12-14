# Computer Science Learning Game (java)
![image](https://user-images.githubusercontent.com/71548980/146038567-70d414d1-ab02-4a21-802f-7a6e116d9a6d.png)

The goal of our game is to allow computer science students practice for their exam period, and to strengthen their knowledge any course they want to.

We present a data base for the following subjects:
Algorithms
Operating Systems
Computer structure
Communication networks

Game of American question and a few answers, that includes lives and score.
In our game the player can use our DB or if he is an admin, he can add some question on his own.
When admin insert a question, he need to provide few things: 
 	- The question
	- The wrong answers that will be present in a sections
	- The right answer
• Admin can also remove question from the DB.
• Player can only practice in the game if he has lives.
• Our system save a cache for the current subject and in the end of the iteration it update the information in the disk so it will save.

![image](https://user-images.githubusercontent.com/71548980/146038834-afc08ef6-fdee-4575-9afe-90f6aebe4f93.png)

![image](https://user-images.githubusercontent.com/71548980/146038866-a66e3af8-fb7d-4eae-85e4-59d5fe801084.png)

##Where to start?
1. Add the jar from here:
"https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc/3.36.0.3"
to the project dependencies in File->Project structure-> Add

2. Download DB Browser from here:
https://sqlitebrowser.org/blog/version-3-12-2-released/
according to your computer.

Now you can open the file "Test.db" that the project create and uses for the game in DB browser :)

Add massage to the player - if the player want to delete a question it will refresh the game

###Enjoy your learning!
