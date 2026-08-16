import os
from pymongo import MongoClient
from datetime import datetime, timedelta, timezone
import time

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


# Document IDs
ORDER_ID = "Order8"
PAYMENT_ID = "Payment7"
CUSTOMER_ID = "Customer8"
LOCK_TTL = 10  # Lock timeout in seconds

def acquire_lock(collection, doc_id, owner_id):
    """Attempt to acquire a pessimistic lock with timezone-aware timestamps"""
    print(f"\n[LOCK ACQUISITION] Attempting to lock {doc_id}...")
    
    # Get current document state
    before_state = collection.find_one({"_id": doc_id})
    print(f"  Before state: locked={before_state.get('locked')}, "
          f"owner={before_state.get('lock_owner')}, "
          f"expiry={before_state.get('lock_expiry')}")

    # Timezone-aware timestamp
    now = datetime.now(timezone.utc)
    lock_expiry = now + timedelta(seconds=LOCK_TTL)
    
    # Atomic locking operation
    result = collection.update_one(
        {
            "_id": doc_id,
            "$or": [
                {"locked": False},
                {"lock_expiry": {"$lt": now}}
            ]
        },
        {
            "$set": {
                "locked": True,
                "lock_owner": owner_id,
                "lock_expiry": lock_expiry
            }
        }
    )

    # Verify update in Compass
    after_state = collection.find_one({"_id": doc_id})
    
    if result.modified_count == 1:
        print(f"  ✓ SUCCESS! Lock acquired by {owner_id}")
        print(f"  After state: locked={after_state.get('locked')}, "
              f"owner={after_state.get('lock_owner')}, "
              f"expiry={after_state.get('lock_expiry')}")
        print("  ACTION: Refresh this document in MongoDB Compass now")
        return True
    else:
        print("  ✗ FAILED! Document already locked")
        print(f"  Current owner: {after_state.get('lock_owner')}")
        print(f"  Lock expires at: {after_state.get('lock_expiry')}")
        return False

def verify_in_compass(collection_name):
    """Helper for manual verification"""
    print(f"\nVERIFICATION IN COMPASS:")
    print(f"1. Open MongoDB Compass")
    print(f"2. Connect to 'ecommerce_db'")
    print(f"3. Go to '{collection_name}' collection")
    print(f"4. Find document with _id: {ORDER_ID if collection_name == 'orders' else PAYMENT_ID}")
    print(f"5. Check these fields:")
    print(f"   - locked (should be true)")
    print(f"   - lock_owner (should be {CUSTOMER_ID})")
    print(f"   - lock_expiry (future timestamp)")
    input("Press Enter after verification...")

# Payment processing example
def process_payment():
    print("\n=== STARTING PAYMENT PROCESS ===")
    
    # 1. Lock Order
    if acquire_lock(db.Orders, ORDER_ID, CUSTOMER_ID):
        verify_in_compass("orders")
        try:
            # 2. Lock Payment
            if acquire_lock(db.Payments, PAYMENT_ID, CUSTOMER_ID):
                verify_in_compass("payments")
                try:
                    # 3. Critical Section
                    print("\n[PROCESSING] Payment in progress...")
                    time.sleep(3)  # Simulate processing
                    
                    # Update documents
                    db.Payments.update_one(
                        {"_id": PAYMENT_ID},
                        {"$set": {"status": "Completed"}}
                    )
                    db.Orders.update_one(
                        {"_id": ORDER_ID},
                        {"$set": {"status": "Processed"}}
                    )
                    print("✓ Payment processed successfully")
                
                finally:
                    # 4. Release Payment Lock
                    db.Payments.update_one(
                        {"_id": PAYMENT_ID},
                        {"$set": {
                            "locked": False,
                            "lock_owner": None,
                            "lock_expiry": None
                        }}
                    )
                    print("✓ Payment lock released")
        
        finally:
            # 5. Release Order Lock
            db.Orders.update_one(
                {"_id": ORDER_ID},
                {"$set": {
                    "locked": False,
                    "lock_owner": None,
                    "lock_expiry": None
                }}
            )
            print("✓ Order lock released")

if __name__ == "__main__":
    # Reset documents first
    db.Orders.update_one(
        {"_id": ORDER_ID},
        {"$set": {
            "locked": False,
            "lock_owner": None,
            "lock_expiry": None,
            "status": "Pending"
        }}
    )
    db.Payments.update_one(
        {"_id": PAYMENT_ID},
        {"$set": {
            "locked": False,
            "lock_owner": None,
            "lock_expiry": None,
            "status": "Pending"
        }}
    )
    print("Documents reset for testing")
    
    process_payment()