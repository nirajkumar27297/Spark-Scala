
import sys
import numpy as np
import pandas as pd
import pickle
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
import sklearn.metrics as metrics
import boto3
import os

inputFile = sys.stdin
inputStockPriceDF = pd.read_csv(inputFile, header=None)
inputStockPriceDF.columns= ["Date","Open","High","Low","Close","Adj Close","Volume"]
inputCols = inputStockPriceDF.drop(["Close","Adj Close","Volume","Date"],axis = 1)
outputCols = inputStockPriceDF["Close"]
lrModel = LinearRegression()
X_train,X_test,y_train,y_test = train_test_split(inputCols,outputCols,test_size = 0.2,random_state = 101)
lrModel.fit(X_train,y_train)
print("The R^2 value is ",lrModel.score(X_test,y_test))
coeff_df = pd.DataFrame(lrModel.coef_,inputCols.columns,columns=['Coefficient'])
print(coeff_df.head())
y_pred = lrModel.predict(X_test)
rmse = np.sqrt(metrics.mean_squared_error(y_test,y_pred))
print("The root mean square error is {:.2f}".format(rmse))
avgValue = np.mean(y_test)
print("The errorValue is {:.2f} %".format(rmse / avgValue ))

with open("/home/niraj/IdeaProjects/AWSSpark/src/test/resources/StockPriceModel.pkl","wb") as file:
    pickle.dump(lrModel,file)

s3 = boto3.client('s3', aws_access_key_id = os.environ.get("AWS_ACCESS_KEY_ID"),
                  aws_secret_access_key = os.environ.get("AWS_SECRET_ACCESS_KEY"))

s3.upload_file("StockPriceModel.pkl","getstocksdata", 'StockPriceModel.pkl')





