from pymongo import MongoClient

# Replace with your actual connection string
uri = "mongodb+srv://admin:urWatulWusqa087@cluster0.u7rda.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0"

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