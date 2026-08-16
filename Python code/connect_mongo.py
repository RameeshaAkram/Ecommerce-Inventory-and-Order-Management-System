import os
from pymongo import MongoClient

# Read MongoDB URI from environment variable (never hardcode credentials)
uri = os.getenv("MONGODB_URI")
if not uri:
    raise EnvironmentError(
        "MONGODB_URI environment variable is not set. "
        "Please set it before running this script."
    )

# Initialize the client
client = MongoClient(uri)

# Connect to your database (replace 'your_database_name' with your actual DB name)
db = client["EcommerceInventoryManagment"]

# --- Optional: Verify replica set support ---

# Check if the client is connected to a mongos (sharded cluster)
print("Is mongos:", client.is_mongos)  # Should be False for non-sharded replica sets

# Run the ismaster command to get replica set info
try:
    ismaster = client.admin.command("ismaster")
    print("Replica set name:", ismaster.get("setName"))
except Exception as e:
    print("Error checking replica set:", e)