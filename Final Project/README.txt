This stylometric analysis tool using Sci-kit learn was my senior project for Westminster college, 2017. Below is documentation for how to use it

-The folders labeled “author” and “not author” are the folders the program uses to read text.
-The files in these folders must be utf-8 format or the program will not work. 
-once you have the text you want classified, you must alter the paths on lines 18 and 26 for your paths on your computer.
-the text you want to classify has to be put into the file “docinquestion” 
-the classifier is currently formatted to classify lines, not entire documents, so keep that in mind. 
-I designated the output as 0 for the “author” class and 1 for the “not author class” so if the computer outputs a 1, it’s guessing the “not author” class for the document you want it to classify. 
-you must also provide the Sci-kit learn python package if you want the program to work. 
-for an example of the classifier working, I’ve included 2 tests labeled “Shakespeare vs Marlowe” and “Mommsen vs Kittiara”. If you wish to see it in action without providing your own text to classify, simply copy the files from the folders to the respective “not author” or “author” folders, and then cut some text out of whatever file you want to have classified into the “docinquestion” file and hit the “run program” button. 

Have…fun?