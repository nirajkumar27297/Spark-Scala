"""
The objective is to take input from a standard Input,create a dataframe from that
Load the LinearRegressionModel which we have dumped earlier and predict the Close Price
and return it using Standard Output
Library Used -
1> pandas [For Creating DataFrames]
  Version - 1.1.2
2> pickle [For Dumping Model]
  Version - 3.8
3> sys [For taking Standard Input and Passing Output Through Standard Output]
@author : Niraj Kumar
"""


import sys
import pandas as pd
import pickle

#Taking Input Through Standard Input
inputStockPriceFile = sys.stdin
#Creating dataframe from standard input and transposing it
inputStockPriceDF = pd.read_csv(inputStockPriceFile, header=None).transpose()
#Providing name to the columns
inputStockPriceDF.columns = {"Open","High","Low","Volume"}
#Loading the dumped LinearRegressionModel pickle file
with open("./app/MachineLearningModel/PythonModel/StockPriceModel.pkl","rb") as modelFile:
    linearRegressionModel = pickle.load(modelFile)

#Predicting the stock close price
predictedClosePrice = linearRegressionModel.predict(inputStockPriceDF)
#Returning the output through standard output
sys.stdout.write(str(predictedClosePrice[0]))
