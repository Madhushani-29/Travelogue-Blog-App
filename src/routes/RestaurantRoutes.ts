import express from "express";
import multer from "multer";
import { validateMyRestaurantRequest } from "../middleware/validation";
import { jwtCheck, jwtParse } from "../middleware/auth";
import RestaurantController from "../controller/RestaurantController";
import { param } from "express-validator";

const router = express.Router();

router.get(
  "/:id",
  param("id")
    .isString()
    .trim()
    .notEmpty()
    .withMessage("Restaurant ID must be a valid string!"),
  RestaurantController.getRestaurantById
);

router.get(
  "/search/:city",
  //validate city param=>must be a valid string and not empty
  param("city")
    .isString()
    .trim()
    .notEmpty()
    .withMessage("City parameter must ve a valid string!"),
  RestaurantController.searchRestaurant
);

export default router;
