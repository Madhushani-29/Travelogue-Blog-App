import mongoose from "mongoose";
import "dotenv/config";

const connectDB = async () => {
  try {
    const connectionString = process.env.MONGODB_CONNECTION_STRING;
    if (connectionString) {
      //asynchronous JavaScript code to pause the execution of a function until a promise is resolved or rejected
      const connect = await mongoose.connect(connectionString);
      console.log(
        "Database Connected !",
        connect.connection.host,
        connect.connection.name
      );
    } else {
      console.log("Cannot find the connection string !");
    }
  } catch (err) {
    console.log(err);
    //if there is an error exit the process
    process.exit(1);
  }
};

export default connectDB;
