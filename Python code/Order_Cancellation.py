import os
from pymongo import MongoClient, WriteConcern
from pymongo.read_concern import ReadConcern  # Correct import path
from pymongo.read_preferences import ReadPreference
from pymongo.errors import PyMongoError
from datetime import datetime, UTC
from bson import ObjectId
# Initialize MongoDB client
# Read MongoDB URI from environment variable (never hardcode credentials)
uri = os.getenv("MONGODB_URI")
if not uri:
    raise EnvironmentError(
        "MONGODB_URI environment variable is not set. "
        "Please set it before running this script."
    )

client = MongoClient(uri)
db = client["EcommerceInventoryManagment"]

def cancel_order(order_id):
    """
    Atomically cancels an order by:
    1. Marking order as 'Cancelled'
    2. Refunding payment
    3. Restocking products
    """
    with client.start_session() as session:
        try:
            # Start transaction explicitly
            session.start_transaction(
                read_concern=ReadConcern('snapshot'),
                write_concern=WriteConcern('majority'),
                read_preference=ReadPreference.PRIMARY
            )
            
            print("Transaction started successfully")  # Debug
            
            # 1. Validate order
            order = db.Orders.find_one(
                {"_id": ObjectId(order_id)},
                session=session
            )
            if not order:
                raise ValueError(f"Order {order_id} not found")
            
            if order.get("status") == "Cancelled":
                raise ValueError("Order already cancelled")

            # 2. Update order
            db.Orders.update_one(
                {"_id": ObjectId(order_id)},
                {"$set": {
                    "status": "Cancelled",
                    "cancelled_at": datetime.now(UTC)
                }},
                session=session
            )

            # 3. Refund payment
            payment_update = db.Payments.update_one(
                {"order_id": ObjectId(order_id)},
                {"$set": {
                    "status": "Refunded",
                    "refund_date": datetime.now(UTC)
                }},
                session=session
            )
            if payment_update.modified_count == 0:
                raise ValueError("Payment not found or already refunded")

            # 4. Restock products
            for item in order["items"]:
                db.Products.update_one(
                    {"_id": item["product_id"]},
                    {"$inc": {"stock": item["quantity"]}},
                    session=session
                )

            session.commit_transaction()
            print(f"✅ Order {order_id} cancelled successfully")
            return True

        except PyMongoError as e:
            print(f"❌ MongoDB error: {str(e)}")
            if session.in_transaction:
                session.abort_transaction()
            raise
        except Exception as e:
            print(f"❌ Transaction failed: {str(e)}")
            if session.in_transaction:
                session.abort_transaction()
            raise

# Example usage
try:
    # Test with a valid order ID from your database
    cancel_order("68373b6110be9a56c3035a5b")  # Replace with actual order ID
except Exception as e:
    print(f"Failed to cancel order: {str(e)}")