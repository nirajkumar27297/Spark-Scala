"""
The objective is to take input from a Amazon S3 resource and call machine learning python file
and save the trained model to S3 using Spark Scala.
Library Used -
1> pandas [For Creating DataFrames]
  Version - 1.1.2
2> pickle [For Dumping Model]
  Version - 3.8
3> Sklearn [For Machine Learning Algorithms]
  Version - 0.23.2
4> boto3 [For Uploading to S3]
    Version -1.14.60
@author : Niraj Kumar
"""

import sys
import numpy as np
import pandas as pd
import pickle
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
import sklearn.metrics as metrics
import boto3
import os

"""
Function to Upload File to S3
@:param LinearRegressionModel
"""
def uploadFileToS3(lrModel):
    try:
        with open("/home/niraj/IdeaProjects/AWSSpark/src/test/resources/StockPriceModel.pkl", "wb") as file:
            pickle.dump(lrModel, file)

        bucketName = sys.argv[1]

        s3 = boto3.client('s3', aws_access_key_id=os.environ.get("AWS_ACCESS_KEY_ID"),
                          aws_secret_access_key=os.environ.get("AWS_SECRET_ACCESS_KEY"))

        s3.upload_file("StockPriceModel.pkl", "getstocksdata", 'StockPriceModel.pkl')

        print("The files has been uploaded to S3 with bucket name is ", bucketName)

    except FileNotFoundError:
        print("The file was not found")
    except NoCredentialsError:
        print("Credentials not available")



#Taking input from Spark's RDD through Pipe
inputFile = sys.stdin
#Creating a DataFrame from the inputFile
inputStockPriceDF = pd.read_csv(inputFile, header=None)
#Assigning Column Names
inputStockPriceDF.columns= ["Date","Open","High","Low","Close","Adj Close","Volume"]
#Taking Only the Needed Input Columns
inputCols = inputStockPriceDF.drop(["Close","Adj Close","Volume","Date"],axis = 1)
#Separating Output Column
outputCol = inputStockPriceDF["Close"]
#Creating a LinearRegression Model
lrModel = LinearRegression()
#Splitting the data in for one training our model and other to test
X_train,X_test,y_train,y_test = train_test_split(inputCols,outputCol,test_size = 0.2,random_state = 101)
#Fitting our model on our training set
lrModel.fit(X_train,y_train)
#Printing the R^2 [Goodness of Fit] for our test data
print("The R^2 value is ",lrModel.score(X_test,y_test))
#GPrinting the coefficients of parameters
coeff_df = pd.DataFrame(lrModel.coef_,inputCols.columns,columns=['Coefficient'])
print(coeff_df.head())
#Predicting the result for our test data
y_pred = lrModel.predict(X_test)
#Calculating and printing Root Mean Square Error and Error Value
rmse = np.sqrt(metrics.mean_squared_error(y_test,y_pred))
print("The root mean square error is {:.2f}".format(rmse))
avgValue = np.mean(y_test)
print("The errorValue is {:.2f} %".format(rmse / avgValue ))
#Calling function to upload model to S3
uploadFileToS3(lrModel)









