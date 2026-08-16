import os
from pymongo import WriteConcern, MongoClient
from pymongo.read_concern import ReadConcern
from pymongo.read_preferences import ReadPreference
from pymongo.errors import PyMongoError
from datetime import datetime, UTC  # Modern timezone-aware UTC


def get_mongo_uri():
    uri = os.getenv("MONGO_URI")
    return uri.strip() if uri and uri.strip() else "mongodb://localhost:27017"


def get_database_name():
    db_name = os.getenv("MONGO_DATABASE")
    return db_name.strip() if db_name and db_name.strip() else "EcommerceInventoryManagment"


uri = get_mongo_uri()
client = MongoClient(uri)
db = client[get_database_name()]

def place_order(customer_id):
    with client.start_session() as session:
        try:
            with session.start_transaction(
                read_concern=ReadConcern("snapshot"),
                write_concern=WriteConcern("majority"),
                read_preference=ReadPreference.PRIMARY
            ):
                # 1. Fetch cart
                cart = db.Cart.find_one({"customer_id": customer_id}, session=session)
                if not cart or not cart.get("items"):
                    raise Exception("Cart is empty or invalid")

                # 2. Validate stock (atomic check)
                for item in cart["items"]:
                    product = db.Products.find_one(
                        {"_id": item["product_id"], "stock": {"$gte": item["quantity"]}},
                        session=session
                    )
                    if not product:
                        raise Exception(f"Insufficient stock for product {item['product_id']}")

                # 3. Create order
                order_data = {
                    "customer_id": customer_id,
                    "items": cart["items"],
                    "total_price": cart["total_price"],
                    "status": "Pending",
                    "order_date": datetime.now(UTC)
                }
                order_result = db.Orders.insert_one(order_data, session=session)
                print(f"Order created: {order_result.inserted_id}")

                # 4. Update stock
                for item in cart["items"]:
                    update_result = db.Products.update_one(
                        {"_id": item["product_id"]},
                        {"$inc": {"stock": -item["quantity"]}},
                        session=session
                    )
                    if update_result.modified_count != 1:
                        raise Exception(f"Stock update failed for {item['product_id']}")

                # 5. Create payment
                payment_data = {
                    "order_id": order_result.inserted_id,
                    "customer_id": customer_id,
                    "amount": cart["total_price"],
                    "payment_method": "Credit Card",
                    "status": "Paid",
                    "transaction_date": datetime.now(UTC)
                }
                db.Payments.insert_one(payment_data, session=session)

                # 6. Clear cart (CRITICAL FIX: Using your exact collection name 'Cart')
                cart_update = db.Cart.update_one(
                    {"customer_id": customer_id},
                    {"$set": {"items": [], "total_price": 0}},
                    session=session
                )
                
                if cart_update.modified_count != 1:
                    raise Exception("Cart clearing failed - document not found or not modified")

                session.commit_transaction()
                print(f"✅ Transaction completed. Order: {order_result.inserted_id}")
                return order_result.inserted_id

        except PyMongoError as e:
            print("❌ MongoDB error:", str(e))
            session.abort_transaction()
            raise
        except Exception as e:
            print("❌ Transaction failed:", str(e))
            session.abort_transaction()
            raise

# Usage
try:
    placed_order_id = place_order("Customer1")
    print(f"Success! Order ID: {placed_order_id}")
except Exception as e:
    print("Fatal error:", str(e))