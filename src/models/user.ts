import mongoose from "mongoose";

const userSchema = new mongoose.Schema({
  auth0ID: {
    type: String,
    required: true,
  },
  email: {
    type: String,
    required: true,
  },
  name: {
    type: String,
  },
  addressLine1: {
    type: String,
  },
  city: {
    type: String,
  },
  country: {
    type: String,
  },
});

//create a model named user using userschema
const User = mongoose.model("User", userSchema);

//export the user model
export default User;
