package Events;

import Enums.FoodType;

import java.util.EventObject;

public class FoodEatenEvent extends EventObject {
    FoodType foodType;
    public FoodEatenEvent(Object source,FoodType foodType) {
        super(source);
        this.foodType = foodType;
    }

    public FoodType getFoodType() {
        return foodType;
    }
}
