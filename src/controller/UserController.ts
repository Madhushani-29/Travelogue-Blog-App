import { Request, Response } from "express";
import User from "../models/user";

const getCurrentUser = async (req: Request, res: Response) => {
  try {
    const user = await User.findById(req.userID);

    if (!user) {
      return res.status(404).json({ message: "User not found" }).send();
    }

    res.status(200).json(user);
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Error finding the user" });
  }
};

const createCurrentUser = async (req: Request, res: Response) => {
  // 1- check the user availability
  // 2- create user if it is not created
  // 3- send the created user as a response
  try {
    const { auth0ID } = req.body;
    const existingUser = await User.findOne({ auth0ID });

    if (existingUser) {
      // Send a 200 OK response and terminate the function
      return res.status(200).json({ message: "User already exists" }).send();
    }

    // Creating a new user object with the request body
    const newUser = new User(req.body);
    // Saving the new user to the database
    await newUser.save();
    // Send a 201 Created response with the newly created user object
    res.status(201).json(newUser.toObject());
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Error creating user" });
  }
};

const updateCurrentUser = async (req: Request, res: Response) => {
  try {
    const user = await User.findById(req.userID);

    if (!user) {
      return res.status(404).json({ message: "User not found" }).send();
    }

    const updatedUser = await User.findByIdAndUpdate(user._id, req.body, {
      new: true,
    });

    res.status(201).json(updatedUser);
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Error updating user" });
  }
};

export default {
  getCurrentUser,
  createCurrentUser,
  updateCurrentUser,
};
