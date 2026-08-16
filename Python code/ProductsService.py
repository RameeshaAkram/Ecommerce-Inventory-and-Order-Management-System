import os
from pymongo import MongoClient
from bson.objectid import ObjectId


def get_mongo_uri():
    uri = os.getenv("MONGO_URI")
    return uri.strip() if uri and uri.strip() else "mongodb://localhost:27017"


def get_database_name():
    db_name = os.getenv("MONGO_DATABASE")
    return db_name.strip() if db_name and db_name.strip() else "EcommerceInventoryManagment"


uri = get_mongo_uri()
client = MongoClient(uri)
db = client[get_database_name()]

products = db.Products  # Your exact collection name

def update_product(product_id, updates, expected_version):
    """
    Optimistic lock update for Products
    Args:
        product_id: "Product19" (string)
        updates: {"stock": 5} or {"price": 360000}
        expected_version: The version you last read (e.g., 1)
    """
    print(f"\n🔧 Attempting to update Product: {product_id}")
    print(f"   Proposed changes: {updates}")
    print(f"   Expected version: {expected_version}")
    
    result = products.update_one(
        {
            "_id": product_id,
            "version": expected_version,
            "is_deleted": False  # Prevent updates to deleted products
        },
        {
            "$set": updates,
            "$inc": {"version": 1}
        }
    )
    
    if result.matched_count == 0:
        current = products.find_one({"_id": product_id})
        if not current:
            print("❌ Error: Product not found or may be deleted")
        elif current["is_deleted"]:
            print("❌ Error: Product is deleted - updates blocked")
        else:
            print(f"❌ Conflict! Current version: {current['version']}")
        return False
    else:
        print("✅ Update successful!")
        return True
    
def update_stock(product_id, quantity_change):
    # Get current product state
    product = products.find_one({"_id": product_id})
    if not product:
        print(f"Product {product_id} not found")
        return
    
    print(f"\n📦 Current stock: {product['stock']}")
    new_stock = product["stock"] + quantity_change
    
    if new_stock < 0:
        print("❌ Insufficient stock!")
        return
    
    success = update_product(
        product_id=product_id,
        updates={"stock": new_stock},
        expected_version=product["version"]
    )
    
    if success:
        print(f"   New stock: {new_stock}")

# Example Usage:
update_stock("Product19", -2)  # Sell 2 units

def update_price(product_id, new_price):
    product = products.find_one({"_id": product_id})
    if not product:
        print(f"Product {product_id} not found")
        return
    
    print(f"\n💰 Current price: {product['price']}")
    
    success = update_product(
        product_id=product_id,
        updates={"price": new_price},
        expected_version=product["version"]
    )
    
    if success:
        print(f"   New price: {new_price}")

# Example Usage:
update_price("Product19", 360000)  # Price increase

# User A reads product (version=2)
product = products.find_one({"_id": "Product19"})

# User B updates first (version→3)
update_stock("Product19", -1)  

# User A tries to update with stale version=2
update_product(
    product_id="Product19",
    updates={"stock": 10},
    expected_version=2  # Stale version
)

# Initialize (run once)
products.update_one(
    {"_id": "Product19"},
    {"$set": {"version": 1}},
    upsert=True
)

# Normal flow
print("=== BEFORE UPDATE ===")
print(products.find_one({"_id": "Product19"}))

update_stock("Product19", -3)  # Sell 3 units
update_price("Product19", 370000)

print("\n=== AFTER UPDATE ===")
print(products.find_one({"_id": "Product19"}))