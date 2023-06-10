package InterfaceLink;

import Events.FoodGeneratedEvent;
import Logic.FoodType;

public interface GenereteFoodListner {
    void generateFood(FoodGeneratedEvent foodGeneratedEvent);
}
