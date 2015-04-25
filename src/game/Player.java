package game;

import java.util.HashMap;

public class Player {

    private HashMap<String, Boolean> unlockedAbilites=new HashMap<String, Boolean>();

    public Player() {
        //add all abilities
        unlockedAbilites.put("kick", true);
    }
    
    public boolean hasAbility(String name){
        return unlockedAbilites.get(name);
    }
    
    public void unlockAbility(String name){
        unlockedAbilites.put(name,true);
    }

}
