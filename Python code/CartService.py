import os
from pymongo import MongoClient
from bson.objectid import ObjectId
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

collection = db["Cart"]  # Using your exact collection name

print("=== STEP 1: Connection Established ===")
print(f"Database: {db.name}, Collection: {collection.name}")
print("-------------------------------------")

def check_cart(cart_id):
    print("\n=== STEP 2: Checking Cart Existence ===")
    print(f"Looking for cart with _id: {cart_id}")
    
    cart = collection.find_one({"_id": cart_id})
    
    if not cart:
        available_carts = list(collection.find({}, {"_id": 1}))
        print("❌ Cart not found. Available carts:")
        for c in available_carts:
            print(f" - {c['_id']}")
        raise ValueError(f"Cart {cart_id} not found")
    else:
        print("✅ Cart found:")
        print(cart)
        return cart

# Test with your cart ID
cart_id = "Cart20"  # Use the exact _id from your database
cart = check_cart(cart_id)
print("-------------------------------------")

def update_cart_with_lock(cart_id, updates, expected_version):
    print("\n=== STEP 3: Attempting Update ===")
    print(f"🔒 Lock check: Version must be {expected_version}")
    print(f"📝 Proposed changes: {updates}")
    
    result = collection.update_one(
        {"_id": cart_id, "version": expected_version},
        {"$set": updates, "$inc": {"version": 1}}
    )
    
    if result.matched_count == 0:
        print("❌ Conflict detected! Version mismatch or cart modified by another process.")
        current = collection.find_one({"_id": cart_id})
        print(f"📌 Current version in DB: {current['version']}")
        raise ValueError("Optimistic lock failed")
    else:
        print("✅ Update successful! New version:", expected_version + 1)
    print("-------------------------------------")

def change_quantity(cart_id, product_id, new_quantity):
    print("\n" + "="*50)
    print("🚀 Starting change_quantity operation")
    print(f"🛒 Cart: {cart_id}, Product: {product_id}, New Qty: {new_quantity}")
    
    # Step 1: Get current cart
    cart = check_cart(cart_id)
    
    # Step 2: Find product position
    item_index = next(
        (i for i, item in enumerate(cart["items"]) 
        if item["product_id"] == product_id),
        None
    )
    
    if item_index is None:
        print(f"❌ Product {product_id} not found in cart items")
        raise ValueError("Product not in cart")
    
    print(f"📌 Found product at index {item_index}")
    
    # Step 3: Calculate new total (simplified example)
    old_quantity = cart["items"][item_index]["quantity"]
    price_per_unit = 1000  # Replace with actual price lookup
    new_total = cart["total_price"] + (new_quantity - old_quantity) * price_per_unit
    
    print(f"🧮 Updating total from {cart['total_price']} to {new_total}")
    
    # Step 4: Apply update
    try:
        update_cart_with_lock(
            cart_id=cart_id,
            updates={
                f"items.{item_index}.quantity": new_quantity,
                "total_price": new_total
            },
            expected_version=cart["version"]
        )
    except ValueError as e:
        print("💥 Update failed:", str(e))
        raise

# Example execution
try:
    change_quantity(
        cart_id="Cart20",
        product_id="Product30",
        new_quantity=3
    )
except Exception as e:
    print("\n🔴 Final result: FAILED -", str(e))
else:
    print("\n🟢 Final result: SUCCESS")
print("="*50)