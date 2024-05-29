import express from "express";
import MyRestaurantController from "../controller/MyRestaurantController";
import multer from "multer";
import { validateMyRestaurantRequest } from "../middleware/validation";
import { jwtCheck, jwtParse } from "../middleware/auth";

const router = express.Router();

//creates a storage engine that stores uploaded files in memory (RAM) as Buffer objects
//the uploaded files are not stored on the disk but remain in memory
//can be useful for handling small files or cases where you don't want to save files to disk.
const storage = multer.memoryStorage();
const upload = multer({
  //specifies the storage engine to be used for handling file storage
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024, //5mb
  },
});

router.get(
  "/",
  jwtCheck,
  jwtParse,
  MyRestaurantController.getCurrentRestaurant
);

router.post(
  "/",
  /*check the body for 'imageFile property and validate it with above also and 
  if there are any validations, it will send a error message to FE, 
  then it will append images to a file object can use in controller function (req.file or req.files)' */
  upload.single("imageFile"),
  jwtCheck,
  jwtParse,
  validateMyRestaurantRequest,
  MyRestaurantController.createRestaurant
);

router.put(
  "/",
  upload.single("imageFile"),
  jwtCheck,
  jwtParse,
  validateMyRestaurantRequest,
  MyRestaurantController.updateRestaurant
);

router.get(
  "/orders",
  jwtCheck,
  jwtParse,
  MyRestaurantController.getMyRestaurantOrders
);

router.patch(
  "/order/:orderID/status",
  jwtCheck,
  jwtParse,
  MyRestaurantController.updateMyRestaurantStatus
);
export default router;
