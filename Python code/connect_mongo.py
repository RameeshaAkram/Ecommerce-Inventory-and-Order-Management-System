import os
from pymongo import MongoClient


def get_mongo_uri():
    uri = os.getenv("MONGO_URI")
    return uri.strip() if uri and uri.strip() else "mongodb://localhost:27017"


def get_database_name():
    db_name = os.getenv("MONGO_DATABASE")
    return db_name.strip() if db_name and db_name.strip() else "EcommerceInventoryManagment"


uri = get_mongo_uri()
client = MongoClient(uri)
db = client[get_database_name()]

# --- Optional: Verify replica set support ---

# Check if the client is connected to a mongos (sharded cluster)
print("Is mongos:", client.is_mongos)  # Should be False for non-sharded replica sets

# Run the ismaster command to get replica set info
try:
    ismaster = client.admin.command("ismaster")
    print("Replica set name:", ismaster.get("setName"))
except Exception as e:
    print("Error checking replica set:", e)