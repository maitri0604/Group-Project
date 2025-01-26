import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
import joblib

# Load the data
df = pd.read_csv('student_data.csv')

# Define the target variable
df['at_risk'] = df.apply(lambda row: (row['avg_assignment_marks'] < 35) or (row['avg_quiz_marks'] < 35), axis=1)

# Features and target
X = df[['avg_assignment_marks', 'avg_quiz_marks']]
y = df['at_risk']

# Split the data
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

# Train the model
model = RandomForestClassifier()
model.fit(X_train, y_train)

# Save the model
joblib.dump(model, 'at_risk_model.pkl')
print("Model successfully trained and saved to at_risk_model.pkl")