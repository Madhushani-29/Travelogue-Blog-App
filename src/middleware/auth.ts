import { auth } from "express-oauth2-jwt-bearer";
import { Request, Response, NextFunction } from "express";
import jwt from "jsonwebtoken";
import User from "../models/user";

//declare a global augmentation to the Request object in the Express namespace
//it adds two properties, userId and auth0Id, to the Request object,
//indicating that TypeScript should recognize
//these properties as available on all Request objects throughout the application.
declare global {
  namespace Express {
    interface Request {
      userID: string;
      auth0ID: string;
    }
  }
}

//focused on standard JWT validation based on configuration parameters
export const jwtCheck = auth({
  audience: process.env.AUTH0_AUDIENCE,
  issuerBaseURL: process.env.AUTH0_ISSUER_BASE_URL,
  tokenSigningAlg: "RS256",
});

export const jwtParse = async (
  req: Request,
  res: Response,
  next: NextFunction
) => {
  //extracts the authorization header from the request's headers
  const { authorization } = req.headers;

  if (!authorization || !authorization.startsWith("Bearer ")) {
    return res.sendStatus(401);
  }

  // Bearer lshdflshdjkhvjkshdjkvh34h5k3h54jkh
  const token = authorization.split(" ")[1];

  try {
    //decode the token and extract information from its payload
    //tells the TypeScript compiler to treat the result of jwt.decode(token) as a specific type, jwt.JwtPayload
    //this helps TypeScript understand the structure of the decoded object and provides type safety when accessing its properties.
    const decoded = jwt.decode(token) as jwt.JwtPayload;
    //decoded is assumed to be an object representing the decoded payload of the JWT token
    //decoded.sub part accesses the sub claim within the decoded JWT payload
    const auth0ID = decoded.sub;

    //need to send the property name as in the data base
    //not id, ID since ID in the mongo
    const user = await User.findOne({ auth0ID });

    if (!user) {
      console.error("User not found for auth0Id:", auth0ID);
      return res.sendStatus(401);
    }

    req.auth0ID = auth0ID as string;
    req.userID = user._id.toString();
    next();
  } catch (error) {
    return res.sendStatus(401);
  }
};
