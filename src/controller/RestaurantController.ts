import { Request, Response } from "express";
import Restaurant from "../models/restaurant";

const getRestaurantById = async (req: Request, res: Response) => {
  try {
    const id = req.params.id;
    const restaurant = await Restaurant.findById(id);

    if (!restaurant) {
      return res.status(404).json({ message: "Restaurant not found" }).send();
    }

    res.status(200).json(restaurant);
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Something went wrong!" });
  }
};

const searchRestaurant = async (req: Request, res: Response) => {
  try {
    // path params/ search query come with the url
    // required something(main search) for the criteria like ids and search words
    // as the main search we use city
    const city = req.params.city;

    // query params
    // typically follow the "?" character in a URL and consist of key-value pairs separated by "&" symbols
    // any filtering, sorting and any other key search terms
    // to search data with city param we use another search queries like cuisine type
    const searchQuery = (req.query.searchQuery as string) || "";
    const selectedCuisines = (req.query.selectedCuisines as string) || "";
    const sortOption = (req.query.sortOption as string) || "lastUpdated";
    const page = parseInt(req.query.page as string) || 1;

    // type any query
    // query is an object used to build a MongoDB query
    let query: any = {};

    // assigns a value to the city key in the query object
    // It represents the field in the database documents that we want to match against
    // city: variable that holds the name of the city obtained from the request parameters.
    // i": flag that is passed to the RegExp constructor stands for "case-insensitive"
    // regular expression will match the city name regardless of whether it's written in uppercase or lowercase.
    // Sri Lanka = sri lanka = Sri lanka
    query["city"] = new RegExp(city, "i");
    const cityCheck = await Restaurant.countDocuments(query);
    if (cityCheck === 0) {
      return res.status(404).json({
        data: [],
        pagination: {
          total: 0,
          page: 1,
          pages: 1,
        },
      });
    }

    // splits the selectedCuisines string into an array of individual cuisine names
    // using the split() method, which divides the string at each comma (,) into separate cuisine
    // selectedCuisines is a comma separated string
    // URL=SelectedCuisines=italian, burger ==> selectedCuisines=[italian, burger]
    if (selectedCuisines) {
      const cuisinesArray = selectedCuisines
        .split(",")
        .map((cuisine) => new RegExp(cuisine, "i"));

      // retrieve all restaurants which have (special) all items in cuisine array in their cuisines
      query["cuisines"] = { $all: cuisinesArray };
    }

    if (searchQuery) {
      const searchRegex = new RegExp(searchQuery, "i");
      // retrieve all restaurants which have (special) the restaurant name or any cuisines from the list match to them
      query["$or"] = [
        { restaurantName: searchRegex },
        { cuisines: { $in: [searchRegex] } },
      ];
    }

    // if currently in page 3 to need to display the retrieve items from 20-30
    // since 1-10 display in first page and 11-20 display in second
    // skip=(3-1)*10=20
    const pageSize = 10;
    const skip = (page - 1) * pageSize;

    const restaurants = await Restaurant.find(query)
      .sort({ [sortOption]: 1 })
      .skip(skip)
      .limit(pageSize)
      //lean(): tells Mongoose to return plain JavaScript objects instead of Mongoose documents
      //lean() can improve performance because it skips the overhead of converting Mongoose documents into plain JavaScript objects./
      .lean();

    //get the document count
    const total = await Restaurant.countDocuments(query);

    const response = {
      data: restaurants,
      pagination: {
        total,
        page,
        // calculates the total number of pages needed for pagination based on the total number of items (total) and the number of items per page (pageSize)
        // The Math.ceil() function is used to round up to the nearest integer,
        pages: Math.ceil(total / pageSize),
      },
    };

    res.json(response);
  } catch (error) {
    console.log(error);
    res.status(500).json({ message: "Something went wrong!" });
  }
};

export default {
  searchRestaurant,
  getRestaurantById,
};
