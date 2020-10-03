
import sys
import numpy as np
import pandas as pd
import pickleStockPredictionPythonConnectivityStockPredictionPythonConnectivity
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
import sklearn.metrics as metrics
from sklearn.pipeline import Pipeline
import boto3

inputFile = sys.stdin
inputDF = pd.read_csv(inputFile,header=None)
inputDF.columns= ["Date","Open","High","Low","Close","Adj Close","Volume"]
inputCols = inputDF.drop(["Close","Adj Close","Volume","Date"],axis = 1)
outputCols = inputDF["Close"]
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
bucket='stockpredictions'
key='StockPrice.pkl'
pickle_byte_obj = pickle.dumps(lrModel)
s3_resource = boto3.resource('s3')
s3_resource.Object(bucket,key).put(Body=pickle_byte_obj)




