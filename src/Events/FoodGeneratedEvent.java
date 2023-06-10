package Events;

import Enums.FoodType;

import java.util.EventObject;

public class FoodGeneratedEvent extends EventObject {
    private final FoodType foodType;
    public FoodGeneratedEvent(Object source,FoodType foodType) {
        super(source);
        this.foodType = foodType;
    }

    public FoodType getFoodType() {
        return foodType;
    }
}
