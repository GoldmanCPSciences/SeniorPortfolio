from __future__ import division #keep division with trailing digits
import string
import glob
import os
import math

class DocumentAnalzyer:
    exclude = set(string.punctuation)
    #letters = ['l', 'b', 'd', 'f', 'h', 'k', 'm', 'n', 'p', 'r', 'v']
    #allowed_error = .1

    #-------------------------------------------------------------------------------------------------------------------
    #Object oriented because: Why not?
    #-------------------------------------------------------------------------------------------------------------------

    def __init__(self):
        self.dictionaryTarget = {}
        self.dictionaryNonTarget = {} #duplicates for non authors corpus of documents
        self.dictionaryFrequency = {} #counting how many documents that word appears in
        self.dictionaryFrequencyNonTarget = {}
        self.docinquestionset = set()
        #get master array for TF-IDF values associated with the corpus.
        self.NoOfDocs = 0
        self.NoOfDocsNonTarget = 0
        #self.lettercount = {}
        #self.lettercountnon = {}
        self.totalletters = 0
        self.totallettersnon = 0
        #self.periodcount = 0
        #self.periodcountnon = 0
        self.wordcount = 0
        self.wordcountnon = 0
        self.wordcountdocinquestion = 0
        self.path = ""
        self.total = 1
        self.totalnon = 1
        self.probability = 0


    def fileprocessing(self):

    #-------------------------------------------------------------------------------------------------------------------
    #Process all of the corpus for "the author's work"
    #-------------------------------------------------------------------------------------------------------------------

        path = '/Users/talon/PycharmProjects/test/Author/'
        for filename in glob.glob(os.path.join(path, '*')):
            with open(filename, 'r') as f:
                for line in f:
                    #self.periodcount = self.periodcount + line.count('.') #look for periods, get count

                    line = ''.join(i for i in line if not i.isdigit()) #prune digits out
                    line = ''.join(ch for ch in line if ch not in self.exclude) #prune out punctuation
                    line = line.lower() # convert to lowercase so we don't get capitalized duplicates
                    for word in line.split(): #get all the words
                        if word not in self.dictionaryTarget:  # populate dynamic dictionary
                            self.dictionaryTarget[word] = 1
                            if word not in self.dictionaryFrequency:
                                self.dictionaryFrequency[word] = 1 #get number of documents word appears in
                            else:
                                self.dictionaryFrequency[word] += 1
                        else:
                            self.dictionaryTarget[word] += 1

                        self.wordcount = self.wordcount + 1

            self.NoOfDocs += 1

    #-------------------------------------------------------------------------------------------------------------------
    #Process all of the corpus for "not the author's work"
    #-------------------------------------------------------------------------------------------------------------------

        path = '/Users/talon/PycharmProjects/test/NotAuthor/'
        for filename in glob.glob(os.path.join(path, '*')):
            with open(filename, 'r') as f:
                for line in f:
                    #self.periodcountnon = self.periodcountnon + line.count('.')

                    line = ''.join(i for i in line if not i.isdigit())
                    line = ''.join(ch for ch in line if ch not in self.exclude)
                    line = line.lower()
                    for word in line.split():
                        if word not in self.dictionaryNonTarget:
                            self.dictionaryNonTarget[word] = 1
                            if word not in self.dictionaryFrequency:
                                self.dictionaryFrequencyNonTarget[word] = 1
                            else:
                                self.dictionaryFrequencyNonTarget[word] += 1
                        else:
                            self.dictionaryNonTarget[word] += 1

                        self.wordcountnon = self.wordcountnon + 1

            self.NoOfDocsNonTarget += 1

    #-------------------------------------------------------------------------------------------------------------------
    #Process entropy for each word: TF-IDF
    #-------------------------------------------------------------------------------------------------------------------

        for word in self.dictionaryTarget:
            self.dictionaryTarget[word] = self.dictionaryTarget[word] * math.log(
                self.NoOfDocs/self.dictionaryFrequency[word], 10)

        for word in self.dictionaryNonTarget:
            self.dictionaryNonTarget[word] = self.dictionaryNonTarget[word] * math.log(
                self.NoOfDocsNonTarget / self.dictionaryFrequencyNonTarget[word], 10)

    #-------------------------------------------------------------------------------------------------------------------
    #Process document to be classified
    #-------------------------------------------------------------------------------------------------------------------

        with open("docinquestion", 'r') as f:
            for line in f:
                #self.periodcount = self.periodcount + line.count('.')

                line = ''.join(i for i in line if not i.isdigit())
                line = ''.join(ch for ch in line if ch not in self.exclude)
                line = line.lower()
                for word in line.split():
                    self.docinquestionset.add(word)
                    self.wordcountdocinquestion += 1

    #-------------------------------------------------------------------------------------------------------------------
    #Final calculation for both corpuses: Naive Bayes.
    #-------------------------------------------------------------------------------------------------------------------


        for word in self.docinquestionset:
            if word in self.dictionaryTarget and word in self.dictionaryFrequency:
                self.total = self.total * ((self.dictionaryTarget.get(word)+1)/(
                self.dictionaryFrequency.get(word)+len(self.dictionaryTarget)))

            if word in self.dictionaryNonTarget and word in self.dictionaryFrequencyNonTarget:
                self.totalnon = self.totalnon * ((self.dictionaryNonTarget.get(word)+1) / (
                self.dictionaryFrequencyNonTarget.get(word) + len(self.dictionaryNonTarget)))

        #And the final answer isssss.....

        self.probability = self.totalnon - self.total

        print self.total
        print self.totalnon

    #-------------------------------------------------------------------------------------------------------------------
    #Previous approach using an english professor's logic about how to classify documents
    #-------------------------------------------------------------------------------------------------------------------

    #for letter in word:
    #    if letter in self.letters: #count number of specific letters
    #        if letter not in self.lettercount:
    #           self.lettercount[letter] = 1
    #        else:
    #            self.lettercount[letter] += 1
    #    self.totalletters += 1

    #def calculations(self,other):
    #   DocumentAnalzyer = other
    #   for key, value in self.lettercount.iteritems():
    #       for key2, value2 in other.lettercount.iteritems():
    #           if key == key2:
    #               if round(abs(self.lettercount[key]/self.totalletters -
    #                                           other.lettercount[key2]/other.totalletters),3) \
    #                                          <= self.allowed_error: #check for letter frequency
    #                  print "both documents have similar frequencies of letters:", key
    #              else:
    #                  print "the letter", key, "occurs", round(abs(self.lettercount[key]/self.totalletters
    #                          - other.lettercount[key2]/other.totalletters),3)*100, "% as frequently"
    #          else:
    #              self.temp = self.temp + key2 + ", "

    #  print(self.dictionary)
    #  print "total  number of words in first file: ", self.wordcount

    # if self.periodcount > 0: #possible divide by 0 error
    #     print "total number of sentences in first file: ", self.periodcount
    # else:
    #     print "no periods, 0 sentences"
    # print "....................."

    #diff = set(self.dictionary.keys()) - set(other.dictionary.keys())
    #check to see which words are in the first document and not in the second
    # diff2 = set(other.dictionary.keys()) - set(self.dictionary.keys())
    #check to see which words are in the second document and not in the first

    # if diff or diff2:
    #     if diff:
    #         print "Word(s) in first file not in second file: ", ", ".join(str(e) for e in diff)
    #         print "...................."
    #        print "first file was written by a different author than second file"
    #    if diff2:
    #        print "Word(s) in second file, not in first file: ", ", ".join(str(e) for e in diff2)
    #        print "...................."
    #        print "second file was written by a different author than first file"
    #else:
    #    print "both documents are similar, as they use identical words"



    #if abs(lettercount['n']/totalletters - lettercount2['n']/totalletters2) <= allowed_error:
    #   print "both documents have similar frequencies of letters"

    #tuple of tuples big tuple to store the two outputs which are pairs dog 30, dog 25
    #for key, value in dictionary.iteritems():
    #   val2 = dictionary2[key]

    #look into what punctuation is
    #conversion to object oriented design complete. Saturday december 3 5:00 am
    #next step: tweaking tolerances maybe helper methods

if __name__ == '__main__':
    doc = DocumentAnalzyer()
    doc2 = DocumentAnalzyer()

    doc.fileprocessing()
    #print doc.dictionary
    #doc2.fileprocessing("test2")
    #print doc2.dictionary
    #doc.calculations(doc2)
    #doc2.calculations(doc)
